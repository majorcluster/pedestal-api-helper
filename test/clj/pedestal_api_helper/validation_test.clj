(ns clj.pedestal-api-helper.validation-test
  (:require [clojure.test :refer :all]
            [matcher-combinators.test]
            [pedestal-api-helper.validation :as validation])
  (:import (clojure.lang ExceptionInfo)))

(deftest validate-test
  (testing "when not valid map is received returns true"
    (is (validation/validate nil {}))
    (is (validation/validate {} {})))
  (testing "mandatoriness is validated"
    (is (validation/validate {:title "Mafic Rock"} {"title" {:validate/type :validate/mandatory}}))
    (are [input specs] (thrown-match? ExceptionInfo
                                      {:type                :bad-format
                                       :validation-messages [{:field   "title"
                                                              :message "Field title is not present"}]}
                                      (validation/validate input specs))
      nil         {"title" {:validate/type :validate/mandatory}}
      {}          {"title" {:validate/type :validate/mandatory}}
      {:title ""} {"title" {:validate/type :validate/mandatory}}))
  (testing "min is validated"
    (are [input specs] (validation/validate input specs)
      {:title "Mafic Rock"} {"title" {:validate/type  :validate/min
                                      :validate/value 1}}
      {:title "Mafic Rock"} {"title" {:validate/type  :validate/min
                                      :validate/value 10}}
      {:age 18}             {"age" {:validate/type :validate/min
                                    :validate/value 18}}
      {}                    {"age" {:validate/type :validate/min
                                    :validate/value 18
                                    :validate/ignore-if-absent true}})
    (are [input specs] (thrown-match? ExceptionInfo
                                      {:type                :bad-format
                                       :validation-messages [{:field   "title"
                                                              :message "Field title must have a minimum size of 11"}]}
                                      (validation/validate input specs))
      {} {"title" {:validate/type  :validate/min
                   :validate/value 11}}
      {:title "Mafic Rock"} {"title" {:validate/type  :validate/min
                                      :validate/value 11}})
    (is (thrown-match? ExceptionInfo
                       {:type                :bad-format
                        :validation-messages [{:field   "title"
                                               :message "Field title is a custom message with size of 11"}]}
                       (validation/validate {:title "Mafic Rock"} {"title" {:validate/type :validate/min
                                                                            :validate/value 11
                                                                            :validate/message "Field %s is a custom message with size of %s"}}))))

  (testing "max is validated"
    (are [input specs] (validation/validate input specs)
      {:title "Mafic Rock"} {"title" {:validate/type :validate/max
                                      :validate/value 10}}
      {:title "Mafic Rock"} {"title" {:validate/type :validate/max
                                      :validate/value 15}}
      {:age 18}             {"age"   {:validate/type :validate/max
                                      :validate/value 18}}
      {}                    {"age"   {:validate/type :validate/max
                                      :validate/value 18
                                      :validate/ignore-if-absent true}})
    (are [input specs] (thrown-match? ExceptionInfo
                                      {:type                :bad-format
                                       :validation-messages [{:field   "title"
                                                              :message "Field title must have a maximum size of 9"}]}
                                      (validation/validate input specs))
      {:title "Mafic Rock"} {"title" {:validate/type :validate/max
                                      :validate/value 9}}
      {}                    {"title" {:validate/type :validate/max
                                      :validate/value 9}}))
  (testing "max and min are validated"
    (are [input specs] (validation/validate input specs)
      {:title "Mafic Rock"} {"title" [{:validate/type :validate/min
                                       :validate/value 10}
                                      {:validate/type :validate/max
                                       :validate/value 10}]}
      {:title "Mafic Rock"} {"title" [{:validate/type :validate/min
                                       :validate/value 1}
                                      {:validate/type :validate/max
                                       :validate/value 11}]})
    (is (thrown-match? ExceptionInfo
                       {:type                :bad-format
                        :validation-messages [{:field   "title"
                                               :message "Field title must have a minimum size of 11"}
                                              {:field   "title"
                                               :message "Field title must have a maximum size of 9"}]}
                       (validation/validate {:title "Mafic Rock"} {"title" [{:validate/type :validate/min
                                                                             :validate/value 11}
                                                                            {:validate/type :validate/max
                                                                             :validate/value 9}]})))
    (is (thrown-match? ExceptionInfo
                       {:type                :bad-format
                        :validation-messages [{:field   "title"
                                               :message "Field title must have a minimum size of 11"}
                                              {:field   "title"
                                               :message "Field title must have a maximum size of 9"}
                                              {:field   "age"
                                               :message "Field age must have a minimum size of 20"}]}
                       (validation/validate {:title "Mafic Rock", :age 19} {"title" [{:validate/type :validate/min
                                                                                      :validate/value 11}
                                                                                     {:validate/type :validate/max
                                                                                      :validate/value 9}]
                                                                            "age" {:validate/type :validate/min
                                                                                   :validate/value 20}}))))
  (testing "regex is validated"
    (are [input specs] (validation/validate input specs)
      {:title "Rock"} {"title" {:validate/type :validate/regex
                                :validate/value #"[A-Za-z]{4}"}}
      {:age "18"}     {"age"   {:validate/type :validate/regex
                                :validate/value #"[1-9]{1,2}"}}
      {}              {"age"   {:validate/type :validate/regex
                                :validate/value #"[1-9]{1,2}"
                                :validate/ignore-if-absent true}})
    (are [input specs] (thrown-match? ExceptionInfo
                                      {:type                :bad-format
                                       :validation-messages [{:field   "title"
                                                              :message "Field title is not valid"}]}
                                      (validation/validate input specs))
      {:title "Mafic Rock"} {"title" {:validate/type :validate/regex
                                      :validate/value #"[A-Za-z]{4}"}}
      {}                    {"title" [{:validate/type :validate/regex
                                       :validate/value #"[A-Za-z]{4}"}]})
    (is (thrown-match? ExceptionInfo
                       {:type                :bad-format
                        :validation-messages [{:field   "title"
                                               :message "Field title is not valid in a custom fantastic message"}]}
                       (validation/validate {:title "Mafic Rock"} {"title" [{:validate/type :validate/regex
                                                                             :validate/value #"[A-Za-z]{4}"
                                                                             :validate/message "Field %s is not valid in a custom fantastic message"}]}))))

  (testing "custom is validated"
    (is (validation/validate {:title "Rock"} {"title" {:validate/type :validate/custom
                                                       :validate/value (fn [value]
                                                                         (= value "Rock"))}}))
    (is (validation/validate {:age 18} {"age" {:validate/type :validate/custom
                                               :validate/value (fn [value]
                                                                 (<= value 18))}}))
    (is (validation/validate {} {"age" {:validate/type :validate/custom
                                        :validate/value string?
                                        :validate/ignore-if-absent true}}))
    (are [input specs] (thrown-match? ExceptionInfo
                                      {:type                :bad-format
                                       :validation-messages [{:field   "title"
                                                              :message "Field title is not valid"}]}
                                      (validation/validate input specs))
                       {:title "Mafic Rock"} {"title" {:validate/type :validate/custom
                                                       :validate/value number?}}
                       {} {"title" {:validate/type :validate/custom
                                    :validate/value string?}})
    (is (thrown-match? ExceptionInfo
                       {:type                :bad-format
                        :validation-messages [{:field   "title"
                                               :message "Field title has such a great message"}]}
                       (validation/validate {:title "Mafic Rock"} {"title" {:validate/type :validate/custom
                                                                            :validate/value (fn [value]
                                                                                              (= value "Sedimentary Rock"))
                                                                            :validate/message "Field %s has such a great message"}})))))
