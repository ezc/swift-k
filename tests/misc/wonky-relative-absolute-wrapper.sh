#!/bin/bash

cd ../language-behaviour

./generate-tc.data

cat tc.data ../sites/tc.data > tmp.tc.data.sites.1

cat tmp.tc.data.sites.1 | sed 's/localhost/wonkyA/' >> tmp.tc.data.sites
cat tmp.tc.data.sites.1 | sed 's/localhost/wonkyB/' >> tmp.tc.data.sites


SITE=wonky/relative-absolute-wrapper.xml

echo testing site configuration: $SITE

export CF=swift.properties.wrongdir-relative-fail
cat $(dirname $(which swift))/../etc/swift.properties | grep --invert-match -E '^execution.retries=' | grep --invert-match -E '^wrapper.invocation.mode='  > $CF
echo execution.retries=0 >> $CF
echo wrapper.invocation.mode=relative >> $CF

export SWIFT_TEST_PARAMS="-sites.file ../sites/${SITE} -tc.file tmp.tc.data.sites -config $CF"

./run 130-fmri

