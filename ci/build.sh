#!/bin/bash -x

set -eou pipefail

GRADLE_OPTS="-Duser.name=jenkins -Duser.home=/tmp/jenkins-home -Djava.io.tmpdir=/tmp" \
 ./gradlew --no-daemon --refresh-dependencies --stacktrace clean build
