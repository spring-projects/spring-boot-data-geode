#!/bin/bash -x

set -eou pipefail

echo "Logged in as user [$USER] with home directory [$HOME] in the current working directory [$PWD]"

./gradlew deployDocs --no-daemon --stacktrace \
 -PdeployDocsSshKeyPath=$DEPLOY_SSH_KEY \
 -PdeployDocsSshUsername=$SPRING_DOCS_USERNAME
