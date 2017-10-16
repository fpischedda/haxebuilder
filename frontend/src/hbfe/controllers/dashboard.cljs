(ns hbfe.controllers.dashboard
  (:require
   [hbfe.config :as config]))

(def initial-state {})

(defmulti control (fn [event] event))

(defmethod control :init [event args state]
  {:state initial-state})

(defmethod control :delete-repo [event args state]
  (let [[repo_id] args]
    {:state state
     :http {:url (config/repository-detail-url repo_id)
            :method :delete
            :success-fn :repo-deleted-succesful
            :error-fn :repo-delete-error}}))

(defmethod control :repo-deleted-successful [event args state]
  {:state state})

(defmethod control :repo-deleted-error [event args state]
  {:state state})

(defmethod control :create-repo [event args state]
  (let [[name url branches targets] args]
    {:state state
     :http {:url config/repository-new-url
            :method :post
            :success-fn :repo-created-succesful
            :error-fn :repo-created-error
            :params {:with-credentials? false
                     :form-params {:name name
                                   :url url
                                   :branches branches
                                   :targets targets}}}}))

(defmethod control :repo-created-successful [event args state]
  {:state state})

(defmethod control :repo-created-error [event args state]
  {:state state})

(defmethod control :load-repositories [event _ state]
  (let [token ((state :login) :token)]
    {:state state
     :http {:url (config/repository-list-url)
            :method :get
            :auth-token token
            :success-fn :repo-loaded-succesful
            :error-fn :repo-loaded-error}}))

(defmethod control :repo-loaded-successful [event args state]
  {:state state})

(defmethod control :repo-loaded-error [event args state]
  {:state state})
