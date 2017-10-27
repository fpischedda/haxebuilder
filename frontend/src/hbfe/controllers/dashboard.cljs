(ns hbfe.controllers.dashboard
  (:require
   [hbfe.config :as config]
   [clojure.string :refer [split]]))

(defn auth-header [token]
  {"X-Auth-Token" token})

(defn load-repos-effect [token]
  {:http
   {:url config/repository-list-url
    :method :get
    :params {:headers (auth-header token)}
    :success-fn :repo-list-loaded
    :error-fn :repo-list-loaded-error}})

(def initial-state {})

(defmulti control (fn [event] event))

(defmethod control :init [event args state]
  {:state initial-state})

(defmethod control :set-token [event [token] state]
  {:state (assoc state :token token)})

(defmethod control :load-repos [event args state]
  (load-repos-effect (:token state)))

(defmethod control :repo-list-loaded [event [response] state]
  (let [repositories (:body response)]
    {:state (assoc state :repositories repositories)}))

(defmethod control :repo-list-loaded-error [event args state]
  {:state state})

(defmethod control :delete-repo [event [repo-id] state]
  {:state state
   :http {:url (config/repository-detail-url repo-id)
          :method :delete
          :params {:headers (auth-header (:token state))}
          :success-fn :repo-deleted-successful
          :error-fn :repo-deleted-error}})

(defmethod control :repo-deleted-successful [event args state]
  (load-repos-effect (:token state)))

(defmethod control :repo-deleted-error [event args state]
  {:state state})

(defmethod control :create-repo [event args state]
  (let [[name url branches targets] args
        tracked-branches (split branches #" ")
        target-list (split targets #" ")]
    {:state state
     :http {:url config/repository-new-url
            :method :post
            :success-fn :repo-created-successful
            :error-fn :repo-created-error
            :params {:headers (auth-header (:token state))
                     :json-params {:name name
                                   :url url
                                   :tracked_branches tracked-branches
                                   :targets target-list}}}}))

(defmethod control :repo-created-successful [event args state]
  (load-repos-effect (:token state)))

(defmethod control :repo-created-error [event args state]
  {:state state})

(defmethod control :update-repo [event args state]
  (let [[repo-id name url branches targets] args
        tracked-branches (split branches #" ")
        target-list (split targets #" ")]
    {:state state
     :http {:url (config/repository-detail-url repo-id)
            :method :patch
            :success-fn :repo-updated-successful
            :error-fn :repo-updated-error
            :params {:headers (auth-header (:token state))
                     :json-params {:name name
                                   :url url
                                   :tracked_branches tracked-branches
                                   :targets target-list}}}}))

(defmethod control :repo-updated-successful [event args state]
  (load-repos-effect (:token state)))

(defmethod control :repo-updated-error [event args state]
  {:state state})
