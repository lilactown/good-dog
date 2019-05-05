# Good-Dog 🦴🐕

A ClojureScript library for fetching data over HTTP(S), based around the
[Fetch standard API](https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API)
available in modern browsers.


## Usage

```clojure
(ns my-app.core
  (:require [good-dog.core :as gd :refer [fetch]]))

;; fetch response from a URL
(-> (fetch {:url "https://lilac.town"})
    ;; Reads the response as text
    (.then gd/text)
    (.then prn))

;; fetch response from a URL and automatically read it as JSON,
;; then convert it to CLJS data
(-> (fetch-json {:url "http://jsonplaceholder.typicode.com/todos/1"})
    (.then prn)
    ;; If an error occurs while reading as JSON or converting to CLJS data
    (.catch #(prn "error" %)))
```


### Options

`good-dog.core/fetch` takes all of the same options as the [native Fetch](https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API/Using_Fetch#Supplying_request_options)
API specifies. It expects a map with keywords as keys.

It has the following differences:
- The URL to send the request to is passed in via the `:url` key
- It supports [Sieppari interceptors](https://github.com/metosin/sieppari) via the `:interceptors` key
- A different `fetch` implementation may be provided via the `:fetch` key


### Response

The object resolved by `good-dog.core/fetch` is the native [Response](https://developer.mozilla.org/en-US/docs/Web/API/Response) type.
Provided in this library are also a number of helpers for handling common operations
such as converting to JSON, FormData, etc.


### Interceptors

`good-dog.core/fetch` also provides the ability to add [Sieppari interceptors](https://github.com/metosin/sieppari)
to a request in order to transform the request and response before it resolves
completely to the consumer.

A simple example:

```clojure
(def json {:leave
           (fn json-leave [{:keys [response] :as ctx}]
             ;; reads the response body to completion and converts to JSON
             (-> (gd/json response)
                 (.then #(assoc ctx :response %))))})
                 
(def ->cljs {:leave (fn to-cljs [ctx]
                      ;; converts JSON response to CLJS data
                      (update ctx :response js->clj :keywordize-keys true))})

(-> (fetch {:fetch node-fetch
            :interceptors [->cljs json]
            :url "https://jsonplaceholder.typicode.com/todos/2"})
    (.then prn))
```

This can be helpful for adding things like auth headers to a request, coercing
data, etc. in a general way that can be used throughout your applications.


### Unsupported browsers and Node.js

For JS environments that do not come with `js/window.fetch` such as Node.js or
older browsers, you may provide your own implementation of `js/fetch` via the
`:fetch` key in the options.

Example using https://github.com/bitinn/node-fetch/:

```clojure
(ns my-app.core
  (:require [good-dog.core :as gd :refer [fetch]]
            ["node-fetch" :as node-fetch]))

(-> (fetch-json {:fetch node-fetch
                 :url "http://jsonplaceholder.typicode.com/todos/1"})
    (.then prn)
    (.catch #(prn "error" %)))
```

## Issues

Please [file an issue on GitHub](https://github.com/Lokeh/good-dog/issues) if any are found.


## License

Copyright 2019 Will Acton. MIT Licensed.
