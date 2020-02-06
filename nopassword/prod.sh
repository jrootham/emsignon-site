#!/bin/bash

ln -sf /home/jrootham/dev/website/servers/nopassword/src/nopassword/devstuff.clj \
	/home/jrootham/dev/website/servers/nopassword/src/nopassword/stuff.clj

lein uberjar

scp /home/jrootham/dev/website/servers/nopassword/target/uberjar/nopassword-0.1.0-standalone.jar \
	jrootham@jrootham.ca:/home/jrootham/servers/nopassword.jar

scp -r /home/jrootham/dev/website/docroot/nopassword \
	jrootham@jrootham.ca:/home/jrootham/http/docroot/
