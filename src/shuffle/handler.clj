(ns shuffle.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [clj-http.client :as client]
            [ring.adapter.jetty :refer [run-jetty]]
            [vault.core :refer [vault]]
            [shuffle.grouping :as g]
            [clojure.string :as string]))

(def hook-url (vault :hook-url))

(def token (vault :token))

(def auth-token (vault :secret))

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

(defn add-person [text]
  (let [parsed (string/split text #" ")]
    (if (not (= 4 (count parsed)))
      (response 200 "Please enter the first name, last name, role and office of the person you wish to add.")
      (->> [(str (first parsed) " " (second parsed)) (nth parsed 2) (last parsed)]
           (g/create-person)
           (g/add-person!)
           (map g/display-person)
           (string/join "\n")
           (#(str "```" (g/pad "NAME" 40) (g/pad "ROLE" 40) "OFFICE\n" % " ```"))
           (response 200)))))

(format "%s" "s")

(defn clear[]
  (g/clear!)
  (response 200 "Cleared the list of people."))

(defn deal [key]
  (if (some #{key} [:role :office])
    (->> (g/deal key)
         (str)
         (response 200))
    (response 400 "Enter a valid key. Keys: office, role")))

(deal :office)

(defn set-groups [n]
  (g/set-ngroups! n)
  (response 200 (str "Set number of groups to " n ".")))

(defn post-results [channel]
  (let [results (g/results)]
    (if (nil? results)
      (response 400 "Add people and call deal to produce results, then you can post them.")
      (do (post-to-slack channel (str results))
          (response 200 (str "Results posted to " channel "."))))))

(defn process-message [{:keys [command text] :as request}]
  (cond 
    (= command "/add")    (add-person text)
    (= command "/clear")  (clear)
    (= command "/groups") (set-groups (Integer/parseInt text))
    (= command "/deal")   (g/deal (keyword (string/lower-case text)))
    (= command "/post")   (post-results text)
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
  (let [port 8080]
    (run-jetty app {:port port :join? false})
    (println (str "Running on port " port))))

(comment (client/post "http://localhost:3000/slack" 
                      {:query-params {:token auth-token 
                                      :command "/add" 
                                      :text "sam gates dev maine"}}))