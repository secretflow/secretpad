#!/bin/bash
#
# Copyright 2023 Ant Group Co., Ltd.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

ROOT=$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd -P)

echo "root path is: " $ROOT

mkdir -p ${ROOT}/test-classes/certs
pushd ${ROOT}/test-classes/certs || exit

DAYS=360
CLIENT=client
SERVER=server
IP=127.0.0.1
subjectAltName="IP:127.0.0.1,DNS:localhost"

#create a PKCS#1 key for root ca
openssl genrsa -out ca.key 2048
openssl req -x509 -new -nodes -key ca.key -subj "/CN=Kuscia" -days 10000 -out ca.crt

#create a PKCS#8 key for server
openssl genpkey -out ${SERVER}.pem -algorithm RSA -pkeyopt rsa_keygen_bits:2048

#generate the Certificate Signing Request
openssl req -new -key ${SERVER}.pem -days ${DAYS} -out ${SERVER}.csr \
    -subj "/CN=kusciaapi"

#sign it with Root CA
openssl x509  -req -in ${SERVER}.csr \
    -extfile <(printf "subjectAltName=${subjectAltName}") \
    -CA ca.crt -CAkey ca.key  \
    -days ${DAYS} -sha256 -CAcreateserial \
    -out ${SERVER}.crt


#create a PKCS#8 key for client(JAVA native supported)
openssl genpkey -out ${CLIENT}.pem -algorithm RSA -pkeyopt rsa_keygen_bits:2048

#generate the Certificate Signing Request
openssl req -new -key ${CLIENT}.pem -days ${DAYS} -out ${CLIENT}.csr \
    -subj "/CN=kusciaapi"

#sign it with Root CA
openssl x509  -req -in ${CLIENT}.csr \
    -extfile <(printf "subjectAltName=${subjectAltName}") \
    -CA ca.crt -CAkey ca.key -out ${CLIENT}.crt -days ${DAYS} -sha256 -CAcreateserial

#generate token file
openssl rand -base64 8 | xargs echo -n >/tmp/token
sha256sum /tmp/token | cut -d' ' -f1 | xargs echo -n >token
rm -rf /tmp/token

popd || exit