(ns hbfe.ui
  (:require
   [citrus.core :as citrus]
   [rum.core :as rum]
   [hbfe.components.dashboard :as dashboard]
   [hbfe.components.login :as login]))

(rum/defc App < rum/reactive [r]
  (let [{route :handler params :route-params} (rum/react (citrus/subscription r [:router]))]
    [:div
     [:p "New Dashboard based on rum and citrus"]
     (case route
       :home (dashboard/dashboard r)
       :login (login/login r)
       :logout (login/logout r)
       (login/login r))]))

