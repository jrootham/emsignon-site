(ns emlogin.config
	(:gen-class)
	(:require [clojure.java.jdbc :as jdbc])
;	(:require [hiccup.core :as hiccup])
;	(:require [hiccup.form :as form])
;	(:require [hiccup.util :as util])
	(:require [ring.util.json-response :as json])
	(:require [emlogin.stuff :as stuff])
;	(:require [emlogin.html :as html])
)

(defn config-link [name]
	(let [href (str stuff/site "servers/emlogin/config?identifier=" name)]
		[:div [:a {:href href} "Configuration"]]
	)
)

(defn config-response [result]
	(let 
		[
			record (first result)
			{name :name address :address} record
			endpoint (str stuff/site "servers/emlogin/app-request")
		]
		(json/json-response 
			{
				:found true
				:name "EMail Login" 
				:endpoint endpoint 
				:identifier name 
				:address address
			}
		)
	)
)

(defn send-config [db identifier]
	(let 
		[
			query "SELECT name, address, count, contact FROM users WHERE valid AND name=?"
			result (jdbc/query db [query identifier])
		]
		(if (== 1 (count result))
			(config-response result)
			(do
				(println "no unique identifier" identifier)
				(json/json-response {:found "false"})
			)
		)
	)
)

(defn config [identifier]
	(let [db stuff/db-spec]
		(send-config db identifier)
	)
)

