## interceptors

### Index
- Symbols
    - [json-out](#json-out)
- Functions

### Symbols
- <h3><a id='json-out'></a><span style="color:green">json-out</span><br></h3>
Map having :leave as fn[context] and rewriting response body from map into json <br>
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

### Functions
