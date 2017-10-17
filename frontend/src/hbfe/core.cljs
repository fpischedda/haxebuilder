(ns hbfe.core
  (:require
   [accountant.core :refer [configure-navigation!]]
   [bidi.bidi :as bidi]
   [citrus.core :as citrus]
   [pushy.core :as pushy]
   [rum.core :as rum]
   [hbfe.dom :as dom]
   [hbfe.controllers.dashboard :as dashboard]
   [hbfe.controllers.login :as login]
   [hbfe.controllers.router :as router-ctrl]
   [hbfe.handlers.http :refer [http]]
   [hbfe.handlers.navigation :refer [goto]]
   [hbfe.handlers.local-storage :refer [local-storage]]
   [hbfe.router :as router]
   [hbfe.ui :as ui]))

(enable-console-print!)

(def routes
  ["/" [["" :dashboard]
        ["index.html" :login]
        ["login" :login]
        ["logout" :logout]
        [["repo/" :id] :repo]]])

(defonce reconciler
  (citrus/reconciler {:state (atom {})
                      :controllers {:dashboard dashboard/control
                                    :login login/control
                                    :router router-ctrl/control}
                      :effect-handlers {:http http
                                        :goto goto
                                        :local-storage local-storage}}))

(citrus/broadcast-sync! reconciler :init)
(citrus/dispatch-sync! reconciler :login :load-profile :profile)

(router/start! #(citrus/dispatch! reconciler :router :push %) routes)

(configure-navigation!
 {
  :nav-handler #(citrus/dispatch! reconciler :router :push (bidi/match-route routes %))
  :path-exists? (fn [path]
                  (boolean (bidi/match-route routes path)))})
;; render
(rum/mount (ui/App reconciler)
           (dom/q "#app"))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
