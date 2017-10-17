(ns hbfe.components.profile
  (:require
   [rum.core :as rum]
   ))

(rum/defc bar [{:keys [token username email]}]
  [:div.profile "User profile"
   (if (nil? token)
     [:a {:href "/login"} "Login"]
     [:span.account (str "Hello " username)])])
