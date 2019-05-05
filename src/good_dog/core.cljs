(ns good-dog.core
  (:require [good-dog.impl.cljs :as impl]
            [good-dog.protocols :as gdp]
            [sieppari.core :as sieppari]
            [good-dog.interceptors :as incpt])
  (:require-macros [good-dog.core]))

(defn array-buffer [res] (gdp/-array-buffer res))

(defn form-data [res] (gdp/-form-data res))

(defn json [res] (gdp/-json res))

(defn text [res] (gdp/-text res))

(defn fetch-handler [request]
  (-> (impl/fetch (get request ::opts)
                  (get request ::url))))

(defn fetch
  ([opts]
   (js/Promise. (fn execute-fetch [resolve reject]
                  (sieppari/execute (into (or (:interceptors opts) [])
                                          [fetch-handler])
                                    {::opts opts ::url (:url opts)}
                                    resolve
                                    reject)))))

(defn- conjv [v & xs]
  (apply conj (or v []) xs))

(defn fetch-json [opts]
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

  (-> (fetch-json {:fetch node-fetch
                   :url "https://jsonplaceholder.typicode.com/todos/2"})
      (.then prn))

  (binding [impl/*fetch-impl* node-fetch]
    (-> (fetch {:url "https://jsonplaceholder.typicode.com/todos/2"})
        (.then json)
        (.then (comp prn js->clj))))

  (require-macros 'good-dog.core)
  (with-fetch node-fetch
    (-> (fetch {:url "https://jsonplaceholder.typicode.com/todos/2"})
        (.then prn)))
  )
