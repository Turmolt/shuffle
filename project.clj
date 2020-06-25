(defproject shuffle "0.1.0-SNAPSHOT"
  :description "a slack bot that creates 'random' groups of people"
  :url "https://github.com/Turmolt/shuffle"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [compojure "1.6.1"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-jetty-adapter "1.6.3"]
                 [org.clojure/data.json "1.0.0"]
                 [environ "1.2.0"]
                 [clj-http "3.10.1"]]
  :main ^:skip-aot shuffle.handler
  :uberjar-name "shuffle-standalone.jar"
  :plugins [[lein-ring "0.12.5"]
            [lein-environ "1.2.0"]]
  :ring {:handler shuffle.handler/app}
  :profiles
  {:dev [:project/dev :profiles/dev]
   :project/dev  {:dependencies [[javax.servlet/servlet-api "2.5"]
                                 [ring/ring-mock "0.3.2"]]}
   :profiles/dev {}
   :uberjar {:aot :all}})