(ns shuffle.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [vault.core :refer [vault]]))

(def hook-url (vault :hook-url))

(def token (vault :token))

(defn slack 
  ([] "https://slack.com/api/")
  ([method] (str "https://slack.com/api/" method)))

(defn post-to-slack [channel message]
  (client/post (slack "chat.postMessage")
               {:query-params {:token token
                               :channel channel
                               :text message}}))

(defn slack-handler [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str (:params req))})

(defroutes app-routes
  (POST "/slack"  [] slack-handler)
  (GET "/slack" [] slack-handler)
  (route/not-found "Not Found!"))

(def app (wrap-defaults app-routes api-defaults))