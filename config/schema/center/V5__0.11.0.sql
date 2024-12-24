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

alter table project_job_task add column extra_info varchar(100)  default "";
alter table node add column inst_token_state varchar(10) default "UNUSED";

insert or ignore into sys_resource(resource_type, resource_code, resource_name)
values
    ('API', 'SCHEDULED_ID', 'SCHEDULED_ID'),
    ('API', 'SCHEDULED_CREATE', 'SCHEDULED_CREATE'),
    ('API', 'SCHEDULED_PAGE', 'SCHEDULED_PAGE'),
    ('API', 'SCHEDULED_OFFLINE', 'SCHEDULED_OFFLINE')
;
insert or ignore into sys_role_resource_rel(role_code, resource_code)
values
    ('EDGE_USER', 'SCHEDULED_ID'),
    ('EDGE_USER', 'SCHEDULED_CREATE'),
    ('EDGE_USER', 'SCHEDULED_PAGE'),
    ('EDGE_USER', 'SCHEDULED_OFFLINE')
;
