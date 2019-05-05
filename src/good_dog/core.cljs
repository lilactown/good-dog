(ns good-dog.core
  (:require [good-dog.impl.cljs :as impl]
            [good-dog.protocols :as gdp]
            [sieppari.core :as sieppari])
  (:require-macros [good-dog.core]))

(defn array-buffer [res] (gdp/-array-buffer res))

(defn form-data [res] (gdp/-form-data res))

(defn json [res] (gdp/-json res))

(defn text [res] (gdp/-text res))

(def fetch-interceptor {:enter (fn fetch-enter [ctx]
                                 (-> (impl/fetch (get-in ctx [:request ::opts])
                                                 (get-in ctx [:request ::url]))
                                     (.then (fn [res]
                                              (-> ctx
                                                  (assoc-in [:request ::response] res)
                                                  (assoc-in [:request :good-dog/result] res))))))})

(defn fetch
  ([url]
   (fetch nil url))
  ([opts url]
   (js/Promise. (fn execute-fetch [resolve reject]
                  (sieppari/execute (reduce conj (or (:interceptors opts) [])
                                            [fetch-interceptor
                                             (fn handle-result [req] (:good-dog/result req))])
                                    {::opts opts ::url url}
                                    resolve
                                    reject)))))

(comment
  (require '["node-fetch" :as node-fetch])
  (require '[good-dog.interceptors :as incpt])
  (-> (fetch {:fetch node-fetch
              :interceptors [incpt/text]} "http://lilac.town")
      #_(.then (fn [res] (text res)))
      ;; (.then (fn [text] (:ok text)))
      #_(.then text)
      (.then prn))

  (-> (fetch {:fetch node-fetch
              ;; :interceptors [incpt/->cljs
              ;;                incpt/json]
              }
             "https://jsonplaceholder.typicode.com/todos/2")
      (.then prn))

  (binding [impl/*fetch-impl* node-fetch]
    (-> (fetch "https://jsonplaceholder.typicode.com/todos/2")
        (.then prn)))

  (require-macros 'good-dog.core)
  (with-fetch node-fetch
    (-> (fetch "https://jsonplaceholder.typicode.com/todos/2")
        (.then prn))))
