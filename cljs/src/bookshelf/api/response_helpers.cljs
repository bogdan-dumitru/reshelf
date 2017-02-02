(ns bookshelf.api.response-helpers
  (:require [re-frame.core :refer [->interceptor]]))

(defn- volume->book 
  [volume] 
  {:book-id (:id volume)
   :loaded? true
   :title (->> volume (:volumeInfo) (:title))
   :authors (->> volume (:volumeInfo) 
                 (:authors) 
                 (clojure.string/join ", "))
   :picture-url (->> volume (:volumeInfo) 
                     (:imageLinks) 
                     (:smallThumbnail))
   :info-url (->> volume (:volumeInfo) (:infoLink))
   :tags (->> volume (:volumeInfo)
              (:categories))
   :published-at (->> volume (:volumeInfo) 
                      (:publishedDate))})

(def parse-google-books-search
  (let [filter-nils #(into {} (filter (comp some? val) %))]
  (->interceptor 
    :id :google-books-search
    :before 
    (fn [context]
      (let [results (first (get-in context [:coeffects :event]))
            book-map (->> results (:items)
                          (take 9)
                          (map (comp filter-nils volume->book))
                          (map #(->[(:book-id %) %])))]
      (assoc-in context [:coeffects :event] [book-map]))))))

(def parse-google-book
  (let [filter-nils #(into {} (filter (comp some? val) %))]
  (->interceptor
    :id :parse-google-book
    :before
    (fn [context]
      (let [result (first (get-in context [:coeffects :event]))
            book ((comp filter-nils volume->book) result)]
      (assoc-in context [:coeffects :event] [book]))))))

(defn- resp->list-item [resp]
  {:id (:id resp)
   :book-id (:book_id resp)
   :list-id (:list_id resp)})

(defn- resp-list->list-and-items
  [resp-list]
  (let [list-items-map ( fn [list-items] 
                         (map #(->[(:id %) (resp->list-item %)]) list-items))
        list-from-resp #(-> {:id (:id %)
                             :name (:name %)
                             :color (:color %)})
        parsed-list-items (list-items-map (:list_items resp-list))
        parsed-list (assoc (list-from-resp resp-list)
                          :list-item-ids
                           (keys parsed-list-items))]
  [parsed-list parsed-list-items]))

(defn- resp-lists->lists-and-items
  [resp-lists]
  (let [reduce-fn (fn [[lists list-items] resp-list]
                      (let [ [parsed-list parsed-list-items] 
                             (resp-list->list-and-items resp-list) ]
                      [(merge lists [(:id parsed-list) parsed-list])
                       (merge list-items parsed-list-items)]))]
  (reduce reduce-fn [{} {}] resp-lists)))

(defn- context->resp
  [context]
  (first (get-in context [:coeffects :event])))

(defn- resp->context
  [context parsed-response]
  (assoc-in context [:coeffects :event] parsed-response))

(def parse-list
  (->interceptor
    :id :parse-list
    :before
    (fn [context]
      (let [resp (context->resp context)
            [parsed-list parsed-list-items] (resp-list->list-and-items resp)]
      (resp->context context [parsed-list parsed-list-items])))))
                         

(def parse-lists
  (->interceptor
    :id :parse-lists
    :before
    (fn [context]
      (let [resp (context->resp context)
            [parsed-lists parsed-list-items] (resp-lists->lists-and-items resp)]
      (resp->context context [parsed-lists parsed-list-items])))))

(def parse-list-item
  (->interceptor
    :id :parse-list-item
    :before
    (fn [context]
      (let [parsed-list-item (-> context (context->resp) (resp->list-item))]
      (resp->context context [parsed-list-item])))))
