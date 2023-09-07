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
echo "cp install.sh secretflow-allinone-package/"
cp install.sh secretflow-allinone-package/
# copy uninstall.sh
echo "cp uninstall.sh secretflow-allinone-package/"
cp uninstall.sh secretflow-allinone-package/
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

echo "kuscia image: $KUSCIA_IMAGE"
echo "secretpad image: $SECRETPAD_IMAGE"
echo "secretflow image: $SECRETFLOW_IMAGE"

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

kusciaTag=${KUSCIA_IMAGE##*:}
echo "kuscia tag: $kusciaTag"
secretpadTag=${SECRETPAD_IMAGE##*:}
echo "secretpad tag: $secretpadTag"
secretflowTag=${SECRETFLOW_IMAGE##*:}
echo "secretflow tag: $secretflowTag"
VERSION_TAG="$(git describe --tags)"
echo "secretflow-allinone-package tag: $VERSION_TAG"

echo "docker save -o ./secretflow-allinone-package/images/kuscia-${kusciaTag}.tar ${KUSCIA_IMAGE} "
docker save -o ./secretflow-allinone-package/images/kuscia-${kusciaTag}.tar ${KUSCIA_IMAGE}

echo "docker save -o ./secretflow-allinone-package/images/secretpad-${secretpadTag}.tar ${SECRETPAD_IMAGE} "
docker save -o ./secretflow-allinone-package/images/secretpad-${secretpadTag}.tar ${SECRETPAD_IMAGE}

echo "docker save -o ./secretflow-allinone-package/images/secretflow-${secretflowTag}.tar ${SECRETFLOW_IMAGE} "
docker save -o ./secretflow-allinone-package/images/secretflow-${secretflowTag}.tar ${SECRETFLOW_IMAGE}

echo "tar --no-xattrs -zcvf secretflow-allinone-package-${VERSION_TAG}.tar.gz ./secretflow-allinone-package"
tar --no-xattrs -zcvf secretflow-allinone-package-${VERSION_TAG}.tar.gz ./secretflow-allinone-package
echo "package done"
