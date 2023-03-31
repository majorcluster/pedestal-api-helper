## Index
- Functions
  - [async-blocker-interceptor](#async-blocker-interceptor)
  - [async-fetch-output-interceptor](#async-fetch-output-interceptor)
  - [async-output-interceptor](#async-output-interceptor)

## Functions

- <h3><a id='async-blocker-interceptor'></a><span style="color:green">async-blocker-interceptor</span> [] [merge-data-fn]</h3><br>
  Async blocker interceptor, the one mandatory to resolve async-channels created by `async-fetch-output-interceptor`
  before jumping into the handlers. Optional merge-data-fn is the function used to merge different data returned by the interceptors.
  The default fn is `merge`. <br>
  <br>
  - merge-data-fn ^fn : optional merge function, when not sent, `merge` is used as default<br>
  - returns ^map context map <br>
```clojure
    (async-blocker-interceptor)
```
```clojure
    (async-blocker-interceptor my-merge-fn)
``` 

- <h3><a id='async-fetch-output-interceptor'></a><span style="color:green">async-fetch-output-interceptor</span> [m]</h3><br>
  Creates an async interceptor that receives a `{:name string? :enter fn? :leave fn?}` as param.<br>
  It creates a channel and adds it the context `:async-channels` map with interceptor `:name` as key.<br>
  The optional `:enter` function is triggered and the result is executed async and put to the channel while the queue goes on.<br>
  The optional `:leave` function is triggered async, but no result is stored anywhere <br>
  <br>
  - m ^map : interceptor map `{:name string? :enter fn? :leave fn?}` one of enter or leave is mandatory, both are also accepted<br>
  - returns ^map context map <br>
```clojure
    (async-fetch-output-interceptor {:name :async-i-name
                                     :enter (fn [context] 
                                              ;... my body sync or async to be paralellized
                                              )})
```

- <h3><a id='async-output-interceptor'></a><span style="color:green">async-output-interceptor</span> [m]</h3><br>
  Creates an async interceptor that receives a `{:name string? :enter fn?}` as param.<br>
  It executes the `:enter` and/or `:leave` function async and the queue goes on while it is executed by the async threads in parallel. <br>
  <br>
  - m ^map : interceptor map `{:name string? :enter fn? :leave fn?}` one of enter or leave is mandatory, both are also accepted<br>
  - returns ^map context map <br>
```clojure
    (async-output-interceptor {:name :async-i-name
                               :enter (fn [context] 
                                           ;... my body sync or async to be parallelized
                                           )})
```
