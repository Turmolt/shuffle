(ns shuffle.grouping
  (:require [clojure.string :as string]))

(def items (atom []))
(def ngroups (atom 4))
(def groups (atom nil))
(def ids (atom []))

(defrecord Item [name key])

(defn clear! []
  (reset! items []))

(defn clear-ids! []
  (reset! ids []))

(defn add-item! [item]
  (swap! items conj item))

(defn add-id! [id]
  (swap! ids conj id))

(defn set-ngroups! [n] (reset! ngroups n))

(defn pad [text min-length]
  (->> (repeat (- min-length (count text)) " ")
       (string/join)
       (vector text)
       (reduce str)))

(defn results [] @groups)

(defn display-item
  [{:keys [name key] :as Keybert}]
  (prn Keybert)
  (str (pad name 40) key))

(defn create-item [[name key]]
  (Item. name key))

(defn split 
  "split by the key then shuffle
   result is sorted by size"
  [coll]
  (->> (sort-by :key coll)
       (partition-by :key)
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
                 (shuffle (rest hand))
                 (conj-in result idx next)
                 (mod (inc idx) @ngroups)))
        
        (if (not-empty pool)
          (recur (rest pool)
                 (first pool)
                 result
                 idx)
          
          result)))))

(defn deal []
  (->> (split-into-groups @items)
       (map-indexed
        (fn [idx itm]
          (->> (map display-item itm)
               (string/join "\n")
               (#(str "*Group " (inc idx) ":* " (get @ids idx "") "\n```" (if (not-empty %) % "-") "```\n")))))
       (string/join "\n")
       (reset! groups)))

;==========================================================================

(def names   ["sam" "scott" "mike" "alec" "sara" "gonz"])
(def roles   ["developer" "producer" "managment" "business development"])
(def offices ["pdx" "sf"])

(defn random-person []
  (Item. (rand-nth names) (rand-nth offices)))

(comment (repeatedly 20 #(add-item! (random-person))))