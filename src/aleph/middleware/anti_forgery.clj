(ns aleph.middleware.anti-forgery
  (:require [aleph.middleware.util :refer [defer]]
            [ring.middleware.anti-forgery :as anti-forgery]))

(def ^:private default-request-token @#'anti-forgery/default-request-token)
(def ^:private make-error-handler @#'anti-forgery/make-error-handler)
(def ^:private find-or-create-token @#'anti-forgery/find-or-create-token)
(def ^:private valid-request? @#'anti-forgery/valid-request?)
(def ^:private add-session-token @#'anti-forgery/add-session-token)

(defn wrap-anti-forgery
  "Middleware that prevents CSRF attacks. Any POST request to the handler
  returned by this function must contain a valid anti-forgery token, or else an
  access-denied response is returned.

  The anti-forgery token can be placed into a HTML page via the
  *anti-forgery-token* var, which is bound to a random key unique to the
  current session. By default, the token is expected to be in a form field
  named '__anti-forgery-token', or in the 'X-CSRF-Token' or 'X-XSRF-Token'
  headers.

  Accepts the following options:

  :read-token     - a function that takes a request and returns an anti-forgery
                    token, or nil if the token does not exist

  :error-response - the response to return if the anti-forgery token is
                    incorrect or missing

  :error-handler  - a handler function to call if the anti-forgery token is
                    incorrect or missing.

  Only one of :error-response, :error-handler may be specified."
  ([handler]
   (wrap-anti-forgery handler {}))
  ([handler options]
   {:pre [(not (and (:error-response options) (:error-handler options)))]}
   (let [read-token    (:read-token options default-request-token)
         error-handler (make-error-handler options)]
     (fn
       ([request]
        (let [token (find-or-create-token request)]
          (binding [anti-forgery/*anti-forgery-token* token]
            (if (valid-request? request read-token)
              (error-handler request)
              ((defer add-session-token) (handler request) request token)))))
       ([request respond raise]
        (let [token (find-or-create-token request)]
          (binding [anti-forgery/*anti-forgery-token* token]
            (if (valid-request? request read-token)
              (error-handler request respond raise)
              (handler request #(respond (add-session-token % request token)) raise)))))))))
