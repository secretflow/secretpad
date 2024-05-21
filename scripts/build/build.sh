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
set -e

WITH_FRONTEND_FLAG=$1

if [[ $WITH_FRONTEND_FLAG == "" ]]; then
	WITH_FRONTEND_FLAG=false
fi

if [[ $WITH_FRONTEND_FLAG == true ]]; then
	ROOT=$(cd "$(dirname "${BASH_SOURCE[0]}")/../../" && pwd -P)
	FRONTEND_LATEST_TAG=$(git ls-remote --sort='version:refname' --refs --tags https://github.com/secretflow/secretpad-frontend.git | tail -n1 | sed 's/.*\///')
	WORK_DIR="./tmp/frontend"
	mkdir -p $WORK_DIR
	wget -O $WORK_DIR/frontend.tar https://secretflow-public.oss-cn-hangzhou.aliyuncs.com/secretpad-frontend/"${FRONTEND_LATEST_TAG}".tar
	tar -xvf $WORK_DIR/frontend.tar -C ${WORK_DIR} --strip-components=1
	DIST_DIR="$WORK_DIR/apps/platform/dist"
	TARGET_DIR="${ROOT}/secretpad-web/src/main/resources/static"
	mkdir -p "${TARGET_DIR}"
	cp -rpf $DIST_DIR/* "${TARGET_DIR}"
	rm -rf "$WORK_DIR"
fi

mvn -version
java -version

mvn clean package -DskipTests
