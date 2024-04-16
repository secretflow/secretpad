#!/bin/bash
#
# Copyright 2024 Ant Group Co., Ltd.
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

SECRETPAD_ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

echo "root path : ${SECRETPAD_ROOT_DIR}"

SECRETPAD_SUREFIRE_REPORT="${SECRETPAD_ROOT_DIR}"/surefire-report
SECRETPAD_SUREFIRE_REPORT_XML="${SECRETPAD_ROOT_DIR}"/test/target/TEST-secretpad.xml

rm -rf "$SECRETPAD_SUREFIRE_REPORT"
mkdir -p "$SECRETPAD_SUREFIRE_REPORT"

cp "${SECRETPAD_ROOT_DIR}"/secretpad-api/client-java-kusciaapi/target/surefire-reports/*.xml "$SECRETPAD_SUREFIRE_REPORT"/ >/dev/null || :
cp "${SECRETPAD_ROOT_DIR}"/secretpad-api/client-java-secretpad/target/surefire-reports/*.xml "$SECRETPAD_SUREFIRE_REPORT"/ >/dev/null || :
cp "${SECRETPAD_ROOT_DIR}"/secretpad-common/target/surefire-reports/*.xml "$SECRETPAD_SUREFIRE_REPORT"/ >/dev/null || :
cp "${SECRETPAD_ROOT_DIR}"/secretpad-manager/target/surefire-reports/*.xml "$SECRETPAD_SUREFIRE_REPORT"/ >/dev/null || :
cp "${SECRETPAD_ROOT_DIR}"/secretpad-persistence/target/surefire-reports/*.xml "$SECRETPAD_SUREFIRE_REPORT"/ >/dev/null || :
cp "${SECRETPAD_ROOT_DIR}"/secretpad-service/target/surefire-reports/*.xml "$SECRETPAD_SUREFIRE_REPORT"/ >/dev/null || :
cp "${SECRETPAD_ROOT_DIR}"/secretpad-web/target/surefire-reports/*.xml "$SECRETPAD_SUREFIRE_REPORT"/ >/dev/null || :

touch "${SECRETPAD_SUREFIRE_REPORT_XML}"
echo '<?xml version="1.0" encoding="UTF-8"?><testsuites>' >"${SECRETPAD_SUREFIRE_REPORT_XML}"

for file in "$SECRETPAD_SUREFIRE_REPORT"/*; do
	if test -f "$file"; then
		# shellcheck disable=SC2046
		# shellcheck disable=SC2005
		echo $(tail -n +2 "$file") >>"${SECRETPAD_SUREFIRE_REPORT_XML}"
	fi
done
echo '</testsuites>' >>"${SECRETPAD_SUREFIRE_REPORT_XML}"
rm -rf "$SECRETPAD_SUREFIRE_REPORT"
