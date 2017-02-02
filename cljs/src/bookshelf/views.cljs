(ns bookshelf.views
  (:require [reagent.core  :as reagent]
            [bookshelf.views.search :as search]
            [bookshelf.views.booklists :as booklists]
            [bookshelf.views.book :as book]
            [re-frame.core :refer [subscribe dispatch]]))

(defn footer
  []
  [:p#footer {:class "animated fadeIn"}
   [:span#line-1 (str "Happy reading, " @(subscribe [:user-name]) "!")]
   [:span#line-2 
    "Made with" 
    [:span#heart "♥"]
    "and"
    [:a {:href "https://github.com/Day8/re-frame"} "re-frame" ]
    " by"
    [:a {:href "http://ironlung.co/"} "Bogdan Dumitru."]]])

(defn booted-app
  []
  (fn []
    (let [book-in-queue-id @(subscribe [:book-in-queue-id])]
    [:div#booted-app-wrapper 
      [search/box]
      [booklists/collection-empty]
      [booklists/collection]
      (when (some? book-in-queue-id) [book/queue-view {:book-id book-in-queue-id}])
      [footer]])))

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
      [:div#signup-container.animated.bounceIn
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
