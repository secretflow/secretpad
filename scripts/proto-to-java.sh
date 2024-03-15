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

set -o errexit
set -o nounset
set -o pipefail

PROTOC=protoc

SECRETPAD_ROOT=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd -P)
echo "${SECRETPAD_ROOT}"

SCRIPT_DIR=${SECRETPAD_ROOT}/scripts
TMP_WORK_DIR=${SECRETPAD_ROOT}/tmp
GRPC_JAVA_ROOT=${TMP_WORK_DIR}/grpc-java
GRPC_JAVA_PLUGIN=${GRPC_JAVA_ROOT}/compiler/build/exe/java_plugin/protoc-gen-grpc-java

#generate protoc-gen-java-grpc plugin
#https://github.com/grpc/grpc-java/tree/master/compiler
function prepare() {
	if [ -e "${GRPC_JAVA_PLUGIN}" ]; then
		echo "protoc-gen-grpc-java plugin already exists"
		return
	fi
	echo "begin to generate protoc-gen-grpc-java plugin"
	rm -rf "${GRPC_JAVA_ROOT}"
	git clone --branch v1.54.1 --depth 1 git@github.com:grpc/grpc-java.git "${GRPC_JAVA_ROOT}"
	pushd "${GRPC_JAVA_ROOT}/compiler" || exit
	../gradlew java_pluginExecutable -PskipAndroid=true
	popd
	echo "generate protoc-gen-grpc-java plugin successfully"
}

# $1: PROTO_DIR
# $2: proto_java_out
function generate_java_code() {
	PROTO_DIR=$1
	for path in "${PROTO_DIR}"/*; do
		[[ -e "${path}" ]] || break
		if [ -d "${path}" ]; then
			generate_java_code "${path}"
		elif [[ ${path} == *.proto ]]; then
			echo "${PROTOC} --proto_path=${PROTO_ROOT_DIR}
                      --plugin=protoc-gen-grpc-java=${GRPC_JAVA_PLUGIN}
                      --grpc-java_out=${PROTO_OUTPUT_PATH}
                      --java_out=${PROTO_OUTPUT_PATH} ${path}"
			${PROTOC} --proto_path="${PROTO_ROOT_DIR}" \
				--plugin=protoc-gen-grpc-java="${GRPC_JAVA_PLUGIN}" \
				--grpc-java_out="${PROTO_OUTPUT_PATH}" \
				--java_out="${PROTO_OUTPUT_PATH}" "${path}"
		fi
	done
}

function generate_kusciaapi_code() {
	PROTO_ROOT_DIR=$1
	PROTO_DIR="$1/kuscia"
	PROTO_OUTPUT_PATH=${SECRETPAD_ROOT}/secretpad-api/client-java-kusciaapi/src/main/java
	pushd "$PROTO_ROOT_DIR" || exit
	generate_java_code "$PROTO_DIR"
	popd
}

function generate_sf_code() {
	PROTO_ROOT_DIR=$1
	PROTO_DIR="$1/secretflow"
	PROTO_OUTPUT_PATH=${SECRETPAD_ROOT}/secretpad-service/src/main/java
	pushd "$PROTO_ROOT_DIR" || exit
	generate_java_code "$PROTO_DIR"
	popd
}

function main() {
	prepare
	generate_kusciaapi_code "${SECRETPAD_ROOT}/proto"
	generate_sf_code "${SECRETPAD_ROOT}/proto"
}

main
