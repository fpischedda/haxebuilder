(ns hbfe.controllers.login
  (:require
   [hbfe.config :as config]))

(def initial-state {})

(defmulti control (fn [event] event))

(defmethod control :init []
  {:state initial-state})

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

(defmethod control :login-successful [event [args] state]
  (let [{:keys [error token username email]} (:body args)]
    (if (nil? error)
      {:state {:profile {:token token
                         :username username
                         :email email}}
       :goto {:url "/"}}
      {:state {:error (:message error)}})))

(defmethod control :login-error [event args state]
  {:state {:error "Network error, please try again in a minute"}})

(defmethod control :logout [event args state]
  {:state {}
   :goto {:url "/login"}})
