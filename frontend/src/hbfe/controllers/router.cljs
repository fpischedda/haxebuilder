(ns hbfe.controllers.router
  (:require
   [bidi.bidi :as bidi]))

(defmulti control (fn [action _ _] action))

(defmethod control :init [_ [route] _]
  {:state route})

(defmethod control :push [_ [route] _]
  {:state route})

(defmethod control :set-token [_ token state]
  {:state state})
