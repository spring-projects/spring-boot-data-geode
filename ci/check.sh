#!/bin/bash -x

set -eou pipefail

GRADLE_OPTS="-Djava.io.tmpdir=/tmp -Dgradle.user.home=/tmp/geode-build-cache" \
 ./gradlew clean check --no-daemon --refresh-dependencies --stacktrace
