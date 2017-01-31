(ns bookshelf.api
  (:require [ajax.core :as ajax]))

(defn api-prepare-body
  [method body {:keys [config]}]
  (let [csrf-body {(keyword (:csrf-param config)) 
                   (:csrf-token config)}
        token-present? (some? (:user-token config))
        token-body (when token-present? {:token (:user-token config)})]

  (condp = method
    :post (merge body csrf-body token-body)
    :put (merge body csrf-body token-body)
    (merge body token-body))))

(defn api-request
  ([method uri db] (api-request method uri db {}))
  ([method uri db body] 
   {:method method
    :uri uri
    :params (api-prepare-body method body db)
    :timeout 3000
    :response-format (ajax/json-response-format {:keywords? true})
    :format          (ajax/json-request-format)}))

(defn get-books
  [{:keys [config]} query]
  (let [api-key (:google-books-token config)]
  { :method :get
    :uri "https://www.googleapis.com/books/v1/volumes"
    :params {:key api-key :q query }
    :response-format (ajax/json-response-format {:keywords? true})
    :timeout 5000 }))

