(ns nopassword.html
	(:gen-class)
	(:require [hiccup.form :as form])
)

;  General html functions

(defn text-input [text-name label-text value]
	[:div (form/label text-name label-text) (form/text-field text-name value)]
)

(defn plain-head []
	[:head [:title "No Password"]]
)

(defn active-head [token]
	[:head [:title "No Password"]]
)

(defn show-errors [error-list]
	[:div (map (fn [line] [:div line]) error-list)]
)
