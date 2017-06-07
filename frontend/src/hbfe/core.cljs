(ns hbfe.core
  (:require
   [rum.core :as rum]
   [hbfe.components.login :as login]
   [hbfe.components.dashboard :as dashboard]))

(enable-console-print!)

(println "This text is printed from src/fe/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

(defn get_jobs []
  [{:_id "111" :created_at "yesterday" :status "finished"}])

(defonce app-state (atom {:job-list (get_jobs)
                          :repositories nil
                          :profile nil}))

(login/mount (rum/cursor-in app-state [:profile]) "app" (fn []
                               (dashboard/mount "app" app-state)))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
