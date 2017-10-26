(ns hbfe.handlers.session
  (:require
   [citrus.core :refer [broadcast-sync!]]))

(defn set-token [reconciler _ token]
  (broadcast-sync! reconciler :set-token token))
