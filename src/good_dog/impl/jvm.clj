(ns good-dog.impl.jvm
  (:require [good-dog.protocols :as gdp])
  (:import [java.net.http HttpClient HttpRequest HttpHeaders HttpResponse
            HttpResponse$BodyHandlers HttpClient$Version]
           [java.net URI]))

(defn create-client* [{:keys [follow-redirects? proxy authenticator]}]
  (cond-> (. HttpClient newBuilder)
    ;; (some? version) (.version (case version
    ;;                             1 ))
    ;; (some? follow-redirects?) (.followRedirects )
    ;; (some? proxy) (.proxy )
    ;; (some? authenticator) (.authenticator )
    :finally (.build)))

(def create-client create-client*)

(comment (create-client {}))

(defn add-headers [request headers]
  (loop [req request
         headers headers]
    (if (some? headers)
      (recur (if-let [hd (first headers)] ;; map entry
               (.header req (key hd) (val hd))
               req)
             (next headers))
      ;; done
      req)))

(defn add-method [req method opts]
  (case method
    :post (.POST req)
    :get req
    req))

(defn ->request [{:keys [url method body timeout headers]}]
  (-> (. HttpRequest newBuilder)
      ;; must have uri
      (.uri (URI. url))
      (cond->
          ;; (some? timeout) ()
          (some? headers) (add-headers headers)
          (some? method) (add-method method nil)
          ;; (some? body) ()
          )
      (.build))
  )

(def c (create-client {}))

(def r (->request {:url "https://lilac.town"}))


(def rs (-> c
            (.send r (. HttpResponse$BodyHandlers ofString)
                   )))

#_(.body rs)

;; (extend-type HttpRequest
;;   gdp/Body
;;   )

(defn fetch
  "Fetches data over HTTP. Returns a Request that can be sent and read."
  ([url]
   )
  ([url opts]
   ))
