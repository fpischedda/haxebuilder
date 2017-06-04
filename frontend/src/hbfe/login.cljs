(ns hbfe.components.login
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [rum.core :as rum]
   [cljs-http.client :as http]
   [cljs.core.async :refer [<!]]
   [hbfe.dom :as dom]))

(defn autenticate [username password]
  (go (let [response (<! (http/post "http://localhost:8000/login"
                                   {:with-credentials? false
                                    :form-params {:username username
                                                  :password password}}))]
        (if (and
             (= 200 (:status response))
             (= nil (:error (:body response))))
          (:body response)
          {:error (str "Server responded with status code" (:status response))}))))

(defn get-profile-from-response [response]
  response)

(rum/defc label-input [label property-map]
  [:label label
   [:input property-map]])

(rum/defc login-button [state success-fn]
  [:button {:on-click (fn[e]
                        (let [res (autenticate (dom/value
                                                   (dom/q "#username"))
                                                  (dom/value
                                                   (dom/q "#password")))]
                          (when (not (= nil (:token res)))
                            (reset! state (get-profile-from-response res))
                            (success-fn))))}
   "Login"])

(rum/defc login [state success-fn]
  [:div.login-box
   [:h2 "HaxeBuilder Dashboard"]
   [:div.login-form
    (label-input "Username"
                 {:type "text" :name "username" :id "username"})
    (label-input "Password"
                 {:type "password" :name "password" :id "password"})
    (login-button state success-fn)]])

(defn mount [state element-id success-fn]
  (rum/mount (login state success-fn)
             (js/document.getElementById element-id)))

