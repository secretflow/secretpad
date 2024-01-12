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

# load images
KUSCIA_IMAGE=""
SECRETPAD_IMAGE=""
SECRETFLOW_IMAGE=""

TEE_APP_IMAGE=""
TEE_DM_IMAGE=""
CAPSULE_MANAGER_SIM_IMAGE=""

LITE_INSTALL_DIR="$HOME/kuscia/lite"
P2P_DEFAULT_DIR="$HOME/kuscia/p2p"
TEE_DOMAIN="tee"

set -e

usage() {
  echo "$(basename "$0") DEPLOY_MODE [OPTIONS]
NETWORK_MODE:
    master       deploy master (default)
    lite         deploy lite node

lite OPTIONS:
    -i              [optional]  The IP address exposed by the domain. Usually the host IP, default is the IP address of interface eth0.
    -c              [optional]  The host directory used to store domain certificates, default is 'kuscia-{{DEPLOY_MODE}}-{{DOMAIN_ID}}-certs'. It will be mounted into the domain container.
    -h              [optional]  Show this help text.
    -p              [optional]  The port exposed by kuscia-lite-gateway, The port must NOT be occupied by other processes, default 10800
    -s              [optional]  The port exposed by secretpad-edge, The port must NOT be occupied by other processes, default 10801
    -k              [optional]  The port exposed by kuscia-lite-api-http, The port must NOT be occupied by other processes, default 40802
    -g              [optional]  The port exposed by kuscia-lite-api-grpc, The port must NOT be occupied by other processes, default 40803
    -t              [mandatory] The deploy token, get this token from secretpad platform.
    -m              [mandatory] The master endpoint.
    -n              [mandatory] Domain id to be deployed.
    -d              [optional]  The install directory. Default is ${LITE_INSTALL_DIR}.

example:
    install.sh
    install.sh lite -n alice-domain-id -m 'https://root-kuscia-master:1080' -t xdeploy-tokenx -p 10080 -s 10081
    "
}

mode="master"
case  "$1" in
master | lite | tee | p2p)
  mode=$1
  shift
  ;;
esac

domain_id=
domain_host_ip=
domain_host_port=
edge_domain_host_port=
lite_api_http_port=
lite_api_grpc_port=
domain_certs_dir=
master_endpoint=
token=
masterca=
volume_path=$(pwd)
install_dir=

while getopts 'c:d:i:n:p:s:t:m:k:g:h' option; do
  case "$option" in
  c)
    domain_certs_dir=$OPTARG
    ;;
  d)
    install_dir=$OPTARG
    ;;
  i)
    domain_host_ip=$OPTARG
    ;;
  n)
    domain_id=$OPTARG
    ;;
  p)
    domain_host_port=$OPTARG
    ;;
  s)
    edge_domain_host_port=$OPTARG
    ;;
  k)
    lite_api_http_port=$OPTARG
    ;;
  g)
    lite_api_grpc_port=$OPTARG
    ;;
  t)
    token=$OPTARG
    ;;
  m)
    master_endpoint=$OPTARG
    ;;
  v)
    masterca=$OPTARG
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

function init_images_from_files() {
    for file in images/*; do
    	if [ -f "$file" ]; then
    		echo "$file"
    		imageInfo="$(docker load <$file)"
    		echo "echo ${imageInfo}"
    		someimage=$(echo ${imageInfo} | sed "s/Loaded image: //")
    		if [[ $someimage == *kuscia* ]]; then
    			KUSCIA_IMAGE=$someimage
    		elif [[ $someimage == *secretpad* ]]; then
    			SECRETPAD_IMAGE=$someimage
    		elif [[ $someimage == *secretflow-lite* ]]; then
    			SECRETFLOW_IMAGE=$someimage
    		elif [[ $someimage == *sf-dev-anolis8* ]]; then
        	SECRETFLOW_IMAGE=$someimage
        elif [[ $someimage == *sf-tee-dm-sim* ]]; then
          TEE_DM_IMAGE=$someimage
        elif [[ $someimage == *capsule-manager-sim* ]]; then
          CAPSULE_MANAGER_SIM_IMAGE=$someimage
        elif [[ $someimage == *teeapps-sim* ]]; then
          TEE_APP_IMAGE=$someimage
        fi
    	fi
    done
}

function start_master() {
  docker run --rm -v $(pwd):/tmp/kuscia "$1" cp -f /home/kuscia/scripts/deploy/start_standalone.sh /tmp/kuscia
  echo "bash $(pwd)/start_standalone.sh -u web"
  bash $(pwd)/start_standalone.sh -u web
  echo "delete alice bob dp domain data"
  docker exec -it  ${USER}-kuscia-master kubectl delete domaindata alice-dp-table -n alice
  docker exec -it  ${USER}-kuscia-master kubectl delete domaindata bob-dp-table -n bob
}
function start_secretpad() {
    edge_opt="-n ${domain_id} -s ${edge_domain_host_port}"
    # initialize start_edge.sh
    docker run --rm --entrypoint /bin/bash -v $(pwd):/tmp/secretpad $SECRETPAD_IMAGE -c 'cp -R /app/scripts/start_edge.sh /tmp/secretpad/'
    echo "bash $(pwd)/start_edge.sh ${edge_opt}"
    bash $(pwd)/start_edge.sh ${edge_opt}
}
function start_p2p_secretpad() {
    KUSCIA_NAME="${USER}-kuscia-autonomy-${domain_id}"
    edge_opt="-n ${domain_id} -s ${edge_domain_host_port} -m ${KUSCIA_NAME}:8083 "

    # initialize start_p2p.sh
    docker run --rm --entrypoint /bin/bash -v $(pwd):/tmp/secretpad $SECRETPAD_IMAGE -c 'cp -R /app/scripts/start_p2p.sh /tmp/secretpad/'
    echo "bash $(pwd)/start_p2p.sh ${edge_opt}"
    bash $(pwd)/start_p2p.sh ${edge_opt}
}
function loadTeeDmImage2Container() {
  local image_tar
  for file in images/*; do
    if [ -f "$file" ]; then
      someimage=$(basename "$file")
      echo "echo ${someimage}"
      if [[ $someimage == *sf-tee-dm-sim* ]]; then
        tee_dm_image_tar=$someimage
      	cp images/$tee_dm_image_tar /tmp/$tee_dm_image_tar
	image_tar=/tmp/$tee_dm_image_tar
      fi
    fi
  done
  echo "Start importing image '${TEE_DM_IMAGE}' Please be patient..."
  if [ -n "$image_tar" ]; then
    echo "load by local image"
  else
    docker pull ${TEE_DM_IMAGE}
    local image_id
    image_id=$(docker images --filter="reference=${TEE_DM_IMAGE}" --format "{{.ID}}")
    image_tar=/tmp/$(echo ${TEE_DM_IMAGE} | sed 's/\//_/g').${image_id}.tar
  fi
  if [ ! -e $image_tar ]; then
    docker save $TEE_DM_IMAGE -o $image_tar
  fi
  local container_id=$1
  local CTR_ROOT=/home/kuscia
  docker exec -it $container_id ctr -a=${CTR_ROOT}/containerd/run/containerd.sock -n=k8s.io images import $image_tar
}
function start_lite() {
  # deploy lite
  if [[ ${domain_id} == "" ]]; then
    printf "empty domain id\n" >&2
    exit 1
  fi

  if [[ ${master_endpoint} == "" ]]; then
    printf "empty master_endpoint \n" >&2
    exit 1
  fi

  if [[ ${token} == "" ]]; then
    printf "empty deploy token \n" >&2
    exit 1
  fi

  if [[ ${domain_host_port} == "" ]]; then
    domain_host_port="10080"
  fi

  if [[ ${edge_domain_host_port} == "" ]]; then
    edge_domain_host_port="10081"
  fi

  if [[ ${lite_api_http_port} == "" ]]; then
    lite_api_http_port="48082"
  fi

  if [[ ${lite_api_grpc_port} == "" ]]; then
    lite_api_grpc_port="48083"
  fi

  if [[ ${install_dir} == "" ]]; then
    install_dir=${LITE_INSTALL_DIR}
  fi

  # set intall dir of the deploy.sh
  # the datapath is ${ROOT}/kuscia-${deploy_mode}-${DOMAIN_ID}-data
  # the certpath is ${ROOT}/kuscia-${deploy_mode}-${DOMAIN_ID}-certs
  export ROOT=${install_dir}

  cmd_opt="-n ${domain_id} -m ${master_endpoint} -t ${token} -p ${domain_host_port} -k ${lite_api_http_port} -g ${lite_api_grpc_port}"

  # set the datapath of lite is ${ROOT}/domain-id/data
  domain_data_dir=${ROOT}/${domain_id}/data
  cmd_opt="${cmd_opt} -d ${domain_data_dir}"
  # set the certpath of lite is ${ROOT}/domain-id/certs
  if [[ ${domain_certs_dir} == "" ]]; then
    domain_certs_dir=${ROOT}/${domain_id}/certs
  fi
  cmd_opt="${cmd_opt} -c ${domain_certs_dir}"
  # set host ip
  if [[ ${domain_host_ip} != "" ]]; then
    cmd_opt="${cmd_opt} -i ${domain_host_ip}"
  fi

  # copy deploy.sh from kuscia image
  docker run --rm -v $(pwd):/tmp/kuscia $KUSCIA_IMAGE cp -f /home/kuscia/scripts/deploy/deploy.sh /tmp/kuscia
  # execute deploy lite shell
  echo "bash $(pwd)/deploy.sh lite ${cmd_opt}"
  bash $(pwd)/deploy.sh lite ${cmd_opt}
  local domain_ctr=${USER}-kuscia-lite-${domain_id}
  echo "init_kusciaapi_client_cert to ${domain_ctr}"
  docker exec -it ${domain_ctr} scripts/deploy/init_kusciaapi_client_certs.sh
  loadTeeDmImage2Container "$domain_ctr"
}

function start_kuscia() {
  if [[ ${domain_id} == "" ]]; then
    printf "empty domain id\n" >&2
    exit 1
  fi

  if [[ ${domain_host_ip} == "" ]]; then
    domain_host_ip="127.0.0.1"
  fi

  if [[ ${domain_host_port} == "" ]]; then
    domain_host_port="8080"
  fi

  if [[ ${lite_api_http_port} == "" ]]; then
    lite_api_http_port="8081"
  fi

  if [[ ${lite_api_grpc_port} == "" ]]; then
    lite_api_grpc_port="8082"
  fi

  if [[ ${install_dir} == "" ]]; then
    install_dir=${P2P_DEFAULT_DIR}
  fi

  # set intall dir of the deploy.sh
  # the datapath is ${ROOT}/kuscia-${deploy_mode}-${DOMAIN_ID}-data
  # the certpath is ${ROOT}/kuscia-${deploy_mode}-${DOMAIN_ID}-certs
  export ROOT=${install_dir}

  cmd_opt="-n ${domain_id} -p ${domain_host_port} -k ${lite_api_http_port} -g ${lite_api_grpc_port} -d ${ROOT}/kuscia-autonomy-${domain_id}-data/${domain_id} -l ${ROOT}/kuscia-autonomy-${domain_id}-log"

  # set the certpath of lite is ${ROOT}/domain-id/certs
  if [[ ${domain_certs_dir} == "" ]]; then
    domain_certs_dir=${ROOT}/kuscia-autonomy-${domain_id}-certs
  fi
  rm -rf domain_certs_dir
  cmd_opt="${cmd_opt} -c ${domain_certs_dir}"
  # set host ip
  if [[ ${domain_host_ip} != "" ]]; then
    cmd_opt="${cmd_opt} -i ${domain_host_ip}"
  fi

  # copy deploy.sh from kuscia image
  docker run --rm -v $(pwd):/tmp/kuscia $KUSCIA_IMAGE cp -f /home/kuscia/scripts/deploy/deploy.sh /tmp/kuscia
  # execute deploy lite shell
  echo "bash $(pwd)/deploy.sh autonomy ${cmd_opt}"
  bash $(pwd)/deploy.sh autonomy ${cmd_opt}
  # add external name svc
  docker exec -it  ${USER}-kuscia-autonomy-${domain_id} scripts/deploy/create_secretpad_svc.sh ${USER}-kuscia-autonomy-secretpad-${domain_id} ${domain_id}
  # delete alice bob domain data  mach kuscia dp alice-dp-table  bob-dp-table
  echo "delete alice bob domain data"
  docker exec -it  ${USER}-kuscia-autonomy-${domain_id} kubectl delete domaindata alice-table bob-table alice-dp-table bob-dp-table -n ${domain_id}
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
        ipv4=$(ip -4 addr show $eth | grep -oP '(?<=inet\s)\d+(\.\d+){3}') || true
      fi
    fi
    ;;
  "Darwin")
    ipv4=$(ipconfig getifaddr en0) || true
    ;;
  esac
  echo $ipv4
}

function start_lite_tee() {
  domain_id=$TEE_DOMAIN
  if [[ ${domain_host_port} == "" ]]; then
    domain_host_port="48080"
  fi

  if [[ ${lite_api_http_port} == "" ]]; then
    lite_api_http_port="47082"
  fi

  if [[ ${lite_api_grpc_port} == "" ]]; then
    lite_api_grpc_port="47083"
  fi
  host_ip=$(getIPV4Address)
  master_endpoint="https://${host_ip}:18080"
  docker exec -it "${USER}-kuscia-master" scripts/deploy/add_domain_lite.sh "${domain_id}"
  token=$(docker exec -it ${USER}-kuscia-master kubectl get domain $domain_id  -o=jsonpath='{.status.deployTokenStatuses[?(@.state=="unused")].token}')
  # init tee
  start_lite
  docker run --rm --entrypoint /bin/bash -v $(pwd):/tmp/secretpad $SECRETPAD_IMAGE -c 'cp -R /app/scripts/pack/tee/init_tee.sh /tmp/secretpad/'
  docker run --rm --entrypoint /bin/bash -v $(pwd):/tmp/secretpad $SECRETPAD_IMAGE -c 'cp -R /app/scripts/pack/tee/tee-capsule-manager.yaml /tmp/secretpad/'
  docker run --rm --entrypoint /bin/bash -v $(pwd):/tmp/secretpad $SECRETPAD_IMAGE -c 'cp -R /app/scripts/pack/tee/tee-image.yaml /tmp/secretpad/'
  bash ./init_tee.sh
}

init_images_from_files

export KUSCIA_IMAGE=$KUSCIA_IMAGE
echo -e  "KUSCIA_IMAGE=$KUSCIA_IMAGE"

export SECRETPAD_IMAGE=$SECRETPAD_IMAGE
echo -e  "SECRETPAD_IMAGE=$SECRETPAD_IMAGE"

export SECRETFLOW_IMAGE=$SECRETFLOW_IMAGE
echo -e  "SECRETFLOW_IMAGE=$SECRETFLOW_IMAGE"

export TEE_APP_IMAGE=$TEE_APP_IMAGE
echo -e  "TEE_APP_IMAGE=$TEE_APP_IMAGE"

export TEE_DM_IMAGE=$TEE_DM_IMAGE
echo -e  "TEE_DM_IMAGE=$TEE_DM_IMAGE"

export CAPSULE_MANAGER_SIM_IMAGE=$CAPSULE_MANAGER_SIM_IMAGE
echo -e  "CAPSULE_MANAGER_SIM_IMAGE=$CAPSULE_MANAGER_SIM_IMAGE"


# deploy master
if [[ ${mode} == "master" ]]; then
  echo -e "start_master $KUSCIA_IMAGE"
  start_master $KUSCIA_IMAGE
  start_lite_tee
  exit 0
fi

if [[ ${mode} == "tee" ]]; then
  domain_id=$TEE_DOMAIN
  # init tee
  start_lite
  bash ./init_tee.sh
  exit 0
fi

if [[ ${mode} == "p2p" ]]; then
  # init kuscia
  start_kuscia
  start_p2p_secretpad
  exit 0
fi

if [[ ${mode} == "lite" ]]; then
  # init kuscia
  start_lite
  start_secretpad
  exit 0
fi

# create lite with secretpad
start_lite
start_kuscia
start_secretpad
start_p2p_secretpad