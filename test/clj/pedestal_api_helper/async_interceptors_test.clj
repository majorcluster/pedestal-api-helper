(ns clj.pedestal-api-helper.async-interceptors-test
  (:require [clojure.core.async :as async]
            [clojure.test :refer :all]
            [pedestal-api-helper.async-interceptors :as a-int])
  (:import [clojure.core.async.impl.channels ManyToManyChannel]))

(deftest async-fetch-output-interceptor-test
  (testing "only enter and name are in the context"
    (let [async-i (a-int/async-fetch-output-interceptor {:name :async-1
                                                         :enter (fn [_] 1)})]
      (is (= 2 (count (select-keys async-i [:name :enter :leave]))))))
  (testing "only leave and name are in the context"
    (let [async-i (a-int/async-fetch-output-interceptor {:name :async-1
                                                         :leave (fn [_] 1)})]
      (is (= 2 (count (select-keys async-i [:name :enter :leave]))))))
  (testing "enter, leave and name are in the context"
    (let [async-i (a-int/async-fetch-output-interceptor {:name :async-1
                                                         :enter (fn [_] 1)
                                                         :leave (fn [_] 1)})]
      (is (= 3 (count (select-keys async-i [:name :enter :leave]))))))
  (testing "async-channel is attached"
    (let [async-i (a-int/async-fetch-output-interceptor {:name :async-1
                                                         :enter (fn [_] 1)})
          context-executed ((:enter async-i) {})]
      (is (instance? ManyToManyChannel (-> context-executed :request :async-channels :async-1)))
      (is (= (-> context-executed :request :async-channels :async-1 async/<!! :async-1)
             1))))
  (testing "async-channels are attached"
    (let [async-i (a-int/async-fetch-output-interceptor {:name :async-1
                                                         :enter (fn [_] 1)})
          async-i-2 (a-int/async-fetch-output-interceptor {:name :async-2
                                                           :enter (fn [_] 2)})
          context-executed (-> {}
                               ((:enter async-i))
                               ((:enter async-i-2)))]
      (is (instance? ManyToManyChannel (-> context-executed :request :async-channels :async-1)))
      (is (= (-> context-executed :request :async-channels :async-1 async/<!! :async-1)
             1))
      (is (= (-> context-executed :request :async-channels :async-2 async/<!! :async-2)
             2)))))

(deftest async-fetch-interceptor-test
  (testing "only enter and name are in the context"
    (let [async-i (a-int/async-output-interceptor {:name :async-1
                                                   :enter (fn [_] 1)})]
      (is (= 2 (count (select-keys async-i [:name :enter :leave]))))))
  (testing "only leave and name are in the context"
    (let [async-i (a-int/async-output-interceptor {:name :async-1
                                                   :leave (fn [_] 1)})]
      (is (= 2 (count (select-keys async-i [:name :enter :leave]))))))
  (testing "enter, leave and name are in the context"
    (let [async-i (a-int/async-output-interceptor {:name :async-1
                                                   :enter (fn [_] 1)
                                                   :leave (fn [_] 1)})]
      (is (= 3 (count (select-keys async-i [:name :enter :leave])))))))
