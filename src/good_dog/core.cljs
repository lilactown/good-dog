(ns good-dog.core
  (:require [good-dog.impl.cljs :as impl]
            [good-dog.protocols :as gdp]
            [sieppari.core :as sieppari]
            [good-dog.interceptors :as incpt])
  (:require-macros [good-dog.core]))

(defn array-buffer
  "Reads a fetch Response and converts it to an Array Buffer."
  [res] (gdp/-array-buffer res))

(defn form-data
  "Reads a fetch Response and converts it to a FormData object."
  [res] (gdp/-form-data res))

(defn json
  "Reads a fetch Response and converts it to JSON."
  [res] (gdp/-json res))

(defn text
  "Reads a fetch Response and converts it to a string."
  [res] (gdp/-text res))

(defn fetch-handler [request]
  (-> (impl/fetch (get request ::opts)
                  (get request ::url))
      ;; Sieparri doesn't handle rejections, but will handle resolved errors
      (.catch (fn [e]
                e))))

(defn fetch
  "Fetches data from a URL over HTTP(S). Returns a Promise.

  Takes the same options as native fetch: https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API/Using_Fetch#Supplying_request_options.

  Differences:
  - URL is passed in as key `:url`
  - Supports sieppari interceptors via `:interceptors`
  - A different `fetch` implementation may be passed in via `:fetch`

  By default, uses `js/window.fetch`. For Node.js, consider using something
  like https://github.com/bitinn/node-fetch/."
  ([opts]
   (js/Promise. (fn execute-fetch [resolve reject]
                  (sieppari/execute (into (or (:interceptors opts) [])
                                          [fetch-handler])
                                    {::opts opts ::url (:url opts)}
                                    resolve
                                    reject)))))

(defn- conjv [v & xs]
  (apply conj (or v []) xs))

(defn fetch-json
  "Fetches JSON data from a URL over HTTP(S), deeply converting the JSON data to
  a CLJS data. Returns a Promise.

  It's a shorthand for:
  ```
  (fetch {:interceptors [good-dog.interceptors/->cljs
                         good-dog.interceptors/json]
          ...})
  ```"
  [opts]
  (fetch (update opts :interceptors conjv incpt/->cljs incpt/json)))


(comment
  (require '["node-fetch" :as node-fetch])
  (require '[good-dog.interceptors :as incpt])
  (-> (fetch {:fetch node-fetch
              :interceptors [incpt/text]
              :url "http://lilac.town"})
      #_(.then (fn [res] (text res)))
      ;; (.then (fn [text] (:ok text)))
      #_(.then text)
      (.then prn))

  (-> (fetch {:fetch node-fetch
              :interceptors [incpt/->cljs
                             incpt/json]
              :url "https://jsonplaceholder.typicode.com/todos/2"})
      (.then prn))

  ;; error
  (-> (fetch-json {:fetch node-fetch
                   :url "htt://jsonplaceholder.typicode.com/todos"})
      (.then prn)
      (.catch #(prn "error" %)))

  (-> (fetch {:fetch node-fetch
              :url "https://lilac.town"
              :interceptors [incpt/json]})
      (.then prn)
      (.catch #(prn "error" %)))

  (binding [impl/*fetch-impl* node-fetch]
    (-> (fetch {:url "https://jsonplaceholder.typicode.com/todos/2"})
        (.then json)
        (.then (comp prn js->clj))))

  (require-macros 'good-dog.core)
  (with-fetch node-fetch
    (-> (fetch {:url "https://jsonplaceholder.typicode.com/todos/2"})
        (.then prn)))
  )
