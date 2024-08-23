# CF Identity Token Service
Exchange instance identity to web identity token (JWT)

## How to deploy to Cloud Foundry

```
# Generate RSA key for JWT signing
./gen_rsa_keys.sh

./mvnw clean package -DskipTests
cf push
```

## How to test mTLS locally

```bash
# Generate TLS Certificates
./gen_self_signed_certs.sh

# Generate RSA key for JWT signing
./gen_rsa_keys.sh
```

```bash
./mvnw clean package -DskipTests
```

```bash
java -jar target/cf-identity-token-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=mtls
```


```bash
$ curl -XPOST https://localhost:8443/token --cacert src/main/resources/self-signed/ca.crt --key src/main/resources/self-signed/client.key --cert src/main/resources/self-signed/client.crt   
eyJraWQiOiJmaXJzdCIsInR5cCI6IkpXVCIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiI0Yjg0NzkzYy1mM2VhLTRhNTUtOTJiNy05NDI3MjZhYWMxNjM6Njc1NWIxOWQtYzU0My00ZTBjLWE0YjMtY2Q2ZTdjOWM2OGEzOjAyNzU2MTkxLWQ4NjktNDgwNi05NzE3LWE2ZWVjNTE0MmU4YSIsImF1ZCI6InN0cy5hbWF6b25hd3MuY29tIiwiYXBwX2d1aWQiOiIwMjc1NjE5MS1kODY5LTQ4MDYtOTcxNy1hNmVlYzUxNDJlOGEiLCJvcmdfZ3VpZCI6IjRiODQ3OTNjLWYzZWEtNGE1NS05MmI3LTk0MjcyNmFhYzE2MyIsImlzcyI6Imh0dHBzOi8vbG9jYWxob3N0Ojg0NDMiLCJleHAiOjE3MjQ0MzAwODcsInNwYWNlX2d1aWQiOiI2NzU1YjE5ZC1jNTQzLTRlMGMtYTRiMy1jZDZlN2M5YzY4YTMiLCJpYXQiOjE3MjQzODY4ODd9.MDvgofP3-NmvJKGn7TuHvdHQJmcQEexC4NEmwMPQNss1gyfoOwcXvUne7LPfSr8OHPc0QSX9L1i6r9nHOa-E9czWGbLYyDldXC_aIoPSOupypRFG2frprBYTDmHS5fooyRjzLf_2e4j6Qlwac8UNqRVEfVyPWH2uxrIK1VStaiP7NvW-q03AL11IFYK1g_S0hW9yWkG03hpbPuwl-kpQUC6T40MD4B4oORaDMwWvM53X3v5gnNyJ2A3N3inhSy2Wkkw5i7HXLXfxJ5HTl26EE0pEKVRswD-d14fP5yGUQjrfG57cePbdX3PvKCV2BtmDRbw9vqd9wxwYM6ZAPLFPUA%
```

```
$ echo eyJzdWIiOiI0Yjg0NzkzYy1mM2VhLTRhNTUtOTJiNy05NDI3MjZhYWMxNjM6Njc1NWIxOWQtYzU0My00ZTBjLWE0YjMtY2Q2ZTdjOWM2OGEzOjAyNzU2MTkxLWQ4NjktNDgwNi05NzE3LWE2ZWVjNTE0MmU4YSIsImF1ZCI6InN0cy5hbWF6b25hd3MuY29tIiwiYXBwX2d1aWQiOiIwMjc1NjE5MS1kODY5LTQ4MDYtOTcxNy1hNmVlYzUxNDJlOGEiLCJvcmdfZ3VpZCI6IjRiODQ3OTNjLWYzZWEtNGE1NS05MmI3LTk0MjcyNmFhYzE2MyIsImlzcyI6Imh0dHBzOi8vbG9jYWxob3N0Ojg0NDMiLCJleHAiOjE3MjQ0MzAwODcsInNwYWNlX2d1aWQiOiI2NzU1YjE5ZC1jNTQzLTRlMGMtYTRiMy1jZDZlN2M5YzY4YTMiLCJpYXQiOjE3MjQzODY4ODd9 | base64 -d | jq .
{
  "sub": "4b84793c-f3ea-4a55-92b7-942726aac163:6755b19d-c543-4e0c-a4b3-cd6e7c9c68a3:02756191-d869-4806-9717-a6eec5142e8a",
  "aud": "sts.amazonaws.com",
  "app_guid": "02756191-d869-4806-9717-a6eec5142e8a",
  "org_guid": "4b84793c-f3ea-4a55-92b7-942726aac163",
  "iss": "https://localhost:8443",
  "exp": 1724430087,
  "space_guid": "6755b19d-c543-4e0c-a4b3-cd6e7c9c68a3",
  "iat": 1724386887
}
```