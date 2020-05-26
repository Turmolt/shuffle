(ns shuffle.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [clj-http.client :as client]
            [clojure.data.json :as json]))


(def hook-url "HOOK")

(defn post-to-slack [url msg]
  (client/post url {:body (json/write-str msg)
                    :content-type :json}))

(post-to-slack hook-url {:text "hello world @channel"})

(defroutes app-routes
  (GET "/" [] "Hello World")
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))

