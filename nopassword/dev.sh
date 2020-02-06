#!/bin/bash

ln -sf /home/jrootham/dev/website/servers/nopassword/src/nopassword/devstuff.clj \
	/home/jrootham/dev/website/servers/nopassword/src/nopassword/stuff.clj

lein uberjar

ln -sf /home/jrootham/dev/website/servers/nopassword/target/uberjar/nopassword-0.1.0-standalone.jar \
	/home/jrootham/dev/website/servers/nopassword.jar
