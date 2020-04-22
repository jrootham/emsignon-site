#!/bin/bash

ln -sf /home/jrootham/dev/business/emlogin-site/emlogin/src/emlogin/devstuff.clj \
	/home/jrootham/dev/business/emlogin-site/emlogin/src/emlogin/stuff.clj

lein uberjar

ln -sf /home/jrootham/dev/business/emlogin-site/emlogin/target/uberjar/emlogin-0.1.0-standalone.jar \
	/home/jrootham/dev/website/servers/emlogin/emlogin.jar

