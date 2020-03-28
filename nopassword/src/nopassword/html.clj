(ns nopassword.html
	(:gen-class)
	(:require [hiccup.core :as hiccup])
	(:require [hiccup.form :as form])
	(:require [nopassword.stuff :as stuff])
)

;  General html functions


(defn custom-meta [name content]
	[:meta {:name name :content content}]	
)
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

(defn href [server-token]
	(format "%sservers/nopassword/login?server-token=%016x" stuff/site server-token)
)

;  The three possible headers

(defn mail-head []
	""
)

(defn browser-head []
	[:head 
		[:title "No Password"]
		[:link {:rel "stylesheet" :type "text/css" :href "nopassword.css"}]
	]
)

(defn app-head [server-token]
	[:head (custom-meta "np-target" (href server-token))]
)

; The standard browser page

(defn page [contents]
	(hiccup/html
		(browser-head)
		[:div {:id "outer"}
			[:div {:id "title"} "No Password"]
			[:div {:id "container"} contents]
			[:div 
				[:div [:a {:href (str stuff/site "nopassword/actions.html")} "Actions"]] 
				[:div [:a {:href (str stuff/site "nopassword/index.html")} "Home"]]
			]
		]
	)
)
