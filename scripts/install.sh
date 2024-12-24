#!/bin/bash
#
# Copyright 2024 Ant Group Co., Ltd.
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

export KUSCIA_IMAGE="secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/kuscia:0.13.0b0"
export SECRETPAD_IMAGE="secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/secretpad:0.12.0b0"
export SECRETFLOW_IMAGE="secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/secretflow-lite-anolis8:1.11.0b1"
export SECRETFLOW_SERVING_IMAGE="secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/serving-anolis8:0.8.0b0"
export TEE_APP_IMAGE="secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/teeapps-sim-ubuntu20.04:0.1.2b0"
export TEE_DM_IMAGE="secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/sf-tee-dm-sim:0.1.0b0"
export CAPSULE_MANAGER_SIM_IMAGE="secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/capsule-manager-sim-ubuntu20.04:v0.1.0b0"
export DATAPROXY_IMAGE="secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/dataproxy:0.3.0b0"
export SCQL_IMAGE="secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/scql:0.9.2b1"

# MPC TEE ALL-IN-ONE
export DEPLOY_MODE="ALL-IN-ONE"
export KUSCIA_PROTOCOL="tls"

set -e
start_time=$(date +%s)
I_PATH="$(cd "$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")" && pwd)"
RED='\033[0;31m'

usage() {
	echo "$(basename "$0") DEPLOY_MODE [OPTIONS]
NETWORK_MODE:
    master            deploy master (default)
    lite              deploy lite node
    autonomy          deploy autonomy  node include secretpad,kuscia
    autonomy-node     deploy autonomy-node  only kuscia autonomy

lite OPTIONS:
    -m              [optional]  (Only used in lite mode)The master endpoint.
    -n              [optional]  Domain id to be deployed.
    -s              [optional]  The port exposed by secretpad-web, default 8080
    -p              [optional]  The port exposed by kuscia-gateway, default 18080
    -k              [optional]  The port exposed by kuscia-api-http, default 18082
    -g              [optional]  The port exposed by kuscia-api-grpc, default 18083
    -t              [optional]  (Only used in lite mode)The deploy token, get this token from secretpad platform.
    -d              [optional]  The install directory. Default is ${INSTALL_DIR}.
    -P              [optional]  kuscia protocol. Default is ${KUSCIA_PROTOCOL}.
    -q              [optional]  (Only used in autonomy or lite mode)The port exposed for internal use by domain. You can set Env 'DOMAIN_HOST_INTERNAL_PORT' instead default 13081.
    -b              [optional]  The port used to debug,default 5005
    -x              [optional]  kuscia METRICS_PORT default 13084
    -h              [optional]  Show this help text.

example:
    install.sh master
    install.sh lite -n alice -m 'https://root-kuscia-master:1080' -t xdeploy-tokenx -p 18080  -k 18082 -g 18083 -s 8080 -q 13081 -x 13084 -P notls
    install.sh autonomy -n alice -s 8080 -g 18083 -k 18082 -p 18080 -q 13081 -P mtls
    install.sh autonomy-node -n alice -g 18083 -k 18082 -p 18080 -q 13081 -P mtls  -m 'https://secretpad:8080' -t xdeploy-tokenx
    "
}
case "${1}" in
master | lite)
	export MODE=$1
	shift
	;;
autonomy)
	export MODE=$1
	export DEPLOY_MODE="MPC"
	shift
	;;
autonomy-node)
	export MODE=$1
	export DEPLOY_MODE="MPC"
	shift
	;;
-h)
	usage
	exit
	;;
*)
	echo "deploy_mode is invalid, must be autonomy, lite,  master(default)"
	usage
	exit 1
	;;
esac

while getopts 'm:n:s:p:k:g:t:d:P:q:b:x:h' option; do
	case "$option" in
	m)
		export KUSCIA_MASTER_ENDPOINT=$OPTARG
		export SECRETPAD_MASTER_ENDPOINT=$OPTARG
		;;
	n)
		export NODE_ID=$OPTARG
		;;
	s)
		export PAD_PORT=$OPTARG
		;;
	p)
		export KUSCIA_GATEWAY_PORT=$OPTARG
		;;
	k)
		export KUSCIA_API_HTTP_PORT=$OPTARG
		;;
	g)
		export KUSCIA_API_GRPC_PORT=$OPTARG
		;;
	t)
		export KUSCIA_TOKEN=$OPTARG
		;;
	d)
		export INSTALL_DIR=$OPTARG
		;;
	P)
		export KUSCIA_PROTOCOL=$OPTARG
		;;
	q)
		export DOMAIN_HOST_INTERNAL_PORT=$OPTARG
		;;
	b)
		export PAD_DEBUG_PORT=$OPTARG
		;;
	x)
		export METRICS_PORT=$OPTARG
		;;
	h)
		usage
		exit
		;;
	:)
		printf "missing argument for -%s\n" "$OPTARG" >&2
		usage
		exit 1
		;;
	\?)
		printf "illegal option: -%s\n" "$OPTARG" >&2
		usage
		exit 1
		;;
	esac
done
shift $((OPTIND - 1))

architecture=$(uname -m)

case $architecture in
arm*)
	echo "ARM architecture detected"
	export DEPLOY_MODE="MPC"
	echo "ARM architecture does not support scql"
	export SCQL_ENABLE="false"
	;;
aarch64*)
	echo "ARM 64-bit architecture detected"
	export DEPLOY_MODE="MPC"
	echo "ARM architecture does not support scql"
	export SCQL_ENABLE="false"
	;;
x86_64*)
	echo "AMD64 or Intel 64-bit architecture detected"
	;;
i386 | i486 | i586 | i686)
	echo "x86 32-bit architecture detected"
	;;
*)
	echo "Unknown architecture: $architecture"
	;;
esac

function need_scql() {
	if [ "${SCQL_ENABLE}" = 'true' ]; then
		return 0
	fi
	return 1
}

function check_pad_kuscia_sf_image() {
	if [ -z "${SECRETPAD_IMAGE}" ]; then
		echo -e "${RED}[ERROR]${NC} you need set the SECRETPAD_IMAGE environment variable"
		exit 1
	fi
	if [ -z "${KUSCIA_IMAGE}" ]; then
		echo -e "${RED}[ERROR]${NC} you need set the KUSCIA_IMAGE environment variable"
		exit 1
	fi
	if [ -z "${SECRETFLOW_IMAGE}" ]; then
		echo -e "${RED}[ERROR]${NC} you need set the SECRETFLOW_IMAGE environment variable"
		exit 1
	fi
	if [ -z "${SECRETFLOW_SERVING_IMAGE}" ]; then
		echo -e "${RED}[ERROR]${NC} you need set the SECRETFLOW_SERVING_IMAGE environment variable"
		exit 1
	fi
}
function check_scql_image() {
	if [ -z "${SCQL_IMAGE}" ]; then
		echo -e "${RED}[ERROR]${NC} you need set the SCQL_IMAGE environment variable"
		exit 1
	fi
}

function check_tee_image() {
	if [ -z "${SECRETPAD_IMAGE}" ]; then
		echo -e "${RED}[ERROR]${NC} you need set the SECRETPAD_IMAGE environment variable"
		exit 1
	fi
	if [ -z "${KUSCIA_IMAGE}" ]; then
		echo -e "${RED}[ERROR]${NC} you need set the KUSCIA_IMAGE environment variable"
		exit 1
	fi
	if [ -z "${TEE_APP_IMAGE}" ]; then
		echo -e "${RED}[ERROR]${NC} you need set the TEE_APP_IMAGE environment variable"
		exit 1
	fi
	if [ -z "${TEE_DM_IMAGE}" ]; then
		echo -e "${RED}[ERROR]${NC} you need set the TEE_DM_IMAGE environment variable"
		exit 1
	fi
	if [ -z "${CAPSULE_MANAGER_SIM_IMAGE}" ]; then
		echo -e "${RED}[ERROR]${NC} you need set the CAPSULE_MANAGER_SIM_IMAGE environment variable"
		exit 1
	fi
}

function show_all_images() {
	echo "SECRETPAD_IMAGE           ${SECRETPAD_IMAGE}"
	echo "KUSCIA_IMAGE              ${KUSCIA_IMAGE}"
	echo "SECRETFLOW_IMAGE          ${SECRETFLOW_IMAGE}"
	echo "SECRETFLOW_SERVING_IMAGE  ${SECRETFLOW_SERVING_IMAGE}"
	echo "TEE_APP_IMAGE             ${TEE_APP_IMAGE}"
	echo "TEE_DM_IMAGE              ${TEE_DM_IMAGE}"
	echo "CAPSULE_MANAGER_SIM_IMAGE ${CAPSULE_MANAGER_SIM_IMAGE}"
	echo "DATAPROXY_IMAGE           ${DATAPROXY_IMAGE}"
	echo "SCQL_IMAGE                ${SCQL_IMAGE}"
}

function show_mpc_images() {
	echo "SECRETPAD_IMAGE           ${SECRETPAD_IMAGE}"
	echo "KUSCIA_IMAGE              ${KUSCIA_IMAGE}"
	echo "SECRETFLOW_IMAGE          ${SECRETFLOW_IMAGE}"
	echo "SECRETFLOW_SERVING_IMAGE  ${SECRETFLOW_SERVING_IMAGE}"
	echo "DATAPROXY_IMAGE           ${DATAPROXY_IMAGE}"
}
function show_scql_images() {
	echo "SCQL_IMAGE                ${SCQL_IMAGE}"
}

function show_tee_images() {
	echo "SECRETPAD_IMAGE           ${SECRETPAD_IMAGE}"
	echo "KUSCIA_IMAGE              ${KUSCIA_IMAGE}"
	echo "TEE_APP_IMAGE             ${TEE_APP_IMAGE}"
	echo "TEE_DM_IMAGE              ${TEE_DM_IMAGE}"
	echo "CAPSULE_MANAGER_SIM_IMAGE ${CAPSULE_MANAGER_SIM_IMAGE}"
}

function empty_image_env() {
	export SECRETPAD_IMAGE=""
	export KUSCIA_IMAGE=""
	export SECRETFLOW_IMAGE=""
	export SECRETFLOW_SERVING_IMAGE=""
	export TEE_APP_IMAGE=""
	export TEE_DM_IMAGE=""
	export CAPSULE_MANAGER_SIM_IMAGE=""
	export DATAPROXY_IMAGE=""
	export SCQL_IMAGE=""
}

function init_images_from_files() {
	empty_image_env
	# shellcheck disable=SC2231
	for file in $I_PATH/images/*; do
		if [ -f "$file" ]; then
			echo "$file"
			imageInfo="$(docker load <"$file")"
			echo "echo ${imageInfo}"
			# shellcheck disable=SC2001
			image=$(echo "${imageInfo}" | sed "s/Loaded image: //")
			if [[ $image == *kuscia* ]]; then
				export KUSCIA_IMAGE=$image
			elif [[ $image == *secretpad* ]]; then
				export SECRETPAD_IMAGE=$image
			elif [[ $image == *secretflow-lite* ]]; then
				export SECRETFLOW_IMAGE=$image
			elif [[ $image == *sf-tee-dm-sim* ]]; then
				export TEE_DM_IMAGE=$image
			elif [[ $image == *capsule-manager-sim* ]]; then
				export CAPSULE_MANAGER_SIM_IMAGE=$image
			elif [[ $image == *teeapps-sim* ]]; then
				export TEE_APP_IMAGE=$image
			elif [[ $image == *serving-anolis8* ]]; then
				export SECRETFLOW_SERVING_IMAGE=$image
			elif [[ $image == *dataproxy* ]]; then
				export DATAPROXY_IMAGE=$image
			elif [[ $image == *scql* ]]; then
				export SCQL_IMAGE=$image
			fi
		fi
	done
}

function update_sf_image_name() {
	if [[ "${SECRETFLOW_IMAGE}" != *aliyuncs* && "${SECRETFLOW_IMAGE}" != *docker.io/* ]]; then
		export SECRETFLOW_IMAGE="docker.io/"${SECRETFLOW_IMAGE}
	fi
	if [[ "${SECRETFLOW_SERVING_IMAGE}" != *aliyuncs* && "${SECRETFLOW_SERVING_IMAGE}" != *docker.io/* ]]; then
		export SECRETFLOW_SERVING_IMAGE="docker.io/"${SECRETFLOW_SERVING_IMAGE}
	fi
}
function update_scql_image_name() {
	if [[ "${SCQL_IMAGE}" != *aliyuncs* && "${SCQL_IMAGE}" != *docker.io/* ]]; then
		export SCQL_IMAGE="docker.io/"${SCQL_IMAGE}
	fi
}

function update_tee_image_name() {
	if [[ "${TEE_DM_IMAGE}" != *aliyuncs* && "${TEE_DM_IMAGE}" != *docker.io/* ]]; then
		export TEE_DM_IMAGE="docker.io/"${TEE_DM_IMAGE}
	fi
	if [[ "${CAPSULE_MANAGER_SIM_IMAGE}" != *aliyuncs* && "${CAPSULE_MANAGER_SIM_IMAGE}" != *docker.io/* ]]; then
		export CAPSULE_MANAGER_SIM_IMAGE="docker.io/"${CAPSULE_MANAGER_SIM_IMAGE}
	fi
	if [[ "${TEE_APP_IMAGE}" != *aliyuncs* && "${TEE_APP_IMAGE}" != *docker.io/* ]]; then
		export TEE_APP_IMAGE="docker.io/"${TEE_APP_IMAGE}
	fi
}

function load_docker_images() {
	if [ -d "$I_PATH/images" ]; then
		echo "load docker image by local images $I_PATH/images"
		init_images_from_files
	else
		echo "load docker image by environment variable"
		if [ "${DEPLOY_MODE}" = 'ALL-IN-ONE' ]; then
			docker pull "${SECRETPAD_IMAGE}"
			docker pull "${KUSCIA_IMAGE}"
			docker pull "${SECRETFLOW_IMAGE}"
			docker pull "${SECRETFLOW_SERVING_IMAGE}"
			docker pull "${TEE_APP_IMAGE}"
			docker pull "${TEE_DM_IMAGE}"
			docker pull "${CAPSULE_MANAGER_SIM_IMAGE}"
			docker pull "${DATAPROXY_IMAGE}"
			if need_scql; then
				docker pull "${SCQL_IMAGE}"
			fi
		fi
		if [ "${DEPLOY_MODE}" = 'MPC' ]; then
			docker pull "${SECRETPAD_IMAGE}"
			docker pull "${KUSCIA_IMAGE}"
			docker pull "${SECRETFLOW_IMAGE}"
			docker pull "${SECRETFLOW_SERVING_IMAGE}"
			docker pull "${DATAPROXY_IMAGE}"
			if need_scql; then
				docker pull "${SCQL_IMAGE}"
			fi
		fi
		if [ "${DEPLOY_MODE}" = 'TEE' ]; then
			docker pull "${SECRETPAD_IMAGE}"
			docker pull "${KUSCIA_IMAGE}"
			docker pull "${TEE_APP_IMAGE}"
			docker pull "${TEE_DM_IMAGE}"
			docker pull "${CAPSULE_MANAGER_SIM_IMAGE}"
		fi
	fi
}

function init_deploy_shell() {
	CTR_ROOT=${CTR_ROOT:-"/home/kuscia"}
	rm -rf "$I_PATH/deploy"
	TEMP_CONTAINER=$(docker create "$SECRETPAD_IMAGE")
	docker cp "$TEMP_CONTAINER:/app/scripts/deploy" "$I_PATH/deploy"
	docker rm "$TEMP_CONTAINER"
	docker run --rm "$KUSCIA_IMAGE" cat "${CTR_ROOT}"/scripts/deploy/kuscia.sh >kuscia.sh && chmod u+x "$I_PATH"/kuscia.sh
	docker run --rm "$KUSCIA_IMAGE" cat "${CTR_ROOT}"/scripts/deploy/register_app_image.sh >register_app_image_0.sh && chmod u+x register_app_image_0.sh
}

function check_image_ok() {
	#  MPC TEE ALL-IN-ONE
	if [ "${DEPLOY_MODE}" = 'ALL-IN-ONE' ]; then
		check_pad_kuscia_sf_image
		check_tee_image
		if need_scql; then
			check_scql_image
			update_scql_image_name
		fi
		update_sf_image_name
		update_tee_image_name
		show_all_images
	fi
	if [ "${DEPLOY_MODE}" = 'MPC' ]; then
		check_pad_kuscia_sf_image
		update_sf_image_name
		if need_scql; then
			check_scql_image
			update_scql_image_name
		fi
		show_mpc_images
	fi
	if [ "${DEPLOY_MODE}" = 'TEE' ]; then
		check_tee_image
		update_tee_image_name
		show_tee_images
	fi
}

function check_and_init() {
	load_docker_images
	check_image_ok
	init_deploy_shell
}

check_and_init

source "${I_PATH}/deploy/common/log.sh"
source "${I_PATH}/deploy/common/utils.sh"
source "${I_PATH}/deploy/common/secretpad.env"

function delete_dp_datasource() {
	local delete_dp_datasource_url="http://localhost:8082/api/v1/domaindatasource/delete"
	if ! noTls; then
		delete_dp_datasource_url="https://localhost:8082/api/v1/domaindatasource/delete"
	fi
	sed "s|{{.URL}}|${delete_dp_datasource_url}|g;
    s|{{.NODE_ID}}|${NODE_ID}|g" \
		"$I_PATH"/deploy/common/delete_dp_datasource.sh >delete_dp_datasource-0.sh
	log "delete_dp_datasource"
	docker cp delete_dp_datasource-0.sh "${KUSCIA_CTR}":/home/kuscia
	docker exec -it "${KUSCIA_CTR}" sh delete_dp_datasource-0.sh
	echo
}

function deploy_secretpad() {
	if ! is_p2p_node; then
		bash "$I_PATH"/deploy/secretpad.sh -b "${PAD_DEBUG_PORT}"
	fi
	if is_p2p_node; then
		post_kuscia_node
	fi
}

function build_kuscia_master_endpoint() {
	if is_alice_lite || is_bob_lite || is_tee_lite; then
		# shellcheck disable=SC2155
		local host_ip=$(getIPV4Address)
		host_ip=${USER}-kuscia-master:1080
		if noTls; then
			export KUSCIA_MASTER_ENDPOINT="http://${host_ip}:${KUSCIA_GATEWAY_PORT}"
			export KUSCIA_MASTER_ENDPOINT="http://${host_ip}"
		else
			export KUSCIA_MASTER_ENDPOINT="https://${host_ip}:${KUSCIA_GATEWAY_PORT}"
			export KUSCIA_MASTER_ENDPOINT="https://${host_ip}"
		fi
	fi
}

function dp_enabled() {
	if [ "${DATAPROXY_ENABLE}" = 'true' ]; then
		return 0
	fi
	return 1
}

function deploy_kuscia() {
	build_kuscia_master_endpoint
	init_kuscia_config
	# shellcheck disable=SC2016
	kuscia_cmd="bash $I_PATH/kuscia.sh start  -p ${KUSCIA_GATEWAY_PORT} -k ${KUSCIA_API_HTTP_PORT} -g ${KUSCIA_API_GRPC_PORT} -s ${KUSCIA_K3S_INSTALL_DIR} -d ${KUSCIA_INSTALL_DIR}  -c ${KUSCIA_CONFIG_INSTALL_DIR}/kuscia.yaml -l ${KUSCIA_LOG_PATH} -q ${DOMAIN_HOST_INTERNAL_PORT} -x ${METRICS_PORT} probe_kuscia ${KUSCIA_CTR}"
	if dp_enabled; then
		kuscia_cmd="bash $I_PATH/kuscia.sh start  -p ${KUSCIA_GATEWAY_PORT} -k ${KUSCIA_API_HTTP_PORT} -g ${KUSCIA_API_GRPC_PORT} -s ${KUSCIA_K3S_INSTALL_DIR} -d ${KUSCIA_INSTALL_DIR}  -c ${KUSCIA_CONFIG_INSTALL_DIR}/kuscia.yaml -l ${KUSCIA_LOG_PATH} -q ${DOMAIN_HOST_INTERNAL_PORT} -x ${METRICS_PORT} probe_kuscia ${KUSCIA_CTR} --data-proxy"
	fi
	log "${kuscia_cmd}"
	eval "${kuscia_cmd}"
	if is_lite; then
		if need_tee; then
			bash register_app_image_0.sh -c "$KUSCIA_CTR" -i "$TEE_DM_IMAGE" --import
		fi
		if need_scql; then
			bash register_app_image_0.sh -c "$KUSCIA_CTR" -i "$SCQL_IMAGE" --import
		fi
		if need_mpc; then
			bash register_app_image_0.sh -c "$KUSCIA_CTR" -i "$SECRETFLOW_SERVING_IMAGE" --import
		fi
		delete_dp_datasource
	fi
	if is_p2p; then
		if need_mpc; then
			applySfServingAppImage
			bash register_app_image_0.sh -c "$KUSCIA_CTR" -i "$SECRETFLOW_SERVING_IMAGE" --import
		fi
		if need_scql; then
			applySfScqlAppImage
			bash register_app_image_0.sh -c "$KUSCIA_CTR" -i "$SCQL_IMAGE" --import
		fi
		delete_dp_datasource
	fi
	if is_p2p_node; then
		if need_mpc; then
			applySfServingAppImage
			bash register_app_image_0.sh -c "$KUSCIA_CTR" -i "$SECRETFLOW_SERVING_IMAGE" --import
		fi
		if need_scql; then
			applySfScqlAppImage
			bash register_app_image_0.sh -c "$KUSCIA_CTR" -i "$SCQL_IMAGE" --import
		fi
		delete_dp_datasource
	fi
	if is_master; then
		applySfServingAppImage
		if need_scql; then
			applySfScqlAppImage
		fi
	fi
	log "docker exec -it ${KUSCIA_CTR} sh scripts/deploy/init_kusciaapi_client_certs.sh"
	docker exec -it "${KUSCIA_CTR}" sh scripts/deploy/init_kusciaapi_client_certs.sh
}

function add_kuscia_domain_lite() {
	# shellcheck disable=SC2155
	export KUSCIA_TOKEN=$(docker exec -it "${USER}"-kuscia-master sh scripts/deploy/add_domain_lite.sh "${NODE_ID}")
}

function add_alice_bob_data() {
	docker exec -it "${USER}"-kuscia-master scripts/deploy/create_domaindata_alice_table.sh alice
	docker exec -it "${USER}"-kuscia-master scripts/deploy/create_domaindata_bob_table.sh bob
	docker run --rm "$KUSCIA_IMAGE" cat /home/kuscia/var/storage/data/alice.csv >"$INSTALL_DIR"/"$PAD_MASTER"/"$PAD_DATA"/alice/alice.csv
	docker run --rm "$KUSCIA_IMAGE" cat /home/kuscia/var/storage/data/bob.csv >"${KUSCIA_INSTALL_DIR}"/bob.csv
	docker exec -it "${USER}"-kuscia-lite-alice curl https://127.0.0.1:8070/api/v1/datamesh/domaindatagrant/create -X POST -H 'content-type: application/json' -d '{"author":"alice","domaindata_id":"alice-table","grant_domain":"bob"}' --cacert var/certs/ca.crt --cert var/certs/ca.crt --key var/certs/ca.key
	docker exec -it "${USER}"-kuscia-lite-bob curl https://127.0.0.1:8070/api/v1/datamesh/domaindatagrant/create -X POST -H 'content-type: application/json' -d '{"author":"bob","domaindata_id":"bob-table","grant_domain":"alice"}' --cacert var/certs/ca.crt --cert var/certs/ca.crt --key var/certs/ca.key
}

function del_alice_bob_dp_table() {
	docker exec -it "${USER}"-kuscia-master kubectl delete domaindata alice-dp-table -n alice
	docker exec -it "${USER}"-kuscia-master kubectl delete domaindata bob-dp-table -n bob
}

function deploy_kuscia_lite_alice_bob_tee() {
	if is_master; then
		if need_lite_alice_bob; then
			log 'init kuscia lite alice'
			export MODE='lite'
			export NODE_ID='alice'
			export DOMAIN_HOST_INTERNAL_PORT="23081"
			export METRICS_PORT="23084"
			add_kuscia_domain_lite
			prepare_environment
			deploy_kuscia

			log 'init kuscia lite bob'
			export MODE='lite'
			export NODE_ID='bob'
			export DOMAIN_HOST_INTERNAL_PORT="33081"
			export METRICS_PORT="33084"
			add_kuscia_domain_lite
			prepare_environment
			deploy_kuscia
			create_alice_bob_domain_route
			add_alice_bob_data
			del_alice_bob_dp_table
		fi

		if need_tee; then
			log 'init kuscia lite tee'
			export MODE='lite'
			export NODE_ID='tee'
			export DOMAIN_HOST_INTERNAL_PORT="43081"
			export METRICS_PORT="43084"
			add_kuscia_domain_lite
			prepare_environment
			deploy_kuscia
			bash register_app_image_0.sh -c "$KUSCIA_CTR" -i "$TEE_APP_IMAGE" --import
			bash register_app_image_0.sh -c "$KUSCIA_CTR" -i "$CAPSULE_MANAGER_SIM_IMAGE" --import
			init_tee
		fi

		export MODE='master'
		prepare_environment
	fi
}

prepare_environment
deploy_kuscia
deploy_kuscia_lite_alice_bob_tee
deploy_secretpad
clear_env
end_time=$(date +%s)
# shellcheck disable=SC2004
execution_time=$(($end_time - $start_time))
log "All components started successfully, time spend: $execution_time second"
