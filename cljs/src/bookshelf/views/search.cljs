(ns bookshelf.views.search
  (:require [reagent.core :as reagent]
            [bookshelf.views.book :as book]
            [re-frame.core :refer [subscribe dispatch]]))


(defn dummy-items
  []
  [:div#search-results
    (for [row (range 3)] ^{:key row}
    [:div.search-results-row 
      (for [col (range 3)] ^{:key col} [book/dummy-search-view])])])

(defn preview-item
  [{:keys [book-id collapse-search]}]
  [book/search-view {:book-id book-id
                     :on-click #(do (collapse-search)
                                    (dispatch [:start-add-new-book book-id]))}])

(defn expanded-item
  [book-id]
  (fn [] 
    [:div.expanded-item "Expanded I am"]))

(defn preview-items
  []
  (fn [{:keys [results collapse-search]}]
    [:div#search-results 
      (for [row (partition-all 3 results)]
        ^{:key (first row)} 
        [:div.search-results-row
          (for [book-id row]  
            ^{:key book-id} [preview-item {:book-id book-id
                                           :collapse-search collapse-search}])])]))
      

(defn results
  [{:keys [query collapse-search]}]
  (fn [{:keys [query]}]
    (let [results @(subscribe [:search-results query])
          loading? @(subscribe [:search-loading?])]
    (if loading? [dummy-items]
                 [preview-items {:results results
                                :collapse-search collapse-search}]))))

(defn box
  []
  (let [placeholder "Search for a book by title, author or ISBN"
        search-collapsed (reagent/atom false)
        query (reagent/atom "")]
    (fn [] 
      (let [show-results? (and (not (clojure.string/blank? @query))
                               (not @search-collapsed))]
        [:div#search-box-container.animated.flipInX 
          [:div#search-box-inner
            {:class (str (when show-results? "expanded")) }
            [:span#search-box-icon]
            [:input#search-box-input.input-underline 
             { :placeholder placeholder
               :on-focus #(reset! search-collapsed false)
               :on-change #(reset! query (-> % .-target .-value))
               :value @query }]

            [results {:query @query
                      :collapse-search #(reset! search-collapsed true)}]]])))) 

