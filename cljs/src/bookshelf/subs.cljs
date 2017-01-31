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

(reg-sub :book
  (fn [db [_ id]]
    (val (find (:books db) id))))

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

(reg-sub :search-loading
  (fn [db _] (:loading (:search db))))

(defn schedule-search [query]
  (js/setTimeout 
    #(when (not (clojure.string/blank? query)) 
           (dispatch [:start-search query])) 
    400))

(reg-sub-raw :search-results
  (fn [db [_ query]]
    (when (not (:loading (:search @db))) (dispatch [:set-search-loading]))
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


