(ns hbfe.handlers.http
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [citrus.core :as citrus]
   [cljs-http.client :as http]))

(defonce http-methods
  {:get http/get
   :post http/post
   :put http/put
   :patch http/patch
   :delete http/delete})

(defn http [reconciler ctrl-name {:keys [method url success-fn error-fn params]}]
  (go (let [response (<! ((http-methods method) url params))]
        (if (= 200 (:status response))
          (citrus/dispatch! reconciler ctrl-name success-fn response)
          (citrus/dispatch! reconciler ctrl-name error-fn response)))))
