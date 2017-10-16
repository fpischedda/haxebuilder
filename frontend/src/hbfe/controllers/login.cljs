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

(defmethod control :login-successful [event args state]
  (println "login successful" args state)
  {:state (:token state)})

(defmethod control :login-error [event args state]
  (println "login error" args state)
  {:state state})

(defmethod control :logout [event args state]
  {:state {}})
