/*
 * Copyright 2023 Ant Group Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

-- delete p2p mode not support sql
delete from main.node_route where route_id = 1 or route_id = 2;
delete from main.node where name = 'alice' or name = 'bob';
delete from main.inst where name = 'alice' or name = 'bob';

--  sqlLite3 optimize
PRAGMA synchronous = NORMAL;
PRAGMA journal_mode = WAL;