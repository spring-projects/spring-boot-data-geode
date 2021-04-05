#!/bin/bash -x

set -eou pipefail

GRADLE_OPTS="-Duser.name=jenkins -Duser.home=/tmp/jenkins-home" \
  ./gradlew deployArtifacts finalizeDeployArtifacts --no-daemon --refresh-dependencies --stacktrace -Psigning.secretKeyRingFile=$SIGNING_KEYRING_FILE -Psigning.keyId=$SPRING_SIGNING_KEYID -Psigning.password=$SIGNING_PASSWORD -PossrhUsername=$OSSRH_USERNAME -PossrhPassword=$OSSRH_PASSWORD -PartifactoryUsername=$ARTIFACTORY_USERNAME -PartifactoryPassword=$ARTIFACTORY_PASSWORD
