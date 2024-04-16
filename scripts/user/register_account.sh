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

# global configuration
DB_PATH="/app/db/secretpad.sqlite"
LOG_FILE_PATH_DEFAULT="/app/log/init.log"
USER_NAME=""
USER_PASSWD=""
OWNER_TYPE="CENTEr"
OWNER_ID="kuscia-system"
# log func
function log() {
	echo -e "\033[32m[INFO]\033[0m\033[36m[$(date +%y/%m/%d-%H:%M:%S)]\033[0m $1"
	if [ -x "$LOG_FILE_PATH_DEFAULT" ]; then
		echo -e "\033[32m[INFO]\033[0m\033[36m[$(date +%y/%m/%d-%H:%M:%S)]\033[0m $1" >>"$LOG_FILE_PATH_DEFAULT"
	fi
}

function account_settings() {
	local RET
	for ((i = 0; i < 3; i++)); do
		read -r -p "Enter username: " USER_NAME
		check_user_name "${USER_NAME}"
		RET=$?
		if [ "${RET}" -eq 0 ]; then
			break
		elif [ "${RET}" -ne 0 ] && [ "${i}" == 2 ]; then
			exit 3
		fi
	done
	stty -echo # echo not display the input(password)
	for ((i = 0; i < 3; i++)); do
		read -r -p "Enter password: " USER_PASSWD
		echo ""
		check_user_passwd "${USER_PASSWD}"
		RET=$?
		if [ "${RET}" -eq 0 ]; then
			local CONFIRM_PASSWD
			read -r -p "Confirm password again: " CONFIRM_PASSWD
			if [ "${CONFIRM_PASSWD}" == "${USER_PASSWD}" ]; then
				break
			else
				log "Password not match!"
				exit 3
			fi
		elif [ "${RET}" -ne 0 ] && [ "${i}" == 2 ]; then
			exit 3
		fi
	done
	stty echo # echo display user input
	password_hash=$(echo -n "${USER_PASSWD}" | sha256sum | cut -d " " -f 1)
	log "password_hash = ${password_hash}"
	sqlite3 ${DB_PATH} "insert into user_accounts (name, password_hash) values (${USER_NAME}, ${password_hash})"
	echo ""
	log "User = ${USER_NAME}}, password = ${USER_PASSWD}"
	log "User ${USER_NAME} is set!"
}

function register() {
	local RET
	check_user_name "${USER_NAME}"
	OWNER_TYPE=$(echo "${OWNER_TYPE}" | tr '[:lower:]' '[:upper:]')
	password_hash=$(echo -n "${USER_PASSWD}" | sha256sum | cut -d " " -f 1)
	log "username = ${USER_NAME}, password_hash = ${password_hash}, owner_type = ${OWNER_TYPE}, owner_id = ${OWNER_ID}"
	sqlite3 ${DB_PATH} "insert into user_accounts (name, password_hash, owner_type, owner_id) values ('${USER_NAME}', '${password_hash}', '${OWNER_TYPE}', '${OWNER_ID}' );"
	log "User ${USER_NAME} is set!"
}

function check_user_name() {
	strlen=$(echo "${USER_NAME}" | grep -E --color '^(.{4,}).*$')
	if [ -n "${strlen}" ]; then
		return 0
	else
		log "Please use a username with its length greater than 4."
		exit 1
	fi
}

function check_user_passwd() {
	# The password requires a length greater than 8
	str_len=$(echo "$USER_PASSWD" | grep -E --color '^(.{8,}).*$')
	# including lowercase letters
	str_low=$(echo "$USER_PASSWD" | grep -E --color '^(.*[a-z]+).*$')
	# including uppercase letters
	str_upp=$(echo "$USER_PASSWD" | grep -E --color '^(.*[A-Z]).*$')
	# including special characters
	str_ts=$(echo "$USER_PASSWD" | grep -E --color '^(.*\W).*$')
	# including numbers
	str_num=$(echo "$USER_PASSWD" | grep -E --color '^(.*[0-9]).*$')
	if [ -n "${str_len}" ] && [ -n "${str_low}" ] && [ -n "${str_upp}" ] && [ -n "${str_ts}" ] && [ -n "${str_num}" ]; then
		return 0
	else
		log "The password requires a length greater than 8, including uppercase and lowercase letters, numbers, and special characters."
		exit 2
	fi
}

show_help() {
	cat <<EOF
Usage:
  register_account.sh [Options]

Options:
  -h, --help                This little help.
  -n, --name                username
  -p, --password            your password

Examples:
  bash register_account.sh -n 'admin' -p 'xxx'

Note:
  !! Use single quote when pass the content or the special characters in password will be ignored.

EOF
}

function check_inputs() {
	if [ "${USER_NAME}" == "" ] || [ "${USER_PASSWD}" == "" ]; then
		show_help
		log "[error]: username or password not set!"
		exit 3
	fi
}

while getopts 'hn:p:t:o:' OPT; do
	case ${OPT} in
	h)
		show_help
		exit 1
		;;
	n)
		USER_NAME="${OPTARG}"
		log "USER_NAME = ${OPTARG}"
		;;
	p)
		USER_PASSWD="${OPTARG}"
		log "pwd = ${USER_PASSWD}"
		;;
	t)
		OWNER_TYPE=$(echo "${OPTARG}" | tr '[:lower:]' '[:upper:]')
		log "type = ${OWNER_TYPE}"
		;;
	o)
		OWNER_ID="${OPTARG}"
		log "OWNER_ID = ${OWNER_ID}"
		;;
	?) show_help ;;
	esac
done

check_inputs
check_user_name
check_user_passwd
register
