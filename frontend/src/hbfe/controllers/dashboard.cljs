(ns hbfe.controllers.dashboard
  (:require
   [hbfe.config :as config]))

(defn auth-header [token]
  {"X-Auth-Token" token})

(def initial-state {})

(defmulti control (fn [event] event))

(defmethod control :init [event args state]
  {:state initial-state})

(defmethod control :load-repos [event [api-token] state]
  {:http
   {:url config/repository-list-url
    :method :get
    :params {:headers (auth-header api-token)}
    :success-fn :repo-list-loaded
    :error-fn :repo-list-loaded-error}})

(defmethod control :repo-list-loaded [event [response] state]
  (let [repositories (:body response)]
    (println repositories)
    {:state {:repositories repositories
             :job-list nil}}))

(defmethod control :repo-list-loaded-error [event args state]
  {:state state})

(defmethod control :delete-repo [event [api-token repo_id] state]
  {:state state
   :http {:url (config/repository-detail-url repo_id)
          :method :delete
          :params {:headers (auth-header api-token)}
          :success-fn :repo-deleted-succesful
          :error-fn :repo-delete-error}})

(defmethod control :repo-deleted-successful [event args state]
  {:state state})

(defmethod control :repo-deleted-error [event args state]
  {:state state})

(defmethod control :create-repo [event args state]
  (let [[api-token name url branches targets] args]
    {:state state
     :http {:url config/repository-new-url
            :method :post
            :success-fn :repo-created-successful
            :error-fn :repo-created-error
            :params {:headers (auth-header api-token)
                     :json-params {:name name
                                   :url url
                                   :tracked_branches branches
                                   :targets targets}}}}))

(defmethod control :repo-created-successful [event args state]
  {:state state})

(defmethod control :repo-created-error [event args state]
  {:state state})
