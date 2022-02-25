#!/bin/bash -x

rm -Rf ./.gradle
rm -Rf ./.m2
rm -Rf `find . -name "build" | grep -v "src"`
rm -Rf `find . -name "target" | grep -v "src"`
exit 0
