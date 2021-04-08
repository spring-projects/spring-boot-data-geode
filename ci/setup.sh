#!/bin/bash -x

## User ID 1001 is "jenkins"

echo "Logged in as user [$USER] with home directory [$HOME] in the current working directory [$PWD]"
chown -R 1001:1001 .
mkdir -p /tmp/geode-build-cache
echo "Logging into Docker..."
docker login --username ${DOCKER_HUB_USR} --password ${DOCKER_HUB_PSW}
exit 0
