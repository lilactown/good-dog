(ns good-dog.interceptors
  (:require [good-dog.protocols :as gdp]))

(defn handle-error [ctx]
  (fn [e]
    (assoc ctx :error e)))

(def text {:leave
           (fn text-leave [{:keys [response] :as ctx}]
             (-> (gdp/-text response)
                 (.then #(assoc ctx :response %))
                 (.catch (handle-error ctx))))})

(def json {:leave
           (fn json-leave [{:keys [response] :as ctx}]
             (-> (gdp/-json response)
                 (.then #(assoc ctx :response %))
                 (.catch (handle-error ctx))))})

(def form-data {:leave
                (fn form-data-leave [{:keys [response] :as ctx}]
                  (-> (gdp/-form-data response)
                      (.then #(assoc ctx :response %))
                      (.catch (handle-error ctx))))})

(def array-buffer {:leave
                   (fn array-buffer-leave [{:keys [response] :as ctx}]
                     (-> (gdp/-array-buffer response)
                         (.then #(assoc ctx :response %))
                         (.catch (handle-error ctx))))})

(def ->cljs {:leave (fn to-cljs [ctx]
                      (update ctx :response js->clj :keywordize-keys true))})
