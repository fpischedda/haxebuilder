(ns hbfe.components.dashboard
  (:require
   [accountant.core :refer [navigate!]]
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

(defn create-repo [r name url branches targets]
  (citrus/dispatch! r
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

(rum/defc repository-form-new [r]
  [:ul.new-repo-form "Track new repository"
   [:li (label-input "Name"
                    {:type "text" :name "name" :id "name"})]
   [:li (label-input "URL"
                    {:type "text" :name "url" :id "url"})]
   [:li (label-input "Tracked Branches"
                    {:type "text" :name "tracked-branches" :id "tracked-branches"})]
   [:li (label-input "Build targets"
                    {:type "text" :name "targets" :id "targets"})]
   [:li (repository-new-button r)]])

(defn single-or-list [value]
  (if (list? value)
    (join "," value)
    value))

(rum/defc repository-item [r item]
  (let [{:keys [_id name url tracked_branches targets]} item]
    [:tr {:on-click (fn [e] false) :key _id}
     [:td name] [:td url] [:td (single-or-list tracked_branches)] [:td (single-or-list targets)] [(repository-delete r _id)]]))

(rum/defc refresh-button [r]
  [:button.refresh-button {:on-click (fn [e] (citrus/dispatch! r
                                                        :dashboard
                                                        :load-repos))}
   "Refresh"])

(rum/defc repository-list [r repositories]
  [:div.repositories
   [:h3 "Your repositories"]
   (refresh-button r)
   [:table.repository-list
    [:thead
     [:tr
      [:td "Name"] [:td "Url"] [:td "Tracked Branches"] [:td "Build Targets"] [:td ""]]]
    [:tbody
     (map #(repository-item r %1) repositories)]]
   [:div.new-repository (repository-form-new r)]])

(rum/defc job-line [item]
  (let [{:keys [_id created_at status]} item]
    [:li {:id _id :key _id} (str _id "-" created_at  "-" status)]))

(rum/defc job-list [jobs]
  [:ul.job-list (map #(job-line %1) jobs)])

(rum/defc dashboard < rum/reactive [r]
  (let [{:keys [token repositories job-list]} (rum/react (citrus/subscription r [:dashboard]))]
    (cond
      (nil? token) (navigate! "/login")
      (nil? repositories) (citrus/dispatch! r
                                            :dashboard
                                            :load-repos))
    [:div
     (repository-list r repositories)]
    ))
