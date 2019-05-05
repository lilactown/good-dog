(ns good-dog.interceptors
  (:require [good-dog.protocols :as gdp]))

(def text {:leave
           (fn text-leave [ctx]
             (-> (get-in ctx [:request :good-dog.core/response])
                 (gdp/-text)
                 (.then #(assoc ctx :response %))))})

(def json {:leave
           (fn json-leave [ctx]
             (let [response (get-in ctx [:request :good-dog.core/response])]
               (-> (gdp/-json response)
                   (.then #(assoc ctx :response %)))))})

(def form-data {:leave
                (fn form-data-leave [ctx]
                  (let [response (get-in ctx [:request :good-dog.core/response])]
                    (-> (gdp/-form-data response)
                        (.then #(assoc ctx :response %)))))})

(def array-buffer {:leave
                   (fn array-buffer-leave [ctx]
                     (let [response (get-in ctx [:request :good-dog.core/response])]
                       (-> (gdp/-array-buffer response)
                           (.then #(assoc ctx :response %)))))})

(def ->cljs {:leave (fn to-cljs [ctx]
                      (update ctx :response js->clj :keywordize-keys true))})
