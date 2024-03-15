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
SECRETPAD_ROOT=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd -P)

echo "root path is: $SECRETPAD_ROOT"
CERT_DIR="$SECRETPAD_ROOT/config/certs"

echo "cert path is: $CERT_DIR"

mkdir -p "$CERT_DIR"
pushd "$CERT_DIR" || exit

DAYS=360
CLIENT=client
SERVER=server
IP=127.0.0.1

#create a PKCS#8 key for client(JAVA native supported)
openssl genpkey -out ${CLIENT}.pem -algorithm RSA -pkeyopt rsa_keygen_bits:2048

#generate the Certificate Signing Request
openssl req -new -key ${CLIENT}.pem -days ${DAYS} -out ${CLIENT}.csr \
	-subj "/CN=kusciaapi"

#sign it with Root CA
openssl x509 -req -in ${CLIENT}.csr \
	-CA ca.crt -CAkey ca.key -out ${CLIENT}.crt -days ${DAYS} -sha256 -CAcreateserial

popd || exit
