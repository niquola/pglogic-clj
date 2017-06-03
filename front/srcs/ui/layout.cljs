(ns ui.layout
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [reagent.core :as reagent]
   [ui.styles :as styles]
   [re-frame.core :as rf]
   [ui.routes :refer [href]]
   [ui.styles :as styles]
   [ui.widgets :as wgt]
   [clojure.string :as str]))


(defn current-page? [route key]
  (= (:match route) key))

(defn nav-item [route k path title]
  [:li.nav-item {:class (when (current-page? @route k) "active")}
   [:a.nav-link {:href (apply href path)} title]])

(defn menu []
  (let [route (rf/subscribe [:route-map/current-route])]
    (fn []
      [:nav.navbar.navbar-toggleable-md.navbar-light.bg-faded
       (styles/style [:nav.navbar {:border-bottom "1px solid #ddd"
                                   :padding-bottom 0}
                      [:li.nav-item {:border-bottom "3px solid transparent"}
                       [:&.active {:border-color "#555"}]]])
       [:div.container
        [:a.navbar-brand {:href "#/"} "MyEHR"]
        [:div.collapse.navbar-collapse
         [:ul.nav.navbar-nav.mr-auto
          [nav-item route :core/tables [:tables] "Tables"]]]]])))

(defn layout [content]
  (fn []
    [:div.app
     [:style styles/basic-style]
     [styles/style [:.sql {:width "100%" :min-height "300px"}]]
     [menu]
     [:div.container
      [:textarea.sql {:on-key-down (fn [ev]
                                     (when (and (= 13 (.-which ev))
                                              (.-shiftKey ev))
                                       (rf/dispatch [:server/send (.. ev -target -value)])))}]
      [:div.container content]]]))
