(ns good-dog.protocols)

(defprotocol Body
  (-array-buffer [this] "Takes a Response stream and reads it to completion. It returns a promise that resolves with an ArrayBuffer.")
  (-blob [this] "Takes a Response stream and reads it to completion. It returns a promise that resolves with a Blob.")
  (-form-data [this] "Takes a Response stream and reads it to completion. It returns a promise that resolves with a FormData object.")
  (-json [this] "Takes a Response stream and reads it to completion. It returns a promise that resolves with the result of parsing the body text as JSON.")
  (-text [this] "Takes a Response stream and reads it to completion. It returns a promise that resolves with a USVString (text)."))


(defprotocol Response
  (-clone [this] "Creates a clone of a Response object.")
  (-error [this] "Returns a new Response object associated with a network error.")
  (-redirect [this url status] "Creates a new response with a different URL."))
