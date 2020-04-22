#!/bin/bash

ln -sf /home/jrootham/dev/business/emlogin-site/emlogin/src/emlogin/prodstuff.clj \
	/home/jrootham/dev/business/emlogin-site/emlogin/src/emlogin/stuff.clj

lein uberjar

scp /home/jrootham/dev/business/emlogin-site/emlogin/target/uberjar/emlogin-0.1.0-standalone.jar \
	jrootham@jrootham.ca:/home/jrootham/servers/emlogin/emlogin.jar

scp -r /home/jrootham/dev/business/emlogin-site/emlogin/resources \
	jrootham@jrootham.ca:/home/jrootham/servers/emlogin

scp -r /home/jrootham/dev/website/docroot/emlogin \
	jrootham@jrootham.ca:/home/jrootham/http/docroot/

scp /home/jrootham/dev/website/docroot/index.html \
	jrootham@jrootham.ca:/home/jrootham/http/docroot/
