# pedestal-api-helper

[![Clojars Project](https://img.shields.io/clojars/v/org.clojars.majorcluster/pedestal-api-helper.svg)](https://clojars.org/org.clojars.majorcluster/pedestal-api-helper)

A Clojure library designed to extend usual pedestal api setup, providing: 
* useful interceptors
* useful utils for dealing with validation and filtering, for example

[Clojars link](https://clojars.org/org.clojars.majorcluster/pedestal-api-helper)

## Usage

* Add the dependency: 
```clojure
[org.clojars.majorcluster/pedestal-api-helper "0.3.0"]
```

### Examples:
* Interceptors:
```clojure
(:require [clj.pedestal-api-helper.interceptors :as api-h.i]
  [...])

(def common-interceptors
  [(p.body-params/body-params)
   api-h.i/json-out])

(def all-routes
  (route/expand-routes
    #{["/status" :get (conj common-interceptors `r.status/get-status) :route-name :get-all-status]}))
```
* Validating and Filtering Params:
```clojure
(:require [clj.pedestal-api-helper.params-helper :as api-h.params]
  [...])

(defn post-status
  [request]
  (try
    (let [crude-body (:json-params request)
          mandatory-fields ["name"]
          allowed-fields ["name"]
          body (api-h.params/validate-and-mop!! crude-body mandatory-fields allowed-fields)]
      (...))
    (catch ExceptionInfo e
      {:status 400, :headers common-headers, :body {:message
                                                    (:validation-messages (.getData e))}})
    ))

(defn patch-status
  [request]
  (try
    (let [crude-body (:json-params request)
          mandatory-fields ["id","name"]
          allowed-fields ["id","name"]
          body (api-h.params/validate-and-mop!! crude-body mandatory-fields allowed-fields)]
      {:status 204})
    (catch ExceptionInfo e
      {:status 400, :headers common-headers, :body {:message
                                                    (:validation-messages (.getData e))}})
    ))
```

## Publish
### Requirements
* Leiningen (of course 😄) 
* GPG (mac => brew install gpg)
* Clojars account
* Enter clojars/tokens page in your account -> generate one and use for password
```shell
export GPG_TTY=$(tty) && lein deploy clojars
```

## Documentation
### pedestal-api-helper/interceptors
| Symbols     | Description |
| ----------- | ----------- |
| json-out    |    Map having :leave as fn[context] and rewriting response body from map into json    |

### pedestal-api-helper/params-helper
| Symbols     | Description |
| ----------- | ----------- |
| uuid-pattern | UUID string regex |

| Functions     | Description |
| ----------- | ----------- |
| uuid | returns a new random UUID <br> ``` (uuid) => 53bd29d3-9b41-4550-83cc-f970d49da04d```|
| uuid-as-string [uuid] | converts uuid into a string <br> ``` (uuid-as-string (uuid)) => "53bd29d3-9b41-4550-83cc-f970d49da04d"```|
| is-uuid [id] | if id param is a string, checks if it matches uuid regex, otherwise returns false <br> ```(is-uuid "53bd29d3-9b41-4550-83cc-f970d49da04d") => true```|
| validate-mandatory [body fields & message-untranslated = "Field %s is not present"] | checks if body map has mandatory keys, if not, throws an exception containing all missing fields in ExceptionInfo .getData :validation-messages <br> `(validate-mandatory {:name "Rosa"} ["name"]) => true` <br> `(validate-mandatory {} ["name"]) => ExceptionInfo thrown`|
| extract-field-value [field body] | gets value from the body using field ks, converting uuid's from string to UUID if needed <br> `(extract-field-value :name {:name "Rosa"}) => "Rosa"` <br> `(extract-field-value :id {:id "53bd29d3-9b41-4550-83cc-f970d49da04d"}) => #uuid "53bd29d3-9b41-4550-83cc-f970d49da04d"`|
| mop-fields [body fields] | Clean the body removing values not present in fields param <br> `(mop-fields {:name "Rosa" :age 41} ["name"]) => {:name "Rosa"}`|
| validate-and-mop!! [body mandatory accepted] | Validates and clean body by executing **validate-mandatory** and **mop-fields** |
