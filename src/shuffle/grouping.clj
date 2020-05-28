(ns shuffle.grouping)

(def people (atom []))
(def ngroups (atom 4))

(def names   ["sam" "scott" "mike" "alec" "sara" "gonz"])
(def roles   ["developer" "producer" "managment" "business development"])
(def offices ["pdx" "sf"])

(defrecord Person [name role office])

(defn random-person []
  (Person. (rand-nth names) (rand-nth roles) (rand-nth offices)))

(defn add-person! [person]
  (swap! people conj person))

(defn split 
  "split by office then shuffle
   result is sorted by size"
  [coll]
  (->> (sort-by :office coll)
       (partition-by :office)
       (sort-by count)
       (map shuffle)))

(defn conj-in [coll idx itm]
  (assoc coll idx (conj (nth coll idx) itm)))

(defn split-into-groups
  [coll]
  (let [sorted (split coll)]
    (loop [pool    (rest sorted)
           hand    (first sorted)
           result  (into [] (take @ngroups (repeat [])))
           idx     0]
      
      (if (not-empty hand)
        (let [next (first hand)]
          (recur pool
                 (rest hand)
                 (conj-in result idx next)
                 (mod (inc idx) @ngroups)))
        
        (if (not-empty pool)
          (recur (rest pool)
                 (first pool)
                 result
                 idx)
          
          result)))))


;(repeatedly 20 #(add-person! (random-person)))
;(add-person! {:name "scott" :office "maine" :role "developer"})

