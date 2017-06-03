(ns pglogical.core
  (:require [honeysql.core :as hsql]
            [cheshire.core :as json]
            [clojure.string :as str]
            [clojure.set]
            [clj-pg.honey :as db]
            [clj-pg.pool :as pool]
            [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log])
  (:import [java.sql DriverManager]
           [org.postgresql PGConnection PGProperty]))

(defonce state (atom {}))

(defn close-replication-connection []
  (when-let  [conn (get @state :repl-conn)]
    (.close conn)
    (swap! state dissoc :repl-conn)))

(defn replication-connection
  [{uri :uri usr :user pwd :password slot-name :slot decod :decoder} cb]
  (close-replication-connection)

  (def pr (java.util.Properties.))

  (.set PGProperty/USER pr usr)
  (.set PGProperty/REPLICATION pr "database")
  (.set PGProperty/PASSWORD pr pwd)
  (.set PGProperty/PREFER_QUERY_MODE pr "simple")
  (.set PGProperty/ASSUME_MIN_SERVER_VERSION pr "9.5")

  (let [slot-name (or slot-name "test_slot")
        conn (-> (DriverManager/getConnection uri pr)
                 (.unwrap PGConnection))
        _  (try
             (-> conn
                 (.prepareStatement (str "DROP_REPLICATION_SLOT " slot-name))
                 (.execute))
             (catch Exception e
               (println e)))

        slot (.. conn
                 (getReplicationAPI)
                 (createReplicationSlot)
                 (logical)
                 (withSlotName slot-name)
                 (withOutputPlugin (or decod "wal2json"))
                 (make))

        stream (.. conn
                   (getReplicationAPI)
                   (replicationStream)
                   (logical)
                   (withSlotName "jdbc_slot_json")
                   (withSlotOption "include-xids", false)
                   (start))]

    (swap! state assoc :repl-conn conn :slot slot-name)
    (future
      (loop []
        (let [msg (.read stream)
              src (.array msg)
              off (.arrayOffset msg)
              lsn (.getLastReceiveLSN stream)]
          (cb (String. src  off  (- (count src) off)))
          (println "LSN:" (str lsn))
          (.setAppliedLSN stream lsn)
          (.setFlushedLSN stream lsn))
        (recur)))))

(defn runme []
  (replication-connection
   {:uri "jdbc:postgresql://localhost:5777/test"
    :user "nicola"
    :slot "myslot"
    :decoder "wal2json" ;; test_decoding
    :password "postgres"}
   println)

  )

(runme)

;; (db/exec! {:connection replConn} "IDENTIFY_SYSTEM")

;; PGConnection replConnection = con.unwrap(PGConnection.class);


(comment
  (db/query {:connection conn} "SELECT * FROM pg_logical_slot_get_changes('jdbc_slot', NULL, NULL);")
  (db/query {:connection conn} "SELECT 1")

  (.close replConn)

  (.. replConn
      (getReplicationAPI)
      (createReplicationSlot)
      (logical)
      (withSlotName "jdbc_slot_json")
      (withOutputPlugin "wal2json")
      (make))

  (def stream
    (.. replConn
        (getReplicationAPI)
        (replicationStream)
        (logical)
        (withSlotName "jdbc_slot_json")
        (withSlotOption "include-xids", false)
        (start)))


  (def stop (atom false))

  (def msg (.read stream))

  
  (loop []
    (let [msg (.read stream)
          src (.array msg)
          off (.arrayOffset msg)]
      (println (String. src  off  (- (count src) off))))
    (recur))

  )
