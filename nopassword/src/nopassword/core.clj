(ns nopassword.core
	(:gen-class)
	(:require [ring.adapter.jetty :as ring])
	(:require [ring.middleware.params :as params])
	(:require [ring.middleware.session :as session])
	(:require [compojure.core :as compojure])
	(:require [compojure.route :as route])
	(:require [clojure.string :as str])
	(:require [ring-debug-logging.core :as debug])
	(:require [nopassword.register :as register])
	(:require [nopassword.login :as login])
	(:require [nopassword.app :as app])
	(:require [nopassword.html :as html])
)

(compojure/defroutes replying
	(compojure/GET "/servers/nopassword/register-prompt" [] (register/register-prompt "" "" []))
	(compojure/POST "/servers/nopassword/register" [name address] (register/register name address))
	(compojure/GET "/servers/nopassword/request-prompt" [] (login/request-prompt "" []))
	(compojure/POST "/servers/nopassword/request" [name] (login/request name))
	(compojure/GET "/servers/nopassword/login" [server-token] (login/login server-token))
	(compojure/GET "/servers/nopassword/app" [:as {{user :user} :session}] (app/app user))
	(compojure/POST "/servers/nopassword/app" [:as {{user :user} :session}] (app/app user))
	(compojure/POST "/servers/nopassword/opt-in" [:as {{user :user} :session}] (app/opt-in user))
	(compojure/POST "/servers/nopassword/opt-out" [:as {{user :user} :session}] (app/opt-out user))
	(compojure/POST "/servers/nopassword/logout" [:as {{user :user} :session}] (app/logout user))
	(compojure/DELETE "/servers/nopassword/delete" [:as {{user :user} :session}] (app/delete user))
	(compojure/GET "/favicon.ico" [] {:status 404})
	(route/resources "/servers/nopassword/")
	(route/not-found {:status 404 :body (html/page [:div "Page not found"])})
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

