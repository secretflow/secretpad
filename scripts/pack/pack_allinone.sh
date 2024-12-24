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

# shellcheck disable=SC2223
: ${KUSCIA_IMAGE:="secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/kuscia:0.13.0b0"}
: ${SECRETPAD_IMAGE:="secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/secretpad:0.12.0b0"}
: ${SECRETFLOW_IMAGE:="secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/secretflow-lite-anolis8:1.11.0b1"}
: ${SECRETFLOW_SERVING_IMAGE:="secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/serving-anolis8:0.8.0b0"}
: ${TEE_APP_IMAGE:="secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/teeapps-sim-ubuntu20.04:0.1.2b0"}
: ${TEE_DM_IMAGE:="secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/sf-tee-dm-sim:0.1.0b0"}
: ${CAPSULE_MANAGER_SIM_IMAGE:="secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/capsule-manager-sim-ubuntu20.04:v0.1.0b0"}
: ${DATAPROXY_IMAGE:="secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/dataproxy:0.3.0b0"}
: ${SCQL_IMAGE:="secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/scql:0.9.2b1"}

GREEN='\033[0;32m'
NC='\033[0m'
function log() {
	local log_content=$1
	echo -e "${GREEN}${log_content}${NC}"
}

NEED_TEE=true
NEED_SCQL=true
MVP_TAR_SUFFIX="linux-x86_64"

if [ -z "$1" ]; then
	platform="linux/amd64"
	echo "platform is empty, make default $platform"
else
	if [ "$1" != "linux/amd64" ] && [ "$1" != "linux/arm64" ]; then
		echo "platform is invalid, linux/amd64 or linux/arm64"
		exit 1
	fi
	platform=$1
fi
if [ "$platform" = "linux/arm64" ]; then
	NEED_TEE=false
	NEED_SCQL=false
	MVP_TAR_SUFFIX="linux-aarch_64"
fi
echo "== start build $platform , TEE included:$NEED_TEE, SCQL included:$NEED_SCQL"

# create dir
echo "mkdir -p secretflow-allinone-package/images"
mkdir -p secretflow-allinone-package/images

# copy install.sh
path="$(
	cd "$(dirname "$0")"
	pwd
)"
echo "cp install.sh secretflow-allinone-package/"
cp "$path"/../install.sh secretflow-allinone-package/
# copy uninstall.sh
echo "cp uninstall.sh secretflow-allinone-package/"
cp "$path"/../uninstall.sh secretflow-allinone-package/

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
if [ "${DATAPROXY_IMAGE}" == "" ]; then
	DATAPROXY_IMAGE=secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/dataproxy:latest
fi
if [ "${SCQL_IMAGE}" == "" ]; then
	SCQL_IMAGE=secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/scql:latest
fi

echo "kuscia image: $KUSCIA_IMAGE"
echo "secretpad image: $SECRETPAD_IMAGE"
echo "secretflow image: $SECRETFLOW_IMAGE"
echo "secretflow serving image: $SECRETFLOW_SERVING_IMAGE"
echo "dataproxy image: $DATAPROXY_IMAGE"

set -e
echo "docker pull --platform=$platform ${KUSCIA_IMAGE}"
docker pull --platform=$platform ${KUSCIA_IMAGE}
log "docker pull --platform=$platform ${KUSCIA_IMAGE} done"

echo "docker pull --platform=$platform ${SECRETPAD_IMAGE}"
docker pull --platform=$platform ${SECRETPAD_IMAGE}
log "docker pull --platform=$platform ${SECRETPAD_IMAGE} done"

echo "docker pull --platform=$platform ${SECRETFLOW_IMAGE}"
docker pull --platform=$platform ${SECRETFLOW_IMAGE}
log "docker pull --platform=$platform ${SECRETFLOW_IMAGE} done"

echo "docker pull --platform=$platform ${SECRETFLOW_SERVING_IMAGE}"
docker pull --platform=$platform ${SECRETFLOW_SERVING_IMAGE}
log "docker pull --platform=$platform ${SECRETFLOW_SERVING_IMAGE} done"

echo "docker pull --platform=$platform ${DATAPROXY_IMAGE}"
docker pull --platform=$platform ${DATAPROXY_IMAGE}
log "docker pull --platform=$platform ${DATAPROXY_IMAGE} done"

kusciaTag=${KUSCIA_IMAGE##*:}
echo "kuscia tag: $kusciaTag"
secretpadTag=${SECRETPAD_IMAGE##*:}
echo "secretpad tag: $secretpadTag"
secretflowTag=${SECRETFLOW_IMAGE##*:}
echo "secretflow tag: $secretflowTag"
secretflowServingTag=${SECRETFLOW_SERVING_IMAGE##*:}
echo "secretflow serving tag: $secretflowServingTag"
dataproxyTag=${DATAPROXY_IMAGE##*:}
echo "dataproxy tag: $dataproxyTag"

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

echo "docker save -o ./secretflow-allinone-package/images/dataproxy-${dataproxyTag}.tar ${DATAPROXY_IMAGE} "
docker save -o ./secretflow-allinone-package/images/dataproxy-${dataproxyTag}.tar ${DATAPROXY_IMAGE}


# scql
if [ "$NEED_SCQL" = "true" ]; then
	echo "scql is needed"
	echo "SCQL_IMAGE image: $SCQL_IMAGE"

	echo "docker pull --platform=$platform ${SCQL_IMAGE}"
	docker pull --platform=$platform ${SCQL_IMAGE}
	log "docker pull --platform=$platform ${SCQL_IMAGE} done"

	scqlTag=${SCQL_IMAGE##*:}
	echo "scql tag: $scqlTag"

	echo "docker save -o ./secretflow-allinone-package/images/scql-${scqlTag}.tar ${SCQL_IMAGE} "
	docker save -o ./secretflow-allinone-package/images/scql-${scqlTag}.tar ${SCQL_IMAGE}
fi

# tee
if [ "$NEED_TEE" = "true" ]; then
	echo "tee is needed"
	echo "TEE_APP_IMAGE image: $TEE_APP_IMAGE"
	echo "TEE_DM_IMAGE image: $TEE_DM_IMAGE"
	echo "CAPSULE_MANAGER_SIM_IMAGE image: $CAPSULE_MANAGER_SIM_IMAGE"

	echo "docker pull --platform=$platform ${TEE_APP_IMAGE}"
	docker pull --platform=$platform ${TEE_APP_IMAGE}
	log "docker pull --platform=$platform ${TEE_APP_IMAGE} done"

	echo "docker pull --platform=$platform ${TEE_DM_IMAGE}"
	docker pull --platform=$platform ${TEE_DM_IMAGE}
	log "docker pull --platform=$platform ${TEE_DM_IMAGE} done"

	echo "docker pull --platform=$platform ${CAPSULE_MANAGER_SIM_IMAGE}"
	docker pull --platform=$platform ${CAPSULE_MANAGER_SIM_IMAGE}
	log "docker pull --platform=$platform ${CAPSULE_MANAGER_SIM_IMAGE} done"

	teeAppTag=${TEE_APP_IMAGE##*:}
	echo "tee app tag: $teeAppTag"
	teeDmTag=${TEE_DM_IMAGE##*:}
	echo "teeDmTag: $teeDmTag"
	capsuleManagerSimTag=${CAPSULE_MANAGER_SIM_IMAGE##*:}
	echo "capsuleManagerSimTag: $capsuleManagerSimTag"

	echo "docker save -o ./secretflow-allinone-package/images/teeapps-sim-${teeAppTag}.tar ${TEE_APP_IMAGE} "
	docker save -o ./secretflow-allinone-package/images/teeapps-sim-${teeAppTag}.tar ${TEE_APP_IMAGE}

	echo "docker save -o ./secretflow-allinone-package/images/sf-tee-dm-sim-${teeDmTag}.tar ${TEE_DM_IMAGE} "
	docker save -o ./secretflow-allinone-package/images/sf-tee-dm-sim-${teeDmTag}.tar ${TEE_DM_IMAGE}

	echo "docker save -o ./secretflow-allinone-package/image/capsule-manager-sim-${capsuleManagerSimTag}.tar ${CAPSULE_MANAGER_SIM_IMAGE} "
	docker save -o ./secretflow-allinone-package/images/capsule-manager-sim-${capsuleManagerSimTag}.tar ${CAPSULE_MANAGER_SIM_IMAGE}
fi

echo "tar --no-xattrs -zcvf secretflow-allinone-${MVP_TAR_SUFFIX}-v${VERSION_TAG}.tar.gz ./secretflow-allinone-package"
tar --no-xattrs -zcvf secretflow-allinone-${MVP_TAR_SUFFIX}-v"${VERSION_TAG}".tar.gz ./secretflow-allinone-package

echo "package done"
