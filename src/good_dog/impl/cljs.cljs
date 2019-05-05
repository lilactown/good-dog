(ns good-dog.impl.cljs
  (:require [goog.object :as gobj]
            [good-dog.protocols :as gdp]
            [clojure.string :as str]
            [clojure.core.protocols :as ccp]))


(def ^:dynamic *fetch-impl*)

(defn- camel-case
  "Returns camel case version of the string, e.g. \"http-equiv\" becomes \"httpEquiv\"."
  [s]
  (if (or (keyword? s)
          (string? s)
          (symbol? s))
    (let [[first-word & words] (str/split (name s) #"-")]
      (if (empty? words)
        s
        (-> (map str/capitalize words)
            (conj first-word)
            str/join)))
    s))

(defn- kebab-case
  "Converts from camel case (e.g. Foo or FooBar) to kebab case
   (e.g. foo or foo-bar)."
  [s]
  (if (> (count s) 1)
    (str/join "-" (map str/lower-case (re-seq #"\w[a-z0-9\?_\./]*" s)))
    s))

(defn- opts->obj
  ([m] (opts->obj m true))
  ([m camelize?]
   (loop [entries (seq m)
          obj #js {}]
     (if (nil? entries)
       obj
       ;; mutates obj
       (let [entry (first entries)
             k (key entry)
             v (val entry)]
         (case k
           :headers (gobj/set obj "headers" (clj->js v))
           (gobj/set obj (if camelize?
                           (camel-case (name k))
                           (name k))
                     v))
         (recur (next entries)
                obj))))))

(opts->obj {:foo "bar"})

;; Maybe memoize?
(defn- response->map [response]
  (loop [ks (gobj/getKeys response)
         m {}]
    (if-let [hd (first ks)]
      (if (str/includes? hd "$")
        ;; skip protocol keys w/ $ in it
        (recur (rest ks) m)
        (recur (rest ks) (assoc m (keyword (kebab-case hd))
                                (gobj/get response hd))))
      m)))

(comment (response->map #js {:foo "bar"}))

(defn- extend-response [response]
  (specify! response
    IPrintWithWriter
    (-pr-writer [this writer _]
      (-write writer (str "#<Response " (response->map this) ">")))
    ccp/Datafiable
    (datafy [this] (response->map this))
    gdp/Body
    (-array-buffer [this] (.arrayBuffer this))
    (-blob [this] (.blob this))
    (-form-data [this] (.formData this))
    (-json [this] (.json this))
    (-text [this] (.text this))
    gdp/Response
    (-clone [this] (.clone this))
    (-error [this] (.error this))
    (-redirect [this url status] (.redirect this url status))))

(defn fetch
  "Fetches data over HTTP. Returns a promise."
  ([url]
   (let [fetch-impl (or *fetch-impl* js/window.fetch)]
     (-> (fetch-impl url)
         (.then extend-response))))
  ([opts url]
   (let [fetch-impl (or *fetch-impl* (:fetch opts) js/window.fetch)]
     (-> (fetch-impl url (-> opts
                             (dissoc :fetch)
                             (opts->obj)))
         (.then extend-response)))))
