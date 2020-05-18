(ns emlogin.core
	(:gen-class)
	(:require [ring.adapter.jetty :as ring])
	(:require [ring.middleware.params :as params])
	(:require [ring.middleware.session :as session])
	(:require [compojure.core :as compojure])
	(:require [compojure.route :as route])
	(:require [clojure.string :as str])
	(:require [ring-debug-logging.core :as debug])
	(:require [emlogin.register :as register])
	(:require [emlogin.login :as login])
	(:require [emlogin.app :as app])
	(:require [emlogin.config :as config])
	(:require [emlogin.html :as html])
)

(compojure/defroutes replying
	(compojure/GET "/servers/emlogin/register-prompt" [] (register/register-prompt "" "" false []))
	(compojure/POST "/servers/emlogin/register" [useapp name address] 
		(register/register useapp name address))

	(compojure/GET "/servers/emlogin/request-prompt" [] (login/request-prompt "" []))
	(compojure/POST "/servers/emlogin/request" [name] (login/request name))
	(compojure/GET "/servers/emlogin/app-request" [identifier token] 
		(login/app-request identifier token))

	(compojure/GET "/servers/emlogin/config" [identifier] (config/config identifier))

	(compojure/GET "/servers/emlogin/login" [server-token] (login/login server-token))

	(compojure/GET "/servers/emlogin/app" [:as {{user :user} :session}] (app/app user))
	(compojure/POST "/servers/emlogin/opt-in" [:as {{user :user} :session}] (app/opt-in user))
	(compojure/POST "/servers/emlogin/opt-out" [:as {{user :user} :session}] (app/opt-out user))
	(compojure/POST "/servers/emlogin/logout" [:as {{user :user} :session}] (app/logout user))
	(compojure/DELETE "/servers/emlogin/delete" [:as {{user :user} :session}] (app/delete user))

	(compojure/GET "/favicon.ico" [] {:status 404})
	(route/resources "/servers/emlogin/")
	(route/not-found {:status 404 :body (html/page [:div "Page not found"])})
)

(defn wrapper []
	(-> replying
		(params/wrap-params)
		(session/wrap-session)
;		(debug/wrap-with-logger)
	)
)

(defn -main
  	"EMail Login"
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
	  	(println "Usage: emlogin port")
	)  	
)

