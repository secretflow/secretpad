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

set -ex

./scripts/build.sh true

DATETIME=$(date +"%Y%m%d%H%M%S")
git fetch --tags
VERSION_TAG="$(git describe --abbrev=0)"
commit_id=$(git log -n 1  --pretty=oneline | awk '{print $1}' | cut -b 1-6)
tag=${VERSION_TAG}-${DATETIME}-"${commit_id}"
local_image=secretpad:$tag
echo "$commit_id"
docker build -f ./build/Dockerfiles/anolis.Dockerfile --platform linux/amd64 -t "$local_image" .
