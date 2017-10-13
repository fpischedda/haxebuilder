(ns hbfe.core
  (:require
   [rum.core :as rum]
   [citrus.core :as citrus]
   [hbfe.dom :as dom]
   [hbfe.controllers.login :as login]
   [hbfe.controllers.dashboard :as dashboard]
   [hbfe.handlers.http :refer [http]]
   [hbfe.ui :as ui]))

(enable-console-print!)

(defonce reconciler
  (citrus/reconciler {:state (atom {})
                      :controllers {:login login/control
                                    :dashboard dashboard/control}
                      :effect-handlers {:http http}}))

;; render
(rum/mount (ui/App reconciler)
           (dom/q "#app"))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
