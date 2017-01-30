(ns bookshelf.events
  (:require
    [bookshelf.coeffects :refer [user-token->local-store]]
    [bookshelf.db :refer [default-value]]
    [day8.re-frame.http-fx]
    [re-frame.core :refer [reg-event-db reg-event-fx inject-cofx path trim-v
                           after debug]]
    [bookshelf.api :refer [api-request]]
    [cljs.spec     :as s]))

(defn check-and-throw
  "throw an exception if db doesn't match the spec"
  [the-spec db]
  (when-not (s/valid? the-spec db)
    (throw (ex-info (str "spec check failed: " (s/explain-str the-spec db)) {}))))

(def check-spec-interceptor (after (partial check-and-throw :bookshelf.db/db)))

(reg-event-fx
  :initialise-db
  [(inject-cofx :local-store-token)
   (inject-cofx :metas)
   check-spec-interceptor]
  (fn [{:keys [local-store-token metas]} _]
    (let [filter-nils #(into {} (filter (comp some? val) %))
          config (filter-nils (-> {}
                     (assoc :user-token local-store-token)
                     (assoc :goodreads-token (:goodreads-token metas))
                     (assoc :csrf-token (:csrf-token metas))
                     (assoc :csrf-param (:csrf-param metas))))]
    {:db (assoc default-value :config config)})))
        

(reg-event-db
  :write-to
  [check-spec-interceptor]
  (fn [db [_ path v]]
    (assoc-in db path v)))
  
(reg-event-db
  :invalidate-token
  [check-spec-interceptor]
  (fn [db _]
    (update-in db [:config] dissoc :user-token)))

(reg-event-db
  :user-loaded
  [check-spec-interceptor
   (after user-token->local-store)]
  (fn [db [_ user]]
    (let [new-db (-> db
                   (assoc :user-loading false)
                   (assoc-in [:config :user-token] (:token user))
                   (assoc :user user))]
    new-db)))

(reg-event-fx
  :login-user
  [check-spec-interceptor]
  (fn [{:keys [db]} _]
    (let [req-params (merge (api-request :get "/api/session" db)
                            {:on-success [:user-loaded]
                             :on-failure [:invalidate-token] })]
    {:db (assoc db :user-loading true)
     :http-xhrio req-params})))
  
(reg-event-fx
  :create-session
  [check-spec-interceptor]
  (fn [{:keys [db]} [_ user-name]]
    (let [req-body {:user {:name user-name}}
          req-params (merge (api-request :post "/api/session" db req-body)
                            {:on-success [:user-loaded]
                             :on-failure [:user-load-error] })]
    {:db (assoc db :user-loading true)
     :http-xhrio req-params})))
  
