(ns hbfe.core
  (:require
   [rum.core :as rum]
   [hbfe.components.login :as login]
   [hbfe.components.dashboard :as dashboard]))

(enable-console-print!)

(println "This text is printed from src/fe/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

(defn get_profile []
  {:token "abcdef" :username "minasss" :email "francesco.pischedda@gmail.com"})

(defn get_jobs []
  [{:_id "111" :created_at "yesterday" :status "finished"}])

(defn get_repositories []
  [{:_id "1223"
    :name "repo name"
    :url "https://blabla.com"
    :tracked_branches ["master" "build"]
    :targets ["html5"]}])

(defonce app-state (atom {:job-list (get_jobs)
                          :repositories (get_repositories)
                          :profile (get_profile)}))

(login/mount "app" app-state (fn []
                               (dashboard/mount "app" app-state)))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
