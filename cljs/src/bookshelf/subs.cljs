(ns bookshelf.subs
  (:require [re-frame.core :refer [reg-sub subscribe reg-sub-raw dispatch]]
            [reagent.ratom :refer [make-reaction]]))

(reg-sub-raw :user
  (fn [db [_ _]]
    (let [no-user? (nil? (:user @db))
          has-token? (some? (:user-token (:config @db)))]
      (when (and no-user? has-token?) (dispatch [:login-user]))
      (make-reaction
        (fn [] (get-in @db [:user] []))
        :on-dispose #(println "User On Dispose")))))

(reg-sub-raw :book
  (fn [db [_ id]]
    (let [book-not-loaded? (not (some-> @db (:books) (find id) (val) (:loaded)))]
    (when book-not-loaded? (dispatch [:load-book id]))
    (make-reaction
      (fn [] (get-in @db [:books id] []))))))


(reg-sub :book-loaded?
  (fn [[_ id] _] [(subscribe [:book id])])
  (fn [[book] _] (:loaded? book)))

(reg-sub :book-title
  (fn [[_ id] _] [(subscribe [:book id])])
  (fn [[book] _] (:title book)))

(reg-sub :book-authors
  (fn [[_ id] _] 
    [(subscribe [:book id])])
  (fn [[book] _] 
    (let [authors (:authors book)]
      (if (clojure.string/blank? authors)
          "[Author missing]"
          authors))))

(reg-sub :book-published-at
  (fn [[_ id] _] [(subscribe [:book id])])
  (fn [[book] _] (:published-at book)))

(reg-sub :book-image-url
  (fn [[_ id] _] [(subscribe [:book id])])
  (fn [[book] _] 
    (let [image-url (:picture-url book)]
      (if (some? image-url) 
          image-url 
          "https://books.google.ro/googlebooks/images/no_cover_thumb.gif"))))

(reg-sub :search-loading?
  (fn [db _] (:loading? (:search db))))

(defn schedule-search [query]
  (js/setTimeout 
    #(when (not (clojure.string/blank? query)) 
           (dispatch [:start-search query])) 
    400))

(reg-sub-raw :search-results
  (fn [db [_ query]]
    (when (not (:loading? (:search @db))) (dispatch [:set-search-loading]))
    (let [scheduled-search (schedule-search query)]
    (make-reaction
      (fn [] (get-in @db [:search :results] []))
      ; Might need to do some cleanup here as well
      :on-dispose #(js/clearTimeout scheduled-search)))))

(reg-sub :user-name
  (fn [_ _] [(subscribe [:user])])
  (fn [[user] _] (:name user)))

(reg-sub :no-saved-session?
  (fn [db _] 
    (nil? (:user-token (:config db)))))

(reg-sub :book-in-queue-id
  (fn [db _]
    (:book-in-queue-id db)))

(reg-sub-raw :booklists
  (fn [db _]
    (when (not (seq (:lists @db))) (dispatch [:load-lists]))
    (make-reaction
      (fn [] (keys (:lists @db)))
      :on-dispose #(println "Is this running?"))))

(reg-sub :sorted-booklist-ids
  (fn [_ _] [(subscribe [:booklists])])
  (fn [[booklists] _]
    (sort-by identity < booklists)))

(reg-sub :booklist
  (fn [db [_ id]]
    (val (find (:lists db) id))))

(reg-sub :booklist-name
  (fn [[_ id] _] [(subscribe [:booklist id])])
  (fn [[booklist] _] (:name booklist)))

(reg-sub :booklist-item-ids
  (fn [[_ id] _] [(subscribe [:booklist id])])
  (fn [[booklist] _] (:list-item-ids booklist)))

(reg-sub :booklist-item
  (fn [db [_ id]]
    (val (find (:list-items db) id))))

(reg-sub :booklist-item-book-id
  (fn [[_ id] _] [(subscribe [:booklist-item id])])
  (fn [[booklist-item] _] (:book-id booklist-item)))


