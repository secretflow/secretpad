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

SecretPadAddress="SecretPad"
Password=$1
OutPath=$2
usage="$(basename "$0") password"

if [[ ${Password} == "" ]]; then
	echo "missing argument: $usage"
	exit 1
fi
# generate jks
set +e

if [ -e "${OutPath}"/server.jks ]; then
	keytool -delete -alias secretpad-server -keystore "${OutPath}"/server.jks \
		-keypass "${Password}" -storepass "${Password}" -dname "OU=SF, O=ANT, L=Shanghai, ST=Shanghai, C=CN, CN=${SecretPadAddress}"
fi

keytool -genkey -keystore "${OutPath}"/server.jks -keyalg RSA -keysize 2048 -validity 3650 \
	-keypass "${Password}" -storepass "${Password}" -dname "OU=SF, O=ANT, L=Shanghai, ST=Shanghai, C=CN, CN=${SecretPadAddress}" -alias secretpad-server
set -e
