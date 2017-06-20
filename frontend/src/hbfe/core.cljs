(ns hbfe.core
  (:require
   [rum.core :as rum]
   [hbfe.components.login :as login]
   [hbfe.components.dashboard :as dashboard]
   [hbfe.state :refer [app-state]]))

(enable-console-print!)

(login/mount (rum/cursor-in app-state [:profile]) "app" (fn []
                               (dashboard/mount "app" app-state)))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
