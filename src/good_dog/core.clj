(ns good-dog.core)

(defmacro with-fetch [fetch-impl body]
  `(binding [good-dog.impl.cljs/*fetch-impl* ~fetch-impl]
     ~@body))
