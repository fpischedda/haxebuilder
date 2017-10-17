(ns hbfe.components.dashboard
  (:require
   [citrus.core :as citrus]
   [clojure.string :refer [join]]
   [rum.core :as rum]
   [cljs-http.client :as http]
   [cljs.core.async :refer [<!]]
   [hbfe.dom :as dom]
   [hbfe.utils :refer [error-message show-message label-input]]
   [hbfe.config :as config]))

(rum/defc repository-delete [r repo_id]
  [:button {:on-click #(citrus/dispatch! r
                                         :dashboard
                                         :delete-repo
                                         repo_id)} "Delete"])

(rum/defc repository-item [r item]
  (let [{:keys [_id name url tracked_branches targets]} item]
    [:tr {:on-click (fn [e] false)}
     [:td name] [:td url] [:td (join "," tracked_branches)] [:td (join "," targets)] [(repository-delete r _id)]]))

(defn create-repo [r name url branches targets]
  #(citrus/dispatch! r
                    :dashboard
                    :create-repo
                    name
                    url
                    branches
                    targets))

(rum/defc repository-new-button [r]
  [:button {:on-click (fn[e]
                        (create-repo r
                                     (dom/value
                                      (dom/q "#name"))
                                     (dom/value
                                      (dom/q "#url"))
                                     (dom/value
                                      (dom/q "#tracked-branches"))
                                     (dom/value
                                      (dom/q "#targets"))))}
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

(rum/defc repository-list [r repositories]
  [:div.repositories
   [:p "Your repositories"]
   [:table.repository-list
    [:thead
     [:tr
      [:td "Name"] [:td "Url"] [:td "Tracked Branches"] [:td "Build Targets"] [:td ""]]]
    [:tbody
     (map #(repository-item r %1) repositories)]]
   [:p (repository-form-new)]])

(rum/defc job-line [item]
  (let [{:keys [_id created_at status]} item]
    [:li {:id _id :key _id} (str _id "-" created_at  "-" status)]))

(rum/defc job-list [jobs]
  [:ul.job-list (map #(job-line %1) jobs)])

(rum/defc dashboard < rum/reactive [r]
  (let [{:keys [repositories r job-list]} (rum/react (citrus/subscription r [:dashboard]))]
    [:div
     (repository-list repositories)]))
