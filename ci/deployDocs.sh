#!/bin/bash -x

set -eou pipefail

echo "Deploying docs on host [$HOSTNAME]"

# User ID 1001 is "jenkins"
# Group ID 1001 is "jenkins"
# Syntax: `chown -R userId:groupId .`
chown -R 1001:1001 .

GRADLE_OPTS="--add-opens java.base/java.io=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED -Duser.name=jenkins -Duser.home=/tmp/jenkins-home -Djava.io.tmpdir=/tmp" \
  ./gradlew deployDocs --no-daemon --stacktrace \
    -PdeployDocsSshKeyPath=$DEPLOY_SSH_KEY \
    -PdeployDocsSshUsername=$SPRING_DOCS_USERNAME
