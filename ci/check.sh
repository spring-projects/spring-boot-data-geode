#!/bin/bash -x

set -eou pipefail

GRADLE_OPTS="-Duser.name=jenkins -Duser.home=/tmp/jenkins-home -Djava.io.tmpdir=/tmp" \
 ./gradlew clean check --no-daemon --refresh-dependencies --stacktrace
