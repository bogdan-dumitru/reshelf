(ns bookshelf.api
  (:require [ajax.core :as ajax]))

(defn api-request
  ([method uri] (api-request method uri nil))
  ([method uri body] 
   {:method method
    :uri uri
    :params body
    :timeout 3000
    :response-format (ajax/json-response-format {:keywords? true})
    :format          (ajax/json-request-format)}))

