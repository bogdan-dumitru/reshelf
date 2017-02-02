(ns bookshelf.events
  (:require
    [bookshelf.coeffects :refer [user-token->local-store]]
    [bookshelf.db :refer [default-value]]
    [day8.re-frame.http-fx]
    [re-frame.core :refer [reg-event-db reg-event-fx inject-cofx path trim-v
                           after debug]]
    [bookshelf.api :refer [api-request get-books get-book]]
    [bookshelf.api.response-helpers :as rh]
    [cljs.spec     :as s]))

(defn check-and-throw
  "throw an exception if db doesn't match the spec"
  [the-spec db]
  (when-not (s/valid? the-spec db)
    (throw (ex-info (str "spec check failed: " (s/explain-str the-spec db)) {}))))

(def check-spec-interceptor (after (partial check-and-throw :bookshelf.db/db)))

(def base-intercept [check-spec-interceptor trim-v])

;; Utility

(reg-event-fx
  :initialise-db
  [(inject-cofx :local-store-token)
   (inject-cofx :metas)
   base-intercept]
  (fn [{:keys [local-store-token metas]} _]
    (let [filter-nils #(into {} (filter (comp some? val) %))
          config (filter-nils (-> {}
                     (assoc :user-token local-store-token)
                     (assoc :google-books-token (:google-books-token metas))
                     (assoc :csrf-token (:csrf-token metas))
                     (assoc :csrf-param (:csrf-param metas))))]
    {:db (assoc default-value :config config)})))
        

(reg-event-db
  :write-to
  [base-intercept]
  (fn [db [path v]]
    (assoc-in db path v)))

(reg-event-fx
  :api-error
  [base-intercept]
  (fn [_ [event resp]] 
    (println "API Error on event " event)
    (println resp)
    {}))

;; Session
  
(reg-event-db
  :invalidate-token
  [base-intercept]
  (fn [db _]
    (update-in db [:config] dissoc :user-token)))

(reg-event-db
  :user-loaded
  [base-intercept
   (after user-token->local-store)]
  (fn [db [user]]
    (let [new-db (-> db
                   (assoc :user-loading false)
                   (assoc-in [:config :user-token] (:token user))
                   (assoc :user user))]
    new-db)))

(reg-event-fx
  :login-user
  [base-intercept]
  (fn [{:keys [db]} _]
    (let [req-params (merge (api-request :get "/api/session" db)
                            {:on-success [:user-loaded]
                             :on-failure [:invalidate-token] })]
    {:db (assoc db :user-loading true)
     :http-xhrio req-params})))
  
(reg-event-fx
  :create-session
  [check-spec-interceptor]
  (fn [{:keys [db]} [event user-name]]
    (let [req-body {:user {:name user-name}}
          req-params (merge (api-request :post "/api/session" db req-body)
                            {:on-success [:user-loaded]
                             :on-failure [:api-error event] })]
    {:db (assoc db :user-loading true)
     :http-xhrio req-params})))
  
;; Search

(reg-event-db
  :set-search-loading
  [base-intercept]
  (fn [db _] 
    (assoc-in db [:search :loading?] true)))

(reg-event-db
  :search-finished
  [base-intercept rh/parse-google-books-search]
  (fn [db [book-map] ]
    (-> db
        (assoc-in [:search :results] (keys book-map))
        (assoc-in [:search :loading?] false)
        (update-in [:books] merge book-map))))

(reg-event-fx
  :start-search
  []
  (fn [{:keys [db]} [event query]]
    (let [req-params (merge (get-books db query)
                            {:on-success [:search-finished]
                             :on-failure [:api-error event] })]
    {:http-xhrio req-params})))

(reg-event-db
  :book-loaded
  [base-intercept rh/parse-google-book]
  (fn [db [book] ]
    (update-in db [:books] merge {(:book-id book) book})))

(reg-event-fx
  :load-book
  []
  (fn [{:keys [db]} [event book-id]]
    (let [req-params (merge (get-book db book-id)
                            {:on-success [:book-loaded]
                             :on-failure [:api-error event] })]
    {:http-xhrio req-params})))

;; Lists

(reg-event-db
  :list-created
  [base-intercept rh/parse-list]
  (fn [db [parsed-list parsed-list-items]]
    (-> db
        (update-in [:lists] conj [(:id parsed-list) parsed-list])
        (update-in [:list-items] merge parsed-list-items))))

(reg-event-fx
  :lists-loaded
  [base-intercept rh/parse-lists]
  (fn [{:keys [db]} [parsed-lists parsed-list-items]]
    (let [ndb (-> db
                (update-in [:lists] merge parsed-lists)
                (update-in [:list-items] merge parsed-list-items))]
    {:db ndb})))


(reg-event-fx
  :load-lists
  []
  (fn [{:keys [db]} [event]]
    (let [req-params (merge (api-request :get "/api/lists" db)
                            {:on-success [:lists-loaded]
                             :on-failure [:api-error event] })]
    {:http-xhrio req-params})))

;; Books

(reg-event-db
  :clear-book-from-queue
  [base-intercept]
  (fn [db _]
    (dissoc db :book-in-queue-id)))

(reg-event-db
  :list-item-created
  [base-intercept rh/parse-list-item]
  (fn [db [list-item]]
    (-> db 
      (update-in [:list-items] merge {(:id list-item) list-item})
      (update-in [:lists (:list-id list-item) :list-item-ids] conj (:id list-item)))))

(defn- add-book-to-new-list
  [event db book-id is-first?]
  (let [user-name (-> db (:user) (:name))
        req-body {:list {:name (str user-name "'s " 
                                    (if is-first? "first" "new")
                                    " list") 
                         :book_id book-id}}]
  (merge (api-request :post "/api/lists" db req-body)
         {:on-success [:list-created]
          :on-failure [:api-error event] })))

(defn- add-book-to-existing-list
  [event db book-id list-id]
  (let [req-body {:list_item {:book_id book-id}}]
  (merge (api-request :post (str "/api/lists/" list-id "/list_items") db req-body)
         {:on-success [:list-item-created]
          :on-failure [:api-error event] })))

(reg-event-fx
  :add-book-to-list
  [check-spec-interceptor]
  (fn [{:keys [db]} [event book-id list-id]]
    {:http-xhrio (if (some? list-id) (add-book-to-existing-list event db book-id list-id)
                                     (add-book-to-new-list event db book-id false))
     :db (dissoc db :book-in-queue-id)}))

(reg-event-fx
  :start-add-new-book
  []
  (fn [{:keys [db]} [event book-id]]
    (if (seq (:lists db))
        {:db (assoc db :book-in-queue-id book-id)}
        {:http-xhrio (add-book-to-new-list event db book-id true)})))

