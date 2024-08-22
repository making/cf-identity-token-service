#!/bin/bash

# Create CA certificate
openssl req -new -nodes -out ca.csr -keyout ca.key -subj "/CN=@making/O=LOL.MAKI/C=JP"
chmod og-rwx ca.key

openssl x509 -req -in ca.csr -days 3650 -signkey ca.key -out ca.crt

# Create Server certificate signed by CA
openssl req -new -nodes -out server.csr -keyout server.key -subj "/OU=app:02756191-d869-4806-9717-a6eec5142e8a/OU=space:6755b19d-c543-4e0c-a4b3-cd6e7c9c68a3/OU=organization:4b84793c-f3ea-4a55-92b7-942726aac163/CN=5b713474-27b5-435c-42b6-1f17"
chmod og-rwx server.key

openssl x509 -req -in server.csr -days 3650 -CA ca.crt -CAkey ca.key -CAcreateserial -out server.crt

rm -f *.csr *.srl