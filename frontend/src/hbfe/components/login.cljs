(ns hbfe.components.login
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [citrus.core :as citrus]
   [rum.core :as rum]
   [cljs.core.async :refer [<!]]
   [hbfe.dom :as dom]
   [hbfe.utils :refer [error-message show-message label-input]]
   [hbfe.config :as config]))

(rum/defc login-button [r]
  [:button {:on-click #(citrus/dispatch! r
                                         :login
                                         :authenticate
                                         (dom/elem-value "#username")
                                         (dom/elem-value "#password"))}
   "Login"])

(rum/defc login [r]
  [:div.login-box
   [:h2 "HaxeBuilder Dashboard"]
   [:div.login-form
    (label-input "Username"
                 {:type "text" :name "username" :id "username"})
    (label-input "Password"
                 {:type "password" :name "password" :id "password"})
    (login-button r)]
   [:div#messages]])
