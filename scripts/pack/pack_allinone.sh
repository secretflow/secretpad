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
SECRETFLOW_SERVING_IMAGE=""

TEE_APP_IMAGE=""
TEE_DM_IMAGE=""
CAPSULE_MANAGER_SIM_IMAGE=""

GREEN='\033[0;32m'
NC='\033[0m'
function log() {
	local log_content=$1
	echo -e "${GREEN}${log_content}${NC}"
}

# create dir
echo "mkdir -p secretflow-allinone-package/images"
mkdir -p secretflow-allinone-package/images

# copy install.sh
path="$(
	cd "$(dirname $0)"
	pwd
)"
echo "cp install.sh secretflow-allinone-package/"
cp "$path"/install.sh secretflow-allinone-package/
# copy uninstall.sh
echo "cp uninstall.sh secretflow-allinone-package/"
cp "$path"/uninstall.sh secretflow-allinone-package/
# copy tee init
echo "cp tee/init_tee.sh secretflow-allinone-package/tee/"
cp "$path"/tee/init_tee.sh secretflow-allinone-package/
cp "$path"/tee/tee-capsule-manager.yaml secretflow-allinone-package/
cp "$path"/tee/tee-image.yaml secretflow-allinone-package/

# remove temp data
echo "rm -rf secretflow-allinone-package/images/*"
rm -rf secretflow-allinone-package/images/*

if [ "${KUSCIA_IMAGE}" == "" ]; then
	KUSCIA_IMAGE=secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/kuscia:latest
fi

if [ "${SECRETPAD_IMAGE}" == "" ]; then
	SECRETPAD_IMAGE=secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/secretpad:latest
fi

if [ "${SECRETFLOW_IMAGE}" == "" ]; then
	SECRETFLOW_IMAGE=secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/secretflow-lite-anolis8:latest
fi
# tee images
if [ "${TEE_APP_IMAGE}" == "" ]; then
	TEE_APP_IMAGE=secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/teeapps-sim:latest
fi

if [ "${TEE_DM_IMAGE}" == "" ]; then
	TEE_DM_IMAGE=secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/sf-tee-dm-sim:latest
fi

if [ "${CAPSULE_MANAGER_SIM_IMAGE}" == "" ]; then
	CAPSULE_MANAGER_SIM_IMAGE=secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/capsule-manager-sim:latest
fi
if [ "${SECRETFLOW_SERVING_IMAGE}" == "" ]; then
	SECRETFLOW_SERVING_IMAGE=secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/serving-anolis8:latest
fi

echo "kuscia image: $KUSCIA_IMAGE"
echo "secretpad image: $SECRETPAD_IMAGE"
echo "secretflow image: $SECRETFLOW_IMAGE"
echo "secretflow serving image: $SECRETFLOW_SERVING_IMAGE"

echo "TEE_APP_IMAGE image: $TEE_APP_IMAGE"
echo "TEE_DM_IMAGE image: $TEE_DM_IMAGE"
echo "CAPSULE_MANAGER_SIM_IMAGE image: $CAPSULE_MANAGER_SIM_IMAGE"

set -e
echo "docker pull ${KUSCIA_IMAGE}"
docker pull ${KUSCIA_IMAGE}
log "docker pull ${KUSCIA_IMAGE} done"
echo "docker pull ${SECRETPAD_IMAGE}"
docker pull ${SECRETPAD_IMAGE}
log "docker pull ${SECRETPAD_IMAGE} done"
echo "docker pull ${SECRETFLOW_IMAGE}"
docker pull ${SECRETFLOW_IMAGE}
log "docker pull ${SECRETFLOW_IMAGE} done"
docker pull --platform=linux/amd64 ${SECRETFLOW_SERVING_IMAGE}
log "docker pull ${SECRETFLOW_SERVING_IMAGE} done"

# tee
docker pull ${TEE_APP_IMAGE}
log "docker pull ${TEE_APP_IMAGE} done"
docker pull ${TEE_DM_IMAGE}
log "docker pull ${TEE_DM_IMAGE} done"
docker pull ${CAPSULE_MANAGER_SIM_IMAGE}
log "docker pull ${CAPSULE_MANAGER_SIM_IMAGE} done"

kusciaTag=${KUSCIA_IMAGE##*:}
echo "kuscia tag: $kusciaTag"
secretpadTag=${SECRETPAD_IMAGE##*:}
echo "secretpad tag: $secretpadTag"
secretflowTag=${SECRETFLOW_IMAGE##*:}
echo "secretflow tag: $secretflowTag"
secretflowServingTag=${SECRETFLOW_SERVING_IMAGE##*:}
echo "secretflow serving tag: $secretflowServingTag"

teeAppTag=${TEE_APP_IMAGE##*:}
echo "tee app tag: $teeAppTag"
teeDmTag=${TEE_DM_IMAGE##*:}
echo "teeDmTag: $teeDmTag"
capsuleManagerSimTag=${CAPSULE_MANAGER_SIM_IMAGE##*:}
echo "capsuleManagerSimTag: $capsuleManagerSimTag"

VERSION_TAG="$(git describe --tags)"
echo "secretflow-allinone-package tag: $VERSION_TAG"

echo "docker save -o ./secretflow-allinone-package/images/kuscia-${kusciaTag}.tar ${KUSCIA_IMAGE} "
docker save -o ./secretflow-allinone-package/images/kuscia-${kusciaTag}.tar ${KUSCIA_IMAGE}

echo "docker save -o ./secretflow-allinone-package/images/secretpad-${secretpadTag}.tar ${SECRETPAD_IMAGE} "
docker save -o ./secretflow-allinone-package/images/secretpad-${secretpadTag}.tar ${SECRETPAD_IMAGE}

echo "docker save -o ./secretflow-allinone-package/images/secretflow-${secretflowTag}.tar ${SECRETFLOW_IMAGE} "
docker save -o ./secretflow-allinone-package/images/secretflow-${secretflowTag}.tar ${SECRETFLOW_IMAGE}

echo "docker save -o ./secretflow-allinone-package/images/serving-${secretflowServingTag}.tar ${SECRETFLOW_SERVING_IMAGE} "
docker save -o ./secretflow-allinone-package/images/serving-${secretflowServingTag}.tar ${SECRETFLOW_SERVING_IMAGE}

echo "docker save -o ./secretflow-allinone-package/images/teeapps-sim-${teeAppTag}.tar ${TEE_APP_IMAGE} "
docker save -o ./secretflow-allinone-package/images/teeapps-sim-${teeAppTag}.tar ${TEE_APP_IMAGE}

echo "docker save -o ./secretflow-allinone-package/images/sf-tee-dm-sim-${teeDmTag}.tar ${TEE_DM_IMAGE} "
docker save -o ./secretflow-allinone-package/images/sf-tee-dm-sim-${teeDmTag}.tar ${TEE_DM_IMAGE}

echo "docker save -o ./secretflow-allinone-package/image/capsule-manager-sim-${capsuleManagerSimTag}.tar ${CAPSULE_MANAGER_SIM_IMAGE} "
docker save -o ./secretflow-allinone-package/images/capsule-manager-sim-${capsuleManagerSimTag}.tar ${CAPSULE_MANAGER_SIM_IMAGE}

echo "tar --no-xattrs -zcvf secretflow-allinone-package-${VERSION_TAG}.tar.gz ./secretflow-allinone-package"
tar --no-xattrs -zcvf secretflow-allinone-package-${VERSION_TAG}.tar.gz ./secretflow-allinone-package
echo "package done"
