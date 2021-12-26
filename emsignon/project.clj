(defproject emlogin "0.1.0"
  	:description "Demo server for emlogin"
  	:url "https://jrootham.ca/emlogin"
	:dependencies 
	[
		[org.clojure/clojure "1.10.1"]
		[ring/ring-jetty-adapter "1.8.0"]
		[ring-json-response "0.2.0"]
		[org.postgresql/postgresql "42.1.4"]
		[org.clojure/java.jdbc "0.7.11"]
		[com.sun.activation/javax.activation "1.2.0"]
		[com.draines/postal "2.0.3"]
		[compojure "1.6.1"]
		[hiccup "2.0.0-alpha2"]
		[clj-http "3.10.0"]
		[cheshire "5.10.0"]
		[valip "0.2.0"]
		[crypto-random "1.2.0"]
		[bananaoomarang/ring-debug-logging "1.1.0"]
	]
	:main ^:skip-aot emlogin.core
  	:target-path "target/%s"
  	:profiles {:uberjar {:aot :all}}
)
