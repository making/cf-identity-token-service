#!/bin/bash
set -ex

DIR=$(dirname $0)/src/main/resources/self-signed

# Create CA certificate
openssl req -new -nodes -out ${DIR}/ca.csr -keyout ${DIR}/ca.key -subj "/CN=@making/O=LOL.MAKI/C=JP"
chmod og-rwx ${DIR}/ca.key

cat <<EOF > ${DIR}/ext_ca.txt
basicConstraints=CA:TRUE
keyUsage=digitalSignature,keyCertSign
EOF

openssl x509 -req -in ${DIR}/ca.csr -days 3650 -signkey ${DIR}/ca.key -out ${DIR}/ca.crt -extfile ${DIR}/ext_ca.txt

cat <<EOF > ${DIR}/ext.txt
basicConstraints=CA:FALSE
keyUsage=digitalSignature,dataEncipherment,keyEncipherment,keyAgreement
extendedKeyUsage=serverAuth,clientAuth
EOF

# Create Server certificate signed by CA
openssl req -new -nodes -out ${DIR}/server.csr -keyout ${DIR}/server.key -subj "/CN=localhost"
chmod og-rwx ${DIR}/server.key
openssl x509 -req -in ${DIR}/server.csr -days 3650 -CA ${DIR}/ca.crt -CAkey ${DIR}/ca.key -CAcreateserial -out ${DIR}/server.crt -extfile ${DIR}/ext.txt

# Create Client certificate signed by CA
openssl req -new -nodes -out ${DIR}/client.csr -keyout ${DIR}/client.key -subj "/OU=app:02756191-d869-4806-9717-a6eec5142e8a/OU=space:6755b19d-c543-4e0c-a4b3-cd6e7c9c68a3/OU=organization:4b84793c-f3ea-4a55-92b7-942726aac163/CN=5b713474-27b5-435c-42b6-1f17"
chmod og-rwx ${DIR}/client.key
openssl x509 -req -in ${DIR}/client.csr -days 3650 -CA ${DIR}/ca.crt -CAkey ${DIR}/ca.key -CAcreateserial -out ${DIR}/client.crt -extfile ${DIR}/ext.txt

rm -f ${DIR}/*.csr ${DIR}/*.srl ${DIR}/ext*.txt