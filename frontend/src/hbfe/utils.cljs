(ns hbfe.utils
  (:require
   [rum.core :as rum]))


(rum/defc label-input [label property-map]
  [:label label
   [:input property-map]])

(defn error-message [response]
  (get-in response [:body :error :message]))

(rum/defc message-label [text]
  [:label.error text])

(defn show-message [element-id text]
  (rum/mount (message-label text)
             (js/document.getElementById element-id)))
