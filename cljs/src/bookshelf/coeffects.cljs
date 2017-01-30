(ns bookshelf.coeffects
  (:require [re-frame.core :as re-frame]))

(def ls-key "bookshelf-user-token")
(defn user-token->local-store
  "Puts the user token into localStorage"
  [user-token]
  (.setItem js/localStorage ls-key (str user-token)))

(re-frame/reg-cofx :local-store-token
  (fn [cofx _]
    "Read the user token from local storage"
    (assoc cofx :local-store-token (.getItem js/localStorage ls-key))))


(defn build-metas-from-head
  "Parse meta tags and extract their content and name into a map"
  []
  (let [as-seq #(for [i (range (.-length %))] (.item % i))
        metas (as-seq (.getElementsByTagName js/document "meta"))]
    (into {} (map #(-> [(keyword (.-name %)) (.-content %)]) metas))))

(re-frame/reg-cofx :metas
  (fn [cofx _]
    "Parse meta tags in head to extract relevant keys"
    (let [metas (build-metas-from-head)]
      (assoc cofx :metas metas))))


