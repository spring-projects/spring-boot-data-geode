#!/bin/bash
# Pivotal CloudFoundry CF CLI commands.

cf cups apacheGeodeService -t "gemfire, cloudcache, database, pivotal" -p '{ "locators": [ "10.99.199.24[10334]" ], "urls": { "gfsh": "https://10.99.199.24/gemfire/v1" }, "users": [{ "password": "admin", "roles": [ "cluster_operator" ], "username": "admin" }] }'
cf push boot-pcc-demo -u none --no-start -p target/client-0.0.1-SNAPSHOT.jar -b https://github.com/cloudfoundry/java-buildpack.git
cf bind-service boot-pcc-demo apacheGeodeService
