(ns pedestal-api-helper.params-helper
  (:require [pedestal-api-helper.validation :as v])
  (:import (java.util UUID)))

(def uuid-pattern
  #"^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")

(defn uuid
  []
  (UUID/randomUUID))

(defn uuid-as-string
  [uuid]
  (.toString uuid))

(defn is-uuid
  [id]
  (cond (string? id) (re-matches uuid-pattern id)
        :else false))

(defn validate-mandatory
  "validates mandatory fields <br>
  - *body* ^map: the body map where key-values will be checked and/or removed
  - *fields* ^coll: [\"field-0-name\",\"field-n-name\"]
  - & *field-message* ^string: Default field message"
  ([body fields message-untranslated]
   (let [fields (map #(keyword %) fields)
         not-present (filter (fn [field]
                               (not (contains? body field))) fields)
         not-present-messages (map (fn [field]
                                     {:field   (name field)
                                      :message (format message-untranslated field)})
                                   not-present)]
     (cond (empty? not-present) true
           :else ((throw (ex-info "Mandatory fields validation failed" {:type                :bad-format
                                                                        :validation-messages not-present-messages}))
                  ))))
  ([body fields]
   (validate-mandatory body fields "Field %s is not present")))

(defn extract-field-value
  "extract field value, by converting string into other objects, uuid is the only conversion so far <br>
  - *field* ^ks: ks (field) to be extracted from *body*
  - *body* ^map: the body map where key-values will be extracted"
  [field body]
  (let [value (field body)
        is-uuid (is-uuid value)]
    (cond is-uuid (UUID/fromString value)
          :else value)))

(defn mop-fields
  "mop fields, removing unwanted key-values <br>
  - *body* ^map: the body map where key-values will be checked and/or removed
  - *fields* ^coll: [\"field-0-name\",\"field-n-name\"] => accepted fields, the other ones will be removed"
  [body fields]
  (let [fields (map #(keyword %) fields)
        cleaned (reduce (fn [map field]
                          (let [has-field? (contains? body field)
                                value (extract-field-value field body)]
                            (if has-field? (assoc map field value)
                                           map)))
                        {}
                        fields)]
    cleaned))

(defn validate-and-mop!!
  "validates fields and mop then, removing unwanted key-values <br>
  - *body* ^map: the body map where key-values will be checked and/or removed
  - *to-validate* ^map: {\"field-name\"
  [{:validate/type :validate/mandatory & :validate/message \"%s is ...\"},\n  <br>
  {:validate/type :validate/min, :validate/value 12 & :validate/message \"% is mandatory\"},\n  <br>
  {:validate/type :validate/max, :validate/value 40 & :validate/message \"% is ...\"},\n  <br>
  {:validate/type :validate/regex, :validate/value #\"^[\\d]{1,2}$\" & :validate/message \"% is ...\"},\n  <br>
  {:validate/type :validate/custom, :validate/value fn & :validate/message \"% is ...\"}]}
  - *to-validate* ^coll: [\"field-0-name\",\"field-n-name\"] => validates only mandatoryness.
  - *accepted* ^coll: [\"field-0-name\",\"field-n-name\"] => accepted fields, the other ones will be removed
  - & *field-message* ^string: Default field message, is used just if to-validate is coll"
  ([body
    to-validate
    accepted
    field-message]
   (cond (map? to-validate) (v/validate body to-validate)
         :else (validate-mandatory body to-validate field-message))
   (mop-fields body accepted))
  ([body
    to-validate
    accepted]
   (cond (map? to-validate) (v/validate body to-validate)
         :else (validate-mandatory body to-validate))
   (mop-fields body accepted)))
