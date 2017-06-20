(ns hbfe.components.dashboard
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [rum.core :as rum]
   [clojure.string :refer [join]]
   [cljs-http.client :as http]
   [cljs.core.async :refer [<!]]
   [hbfe.state :refer [app-state]]
   [hbfe.dom :as dom]
   [hbfe.utils :refer [error-message show-message label-input]]
   [hbfe.config :as config]))

(defn delete-repo [repo_id]
  (go (let [response (<! (http/delete (config/repository-detail-url repo_id)
                                    {:with-credentials? false
                                     :headers {"X-Auth-Token" (:token (:profile @app-state))}}))])))

(rum/defc repository-delete [repo_id]
  [:button {:on-click (fn [e]
                        (delete-repo repo_id))} "Delete"])

(rum/defc repository-item [item]
  (let [{:keys [_id name url tracked_branches targets]} item]
    [:tr {:on-click (fn [e] false)}
     [:td name] [:td url] [:td (join "," tracked_branches)] [:td (join "," targets)] [(repository-delete _id)]]))

(defn create-repo [name url branches targets state success-fn]
  nil)

(rum/defc repository-new-button [state success-fn]
  [:button {:on-click (fn[e]
                        (create-repo (dom/value
                                      (dom/q "#name"))
                                     (dom/value
                                      (dom/q "#url"))
                                     (dom/value
                                      (dom/q "#tracked-branches"))
                                     (dom/value
                                      (dom/q "#targets"))
                                     state
                                     success-fn))}
   "Register repository"])

(rum/defc repository-form-new []
  [:div "Track new repository"
   [:p (label-input "Name"
                    {:type "text" :name "username" :id "username"})]
   [:p (label-input "URL"
                    {:type "text" :name "url" :id "url"})]
   [:p (label-input "Tracked Branches"
                    {:type "text" :name "tracked-branches" :id "tracked-branches"})]
   [:p (label-input "Build targets"
                    {:type "text" :name "targets" :id "targets"})]])

(rum/defc repository-list [repositories]
  [:div.repositories
   [:p "Your repositories"]
   [:table.repository-list
    [:thead
     [:tr
      [:td "Name"] [:td "Url"] [:td "Tracked Branches"] [:td "Build Targets"] [:td ""]]]
    [:tbody
     (map #(repository-item %1) repositories)]]
   [:p (repository-form-new)]])

(rum/defc job-line [item]
  (let [{:keys [_id created_at status]} item]
    [:li {:id _id :key _id} (str _id "-" created_at  "-" status)]))

(rum/defc job-list [jobs]
  [:ul.job-list (map #(job-line %1) jobs)])

(rum/defc profile-line [user]
  (let [{:keys [username email]} user]
    [:div.profile
     (str "Hello " username)]))

(rum/defc dashboard [state]
  (let [{:keys [profile repositories job-list]} state]
    [:div
     (profile-line profile)
     (repository-list repositories)]))

(defn got-repositories [state response mount-fn]
  (reset! state (assoc @state :repositories (:body response)))
  (mount-fn))

(defn load-repositories [state mount-fn]
  (go (let [response (<! (http/get config/repositories-url
                                   {:with-credentials? false
                                    :headers {"X-Auth-Token" (:token (:profile @state))}}))]
        (if (and
             (= 200 (:status response))
             (= nil (:error (:body response))))
          (got-repositories state response mount-fn)
          {:error (str "Server responded with error message " (error-message response))}))))

(defn mount [element-id state]
  (load-repositories state (fn[]
                             (rum/mount (dashboard @state)
                                        (js/document.getElementById element-id)))))
