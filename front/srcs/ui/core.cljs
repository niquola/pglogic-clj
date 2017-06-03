(ns ui.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [clojure.string :as str]
   [cljsjs.react]
   [reagent.core :as reagent]
   [re-frame.core :as rf]
   [frames.routing]
   [ui.db]
   [ui.pages :as pages]
   [ui.dashboard.core]
   [ui.routes :as routes]
   [ui.layout :as layout]
   [ui.server :as server]))

(def open-id-keys
  {:client-id "646067746089-6ujhvnv1bi8qvd7due8hdp3ob9qtcumv.apps.googleusercontent.com"
   :uri "https://accounts.google.com/o/oauth2/v2/auth"})

;; (def base-url "http://cleoproto.aidbox.io/fhir")
(def base-url "http://cleoproto.aidbox.io/fhir")

;; (def open-id-keys
;;   {:client-id "khI6JcdsQ3dgHMdWJnej0OZjr5DXGWRU"
;;    :uri "https://aidbox.auth0.com/authorize"})


;; this is the root component wich switch pages
;; using current-route key from database
(defn current-page []
  (let [{page :match params :params} @(rf/subscribe [:route-map/current-route])]
    (if page
      (if-let [cmp (get @pages/pages page)]
        [:div [cmp params]]
        [:div.not-found (str "Page not found [" (str page) "]" )])
      [:div.not-found (str "Route not found ")])))

;; this is first event, which should initialize
;; application
;; handler use coefects cookies & openid to check for
;; user in cookies or in location string (after OpenId redirect)

;; (.. js/window -location -host)
(defn chan-uri []
  (str "ws://localhost:3333"))

(rf/reg-event-fx
 ::initialize
 []
 (fn [coef _]
   {:dispatch-n [[:route-map/init routes/routes]
                 [:server/init (chan-uri)]]}))


(defn- mount-root []
  (reagent/render
   [layout/layout [current-page]]
   (.getElementById js/document "app")))

(defn init! []
  (rf/dispatch [::initialize])
  (mount-root))
