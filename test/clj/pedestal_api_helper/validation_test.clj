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
    (is (thrown-match? ExceptionInfo
                       {:type                :bad-format
                        :validation-messages [{:field   "title"
                                               :message "Field title is not present"}]}
                       (validation/validate nil {"title" {:validate/type :validate/mandatory}})))
    (is (thrown-match? ExceptionInfo
                       {:type                :bad-format
                        :validation-messages [{:field   "title"
                                               :message "Field title is not present"}]}
                       (validation/validate {} {"title" {:validate/type :validate/mandatory}}))))
  (testing "min is validated"
    (is (validation/validate {:title "Mafic Rock"} {"title" {:validate/type  :validate/min
                                                             :validate/value 1}}))
    (is (validation/validate {:title "Mafic Rock"} {"title" {:validate/type  :validate/min
                                                             :validate/value 10}}))
    (is (validation/validate {:age 18} {"age" {:validate/type :validate/min
                                               :validate/value 18}}))
    (is (thrown-match? ExceptionInfo
                       {:type                :bad-format
                        :validation-messages [{:field   "title"
                                               :message "Field title must have a minimum size of 11"}]}
                       (validation/validate {:title "Mafic Rock"} {"title" {:validate/type  :validate/min
                                                                            :validate/value 11}})))
    (is (thrown-match? ExceptionInfo
                       {:type                :bad-format
                        :validation-messages [{:field   "title"
                                               :message "Field title is a custom message with size of 11"}]}
                       (validation/validate {:title "Mafic Rock"} {"title" {:validate/type :validate/min
                                                                            :validate/value 11
                                                                            :validate/message "Field %s is a custom message with size of %s"}}))))

  (testing "max is validated"
    (is (validation/validate {:title "Mafic Rock"} {"title" {:validate/type :validate/max
                                                             :validate/value 10}}))
    (is (validation/validate {:title "Mafic Rock"} {"title" {:validate/type :validate/max
                                                             :validate/value 15}}))
    (is (validation/validate {:age 18} {"age" {:validate/type :validate/max
                                               :validate/value 18}}))
    (is (thrown-match? ExceptionInfo
                       {:type                :bad-format
                        :validation-messages [{:field   "title"
                                               :message "Field title must have a maximum size of 9"}]}
                       (validation/validate {:title "Mafic Rock"} {"title" {:validate/type :validate/max
                                                                            :validate/value 9}}))))
  (testing "max and min are validated"
    (is (validation/validate {:title "Mafic Rock"} {"title" [{:validate/type :validate/min
                                                              :validate/value 10}
                                                             {:validate/type :validate/max
                                                              :validate/value 10}]}))
    (is (validation/validate {:title "Mafic Rock"} {"title" [{:validate/type :validate/min
                                                              :validate/value 1}
                                                             {:validate/type :validate/max
                                                              :validate/value 11}]}))
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
                                                                                   :validate/value 20}})))
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
                                                                            "age" [{:validate/type :validate/min
                                                                                    :validate/value 20}]}))))
  (testing "regex is validated"
    (is (validation/validate {:title "Rock"} {"title" {:validate/type :validate/regex
                                                       :validate/value #"[A-Za-z]{4}"}}))
    (is (validation/validate {:age "18"} {"age" {:validate/type :validate/regex
                                                 :validate/value #"[1-9]{1,2}"}}))
    (is (thrown-match? ExceptionInfo
                       {:type                :bad-format
                        :validation-messages [{:field   "title"
                                               :message "Field title is not valid"}]}
                       (validation/validate {:title "Mafic Rock"} {"title" {:validate/type :validate/regex
                                                                            :validate/value #"[A-Za-z]{4}"}})))
    (is (thrown-match? ExceptionInfo
                       {:type                :bad-format
                        :validation-messages [{:field   "title"
                                               :message "Field title is not valid"}]}
                       (validation/validate {:title "Mafic Rock"} {"title" [{:validate/type :validate/regex
                                                                             :validate/value #"[A-Za-z]{4}"}]})))
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
    (is (thrown-match? ExceptionInfo
                       {:type                :bad-format
                        :validation-messages [{:field   "title"
                                               :message "Field title is not valid"}]}
                       (validation/validate {:title "Mafic Rock"} {"title" {:validate/type :validate/custom
                                                                            :validate/value (fn [value]
                                                                                              (= value "Sedimentary Rock"))}})))
    (is (thrown-match? ExceptionInfo
                       {:type                :bad-format
                        :validation-messages [{:field   "title"
                                               :message "Field title has such a great message"}]}
                       (validation/validate {:title "Mafic Rock"} {"title" {:validate/type :validate/custom
                                                                            :validate/value (fn [value]
                                                                                              (= value "Sedimentary Rock"))
                                                                            :validate/message "Field %s has such a great message"}})))))
