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

usage() {
  echo "$(basename "$0") DEPLOY_MODE [OPTIONS]
 OPTIONS:
    -n              [mandatory] Domain id to be deployed.
    -s              [optional]  The port exposed by edge, The port must NOT be occupied by other processes, default 10801
    -k              [optional]  The port exposed by kuscia-lite-api-http, The port must NOT be occupied by other processes, default 40802
    -g              [optional]  The port exposed by kuscia-lite-api-grpc, The port must NOT be occupied by other processes, default 40803
    -d              [optional]  The data directory used to store domain data. It will be mounted into the domain container. default is $HOME/kuscia/lite/${NODE_ID}
    "
}

ROOT=$HOME/kuscia/edge
mkdir -p $ROOT

GREEN='\033[0;32m'
NC='\033[0m'

NODE_ID=""
CTR_PREFIX=${USER}-kuscia
CTR_ROOT=/home/kuscia
CTR_CERT_ROOT=${CTR_ROOT}/etc/certs
FORCE_START=false
KUSCIA_LITE_CTR=${CTR_PREFIX}-lite
NETWORK_NAME="kuscia-exchange"
IMAGE=secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/kuscia
if [ "${KUSCIA_IMAGE}" != "" ]; then
  IMAGE=${KUSCIA_IMAGE}
fi
echo -e "IMAGE=${IMAGE}"
if [ "${SECRETPAD_IMAGE}" == "" ]; then
  SECRETPAD_IMAGE=secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/secretpad
fi
echo -e "SECRETPAD_IMAGE=${SECRETPAD_IMAGE}"
SECRETPAD_USER_NAME=""
SECRETPAD_PASSWORD=""
VOLUME_PATH=${ROOT}
JAVA_OPTS=""
SPRING_PROFILES_ACTIVE="edge"
KUSCIA_API_ADDRESS=""
EDGE_DOMAIN_HOST_PORT=""
LITE_MEMORY_LIMIT=4G
function log() {
  local log_content=$1
  echo -e "${GREEN}${log_content}${NC}"
}

function need_start_docker_container() {
  ctr=$1

  if [[ ! "$(docker ps -a -q -f name=^/${ctr}$)" ]]; then
    # need start your container
    return 0
  fi

  if $FORCE_START; then
    log "Remove container '${ctr}' ..."
    docker rm -f $ctr >/dev/null 2>&1
    # need start your container
    return 0
  fi

  read -rp "$(echo -e ${GREEN}The container \'${ctr}\' already exists. Do you need to recreate it? [y/n]: ${NC})" yn
  case $yn in
  [Yy]*)
    echo -e "${GREEN}Remove container ${ctr} ...${NC}"
    docker rm -f $ctr
    # need start your container
    return 0
    ;;
  *)
    return 1
    ;;
  esac

  return 1
}

function copy_secretpad_file_to_volume() {
  local dst_path=$1
  mkdir -p ${dst_path}/secretpad
  mkdir -p ${dst_path}/data
  # copy config file
  docker run --rm --entrypoint /bin/bash -v ${dst_path}/secretpad:/tmp/secretpad $SECRETPAD_IMAGE -c 'cp -R /app/config /tmp/secretpad/'
  # copy sqlite db file
  docker run --rm --entrypoint /bin/bash -v ${dst_path}/secretpad:/tmp/secretpad $SECRETPAD_IMAGE -c 'cp -R /app/db /tmp/secretpad/'
  # copy demo data file
  docker run --rm --entrypoint /bin/bash -v ${dst_path}:/tmp/secretpad $SECRETPAD_IMAGE -c 'cp -R /app/data /tmp/secretpad/'
  log "Copy webserver config and database file done"
}

function generate_secretpad_serverkey() {
  local tmp_volume=$1
  local password=$2
  # generate server key in edge container
  docker run -it --rm --entrypoint /bin/bash --volume=${tmp_volume}/secretpad/config/:/tmp/temp ${SECRETPAD_IMAGE} -c "scripts/gen_secretpad_serverkey.sh ${password} /tmp/temp"
  rm -rf ${tmp_volume}/server.jks
  log "Generate webserver server key done"
}

function init_secretpad_db() {
  # generate server key in edge container
  docker run -it --rm --entrypoint /bin/bash --volume=${volume_path}/secretpad/db:/app/db ${SECRETPAD_IMAGE} -c "scripts/edge-update-sql.sh"
  log "Initialize  webserver database done"
}

function create_secretpad_user_password() {
  local volume_path=$1
  local user_name=$2
  local password=$3
  # generate server key in edge container
  docker run -it --rm --entrypoint /bin/bash --volume=${volume_path}/secretpad/db:/app/db ${SECRETPAD_IMAGE} -c "scripts/register_account.sh -n '${user_name}' -p '${password}' -t EDGE -o '${NODE_ID}'"

  log "Create webserver user and password done"
}

function copy_kuscia_api_client_certs() {
  local volume_path=$1
  # copy result
  tmp_path=${volume_path}/temp/certs
  mkdir -p ${tmp_path}
  docker cp ${KUSCIA_LITE_CTR}-${NODE_ID}:/${CTR_CERT_ROOT}/ca.crt ${tmp_path}/ca.crt
  docker cp ${KUSCIA_LITE_CTR}-${NODE_ID}:/${CTR_CERT_ROOT}/kusciaapi-client.crt ${tmp_path}/client.crt
  docker cp ${KUSCIA_LITE_CTR}-${NODE_ID}:/${CTR_CERT_ROOT}/kusciaapi-client.key ${tmp_path}/client.pem
  docker cp ${KUSCIA_LITE_CTR}-${NODE_ID}:/${CTR_CERT_ROOT}/token ${tmp_path}/token
  docker run -d --rm --name ${CTR_PREFIX}-dummy --volume=${volume_path}/secretpad/config:/tmp/temp $IMAGE tail -f /dev/null >/dev/null 2>&1
  docker cp -a ${tmp_path} ${CTR_PREFIX}-dummy:/tmp/temp/
  docker rm -f ${CTR_PREFIX}-dummy >/dev/null 2>&1
  rm -rf ${volume_path}/temp
  log "Copy kuscia api client certs to web server container done"
}

function render_secretpad_config() {
  local volume_path=$1
  local tmpl_path=${volume_path}/secretpad/config/template/application.yaml.tmpl
  local store_key_password=$2
  #local default_login_password
  # create data mesh service
  log "kuscia_lite_ip: '${KUSCIA_LITE_CTR}-${NODE_ID}'"
  # render kuscia api address
  sed "s/{{.KUSCIA_API_ADDRESS}}/${KUSCIA_LITE_CTR}-${NODE_ID}/g;
  s/{{.KUSCIA_API_LITE_ALICE_ADDRESS}}/${CTR_PREFIX}-lite-alice/g;
  s/{{.KUSCIA_API_LITE_BOB_ADDRESS}}/${CTR_PREFIX}-lite-bob/g" \
  ${tmpl_path} >${volume_path}/application_01.yaml
  # render store password
  sed "s/{{.PASSWORD}}/${store_key_password}/g" ${volume_path}/application_01.yaml >${volume_path}/application.yaml
  # cp file to secretpad's config path
  docker run -d --rm --name ${CTR_PREFIX}-dummy --volume=${volume_path}/secretpad/config:/tmp/temp $IMAGE tail -f /dev/null >/dev/null 2>&1
  docker cp ${volume_path}/application.yaml ${CTR_PREFIX}-dummy:/tmp/temp/
  docker rm -f ${CTR_PREFIX}-dummy >/dev/null 2>&1
  # rm temp file
  rm -rf ${volume_path}/application_01.yaml ${volume_path}/application.yaml
  # render default_login_password
  log "Render webserver config done"
}

function do_http_probe() {
  local ctr=$1
  local endpoint=$2
  local max_retry=$3
  local retry=0
  while [ $retry -lt $max_retry ]; do
    local status_code
    # TODO support MTLS
    status_code=$(docker exec -it $ctr curl -k --write-out '%{http_code}' --silent --output /dev/null ${endpoint})
    if [[ $status_code -eq 200 || $status_code -eq 404 || $status_code -eq 401 ]]; then
      return 0
    fi
    sleep 1
    retry=$((retry + 1))
  done

  return 1
}

function probe_secret_pad() {
  local secretpad_ctr=$1
  if ! do_http_probe $secretpad_ctr "http://127.0.0.1:8080" 60; then
    echo "[Error] Probe secret pad in container '$secretpad_ctr' failed. Please check the log" >&2
    exit 1
  fi
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

function start_edge() {
  # volume_path
  # ├── data
  # │   ├── alice
  # │   │   └── alice.csv
  # │   └── bob
  # │       └── bob.csv
  # └── secretpad
  #     ├── config
  #     └── db
  #
  account_settings
  local volume_path=$1
  local port=$2
  local lite_data_path=$3
  local user_name=$SECRETPAD_USER_NAME
  local password=$SECRETPAD_PASSWORD
  local secretpad_ctr=${CTR_PREFIX}-secretpad-edge-${NODE_ID}
  if need_start_docker_container $secretpad_ctr; then
    log "Starting container '$secretpad_ctr' ..."
    secretpad_key_pass="secretpad"
    # copy db,config,demodata from secretpad image
    log "Copy db,config,demodata to '$volume_path' ..."
    copy_secretpad_file_to_volume ${volume_path}
    # generate server key
    log "Generate server key '$volume_path' ..."
    generate_secretpad_serverkey ${volume_path} ${secretpad_key_pass}
    # initialize secretpad dbd
    init_secretpad_db
    # create secretpad user and password
    create_secretpad_user_password ${volume_path} ${user_name} ${password}
    # copy kuscia api client certs
    copy_kuscia_api_client_certs ${volume_path}
    # render secretpad config
    render_secretpad_config ${volume_path} ${secretpad_key_pass}
    # make directory
    mkdir -p ${volume_path}
    # run secretpad
    docker run -itd --init --name=${secretpad_ctr} --restart=always --network=${NETWORK_NAME} -m $LITE_MEMORY_LIMIT \
      --volume=${lite_data_path}:/app/data/${NODE_ID} \
      --volume=${volume_path}/secretpad/config:/app/config \
      --volume=${volume_path}/secretpad/db:/app/db \
      --workdir=/app \
      -p $port:8080 \
      -e SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} \
      -e NODE_ID=${NODE_ID} \
      -e KUSCIA_API_ADDRESS=${KUSCIA_LITE_CTR}-${NODE_ID}:8083 \
      -e KUSCIA_GW_ADDRESS=${KUSCIA_LITE_CTR}-${NODE_ID}:80 \
      ${SECRETPAD_IMAGE}
    probe_secret_pad ${secretpad_ctr}
    log "Web server started successfully"
    log "Please visit the website http://localhost:8080} (or http://{the IPAddress of this machine}:$port) to experience the Kuscia web's functions ."
    log "The login name:'${SECRETPAD_USER_NAME}' ,The login password:'${SECRETPAD_PASSWORD}' ."
    log "The demo data would be stored in the path: ${VOLUME_PATH} ."
  fi
}

while getopts 'n:d:s:' option; do
  case "$option" in
  n)
    NODE_ID=$OPTARG
    VOLUME_PATH="${VOLUME_PATH}/${OPTARG}"
    ;;
  d)
    KUSCIA_LITE_DATA_PATH=${OPTARG}
    ;;
  s)
    EDGE_DOMAIN_HOST_PORT=$OPTARG
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

if [[ ${KUSCIA_LITE_DATA_PATH} == "" ]]; then
  KUSCIA_LITE_DATA_PATH=$HOME/kuscia/lite/${NODE_ID}/data
fi

start_edge "${VOLUME_PATH}" "$EDGE_DOMAIN_HOST_PORT" "$KUSCIA_LITE_DATA_PATH"
shift $((OPTIND - 1))

