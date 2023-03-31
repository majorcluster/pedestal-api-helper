(ns pedestal-api-helper.async-interceptors
  (:require [clojure.core.async :as async]))

(defn- push-async-channel
  [context channel-id channel]
  (assoc-in context [:request :async-channels channel-id] channel))

(defn- push-async-data
  ([context data]
   (assoc-in context [:request :async-data] (conj (get-in context [:request :async-data] [])
                                                  data)))
  ([context id data]
   (assoc-in context [:request :async-data id] data)))

(defn- executes-context-async-fn
  [m-fn context]
  (async/go
    (do
      (println "exit executes-context-async-fn")
      (m-fn context)))
  context)

(defn async-fetch-output-interceptor
  "Creates an async interceptor that receives a `{:name string? :enter fn? :leave fn?}` as param.
   It creates a channel and adds it the context `:async-channels` map with interceptor `:name` as key.
   The optional `:enter` function is triggered and the result is executed async and put to the channel while the queue goes on.
   The optional `:leave` function is triggered async, but no result is stored anywhere"
  [m]
  (let [enter-m (if (:enter m) (assoc m :enter (fn [context]
                                                 (let [chan (async/chan)]
                                                   (async/go
                                                     (async/>! chan (get-in (push-async-data context (:name m) ((:enter m) context)) [:request :async-data])))
                                                   (push-async-channel context (:name m) chan))))
                    {})
        leave-m (if (:leave m) (assoc m :leave #(executes-context-async-fn (:leave m) %))
                    {})]
    (merge enter-m leave-m)))

(defn async-output-interceptor
  "Creates an async interceptor that receives a `{:name string? :enter fn?}` as param.
   It executes the `:enter` and/or `:leave` function async and the queue goes on while it is executed by the async threads in parallel."
  [m]
  (let [enter-m (if (:enter m) (assoc m :enter #(executes-context-async-fn (:enter m) %))
                    {})
        leave-m (if (:leave m) (assoc m :leave #(executes-context-async-fn (:leave m) %))
                    {})]
    (merge enter-m leave-m)))

(defn async-blocker-interceptor
  "Async blocker interceptor, the one mandatory to resolve async-channels created by all `async-fetch-output-interceptor`
   before jumping into the handlers. Optional merge-data-fn is the function used to merge different data returned by the interceptors.
   The default fn is `merge`"
  ([merge-data-fn]
   {:name  ::async-blocker
    :enter (fn [context]
             (println "async-blocker-interceptor")
             (let [channels (-> context :request :async-channels vals)
                   merged-channels (async/map merge-data-fn channels)
                   async-data (async/<!! merged-channels)
                   context (assoc-in context [:request :async-data] async-data)]
               (async/close! merged-channels)
               context))})
  ([]
   (async-blocker-interceptor merge)))
