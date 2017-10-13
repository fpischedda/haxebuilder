(ns hbfe.ui
  (:require
   [rum.core :as rum]
   [hbfe.components.login :as login]))

(rum/defc App < rum/reactive [r]
  [:div
   [:p "New Dashboard based on rum and citrus"]
   (login/login r)])

