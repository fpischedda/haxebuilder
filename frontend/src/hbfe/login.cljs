(ns hbfe.components.login
  (:require
   [rum.core :as rum]
   [hbfe.dom :as dom]))

(defn get-auth-token [username password]
  {:username username :token "aaaa" :email "ffff@fff.ff"})

(defn get-profile-from-response [response]
  response)

(rum/defc label-input [label property-map]
  [:label label
   [:input property-map]])

(rum/defc login-button [state success-fn]
  [:button {:on-click (fn[e]
                        (let [res (get-auth-token (dom/value
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

