#!/bin/bash -x

set -eou pipefail

GRADLE_OPTS="-Duser.name=jenkins -Djava.io.tmpdir=/tmp -Dgradle.user.home=/tmp/geode/boot/build-gradle-cache" \
 ./gradlew clean check --no-daemon --refresh-dependencies --stacktrace
