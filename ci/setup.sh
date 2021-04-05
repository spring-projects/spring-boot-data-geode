#!/bin/bash -x

mkdir -p /tmp/jenkins-home
chown -R 1001:1001 .
echo "Logging into Docker..."
docker login --username ${DOCKER_HUB_USR} --password ${DOCKER_HUB_PSW}
exit 0
