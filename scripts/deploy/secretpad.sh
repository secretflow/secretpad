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
	docker run -it --rm --entrypoint /bin/bash --volume="${volume_path}"/db:/app/db -e SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE}"  "${SECRETPAD_IMAGE}" -c "scripts/sql/update-sql.sh"
	log "initialize ${SPRING_PROFILES_ACTIVE}  webserver database done "
}

function create_secretpad_user_password() {
	local volume_path=$1
	local user_name=$2
	local password=$3
	docker run -it --rm --entrypoint /bin/bash --volume="${volume_path}"/db:/app/db "${SECRETPAD_IMAGE}" -c "scripts/user/register_account.sh -n '${user_name}' -p '${password}' -t '${SPRING_PROFILES_ACTIVE}' -o '${NODE_ID}'"
	log "create webserver user and password done"
}

function copy_secretpad_file_to_volume() {
	local dst_path=$1
	docker run --rm --entrypoint /bin/bash -v "${dst_path}":/tmp/secretpad "$SECRETPAD_IMAGE" -c 'cp -R /app/config /tmp/secretpad/'
	docker run --rm --entrypoint /bin/bash -v "${dst_path}":/tmp/secretpad "$SECRETPAD_IMAGE" -c 'cp -R /app/db /tmp/secretpad/'
	log "copy webserver config and database file done"
}

function copy_kuscia_api_client_certs() {
	local volume_path=$1
	# copy result
	tmp_path=${volume_path}/temp/certs
	mkdir -p "${tmp_path}"
	# shellcheck disable=SC2086
	docker cp ${KUSCIA_CTR}:/${CTR_CERT_ROOT}/ca.crt ${tmp_path}/ca.crt
	docker cp "${KUSCIA_CTR}":/"${CTR_CERT_ROOT}"/kusciaapi-client.crt "${tmp_path}"/client.crt
	docker cp "${KUSCIA_CTR}":/"${CTR_CERT_ROOT}"/kusciaapi-client.key "${tmp_path}"/client.pem
	if ! noTls; then
	  docker cp "${KUSCIA_CTR}":/"${CTR_CERT_ROOT}"/token "${tmp_path}"/token
	fi
	docker run -d --rm --name "${KUSCIA_CTR_PREFIX}"-dummy --volume="${volume_path}"/config:/tmp/temp "$KUSCIA_IMAGE" tail -f /dev/null >/dev/null 2>&1
	docker cp -a "${tmp_path}" "${KUSCIA_CTR_PREFIX}"-dummy:/tmp/temp/
	docker rm -f "${KUSCIA_CTR_PREFIX}"-dummy >/dev/null 2>&1
	rm -rf "${volume_path}"/temp
	log "Copy kuscia api client certs to web server container done"
}

function render_secretpad_config() {
	local volume_path=$1
	local tmpl_path=${volume_path}/config/template/application.yaml.tmpl
	local store_key_password=$2
	#local default_login_password
	# create data mesh service
	log "kuscia_lite_ip: '${KUSCIA_CTR}'"
	# render kuscia api address
	sed "s/{{.KUSCIA_API_ADDRESS}}/${KUSCIA_CTR}/g;
  s/{{.KUSCIA_API_LITE_ALICE_ADDRESS}}/${KUSCIA_CTR_PREFIX}-lite-alice/g;
  s/{{.KUSCIA_API_LITE_BOB_ADDRESS}}/${KUSCIA_CTR_PREFIX}-lite-bob/g" \
		"${tmpl_path}" >"${volume_path}"/application_01.yaml
	# render store password
	sed "s/{{.PASSWORD}}/${store_key_password}/g" "${volume_path}"/application_01.yaml >"${volume_path}"/application.yaml
	docker run -d --rm --name "${KUSCIA_CTR_PREFIX}"-dummy --volume="${volume_path}"/config:/tmp/temp "$KUSCIA_IMAGE" tail -f /dev/null >/dev/null 2>&1
	docker cp "${volume_path}"/application.yaml "${KUSCIA_CTR_PREFIX}"-dummy:/tmp/temp/
	docker rm -f "${KUSCIA_CTR_PREFIX}"-dummy >/dev/null 2>&1
	# rm temp file
	rm -rf "${volume_path}"/application_01.yaml "${volume_path}"/application.yaml
	# render default_login_password
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
			log "Would use default password: 12#\$qwER"
			SECRETPAD_PASSWORD="12#\$qwER"
		fi
	done
	set -e
	stty echo # enable display
	log "The user and password have been set up successfully."
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
		docker cp "${domain_ctr}":/"${CTR_CERT_ROOT}"/ca.crt "${tmp_path}"/ca.crt
		docker cp "${domain_ctr}":/"${CTR_CERT_ROOT}"/kusciaapi-client.crt "${tmp_path}"/client.crt
		docker cp "${domain_ctr}":/"${CTR_CERT_ROOT}"/kusciaapi-client.key "${tmp_path}"/client.pem
		if ! noTls; then
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
	account_settings
	local volume_path=$PAD_VOLUME_PATH/$NODE_ID
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
		# initialize secretpad dbd
		log "initialize secretpad db ..."
		init_secretpad_db
		# create secretpad user and password
		log "create secretpad user and password ..."
		create_secretpad_user_password "${volume_path}" "$SECRETPAD_USER_NAME" "$SECRETPAD_PASSWORD"
		# copy kuscia api client certs
		log "copy kuscia api client certs ..."
		copy_kuscia_api_client_certs "${volume_path}"
		# copy alice/bob lite certs
		copy_kuscia_api_lite_client_certs alice "${volume_path}"
		copy_kuscia_api_lite_client_certs bob "${volume_path}"
		# render secretpad config
		log "render secretpad config ..."
		render_secretpad_config "${volume_path}" ${secretpad_key_pass}
		# make directory
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
			-e KUSCIA_API_ADDRESS="${KUSCIA_CTR}" \
			-e KUSCIA_GW_ADDRESS="${KUSCIA_CTR}":80 \
			-e SECRETPAD_IMAGE="${SECRETPAD_IMAGE}" \
			-e KUSCIA_IMAGE="${KUSCIA_IMAGE}" \
			-e SECRETFLOW_IMAGE="${SECRETFLOW_IMAGE}" \
			-e KUSCIA_PROTOCOL="${KUSCIA_PROTOCOL}" \
			-e JAVA_OPTS="${JAVA_OPTS}" \
			"${SECRETPAD_IMAGE}"
		probe_secret_pad "${PAD_CTR}"
		create_secretpad_svc "${KUSCIA_CTR}" "${PAD_CTR}" "$NODE_ID"
		log "Web server started successfully"
		log "Please visit the website http://localhost:${PAD_PORT} (or http://{the IPAddress of this machine}:$PAD_PORT) to experience the Kuscia web's functions ."
		log "The login name:'${SECRETPAD_USER_NAME}' ,The login password:'${SECRETPAD_PASSWORD}' ."
		log "The demo data would be stored in the path: $PAD_INSTALL_DIR ."
		log "the SECRETPAD_IMAGE is: ${SECRETPAD_IMAGE} ."
		log "the KUSCIA_IMAGE is: ${KUSCIA_IMAGE} ."
		log "the SECRETFLOW_TAG is: ${SECRETFLOW_IMAGE} ."
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

while getopts 'n:d:s:' option; do
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
