(ns clj.pedestal-api-helper.interceptors-test
  (:require [clojure.test :refer :all]
            [pedestal-api-helper.interceptors :refer :all]))

(defn get-resp-body
  [result]
  (-> result
      :response
      :body))

(defn exec-json-out-w-response
  [response-body]
  ((:leave json-out) {:response {:body response-body}}))

(deftest json-out-test
  (testing "when json-out leaves with non-map response, same response returns"
    (is (= "{\"payload\":true}"
           (-> "{\"payload\":true}"
               (exec-json-out-w-response)
               (get-resp-body))))
    (is (= "{\"payload\":1}"
           (-> "{\"payload\":1}"
               (exec-json-out-w-response)
               (get-resp-body))))
    (is (= "something"
           (-> "something"
               (exec-json-out-w-response)
               (get-resp-body))))
    (is (= 1917
           (-> 1917
               (exec-json-out-w-response)
               (get-resp-body)))))
  (testing "when json-out leaves with empty map response, empty body is returned"
    (is (= "{}"
           (-> {}
               (exec-json-out-w-response)
               (get-resp-body)))))
  (testing "when json-out leaves with a filled map, it is converted to json"
    (is (= "{\"payload\":{\"message\":\"Hi!\",\"tries\":4}}"
           (-> {:payload {:message "Hi!"
                          :tries 4}}
               (exec-json-out-w-response)
               (get-resp-body))))
    (is (= "{\"message\":\"Hi!\",\"tries\":4}"
           (-> {:message "Hi!"
                          :tries 4}
               (exec-json-out-w-response)
               (get-resp-body))))
    (is (= "{\"message\":\"Hi!\",\"tried\":false}"
           (-> {:message "Hi!"
                :tried false}
               (exec-json-out-w-response)
               (get-resp-body))))))
