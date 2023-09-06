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

set -ex

SECRETPAD_ROOT=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd -P)

echo "root path is: $SECRETPAD_ROOT"

function init_kusciaapi_certs() {
  CERT_DIR="$SECRETPAD_ROOT/config/certs"
  echo "cert path is: $CERT_DIR"
  mkdir -p "$CERT_DIR"
  pushd "$CERT_DIR" || exit

  DAYS=360
  CLIENT=client
  SERVER=server
  IP=127.0.0.1

  #create a PKCS#1 key for root ca
  if [ ! -e ca.key ] && [ ! -e ca.crt ]; then
    echo "generate ca.key and ca.crt"
    openssl genrsa -out ca.key 2048
    openssl req -x509 -new -nodes -key ca.key -subj "/CN=Kuscia" -days 10000 -out ca.crt
  fi

  #create a PKCS#8 key for client(JAVA native supported)
  openssl genpkey -out ${CLIENT}.pem -algorithm RSA -pkeyopt rsa_keygen_bits:2048

  #generate the Certificate Signing Request
  openssl req -new -key ${CLIENT}.pem -days ${DAYS} -out ${CLIENT}.csr \
    -subj "/CN=kusciaapi"

  #sign it with Root CA
  openssl x509 -req -in ${CLIENT}.csr \
    -CA ca.crt -CAkey ca.key -out ${CLIENT}.crt -days ${DAYS} -sha256 -CAcreateserial

  #generate token file
  if [ ! -e token ]; then # token not exists
    openssl rand -base64 8 | xargs echo -n >/tmp/token
    sha256sum /tmp/token | cut -d' ' -f1 | xargs echo -n >token
    rm -rf /tmp/token
  fi

  popd || exit
}

echo "start to generate kusciaapi certs"
init_kusciaapi_certs
echo "generate kusciaapi certs successfully"

echo "start to init sqlite"
mkdir -p "${SECRETPAD_ROOT}/db"
SQL_PATH="${SECRETPAD_ROOT}/config/schema/v1.sql"
SQL_PATH_V2="${SECRETPAD_ROOT}/config/schema/v2.sql"
DB_PATH="${SECRETPAD_ROOT}/db/secretpad.sqlite"
if [ -e "${DB_PATH}" ]; then
  echo >"${DB_PATH}"
  sqlite3 "${DB_PATH}" ".read ${SQL_PATH}"
  sqlite3 "${DB_PATH}" ".read ${SQL_PATH_V2}"
fi
echo "init sqlite successfully"
