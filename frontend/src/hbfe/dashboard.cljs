(ns hbfe.components.dashboard
  (:require
   [rum.core :as rum]))

(rum/defc repository-item [item]
  [:li {:id (:_id item) :key (:_id item)} (str (:_id item) "-" (:name item) "-" (:url item))])

(rum/defc repository-list [repositories]
  [:ul.repository-list {:key "repositories"} (map #(repository-item %1) repositories)])

(rum/defc job-line [item]
  [:li {:id (:_id item) :key (:_id item)} (str (:_id item) "-" (:created_at item) "-" (:status item))])

(rum/defc job-list [jobs]
  [:ul.job-list {:key "jobs"} (map #(job-line %1) jobs)])

(rum/defc profile [user]
  [:div.profile {:key "profile"}
   [:span (:username user)]])

(rum/defc dashboard [state]
  [:div
   (profile (:profile state))
   (repository-list (:repositories state))
   (job-list (:job-list state))])

(defn mount [element-id state]
  (rum/mount (dashboard @state)
             (js/document.getElementById element-id)))
