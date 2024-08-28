/*
 * Copyright 2024 Ant Group Co., Ltd.
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
alter table node add column inst_id varchar(64) not null default "";
alter table node add column inst_token varchar(300)  default "";
alter table node add column protocol varchar(32)  default "";

alter table user_accounts add column inst_id varchar(64) not null default "";

alter table project_approval_config add column participant_node_info text default '';

create unique index if not exists `upk_inst_node_id` on node (`inst_id`, `node_id`);
