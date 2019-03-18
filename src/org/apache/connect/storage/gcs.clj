(ns org.apache.connect.storage.gcs
  (:require [cheshire.core :as cheshire])
  (:import [java.util Base64]
           [java.nio ByteBuffer]
           [java.nio.charset StandardCharsets]
           [org.apache.kafka.connect.storage OffsetBackingStore]
           [com.google.cloud.storage BlobId
                                     BlobInfo
                                     StorageOptions
                                     Storage$BlobWriteOption
                                     Storage$BlobSourceOption])
  (:gen-class
    :name org.apache.kafka.connect.storage.gcs.GCSOffsetBackingStore
    :extends org.apache.kafka.connect.storage.MemoryOffsetBackingStore
    :exposes {data {:get getdata :set setdata}}
    :exposes-methods {start superstart
                      configure superconfigure}
    :init init
    :state state))

(def ^:private encoder (Base64/getEncoder))
(def ^:private decoder (Base64/getDecoder))

(defn string-from-byte-buffer [x]
  "Encode a bytebuffer as Base64 encoded string."
  (let [base64-buffer (.encode encoder (ByteBuffer/wrap (.array x)))]
    (String. (.array base64-buffer))))

(defn string-to-byte-buffer [x]
  "Decode a Base64 encoded string as bytebuffer."
  (ByteBuffer/wrap (.decode decoder x)))

(defn map-kv [m f]
  (into {}
        (map (fn [[k v]]
               [(f k) (f v)]))
        m))

(defn create-blob-id [bucket path]
  (BlobId/of bucket path))

(defn create-blob-info [bucket path]
  (let [blob-id (create-blob-id bucket path)
        builder (BlobInfo/newBuilder blob-id)]
    (doto builder
      (.setContentType "application/json"))
    (.build builder)))

(defn create-blob-source-options [& options]
  (into-array Storage$BlobSourceOption options))

(defn create-blob-write-options [& options]
  (into-array Storage$BlobWriteOption options))

(defn read-blob [{:keys [storage bucket path]}]
  (let [blob-id (create-blob-id bucket path)]
    (cheshire/parse-string (String. (.readAllBytes storage blob-id (create-blob-source-options))))))

(defn write-blob! [{:keys [storage bucket path]} offsets]
  (let [blob-info (create-blob-info bucket path)
        options (create-blob-write-options)
        content (cheshire/generate-string offsets)]
    (with-open [writer (.writer storage blob-info options)]
      (.write writer (ByteBuffer/wrap (.getBytes content StandardCharsets/UTF_8))))))

(defn with-callback [callback f]
  (try
    (let [res (f)]
      (when callback
        (.onCompletion callback nil res))
      res)
    (catch Exception e
      (when callback
        (.onCompletion callback e nil)))))

(defn -init []
  [[] (atom {})])

(defn -configure [this config]
  (.superconfigure this config)
  (let [state (.state this)
        bucket (.getString config "offset.storage.gcs.bucket")
        path (.getString config "offset.storage.gcs.path")
        storage (.getService (StorageOptions/getDefaultInstance))]
    (swap! state merge {:bucket bucket :path path :storage storage})))

(defn -start [this]
  (.superstart this)
  (let [state (.state this)
        data (read-blob @state)]
    (.setdata this (java.util.HashMap. (map-kv data string-to-byte-buffer)))))

(defn -save [this]
  (let [state (.state this)
        data (map-kv (.getdata this) string-from-byte-buffer)]
    (write-blob! @state data)))
