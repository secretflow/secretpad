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

github_flag=true
./scripts/build/build.sh ${github_flag}

DATETIME=$(date +"%Y%m%d%H%M%S")
git fetch --tags
# shellcheck disable=SC2046
# shellcheck disable=SC2006
VERSION_TAG="$(git describe --tags $(git rev-list --tags --max-count=1))"
commit_id=$(git log -n 1 --pretty=oneline | awk '{print $1}' | cut -b 1-6)
tag=${VERSION_TAG}-${DATETIME}-"${commit_id}"
local_image=secretpad:${tag}
echo "$commit_id"

BUILDER_EXISTS=$(
	docker buildx inspect secretpad_image_buildx >/dev/null 2>&1
	echo $?
)

if [ "$BUILDER_EXISTS" -eq 0 ]; then
	echo "existing buildx builder: secretpad_image_buildx"
	docker buildx use secretpad_image_buildx
else
	echo "creating new buildx builder: secretpad_image_buildx"
	docker buildx create --name secretpad_image_buildx --use
fi

if [[ "$github_flag" == "true" ]]; then
	echo "github_flag is true"
	docker buildx build \
		--platform linux/arm64,linux/amd64 \
		--tag "${local_image}" \
		-f ./build/Dockerfiles/anolis.Dockerfile . \
		--load
fi
