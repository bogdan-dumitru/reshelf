(ns bookshelf.db
  (:require [cljs.spec :as s]
            [re-frame.core :as re-frame]))


;; Configuration
(s/def ::user-token (s/or :token string? :no-token nil?))
(s/def ::google-books-token string?)
(s/def ::csrf-token string?)
(s/def ::csrf-param string?)
(s/def ::config (s/keys :req-un [::google-books-token ::csrf-token] :opt-un [::user-token]))

;; Common
(s/def ::id int?)
(s/def ::name string?)
(s/def ::loading? boolean?)
(s/def ::loaded? boolean?)

;; Book
(s/def ::book-id string?)
(s/def ::title string?)
(s/def ::picture-url string?)
(s/def ::authors string?)
(s/def ::info-url string?)
(s/def ::published-at string?)
(s/def ::tags (s/cat :tag (s/* string?)))
(s/def ::book (s/keys :req-un [::book-id ::loaded? ::title]
                      :opt-un [::info-url ::published-at 
                               ::picture-url ::tags ::authors]))

(s/def ::books (s/and
                 (s/map-of ::book-id ::book)
                 #(instance? PersistentTreeMap %)))

;; ListItem
(s/def ::list-id int?)
(s/def ::list-item (s/keys :req-un [::id ::book-id ::list-id]))
(s/def ::list-items (s/and
                      (s/map-of ::id ::list-item)
                      #(instance? PersistentTreeMap %)))

;; List
(s/def ::color string?)
(s/def ::position int?)
(s/def ::list-item-ids (s/cat :list-item-id (s/* int?)))
(s/def ::list (s/keys :req-un [::id ::name ::color ::list-item-ids] :opt-un [::position]))
(s/def ::lists (s/and
                 (s/map-of ::id ::list)
                 #(instance? PersistentTreeMap %)))

;; User
(s/def ::user (s/keys :req-un [::id ::name]))

;; Search
; (s/def ::query string?)
(s/def ::results (s/cat :book-ids (s/* ::book-id)))
(s/def ::search (s/keys :opt-un [::results ::loading?]))

(s/def ::book-in-queue-id string?)
(s/def ::db (s/keys :req-un [::config ::books ::list-items ::lists ::search] 
                    :opt-un [::user ::book-in-queue-id]))

(def default-value
  {:config {}
   :search { :loading? false }
   :books (sorted-map)
   :list-items (sorted-map)
   :lists (sorted-map)})

