(ns pedestal-api-helper.validation
  (:use clojure.pprint))

(defn- validate-mandatory
  ([body field-name raw-message]
   (let [field-ks (keyword field-name)
         valid? (contains? body field-ks)]
     (cond valid? {:validate/field field-name
                   :validate/valid true}
           :else {:validate/field field-name
                  :validate/result-message (format raw-message field-name),
                  :validate/valid false})))
  ([body field-name]
   (validate-mandatory body field-name "Field %s is not present")))

(defn- validate-min
  ([body field-name validation-value raw-message]
   (let [field-ks (keyword field-name)
         field-value (field-ks body)
         number-value (cond (coll? field-value) (count field-value)
                            (map? field-value) (count field-value)
                            (string? field-value) (.length field-value)
                            (number? field-value) field-value)
         valid? (>= number-value validation-value)]
     (cond valid? {:validate/field field-name
                   :validate/valid true}
           :else {:validate/field field-name
                  :validate/result-message (format raw-message field-name validation-value),
                  :validate/valid false})))
  ([body field-name validation-value]
   (validate-min body field-name validation-value "Field %s must have a minimum size of %s")))

(defn- validate-max
  ([body field-name validation-value raw-message]
   (let [field-ks (keyword field-name)
         field-value (field-ks body)
         number-value (cond (coll? field-value) (count field-value)
                            (map? field-value) (count field-value)
                            (string? field-value) (.length field-value)
                            (number? field-value) field-value)
         valid? (<= number-value validation-value)]
     (cond valid? {:validate/field field-name
                   :validate/valid true}
           :else {:validate/field field-name
                  :validate/result-message (format raw-message field-name validation-value),
                  :validate/valid false})))
  ([body field-name validation-value]
   (validate-max body field-name validation-value "Field %s must have a maximum size of %s")))

(defn- validate-regex
  ([body field-name validation-value raw-message]
   (let [field-ks (keyword field-name)
         field-value (field-ks body)
         valid? (cond (string? field-value) (re-matches validation-value field-value)
                      :else false)]
     (cond valid? {:validate/field field-name
                   :validate/valid true}
           :else {:validate/field field-name
                  :validate/result-message (format raw-message field-name),
                  :validate/valid false})))
  ([body field-name validation-value]
   (validate-regex body field-name validation-value "Field %s is not valid")))

(defn- validate-custom
  ([body field-name validation-value raw-message]
   (let [field-ks (keyword field-name)
         field-value (field-ks body)
         valid? (validation-value field-value)]
     (cond valid? {:validate/field field-name
                   :validate/valid true}
           :else {:validate/field field-name
                  :validate/result-message (format raw-message field-name),
                  :validate/valid false})))
  ([body field-name validation-value]
   (validate-custom body field-name validation-value "Field %s is not valid")))

(defn- exec-validation-with-value
  [validation-fn body key validation-value raw-message]
  (cond (nil? raw-message) (validation-fn body key validation-value)
        :else (validation-fn body key validation-value raw-message)))

(defn- exec-validation
  [validation-fn body key raw-message]
  (cond (nil? raw-message) (validation-fn body key)
        :else (validation-fn body key raw-message)))

(defn- validate-by-specs
  [specs body key]
  (let [type (get specs :validate/type :validate/mandatory)
        raw-message (:validate/message specs)
        validation-value (get specs :validate/value (fn [_] true))]
    (cond (= type :validate/mandatory)  (exec-validation validate-mandatory body key raw-message)
          (= type :validate/min)        (exec-validation-with-value validate-min body key validation-value raw-message)
          (= type :validate/max)        (exec-validation-with-value validate-max body key validation-value raw-message)
          (= type :validate/regex)      (exec-validation-with-value validate-regex body key validation-value raw-message)
          (= type :validate/custom)     (exec-validation-with-value validate-custom body key validation-value raw-message)
          :else true)))

(defn- map-invalid-messages
  [result]
  (when-not (:validate/valid result)
    {:field   (:validate/field result)
     :message (:validate/result-message result)}))

(defn- flatten-invalid-messages
  [result]
  (cond (coll? result) (flatten result)
        :else result))

(defn validate
  "validates fields <br>
  - *body* ^map: the body map where key-values will be checked and/or removed
  - *fields* ^map: <br>
  {\"field-name\" [{:validate/type :validate/mandatory & :validate/message \"%s is ...\"},
  <br> {:validate/type :validate/min, :validate/value 12 & :validate/message \"% is mandatory\"},
  <br> {:validate/type :validate/max, :validate/value 40 & :validate/message \"% is ...\"},
  <br> {:validate/type :validate/regex, :validate/value #\"^[\\d]{1,2}$\" & :validate/message \"% is ...\"},
  <br> {:validate/type :validate/custom, :validate/value fn & :validate/message \"% is ...\"}]}"
  [body fields]
  (let [body (cond (nil? body) {}
                   :else body)
        validation-result (map (fn [[key specs]]
                                 (cond (map? specs) (validate-by-specs specs body key)
                                       :else (reduce (fn [messages spec]
                                                       (conj messages (validate-by-specs spec body key)))
                                                     []
                                                     specs)))
                          fields)
        validation-result  (flatten-invalid-messages validation-result)
        not-valid-messages (map map-invalid-messages validation-result)
        not-valid-messages (into [] (filter second not-valid-messages))]
    (cond (or (nil? not-valid-messages)
              (empty? not-valid-messages)) true
          :else ((throw (ex-info "Field validation failed" {:type                :bad-format
                                                            :validation-messages not-valid-messages}))
                 ))))
