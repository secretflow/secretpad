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

SF_IMAGE=""
DEPLOY_USER="${USER}"

usage="$(basename "$0") [OPTIONS]

OPTIONS:
    -h    [optional] show this help text
    -i    [mandatory] secretflow docker image info
    -u    [optional] the user name of deploying the secretpad and kuscia container, default value: ${USER}

example:
 ./update-sf-components.sh -u ${USER} -i secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/secretflow-lite-anolis8:latest
"

while getopts ':hi:u:' option; do
	case "$option" in
	h)
		echo "$usage"
		exit
		;;
	i)
		SF_IMAGE=$OPTARG
		;;
	u)
		DEPLOY_USER=$OPTARG
		;;
	\?)
		echo -e "invalid option: -$OPTARG" && echo "${usage}"
		exit 1
		;;
	esac
done

if [[ $SF_IMAGE = "" ]]; then
	echo "please use flag '-i' to provide secretflow image info"
	echo "$usage"
	exit 1
fi

SECRETPAD_CONTAINER_NAME="${DEPLOY_USER}-kuscia-secretpad"
SF_COMP_TEMP_DIR="/tmp/sf-cpts"
COMP_LIST_FILE="sf_comp_list.json"
COMP_TRANSLATION_FILE="sf_comp_translation.json"

function parse_sf_image_labels() {
	echo "=> => parse secretflow image labels"
	comp_list=$(docker run --rm "${SF_IMAGE}" secretflow component inspect -a)
	comp_translation=$(docker run --rm "${SF_IMAGE}" secretflow component get_translation)

	if [[ comp_list == "" ]]; then
		echo "=> => => label kuscia.secretflow.comp_list can't be empty in sf image"
		exit 1
	fi

	if [[ comp_translation == "" ]]; then
		echo "=> => => label kuscia.secretflow.translation can't be empty in sf image"
		exit 1
	fi

	if [[ ! -d ${SF_COMP_TEMP_DIR} ]]; then
		mkdir ${SF_COMP_TEMP_DIR}
	fi

	echo "${comp_list}" >"${SF_COMP_TEMP_DIR}/${COMP_LIST_FILE}"
	echo "${comp_translation}" >"${SF_COMP_TEMP_DIR}/${COMP_TRANSLATION_FILE}"
	echo "=> => finish parsing secretflow image labels"
}

function post_action() {
	echo "=> => remove temporary directory ${SF_COMP_TEMP_DIR}"
	rm -rf "${SF_COMP_TEMP_DIR}"
}

function update_sf_components() {
	echo "=> update secretflow components of the ${SECRETPAD_CONTAINER_NAME} container"

	parse_sf_image_labels

	echo "=> => replace secretflow components config file"
	docker cp "${SF_COMP_TEMP_DIR}/${COMP_LIST_FILE}" "${SECRETPAD_CONTAINER_NAME}:/app/config/components/secretflow.json" || exit 1
	docker cp "${SF_COMP_TEMP_DIR}/${COMP_TRANSLATION_FILE}" "${SECRETPAD_CONTAINER_NAME}:/app/config/i18n/secretflow.json" || exit 1
	echo "=> => finish replacing secretflow components config file"

	echo "=> => restart container: ${SECRETPAD_CONTAINER_NAME}"
	docker restart "${SECRETPAD_CONTAINER_NAME}" >/dev/null

	post_action
	echo "=> finish updating secretflow components of the ${SECRETPAD_CONTAINER_NAME} container"
}

update_sf_components
