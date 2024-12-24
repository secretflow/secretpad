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
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd)"
SF_IMAGE=secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/secretflow-lite-anolis8:1.11.0b1


PULL_FLAG=$2
if [[ $1 != "" ]]; then
	SF_IMAGE=$1
fi
echo "update_components: SF_IMAGE  ->  $SF_IMAGE"

if [[ ${PULL_FLAG} == "" ]]; then
	PULL_FLAG=true
	echo "missing argument: PULL_FLAG  use default $PULL_FLAG"
fi
if [[ $PULL_FLAG == true ]]; then
	docker pull "${SF_IMAGE}"
fi

# x86 only arm is not ok
docker run --rm "${SF_IMAGE}" secretflow component inspect -a > "${SCRIPT_DIR}"/../config/components/secretflow.json
echo 'update comp_list'

# x86 only arm is not ok
docker run --rm "${SF_IMAGE}" secretflow component get_translation > "${SCRIPT_DIR}"/../config/i18n/secretflow.json
echo 'update translation'
