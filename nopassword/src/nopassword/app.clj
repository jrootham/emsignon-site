(ns nopassword.app
	(:gen-class)
	(:require [clojure.java.jdbc :as jdbc])
	(:require [hiccup.core :as hiccup])
	(:require [hiccup.form :as form])
	(:require [nopassword.stuff :as stuff])
	(:require [nopassword.html :as html])
)

(defn redirect []
	{
		:status 301 
		:headers {"Location" "/servers/nopassword/app"} 
	}

)

(defn no-user []
	{:headers {"Cache-Control" "no-cache"} :body (html/page "No user logged in")}		
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
		]
		[:div {:id "data"}
			[:div "User name"]
			[:div name]

			[:div "Email address"]
			[:div address] 

			[:div "Login count"]
			[:div (format "Logged on %d times" count)]

			[:div (if contact "Allow contact" "Disallow contact")]
			(if contact
				(form/form-to [:post "/servers/nopassword/opt-out"] (form/submit-button "Disallow Contact"))
				(form/form-to [:post "/servers/nopassword/opt-in"] (form/submit-button "Allow Contact"))
			)

			(form/form-to [:delete "/servers/nopassword/delete"] (form/submit-button "Delete"))
			(form/form-to [:post "/servers/nopassword/logout"] (form/submit-button "Logout"))
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
			(redirect)
		)
		(no-user)
	)
)

(defn opt-out [user-id]
	(if user-id
		(jdbc/with-db-transaction [db stuff/db-spec]
			(set-allow db user-id false)
			(redirect)
		)
		(no-user)
	)
)

(defn kill-session [body]
	{:headers {"Cache-Control" "no-cache"} :body body :session nil}
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
			(kill-session (html/page (str (get-name db user-id) "is deleted")))
		)
		(no-user)		
	)
)

(defn app [user-id]
	(if user-id
		{:headers {"Cache-Control" "no-cache"} :body (app-page stuff/db-spec user-id)}
		(no-user)		
	)
)


