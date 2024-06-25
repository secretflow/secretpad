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
    mode:       $MODE     $DEPLOY_MODE
    node:       $NODE_ID
    secretpad   http      port: $PAD_PORT
    kuscia      http      port: $KUSCIA_API_HTTP_PORT
    kuscia      grpc      port: $KUSCIA_API_GRPC_PORT
    kuscia      gateway   port: $KUSCIA_GATEWAY_PORT
    kuscia      protocol: $KUSCIA_PROTOCOL
    secretpad   ctr:      $PAD_CTR
    kuscia      ctr:      $KUSCIA_CTR
    secretpad   dir:      $PAD_VOLUME_PATH
    kuscia      dir:      $KUSCIA_INSTALL_DIR
    "
}

function clear_env() {
	unset MODE
	unset PAD_PORT
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
}

function prepare_environment() {
	if is_master; then
		export MODE="master"
		export KUSCIA_GATEWAY_PORT=18080
		export KUSCIA_API_HTTP_PORT=18082
		export KUSCIA_API_GRPC_PORT=18083
		export KUSCIA_CTR="$KUSCIA_CTR_PREFIX-$PAD_MASTER"
		export PAD_CTR="$KUSCIA_CTR_PREFIX-$PAD_MASTER-$PAD"
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
		export KUSCIA_INSTALL_DIR="$INSTALL_DIR/$PAD_LITE/$PAD_DATA/$NODE_ID"
		export PAD_INSTALL_DIR="$INSTALL_DIR/$PAD_LITE/$PAD_DATA"
		export PAD_VOLUME_PATH="$INSTALL_DIR/$PAD_LITE/$PAD"
		export KUSCIA_LOG_PATH="$INSTALL_DIR/$PAD_LITE/$NODE_ID"
		export SPRING_PROFILES_ACTIVE="edge"
		if is_alice_lite; then
			export KUSCIA_GATEWAY_PORT=28080
			export KUSCIA_API_HTTP_PORT=28082
			export KUSCIA_API_GRPC_PORT=28083
			export KUSCIA_INSTALL_DIR="$INSTALL_DIR/$PAD_MASTER/$PAD_DATA/alice"
			export PAD_VOLUME_PATH="$INSTALL_DIR/$PAD_MASTER/$PAD"
			export KUSCIA_LOG_PATH="$INSTALL_DIR/$PAD_MASTER/$NODE_ID"
		fi
		if is_bob_lite; then
			export KUSCIA_GATEWAY_PORT=38080
			export KUSCIA_API_HTTP_PORT=38082
			export KUSCIA_API_GRPC_PORT=38083
			export KUSCIA_INSTALL_DIR="$INSTALL_DIR/$PAD_MASTER/$PAD_DATA/bob"
			export PAD_VOLUME_PATH="$INSTALL_DIR/$PAD_MASTER/$PAD"
			export KUSCIA_LOG_PATH="$INSTALL_DIR/$PAD_MASTER/$NODE_ID"
		fi
		if is_tee_lite; then
			export KUSCIA_GATEWAY_PORT=48080
			export KUSCIA_API_HTTP_PORT=48082
			export KUSCIA_API_GRPC_PORT=48083
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

function loadImage2Container() {
	local container_id=$1
	local image_tar=$2
	docker exec -it "$container_id" ctr -a="${CTR_ROOT}"/containerd/run/containerd.sock -n=k8s.io images import "$image_tar"
}

function save_image() {
	local image=$1
	image_id=$(docker images --filter="reference=${image}" --format "{{.ID}}")
	image_tar=/tmp/$(echo "${image}" | sed 's/\//_/g').${image_id}.tar
	if [ ! -e "$image_tar" ]; then
		docker save "$image" -o "$image_tar"
	fi
	image_tar_path=$image_tar
}

function saveAndLoad2Container() {
	local image_basename=$1
	local container_id=$2
	if docker exec -it "$container_id" crictl inspecti "${image_basename}" >/dev/null 2>&1; then
		log "Image '${image_basename}' already exists in domain '${container_id}'"
		return
	fi
	save_image "$image_basename"
	log "Save image: $image_basename path: $image_tar_path"
	log "loadImage2Container $container_id $image_tar_path"
	loadImage2Container "$container_id" "$image_tar_path"
	log "Successfully imported image '${image_basename}' to container '${container_id}' ..."
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
	docker exec -it "${KUSCIA_MASTER_CTR}" kubectl delete -f tee-capsule-manager-0.yaml || true
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
	mkdir -p "${KUSCIA_CTR}"
	kuscia_config_file="${KUSCIA_CTR}/kuscia.yaml"
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
	if [ -n "$domainKeyData" ]; then
		echo "domainKeyData is not empty, cover kuscia.yaml"
		sed -i "s/^domainKeyData: .*/domainKeyData: ${domainKeyData}/" "${kuscia_config_file}"
	fi

}
