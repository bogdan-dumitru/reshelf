(ns bookshelf.views
  (:require [reagent.core  :as reagent]
            [re-frame.core :refer [subscribe dispatch]]))

(defn search-box
  []
  (let [placeholder "Search for a book by title, author or ISBN"]
    [:div#search-box-container.animated.fadeInDown
      [:span#search-box-icon]
      [:input#search-box-input.input-underline {:placeholder placeholder}]]))

(defn booklist-view
  [booklist]
  (let [id (str "list-" booklist)]

    [:div.booklist {:id id}]))


(defn booklists-view
  []
  (let [booklists [1, 2, 3, 4, 5, 6]]

    [:div#booklists-container.animated.slideInRight
      (for [booklist booklists] 
        ^{:key booklist} [booklist-view booklist])]))

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

(defn loading [] [:div#user-loading.animated.fadeIn [:div.cp-spinner.cp-hue]])

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
