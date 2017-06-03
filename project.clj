(defproject pglogical "0.0.1-RC1"
  :description ""
  :url "http://github.com/healthsamurai/sansara/proto"
  :dependencies [[org.clojure/clojure "1.9.0-alpha16"]
                 [ch.qos.logback/logback-classic "1.2.2"]
                 [clj-pg "0.0.3"]
                 [json-schema "0.2.0-RC2"]
                 [matcho "0.1.0-RC5"]
                 [org.clojure/java.jdbc "0.6.1"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.postgresql/postgresql "42.1.1"]

                 [crypto-password "0.2.0"]

                 ;; formats
                 [clj-yaml "0.4.0"]
                 [com.cognitect/transit-clj "0.8.300"]
                 [cheshire "5.6.3"]

                 ;; http stack
                 [route-map "0.0.4"]
                 [http-kit "2.2.0"]
                 [ring "1.5.1"]
                 [ring/ring-defaults "0.2.3"]])
