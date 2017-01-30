(ns bookshelf.views
  (:require [reagent.core  :as reagent]
            [re-frame.core :refer [subscribe dispatch]]))

(defn search-box
  []
  (let [placeholder "Search for a book by title, author or ISBN"]
    [:div#search-box-container.animated.fadeInDown
      [:input#search-box-input {:placeholder placeholder}]]))

(defn booklist-view
  [booklist]
  (let [id (str "list-" booklist)]

    [:div.booklist {:id id}]))


(defn booklists-view
  []
  (let [booklists [1, 2, 3]]

    [:div#booklists-container.animated.slideInRight
      (for [booklist booklists] 
        ^{:key booklist} [booklist-view booklist])]))

(defn app
  []
  [:div#app-container 
    [search-box]
    [booklists-view]])

