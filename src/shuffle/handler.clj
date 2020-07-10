(ns shuffle.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [clj-http.client :as client]
            [ring.adapter.jetty :refer [run-jetty]]
            [shuffle.grouping :as g]
            [environ.core :refer [env]]
            [clojure.string :as string])
  (:gen-class))

(def token (env :token))

(def auth-token (env :secret))

(defn slack 
  ([] "https://slack.com/api/")
  ([method] (str "https://slack.com/api/" method)))

(defn post-to-slack [channel message]
  (client/post (slack "chat.postMessage")
               {:query-params {:token token
                               :channel channel
                               :text message}}))

(defn response [status body]
  {:status status
   :headers {"Content-Type" "text/html"}
   :body body})

(defn items-response [items]
  (if (< 0 (count items))
    (->> (map g/display-item items)
         (string/join "\n")
         (#(str "```" (g/pad "NAME" 40) "KEY\n" % " ```"))
         (response 200))
    (response 200 "The list is empty.")))

(defn add-item [item]
  (->> (g/create-item item)
       (g/add-item!)
       (items-response)))

(defn add-items [text]
  (let [tokens (string/split text #",")
        tokens (map string/trim tokens)]
    (if (> 2 (count tokens))
      (response 400 "Please enter an even list of items")
      (->> (partition 2 tokens)
           (filter (comp even? count))
           (map add-item)
           (last)))))

(defn remove-item [text]
  (-> (swap! g/items (partial remove (fn [{:keys [name]}] (= name text))))
      (items-response)))

(defn add-id [text]
  (if (< 0 (count text))
    (->> (g/add-id! text)
         (string/join "\n")
         (#(str "```" % "```") )
         (response 200))
    (response 400 "Please send an id to add.")))

(defn clear[]
  (g/clear!)
  (response 200 "Cleared the list of items."))

(defn clear-ids []
  (g/clear-ids!)
  (response 200 "Cleared the list of ids."))

(defn deal []
  (->> (g/deal)
       (str)
       (response 200)))

(defn set-groups [n]
  (g/set-ngroups! n)
  (response 200 (str "Set number of groups to " n ".")))

(defn post-results [channel]
  (let [results (g/results)]
    (if (nil? results)
      (response 400 "Add items and call deal to produce results, then you can post them.")
      (do (post-to-slack channel (str results))
          (response 200 (str "Results posted to " channel "."))))))

(defn process-message [{:keys [command text]}]
  (cond
    (= command "/add")       (add-items text)
    (= command "/clear")     (clear)
    (= command "/remove")    (remove-item text)
    (= command "/clear-ids") (clear-ids)
    (= command "/groups")    (set-groups (Integer/parseInt text))
    (= command "/deal")      (g/deal)
    (= command "/id")        (add-id text)
    (= command "/post")      (post-results text)
    :else (response 400 "Please enter a valid command.")))

(defn slack-handler [{:keys [params]}]
  (if (= auth-token (:token params))
    (process-message params)
    (response 400 (str "Invalid Request"))))

(defroutes app-routes
  (POST "/slack"  [] slack-handler)
  (GET "/slack" [] slack-handler)
  (route/not-found "Not Found!"))

(def app (wrap-defaults app-routes api-defaults))

(defn -main [& args]
  (let [port (Integer. (or (System/getenv "PORT") "8080"))]
    (run-jetty app {:port port :join? false})
    (println (str "Running on port " port))))


(comment (client/post "http://localhost:3000/slack" 
                      {:query-params {:token auth-token 
                                      :command "/add" 
                                      :text "sam gates dev maine"}}))