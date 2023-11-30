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

alter table user_accounts add column owner_type varchar(16) not null default 'CENTER';
alter table user_accounts add column owner_id varchar(64) not null default 'kuscia-system';
alter table user_tokens add column session_data text null;


create table if not exists sys_resource (
    id            integer primary key autoincrement,
    resource_type varchar(16) not null ,                          -- comment 'INTERFACE|NODE'
    resource_code varchar(64) not null UNIQUE,                   -- comment '{Code} or ALL'
    resource_name varchar(64),
    is_deleted    tinyint(1)  default '0' not null,               -- delete flag
    gmt_create    datetime    default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified  datetime    default CURRENT_TIMESTAMP not null  -- modified time
);
create index `key_resource_type` on sys_resource (`resource_type`);


create table if not exists  sys_role (
    id            integer primary key autoincrement,
    role_code     varchar(64) unique not null,
    role_name     varchar(64),
    is_deleted    tinyint(1)  default '0' not null,               -- delete flag
    gmt_create    datetime    default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified  datetime    default CURRENT_TIMESTAMP not null  -- modified time
);


create table if not exists sys_role_resource_rel (
    id            integer primary key autoincrement,
    role_code     varchar(64) not null,
    resource_code varchar(64) not null,
    gmt_create    datetime    default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified  datetime    default CURRENT_TIMESTAMP not null  -- modified time
);
create unique index `uniq_role_code_resource_code` on sys_role_resource_rel (`role_code`, `resource_code`);


create table if not exists sys_user_permission_rel (
    id           integer primary key autoincrement,
    user_type    varchar(16) not null,
    user_key     varchar(64) not null,
    target_type  varchar(16) not null default 'ROLE',
    target_code  varchar(16) not null,
    gmt_create   datetime    default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified datetime    default CURRENT_TIMESTAMP not null  -- modified time
);
create unique index `uniq_user_key_target_code` on sys_user_permission_rel (`user_key`, `target_code`);


create table if not exists sys_user_node_rel (
    id           integer primary key autoincrement,
    user_id      varchar(64) not null,
    node_id      varchar(64) not null,
    gmt_create   datetime    default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified datetime    default CURRENT_TIMESTAMP not null  -- modified time
);
create unique index `uniq_user_id_node_id` on sys_user_node_rel (`user_id`, `node_id`);


create table if not exists `vote_request`
(
  id                    integer primary key autoincrement,
  vote_id               varchar(64)  not null,
  initiator             varchar(64)  not null,
  type                  varchar(16)  not null,
  voters                text         not null,
  vote_counter          varchar(64)  not null,
  executors             text         not null,
  approved_threshold    integer      not null,
  request_msg           text         default null,
  status                tinyint(1)   not null,
  execute_status        varchar(16)  default 'COMMITTED' not null,
  msg                   text         default null,
  desc                  varchar(64)  not null,
  is_deleted            tinyint(1)   default '0' not null,
  gmt_create            datetime     default CURRENT_TIMESTAMP not null,
  gmt_modified          datetime     default CURRENT_TIMESTAMP not null
);
create unique index `upk_vote_id` on vote_request (`vote_id`);

create table if not exists `tee_download_approval_config`
(
  id                    integer primary key autoincrement,
  vote_id               varchar(64) not null,
  task_id               varchar(64) not null,
  job_id                varchar(64) not null,
  resource_id           varchar(64) not null,
  resource_type         varchar(16) not null,
  project_id            varchar(64) not null,
  graph_id              varchar(64) not null,
  all_participants      text        not null,                           -- contains initiator and all inviters
  is_deleted            tinyint(1)  default '0' not null,               -- delete flag
  gmt_create            datetime    default CURRENT_TIMESTAMP not null, -- create time
  gmt_modified          datetime    default CURRENT_TIMESTAMP not null  -- modified time
);
create unique index `upk_tee_download_approval_config_vote_id` on tee_download_approval_config (`vote_id`);

create table if not exists `node_route_approval_config`
(
  id                    integer primary key autoincrement,
  vote_id               varchar(64) not null,
  is_single             tinyint(1)  not null,
  src_node_id           varchar(64) not null,
  src_node_addr         varchar(64) not null,
  des_node_id           varchar(64) not null,
  des_node_addr         varchar(64) not null,
  all_participants      text        not null,                           -- contains initiator and all inviters
  is_deleted            tinyint(1)  default '0' not null,               -- delete flag
  gmt_create            datetime    default CURRENT_TIMESTAMP not null, -- create time
  gmt_modified          datetime    default CURRENT_TIMESTAMP not null  -- modified time
);
create unique index `upk_node_route_approval_config_vote_id` on node_route_approval_config (`vote_id`);

create table if not exists `vote_invite`
(
  id                    integer primary key autoincrement,
  vote_id               varchar(64)  not null,
  initiator             varchar(64)  not null,
  vote_participant_id   varchar(64)  not null,
  type                  varchar(16)  not null,
  vote_msg              text         default null,
  action                varchar(16)  default 'REVIEWING',
  reason                varchar(64)  default null,
  desc                  varchar(64)  not null,
  is_deleted            tinyint(1)   default '0' not null,               -- delete flag
  gmt_create            datetime     default CURRENT_TIMESTAMP not null, -- create time
  gmt_modified          datetime     default CURRENT_TIMESTAMP not null  -- modified time
);
create unique index `upk_vote_invite_participant_id` on vote_invite (`vote_id`,`vote_participant_id`);

create table if not exists `tee_node_datatable_management`
(
    id            integer primary key autoincrement,
    node_id       varchar(64) not null,
    tee_node_id   varchar(64) not null,
    datatable_id  varchar(64) not null,
    datasource_id varchar(64) not null,
    kind          varchar(16) not null,                           -- datatable manage operate kind
    job_id        varchar(64) not null,                           -- datatable manage TEE job id
    status        varchar(32) not null,                           -- datatable manage TEE job status
    err_msg       text,                                           -- datatable manage TEE job errorMsg
    operate_info  text,                                           -- datatable manage operate information
    is_deleted    tinyint(1)  default '0' not null,               -- delete flag
    gmt_create    datetime    default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified  datetime    default CURRENT_TIMESTAMP not null  -- modified time
);
create unique index `upk_node_datatable_management` on tee_node_datatable_management (`node_id`, `tee_node_id`, `datatable_id`, `job_id`);




-- resource
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'ALL_INTERFACE_RESOURCE','ALL_INTERFACE_RESOURCE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_UPDATE','NODE_UPDATE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_CREATE','NODE_CREATE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_PAGE','NODE_PAGE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_GET','NODE_GET');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_DELETE','NODE_DELETE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_TOKEN','NODE_TOKEN');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_NEW_TOKEN','NODE_NEW_TOKEN');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_REFRESH','NODE_REFRESH');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_LIST','NODE_LIST');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_RESULT_LIST','NODE_RESULT_LIST');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_RESULT_DETAIL','NODE_RESULT_DETAIL');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'DATA_CREATE','DATA_CREATE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'DATA_CREATE_DATA','DATA_CREATE_DATA');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'DATA_UPLOAD','DATA_UPLOAD');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'DATA_DOWNLOAD','DATA_DOWNLOAD');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'DATA_LIST_DATASOURCE','DATA_LIST_DATASOURCE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'DATATABLE_LIST','DATATABLE_LIST');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'DATATABLE_GET','DATATABLE_GET');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'DATATABLE_DELETE','DATATABLE_DELETE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'GRAPH_COMM_I18N','GRAPH_COMM_I18N');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'GRAPH_COMM_LIST','GRAPH_COMM_LIST');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'GRAPH_COMM_GET','GRAPH_COMM_GET');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'GRAPH_COMM_BATH','GRAPH_COMM_BATH');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'GRAPH_CREATE','GRAPH_CREATE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'GRAPH_DELETE','GRAPH_DELETE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'GRAPH_META_UPDATE','GRAPH_META_UPDATE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'GRAPH_UPDATE','GRAPH_UPDATE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'GRAPH_LIST','GRAPH_LIST');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'GRAPH_NODE_UPDATE','GRAPH_NODE_UPDATE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'GRAPH_START','GRAPH_START');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'GRAPH_NODE_STATUS','GRAPH_NODE_STATUS');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'GRAPH_STOP','GRAPH_STOP');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'GRAPH_DETAIL','GRAPH_DETAIL');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'GRAPH_NODE_OUTPUT','GRAPH_NODE_OUTPUT');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'GRAPH_NODE_LOGS','GRAPH_NODE_LOGS');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'INDEX','INDEX');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_ROUTE_CREATE','NODE_ROUTE_CREATE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_ROUTE_PAGE','NODE_ROUTE_PAGE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_ROUTE_GET','NODE_ROUTE_GET');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_ROUTE_UPDATE','NODE_ROUTE_UPDATE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_ROUTE_LIST_NODE','NODE_ROUTE_LIST_NODE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_ROUTE_REFRESH','NODE_ROUTE_REFRESH');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_ROUTE_DELETE','NODE_ROUTE_DELETE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'PRJ_CREATE','PRJ_CREATE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'PRJ_LIST','PRJ_LIST');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'PRJ_GET','PRJ_GET');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'PRJ_UPDATE','PRJ_UPDATE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'PRJ_DELETE','PRJ_DELETE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'PRJ_ADD_INST','PRJ_ADD_INST');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'PRJ_ADD_NODE','PRJ_ADD_NODE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'PRJ_ADD_TABLE','PRJ_ADD_TABLE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'PRJ_DATATABLE_DELETE','PRJ_DATATABLE_DELETE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'PRJ_DATATABLE_GET','PRJ_DATATABLE_GET');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'PRJ_JOB_LIST','PRJ_JOB_LIST');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'PRJ_JOB_GET','PRJ_JOB_GET');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'PRJ_JOB_STOP','PRJ_JOB_STOP');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'PRJ_TASK_LOGS','PRJ_TASK_LOGS');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'PRJ_TASK_OUTPUT','PRJ_TASK_OUTPUT');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'USER_CREATE','USER_CREATE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'USER_GET','USER_GET');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'REMOTE_USER_RESET_PWD','REMOTE_USER_RESET_PWD');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'REMOTE_USER_CREATE','REMOTE_USER_CREATE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'REMOTE_USER_LIST_BY_NODE','REMOTE_USER_LIST_BY_NODE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_USER_RESET_PWD','NODE_USER_RESET_PWD');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_USER_CREATE','NODE_USER_CREATE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_USER_LIST_BY_NODE','NODE_USER_LIST_BY_NODE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_USER_NODE_LIST','NODE_USER_NODE_LIST');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_USER_REMOTE_NODE_LIST','NODE_USER_REMOTE_NODE_LIST');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_USER_OTHER_NODE_BASE_INFO_LIST','NODE_USER_OTHER_NODE_BASE_INFO_LIST');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'AUTH_LOGIN','AUTH_LOGIN');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'AUTH_LOGOUT','AUTH_LOGOUT');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'ENV_GET','ENV_GET');

-----------
--  role --
insert into sys_role(role_code, role_name) values('ADMIN', '管理员');
insert into sys_role(role_code, role_name) values('EDGE_NODE', 'Edge node rpc');
insert into sys_role(role_code, role_name) values('EDGE_USER', 'Edge 用户');

-----------
-- role resource --
------------
-- Center admin user
insert into sys_role_resource_rel(role_code, resource_code) values('CENTER_ADMIN', 'ALL_INTERFACE_RESOURCE');
-- Edge user common
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_NODE', 'INDEX');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_NODE', 'ENV_GET');
insert into sys_role_resource_rel(role_code, resource_code) values('EDGE_USER', 'INDEX');
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
