(ns nopassword.html
	(:gen-class)
	(:require [hiccup.form :as form])
)

;  General html functions

(defn text-input-row [text-name label-text value]
	[:tr [:td (form/label text-name label-text)] [:td (form/text-field text-name value)]]
)

(defn text-input [text-name label-text value]
	[:div (form/label text-name label-text) (form/text-field text-name value)]
)

(defn plain-head []
	[:head 
		[:title "No Password"]
		[:link {:rel "stylesheet" :type "text/css" :href "nopassword.css"}]
	]
)

(defn active-head [token]
	(plain-head)
)

(defn show-errors [error-list]
	[:div (map (fn [line] [:div line]) error-list)]
)
