(ns org.apache.connect.storage.gcs-test
  (:require [clojure.test :refer :all]
            [org.apache.connect.storage.gcs :refer :all])
  (:import [java.nio ByteBuffer]
           [java.nio.charset StandardCharsets]
           [com.google.cloud WriteChannel]
           [com.google.cloud.storage Storage]
           [org.apache.kafka.connect.util Callback]
           [org.apache.kafka.common.config ConfigDef]
           [org.apache.kafka.connect.runtime WorkerConfig])
  (:gen-class))

(defn create-byte-buffer [s]
  (ByteBuffer/wrap (.getBytes s StandardCharsets/UTF_8)))

(defn decode-byte-buffer [x]
  (when x
    (String. (.array x))))

(defn create-storage [ref]
  (reify Storage
    (readAllBytes [this blob-id _]
      (.getBytes @ref StandardCharsets/UTF_8))
    (writer [this info options]
      (reify WriteChannel
        (isOpen [_])
        (close [_])
        (write [_ buffer]
          (reset! ref (String. (.array buffer)))
          1)))))

(defn create-callback [promise]
  (reify Callback
    (onCompletion [_ error result]
      (deliver promise {:error error :result result}))))

(deftest helpers
  (testing "string-from-byte-buffer & string-to-byte-buffer"
    (is (= (create-byte-buffer "foobar")
           (string-to-byte-buffer (string-from-byte-buffer (create-byte-buffer "foobar"))))))

  (testing "mak-kv"
    (is (= {"foo" "bar"} (map-kv {:foo "bar"} name))))

  (testing "read-blob"
    (let [storage (create-storage (atom "{\"foo\": \"bar\"}"))]
      (is (= {"foo" "bar"} (read-blob {:storage storage :bucket "bucket" :path "path"})))))

  (testing "write-blob!"
    (let [spy (atom "{\"foo\": \"bar\"}")
          storage (create-storage spy)]
      (write-blob! {:storage storage :bucket "bucket" :path "path"} {"baz" "nyan"})
      (is (= "{\"baz\":\"nyan\"}" @spy))))

  (testing "with-callback-success"
    (let [p (promise)
          callback (create-callback p)
          res (with-callback callback #(+ 1 1))]
      (is (= 2 res (:result @p)))))

  (testing "with-callback-error"
    (let [p (promise)
          ex (Exception. "foobar")
          callback (create-callback p)
          res (with-callback callback #(throw ex))]
      (are [x y] (= x y)
        nil res
        ex (:error @p)))))

(defn create-config [config]
  (proxy [WorkerConfig] [(ConfigDef.) {}]
    (originals []
      config)))

(defn create-instance [content]
  (let [instance (new org.apache.kafka.connect.storage.gcs.GCSOffsetBackingStore)
        storage (create-storage content)
        config (create-config {"offset.storage.gcs.bucket" "bucket"
                               "offset.storage.gcs.path" "path"})]
    (.configure instance config)
    (swap! (.state instance) assoc :storage storage)
    instance))

(deftest GCSOffsetBackingStore
  (testing "configure"
    (let [instance (create-instance (atom nil))
          state (.state instance)]
      (are [x y] (= (get @state x) y)
        :bucket "bucket"
        :path "path")))

  (testing "start"
    (let [instance (create-instance (atom "{\"Zm9v\":\"YmFy\"}"))
          state (.state instance)]
      (is (= {} (into {} (.getdata instance))))
      (.start instance)
      (is (= {"foo" "bar"}
             (map-kv (.getdata instance) decode-byte-buffer)))))

  (testing "read after write"
    (let [blob (atom "{}")
          instance (create-instance blob)
          state (.state instance)]
      (.start instance)
      @(.set instance {(create-byte-buffer "foo") (create-byte-buffer "bar")} nil)
      (is (= {"foo" "bar"} (map-kv (.getdata instance) decode-byte-buffer)))
      (is (= {"foo" "bar"}
             (map-kv @(.get instance [(create-byte-buffer "foo")] nil) decode-byte-buffer)))
      (is (= "{\"Zm9v\":\"YmFy\"}" @blob)))))
