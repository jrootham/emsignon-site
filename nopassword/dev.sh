#!/bin/bash

ln -sf /home/jrootham/dev/business/npswd-site/nopassword/src/nopassword/devstuff.clj \
	/home/jrootham/dev/business/npswd-site/nopassword/src/nopassword/stuff.clj

lein uberjar

ln -sf /home/jrootham/dev/business/npswd-site/nopassword/target/uberjar/nopassword-0.1.0-standalone.jar \
	/home/jrootham/dev/website/servers/nopassword/nopassword.jar

