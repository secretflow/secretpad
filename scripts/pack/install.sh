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
LITE_INSTALL_DIR="$HOME/kuscia/lite"
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
    -p              [optional]  The port exposed by domain, The port must NOT be occupied by other processes, default 10800
    -t              [mandatory] The deploy token, get this token from secretpad platform.
    -m              [mandatory] The master endpoint.
    -n              [mandatory] Domain id to be deployed.
    -d              [optional]  The install directory. Default is ${LITE_INSTALL_DIR}.

example:
    install.sh
    install.sh lite -n alice-domain-id -m 'https://root-kuscia-master:1080' -t xdeploy-tokenx
    "
}

mode="master"
case  "$1" in
master | lite )
  mode=$1
  shift
  ;;
esac

domain_id=
domain_host_ip=
domain_host_port=
domain_certs_dir=
master_endpoint=
token=
masterca=
volume_path=$(pwd)
install_dir=

while getopts 'c:d:i:n:p:t:m:h' option; do
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
    fi
	fi
done
export KUSCIA_IMAGE=$KUSCIA_IMAGE
export SECRETPAD_IMAGE=$SECRETPAD_IMAGE
export SECRETFLOW_IMAGE=$SECRETFLOW_IMAGE
# deploy master
if [[ ${mode} == "master" ]]; then
  docker run --rm -v $(pwd):/tmp/kuscia $KUSCIA_IMAGE cp -f /home/kuscia/scripts/deploy/start_standalone.sh /tmp/kuscia
  echo "bash $(pwd)/start_standalone.sh -u web"
  bash $(pwd)/start_standalone.sh -u web
  exit 0
fi

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

if [[ ${install_dir} == "" ]]; then
  install_dir=${LITE_INSTALL_DIR}
fi

# set intall dir of the deploy.sh
# the datapath is ${ROOT}/kuscia-${deploy_mode}-${DOMAIN_ID}-data
# the certpath is ${ROOT}/kuscia-${deploy_mode}-${DOMAIN_ID}-certs
export ROOT=${install_dir}

cmd_opt="-n ${domain_id} -m ${master_endpoint} -t ${token} -p ${domain_host_port}"

if [[ ${domain_certs_dir} != "" ]]; then
  cmd_opt="${cmd_opt} -c ${domain_certs_dir}"
fi

if [[ ${domain_host_ip} != "" ]]; then
  cmd_opt="${cmd_opt} -i ${domain_host_ip}"
fi

# copy deploy.sh from kuscia image
docker run --rm -v $(pwd):/tmp/kuscia $KUSCIA_IMAGE cp -f /home/kuscia/scripts/deploy/deploy.sh /tmp/kuscia
# execute deploy lite shell
echo "bash $(pwd)/deploy.sh lite ${cmd_opt}"
bash $(pwd)/deploy.sh lite ${cmd_opt}