(ns good-dog.interceptors
  (:require [good-dog.protocols :as gdp]))

(def text {:leave
           (fn text-leave [{:keys [response] :as ctx}]
             (-> (gdp/-text response)
             (.then #(assoc ctx :response %))))})

(def json {:leave
           (fn json-leave [{:keys [response] :as ctx}]
             (-> (gdp/-json response)
                 (.then #(assoc ctx :response %))))})

(def form-data {:leave
                (fn form-data-leave [{:keys [response] :as ctx}]
                  (-> (gdp/-form-data response)
                      (.then #(assoc ctx :response %))))})

(def array-buffer {:leave
                   (fn array-buffer-leave [{:keys [response] :as ctx}]
                     (-> (gdp/-array-buffer response)
                         (.then #(assoc ctx :response %))))})

(def ->cljs {:leave (fn to-cljs [ctx]
                      (update ctx :response js->clj :keywordize-keys true))})
