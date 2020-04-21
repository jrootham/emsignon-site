#!/bin/bash

#  Create all the .html and .css files

cp emlogin.css ../docroot

cat head.html nav.html index.html tail.html > ../docroot/index.html
cat head.html nav.html background.html tail.html > ../docroot/background.html
cat head.html nav.html policy.html tail.html > ../docroot/policy.html
cat head.html nav.html privacy.html tail.html > ../docroot/privacy.html
