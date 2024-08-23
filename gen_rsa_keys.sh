#!/bin/bash
set -ex
DIR=$(dirname $0)/src/main/resources

openssl genrsa -out ${DIR}/private.pem 2048
openssl rsa -in ${DIR}/private.pem -outform PEM -pubout -out ${DIR}/public.pem
openssl pkcs8 -topk8 -inform PEM -in ${DIR}/private.pem -out ${DIR}/private_key.pem -nocrypt
rm -f ${DIR}/private.pem
