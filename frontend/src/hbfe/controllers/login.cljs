(ns hbfe.controllers.login
  (:require
   [hbfe.config :as config]))

(def initial-state {})

(defmulti control (fn [event] event))

(defmethod control :init []
  {:state initial-state})

(defmethod control :load-profile [_ [key]]
  {:local-storage
   {:op :get
    :key key
    :on-ready :profile-loaded}})

(defmethod control :profile-loaded [_ [profile]]
  (if-not (nil? profile)
    {:state (js->clj (.parse js/JSON profile) :keywordize-keys true)
     :goto {:url "/"}}
    {:state initial-state}))

(defmethod control :authenticate [event args state]
  (let [[username password] args]
    {:state state
     :http {:url config/login-url
            :method :post
            :success-fn :login-successful
            :error-fn :login-error
            :params {:with-credentials? false
                     :form-params {:username username
                                   :password password}}}}))

(defmethod control :login-successful [event [response] state]
  (let [{:keys [error token username email]} (:body response)
        profile {:profile {:token token
                           :username username
                           :email email}}]
    (if (nil? error)
      {:state profile
       :local-storage {:op :set
                       :key :profile
                       :value (.stringify js/JSON (clj->js profile))}
       :goto {:url "/"}}
      {:state {:error (:message error)}})))

(defmethod control :login-error [event args state]
  {:state {:error "Network error, please try again in a minute"}})

(defmethod control :logout [event args state]
  {:state {}
   :goto {:url "/login"}})
