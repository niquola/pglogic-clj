(ns ui.dashboard.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [reagent.core :as reagent]
   [ui.pages :as pages]
   [re-frame.core :as rf]
   [ui.widgets :as wgt]
   [clojure.string :as str]))


(rf/reg-sub-raw
 ::messages
 (fn [db _] (reaction (:messages @db))))

(defn index [params]
  (let [route @(rf/subscribe [:route-map/current-route])
        msg (rf/subscribe [::messages])]
    (fn []
      [:div.container
       (for [m @msg]
         [:div {:key (:id m)} m])])))

(pages/reg-page :core/index index)
(pages/reg-page :core/tables index)

