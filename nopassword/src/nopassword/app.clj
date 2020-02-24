(ns nopassword.app
	(:gen-class)
	(:require [clojure.java.jdbc :as jdbc])
	(:require [hiccup.core :as hiccup])
	(:require [hiccup.form :as form])
	(:require [nopassword.stuff :as stuff])
	(:require [nopassword.html :as html])
)

(defn mark-invalid [db user-id]
	(jdbc/update! db :users {:valid false} ["id=?" user-id])
)

(defn set-allow [db user-id allow]
	(jdbc/update! db :users {:contact allow} ["id=?" user-id])
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
			(if contact
				(form/form-to [:post "/servers/nopassword/opt-out"] (form/submit-button "Disallow Contact"))
				(form/form-to [:post "/servers/nopassword/opt-in"] (form/submit-button "Allow Contact"))
			)
			(form/form-to [:post "/servers/nopassword/delete"] (form/submit-button "Delete"))
			(form/form-to [:post "/servers/nopassword/logout"] (form/submit-button "Logout"))
		]
	)
)

(defn app-page [db user-id]
	(hiccup/html (html/plain-head) (app-page-body db user-id))
)

(defn opt-in [user-id]
	(jdbc/with-db-transaction [db stuff/db-spec]
		(set-allow db user-id true)
		(app-page db user-id)
	)
)

(defn opt-out [user-id]
	(jdbc/with-db-transaction [db stuff/db-spec]
		(set-allow db user-id false)
		(app-page db user-id)
	)
)

(defn kill-session [body]
	{:body body :session nil}
)

(defn logout [user-id]
	(kill-session "logged out")
)

(defn delete [user-id]
	(jdbc/with-db-transaction [db stuff/db-spec]
		(mark-invalid db user-id)
		(kill-session "Deleted")
	)
)

