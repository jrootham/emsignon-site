(ns emlogin.app
	(:gen-class)
	(:require [clojure.java.jdbc :as jdbc])
	(:require [hiccup.core :as hiccup])
	(:require [hiccup.form :as form])
	(:require [hiccup.util :as util])
	(:require [emlogin.stuff :as stuff])
	(:require [emlogin.html :as html])
)

(defn paste-data [name address]
	[:div [:p "The following data is to be cut and pasted into the EMail Login application"]
		[:div 
			{:id "copy"} 
			[:pre 
				(str 
					"emlogin\n" 
					stuff/site "servers/emlogin/app-request\n" 
					(util/escape-html name) "\n"
					(util/escape-html address) "\n"
				)
			]
		]
	]
)

(def redirect
	{
		:status 303
		:headers {"Location" "/servers/emlogin/app"} 
	}
)

(defn kill-session [body]
	{:body body :session nil}
)


(def no-cache
	{"Cache-Control" "no-store"}
)

(defn no-user []
	(html/page "No user logged in")
)

(defn mark-invalid [db user-id]
	(jdbc/update! db :users {:valid false} ["id=?" user-id])
)

(defn set-allow [db user-id allow]
	(jdbc/update! db :users {:contact allow} ["id=?" user-id])
)

(defn get-name [db user-id]
	(get (first (jdbc/query db ["SELECT name FROM users WHERE id=?" user-id])) :name)
)

(defn app-page-contents [db user-id]
	(let 
		[
			query "SELECT name, address, count, contact FROM users WHERE id=?"
			record (first(jdbc/query db [query user-id]))
			{name :name address :address count :count contact :contact} record
			out "/servers/emlogin/opt-out"
			in "/servers/emlogin/opt-in"
			delete "/servers/emlogin/delete"
			logout "/servers/emlogin/logout"
		]
		[:div
			[:div {:id "data"}
				[:div "User name"]
				[:div (util/escape-html name)]

				[:div "Email address"]
				[:div (util/escape-html address)] 

				[:div "Login count"]
				[:div (format "Logged on %d times" count)]

				[:div (if contact "Allow contact" "Disallow contact")]
				(if contact
					(form/form-to [:post out] (form/submit-button "Disallow Contact"))
					(form/form-to [:post in] (form/submit-button "Allow Contact"))
				)

				(form/form-to [:delete delete] (form/submit-button "Delete"))
				(form/form-to [:post logout] (form/submit-button "Logout"))

			]
			(paste-data name address)
		]
	)
)

(defn app-page [db user-id]
	(html/page (app-page-contents db user-id))
)

(defn opt-in [user-id]
	(if user-id
		(jdbc/with-db-transaction [db stuff/db-spec]
			(set-allow db user-id true)
			redirect
		)
		(no-user)
	)
)

(defn opt-out [user-id]
	(if user-id
		(jdbc/with-db-transaction [db stuff/db-spec]
			(set-allow db user-id false)
			redirect
		)
		(no-user)
	)
)

(defn logout [user-id]
	(if user-id
		(kill-session (html/page (str (get-name stuff/db-spec user-id) " is logged out")))
		(no-user)
	)
)

(defn delete [user-id]
	(if user-id
		(jdbc/with-db-transaction [db stuff/db-spec]
			(mark-invalid db user-id)
			(kill-session (html/page (str (get-name db user-id) " is deleted")))
		)
		(no-user)		
	)
)

(defn app [user-id]
	(if user-id
		{:headers no-cache :body (app-page stuff/db-spec user-id)}
		(no-user)		
	)
)


