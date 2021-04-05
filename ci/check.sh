#!/bin/bash -x

set -eou pipefail

GRADLE_OPTS="-Duser.name=jenkins -Duser.home=/tmp/jenkins-home" \
 ./gradlew --no-daemon --refresh-dependencies --stacktrace clean check
