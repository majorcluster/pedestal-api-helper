(ns clj.pedestal-api-helper.core-test
  (:require [clojure.test :refer :all]
            [io.pedestal.http :as http]))

(def service-map {:env :test
                  ::http/routes #{}
                  ::http/resource-path "/public"

                  ::http/type :jetty
                  ::http/port 8080
                  ::http/container-options {:h2c? true
                                            :h2? false
                                            :ssl? false}})
