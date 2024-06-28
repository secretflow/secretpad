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

-- feature_table_new
create table if not exists `feature_table_new`
(
    id                 integer primary key autoincrement,
    feature_table_id   varchar(8)                            not null,                -- feature table id
    feature_table_name varchar(32)                           not null,                -- feature table name
    node_id            varchar(64)                           not null,                -- node_id
    type               varchar(8)                            not null default 'HTTP', -- feature datasource type
    description        varchar(64) default null,                                      -- feature table desc
    url                varchar(64)                           not null,                -- feature table service addr
    columns            text                                  not null,                -- feature table columns
    status             varchar(16)                           not null,                -- Available,Unavailable
    is_deleted         tinyint(1)  default '0'               not null,                -- delete flag
    gmt_create         datetime    default CURRENT_TIMESTAMP not null,                -- create time
    gmt_modified       datetime    default CURRENT_TIMESTAMP not null,                 -- modified time
    datasource_id      varchar(64) default "http-data-source" not null                --default datasource
);
-- finish create feature_table_new

-- syn data  ignore  datasource_id use default_value
insert or ignore into feature_table_new (id,feature_table_id,feature_table_name,node_id,type, description,url,columns,status,is_deleted,gmt_create,gmt_modified)
    SELECT id,feature_table_id,feature_table_name,node_id,type, description,url,columns,status,is_deleted,gmt_create,gmt_modified
    FROM feature_table;

-- rename old
alter table feature_table rename to feature_table_backup;
-- remove old index
drop index upk_feature_table_id;
-- remove old index
drop index upk_datasource_feature_table_id;

-- rename new
alter table feature_table_new rename to feature_table;

-- index rebuild
create unique index if not exists  `upk_feature_table_id` on feature_table (`feature_table_id`);
-- index rebuild
create unique index if not exists `upk_datasource_feature_table_id` on feature_table (`feature_table_id`, `node_id`, `datasource_id`);


-- project_feature_table_new
create table if not exists `project_feature_table_new`
(
    id               integer primary key autoincrement,
    project_id       varchar(64)                          not null,
    node_id          varchar(64)                          not null,
    feature_table_id varchar(64)                          not null,
    table_configs    text                                 not null, -- project feature table config
    source           varchar(16)                          not null, -- project feature table from: IMPORTED/CREATED
    is_deleted       tinyint(1) default '0'               not null, -- delete flag
    gmt_create       datetime   default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified     datetime   default CURRENT_TIMESTAMP not null,  -- modified time
    datasource_id      varchar(64) default "http-data-source" not null  --default datasource
);

insert or ignore into project_feature_table_new (id, project_id, node_id, feature_table_id,table_configs, source, is_deleted, gmt_create, gmt_modified)
    SELECT id, project_id, node_id, feature_table_id, table_configs, source, is_deleted, gmt_create, gmt_modified
    FROM project_feature_table;


-- rename old
alter table project_feature_table rename to project_feature_table_backup;
-- remove old index
drop index upk_project_feature_table_id;

-- rename new
alter table project_feature_table_new rename to project_feature_table;

-- index rebuild
create unique index if not exists `upk_project_feature_table_id` on project_feature_table (`project_id`, `node_id`, `feature_table_id`);
