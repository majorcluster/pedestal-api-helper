(ns pedestal-api-helper.params-helper
  (:require [pedestal-api-helper.validation :as v])
  (:import (java.util UUID)))

(def uuid-pattern
  #"^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")

(defn uuid
  "[docs](https://github.com/mtsbarbosa/pedestal-api-helper/tree/main/doc/params_helper.md)"
  []
  (UUID/randomUUID))

(defn uuid-as-string
  "[docs](https://github.com/mtsbarbosa/pedestal-api-helper/tree/main/doc/params_helper.md)"
  [uuid]
  (.toString uuid))

(defn is-uuid
  "[docs](https://github.com/mtsbarbosa/pedestal-api-helper/tree/main/doc/params_helper.md)"
  [id]
  (cond (string? id) (re-matches uuid-pattern id)
        :else false))

(defn validate-mandatory
  "[docs](https://github.com/mtsbarbosa/pedestal-api-helper/tree/main/doc/params_helper.md)"
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
  "[docs](https://github.com/mtsbarbosa/pedestal-api-helper/tree/main/doc/params_helper.md)"
  [field body]
  (let [value (field body)
        is-uuid (is-uuid value)]
    (cond is-uuid (UUID/fromString value)
          :else value)))

(defn mop-fields
  "[docs](https://github.com/mtsbarbosa/pedestal-api-helper/tree/main/doc/params_helper.md)"
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
  "[docs](https://github.com/mtsbarbosa/pedestal-api-helper/tree/main/doc/params_helper.md)"
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
