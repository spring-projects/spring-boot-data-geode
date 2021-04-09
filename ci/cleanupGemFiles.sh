#!/bin/bash -x

rm -Rf `find . -name "BACKUPDEFAULT*"`
rm -Rf `find . -name "ConfigDiskDir*"`
rm -Rf `find . -name "locator*" | grep -v "src" | grep -v "locator-application"`
rm -Rf `find . -name "newDB"`
rm -Rf `find . -name "server" | grep -v "src"`
rm -Rf `find . -name "*.log"`
exit 0
