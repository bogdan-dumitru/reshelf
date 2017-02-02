(ns bookshelf.views.book
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [subscribe dispatch]]))

(defn- dummy-view [wrapper-class]
  [:div {:class wrapper-class}
   [:div.dummy-search-result-item.animated-background 
    [:div.background-masker.header-top]
    [:div.background-masker.header-left]
    [:div.background-masker.header-right]
    [:div.background-masker.header-bottom]
    [:div.background-masker.subheader-left]
    [:div.background-masker.subheader-right]
    [:div.background-masker.subheader-bottom]]])

(defn dummy-list-view [] [dummy-view "booklist-book-item"])
(defn dummy-search-view [] [dummy-view "search-result-item"])

(defn list-view []
  (fn [{:keys [book-id]}]
    (let [book-title @(subscribe [:book-title book-id])
          book-authors @(subscribe [:book-authors book-id])
          book-published-at @(subscribe [:book-published-at book-id])
          book-image-url @(subscribe [:book-image-url book-id])]
    [:div.booklist-book-item 
      [:div.book-picture-wrapper
        [:img.book-picture {:src book-image-url}]]
      [:div.book-body
        [:p.book-title book-title]
        [:p.book-authors book-authors ]]])))


(defn queue-view []
  (let [bounce-out? (reagent/atom false)
        actual-remove #(do (dispatch [:clear-book-from-queue])
                           (reset! bounce-out? false))
        hide-and-remove #(do (reset! bounce-out? true)
                             (js/setTimeout actual-remove 500))]
  (fn [{:keys [book-id]}]
    (let [book-title @(subscribe [:book-title book-id])
          book-authors @(subscribe [:book-authors book-id])
          book-published-at @(subscribe [:book-published-at book-id])
          book-image-url @(subscribe [:book-image-url book-id])]
    [:div.search-result-item.in-queue.animated
      {:on-click hide-and-remove 
       :class (if @bounce-out? "bounceOut" "bounceIn")}
      [:div.book-picture-wrapper
        [:img.book-picture {:src book-image-url}]]
      [:div.book-body
        [:p.book-title book-title]
        [:p.book-authors book-authors ]
        (when (some? book-published-at)
          [:p.book-published-at" (" book-published-at ")"])
        [:a "Cancel"]]]))))

(defn search-view []
  (fn [{:keys [book-id on-click]}]
    (let [book-title @(subscribe [:book-title book-id])
          book-authors @(subscribe [:book-authors book-id])
          book-published-at @(subscribe [:book-published-at book-id])
          book-image-url @(subscribe [:book-image-url book-id])]
    [:div.search-result-item 
      {:on-click on-click}
      [:div.book-picture-wrapper
        [:img.book-picture {:src book-image-url}]]
      [:div.book-body
        [:p.book-title book-title]
        [:p.book-authors book-authors ]
        (when (some? book-published-at)
          [:p.book-published-at" (" book-published-at ")"])
        [:a "Add to list"]]])))



