(ns pedestal-api-helper.interceptors
  (:require [clojure.data.json :as json]))

(def json-out
  {:name ::json-out
   :leave
   (fn [context]
     (let [response         (get context :response {})
           body             (get response :body {})
           updated-response (cond (map? body) (assoc response :body (json/write-str body))
                                  :else response)]
       (assoc context :response updated-response)))})
