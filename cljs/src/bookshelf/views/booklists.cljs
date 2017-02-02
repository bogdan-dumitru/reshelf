(ns bookshelf.views.booklists
  (:require [reagent.core :as reagent]
            [bookshelf.views.book :as book]
            [re-frame.core :refer [subscribe dispatch]]))

(defn booklist-item
  []
  (fn [{:keys [item-id]}]
    (let [book-id @(subscribe [:booklist-item-book-id item-id])
          book-loaded? @(subscribe [:book-loaded? book-id])]
      (if book-loaded? [book/list-view {:book-id book-id}]
                       [book/dummy-list-view]))))
 
(defn booklist
  []
  (fn [{:keys [booklist-id book-in-queue-id]}]
    (let [booklist-name @(subscribe [:booklist-name booklist-id])
          booklist-item-ids @(subscribe [:booklist-item-ids booklist-id])
          id (str "list-" booklist-id)
          add-book-to-list #(dispatch [:add-book-to-list 
                                       book-in-queue-id 
                                       booklist-id])]
    [:div.booklist.animated.fadeInRight
     {:id id
      :on-click (when (some? book-in-queue-id) add-book-to-list)}
     [:div.booklist-name booklist-name]
     [:div.booklist-items-body
       (for [item-id booklist-item-ids]
         ^{:key item-id} [booklist-item {:item-id item-id}])]])))

(defn new-booklist
  [{:keys [book-id]}]
  [:div.booklist.add-new-booklist.animated.fadeIn
   {:on-click #(dispatch [:add-book-to-list book-id])}])

(defn collection 
  []
  (fn []
    (let [booklist-ids @(subscribe [:sorted-booklist-ids])
          book-in-queue-id @(subscribe [:book-in-queue-id])]
    [:div#booklists-container
     {:class (when (some? book-in-queue-id) "with-book-in-queue")}
     (for [booklist-id booklist-ids] 
       ^{:key booklist-id} [booklist {:booklist-id booklist-id
                                      :book-in-queue-id book-in-queue-id}])
     (when (some? book-in-queue-id) [new-booklist {:book-id book-in-queue-id}])])))

(defn collection-empty
  []
  (fn []
    (let [booklists @(subscribe [:booklists])]
    [:div#no-booklists.animated {:class (if (seq booklists) "bounceOut" "bounceIn")}
     [:p "You don't have any booklists yet." 
      [:br] 
      "Start by searching for a book"]])))

