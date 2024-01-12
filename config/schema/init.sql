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
    id              integer primary key autoincrement,
    node_id         varchar(64)  not null,
    name            varchar(256) not null,
    auth            text,                                           -- ca
    description     text        default '',                         -- description
    control_node_id varchar(64)  not null,                          -- node control id
    net_address     varchar(100),                                   -- node net address
    token           varchar(100),                                   -- aging token used by node deploy
    type            varchar(10) default 'normal',                   -- node type :embedded、normal
    mode            integer     default '0' not null,               -- node feature 0 - mpc | 1 - tee | 2 mpc&tee
    master_node_id  varchar(64) default 'master',                   -- master node id
    is_deleted      tinyint(1)  default '0' not null,               -- delete flag
    gmt_create      datetime    default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified    datetime    default CURRENT_TIMESTAMP not null  -- modified time
);
create unique index `upk_node_id` on node (`node_id`);
create index `key_node_name` on node (`name`);

create table if not exists `node_route`
(
    id              integer primary key autoincrement,
    route_id        varchar(64)  not null,
    src_node_id     varchar(64)  not null,
    dst_node_id     varchar(64)  not null,
    src_net_address varchar(100),                                  -- node net address
    dst_net_address varchar(100),                                  -- cooperate node net address
    is_deleted      tinyint(1) default '0' not null,               -- delete flag
    gmt_create      datetime   default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified    datetime   default CURRENT_TIMESTAMP not null  -- modified time
);
create unique index `upk_route_src_dst` on node_route (`src_node_id`, `dst_node_id`);
create index `key_router_src` on node_route (`src_node_id`);
create index `key_router_dst` on node_route (`dst_node_id`);

-- project - new project and project info add compute_mode enum: mpc,tee  todo: mpc may be not suitable
create table if not exists `project`
(
    id           integer primary key autoincrement,
    project_id   varchar(64)  not null,                           -- project ID
    name         varchar(256) not null,                           -- project name
    compute_mode varchar(64)  default 'mpc' not null,             -- compute_mode: mpc,tee
    compute_func varchar(64)  default 'ALL' not null,             -- compute_func: DAG,PSI,ALL
    project_info text         default '' not null,                -- tee dag runtimeParams
    description  text         default '',                         -- project description
    owner_id     varchar(64)  not null default '',
    status       tinyint(1)   default '0' not null,               -- archive flag
    is_deleted   tinyint(1)   default '0' not null,               -- delete flag
    gmt_create   datetime     default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified datetime     default CURRENT_TIMESTAMP not null  -- modified time
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
    graph_id      varchar(64),                                   -- uniq graph id
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
    owner_id     varchar(64)  not null default '',
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
    owner_type    varchar(16)  not null default 'CENTER',
    owner_id      varchar(64)  not null default 'kuscia-system',
    passwd_reset_failed_attempts  integer     default null,  -- modified time
    gmt_passwd_reset_release       datetime     default null,  -- modified time
    is_deleted    tinyint(1)   default '0' not null,               -- delete flag
    gmt_create    datetime     default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified  datetime     default CURRENT_TIMESTAMP not null,  -- modified time
    failed_attempts  integer     default null,  -- modified time
    locked_invalid_time  datetime     default null  -- modified time
);

create table if not exists 'user_tokens'
(
    id           integer primary key autoincrement,
    name         varchar(128) not null,                          -- username
    token        varchar(64)  default null,                       -- login token
    gmt_token    datetime     default null,                       -- token effective time
    session_data text         default null,
    is_deleted   tinyint(1)   default '0' not null,               -- delete flag
    gmt_create   datetime     default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified datetime     default CURRENT_TIMESTAMP not null, -- modified time
    CONSTRAINT 'fk_name' FOREIGN KEY ('name') REFERENCES user_accounts ('name')
);

create table if not exists `tee_node_datatable_management`
(
    id            integer primary key autoincrement,
    node_id       varchar(64) not null,
    tee_node_id   varchar(64) not null,
    datatable_id  varchar(64) not null,
    datasource_id varchar(64) not null,
    kind          varchar(16) not null,                          -- datatable manage operate kind
    job_id        varchar(64) not null,                          -- datatable manage TEE job id
    status        varchar(32) not null,                          -- datatable manage TEE job status
    err_msg       text,                                          -- datatable manage TEE job errorMsg
    operate_info  text,                                          -- datatable manage operate information
    is_deleted    tinyint(1) default '0' not null,               -- delete flag
    gmt_create    datetime  default CURRENT_TIMESTAMP not null,  -- create time
    gmt_modified  datetime  default CURRENT_TIMESTAMP not null   -- modified time
);
create unique index `upk_node_datatable_management` on tee_node_datatable_management (`node_id`, `tee_node_id`, `datatable_id`, `job_id`);

create table if not exists `edge_data_sync_log`
(
    table_name       varchar(64) primary key not null,
    last_update_time varchar(64)             not null
);

CREATE table if not exists sys_resource (
    id            integer primary key autoincrement,
    resource_type  varchar(16) not null , -- comment 'INTERFACE|NODE'
    resource_code VARCHAR (64) not null UNIQUE, -- comment '{Code} or ALL'
    resource_name VARCHAR (64),
    is_deleted      tinyint(1)  default '0' not null,               -- delete flag
    gmt_create      datetime    default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified    datetime    default CURRENT_TIMESTAMP not null -- modified time
);
create index `key_resource_type` on sys_resource (`resource_type`);


CREATE table if not exists  sys_role (
    id            integer primary key autoincrement,
    role_code     VARCHAR (64) UNIQUE NOT NULL,
    role_name      VARCHAR (64),
    is_deleted      tinyint(1)  default '0' not null,               -- delete flag
    gmt_create      datetime    default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified    datetime    default CURRENT_TIMESTAMP not null -- modified time
);


CREATE TABLE sys_role_resource_rel (
    id            integer primary key autoincrement,
    role_code     VARCHAR (64) NOT NULL,
    resource_code VARCHAR (64) NOT NULL,
    gmt_create      datetime    default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified    datetime    default CURRENT_TIMESTAMP not null -- modified time
);
create unique INDEX `uniq_role_code_resource_code` on sys_role_resource_rel (`role_code`, `resource_code`);


CREATE TABLE sys_user_permission_rel (
    id            integer primary key autoincrement,
    user_type    VARCHAR (16)       NOT NULL,
    user_key     VARCHAR (64)      NOT NULL,
    target_type   VARCHAR (16) NOT NULL DEFAULT 'ROLE',
    target_code  VARCHAR (16) NOT NULL,
    gmt_create      datetime    default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified    datetime    default CURRENT_TIMESTAMP not null -- modified time
);
create unique INDEX `uniq_user_key_target_code` on sys_user_permission_rel (`user_key`, `target_code`);


CREATE TABLE sys_user_node_rel (
    id            integer primary key autoincrement,
    user_id    VARCHAR (64)       NOT NULL,
    node_id     VARCHAR (64)      NOT NULL,
    gmt_create      datetime    default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified    datetime    default CURRENT_TIMESTAMP not null -- modified time
);
create unique INDEX `uniq_user_id_node_id` on sys_user_node_rel (`user_id`, `node_id`);

create table if not exists `vote_request`
(
  id                    integer primary key autoincrement,
  vote_id               varchar(64)  not null, --unique vote id
  initiator             varchar(64)  not null, -- vote initiator
  type                  varchar(16)  not null, -- vote type
  voters                text         not null, -- voter inviters
  vote_counter          varchar(64)  not null, -- vote counter
  executors             text         not null, -- vote executors
  approved_threshold    integer      not null, -- the number of votes needed to succeed
  request_msg           text         default null, -- vote msg
  status                tinyint(1)   not null, --vote status
  execute_status        varchar(16)  default 'COMMITTED' not null, -- executors call back status
  msg                   text         default null,-- error msg
  party_vote_info       text        default null,
  desc                  varchar(64)  not null, --vote desc
  is_deleted            tinyint(1)   default '0' not null,
  gmt_create            datetime     default CURRENT_TIMESTAMP not null,
  gmt_modified          datetime     default CURRENT_TIMESTAMP not null
);
create unique index `upk_vote_id` on vote_request (`vote_id`);

create table if not exists `tee_download_approval_config`
(
  id                    integer primary key autoincrement,
  vote_id               varchar(64) not null, --unique vote id
  task_id               varchar(64) not null, --the resource task id
  job_id                varchar(64) not null, --the resource job id
  resource_id           varchar(64) not null, --resource id
  resource_type         varchar(16) not null, --resource type
  project_id            varchar(64) not null, --project id
  graph_id              varchar(64) not null, --graph id
  all_participants      text        not null, --contains initiator and all inviters
  is_deleted            tinyint(1)  default '0' not null,            -- delete flag
  gmt_create            datetime    default CURRENT_TIMESTAMP not null, -- create time
  gmt_modified          datetime    default CURRENT_TIMESTAMP not null  -- modified time
);
create unique index `upk_tee_download_approval_config_vote_id` on tee_download_approval_config (`vote_id`);

create table if not exists `node_route_approval_config`
(
  id                    integer primary key autoincrement,
  vote_id               varchar(64) not null, --unique vote id
  is_single             tinyint(1)  not null, --single node route flag
  src_node_id           varchar(64) not null, --src node id
  src_node_addr         varchar(64) not null, --src node address
  des_node_id           varchar(64) not null, --destination node id
  des_node_addr         varchar(64) not null, -- destination node addr
  all_participants      text        not null,--contains initiator and all inviters
  is_deleted            tinyint(1)  default '0' not null,            -- delete flag
  gmt_create            datetime    default CURRENT_TIMESTAMP not null, -- create time
  gmt_modified          datetime    default CURRENT_TIMESTAMP not null  -- modified time
);
create unique index `upk_node_route_approval_config_vote_id` on node_route_approval_config (`vote_id`);

create table if not exists `project_approval_config`
(
  id                    integer primary key autoincrement,
  vote_id               varchar(64)  not null, --unique vote id
  initiator             varchar(64)  not null, --vote initiator
  type                  varchar(16)  not null, --vote type
  parties				text      	 default null, --all parties node id
  project_id			varchar(64)	 default null,  --projectID
  invite_node_id		varchar(64)  default null,
  is_deleted            tinyint(1)   default '0' not null,            -- delete flag
  gmt_create            datetime     default CURRENT_TIMESTAMP not null, -- create time
  gmt_modified          datetime     default CURRENT_TIMESTAMP not null  -- modified time
);
create unique index `upk_project_approval_config_vote_id` on project_approval_config (`vote_id`);


create table if not exists `vote_invite`
(
    id                  integer primary key autoincrement,
    vote_id             varchar(64) not null,                           --unique vote id
    initiator           varchar(64) not null,                           --vote initiator
    vote_participant_id varchar(64) not null,                           --vote invitor id
    type                varchar(16) not null,                           -- vote type
    vote_msg            text        default null,                       -- vote msg
    action              varchar(16) default 'REVIEWING',                --vote action
    reason              varchar(64) default null,                       --reject reason
    desc                varchar(64) not null,                           -- vote desc
    is_deleted          tinyint(1)  default '0' not null,               -- delete flag
    gmt_create          datetime    default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified        datetime    default CURRENT_TIMESTAMP not null  -- modified time
);
create unique index `upk_vote_invite_participant_id` on vote_invite (`vote_id`, `vote_participant_id`);
-------- add mock data --------------

insert into main.inst (inst_id, name, is_deleted)
values ('alice', 'alice', 0),
       ('bob', 'bob', 0);

insert into main.node (node_id, name, control_node_id, auth, description, type, is_deleted, net_address, mode)
values ('alice', 'alice', 'alice', 'alice', 'alice', 'embedded', 0, '127.0.0.1:28080', 1),
       ('bob', 'bob', 'bob', 'bob', 'bob', 'embedded', 0, '127.0.0.1:38080', 1);

insert into main.node_route (route_id, src_node_id, dst_node_id, is_deleted, src_net_address, dst_net_address)
values (1, 'alice', 'bob', 0, '127.0.0.1:28080', '127.0.0.1:38080'),
       (2, 'bob', 'alice', 0, '127.0.0.1:38080', '127.0.0.1:28080');

-- resource
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'ALL_INTERFACE_RESOURCE', 'ALL_INTERFACE_RESOURCE');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'NODE_UPDATE', 'NODE_UPDATE');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'NODE_CREATE', 'NODE_CREATE');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'NODE_PAGE', 'NODE_PAGE');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'NODE_GET', 'NODE_GET');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'NODE_DELETE', 'NODE_DELETE');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'NODE_TOKEN', 'NODE_TOKEN');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'NODE_NEW_TOKEN', 'NODE_NEW_TOKEN');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'NODE_REFRESH', 'NODE_REFRESH');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'NODE_LIST', 'NODE_LIST');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'NODE_RESULT_LIST', 'NODE_RESULT_LIST');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'NODE_RESULT_DETAIL', 'NODE_RESULT_DETAIL');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'DATA_CREATE', 'DATA_CREATE');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'DATA_CREATE_DATA', 'DATA_CREATE_DATA');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'DATA_UPLOAD', 'DATA_UPLOAD');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'DATA_DOWNLOAD', 'DATA_DOWNLOAD');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'DATA_LIST_DATASOURCE', 'DATA_LIST_DATASOURCE');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'DATATABLE_LIST', 'DATATABLE_LIST');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'DATATABLE_GET', 'DATATABLE_GET');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'DATATABLE_DELETE', 'DATATABLE_DELETE');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'GRAPH_COMM_I18N', 'GRAPH_COMM_I18N');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'GRAPH_COMM_LIST', 'GRAPH_COMM_LIST');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'GRAPH_COMM_GET', 'GRAPH_COMM_GET');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'GRAPH_COMM_BATH', 'GRAPH_COMM_BATH');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'GRAPH_CREATE', 'GRAPH_CREATE');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'GRAPH_DELETE', 'GRAPH_DELETE');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'GRAPH_META_UPDATE', 'GRAPH_META_UPDATE');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'GRAPH_UPDATE', 'GRAPH_UPDATE');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'GRAPH_LIST', 'GRAPH_LIST');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'GRAPH_NODE_UPDATE', 'GRAPH_NODE_UPDATE');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'GRAPH_START', 'GRAPH_START');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'GRAPH_NODE_STATUS', 'GRAPH_NODE_STATUS');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'GRAPH_STOP', 'GRAPH_STOP');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'GRAPH_DETAIL', 'GRAPH_DETAIL');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'GRAPH_NODE_OUTPUT', 'GRAPH_NODE_OUTPUT');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'GRAPH_NODE_LOGS', 'GRAPH_NODE_LOGS');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'INDEX', 'INDEX');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'NODE_ROUTE_CREATE', 'NODE_ROUTE_CREATE');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'NODE_ROUTE_PAGE', 'NODE_ROUTE_PAGE');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'NODE_ROUTE_GET', 'NODE_ROUTE_GET');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'NODE_ROUTE_UPDATE', 'NODE_ROUTE_UPDATE');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'NODE_ROUTE_LIST_NODE', 'NODE_ROUTE_LIST_NODE');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'NODE_ROUTE_REFRESH', 'NODE_ROUTE_REFRESH');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'NODE_ROUTE_DELETE', 'NODE_ROUTE_DELETE');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'PRJ_CREATE', 'PRJ_CREATE');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'PRJ_LIST', 'PRJ_LIST');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'PRJ_GET', 'PRJ_GET');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'PRJ_UPDATE', 'PRJ_UPDATE');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'PRJ_DELETE', 'PRJ_DELETE');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'PRJ_ADD_INST', 'PRJ_ADD_INST');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'PRJ_ADD_NODE', 'PRJ_ADD_NODE');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'PRJ_ADD_TABLE', 'PRJ_ADD_TABLE');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'PRJ_DATATABLE_DELETE', 'PRJ_DATATABLE_DELETE');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'PRJ_DATATABLE_GET', 'PRJ_DATATABLE_GET');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'PRJ_JOB_LIST', 'PRJ_JOB_LIST');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'PRJ_JOB_GET', 'PRJ_JOB_GET');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'PRJ_JOB_STOP', 'PRJ_JOB_STOP');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'PRJ_TASK_LOGS', 'PRJ_TASK_LOGS');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'PRJ_TASK_OUTPUT', 'PRJ_TASK_OUTPUT');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'USER_CREATE', 'USER_CREATE');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'USER_GET', 'USER_GET');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'REMOTE_USER_RESET_PWD', 'REMOTE_USER_RESET_PWD');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'REMOTE_USER_CREATE', 'REMOTE_USER_CREATE');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'REMOTE_USER_LIST_BY_NODE', 'REMOTE_USER_LIST_BY_NODE');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'NODE_USER_RESET_PWD', 'NODE_USER_RESET_PWD');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'NODE_USER_CREATE', 'NODE_USER_CREATE');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'NODE_USER_LIST_BY_NODE', 'NODE_USER_LIST_BY_NODE');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'NODE_USER_NODE_LIST', 'NODE_USER_NODE_LIST');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'NODE_USER_REMOTE_NODE_LIST', 'NODE_USER_REMOTE_NODE_LIST');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'NODE_USER_OTHER_NODE_BASE_INFO_LIST', 'NODE_USER_OTHER_NODE_BASE_INFO_LIST');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'AUTH_LOGIN', 'AUTH_LOGIN');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'AUTH_LOGOUT', 'AUTH_LOGOUT');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'ENV_GET', 'ENV_GET');
insert into sys_resource(resource_type, resource_code, resource_name)
values ('API', 'USER_UPDATE_PWD', 'USER_UPDATE_PWD');


-----------
--  role --
insert into sys_role(role_code, role_name)
values ('ADMIN', '管理员');
insert into sys_role(role_code, role_name)
values ('EDGE_NODE', 'Edge node rpc');
insert into sys_role(role_code, role_name)
values ('EDGE_USER', 'Edge 用户');
insert into sys_role(role_code, role_name)
values('P2P_NODE', 'P2P 用户');

-----------
-- role resource --
------------
-- Center admin user
insert into sys_role_resource_rel(role_code, resource_code)
values ('CENTER_ADMIN', 'ALL_INTERFACE_RESOURCE');
-- Edge user common
insert into sys_role_resource_rel(role_code, resource_code)
values ('EDGE_NODE', 'INDEX');
insert into sys_role_resource_rel(role_code, resource_code)
values ('EDGE_NODE', 'ENV_GET');
insert into sys_role_resource_rel(role_code, resource_code)
values ('EDGE_USER', 'INDEX');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'ENV_GET');
-- Edge user manage
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_NODE', 'NODE_USER_CREATE');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_NODE', 'NODE_USER_RESET_PWD');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_NODE', 'NODE_USER_LIST_BY_NODE');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'NODE_USER_CREATE');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'NODE_USER_RESET_PWD');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'NODE_USER_LIST_BY_NODE');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'NODE_USER_NODE_LIST');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'NODE_USER_REMOTE_NODE_LIST');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_NODE', 'NODE_USER_OTHER_NODE_BASE_INFO_LIST');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_NODE', 'DATA_CREATE_DATA');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_NODE', 'PRJ_ADD_TABLE');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_NODE', 'NODE_UPDATE');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_NODE', 'NODE_ROUTE_UPDATE');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_NODE', 'NODE_ROUTE_DELETE');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_NODE', 'DATATABLE_DELETE');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_NODE', 'PRJ_DATATABLE_DELETE');

-- Edge user login permission
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'AUTH_LOGIN');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'AUTH_LOGOUT');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'USER_GET');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'USER_UPDATE_PWD');
-- Edge user permission
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'GRAPH_COMM_BATH');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'GRAPH_COMM_GET');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'GRAPH_COMM_I18N');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'GRAPH_COMM_LIST');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'GRAPH_CREATE');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'GRAPH_DELETE');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'GRAPH_DETAIL');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'GRAPH_LIST');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'GRAPH_META_UPDATE');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'GRAPH_NODE_LOGS');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'GRAPH_NODE_OUTPUT');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'GRAPH_NODE_STATUS');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'GRAPH_NODE_UPDATE');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'GRAPH_START');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'GRAPH_STOP');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'GRAPH_UPDATE');

insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'PRJ_ADD_INST');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'PRJ_ADD_NODE');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'PRJ_ADD_TABLE');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'PRJ_CREATE');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'PRJ_DATATABLE_DELETE');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'PRJ_DATATABLE_GET');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'PRJ_DELETE');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'PRJ_GET');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'PRJ_JOB_GET');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'PRJ_JOB_LIST');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'PRJ_JOB_STOP');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'PRJ_LIST');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'PRJ_TASK_LOGS');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'PRJ_TASK_OUTPUT');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'PRJ_UPDATE');

