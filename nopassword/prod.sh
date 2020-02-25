#!/bin/bash

ln -sf /home/jrootham/dev/business/npswd-site/nopassword/src/nopassword/prodstuff.clj \
	/home/jrootham/dev/business/npswd-site/nopassword/src/nopassword/stuff.clj

lein uberjar

scp /home/jrootham/dev/business/npswd-site/nopassword/target/uberjar/nopassword-0.1.0-standalone.jar \
	jrootham@jrootham.ca:/home/jrootham/servers/nopassword/nopassword.jar

scp -r /home/jrootham/dev/business/npswd-site/nopassword/resources \
	jrootham@jrootham.ca:/home/jrootham/servers/nopassword

scp -r /home/jrootham/dev/website/docroot/nopassword \
	jrootham@jrootham.ca:/home/jrootham/http/docroot/

scp /home/jrootham/dev/website/docroot/index.html \
	jrootham@jrootham.ca:/home/jrootham/http/docroot/
