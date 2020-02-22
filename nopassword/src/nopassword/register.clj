(ns nopassword.register
	(:gen-class)
	(:require [clojure.java.jdbc :as jdbc])
	(:require [clojure.string :as str])
	(:require [hiccup.page :as page])
	(:require [hiccup.form :as form])
	(:require [valip.core :as valip])
	(:require [valip.predicates :as pred])
	(:require [nopassword.stuff :as stuff])
	(:require [nopassword.html :as html])
	(:require [nopassword.login :as login])
)

;  Registration prompt page html (only thing it does)

(defn register-prompt-form [name address error-list]
	[:div
		(form/form-to [:post "/servers/nopassword/register.exe"]
			[:div
				(html/show-errors error-list)
				(html/text-input :name "Name " name) 
				(html/text-input :address "Address " address)
				(form/submit-button "Register")
			]
		)
	]
)

(defn register-prompt-body [name address error-list]
	[:body [:div (register-prompt-form name address error-list)]]
)

(defn register-prompt [name address error-list]
	(page/html5 (html/plain-head) (register-prompt-body name address error-list))
)

;  registration actions, mail, page

(defn validate-name [db name]
	(let
		[
			query "SELECT COUNT(*) AS count FROM users WHERE valid AND users.name=?;"
			result (jdbc/query db [query name])
		]
		(if (= 0 (get (first result) :count))
			[]
			[(str name " already exists")]
		)
	)
)

(defn validate-address [address]
	(let
		[
			package {:address address}
			message (str address " is not a valid email address")
			errors (valip/validate package [:address pred/email-address? message])
		]
		(get errors :address)
	)
)

(defn insert-user [db name address]
	(let [result (jdbc/insert! db :users {:name name :address address})]
		(get (first result) :id)
	)
)

(defn register-body [name address]
	[:div (str name " has been registered at " address)]
)

(defn registered-page [name address]
	(page/html5 (html/plain-head) (register-body name address))
)

(defn register [name address]
	(jdbc/with-db-transaction [db stuff/db-spec]
		(let [error-list (concat (validate-address address) (validate-name db name))]
			(if (= 0 (count error-list))
				(let [user-id (insert-user db name address)]
					(login/login-email db user-id name address)
					(registered-page name address)
				)
				(register-prompt name address error-list) 
			)
		)
	)
)

