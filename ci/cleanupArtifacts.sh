#!/bin/bash -x

set -eou pipefail
chown -R 1001:1001 .
rm -Rf ./.gradle
rm -Rf ./.m2
