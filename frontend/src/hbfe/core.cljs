(ns hbfe.core
  (:require
   [citrus.core :as citrus]
   [rum.core :as rum]
   [hbfe.dom :as dom]
   [hbfe.controllers.dashboard :as dashboard]
   [hbfe.controllers.login :as login]
   [hbfe.controllers.router :as router-ctrl]
   [hbfe.handlers.http :refer [http]]
   [hbfe.router :as router]
   [hbfe.ui :as ui]))

(enable-console-print!)

(def routes
  ["/" [["" :login]
        ["index.html" :login]
        ["login" :login]
        ["logout" :logout]
        [["repo/" :id] :repo]]])

(defonce reconciler
  (citrus/reconciler {:state (atom {})
                      :controllers {:dashboard dashboard/control
                                    :login login/control
                                    :router router-ctrl/control}
                      :effect-handlers {:http http}}))

(citrus/broadcast-sync! reconciler :init)

(router/start! #(citrus/dispatch! reconciler :router :push %) routes)

;; render
(rum/mount (ui/App reconciler)
           (dom/q "#app"))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
