(ns nopassword.login
	(:gen-class)
	(:require [clojure.java.jdbc :as jdbc])
	(:require [clojure.string :as str])
	(:require [hiccup.core :as hiccup])
	(:require [hiccup.page :as page])
	(:require [hiccup.form :as form])
	(:require [crypto.random :as random])
	(:require [nopassword.stuff :as stuff])
	(:require [nopassword.mail :as mail])
	(:require [nopassword.html :as html])
	(:require [nopassword.app :as app])
)


(defn make-token []
	(Long/parseUnsignedLong (random/hex 8) 16)
)

(defn request-prompt-contents [name error-list]
	[:div
		[:div "Please enter your user name for the site No Password."]
		[:div "An email will be sent to the email address we have on file with a link to signon to the site with."]
		(form/form-to [:post "/servers/nopassword/request"]
			(html/show-errors error-list)
			(html/text-input [:div {:id "login-group"}] :name "User name " name)
			(form/submit-button "Request login")
		)
	]
)

(defn request-prompt [name error-list]
	(html/page (request-prompt-contents name error-list))
)


(defn mail-contents [server-token name]
	[:body
		[:div
			[:h1 "No Password"]
			[:div (str "No password login for " name)]
			(let [format-string "%s/servers/nopassword/login?server-token=%016x"]
				[:div [:a {:href (format format-string stuff/site server-token)} "Login"]]
			)
		]
	]
)

(defn mail-subject [server-token]
;	(format "[#! nopassword %016x] No Password Signon" server-token)
	(format "No Password Login" server-token)
)

(defn mail-body [server-token name]
	(page/html5 (html/active-head server-token) (mail-contents server-token name))
)

(defn login-email [db user-id name address]
	(let 
		[
			server-token (make-token)
			subject (mail-subject server-token)
			body (mail-body server-token name)
		]
		(jdbc/insert! db :tokens {:server_token server-token :user_id user-id})
		(mail/send-mail mail/std-from address subject body)
	)
)

(defn request-body [name address]
	[:div 
		[:div (str "User " name " at " address " has requested a log in")]
		[:div "An email will be sent to the email address we have on file with a link to log in to the site with."]	
	]
)

(defn request-page [name address]
	(html/page (request-body name address))
)

(defn get-user [db name]
	(jdbc/query db ["SELECT id, name, address FROM users WHERE valid AND name=?" name])
)

(defn request [name]
	(jdbc/with-db-transaction [db stuff/db-spec]
		(let [result (get-user db name)]
			(if (= 1 (count result))
				(let [{user-id :id name :name address :address} (first result)]
					(login-email db user-id name address)
					(request-page name address)
				)
				(request-prompt name ["Name not found"])
			)
		)
	)	
)

(defn fetch-token-user [db server-token]
	(let
		[
			query "SELECT user_id FROM tokens WHERE server_token=?;"
			result (jdbc/query db [query server-token])
		]
		(if (= 1 (count result))
			(let [user-id (get (first result) :user_id)]
				(jdbc/delete! db :tokens ["server_token=?" server-token])
				user-id
			)
			nil
		)
	)
)

(defn update-count [db user-id]
	(let
		[
			query "SELECT count FROM users WHERE id=?"
			record (first(jdbc/query db [query user-id]))
			count (get record :count)
		]
		(jdbc/update! db :users {:count (+ count 1)} ["id=?" user-id])
	)
)

(defn login [server-token-string]
	(jdbc/with-db-transaction [db stuff/db-spec]
		(let 
			[
				server-token (Long/parseUnsignedLong server-token-string 16)
				user-id (fetch-token-user db server-token)
			]
			(if user-id
				(do 
					(update-count db user-id)
					{
						:status 301 
						:headers {"Location" "/servers/nopassword/app"} 
						:session {:user user-id}
					}
				)
				(html/page "Attempted to reuse link. They are only good for one try.")
			)
		) 
	)
)
