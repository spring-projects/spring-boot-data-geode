#!/bin/bash -x

set -eou pipefail
mkdir -p /tmp/jenkins-home
chown -R 1001:1001 .

GRADLE_OPTS="-Duser.name=jenkins -Duser.home=/tmp/jenkins-home" \
 ./gradlew --no-daemon --refresh-dependencies --stacktrace clean check
