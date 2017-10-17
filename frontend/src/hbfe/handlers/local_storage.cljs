(ns hbfe.handlers.local-storage
  (:require
   [citrus.core :as citrus]))

(defn local-storage [r c {:keys [op key value on-ready]}]
  (case op
    :get
    (->> (name key)
         js/localStorage.getItem
         (citrus/dispatch! r c on-ready))
    :set
    (-> (name key)
        (js/localStorage.setItem value))))
