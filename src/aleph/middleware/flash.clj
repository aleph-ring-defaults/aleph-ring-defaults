(ns aleph.middleware.flash
  (:require [aleph.middleware.util :refer [defer]]
            [ring.middleware.flash :refer [flash-request flash-response]]))

(defn wrap-flash
  "If a :flash key is set on the response by the handler, a :flash key with
  the same value will be set on the next request that shares the same session.
  This is useful for small messages that persist across redirects."
  [handler]
  (fn [request]
    (if-let [resp (handler (flash-request request))]
      ((defer flash-response) resp (flash-request request)))))
