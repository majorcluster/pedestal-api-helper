(ns pedestal-api-helper.validation)

(defn- validate-mandatory
  ([body field-name raw-message]
   (let [field-ks (keyword field-name)
         field-value (field-ks body)
         valid? (cond (string? field-value) (seq field-value)
                      :else (not (nil? field-value)))]
     (cond valid? {:validate/field field-name
                   :validate/valid true}
           :else {:validate/field field-name
                  :validate/result-message (format raw-message field-name),
                  :validate/valid false})))
  ([body field-name]
   (validate-mandatory body field-name "Field %s is not present")))

(defn- is-absent-and-ignored
  [body field-ks igore-if-absent?]
  (and igore-if-absent? (not (contains? body field-ks))))

(defn- validate-min
  ([body field-name validation-value raw-message igore-if-absent?]
   (let [field-ks (keyword field-name)
         field-value (field-ks body)
         number-value (cond (coll? field-value) (count field-value)
                            (map? field-value) (count field-value)
                            (string? field-value) (.length field-value)
                            (number? field-value) field-value)
         valid? (or (is-absent-and-ignored body field-ks igore-if-absent?)
                    (and (contains? body field-ks) (>= number-value validation-value)))]
     (cond valid? {:validate/field field-name
                   :validate/valid true}
           :else {:validate/field field-name
                  :validate/result-message (format raw-message field-name validation-value),
                  :validate/valid false})))
  ([body field-name validation-value igore-if-absent?]
   (validate-min body field-name validation-value "Field %s must have a minimum size of %s" igore-if-absent?)))

(defn- validate-max
  ([body field-name validation-value raw-message igore-if-absent?]
   (let [field-ks (keyword field-name)
         field-value (field-ks body)
         number-value (cond (coll? field-value) (count field-value)
                            (map? field-value) (count field-value)
                            (string? field-value) (.length field-value)
                            (number? field-value) field-value)
         valid? (or (is-absent-and-ignored body field-ks igore-if-absent?)
                    (and (contains? body field-ks) (<= number-value validation-value)))]
     (cond valid? {:validate/field field-name
                   :validate/valid true}
           :else {:validate/field field-name
                  :validate/result-message (format raw-message field-name validation-value),
                  :validate/valid false})))
  ([body field-name validation-value igore-if-absent?]
   (validate-max body field-name validation-value "Field %s must have a maximum size of %s" igore-if-absent?)))

(defn- validate-regex
  ([body field-name validation-value raw-message igore-if-absent?]
   (let [field-ks (keyword field-name)
         field-value (field-ks body)
         valid? (cond (is-absent-and-ignored body field-ks igore-if-absent?) true
                      (string? field-value) (re-matches validation-value field-value)
                      :else false)]
     (cond valid? {:validate/field field-name
                   :validate/valid true}
           :else {:validate/field field-name
                  :validate/result-message (format raw-message field-name),
                  :validate/valid false})))
  ([body field-name validation-value igore-if-absent?]
   (validate-regex body field-name validation-value "Field %s is not valid" igore-if-absent?)))

(defn- validate-custom
  ([body field-name validation-value raw-message _]
   (let [field-ks (keyword field-name)
         field-value (field-ks body)
         valid? (validation-value field-value)]
     (cond valid? {:validate/field field-name
                   :validate/valid true}
           :else {:validate/field field-name
                  :validate/result-message (format raw-message field-name),
                  :validate/valid false})))
  ([body field-name validation-value _]
   (validate-custom body field-name validation-value "Field %s is not valid" _)))

(defn- exec-validation-with-value
  [validation-fn body key validation-value raw-message igore-if-absent?]
  (cond (nil? raw-message) (validation-fn body key validation-value igore-if-absent?)
        :else (validation-fn body key validation-value raw-message igore-if-absent?)))

(defn- exec-validation
  [validation-fn body key raw-message]
  (cond (nil? raw-message) (validation-fn body key)
        :else (validation-fn body key raw-message)))

(defn- validate-by-specs
  [specs body key]
  (let [type (get specs :validate/type :validate/mandatory)
        raw-message (:validate/message specs)
        validation-value (get specs :validate/value (fn [_] true))
        igore-if-absent? (get specs :validate/ignore-if-absent false)]
    (cond (= type :validate/mandatory)  (exec-validation validate-mandatory body key raw-message)
          (= type :validate/min)        (exec-validation-with-value validate-min body key validation-value raw-message igore-if-absent?)
          (= type :validate/max)        (exec-validation-with-value validate-max body key validation-value raw-message igore-if-absent?)
          (= type :validate/regex)      (exec-validation-with-value validate-regex body key validation-value raw-message igore-if-absent?)
          (= type :validate/custom)     (exec-validation-with-value validate-custom body key validation-value raw-message igore-if-absent?)
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
  "[docs](https://github.com/majorcluster/pedestal-api-helper/tree/main/doc/validation.md)"
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
                                                            :validation-messages not-valid-messages}))))))
