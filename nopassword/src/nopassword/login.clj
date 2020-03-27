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
			(html/group [:div {:id "login-group"}] [(html/label-text-field :name "User name " name)])
			(form/submit-button "Request login")
		)
	]
)

(defn request-prompt [name error-list]
	(html/page (request-prompt-contents name error-list))
)

(defn href [server-token]
	(format "%s/servers/nopassword/login?server-token=%016x" stuff/site server-token)
)

(defn mail-contents [server-token name]
	[:body
		[:div
			[:h1 "No Password"]
			[:div (str "No password login for " name)]
			[:div [:a {:href (href server-token)} "Login"]]
		]
	]
)

(defn body [head server-token name]
	(page/html5 head (mail-contents server-token name))
)

(defn mail [db user-id server-token subject head name address]
	(jdbc/insert! db :tokens {:server_token server-token :user_id user-id})
	(mail/send-mail mail/std-from address subject (body head server-token name))
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

(defn make-request [name server-token subject headers found not-found]
	(jdbc/with-db-transaction [db stuff/db-spec]
		(let [result (get-user db name)]
			(if (== 1 (count result))
				(let [{user-id :id address :address} (first result)]
					(mail db user-id server-token subject headers name address)
					(found name address)
				)
				(not-found name)
			)
		)
	)	
)

(defn app-subject [app-token]
	(format "[#! nopassword %s] No Password Signon" app-token)
)

(defn app-head [server-token]
	[:head
		[
			(html/custom-meta "np-target" (href server-token))
		]
	]
)
	
(defn app-found [name address]
	{:status 200 :body ""}
)
					
(defn app-not-found [name]
	{:status 404 :body (str name " not found")}
)

(defn app-request [name app-token]
	(let 
		[
			server-token (make-token)
			subject (app-subject app-token)
			head (app-head server-token)
		]
		(make-request name server-token subject head app-found app-not-found)
	)
)

(def simple-subject "No Password Login")

(defn found [name address]
	(request-page name address)
)

(defn not-found [name]
	(request-prompt name [(str "Name " name " not found")])
)

(defn request [name]
	(make-request name (make-token) simple-subject (html/plain-head) found not-found)
)

(defn login-email [db user-id name address]	
	(mail db user-id (make-token) simple-subject (html/plain-head) name address)
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
					(assoc app/redirect :session {:user user-id})
				)
				(html/page "Attempted to reuse link. They are only good for one try.")
			)
		) 
	)
)
