(ns bookshelf.subs
  (:require [re-frame.core :refer [reg-sub subscribe reg-sub-raw dispatch]]
            [reagent.ratom :refer [make-reaction]]))

(defn login-user!
  [token {on-success :on-success on-error :on-error}]
  (println "Running query -> " token)
; (js/setTimeout (on-error)))
  (js/setTimeout #(on-success {:id 14 :name "Goerge" :token "1234"}) 4000))
  

(reg-sub :user
  (fn [db _] (:user db)))

(reg-sub-raw :user
  (fn [db [_ _]]
    (let [no-user? (nil? (:user @db))
          has-token? (some? (:user-token (:config @db)))]
      (when (and no-user? has-token?) (dispatch [:login-user]))
    ; (when (nil? (:user @db)) (dispatch [:login-user]))
    ; (login-user! 
    ;   (get-in @db [:config :user-token] [])
    ;   { :on-error #(dispatch [:invalidate-token])
    ;     :on-success #(dispatch [:user-loaded %]) })
      (make-reaction
        (fn [] (get-in @db [:user] []))
        :on-dispose #(println "On Dispose")))))


(reg-sub :user-name
  (fn [_ _] [(subscribe [:user])])
  (fn [[user] _] (:name user)))

(reg-sub :no-saved-session?
  (fn [db _] 
    (nil? (:user-token (:config db)))))

