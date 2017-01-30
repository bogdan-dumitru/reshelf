(ns bookshelf.db
  (:require [cljs.spec :as s]
            [re-frame.core :as re-frame]))


;; Configuration
(s/def ::user-token (s/or :token string? :no-token nil?))
(s/def ::goodreads-token string?)
(s/def ::csrf-token string?)
(s/def ::csrf-param string?)
(s/def ::config (s/keys :req-un [::goodreads-token ::csrf-token] :opt-un [::user-token]))

;; Common
(s/def ::id int?)
(s/def ::name string?)

;; Author
(s/def ::author (s/keys :req-un [::id ::name]))
(s/def ::authors (s/and
                   (s/map-of ::id ::author)
                   #(instance? PersistentTreeMap %)))

;; Book
(s/def ::title string?)
(s/def ::isbn string?)
(s/def ::picture-url string?)
(s/def ::book (s/keys :req-un [::id ::title ::author ::isbn ::picture-url]))
(s/def ::books (s/and
                 (s/map-of ::id ::book)
                 #(instance? PersistentTreeMap %)))

;; ListItem
(s/def ::book-id int?)
(s/def ::list-id int?)
(s/def ::list-item (s/keys :req-un [::id ::book-id ::list-id]))
(s/def ::list-items (s/and
                      (s/map-of ::id ::list-item)
                      #(instance? PersistentTreeMap %)))

;; List
(s/def ::color string?)
(s/def ::position int?)
(s/def ::list (s/keys :req-un [::id ::name ::color ::position]))
(s/def ::lists (s/and
                 (s/map-of ::id ::list-item)
                 #(instance? PersistentTreeMap %)))

;; User
(s/def ::user (s/keys :req-un [::id ::name]))

;; Loading

(s/def ::db (s/keys :req-un [::config ::authors ::books
                             ::list-items ::lists] 
                    :opt-un [::user]))

(def default-value
  {:config {}
   :authors (sorted-map)
   :books (sorted-map)
   :list-items (sorted-map)
   :lists (sorted-map)})

