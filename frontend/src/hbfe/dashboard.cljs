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
  [:li {:id (:_id item) :key (:_id item)} (str (:_id item) "-" (:name item) "-" (:url item))])

(rum/defc repository-list [repositories]
  [:ul.repository-list {:key "repositories"} (map #(repository-item %1) repositories)])

(rum/defc job-line [item]
  [:li {:id (:_id item) :key (:_id item)} (str (:_id item) "-" (:created_at item) "-" (:status item))])

(rum/defc job-list [jobs]
  [:ul.job-list {:key "jobs"} (map #(job-line %1) jobs)])

(rum/defc profile [user]
  [:div.profile {:key "profile"}
   [:span (:username user)]])

(rum/defc dashboard [state]
  [:div
   (profile (:profile state))
   (repository-list (:repositories state))
   (job-list (:job-list state))])

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
