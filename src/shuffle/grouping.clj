(ns shuffle.grouping
  (:require [clojure.string :as string]))

(def people (atom []))
(def ngroups (atom 4))

(defrecord Person [name role office])

(defn clear! []
  (reset! people []))

(defn add-person! [person]
  (swap! people conj person))

(defn set-ngroups! [n] (reset! ngroups n))

(defn display-person
  [{:keys [name role office]}]
  (string/join ", " [name role office]))

(defn create-person [[name role dev]]
  (Person. name role dev))

(defn split 
  "split by a key then shuffle
   result is sorted by size"
  [coll key]
  (->> (sort-by key coll)
       (partition-by key)
       (sort-by count)
       (map shuffle)))

(defn conj-in [coll idx itm]
  (assoc coll idx (conj (nth coll idx) itm)))

(defn split-into-groups
  [coll key]
  (let [sorted (split coll key)]
    (loop [pool    (rest sorted)
           hand    (first sorted)
           result  (into [] (take @ngroups (repeat [])))
           idx     0]
      
      (if (not-empty hand)
        (let [next (first hand)]
          (recur pool
                 (shuffle (rest hand))
                 (conj-in result idx next)
                 (mod (inc idx) @ngroups)))
        
        (if (not-empty pool)
          (recur (rest pool)
                 (first pool)
                 result
                 idx)
          
          result)))))

(defn deal [key]
  (->> (split-into-groups @people key)
       (map-indexed
        (fn [idx itm]
          (->> (map display-person itm)
               (string/join " & ")
               (str "Group " (inc idx) ": "))))
       (string/join "\n")))


;==========================================================================

(def names   ["sam" "scott" "mike" "alec" "sara" "gonz"])
(def roles   ["developer" "producer" "managment" "business development"])
(def offices ["pdx" "sf"])

(defn random-person []
  (Person. (rand-nth names) (rand-nth roles) (rand-nth offices)))

(comment (repeatedly 20 #(add-person! (random-person))))