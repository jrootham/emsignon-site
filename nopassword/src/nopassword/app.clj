(ns nopassword.app
	(:gen-class)
	(:require [clojure.java.jdbc :as jdbc])
	; (:require [clojure.string :as str])
	(:require [hiccup.page :as page])
	; (:require [hiccup.form :as form])
	; (:require [clj-http.client :as client])
	; (:require [valip.core :as valip])
	; (:require [valip.predicates :as pred])
	; (:require [crypto.random :as random])
	; (:require [nopassword.stuff :as stuff])
	; (:require [nopassword.mail :as mail])
	(:require [nopassword.html :as html])
	; (:require [nopassword.register :as register])
	; (:require [nopassword.signon :as signon])
	; (:require [nopassword.app :as app])
)

(defn mark-invalid [db user-id]
	(jdbc/update! db :users {:valid false} ["id=?" user-id])
)

(defn app-page-body [db user-id]
	(let 
		[
			query "SELECT name, address, count, contact FROM users WHERE id=?"
			record (first(jdbc/query db [query user-id]))
			{name :name address :address count :count contact :contact} record
		]
		[:div
			[:div name]
			[:div address] 
			[:div (format "Logged on %d times" count)]
			[:div (if contact "Allow contact" "Disallow contact")]
		]
	)
)

(defn app-page [db user-id]
	(page/html5 (html/plain-head) (app-page-body db user-id))
)


(defn check []
)

(defn opt []
)

(defn delete []
)

