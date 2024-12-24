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
PAD_DIR="$(cd "$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")" && pwd)"
source "$PAD_DIR/common/log.sh"
source "$PAD_DIR/common/utils.sh"
source "$PAD_DIR/common/secretpad.env"

function generate_secretpad_serverkey() {
	local tmp_volume=$1
	local password=$2
	docker run -it --rm --entrypoint /bin/bash --volume="${tmp_volume}"/config/:/tmp/temp "${SECRETPAD_IMAGE}" -c "scripts/cert/gen_secretpad_serverkey.sh ${password} /tmp/temp"
	rm -rf "${tmp_volume}"/server.jks
	log "generate webserver server key done"
}

function init_secretpad_db() {
	docker run -it --rm --entrypoint /bin/bash --volume="${volume_path}"/db:/app/db -e SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE}" "${SECRETPAD_IMAGE}" -c "scripts/sql/update-sql.sh"
	log "initialize ${SPRING_PROFILES_ACTIVE}  webserver database done "
}

# @Deprecated: This function should no longer be used, use env pass to app exec
function create_secretpad_user_password() {
	local volume_path=$1
	local user_name=$2
	local password=$3
	docker run -it --rm --entrypoint /bin/bash --volume="${volume_path}"/db:/app/db "${SECRETPAD_IMAGE}" -c "scripts/user/register_account.sh -n '${user_name}' -p '${password}' -t '${SPRING_PROFILES_ACTIVE}' -o '${NODE_ID}'"
	log "create webserver user and password done"
}

function is_reinstall() {
	local volume_path=$1
	if [ -f "${volume_path}/db/.update" ]; then
		x=$(cat "${volume_path}"/db/.update)
		read -rp "$(echo -e "${GREEN}"The data \'"${x}"\' already exists. keep it? [y/n]: "${NC}")" yn
		case $yn in
		[Yy]*)
			echo -e "${GREEN}keep data ${x} ...${NC}"
			export SECRETPAD_KEEP_HISTORY_DATA=true
			;;
		*)
			echo -e "${GREEN}remove data ${x} ...${NC}"
			export SECRETPAD_KEEP_HISTORY_DATA=false
			;;
		esac
	else
		export SECRETPAD_KEEP_HISTORY_DATA=false
	fi
}

function copy_secretpad_file_to_volume() {
	local dst_path=$1
	if [ -f "${volume_path}/db/.update" ]; then
		x=$(cat "${dst_path}"/db/.update)
		cp -rp "${dst_path}" "${dst_path}"_back_up_"${x}"
		log "back up secretpad data ... ${dst_path}_back_up_${x}"
	fi
	if [ "$SECRETPAD_KEEP_HISTORY_DATA" == "false" ]; then
		log "create ${dst_path}/db/.update"
		rm -rf "${dst_path}"
		docker run --rm --entrypoint /bin/bash -v "${dst_path}":/tmp/secretpad "$SECRETPAD_IMAGE" -c 'cp -R /app/db /tmp/secretpad/'
		echo "${SECRETPAD_IMAGE##*:}" >"${dst_path}"/db/.update
	fi
	docker run --rm --entrypoint /bin/bash -v "${dst_path}":/tmp/secretpad "$SECRETPAD_IMAGE" -c 'cp -R /app/config /tmp/secretpad/'
	log "copy webserver config and database file done"
}

function update_sf_comp() {
	mkdir -p "${volume_path}"/scripts
	TEMP_CONTAINER=$(docker create "$SECRETPAD_IMAGE")
	docker cp "$TEMP_CONTAINER:/app/scripts/update_components.sh" "${volume_path}"/scripts/update_components.sh
	docker rm "$TEMP_CONTAINER"
	bash "${volume_path}"/scripts/update_components.sh "${SECRETFLOW_IMAGE}" false
}

function copy_kuscia_api_client_certs() {
	local volume_path=$1
	tmp_path=${volume_path}/temp/certs
	mkdir -p "${tmp_path}"
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

function render_secretpad_config() {
	local volume_path=$1
	local store_key_password=$2
	export KEY_PASSWORD=${store_key_password}
	export KUSCIA_API_ADDRESS=${KUSCIA_CTR}:8083
	export KUSCIA_API_LITE_ALICE_ADDRESS=${KUSCIA_CTR_PREFIX}-lite-alice:8083
	export KUSCIA_API_LITE_BOB_ADDRESS=${KUSCIA_CTR_PREFIX}-lite-bob:8083
	export KUSCIA_API_LITE_TEE_ADDRESS=${KUSCIA_CTR_PREFIX}-lite-tee:8083
	log "Render webserver config done"
}

function check_user_name() {
	local user_name=$1
	strlen=$(echo "${user_name}" | grep -E --color '^(.{4,}).*$')
	if [ -n "${strlen}" ]; then
		return 0
	else
		log "The username requires a length greater than 4"
		return 1
	fi
}

function check_user_passwd() {
	local password=$1
	# length greater than 8
	str_len=$(echo "${password}" | grep -E --color '^(.{8,}).*$')
	# with lowercase letters
	str_low=$(echo "${password}" | grep -E --color '^(.*[a-z]+).*$')
	# with uppercase letters
	str_upp=$(echo "${password}" | grep -E --color '^(.*[A-Z]).*$')
	# with special characters
	str_ts=$(echo "${password}" | grep -E --color '^(.*\W).*$')
	# with numbers
	str_num=$(echo "${password}" | grep -E --color '^(.*[0-9]).*$')
	if [ -n "${str_len}" ] && [ -n "${str_low}" ] && [ -n "${str_upp}" ] && [ -n "${str_ts}" ] && [ -n "${str_num}" ]; then
		return 0
	else
		log "The password requires a length greater than 8, including uppercase and lowercase letters, numbers, and special characters."
		return 2
	fi
}

function instName_settings() {
	export LANG=C.UTF-8
	if ! is_p2p; then
		return 0
	fi
	if [ "$SECRETPAD_KEEP_HISTORY_DATA" == "true" ]; then
		return 0
	fi
	default_org_name="DEFAULT_INST"
	log "Please set the instName(${default_org_name}).length should be between 4 and 16 characters, spaces will be ignored"
	read -r -p "Enter instName(DEFAULT_INST):" input_org_name
	trimmed_input_org_name=$(echo "$input_org_name" | xargs)

	while true; do
		if [[ -z "$trimmed_input_org_name" ]]; then
			org_name="$default_org_name"
			break
		elif [[ ${#trimmed_input_org_name} -ge 4 && ${#trimmed_input_org_name} -le 16 ]]; then
			org_name=$(echo "$trimmed_input_org_name" | tr -d '[:space:]')
			break
		elif [[ ${#trimmed_input_org_name} -lt 4 ]]; then
			log_warn "The entered name is too short and requires at least 4 characters (after removing spaces). Please re-enter."
			read -r -p "Enter instName(DEFAULT_INST):" input_org_name
			trimmed_input_org_name=$(echo "$input_org_name" | xargs)
		else
			log_warn "The entered name (after removing spaces) is too long and cannot exceed 16 characters. Please re-enter."
			read -r -p "Enter instName(DEFAULT_INST):" input_org_name
			trimmed_input_org_name=$(echo "$input_org_name" | xargs)
		fi
	done
	export INST_NAME="$org_name"
	log "instName is: $INST_NAME"
}

function account_settings() {
	local RET
	set +e
	log "Please set the username and the password used to login the KUSCIA-WEB.\n\
The username requires a length greater than 4, The password requires a length greater than 8,\n\
including uppercase and lowercase letters, numbers, and special characters."
	for ((i = 0; i < 1; i++)); do
		read -r -p "Enter username(admin):" SECRETPAD_USER_NAME
		check_user_name "${SECRETPAD_USER_NAME}"
		RET=$?
		if [ "${RET}" -eq 0 ]; then
			break
		elif [ "${RET}" -ne 0 ] && [ "${i}" == 0 ]; then
			log "Would use default user: admin"
			SECRETPAD_USER_NAME="admin"
		fi
	done
	stty -echo # disable display
	for ((i = 0; i < 3; i++)); do
		read -r -p "Enter password: " SECRETPAD_PASSWORD
		echo ""
		check_user_passwd "${SECRETPAD_PASSWORD}"
		RET=$?
		if [ "${RET}" -eq 0 ]; then
			local CONFIRM_PASSWD
			read -r -p "Confirm password again: " CONFIRM_PASSWD
			echo ""
			if [ "${CONFIRM_PASSWD}" == "${SECRETPAD_PASSWORD}" ]; then
				break
			else
				log "Password not match! please reset"
			fi
		elif [ "${RET}" -ne 0 ] && [ "${i}" == 2 ]; then
			export LC_ALL=C
			SECRETPAD_PASSWORD=$(generate_password)
			log "Would use random password: ${SECRETPAD_PASSWORD}"
		fi
	done
	set -e
	stty echo # enable display
	log "The user and password have been set up successfully."
}

# shellcheck disable=SC2120
function generate_password() {
	local length=${1:-7}
	local lcs="abcdefghijklmnopqrstuvwxyz"
	local ucs="ABCDEFGHIJKLMNOPQRSTUVWXYZ"
	local digits="0123456789"
	local special="!@#$%^&*()+=-"
	# Generate a random password that may not contain all types of characters
	# shellcheck disable=SC2155
	local password=$(openssl rand -base64 12 | tr -d '/+=' | cut -c1-"$length")
	# Ensure that the password contains lowercase letters, uppercase letters, and numbers
	while [[ ! "$password" =~ [$lcs] || ! "$password" =~ [$ucs] || ! "$password" =~ [$digits] ]]; do
		password=$(openssl rand -base64 12 | tr -d '/+=' | cut -c1-"$length")
	done
	# If the password still does not meet the length requirement, then supplement
	# shellcheck disable=SC2004
	while ((${#password} < $length)); do
		local char_type=$((RANDOM % 3))
		case $char_type in
		0) password+="${lcs:$((RANDOM % ${#lcs})):1}" ;;
		1) password+="${ucs:$((RANDOM % ${#ucs})):1}" ;;
		2) password+="${digits:$((RANDOM % ${#digits})):1}" ;;
		esac
	done
	# Add a special character
	password+="${special:$((RANDOM % ${#digits})):1}"
	echo "$password"
}

function copy_kuscia_api_lite_client_certs() {
	if is_master; then
		local domain_id=$1
		local volume_path=$2
		local IMAGE=$SECRETPAD_IMAGE
		local domain_ctr=${KUSCIA_CTR_PREFIX}-lite-${domain_id}
		# copy result
		tmp_path=${volume_path}/temp/certs/${domain_id}
		mkdir -p "${tmp_path}"
		if ! noTls; then
			docker cp "${domain_ctr}":/"${CTR_CERT_ROOT}"/ca.crt "${tmp_path}"/ca.crt
			docker cp "${domain_ctr}":/"${CTR_CERT_ROOT}"/kusciaapi-client.crt "${tmp_path}"/client.crt
			docker cp "${domain_ctr}":/"${CTR_CERT_ROOT}"/kusciaapi-client.key "${tmp_path}"/client.pem
			docker cp "${domain_ctr}":/"${CTR_CERT_ROOT}"/token "${tmp_path}"/token
		fi
		docker run -d --rm --name "${KUSCIA_CTR_PREFIX}"-dummy --volume="${volume_path}"/config/certs:/tmp/temp "$IMAGE" tail -f /dev/null >/dev/null 2>&1
		docker cp -a "${tmp_path}" "${KUSCIA_CTR_PREFIX}"-dummy:/tmp/temp/
		docker rm -f "${KUSCIA_CTR_PREFIX}"-dummy >/dev/null 2>&1
		rm -rf "${volume_path}"/temp
		log "copy kuscia api client lite :${domain_id} certs to web server container done"
	fi
}

function create_secretpad_svc() {
	local ctr=$1
	local secretpad_ctr=$2
	local domain_id=$3
	if is_master || is_p2p; then
		if is_master; then
			ctr=${KUSCIA_MASTER_CTR}
			domain_id=${KUSCIA_MASTER_NODE_ID}
		fi
		docker exec -it "${ctr}" scripts/deploy/create_secretpad_svc.sh "${secretpad_ctr}" "${domain_id}"
	fi
}

function start() {
	# volume_path
	# ├── data
	# │   ├── alice
	# │   │   └── alice.csv
	# │   └── bob
	# │       └── bob.csv
	# └── secretpad
	# │   ├── alice
	# │   │   └── config
	# │   │   └── db
	# │   │   └── log
	# │   └── bob
	# │   │   └── config
	# │   │   └── db
	# │   │   └── log
	#
	local volume_path=$PAD_VOLUME_PATH/$NODE_ID
	is_reinstall "${volume_path}"
	instName_settings
	account_settings
	if need_start_docker_container "$PAD_CTR"; then
		log "Starting container '$PAD_CTR' ..."
		mkdir -p "${volume_path}"
		secretpad_key_pass="secretpad"
		# copy db,config from secretpad image
		log "Copy db,config to '$volume_path' ..."
		copy_secretpad_file_to_volume "${volume_path}"
		# generate server key
		log "Generate server key '$volume_path' ..."
		generate_secretpad_serverkey "${volume_path}" ${secretpad_key_pass}
		log "copy kuscia api client certs ..."
		copy_kuscia_api_client_certs "${volume_path}"
		# copy alice/bob lite certs
		copy_kuscia_api_lite_client_certs alice "${volume_path}"
		copy_kuscia_api_lite_client_certs bob "${volume_path}"
		if need_tee; then
			copy_kuscia_api_lite_client_certs tee "${volume_path}"
		fi
		# render secretpad config
		log "render secretpad config ..."
		render_secretpad_config "${volume_path}" ${secretpad_key_pass}
		# run secretpad
		docker run -itd --init --name="${PAD_CTR}" --restart=always --network="${NETWORK_NAME}" -m "$LITE_MEMORY_LIMIT" \
			--volume="${PAD_INSTALL_DIR}":/app/data \
			--volume="${volume_path}"/log:/app/log \
			--volume="${volume_path}"/config:/app/config \
			--volume="${volume_path}"/db:/app/db \
			--workdir=/app \
			-p "${PAD_PORT}":8080 \
			-e SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE}" \
			-e NODE_ID="${NODE_ID}" \
			-e DEPLOY_MODE="${DEPLOY_MODE}" \
			-e KUSCIA_GW_ADDRESS="${KUSCIA_CTR}":80 \
			-e SECRETPAD_IMAGE="${SECRETPAD_IMAGE##*:}" \
			-e KUSCIA_IMAGE="${KUSCIA_IMAGE##*:}" \
			-e SECRETFLOW_IMAGE="${SECRETFLOW_IMAGE##*:}" \
			-e SECRETFLOW_SERVING_IMAGE="${SECRETFLOW_SERVING_IMAGE##*:}" \
			-e TEE_APP_IMAGE="${TEE_APP_IMAGE##*:}" \
			-e TEE_DM_IMAGE="${TEE_DM_IMAGE##*:}" \
			-e CAPSULE_MANAGER_SIM_IMAGE="${CAPSULE_MANAGER_SIM_IMAGE##*:}" \
			-e DATA_PROXY_IMAGE="${DATAPROXY_IMAGE##*:}" \
			-e SCQL_IMAGE="${SCQL_IMAGE##*:}" \
			-e SECRETPAD_CLOUD_LOG_SLS_AK="${SECRETPAD_CLOUD_LOG_SLS_AK}" \
			-e SECRETPAD_CLOUD_LOG_SLS_SK="${SECRETPAD_CLOUD_LOG_SLS_SK}" \
			-e SECRETPAD_CLOUD_LOG_SLS_HOST="${SECRETPAD_CLOUD_LOG_SLS_HOST}" \
			-e SECRETPAD_CLOUD_LOG_SLS_PROJECT="${SECRETPAD_CLOUD_LOG_SLS_PROJECT}" \
			-e KUSCIA_PROTOCOL="${KUSCIA_PROTOCOL}" \
			-e KEY_PASSWORD="${KEY_PASSWORD}" \
			-e KUSCIA_API_ADDRESS="${KUSCIA_API_ADDRESS}" \
			-e KUSCIA_API_LITE_ALICE_ADDRESS="${KUSCIA_API_LITE_ALICE_ADDRESS}" \
			-e KUSCIA_API_LITE_BOB_ADDRESS="${KUSCIA_API_LITE_BOB_ADDRESS}" \
			-e KUSCIA_API_LITE_TEE_ADDRESS="${KUSCIA_API_LITE_TEE_ADDRESS}" \
			-e SECRETPAD_USER_NAME="${SECRETPAD_USER_NAME}" \
			-e SECRETPAD_PASSWORD="${SECRETPAD_PASSWORD}" \
			-e INST_NAME="${INST_NAME}" \
			-e JAVA_OPTS="${JAVA_OPTS}" \
			-e DATAPROXY_ENABLE="${DATAPROXY_ENABLE}" \
			-e SCQL_ENABLE="${SCQL_ENABLE}" \
			"${SECRETPAD_IMAGE}"
		probe_secret_pad "${PAD_CTR}"
		create_secretpad_svc "${KUSCIA_CTR}" "${PAD_CTR}" "$NODE_ID"
		log "Web server started successfully"
		log "Please visit the website http://localhost:${PAD_PORT} (or http://{the IPAddress of this machine}:$PAD_PORT) to experience the Kuscia web's functions ."
		log "The login name:'${SECRETPAD_USER_NAME}' ,The login password:'${SECRETPAD_PASSWORD}' ."
		log "The data would be stored in the path: $PAD_INSTALL_DIR ."
	fi
}

usage() {
	echo "$(basename "$0") DEPLOY_MODE [OPTIONS]
 OPTIONS:
    -n              [optional]  p2p|edge domain id to be deployed.
    -s              [optional]  secretpad webserver port, The port must NOT be occupied by other processes, default 8088
    -d              [optional]  The data directory used to store domain data. It will be mounted into the domain container. default is $INSTALL_DIR/${NODE_ID}
    "
}

while getopts 'n:d:s:b' option; do
	case "$option" in
	n)
		export NODE_ID=$OPTARG
		;;
	d)
		export PAD_DATA_PATH=${OPTARG}
		;;
	s)
		export PAD_PORT=$OPTARG
		;;
	b)
		export PAD_DEBUG_PORT=$OPTARG
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
start
