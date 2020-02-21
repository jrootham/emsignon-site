(ns nopassword.core
	(:gen-class)
	(:require [clojure.java.jdbc :as jdbc])
	(:require [ring.adapter.jetty :as ring])
	(:require [ring.middleware.params :as params])
	(:require [ring.middleware.session :as session])
	(:require [compojure.core :as compojure])
	(:require [compojure.route :as route])
	(:require [clojure.string :as str])
	(:require [ring-debug-logging.core :as debug])
	(:require [hiccup.page :as page])
	(:require [hiccup.form :as form])
	(:require [clj-http.client :as client])
	(:require [valip.core :as valip])
	(:require [valip.predicates :as pred])
	(:require [crypto.random :as random])
	(:require [nopassword.stuff :as stuff])
)

; constants

(def std-from "jim.rootham@utoronto.ca")

;  general mail functions

(defn mail-config [from to subject body]
	{
		:oauth-token stuff/mail-key
		:content-type :applicaton/json
		:form-params
		{
			:personalizations[{:to [{:email to}]}]
			:from {:email from}
			:subject subject
			:content [{:type "text/html" :value body}]
		}
	}
)

(defn send-mail [from to subject body]
	(client/post "https://api.sendgrid.com/v3/mail/send" (mail-config from to subject body))
)

;  General html functions

(defn text-input [text-name label-text value]
	[:div (form/label text-name label-text) (form/text-field text-name value)]
)

(defn plain-head []
	[:head [:title "No Password"]]
)

(defn active-head [token]
	[:head [:title "No Password"]]
)

;  Prompt page html (only thing it does)

(defn prompt-form [name address errors]
	[:div
		(form/form-to [:post "/servers/nopassword/register.exe"]
			[:div
				[:div (map (fn [line] [:div line]) errors)]
				(text-input :name "Name " name) 
				(text-input :address "Address " address)
				(form/submit-button "Register")
			]
		)
	]
)

(defn prompt-body [name address errors]
	[:body [:div (prompt-form name address errors)]]
)

(defn prompt [name address errors]
	(page/html5 (plain-head) (prompt-body name address errors))
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

(defn make-token []
	(Long/parseUnsignedLong (random/hex 8) 16)
)

(defn html-body [token name]
	[:body
		[:div
			[:h1 "No Password"]
			[:div (str "No password signon for " name)]
			(let [format-string "%s/servers/nopassword/login.exe?token=%016x"]
				[:div [:a {:href (format format-string stuff/site token)} "Signon"]]
			)
		]
	]
)

(defn mail-subject [token]
	(format "[#! nopassword %016x] No Password Signon" token)
)

(defn mail-body [token name]
	(page/html5 (active-head token) (html-body token name))
)

(defn login-email [db user-id name address]
	(let [token (make-token)]
		(jdbc/insert! db :tokens {:token token :user_id user-id})
		(send-mail std-from address (mail-subject token) (mail-body token name))
	)
)

(defn register-body [name address]
	[:div (str name " has been registered at " address)]
)

(defn registered-page [name address]
	(page/html5 (plain-head) (register-body name address))
)

(defn register [name address]
	(jdbc/with-db-transaction [db stuff/db-spec]
		(let [error-list (concat (validate-address address) (validate-name db name))]
			(if (= 0 (count error-list))
				(let [user-id (insert-user db name address)]
					(login-email db user-id name address)
					(registered-page name address)
				)
				(prompt name address error-list) 
			)
		)
	)
)

(defn page-body [db user-id]
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

(defn page [db user-id]
	(page/html5 (plain-head) (page-body db user-id))
)

(defn request [name]
)

(defn check []
)


(defn fetch-token-user [db token]
	(let
		[
			query "SELECT user_id FROM tokens WHERE token=?;"
			result (jdbc/query db [query token])
		]
		(if (= 1 (count result))
			(let [user-id (get (first result) :user_id)]
				(jdbc/delete! db :tokens ["token=?" token])
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

(defn login [token-string]
	(jdbc/with-db-transaction [db stuff/db-spec]
		(let 
			[
				token (Long/parseUnsignedLong token-string 16)
				user-id (fetch-token-user db token)
			]
			(update-count db user-id)
			(if user-id
				{:body (page db user-id) :sesssion user-id}
				{:status 400 :body "token search failure"}
			)
		) 
	)
)

(defn opt []
)

(defn delete []
)

(compojure/defroutes replying
	(compojure/GET "/servers/nopassword/prompt.exe" [] (prompt "" "" []))
	(compojure/POST "/servers/nopassword/register.exe" [name address] (register name address))
	(compojure/POST "/servers/nopassword/request.exe" [name] (request name))
	(compojure/POST "/servers/nopassword/check.exe" [] (check))
	(compojure/GET "/servers/nopassword/login.exe" [token] (login token))
	(compojure/POST "/servers/nopassword/opt.exe" [] (opt))
	(compojure/POST "/servers/nopassword/delete.exe" [] (delete))
	(compojure/GET "/favicon.ico" [] {:status 404})
	(route/not-found {:status 404 :body "Not Found"})
)

(defn wrapper []
	(-> replying
		(params/wrap-params)
		(session/wrap-session)
		(debug/wrap-with-logger)
	)
)

(defn -main
  	"No password"
  	[& args]
  	(if (== 1 (count args))
		(try
			(let [port (Integer/parseInt (first args))]
				(ring/run-jetty (wrapper) {:host "127.0.0.1" :port port})
			)
			(catch NumberFormatException exception 
				(println (str args[0] " is not an int"))
			)
		)
	  	(println "Usage: hello port")
	)  	
)

