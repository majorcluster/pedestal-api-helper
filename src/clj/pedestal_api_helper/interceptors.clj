(ns pedestal-api-helper.interceptors
  (:require [clojure.data.json :as json]))

(defn- convert-to-json
  [x]
  (cond (map? x) (json/write-str x)
        (coll? x) (let [converted-jsons (->> x
                                             (map json/write-str)
                                             (interpose ",")
                                             (apply str))]
                    (str "[" converted-jsons "]"))
        :else x))

(defn- get-last
  [ks m]
  (get m ks {}))

(def json-out
  {:name ::json-out
   :leave
   (fn [context]
     (->> context
          (get-last :response)
          (get-last :body)
          (convert-to-json)
          (assoc-in context [:response :body])))})
