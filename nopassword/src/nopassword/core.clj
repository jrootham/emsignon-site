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
)

(compojure/defroutes replying
	(compojure/GET "/servers/nopassword/register-prompt.exe" [] (register/register-prompt "" "" []))
	(compojure/POST "/servers/nopassword/register.exe" [name address] (register/register name address))
	(compojure/GET "/servers/nopassword/request-prompt.exe" [] (login/request-prompt "" []))
	(compojure/POST "/servers/nopassword/request.exe" [name] (login/request name))
	(compojure/POST "/servers/nopassword/check.exe" [] (app/check))
	(compojure/GET "/servers/nopassword/login.exe" [token] (login/login token))
	(compojure/POST "/servers/nopassword/opt.exe" [] (app/opt))
	(compojure/POST "/servers/nopassword/delete.exe" [] (app/delete))
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

