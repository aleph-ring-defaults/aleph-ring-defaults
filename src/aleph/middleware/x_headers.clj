(ns aleph.middleware.x-headers
  (:require [aleph.middleware.util :refer [defer]]
            [ring.middleware.x-headers :as x]
            [ring.util.response :as resp]))

(defn- wrap-x-header [handler header-name header-value]
  (fn
    ([request]
     (some-> (handler request) ((defer resp/header) header-name header-value)))
    ([request respond raise]
     (handler request #(respond (some-> % (resp/header header-name header-value))) raise))))

(defn wrap-content-type-options
  "Middleware that adds the X-Content-Type-Options header to the response. This
  currently only accepts one option:

  :nosniff - prevent resources with invalid media types being loaded as
             stylesheets or scripts

  This prevents attacks based around media type confusion. See:
  http://msdn.microsoft.com/en-us/library/ie/gg622941(v=vs.85).aspx"
  [handler content-type-options]
  {:pre [(= content-type-options :nosniff)]}
  (wrap-x-header handler "X-Content-Type-Options" (name content-type-options)))

(def ^:private format-xss-protection @#'x/format-xss-protection)

(defn wrap-xss-protection
  "Middleware that adds the X-XSS-Protection header to the response. This header
  enables a heuristic filter in browsers for detecting cross-site scripting
  attacks. Usually on by default.

  The enable? attribute determines whether the filter should be turned on.
  Accepts one additional option:

  :mode - currently accepts only :block

  See: http://msdn.microsoft.com/en-us/library/dd565647(v=vs.85).aspx"
  ([handler enable?]
   (wrap-xss-protection handler enable? nil))
  ([handler enable? options]
   {:pre [(or (nil? options) (= options {:mode :block}))]}
   (wrap-x-header handler "X-XSS-Protection" (format-xss-protection enable? options))))

(def ^:private format-frame-options @#'x/format-frame-options)

(def ^:private allow-from? @#'x/allow-from?)

(defn wrap-frame-options
  "Middleware that adds the X-Frame-Options header to the response. This governs
  whether your site can be rendered in a <frame>, <iframe> or <object>, and is
  typically used to prevent clickjacking attacks.

  The following frame options are allowed:

  :deny             - prevent any framing of the content
  :sameorigin       - allow only the current site to frame the content
  {:allow-from uri} - allow only the specified URI to frame the page

  The :deny and :sameorigin options are keywords, while the :allow-from option
  is a map consisting of one key/value pair.

  Note that browser support for :allow-from is incomplete. See:
  https://developer.mozilla.org/en-US/docs/Web/HTTP/X-Frame-Options"
  [handler frame-options]
  {:pre [(or (= frame-options :deny)
             (= frame-options :sameorigin)
             (allow-from? frame-options))]}
  (wrap-x-header handler "X-Frame-Options" (format-frame-options frame-options)))
