(ns clj.pedestal-api-helper.async-interceptors-i-test
  (:require [clj.pedestal-api-helper.core-test :refer [service-map]]
            [clojure.edn :as edn]
            [clojure.test :refer :all]
            [io.pedestal.http :as http]
            [io.pedestal.test :refer [response-for]]
            [mockfn.macros :refer [verifying]]
            [mockfn.matchers :refer [exactly]]
            [pedestal-api-helper.async-interceptors :as async-i]))

(defn get-foo
  [request]
  {:status 200 :body (-> request :async-data)})

(defn http-out
  [v] v)

(def fetch-interceptors
  [(async-i/async-fetch-output-interceptor {:name :async-1
                                            :enter (fn [_] (http-out 1) 1)})
   (async-i/async-blocker-interceptor)])

(def output-enter-interceptors
  [(async-i/async-output-interceptor {:name :async-2
                                      :enter (fn [_] (http-out 2) 2)})])

(def output-leave-interceptors
  [(async-i/async-output-interceptor {:name :async-3
                                      :leave (fn [_] (http-out 3) 3)})])

(def all-interceptors
  (into [] (concat output-enter-interceptors fetch-interceptors output-leave-interceptors)))

(def service
  (::http/service-fn (http/create-servlet (assoc service-map
                                            ::http/routes #{["/foo" :get (conj fetch-interceptors
                                                                               `get-foo) :route-name :get-foo]
                                                            ["/foo-2" :get (conj output-enter-interceptors
                                                                                 `get-foo) :route-name :get-foo-2]
                                                            ["/foo-3" :get (conj output-leave-interceptors
                                                                                 `get-foo) :route-name :get-foo-3]
                                                            ["/foo-4" :get (conj all-interceptors
                                                                                 `get-foo) :route-name :get-foo-4]}))))

(deftest foo-get-test
  (verifying [(http-out 1) 1 (exactly 1)]
    (let [response (response-for service :get "/foo")]
      (is (= (:status response) 200))
      (is (= (edn/read-string (:body response)) {:async-1 1})))))

(deftest foo-2-get-test
  (verifying [(http-out 2) 2 (exactly 1)]
    (let [response (response-for service :get "/foo-2")]
      (is (= (:status response) 200))
      (is (= (:body response) "")))))

(deftest foo-3-get-test
  (verifying [(http-out 3) 3 (exactly 1)]
    (let [response (response-for service :get "/foo-3")]
      (is (= (:status response) 200))
      (is (= (:body response) "")))))

(deftest foo-4-get-test
  (verifying [(http-out 2) 2 (exactly 1)
              (http-out 1) 1 (exactly 1)
              (http-out 3) 3 (exactly 1)]
    (let [response (response-for service :get "/foo-4")]
      (is (= (:status response) 200))
      (is (= (edn/read-string (:body response)) {:async-1 1})))))
