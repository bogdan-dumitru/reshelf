(ns bookshelf.core
  (:require [devtools.core :as devtools]
            [clojure.browser.dom :as dom]))

;; -- Debugging aids ----------------------------------------------------------
(devtools/install!)       ;; we love https://github.com/binaryage/cljs-devtools
(enable-console-print!)   ;; so that println writes to `console.log`

(defn ^:export main
  []
  (dom/append (.-body js/document) (dom/element "div" "Let's get cracking!")))
