# pedestal-api-helper

A Clojure library designed to extend usual pedestal api setup, providing: 
* useful interceptors
* useful utils for dealing with validation and filtering, for example

[Clojars link](https://clojars.org/org.clojars.majorcluster/pedestal-api-helper)

## Usage

* Add the dependency: 
```clojure
[org.clojars.majorcluster/pedestal-api-helper "0.2.0"]
```

### Examples:
* Interceptors:
```clojure
(:require [clj.pedestal-api-helper.interceptors :as api-h.i]
  [...])

(def common-interceptors
  [(p.body-params/body-params)
   interceptors/json-out])

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


## License

Copyright © 2022 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
