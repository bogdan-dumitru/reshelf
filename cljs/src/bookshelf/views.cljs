(ns bookshelf.views
  (:require [reagent.core  :as reagent]
            [re-frame.core :refer [subscribe dispatch]]))

(defn dummy-search-result-item
  []
  [:div.search-result-item 
   [:div.dummy-search-result-item.animated-background 
    [:div.background-masker.header-top]
    [:div.background-masker.header-left]
    [:div.background-masker.header-right]
    [:div.background-masker.header-bottom]
    [:div.background-masker.subheader-left]
    [:div.background-masker.subheader-right]
    [:div.background-masker.subheader-bottom]]])

(defn dummy-search-results
  []
  [:div#search-results
    (for [row (range 3)] ^{:key row}
    [:div.search-results-row 
      (for [col (range 3)] ^{:key col} [dummy-search-result-item])])])

(defn search-result
  [book-id]
  (fn []
    (let [book-title @(subscribe [:book-title book-id])
          book-authors @(subscribe [:book-authors book-id])
          book-published-at @(subscribe [:book-published-at book-id])
          book-image-url @(subscribe [:book-image-url book-id])]
    [:div.search-result-item
      [:div.book-picture-wrapper
        [:img.book-picture {:src book-image-url}]]
      [:div.book-body
        [:p.book-title book-title]
        [:p.book-authors book-authors ]
        [:p.book-published-at" (" book-published-at ")"]]])))


(defn actual-search-results
  [results]
  [:div#search-results 
    (for [row (partition-all 3 results)]
      ^{:key (first row)} 
      [:div.search-results-row
        (for [book-id row] 
          ^{:key book-id} [search-result book-id])])])

(defn search-results
  [{:keys [query]}]
  (fn [{:keys [query]}]
    (let [results @(subscribe [:search-results query])
          loading @(subscribe [:search-loading])]
    (if loading (dummy-search-results)
                (actual-search-results results)))))

(defn search-box
  []
  (let [placeholder "Search for a book by title, author or ISBN"
        query (reagent/atom "")]
    (fn [] 
      (let [show-results? (not (clojure.string/blank? @query))]
        [:div#search-box-container.animated.flipInX 
          [:div#search-box-inner
            {:class (str (when show-results? "expanded")) }
            [:span#search-box-icon]
            [:input#search-box-input.input-underline 
             { :placeholder placeholder
               :on-change #(reset! query (-> % .-target .-value))
               :value @query }]

            [search-results {:query @query}]]]))))

(defn booklist-view
  [booklist]
  (let [id (str "list-" booklist)]

    [:div.booklist {:id id}]))


(defn booklists-view
  []
  (let [booklists []]
    (cond 
      (seq booklists) 
        [:div#booklists-container.animated.slideInRight
          (for [booklist booklists] 
            ^{:key booklist} [booklist-view booklist])]
      :else
        [:div#no-booklists.animated.bounceIn
         [:p "You don't have any booklists yet." 
             [:br] 
             "Start by searching for a book"]])))

(defn footer
  []
  [:p#footer {:class "animated fadeIn"}
   [:span#line-1 (str "Happy reading, " @(subscribe [:user-name]) "!")]
   [:span#line-2 
    "Made with" 
    [:span#heart "♥"]
    "by"
    [:a {:href "http://ironlung.co/"} "Bogdan Dumitru."]]])

(defn booted-app
  []
  [:div#booted-app-wrapper 
    [search-box]
    [booklists-view]
    [footer]
   ])

(defn loading [] [:div#user-loading [:div.cp-spinner.cp-hue]])

(defn signup
  []
  (let [name (reagent/atom "")
        warning (reagent/atom nil)
        continue #(dispatch [:create-session @name])
        set-warning #(reset! warning true)
        clear-warning #(reset! warning false)
        try-continue #(if (clojure.string/blank? @name) (set-warning) (continue))]
    (fn []
      [:div#signup-container.animated.zoomIn
        [:h2 "Welcome to Reshelf"]
        [:div#signup-input-container.u-relative
          [:span.warning {:class (str "animated " 
                                      (when (nil? @warning) "u-hidden ") 
                                      (if @warning "bounceIn " "bounceOut")) } 
                          "*" ]
          [:input#signup-user-name.input-underline 
            { :placeholder "What's your name?"
              :value @name
              :on-change #(reset! name (-> % .-target .-value))
              :on-key-down #(if (= 13 (.-which %)) 
                                (try-continue) 
                                (when @warning (clear-warning))) }]]
        [:div#signup-actions 
          [:span (:name @(subscribe [:user]))]
          [:button {:on-click try-continue} "Continue"]]])))


(defn app
  []
  [:div#app-container
   (cond
      (seq @(subscribe [:user])) [booted-app]
      @(subscribe [:no-saved-session?]) [signup]
      :else [loading])])
