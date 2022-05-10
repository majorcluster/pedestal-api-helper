(ns clj.pedestal-api-helper.params-helper-test
  (:require [clojure.test :refer :all]
            [pedestal-api-helper.params-helper :refer :all]
            [matcher-combinators.test])
  (:import (clojure.lang ExceptionInfo)))

(deftest validate-mandatory-test
  (testing "when all mandatory are present returns true"
    (is (validate-mandatory {:name "Marx" :age 46} ["name" "age"]))
    (is (validate-mandatory {:name "Lenin" :age 45} []))
    (is (validate-mandatory {:name "Sankara" :age 38} ["name"]))
    (is (validate-mandatory {} []))
    (is (validate-mandatory nil [])))
  (testing "when mandatory are not present throws ex-info"
    (is (thrown-match? ExceptionInfo
                       {:type :bad-format
                        :validation-messages [{:field "age"
                                               :message "Field :age is not present"}]}
                       (validate-mandatory {:name "Lenin"} ["name" "age"])))
    (is (thrown-match? ExceptionInfo
                       {:type :bad-format
                        :validation-messages [{:field "name"
                                               :message "Field :name is not present"},
                                              {:field "age"
                                               :message "Field :age is not present"}]}
                       (validate-mandatory {} ["name" "age"])))
    (is (thrown-match? ExceptionInfo
                       {:type :bad-format
                        :validation-messages [{:field "age"
                                               :message "Field :age is not present"}]}
                       (validate-mandatory {:name "Lenin"} ["age"])))
    (is (thrown-match? ExceptionInfo
                       {:type :bad-format
                        :validation-messages [{:field "age"
                                               :message "Field :age is not present"}]}
                       (validate-mandatory nil ["age"])))
    ))

(deftest extract-field-value-test
  (testing "when being not uuid return right type"
    (is (= "17"
           (extract-field-value :age {:age "17"})))
    (is (= 34
           (extract-field-value :age {:age 34})))
    )
  (testing "when being uuid it gets converted"
    (let [new-uuid (uuid)
          uuid-as-string (uuid-as-string new-uuid)]
      (is (= new-uuid
             (extract-field-value :id {:id uuid-as-string}))))))

(deftest mop-fields-test
  (testing "when all allowed fields are present returns body"
    (is (= {:name "Lenin"}
           (mop-fields {:name "Lenin"} ["name"])))
    (is (= {:name "Lenin" :age 47}
           (mop-fields {:name "Lenin" :age 47} ["name" "age"])))
    (is (= {}
           (mop-fields {} []))))
  (testing "when not allowed fields are present they are removed"
    (is (= {:name "Lenin"}
           (mop-fields {:name "Lenin" :hacking "trying"} ["name"])))
    (is (= {:name "Lenin" :age 47}
           (mop-fields {:name "Lenin" :age 47 :hacking "trying"} ["name" "age"])))
    (is (= {}
           (mop-fields {:hacking "trying"} []))))
  (let [new-uuid (uuid)
        uuid-as-string (uuid-as-string new-uuid)]
    (testing "when having uuid it is converted"
      (= {:name "Lenin" :id new-uuid}
         (mop-fields {:name "Lenin", :id uuid-as-string} ["name" "id"])))))

(deftest validate-and-mop-test
  (testing "to-validate is coll therefore mandatory validation is done"
    (is (validate-and-mop!! {:name "Marx" :age 46} ["name" "age"] ["name" "age"]))
    (is (thrown-match? ExceptionInfo
                       {:type :bad-format
                        :validation-messages [{:field "age"
                                               :message "Field :age is not present"}]}
                       (validate-and-mop!! {:name "Lenin"} ["age"] ["age"])))
    (is (thrown-match? ExceptionInfo
                       {:type :bad-format
                        :validation-messages [{:field "age"
                                               :message "Field :age is not present"}]}
                       (validate-and-mop!! {:name "Lenin"} ["age"] ["age", "name"]))))
  (testing "to-validate is map and specific validations are triggered"
    (is (validate-and-mop!! {:name "Marx" :age 46} {"name" {:validate/type :validate/mandatory}
                                                    "age"  [{:validate/type :validate/mandatory}]} ["name" "age"]))
    (is (thrown-match? ExceptionInfo
                       {:type :bad-format
                        :validation-messages [{:field "age"
                                               :message "Field age is not present"}]}
                       (validate-and-mop!! {:name "Lenin"} {"age" [{:validate/type :validate/mandatory}]} ["age"])))
    (is (thrown-match? ExceptionInfo
                       {:type :bad-format
                        :validation-messages [{:field "age"
                                               :message "Field age is not present"}]}
                       (validate-and-mop!! {:name "Lenin"} {"age" {:validate/type :validate/mandatory}} ["age", "name"])))))
