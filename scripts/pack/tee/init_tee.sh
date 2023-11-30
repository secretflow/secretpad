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

function log() {
  local log_content=$1
  echo -e "${GREEN}${log_content}${NC}"
}

#TEE_APP_IMAGE=""
#TEE_DM_IMAGE=""
#CAPSULE_MANAGER_SIM_IMAGE=""

master="${USER}-kuscia-master"
tee="${USER}-kuscia-lite-tee"
alice="${USER}-kuscia-lite-alice"
bob="${USER}-kuscia-lite-bob"
CTR_ROOT=/home/kuscia

tee_app_image_tar=""
tee_dm_image_tar=""
capsule_manager_sim_image_tar=""

function init_images_version_from_files() {
    for file in images/*; do
    	if [ -f "$file" ]; then
    		someimage=$(basename "$file")
    	  echo "echo ${someimage}"
        if [[ $someimage == *teeapps-sim* ]]; then
    			tee_app_image_tar=$someimage
    			cp images/$tee_app_image_tar /tmp/$tee_app_image_tar

    	  elif [[ $someimage == *sf-tee-dm-sim* ]]; then
    			tee_dm_image_tar=$someimage
    			cp images/$tee_dm_image_tar /tmp/$tee_dm_image_tar
    	  elif [[ $someimage == *capsule-manager-sim* ]]; then
    		  capsule_manager_sim_image_tar=$someimage
    		  cp images/$capsule_manager_sim_image_tar /tmp/$capsule_manager_sim_image_tar
        fi
      fi
    done
}

function save_image() {
  local image=$1
  image_id=$(docker images --filter="reference=${image}" --format "{{.ID}}")
  image_tar=/tmp/$(echo ${image} | sed 's/\//_/g').${image_id}.tar
  if [ ! -e $image_tar ]; then
    docker save $image -o $image_tar
  fi
  return $image_tar
}

function loadTeeImage2Container() {
  local container_id=$1
  local image_tar=$2
  docker exec -it $container_id ctr -a=${CTR_ROOT}/containerd/run/containerd.sock -n=k8s.io images import $image_tar
}


function loadFormLocal() {

  init_images_version_from_files

  if [ -e "/tmp/$tee_app_image_tar" ]; then
    log "loadTeeImage2Container $tee /tmp/$tee_app_image_tar"
    loadTeeImage2Container $tee /tmp/$tee_app_image_tar
  else
    log "Image tar '/tmp/$tee_app_image_tar' not exists!"
  fi

  if [ -e "/tmp/$tee_dm_image_tar" ]; then
    log "loadTeeImage2Container $tee /tmp/$tee_dm_image_tar"
    loadTeeImage2Container $tee /tmp/$tee_dm_image_tar

    log "loadTeeImage2Container $alice /tmp/$tee_dm_image_tar"
    loadTeeImage2Container $alice /tmp/$tee_dm_image_tar

    log "loadTeeImage2Container $bob /tmp/$tee_dm_image_tar"
    loadTeeImage2Container $bob /tmp/$tee_dm_image_tar
  else
    log "Image tar '/tmp/$tee_dm_image_tar' not exists!"
  fi

  if [ -e "/tmp/$capsule_manager_sim_image_tar" ]; then
    log "loadTeeImage2Container $tee /tmp/$capsule_manager_sim_image_tar"
    loadTeeImage2Container $tee /tmp/$capsule_manager_sim_image_tar
  else
    log "Image tar '/tmp/$capsule_manager_sim_image_tar' not exists!"
  fi
}

function saveAndLoad2Container() {
  local image_basename=$1
  local container_id=$2

  image_tar_path=$(save_image $image_basename)
  log "Save image: $image_basename path: $image_tar_path"
  log "loadTeeImage2Container $container_id $image_tar_path"

  loadTeeImage2Container $container_id $image_tar_path
  log "Successfully imported image '${image_basename}' to container '${container_id}' ..."

}

function loadFormOnline() {
  docker pull $TEE_DM_IMAGE
  docker pull $TEE_APP_IMAGE
  docker pull $CAPSULE_MANAGER_SIM_IMAGE

  saveAndLoad2Container $TEE_APP_IMAGE $tee

  saveAndLoad2Container $TEE_DM_IMAGE $tee
  saveAndLoad2Container $TEE_DM_IMAGE $alice
  saveAndLoad2Container $TEE_DM_IMAGE $bob

  saveAndLoad2Container $CAPSULE_MANAGER_SIM_IMAGE $tee
}

function run_tee() {
  if [ ! -d "images/" ]; then
    echo "load tee images from online"
    loadFormOnline
  else
    echo "local tee images from ./images"
    loadFormLocal
  fi
}

function loadCrdInfo() {
  echo "echo TEE_DM_IMAGE $TEE_DM_IMAGE "
  echo "echo TEE_APP_IMAGE $TEE_APP_IMAGE "

  if [[ $CAPSULE_MANAGER_SIM_IMAGE == *aliyuncs* ]]
  then
    echo "CAPSULE_MANAGER_SIM_IMAGE $CAPSULE_MANAGER_SIM_IMAGE"
  else
    CAPSULE_MANAGER_SIM_IMAGE="docker.io/"$CAPSULE_MANAGER_SIM_IMAGE
    echo "CAPSULE_MANAGER_SIM_IMAGE $CAPSULE_MANAGER_SIM_IMAGE"
  fi

  sed "s|{{.USER}}|${USER}|g;
  s|{{.TEE_CAPSULE_MANAGER_SIM_IMAGE}}|${CAPSULE_MANAGER_SIM_IMAGE}|g" \
  tee-capsule-manager.yaml >tee-capsule-manager-0.yaml

  if [[ $TEE_DM_IMAGE == *aliyuncs* ]]
    then
      echo "TEE_DM_IMAGE $TEE_DM_IMAGE"
    else
      TEE_DM_IMAGE="docker.io/"$TEE_DM_IMAGE
      echo "TEE_DM_IMAGE $TEE_DM_IMAGE"
  fi

  if [[ $TEE_APP_IMAGE == *aliyuncs* ]]
      then
        echo "TEE_APP_IMAGE $TEE_APP_IMAGE"
      else
        TEE_APP_IMAGE="docker.io/"$TEE_APP_IMAGE
        echo "TEE_APP_IMAGE $TEE_APP_IMAGE"
  fi
  echo "echo TEE_DM_IMAGE $TEE_DM_IMAGE "
  TEE_DM_SIM_IMAGE_TAG=${TEE_DM_IMAGE##*:}
  echo "echo TEE_DM_SIM_IMAGE_TAG $TEE_DM_SIM_IMAGE_TAG "
  TEE_DM_SIM_IMAGE_NAME=$(echo "$TEE_DM_IMAGE" | sed "s/:${TEE_DM_SIM_IMAGE_TAG}//")
  echo "echo TEE_DM_SIM_IMAGE_NAME $TEE_DM_SIM_IMAGE_NAME "

  echo "echo TEE_APP_IMAGE $TEE_APP_IMAGE "
  TEEAPPS_SIM_IMAGE_TAG=${TEE_APP_IMAGE##*:}
  echo "echo TEEAPPS_SIM_IMAGE_TAG $TEEAPPS_SIM_IMAGE_TAG "
  TEEAPPS_SIM_IMAGE_NAME=$(echo "$TEE_APP_IMAGE" | sed "s/:${TEEAPPS_SIM_IMAGE_TAG}//")
  echo "echo TEEAPPS_SIM_IMAGE_NAME $TEEAPPS_SIM_IMAGE_NAME "

  sed "s|{{.TEE_DM_SIM_IMAGE_NAME}}|${TEE_DM_SIM_IMAGE_NAME}|g;
  s|{{.TEE_DM_SIM_IMAGE_TAG}}|${TEE_DM_SIM_IMAGE_TAG}|g;
  s|{{.TEEAPPS_SIM_IMAGE_NAME}}|${TEEAPPS_SIM_IMAGE_NAME}|g;
  s|{{.TEEAPPS_SIM_IMAGE_TAG}}|${TEEAPPS_SIM_IMAGE_TAG}|g" \
  tee-image.yaml >tee-image-0.yaml
}

function init_tee_crd() {
  loadCrdInfo
  echo "echo docker cp tee-image.yaml  $master:/home/kuscia"
  docker cp tee-image-0.yaml  $master:/home/kuscia
  echo "echo docker exec -it $master kubectl apply -f tee-image.yaml"
  docker exec -it $master kubectl apply -f tee-image-0.yaml

  echo "echo docker cp tee-capsule-manager.yaml  $master:/home/kuscia"
  docker cp tee-capsule-manager-0.yaml  $master:/home/kuscia
  echo "echo docker exec -it $master kubectl apply -f tee-image.yaml"
  docker exec -it $master kubectl apply -f tee-capsule-manager-0.yaml
}

echo "create domain route: alice -> tee"
docker exec -it $master sh scripts/deploy/create_cluster_domain_route.sh alice tee http://${tee}:1080
echo "create domain route: tee -> alice"
docker exec -it $master sh scripts/deploy/create_cluster_domain_route.sh tee alice http://${alice}:1080
echo "create domain route: bob -> tee"
docker exec -it $master sh scripts/deploy/create_cluster_domain_route.sh bob tee   http://${tee}:1080
echo "create domain route: tee -> bob"
docker exec -it $master sh scripts/deploy/create_cluster_domain_route.sh tee bob   http://${bob}:1080

echo "run_tee..."
run_tee
init_tee_crd

echo "Successfully init tee app image"