(ns pglogical.server
  (:require
   [clojure.string :as str]
   [org.httpkit.server :as http]
   [ring.util.codec]
   [pglogical.core :as pg]
   [clojure.pprint :as pprint]))

(defonce clients (atom #{}))

(defn on-message [raw-msg]
  (println "message " raw-msg))

(defn app [req]
  (http/with-channel req ch
    (println "Incomming connection" ch)
    (swap! clients conj ch)
    (http/on-receive ch #'on-message)
    (http/on-close ch (fn [_] (swap! clients disj ch)))))

(defonce server (atom nil))

(defn broad-cast [msg]
  (doseq [c @clients]
    (http/send! c msg)))

(defn runme []
  (pg/replication-connection
   {:uri "jdbc:postgresql://localhost:5777/test"
    :user "nicola"
    :slot "myslot"
    :decoder "wal2json" ;; test_decoding
    :password "postgres"}
   (fn [msg]
     (broad-cast msg))))

(defn restart []
  ;; todo validate config
  (when-let [s @server]
    (@server)
    (reset! server nil))
  (runme)
  (reset! server
          (http/run-server #'app
                               {:port 3333})))

(comment
  (restart)
  )
