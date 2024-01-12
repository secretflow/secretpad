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
SECRETPAD_ROOT=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd -P)

DB_PATH="$SECRETPAD_ROOT/db/secretpad.sqlite"
SCHEMA_PATH="$SECRETPAD_ROOT/config/schema/init.sql"

SCHEMA_P2P_PATH_V1="${SECRETPAD_ROOT}/config/schema/p2p/v1.sql"

rm -rf "$DB_PATH"

sqlite3 "$DB_PATH" ".read $SCHEMA_PATH"

sqlite3 "$DB_PATH" ".read $SCHEMA_P2P_PATH_V1"
