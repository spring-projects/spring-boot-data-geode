#!/bin/bash -x

set -eou pipefail

GRADLE_OPTS="-Duser.name=jenkins -Duser.home=/opt/jenkins -Djava.io.tmpdir=/tmp" \
 ./gradlew -Pjenkins=true clean check --no-daemon --refresh-dependencies --stacktrace
