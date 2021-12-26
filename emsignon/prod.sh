#!/bin/bash

EMSITE=/home/jrootham/dev/business/emlogin-site
SERVER=jrootham@jrootham.ca:/home/jrootham

ln -sf $EMSITE/emlogin/src/emlogin/prodstuff.clj $EMSITE/emlogin/src/emlogin/stuff.clj

lein uberjar

scp $EMSITE/emlogin/target/uberjar/emlogin-0.1.0-standalone.jar $SERVER/servers/emlogin/emlogin.jar

scp -r $EMSITE/emlogin/resources $SERVER/servers/emlogin

