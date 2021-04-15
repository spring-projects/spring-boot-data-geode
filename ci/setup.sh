#!/bin/bash -x

# User ID 1001 is "jenkins"
# Group ID 1001 is "jenkins"
# Syntax: `chown -R userId:groupId .`

echo "Logged in as user [$USER] with home directory [$HOME] in the current working directory [$PWD]"
echo "Running with JRE [$JAVA_HOME]"
chown -R 1001:1001 .
mkdir -p /tmp/geode/boot/artifacts-gradle-cache
mkdir -p /tmp/geode/boot/build-gradle-cache
mkdir -p /tmp/geode/boot/docs-gradle-cache
echo "Logging into Docker..."
docker login --username ${DOCKER_HUB_USR} --password ${DOCKER_HUB_PSW}
exit 0
