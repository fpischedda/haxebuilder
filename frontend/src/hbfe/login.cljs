(ns hbfe.components.login
  (:require
   [rum.core :as rum]))


(rum/defc label-input [label property-map]
  [:label label
   [:input property-map]])

(rum/defc login [success-fn]
  [:div.login
   (label-input "Username" {:type "text" :name "username"})
   (label-input "Password" {:type "password" :name "password"})
   [:button {:on-click (fn[e]
                         (success-fn))} "Login"]])

(defn mount [element-id state success-fn]
  (rum/mount (login success-fn)
             (js/document.getElementById element-id)))

