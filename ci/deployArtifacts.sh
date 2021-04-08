#!/bin/bash -x

set -eou pipefail

echo "Logged in as user [$USER] with home directory [$HOME] in the current working directory [$PWD]"

./gradlew deployArtifacts finalizeDeployArtifacts --no-build-cache- --no-configuration-cache --no-daemon --stacktrace \
 -PartifactoryUsername=$ARTIFACTORY_USERNAME \
 -PartifactoryPassword=$ARTIFACTORY_PASSWORD \
 -PossrhUsername=$OSSRH_USERNAME \
 -PossrhPassword=$OSSRH_PASSWORD \
 -Psigning.keyId=$SPRING_SIGNING_KEYID \
 -Psigning.password=$SIGNING_PASSWORD \
 -Psigning.secretKeyRingFile=$SIGNING_KEYRING_FILE
