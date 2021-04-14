#!/bin/bash -x

set -eou pipefail

echo "Deploying docs on host [$HOSTNAME]"

# User ID 1001 is "jenkins"
# Group ID 1001 is "jenkins"
# Syntax: `chown -R userId:groupId .`
chown -R 1001:1001 .

GRADLE_OPTS="-Duser.name=jenkins -Djava.io.tmpdir=/tmp -Dgradle.user.home=/tmp/geode/boot/docs-gradle-cache" \
  ./gradlew deployDocs --no-daemon --stacktrace \
    -PdeployDocsSshKeyPath=$DEPLOY_SSH_KEY \
    -PdeployDocsSshUsername=$SPRING_DOCS_USERNAME
