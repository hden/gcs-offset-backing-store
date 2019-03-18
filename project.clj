(defproject gcs-offset-backing-store "0.1.1"
  :description "A Kafka Connect OffsetBackingStore backed by Google Cloud Storage."
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [com.google.cloud/google-cloud-storage "1.65.0"]
                 [org.apache.kafka/connect-runtime "2.1.1"]
                 [cheshire "5.8.1"]]
  :plugins [[lein-cloverage "1.1.1"]]
  :repl-options {:init-ns org.apache.connect.storage.gcs}
  :aot [org.apache.connect.storage.gcs]
  :profiles {:uberjar {:aot :all}})
