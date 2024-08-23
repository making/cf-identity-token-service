# CF Identity Token Service
Exchange instance identity to web identity token (JWT)

By using this service, when an app on Cloud Foundry accesses a cloud service such as AWS, it can authenticate using a JWT that identifies the app itself, without having to pass an access key/secret key.

## How to deploy to Cloud Foundry

```bash
# Generate RSA key for JWT signing
./gen_rsa_keys.sh

./mvnw clean package -DskipTests
cf push
```

Let's go inside the container and issue a token.

```bash
cf ssh cits
```

In the container

```bash
CITS_DOMAIN=$(echo $VCAP_APPLICATION  | jq -r .application_uris[0])

curl -XPOST https://${CITS_DOMAIN}/token --cert ${CF_INSTANCE_CERT} --key ${CF_INSTANCE_KEY}
# eyJra...
```

Note that the token service is accessed via the Go Router, not via localhost. The router performs TLS verification and sets the validated certificate in `X-Forwarded-Client-Cert` header.

## How to register CF Identity Token Service to AWS IAM as an OIDC Provider

Obtain certificate thumbprint

https://docs.aws.amazon.com/IAM/latest/UserGuide/id_roles_providers_create_oidc_verify-thumbprint.html#oidc-obtain-thumbprint

```bash
# CITS_DOMAIN=cits.<apps_domain>
CITS_DOMAIN=$(cf curl /v3/apps/$(cf app cits --guid)/routes | jq -r '.resources[0].url')
FINGERPRINT=$(openssl s_client -servername $CITS_DOMAIN -showcerts -connect $CITS_DOMAIN:443 </dev/null 2>/dev/null | openssl x509 -fingerprint -sha1 -noout | sed 's/sha1 Fingerprint=//' | sed 's/://g')
```

Create an OIDC Provider

https://docs.aws.amazon.com/IAM/latest/UserGuide/iam_example_iam_CreateOpenIdConnectProvider_section.html

```bash
cat <<EOF > oidc-provider.json
{
    "Url": "https://$CITS_DOMAIN",
    "ClientIDList": [
        "sts.amazonaws.com"
    ],
    "ThumbprintList": [
        "$FINGERPRINT"
    ]
}
EOF

aws iam create-open-id-connect-provider --cli-input-json file://oidc-provider.json
```

```bash
OIDC_PROVIDER_ARN=$(aws iam list-open-id-connect-providers --query 'OpenIDConnectProviderList[?ends_with(Arn, `cits.apps.sandbox.aws.maki.lol`)].Arn' --output text)
```

## Create a sample IAM Role

As an example, let's give the cf app in the current space access to a dynamo table.

```bash
# current org/space name
ORG_NAME=$(cat ~/.cf/config.json | jq -r .OrganizationFields.Name)
SPACE_NAME=$(cat ~/.cf/config.json | jq -r .SpaceFields.Name)

ORG_GUID=$(cf org $ORG_NAME --guid)
SPACE_GUID=$(cf space $SPACE_NAME --guid)
```

```bash
cat << EOF > cf-${ORG_NAME}-${SPACE_NAME}-trust-policy.json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Principal": {
                "Federated": "${OIDC_PROVIDER_ARN}"
            },
            "Action": "sts:AssumeRoleWithWebIdentity",
            "Condition": {
                "StringLike": {
                    "${CITS_DOMAIN}:sub": "${ORG_GUID}:${SPACE_GUID}:*",
                    "${CITS_DOMAIN}:aud": "sts.amazonaws.com"
                }
            }
        }
    ]
}
EOF

aws iam create-role --role-name cf-${ORG_NAME}-${SPACE_NAME} --assume-role-policy-document file://cf-${ORG_NAME}-${SPACE_NAME}-trust-policy.json
```

```bash
export AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
export AWS_REGION=ap-northeast-1

cat <<EOF > cf-${ORG_NAME}-${SPACE_NAME}-policy-dynamo.json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "PrefixFullAccess",
            "Effect": "Allow",
            "Action": "dynamodb:*",
            "Resource": "arn:aws:dynamodb:${AWS_REGION}:${AWS_ACCOUNT_ID}:table/${ORG_NAME}-${SPACE_NAME}-*"
        }
    ]
}
EOF

aws iam put-role-policy --role-name cf-${ORG_NAME}-${SPACE_NAME} --policy-name dynamo-prefix-full-access-${ORG_NAME}-${SPACE_NAME} --policy-document file://cf-${ORG_NAME}-${SPACE_NAME}-policy-dynamo.json
```

Login to a cf container with aws CLI to try to assume role

```bash
# copy the result of `aws iam get-role --role-name cf-${ORG_NAME}-${SPACE_NAME} --query 'Role.Arn' --output text`
cf push aws-cli -m 128m -o public.ecr.aws/aws-cli/aws-cli --no-route -u process --no-manifest -c 'sleep infinity'
cf ssh aws-cli
```

```bash
export PATH=$PATH:/usr/local/bin
CITS_DOMAIN=cits.<apps_domain> # changeme
curl -s -XPOST https://${CITS_DOMAIN}/token --cert ${CF_INSTANCE_CERT} --key ${CF_INSTANCE_KEY} > /tmp/token

export AWS_REGION=ap-northeast-1
export AWS_WEB_IDENTITY_TOKEN_FILE=/tmp/token
export AWS_ROLE_ARN=arn:aws:iam::****:role/cf-*** # changeme (paste the copied arn above)
export AWS_ROLE_SESSION_NAME=cf-demo

aws sts get-caller-identity
```

Create a dynamo table with the prefix `<org_name>-<space_name>-`

```bash
TABLENAME=<org_name>-<space_name>-movie # changeme
aws dynamodb create-table \
    --table-name ${TABLENAME} \
    --attribute-definitions \
        AttributeName=movieId,AttributeType=S \
        AttributeName=title,AttributeType=S \
        AttributeName=genre,AttributeType=S \
    --key-schema \
        AttributeName=movieId,KeyType=HASH \
    --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 \
    --global-secondary-indexes \
        '[
            {
                "IndexName": "title-index",
                "KeySchema": [{"AttributeName":"title","KeyType":"HASH"}],
                "Projection": {"ProjectionType":"ALL"},
                "ProvisionedThroughput": {"ReadCapacityUnits": 5, "WriteCapacityUnits": 5}
            },
            {
                "IndexName": "genre-index",
                "KeySchema": [{"AttributeName":"genre","KeyType":"HASH"}],
                "Projection": {"ProjectionType":"ALL"},
                "ProvisionedThroughput": {"ReadCapacityUnits": 5, "WriteCapacityUnits": 5}
            }
        ]'

# wait until the table is created

aws dynamodb put-item \
    --table-name ${TABLENAME} \
    --item \
        '{
            "movieId": {"S": "1e7b56f3-0c65-4fa6-9a32-6d0a65fbb3a5"},
            "title": {"S": "Inception"},
            "releaseYear": {"N": "2010"},
            "genre": {"S": "Science Fiction"},
            "rating": {"N": "8.8"},
            "director": {"S": "Christopher Nolan"}
        }'

aws dynamodb put-item \
    --table-name ${TABLENAME} \
    --item \
        '{
            "movieId": {"S": "2a4b6d72-789b-4a1a-9c7f-74e5a8f7676d"},
            "title": {"S": "The Matrix"},
            "releaseYear": {"N": "1999"},
            "genre": {"S": "Action"},
            "rating": {"N": "8.7"},
            "director": {"S": "The Wachowskis"}
        }'

aws dynamodb put-item \
    --table-name ${TABLENAME} \
    --item \
        '{
            "movieId": {"S": "3f6c8f74-2e6a-48e9-a07f-034f8a67b9e6"},
            "title": {"S": "Interstellar"},
            "releaseYear": {"N": "2014"},
            "genre": {"S": "Adventure"},
            "rating": {"N": "8.6"},
            "director": {"S": "Christopher Nolan"}
        }'

aws dynamodb scan --table-name ${TABLENAME}
```

You do not have access to table without prefix

```
bash-4.2# aws dynamodb scan --table-name movie
An error occurred (AccessDeniedException) when calling the Scan operation: User: arn:aws:sts::****:assumed-role/cf-****-****/cf-demo is not authorized to perform: dynamodb:Scan on resource: arn:aws:dynamodb:ap-northeast-1:****:table/movie because no identity-based policy allows the dynamodb:Scan action
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