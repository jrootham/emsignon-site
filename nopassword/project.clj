(defproject nopassword "0.1.0"
  	:description "Demo server for nopassword"
  	:url "http://jrootham.ca/nopassword"
	:dependencies 
	[
		[org.clojure/clojure "1.8.0"]
		[ring/ring-jetty-adapter "1.8.0"]
		[compojure "1.6.1"]
		[hiccup "2.0.0-alpha2"]
		[clj-http "3.10.0"]
		[cheshire "5.10.0"]
		[bananaoomarang/ring-debug-logging "1.1.0"]
	]
	:main ^:skip-aot nopassword.core
  	:target-path "target/%s"
  	:profiles {:uberjar {:aot :all}}
)
