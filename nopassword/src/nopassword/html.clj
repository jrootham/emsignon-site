(ns nopassword.html
	(:gen-class)
	(:require [hiccup.core :as hiccup])
	(:require [hiccup.form :as form])
)

;  General html functions

(defn label-text-field [text-name label-text value]
	[(form/label text-name label-text) (form/text-field text-name value)]
)

(defn label-checkbox [checkbox-name label-text checked]
	[(form/label checkbox-name label-text) (form/check-box checkbox-name checked)]
)

(defn group [group-head contents]
	(reduce (fn [rest next] (let [[a b] next] (conj (conj rest a) b))) group-head contents)
)

(defn show-errors [error-list]
	[:div (map (fn [line] [:div line]) error-list)]
)

(defn plain-head []
	[:head 
		[:title "No Password"]
		[:link {:rel "stylesheet" :type "text/css" :href "nopassword.css"}]
	]
)

(defn page [contents]
	(hiccup/html
		(plain-head)
		[:div {:id "outer"}
			[:div {:id "title"} "No Password"]
			[:div {:id "container"} contents]
		]
	)
)

(defn custom-meta [name content]
	[:meta (str "name=\"" name "\" content=\"" content "\"")]	
)
