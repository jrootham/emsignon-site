#!/bin/bash

EMSITE=/home/jrootham/dev/business/emlogin-site
SERVER=jrootham@jrootham.ca:/home/jrootham

cd $EMSITE/html-src
./make-html.sh
cd $EMSITE/emlogin

scp -r /home/jrootham/dev/website/docroot/emlogin $SERVER/http/docroot/

scp /home/jrootham/dev/website/docroot/index.html $SERVER/http/docroot/
