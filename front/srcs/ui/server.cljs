(ns ui.server 
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [clojure.string :as str]))


(defonce state (atom {}))


(defn init-chan [uri]
  (when-let [s (:socket @state)] (.close s))
  (let [s (js/WebSocket. uri)]
    (aset s "onmessage" (fn [ev] (rf/dispatch [:server/message (.-data ev)])))
    (swap! state assoc :socket s)))

(rf/reg-event-fx
 :server/init
 (fn [coef [_ uri]]
   (println "server init")
   (init-chan uri)
   {}))

(rf/reg-event-db
 :server/send
 (fn [db [_ msg]]
   (println "send to server" msg)
   (when-let [s (:socket @state)]
     (.send s msg))
   (update db :messages conj {:id (str (gensym)) :command msg})))

(rf/reg-event-db
 :server/message
 (fn [db [k event]]
   (update db :messages conj {:id (str (gensym)) :message event})))
