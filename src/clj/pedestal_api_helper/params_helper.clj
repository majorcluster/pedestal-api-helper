(ns pedestal-api-helper.params-helper
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
  ([body fields message-untranslated]
    (let [fields (map #(keyword %) fields)
          not-present (filter (fn [field]
                                (not (contains? body field))) fields)
          not-present-messages (map (fn [field]
                                      {:field (name field)
                                       :message (format message-untranslated field)})
                                    not-present)]
      (cond (empty? not-present) true
            :else ((throw (ex-info "Mandatory fields validation failed" {:type :bad-format
                                                                         :validation-messages not-present-messages}))
                   ))))
  ([body fields]
   (validate-mandatory body fields "Field %s is not present")))

(defn extract-field-value
  [field body]
  (let [value (field body)
        is-uuid (is-uuid value)]
    (cond is-uuid (UUID/fromString value)
          :else value)))

(defn mop-fields
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
  [body
   mandatory
   accepted]
  (validate-mandatory body mandatory)
  (mop-fields body accepted))
