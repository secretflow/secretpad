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

-- the `id` is integer because of AUTOINCREMENT is only allowed on an INTEGER PRIMARY KEY.
-- default: id is varchar(64), name is varchar(256).

create table if not exists `inst`
(
    id           integer primary key autoincrement,
    inst_id      varchar(64) not null,                            -- inst id
    name         varchar(256) default NULL,                       -- inst name
    is_deleted   tinyint(1)   default '0' not null,               -- delete flag
    gmt_create   datetime     default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified datetime     default CURRENT_TIMESTAMP not null  -- modified time
);
create unique index `upk_inst_id` on inst (`inst_id`);
create index `key_inst_name` on inst (`name`);

create table if not exists `node`
(
    id           integer primary key autoincrement,
    node_id      varchar(64)  not null,
    name         varchar(256) not null,
    auth         text,                                          -- auth
    description  text       default '',                         -- node description
    is_deleted   tinyint(1) default '0' not null,               -- delete flag
    gmt_create   datetime   default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified datetime   default CURRENT_TIMESTAMP not null  -- modified time
);
create unique index `upk_node_id` on node (`node_id`);
create index `key_node_name` on node (`name`);

create table if not exists `node_route`
(
    id           integer primary key autoincrement,
    src_node_id  varchar(64) not null,
    dst_node_id  varchar(64) not null,
    is_deleted   tinyint(1) default '0' not null,               -- delete flag
    gmt_create   datetime   default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified datetime   default CURRENT_TIMESTAMP not null  -- modified time
);
create unique index `upk_route_src_dst` on node_route (`src_node_id`, `dst_node_id`);
create index `key_router_src` on node_route (`src_node_id`);
create index `key_router_dst` on node_route (`dst_node_id`);

create table if not exists `project`
(
    id           integer primary key autoincrement,
    project_id   varchar(64)  not null,                         -- project ID
    name         varchar(256) not null,                         -- project name
    description  text       default '',                         -- project description
    is_deleted   tinyint(1) default '0' not null,               -- delete flag
    gmt_create   datetime   default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified datetime   default CURRENT_TIMESTAMP not null  -- modified time
);
create unique index `upk_project_id` on project (`project_id`);
create index `key_project_name` on project (`name`);

create table if not exists `project_inst`
(
    id           integer primary key autoincrement,
    project_id   varchar(64) not null,
    inst_id      varchar(64) not null,
    is_deleted   tinyint(1) default '0' not null,               -- delete flag
    gmt_create   datetime   default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified datetime   default CURRENT_TIMESTAMP not null  -- modified time
);
create unique index `upk_project_inst_id` on project_inst (`project_id`, `inst_id`);

create table if not exists `project_node`
(
    id           integer primary key autoincrement,
    project_id   varchar(64) not null,
    node_id      varchar(64) not null,
    is_deleted   tinyint(1) default '0' not null,               -- delete flag
    gmt_create   datetime   default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified datetime   default CURRENT_TIMESTAMP not null  -- modified time
);
create unique index `upk_project_node_id` on project_node (`project_id`, `node_id`);

create table if not exists `project_datatable`
(
    id            integer primary key autoincrement,
    project_id    varchar(64) not null,
    node_id       varchar(64) not null,
    datatable_id  varchar(64) not null,
    table_configs text        not null,                          -- project datatable config
    source        varchar(16) not null,                          -- project datatable from: IMPORTED/CREATED
    is_deleted    tinyint(1) default '0' not null,               -- delete flag
    gmt_create    datetime   default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified  datetime   default CURRENT_TIMESTAMP not null  -- modified time
);
create unique index `upk_project_datatable_id` on project_datatable (`project_id`, `node_id`, `datatable_id`);

create table if not exists `project_fed_table`
(
    id           integer primary key autoincrement,
    project_id   varchar(64) not null,
    fed_table_id varchar(64) not null,
    joins        text        not null,                          -- What tables are made up [{nodeId, datatableId}]
    is_deleted   tinyint(1) default '0' not null,               -- delete flag
    gmt_create   datetime   default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified datetime   default CURRENT_TIMESTAMP not null  -- modified time
);
create unique index `upk_project_fed_table_id` on project_fed_table (`project_id`, `fed_table_id`);

create table if not exists `project_model`
(
    id           integer primary key autoincrement,
    project_id   varchar(64) not null,
    model_id     varchar(64) not null,
    is_deleted   tinyint(1) default '0' not null,               -- delete flag
    gmt_create   datetime   default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified datetime   default CURRENT_TIMESTAMP not null  -- modified time
);
create unique index `upk_project_model_id` on project_model (`project_id`, `model_id`);

create table if not exists `project_rule`
(
    id           integer primary key autoincrement,
    project_id   varchar(64) not null,
    rule_id      varchar(64) not null,
    is_deleted   tinyint(1) default '0' not null,               -- delete flag
    gmt_create   datetime   default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified datetime   default CURRENT_TIMESTAMP not null  -- modified time
);
create unique index `upk_project_rule_id` on project_rule (`project_id`, `rule_id`);

create table if not exists `project_report`
(
    id           integer primary key autoincrement,
    project_id   varchar(64) not null,
    report_id    varchar(64) not null,
    content      varchar(64) not null,                          -- report info
    is_deleted   tinyint(1) default '0' not null,               -- delete flag
    gmt_create   datetime   default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified datetime   default CURRENT_TIMESTAMP not null  -- modified time
);
create unique index `upk_project_report_id` on project_report (`project_id`, `report_id`);

create table if not exists `project_result`
(
    id           integer primary key autoincrement,
    project_id   varchar(64) not null,
    kind         varchar(16) not null,                          -- project result type: model\fed_table\rule\report_table # TODO: depend on data object
    node_id      varchar(64) not null,                          -- node_id
    ref_id       varchar(64) not null,                          -- generated resource Id
    job_id       varchar(64),                                   -- create by job id
    task_id      varchar(64),                                   -- create by task id
    is_deleted   tinyint(1) default '0' not null,               -- delete flag
    gmt_create   datetime   default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified datetime   default CURRENT_TIMESTAMP not null  -- modified time
);
create unique index `upk_project_result_kind_node_ref_id` on project_result (`project_id`, `kind`, `node_id`, `ref_id`);

create table if not exists `project_job`
(
    id            integer primary key autoincrement,
    project_id    varchar(64) not null,
    job_id        varchar(64) not null,
    `name`        varchar(40) not null,                          -- Job name
    status        varchar(32) not null,                          -- Job status
    err_msg       text,                                          -- err_msg
    graph_id      varchar(64),                                   -- 由哪个graph创建
    edges         text,                                          -- create by graph edges
    finished_time datetime   default null,                       -- finished_time
    is_deleted    tinyint(1) default '0' not null,               -- delete flag
    gmt_create    datetime   default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified  datetime   default CURRENT_TIMESTAMP not null  -- modified time
);
create unique index `upk_project_job_id` on project_job (`project_id`, `job_id`);
create unique index `upk_job_id` on project_job (`job_id`); -- Kuscia，Job unique

create table if not exists `project_job_task`
(
    id            integer primary key autoincrement,
    project_id    varchar(64) not null,
    job_id        varchar(64) not null,
    task_id       varchar(64) not null,
    parties       text        not null,
    status        varchar(32) not null,                          -- Task status
    err_msg       text,                                          -- err_msg
    graph_node_id varchar(64),                                   -- create by graph node
    graph_node    text,                                          -- graph node coordinate，x,y,code_name, label
    is_deleted    tinyint(1) default '0' not null,               -- delete flag
    gmt_create    datetime   default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified  datetime   default CURRENT_TIMESTAMP not null  -- modified time
);
create unique index `upk_project_job_task_id` on project_job_task (`project_id`, `job_id`, `task_id`);

create table if not exists `project_graph`
(
    id           integer primary key autoincrement,
    project_id   varchar(64) not null,
    graph_id     varchar(64) not null,
    name         varchar(128),
    edges        text,
    is_deleted   tinyint(1) default '0' not null,               -- delete flag
    gmt_create   datetime   default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified datetime   default CURRENT_TIMESTAMP not null  -- modified time
);
create unique index `upk_project_graph` on project_graph (`project_id`, `graph_id`);

create table if not exists `project_graph_node`
(
    id            integer primary key autoincrement,
    project_id    varchar(64) not null,
    graph_id      varchar(64) not null,
    graph_node_id varchar(64) not null,
    code_name     varchar(64),
    label         varchar(64),
    x             integer,
    y             integer,
    inputs        text,
    outputs       text,
    node_def      text,
    is_deleted    tinyint(1) default '0' not null,               -- delete flag
    gmt_create    datetime   default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified  datetime   default CURRENT_TIMESTAMP not null  -- modified time
);
create unique index `upk_project_graph_node` on project_graph_node (`project_id`, `graph_id`, `graph_node_id`);

create table if not exists `project_job_task_log`
(
    id         integer primary key autoincrement,
    project_id varchar(64) not null,
    job_id     varchar(64) not null,
    task_id    varchar(64) not null,
    log        text        not null,
    gmt_create datetime default CURRENT_TIMESTAMP not null -- create time
);
create index `idx_project_job_task_log` on project_job_task_log (`project_id`, `job_id`, `task_id`);

create table if not exists 'user_accounts'
(
    id            integer primary key autoincrement,
    name          varchar(128) not null,                         -- username
    password_hash varchar(128) not null,                         -- password_hash
    is_deleted    tinyint(1) default '0' not null,               -- delete flag
    gmt_create    datetime   default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified  datetime   default CURRENT_TIMESTAMP not null  -- modified time
);

create table if not exists 'user_tokens'
(
    id           integer primary key autoincrement,
    name         varchar(128) not null,                          -- username
    token        varchar(64) default null,                       -- login token
    gmt_token    datetime    default null,                       -- token effective time
    is_deleted   tinyint(1)  default '0' not null,               -- delete flag
    gmt_create   datetime    default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified datetime    default CURRENT_TIMESTAMP not null, -- modified time
    CONSTRAINT 'fk_name' FOREIGN KEY ('name') REFERENCES user_accounts ('name')
);

-------- add mock data --------------

insert into main.inst (inst_id, name, is_deleted, gmt_create, gmt_modified)
values ('alice', 'alice', 0, '1683391498000', '1683391503000'),
       ('bob', 'bob', 0, '1683391498000', '1683391503000');

insert into main.node (node_id, name, auth, description, is_deleted, gmt_create, gmt_modified)
values ('alice', 'alice', 'alice', 'alice', 0, '2023-05-11 11:54:08', '2023-05-11 11:54:08'),
       ('bob', 'bob', 'bob', 'bob', 0, '2023-05-11 11:54:08', '2023-05-11 11:54:08');

insert into main.node_route (src_node_id, dst_node_id, is_deleted, gmt_create, gmt_modified)
values ('alice', 'bob', 0, '2023-05-11 11:54:08', '2023-05-11 11:54:08'),
       ('bob', 'alice', 0, '2023-05-11 11:54:08', '2023-05-11 11:54:08');
