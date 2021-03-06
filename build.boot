(def +project+ 'aleph-ring-defaults)
(def +version+ "0.1")

(set-env!
  :source-paths #{"src"}
  :dependencies '[[adzerk/bootlaces "0.1.13" :scope "test"]
                  [onetom/boot-lein-generate "0.1.3" :scope "test"]
                  [org.clojure/clojure "1.9.0-alpha20" :scope "test"]
                  [aleph-middleware "0.2.0"]
                  [ring/ring-defaults "0.3.1"]])

(require '[adzerk.bootlaces :refer [bootlaces! build-jar push-snapshot push-release]]
         '[boot.lein :as lein])

(bootlaces! +version+)
(lein/generate)

(task-options!
  pom {:project     +project+
       :version     +version+
       :description "Like ring-defaults, but with support for Aleph's deferred responses."
       :url         "https://github.com/aleph-ring-defaults/aleph-ring-defaults"
       :scm         {:url "https://github.com/aleph-ring-defaults/aleph-ring-defaults"}
       :license     {"MIT" "https://opensource.org/licenses/MIT"}})
