#!/bin/bash

# config section - change as needed
PROTOCOL=
HOST=
PORT=
DATASOURCE=

KIE_USER=
KIE_PWD=

# no need to change here
KIE_CRED=$KIE_USER:$KIE_PWD
PROJECT_GAV=com.redhat:signal-default:1.0


for i in {1..3000}; do curl -X POST -H "Content-Type: application/json" -d '{"personName":"pippo"}' -u ${KIE_CRED} http://${HOST}:${PORT}/kie-server/services/rest/server/containers/${PROJECT_GAV}/processes/testSig/instances; done


