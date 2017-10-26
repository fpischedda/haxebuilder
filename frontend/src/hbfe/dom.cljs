(ns hbfe.dom
  (:require
   [clojure.string :as str]
   [goog.dom.classes :as classes]))

(defn q [selector]
  (js/document.querySelector selector))

(defn set-value! [el value]
  (set! (.-value el) value))

(defn value [el]
  (let [val (.-value el)]
    (when-not (str/blank? val)
      (str/trim val))))

(defn elem-value [selector]
  (value (q selector)))

(defn add-class!
  "Adds the specified CSS class to each node in the content."
  [node class]
  (classes/add node class)
  node)

(defn remove-class!
  "Removes the specified CSS class from each node in the content."
  [node class]
  (classes/remove node class)
  node)

(defn toggle-class!
  "Toggles the specified CSS class from each node in the content."
  [node class]
  (classes/toggle node class)
  node)
