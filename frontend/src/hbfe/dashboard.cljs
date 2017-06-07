(ns hbfe.components.dashboard
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [rum.core :as rum]
   [cljs-http.client :as http]
   [cljs.core.async :refer [<!]]
   [hbfe.dom :as dom]
   [hbfe.utils :refer [error-message show-message]]
   [hbfe.config :as config]))

(rum/defc repository-item [item]
  (let [{:keys [_id name url]} item]
    [:li {:id _id :key _id} (str _id "-" name "-" url)]))

(rum/defc repository-list [repositories]
  [:ul.repository-list {:key "repositories"} (map #(repository-item %1) repositories)])

(rum/defc job-line [item]
  (let [{:keys [_id created_at status]} item]
    [:li {:id _id :key _id} (str _id "-" created_at  "-" status)]))

(rum/defc job-list [jobs]
  [:ul.job-list {:key "jobs"} (map #(job-line %1) jobs)])

(rum/defc profile [user]
  (let [{:keys [username email]} user]
    [:div.profile {:key "profile"}
     [:span username]
     [:span email]]))

(rum/defc dashboard [state]
  (let [{:keys [profile repositories job-list]} state]
    [:div
     (profile profile)
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
