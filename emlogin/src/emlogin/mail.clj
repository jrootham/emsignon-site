(ns emlogin.mail
	(:gen-class)
	(:require [emlogin.stuff :as stuff])
	(:require [postal.core :as postal])
)

; constants

(def std-from "jim.rootham@utoronto.ca")

;  general mail functions

(defn mail-config [from to subject body]
	{
		:to to
		:from from
		:subject subject
		:body [{:type "text/html" :content body}]
	}
)

(defn send-mail [from to subject body]
	(postal/send-message stuff/mail-spec (mail-config from to subject body))
)

