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

set -e
DIR="$(cd "$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")" && pwd)"
source "$DIR/log.sh"
source "$DIR/secretpad.env"

function need_start_docker_container() {
	ctr=$1

	if [[ ! "$(docker ps -a -q -f name=^/"${ctr}"$)" ]]; then
		# need start your container
		return 0
	fi

	if $FORCE_START; then
		log "Remove container '${ctr}' ..."
		docker rm -f "$ctr" >/dev/null 2>&1
		# need start your container
		return 0
	fi

	read -rp "$(echo -e "${GREEN}"The container \'"${ctr}"\' already exists. Do you need to recreate it? [y/n]: "${NC}")" yn
	case $yn in
	[Yy]*)
		echo -e "${GREEN}Remove container ${ctr} ...${NC}"
		docker rm -f "$ctr"
		# need start your container
		return 0
		;;
	*)
		return 1
		;;
	esac

	return 1
}

function noTls() {
	if [ "${KUSCIA_PROTOCOL}" = "notls" ]; then
		return 0
	else
		return 1
	fi
}

function do_http_probe() {
	local ctr=$1
	local endpoint=$2
	local max_retry=$3
	local retry=0
	while [ $retry -lt "$max_retry" ]; do
		local status_code
		status_code=$(docker exec -it "$ctr" curl -k --write-out '%{http_code}' --silent --output /dev/null "${endpoint}")
		if [[ $status_code -eq 200 || $status_code -eq 404 || $status_code -eq 401 ]]; then
			return 0
		fi
		sleep 1
		retry=$((retry + 1))
	done
	return 1
}

function probe_kuscia() {
	local kuscia_ctr=$1
	local protocol="http"
	if ! noTls; then
		protocol="https"
	fi
	if ! do_http_probe "$kuscia_ctr" "${protocol}://127.0.0.1:1080" 60; then
		log_error "Probe kuscia in container '$kuscia_ctr' failed. Please check the log" >&2
		exit 1
	fi
}

function probe_secret_pad() {
	local secretpad_ctr=$1
	if ! do_http_probe "$secretpad_ctr" "http://127.0.0.1:8080" 60; then
		log_error "Probe secretpad in container '$secretpad_ctr' failed. Please check the log" >&2
		exit 1
	fi
}

function getIPV4Address() {
	local ipv4=""
	arch=$(uname -s || true)
	case $arch in
	"Linux")
		eth="eth0"
		ipv4=$(ip -4 addr show $eth | grep -oP '(?<=inet\s)\d+(\.\d+){3}') || true
		if [[ $ipv4 == "" ]]; then
			eth=$(ip route | grep default | cut -d" " -f5) || true
			if [[ $eth != "" ]]; then
				ipv4=$(ip -4 addr show "$eth" | grep -oP '(?<=inet\s)\d+(\.\d+){3}') || true
			fi
		fi
		;;
	"Darwin")
		ipv4=$(ipconfig getifaddr en0) || true
		;;
	esac
	echo "$ipv4"
}

function check_rw() {
	path=$1
	if [ -r "${path}" ] && [ -w "${path}" ]; then
		log_success "yon have rw permissions to ${path}"
	else
		log_error "you does not have rw permissions to ${path}"
		exit 1
	fi
}

function check_str() {
	local string=$1
	local msg=$2
	if [ -z "$string" ]; then
		log_error "$msg"
		exit 1
	fi
}

function is_master() {
	if [ "$MODE" = "master" ]; then
		return 0
	elif [ -z "$MODE" ]; then
		export MODE="master"
		return 0
	else
		return 1
	fi
}

function is_lite() {
	if [ "$MODE" = 'lite' ]; then
		return 0
	else
		return 1
	fi
}

function is_p2p() {
	if [ "$MODE" = 'autonomy' ]; then
		return 0
	else
		return 1
	fi
}

function is_p2p_node() {
	if [ "$MODE" = 'autonomy-node' ]; then
		return 0
	else
		return 1
	fi
}

function is_alice_lite() {
	if [ "$NODE_ID" = 'alice' ] && is_lite; then
		return 0
	else
		return 1
	fi
}

function is_bob_lite() {
	if [ "$NODE_ID" = 'bob' ] && is_lite; then
		return 0
	else
		return 1
	fi
}

function is_tee_lite() {
	if [ "$NODE_ID" = 'tee' ] && is_lite; then
		return 0
	else
		return 1
	fi
}

function show_env() {
	log "
    mode:         $MODE     $DEPLOY_MODE
    node:         $NODE_ID
    secretpad     http      port: $PAD_PORT
    kuscia        http      port: $KUSCIA_API_HTTP_PORT
    kuscia        grpc      port: $KUSCIA_API_GRPC_PORT
    kuscia        gateway   port: $KUSCIA_GATEWAY_PORT
    kuscia        protocol: $KUSCIA_PROTOCOL
    secretpad     ctr:      $PAD_CTR
    kuscia        ctr:      $KUSCIA_CTR
    secretpad     dir:      $PAD_VOLUME_PATH
    kuscia        dir:      $KUSCIA_INSTALL_DIR
    k3s           dir:      $KUSCIA_K3S_INSTALL_DIR
    kuscia.yaml   dir:      $KUSCIA_CONFIG_INSTALL_DIR
    "
}

function clear_env() {
	unset MODE
	unset PAD_PORT
	unset PAD_DEBUG_PORT
	unset KUSCIA_API_HTTP_PORT
	unset KUSCIA_API_GRPC_PORT
	unset KUSCIA_GATEWAY_PORT
	unset KUSCIA_MASTER_ENDPOINT
	unset NODE_ID
	unset KUSCIA_TOKEN
	unset INSTALL_DIR
	unset PAD_CTR
	unset KUSCIA_CTR
	unset KUSCIA_INSTALL_DIR
	unset PAD_INSTALL_DIR
	unset KUSCIA_MASTER_ENDPOINT
	unset KUSCIA_TOKEN
	unset SECRETPAD_IMAGE
	unset KUSCIA_IMAGE
	unset SECRETFLOW_IMAGE
	unset SECRETFLOW_SERVING_IMAGE
	unset TEE_APP_IMAGE
	unset TEE_DM_IMAGE
	unset CAPSULE_MANAGER_SIM_IMAGE
	unset PAD_VOLUME_PATH
	unset SPRING_PROFILES_ACTIVE
	unset DOMAIN_HOST_INTERNAL_PORT
	unset KUSCIA_K3S_INSTALL_DIR
	unset KUSCIA_CONFIG_INSTALL_DIR
	unset DATAPROXY_IMAGE
	unset SCQL_IMAGE
}

function prepare_environment() {
	if is_master; then
		export MODE="master"
		export KUSCIA_GATEWAY_PORT=18080
		export KUSCIA_API_HTTP_PORT=18082
		export KUSCIA_API_GRPC_PORT=18083
		export KUSCIA_CTR="$KUSCIA_CTR_PREFIX-$PAD_MASTER"
		export PAD_CTR="$KUSCIA_CTR_PREFIX-$PAD_MASTER-$PAD"
		export KUSCIA_K3S_INSTALL_DIR="$INSTALL_DIR/$PAD_MASTER/$KUSCIA_MASTER_NODE_ID/k3s"
		export KUSCIA_CONFIG_INSTALL_DIR="$INSTALL_DIR/$PAD_MASTER/$KUSCIA_MASTER_NODE_ID"
		export KUSCIA_INSTALL_DIR="$INSTALL_DIR/$PAD_MASTER/$PAD_DATA"
		export PAD_INSTALL_DIR="$INSTALL_DIR/$PAD_MASTER/$PAD_DATA"
		export PAD_VOLUME_PATH="$INSTALL_DIR/$PAD_MASTER/$PAD"
		export KUSCIA_LOG_PATH="$INSTALL_DIR/$PAD_MASTER"
		export NODE_ID="kuscia-system"
		export SPRING_PROFILES_ACTIVE="center"
	fi
	if is_lite; then
		check_str "$KUSCIA_MASTER_ENDPOINT" "missing args -m"
		check_str "$KUSCIA_TOKEN" "missing args -t"
		check_str "$NODE_ID" "missing args -n"
		check_str "$DOMAIN_HOST_INTERNAL_PORT" "missing args -q"
		export KUSCIA_CTR="$KUSCIA_CTR_PREFIX-$PAD_LITE-$NODE_ID"
		export PAD_CTR="$KUSCIA_CTR_PREFIX-$PAD_LITE-$PAD-$NODE_ID"
		export KUSCIA_K3S_INSTALL_DIR="$INSTALL_DIR/$PAD_LITE/$NODE_ID/k3s"
		export KUSCIA_CONFIG_INSTALL_DIR="$INSTALL_DIR/$PAD_LITE/$NODE_ID"
		export KUSCIA_INSTALL_DIR="$INSTALL_DIR/$PAD_LITE/$PAD_DATA/$NODE_ID"
		export PAD_INSTALL_DIR="$INSTALL_DIR/$PAD_LITE/$PAD_DATA"
		export PAD_VOLUME_PATH="$INSTALL_DIR/$PAD_LITE/$PAD"
		export KUSCIA_LOG_PATH="$INSTALL_DIR/$PAD_LITE/$NODE_ID"
		export SPRING_PROFILES_ACTIVE="edge"
		if is_alice_lite; then
			export KUSCIA_GATEWAY_PORT=28080
			export KUSCIA_API_HTTP_PORT=28082
			export KUSCIA_API_GRPC_PORT=28083
			export KUSCIA_K3S_INSTALL_DIR="$INSTALL_DIR/$PAD_MASTER/alice/k3s"
			export KUSCIA_CONFIG_INSTALL_DIR="$INSTALL_DIR/$PAD_MASTER/alice"
			export KUSCIA_INSTALL_DIR="$INSTALL_DIR/$PAD_MASTER/$PAD_DATA/alice"
			export PAD_VOLUME_PATH="$INSTALL_DIR/$PAD_MASTER/$PAD"
			export KUSCIA_LOG_PATH="$INSTALL_DIR/$PAD_MASTER/$NODE_ID"
		fi
		if is_bob_lite; then
			export KUSCIA_GATEWAY_PORT=38080
			export KUSCIA_API_HTTP_PORT=38082
			export KUSCIA_API_GRPC_PORT=38083
			export KUSCIA_K3S_INSTALL_DIR="$INSTALL_DIR/$PAD_MASTER/bob/k3s"
			export KUSCIA_CONFIG_INSTALL_DIR="$INSTALL_DIR/$PAD_MASTER/bob"
			export KUSCIA_INSTALL_DIR="$INSTALL_DIR/$PAD_MASTER/$PAD_DATA/bob"
			export PAD_VOLUME_PATH="$INSTALL_DIR/$PAD_MASTER/$PAD"
			export KUSCIA_LOG_PATH="$INSTALL_DIR/$PAD_MASTER/$NODE_ID"
		fi
		if is_tee_lite; then
			export KUSCIA_GATEWAY_PORT=48080
			export KUSCIA_API_HTTP_PORT=48082
			export KUSCIA_API_GRPC_PORT=48083
			export KUSCIA_K3S_INSTALL_DIR="$INSTALL_DIR/$PAD_MASTER/tee/k3s"
			export KUSCIA_CONFIG_INSTALL_DIR="$INSTALL_DIR/$PAD_MASTER/tee"
			export KUSCIA_INSTALL_DIR="$INSTALL_DIR/$PAD_MASTER/$PAD_DATA/tee"
			export PAD_VOLUME_PATH="$INSTALL_DIR/$PAD_MASTER/$PAD"
			export KUSCIA_LOG_PATH="$INSTALL_DIR/$PAD_MASTER/$NODE_ID"
		fi
	fi
	if is_p2p; then
		check_str "$NODE_ID" "missing args -n"
		check_str "$DOMAIN_HOST_INTERNAL_PORT" "missing args -q"
		check_str "$PAD_PORT" "missing args -s"
		check_str "$KUSCIA_GATEWAY_PORT" "missing args -p"
		check_str "$KUSCIA_API_HTTP_PORT" "missing args -k"
		check_str "$KUSCIA_API_GRPC_PORT" "missing args -g"
		export KUSCIA_CTR="$KUSCIA_CTR_PREFIX-$PAD_AUTONOMY-$NODE_ID"
		export PAD_CTR="$KUSCIA_CTR_PREFIX-$PAD_AUTONOMY-$PAD-$NODE_ID"
		export KUSCIA_K3S_INSTALL_DIR="$INSTALL_DIR/$PAD_AUTONOMY/$NODE_ID/k3s"
		export KUSCIA_CONFIG_INSTALL_DIR="$INSTALL_DIR/$PAD_AUTONOMY/$NODE_ID"
		export KUSCIA_INSTALL_DIR="$INSTALL_DIR/$PAD_AUTONOMY/$PAD_DATA/$NODE_ID"
		export PAD_INSTALL_DIR="$INSTALL_DIR/$PAD_AUTONOMY/$PAD_DATA"
		export PAD_VOLUME_PATH="$INSTALL_DIR/$PAD_AUTONOMY/$PAD"
		export KUSCIA_LOG_PATH="$INSTALL_DIR/$PAD_AUTONOMY/$NODE_ID"
		export SPRING_PROFILES_ACTIVE="p2p"
		export DEPLOY_MODE="MPC"
	fi
	if is_p2p_node; then
		check_str "$NODE_ID" "missing args -n"
		check_str "$DOMAIN_HOST_INTERNAL_PORT" "missing args -q"
		check_str "$SECRETPAD_MASTER_ENDPOINT" "missing args -m"
		check_str "$KUSCIA_TOKEN" "missing args -t"
		check_str "$KUSCIA_GATEWAY_PORT" "missing args -p"
		check_str "$KUSCIA_API_HTTP_PORT" "missing args -k"
		check_str "$KUSCIA_API_GRPC_PORT" "missing args -g"
		export KUSCIA_CTR="$KUSCIA_CTR_PREFIX-$PAD_AUTONOMY-$NODE_ID"
		export PAD_CTR="$KUSCIA_CTR_PREFIX-$PAD_AUTONOMY-$PAD-$NODE_ID"
		export KUSCIA_K3S_INSTALL_DIR="$INSTALL_DIR/$PAD_AUTONOMY/$NODE_ID/k3s"
		export KUSCIA_CONFIG_INSTALL_DIR="$INSTALL_DIR/$PAD_AUTONOMY/$NODE_ID"
		export KUSCIA_INSTALL_DIR="$INSTALL_DIR/$PAD_AUTONOMY/$PAD_DATA/$NODE_ID"
		export PAD_INSTALL_DIR="$INSTALL_DIR/$PAD_AUTONOMY/$PAD_DATA"
		export PAD_VOLUME_PATH="$INSTALL_DIR/$PAD_AUTONOMY/$PAD"
		export KUSCIA_LOG_PATH="$INSTALL_DIR/$PAD_AUTONOMY/$NODE_ID"
		export SPRING_PROFILES_ACTIVE="p2p"
		export DEPLOY_MODE="MPC"
	fi
	show_env
}

function need_tee() {
	if [ "${DEPLOY_MODE}" = "ALL-IN-ONE" ] || [ "${DEPLOY_MODE}" = "TEE" ]; then
		return 0
	else
		return 1
	fi
}

function need_mpc() {
	if [ "${DEPLOY_MODE}" = "ALL-IN-ONE" ] || [ "${DEPLOY_MODE}" = "MPC" ]; then
		return 0
	else
		return 1
	fi
}

function need_lite_alice_bob() {
	if [ "${DEPLOY_MODE}" = "ALL-IN-ONE" ] || [ "${DEPLOY_MODE}" = "MPC" ]; then
		return 0
	else
		return 1
	fi
}

function init_tee() {
	log "start init tee crd"
	# shellcheck disable=SC2046
	docker run --rm --entrypoint /bin/bash -v $(pwd):/tmp/secretpad "$SECRETPAD_IMAGE" -c 'cp -R /app/scripts/templates/tee-image.yaml /tmp/secretpad/'
	# shellcheck disable=SC2046
	docker run --rm --entrypoint /bin/bash -v $(pwd):/tmp/secretpad "$SECRETPAD_IMAGE" -c 'cp -R /app/scripts/templates/tee-capsule-manager.yaml /tmp/secretpad/'
	TEE_NODE_NAME=$(docker exec -it "${USER}"-kuscia-master kubectl get nodes | grep tee | awk '{print $1}')
	sed "s|{{.TEE_NODE_NAME}}|${TEE_NODE_NAME}|g;
  s|{{.TEE_CAPSULE_MANAGER_SIM_IMAGE}}|${CAPSULE_MANAGER_SIM_IMAGE}|g" \
		tee-capsule-manager.yaml >tee-capsule-manager-0.yaml

	TEE_DM_SIM_IMAGE_TAG=${TEE_DM_IMAGE##*:}
	log "TEE_DM_SIM_IMAGE_TAG $TEE_DM_SIM_IMAGE_TAG "
	# shellcheck disable=SC2001
	TEE_DM_SIM_IMAGE_NAME=$(echo "$TEE_DM_IMAGE" | sed "s/:${TEE_DM_SIM_IMAGE_TAG}//")
	log "TEE_DM_SIM_IMAGE_NAME $TEE_DM_SIM_IMAGE_NAME "

	TEEAPPS_SIM_IMAGE_TAG=${TEE_APP_IMAGE##*:}
	log "TEEAPPS_SIM_IMAGE_TAG $TEEAPPS_SIM_IMAGE_TAG "
	# shellcheck disable=SC2001
	TEEAPPS_SIM_IMAGE_NAME=$(echo "$TEE_APP_IMAGE" | sed "s/:${TEEAPPS_SIM_IMAGE_TAG}//")
	log "TEEAPPS_SIM_IMAGE_NAME $TEEAPPS_SIM_IMAGE_NAME "

	sed "s|{{.TEE_DM_SIM_IMAGE_NAME}}|${TEE_DM_SIM_IMAGE_NAME}|g;
  s|{{.TEE_DM_SIM_IMAGE_TAG}}|${TEE_DM_SIM_IMAGE_TAG}|g;
  s|{{.TEEAPPS_SIM_IMAGE_NAME}}|${TEEAPPS_SIM_IMAGE_NAME}|g;
  s|{{.TEEAPPS_SIM_IMAGE_TAG}}|${TEEAPPS_SIM_IMAGE_TAG}|g" \
		tee-image.yaml >tee-image-0.yaml

	log "create tee appImage"
	docker cp tee-image-0.yaml "${KUSCIA_MASTER_CTR}":/home/kuscia
	docker exec -it "${KUSCIA_MASTER_CTR}" kubectl apply -f tee-image-0.yaml

	log "create tee capsule deploy"
	docker cp tee-capsule-manager-0.yaml "${KUSCIA_MASTER_CTR}":/home/kuscia
	docker exec -it "${KUSCIA_MASTER_CTR}" kubectl delete -f tee-capsule-manager-0.yaml >/dev/null 2>&1 || true
	docker exec -it "${KUSCIA_MASTER_CTR}" kubectl apply -f tee-capsule-manager-0.yaml
	local alice=${KUSCIA_CTR_PREFIX}-${PAD_LITE}-alice
	local bob=${KUSCIA_CTR_PREFIX}-${PAD_LITE}-bob
	local tee=${KUSCIA_CTR_PREFIX}-${PAD_LITE}-tee
	local protocol="http"
	if ! noTls; then
		protocol="https"
	fi
	log "create domain route: alice -> tee"
	docker exec -it "${KUSCIA_MASTER_CTR}" sh scripts/deploy/create_cluster_domain_route.sh alice tee ${protocol}://"${tee}":1080
	log "create domain route: tee -> alice"
	docker exec -it "${KUSCIA_MASTER_CTR}" sh scripts/deploy/create_cluster_domain_route.sh tee alice ${protocol}://"${alice}":1080
	log "create domain route: bob -> tee"
	docker exec -it "${KUSCIA_MASTER_CTR}" sh scripts/deploy/create_cluster_domain_route.sh bob tee ${protocol}://"${tee}":1080
	log "create domain route: tee -> bob"
	docker exec -it "${KUSCIA_MASTER_CTR}" sh scripts/deploy/create_cluster_domain_route.sh tee bob ${protocol}://"${bob}":1080
	log "end init tee crd"
}

function applySfServingAppImage() {
	local container_id=${KUSCIA_MASTER_CTR}
	if is_p2p; then
		container_id=${KUSCIA_CTR}
	fi
	if is_p2p_node; then
		container_id=${KUSCIA_CTR}
	fi
	log "apply sf serving appImage"
	# shellcheck disable=SC2046
	docker run --rm --entrypoint /bin/bash -v $(pwd):/tmp/secretpad "$SECRETPAD_IMAGE" -c 'cp -R /app/scripts/templates/sf-serving.yaml /tmp/secretpad/'
	SECRETFLOW_SERVING_IMAGE_TAG=${SECRETFLOW_SERVING_IMAGE##*:}
	log "SECRETFLOW_SERVING_IMAGE_TAG $SECRETFLOW_SERVING_IMAGE_TAG "
	# shellcheck disable=SC2001
	SECRETFLOW_SERVING_IMAGE_NAME=$(echo "$SECRETFLOW_SERVING_IMAGE" | sed "s/:${SECRETFLOW_SERVING_IMAGE_TAG}//")
	log "SECRETFLOW_SERVING_IMAGE_NAME $SECRETFLOW_SERVING_IMAGE_NAME "
	sed "s|{{.SECRETFLOW_SERVING_IMAGE_NAME}}|${SECRETFLOW_SERVING_IMAGE_NAME}|g;
    s|{{.SECRETFLOW_SERVING_IMAGE_TAG}}|${SECRETFLOW_SERVING_IMAGE_TAG}|g" \
		sf-serving.yaml >sf-serving-0.yaml
	log "docker cp sf-serving.yaml  $container_id:/home/kuscia"
	docker cp sf-serving-0.yaml "$container_id":/home/kuscia
	log "docker exec -it $container_id kubectl apply -f sf-serving.yam"
	docker exec -it "$container_id" kubectl apply -f sf-serving-0.yaml
}

function applySfScqlAppImage() {
	if [ "$NEED_SCQL" = false ]; then
		log_warn "ARM architecture does not support scql"
		return
	fi
	local container_id=${KUSCIA_MASTER_CTR}
	if is_p2p; then
		container_id=${KUSCIA_CTR}
	fi
	if is_p2p_node; then
		container_id=${KUSCIA_CTR}
	fi
	log "apply sf scql appImage"
	# shellcheck disable=SC2046
	docker run --rm --entrypoint /bin/bash -v $(pwd):/tmp/secretpad "$SECRETPAD_IMAGE" -c 'cp -R /app/scripts/templates/sf-scql.yaml /tmp/secretpad/'
	SCQL_IMAGE_TAG=${SCQL_IMAGE##*:}
	log "SCQL_IMAGE_TAG $SCQL_IMAGE_TAG "
	# shellcheck disable=SC2001
	SCQL_IMAGE_NAME=$(echo "$SCQL_IMAGE" | sed "s/:${SCQL_IMAGE_TAG}//")
	log "SCQL_IMAGE_NAME $SCQL_IMAGE_NAME "
	sed "s|{{.SCQL_IMAGE_NAME}}|${SCQL_IMAGE_NAME}|g;
    s|{{.SCQL_IMAGE_TAG}}|${SCQL_IMAGE_TAG}|g" \
		sf-scql.yaml >sf-scql-0.yaml
	log "docker cp sf-scql.yaml  $container_id:/home/kuscia"
	docker cp sf-scql-0.yaml "$container_id":/home/kuscia
	log "docker exec -it $container_id kubectl apply -f sf-scql.yam"
	docker exec -it "$container_id" kubectl apply -f sf-scql-0.yaml
}

function create_alice_bob_domain_route() {
	local alice=${KUSCIA_CTR_PREFIX}-${PAD_LITE}-alice
	local bob=${KUSCIA_CTR_PREFIX}-${PAD_LITE}-bob
	local protocol="http"
	if ! noTls; then
		protocol="https"
	fi
	log "create domain route: alice -> bob"
	docker exec -it "${KUSCIA_MASTER_CTR}" sh scripts/deploy/create_cluster_domain_route.sh alice bob ${protocol}://"${bob}":1080
	log "create domain route: bob -> alice"
	docker exec -it "${KUSCIA_MASTER_CTR}" sh scripts/deploy/create_cluster_domain_route.sh bob alice ${protocol}://"${alice}":1080
}

function init_kuscia_config() {
	mkdir -p "${KUSCIA_INSTALL_DIR}"
	mkdir -p "${KUSCIA_K3S_INSTALL_DIR}"
	kuscia_config_old_file="${KUSCIA_CTR}/kuscia.yaml"
	kuscia_config_file="${KUSCIA_CONFIG_INSTALL_DIR}/kuscia.yaml"
	if [ -f "${kuscia_config_file}" ]; then
		if [ -f "${kuscia_config_file}" ]; then
			echo "${kuscia_config_file} exit, remove old file ${kuscia_config_old_file}"
			rm -f "${kuscia_config_old_file}"
		else
			echo "${kuscia_config_file} not exit, move old file to path ${kuscia_config_file}"
			mv "${kuscia_config_old_file}" "${kuscia_config_file}"
		fi
	fi
	local domainKeyData=""
	if [ -f "${kuscia_config_file}" ]; then
		echo "${kuscia_config_file} exit, use old domainKeyData"
		domainKeyData=$(grep "domainKeyData" "${kuscia_config_file}" | awk '{ print $2 }')
	else
		touch "${kuscia_config_file}"
	fi
	log "kuscia init kuscia.yaml ${kuscia_config_file}"
	if is_master; then
		docker run -it --rm "${KUSCIA_IMAGE}" kuscia init --mode master --domain "${KUSCIA_MASTER_NODE_ID}" --protocol "${KUSCIA_PROTOCOL}" >"${kuscia_config_file}"
	fi
	if is_lite; then
		# shellcheck disable=SC2155
		if is_alice_lite || is_bob_lite || is_tee_lite; then
			export KUSCIA_TOKEN=$(docker exec -it "${KUSCIA_MASTER_CTR}" scripts/deploy/add_domain_lite.sh "${NODE_ID}" "${KUSCIA_MASTER_NODE_ID}" | tr -d '\r\n')
		fi
		docker run -it --rm "${KUSCIA_IMAGE}" kuscia init --mode lite --domain "${NODE_ID}" --master-endpoint "${KUSCIA_MASTER_ENDPOINT}" --lite-deploy-token "${KUSCIA_TOKEN}" --protocol "${KUSCIA_PROTOCOL}" >"${kuscia_config_file}"
	fi
	if is_p2p; then
		docker run -it --rm "${KUSCIA_IMAGE}" kuscia init --mode autonomy --domain "${NODE_ID}" --protocol "${KUSCIA_PROTOCOL}" >"${kuscia_config_file}"
	fi
	if is_p2p_node; then
		docker run -it --rm "${KUSCIA_IMAGE}" kuscia init --mode autonomy --domain "${NODE_ID}" --protocol "${KUSCIA_PROTOCOL}" >"${kuscia_config_file}"
	fi
	if [ -n "$domainKeyData" ]; then
		echo "domainKeyData is not empty, cover kuscia.yaml"
		if [[ "$OSTYPE" == "darwin"* ]]; then
			sed -i '' "s/^domainKeyData: .*/domainKeyData: ${domainKeyData}/" "${kuscia_config_file}"
		else
			sed -i "s/^domainKeyData: .*/domainKeyData: ${domainKeyData}/" "${kuscia_config_file}"
		fi
	fi
}

function copy_kuscia_api_client_certs() {
	local volume_path=$1
	# copy result
	tmp_path=${volume_path}/temp/certs
	mkdir -p "${tmp_path}"
	# shellcheck disable=SC2086
	if ! noTls; then
		docker cp "${KUSCIA_CTR}":/"${CTR_CERT_ROOT}"/token "${tmp_path}"/token
	fi
	docker cp "${KUSCIA_CTR}":/"${CTR_CERT_ROOT}"/ca.crt "${tmp_path}"/ca.crt
	docker cp "${KUSCIA_CTR}":/"${CTR_CERT_ROOT}"/kusciaapi-client.crt "${tmp_path}"/client.crt
	docker cp "${KUSCIA_CTR}":/"${CTR_CERT_ROOT}"/kusciaapi-client.key "${tmp_path}"/client.pem
	docker run -d --rm --name "${KUSCIA_CTR_PREFIX}"-dummy --volume="${volume_path}"/config:/tmp/temp "$KUSCIA_IMAGE" tail -f /dev/null >/dev/null 2>&1
	docker cp -a "${tmp_path}" "${KUSCIA_CTR_PREFIX}"-dummy:/tmp/temp/
	docker rm -f "${KUSCIA_CTR_PREFIX}"-dummy >/dev/null 2>&1
	rm -rf "${volume_path}"/temp
	log "Copy kuscia api client certs to web server container done"
}

function post_kuscia_node() {
	log "init kuscia_node by post"
	copy_kuscia_api_client_certs "${DIR}"
	if [[ ! -e "${DIR}/config/certs/token" ]]; then
		touch "${DIR}/config/certs/token"
	fi
	local PAD_HTTP_STATUS=0
	local curl_cmd="curl --retry 3 --retry-delay 1 --max-time 10 -k --write-out '%{http_code}' --silent -o post_kuscia_node.txt -X POST \
		-H \"Content-Type: multipart/form-data\" \
		-F 'json_data={\"domainId\":\"${NODE_ID}\",\"token\":\"${KUSCIA_TOKEN}\",\"mode\":\"p2p\",\"port\":\"${KUSCIA_API_GRPC_PORT}\",\"protocol\":\"${KUSCIA_PROTOCOL}\",\"transPort\":\"${KUSCIA_GATEWAY_PORT}\"};type=application/json' \
		-F \"certFile=@${DIR}/config/certs/client.crt\" \
		-F \"keyFile=@${DIR}/config/certs/client.pem\" \
		-F \"token=@${DIR}/config/certs/token\" \
		\"${SECRETPAD_MASTER_ENDPOINT}/api/v1alpha1/inst/node/register\""

	PAD_HTTP_STATUS=$(eval "$curl_cmd") || true
	if [[ ! -e post_kuscia_node.txt ]]; then
		log_error "HTTP failed，code: $PAD_HTTP_STATUS ; ${KUSCIA_CTR} removed"
		docker rm -f "${KUSCIA_CTR}" >/dev/null 2>&1
		log "init kuscia_node error..."
		return 1
	fi
	resp=$(cat post_kuscia_node.txt)
	log "HTTP response: $resp"
	code=$(echo "$resp" | grep -o '"code":[0-9]*' | cut -d':' -f2 | tr -d ' ')
	if [ "$PAD_HTTP_STATUS" -eq 200 ] && [ "$code" -eq 0 ]; then
		log_success "HTTP success，code: $PAD_HTTP_STATUS"
		log "init kuscia_node success..."
	else
		log_error "HTTP failed，code: $PAD_HTTP_STATUS ; ${KUSCIA_CTR} removed"
		docker rm -f "${KUSCIA_CTR}" >/dev/null 2>&1
		log "init kuscia_node error..."
		return 1
	fi
}
