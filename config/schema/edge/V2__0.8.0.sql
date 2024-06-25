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

-- project_graph_domain_datasource
create table if not exists `project_graph_domain_datasource`
(
    id               integer primary key autoincrement,
    project_id       varchar(64)                               not null, -- project_id
    graph_id         varchar(64)                               not null, -- graph_id
    domain_id        varchar(64)                               not null, -- domain_id
    data_source_id   varchar(64) default 'default-data-source' not null, -- data_source_id
    data_source_name varchar(64) default 'default-data-source' not null, -- data_source_name
    edit_enable      tinyint(1)  default '1'                   not null, -- edit enabled flag
    is_deleted       tinyint(1)  default '0'                   not null, -- delete flag
    gmt_create       datetime    default CURRENT_TIMESTAMP     not null, -- create time
    gmt_modified     datetime    default CURRENT_TIMESTAMP     not null  -- modified time
);

create unique index if not exists `upk_project_graph_domain`
    on project_graph_domain_datasource (`project_id`, `graph_id`, `domain_id`);


alter table feature_table add column datasource_id varchar(64) not null default "";
create unique index if not exists `upk_datasource_feature_table_id` on feature_table (`feature_table_id`, `node_id`, `datasource_id`);


alter table project_feature_table add column datasource_id varchar(64) not null default "";
drop index if exists `upk_project_feature_table_id`;
create unique index if not exists `upk_project_feature_table_id` on project_feature_table (`project_id`,`node_id`, `feature_table_id`, `datasource_id`);

insert or ignore into sys_role_resource_rel(role_code, resource_code)
values ('EDGE_NODE', 'DATATABLE_CREATE');

insert or ignore into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'DATATABLE_CREATE', 'DATATABLE_CREATE');