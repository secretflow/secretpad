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

-- the `id` is integer because of AUTOINCREMENT is only allowed on an INTEGER PRIMARY KEY.
-- default: id is varchar(64), name is varchar(256).

create database if not exists `secretpad`
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

use `secretpad`;

create table if not exists `inst`
(
    `id`           int auto_increment PRIMARY KEY,
    `inst_id`      varchar(64) not null, -- inst id
    `name`         varchar(256) default null, -- inst name
    `is_deleted`   tinyint(1) default '0' not null, -- delete flag
    `gmt_create`   datetime default CURRENT_TIMESTAMP not null, -- create time
    `gmt_modified` datetime default CURRENT_TIMESTAMP not null  -- modified time
);
create unique index `upk_inst_id` on `inst` (`inst_id`);
create index `key_inst_name` on `inst` (`name`);


create table if not exists `node`
(
    `id`              int auto_increment primary key,
    `node_id`         varchar(64) not null,
    `name`            varchar(256) not null,
    `auth`            text, -- ca
    `description`     text default null, -- description
    `control_node_id` varchar(64) not null, -- node control id
    `net_address`     varchar(100), -- node net address
    `token`           varchar(100), -- aging token used by node deploy
    `type`            varchar(10) default 'normal', -- node type :embedded、normal
    `mode`            int default '0' not null, -- node feature 0 - mpc | 1 - tee | 2 mpc&tee
    `master_node_id`  varchar(64) default 'master', -- master node id
    `is_deleted`      tinyint(1) default '0' not null, -- delete flag
    `gmt_create`      datetime default current_timestamp not null, -- create time
    `gmt_modified`    datetime default current_timestamp not null -- modified time
);

create unique index `upk_node_id` on `node` (`node_id`);
create index `key_node_name` on `node` (`name`);
create table if not exists `node_route`
(
    `id`              int auto_increment primary key,
    `route_id`        varchar(64) not null,
    `src_node_id`     varchar(64) not null,
    `dst_node_id`     varchar(64) not null,
    `src_net_address` varchar(100), -- node net address
    `dst_net_address` varchar(100), -- cooperate node net address
    `is_deleted`      tinyint(1) default '0' not null, -- delete flag
    `gmt_create`      datetime default current_timestamp not null, -- create time
    `gmt_modified`    datetime default current_timestamp not null -- modified time
);

create unique index `upk_route_src_dst` on `node_route` (`src_node_id`, `dst_node_id`);
create index `key_router_src` on `node_route` (`src_node_id`);
create index `key_router_dst` on `node_route` (`dst_node_id`);


-- project - new project and project info add compute_mode enum: mpc,tee  todo: mpc may be not suitable
create table if not exists `project`
(
    `id`           int auto_increment primary key,
    `project_id`   varchar(64) not null, -- project ID
    `name`         varchar(256) not null, -- project name
    `compute_mode` varchar(64) default 'mpc' not null, -- compute_mode: mpc,tee
    `compute_func` varchar(64) default 'ALL' not null, -- compute_func: DAG,PSI,ALL
    `project_info` text, -- tee dag runtimeParams
    `description`  text default null, -- project description
    `owner_id`     varchar(64) default '' not null,
    `status`       tinyint(1) default '0' not null, -- archive flag
    `is_deleted`   tinyint(1) default '0' not null, -- delete flag
    `gmt_create`   datetime default current_timestamp not null, -- create time
    `gmt_modified` datetime default current_timestamp not null  -- modified time
);

create unique index `upk_project_id` on `project` (`project_id`);

create index `key_project_name` on `project` (`name`);

create table if not exists `project_inst`
(
    `id`           int auto_increment primary key,
    `project_id`   varchar(64) not null,
    `inst_id`      varchar(64) not null,
    `is_deleted`   tinyint(1) default '0' not null, -- delete flag
    `gmt_create`   datetime default current_timestamp not null, -- create time
    `gmt_modified` datetime default current_timestamp not null  -- modified time
);

create unique index `upk_project_inst_id` on `project_inst` (`project_id`, `inst_id`);

create table if not exists `project_node`
(
    `id`           int auto_increment primary key,
    `project_id`   varchar(64) not null,
    `node_id`      varchar(64) not null,
    `is_deleted`   tinyint(1) default '0' not null, -- delete flag
    `gmt_create`   datetime default current_timestamp not null, -- create time
    `gmt_modified` datetime default current_timestamp not null  -- modified time
);

create unique index `upk_project_node_id` on `project_node` (`project_id`, `node_id`);

create table if not exists `project_datatable`
(
    `id`            int auto_increment primary key,
    `project_id`    varchar(64) not null,
    `node_id`       varchar(64) not null,
    `datatable_id`  varchar(64) not null,
    `table_configs` text, -- project datatable config
    `source`        varchar(16) not null, -- project datatable from: IMPORTED/CREATED
    `is_deleted`    tinyint(1) default '0' not null, -- delete flag
    `gmt_create`    datetime default current_timestamp not null, -- create time
    `gmt_modified`  datetime default current_timestamp not null  -- modified time
);

create unique index `upk_project_datatable_id` on `project_datatable` (`project_id`, `node_id`, `datatable_id`);

create table if not exists `project_fed_table`
(
    `id`           int auto_increment primary key,
    `project_id`   varchar(64) not null,
    `fed_table_id` varchar(64) not null,
    `joins`        text not null, -- What tables are made up [{nodeId, datatableId}]
    `is_deleted`   tinyint(1) default '0' not null, -- delete flag
    `gmt_create`   datetime default current_timestamp not null, -- create time
    `gmt_modified` datetime default current_timestamp not null  -- modified time
);

create unique index `upk_project_fed_table_id` on `project_fed_table` (`project_id`, `fed_table_id`);

create table if not exists `project_model`
(
    `id`           int auto_increment primary key,
    `project_id`   varchar(64) not null,
    `model_id`     varchar(64) not null,
    `is_deleted`   tinyint(1) default '0' not null, -- delete flag
    `gmt_create`   datetime default current_timestamp not null, -- create time
    `gmt_modified` datetime default current_timestamp not null  -- modified time
);
create unique index `upk_project_model_id` on `project_model` (`project_id`, `model_id`);

create table if not exists `project_rule`
(
    `id`           int auto_increment primary key,
    `project_id`   varchar(64) not null,
    `rule_id`      varchar(64) not null,
    `is_deleted`   tinyint(1) default '0' not null, -- delete flag
    `gmt_create`   datetime default current_timestamp not null, -- create time
    `gmt_modified` datetime default current_timestamp not null  -- modified time
);
create unique index `upk_project_rule_id` on `project_rule` (`project_id`, `rule_id`);

create table if not exists `project_report`
(
    `id`         int auto_increment primary key,
    `project_id` varchar(64) not null,
    `report_id`  varchar(64) not null,
    `content`    longtext not null, -- report info
    `is_deleted` tinyint(1) default '0' not null, -- delete flag
    `gmt_create` datetime default current_timestamp not null, -- create time
    `gmt_modified` datetime default current_timestamp not null -- modified time
);

create unique index `upk_project_report_id` on `project_report` (`project_id`, `report_id`);

create table if not exists `project_read_data`
(
    `id`           int auto_increment primary key,
    `project_id`   varchar(64) not null,
    `output_id`    varchar(64) not null,
    `report_id`    varchar(64) not null,
    `hash`         varchar(64) default '' not null, -- read_data hash
    `task`         varchar(64) default '' not null, -- read_data task
    `grap_node_id` varchar(64) default '' not null, -- read_data grap node id
    `content`      varchar(64) not null, -- read_data info
    `raw`          varchar(64) not null, -- read_data raw info
    `is_deleted`   tinyint(1) default '0' not null, -- delete flag
    `gmt_create`   datetime default current_timestamp not null, -- create time
    `gmt_modified` datetime default current_timestamp not null -- modified time
);

create unique index `upk_project_read_data_id` on `project_read_data` (`project_id`, `report_id`);

create table if not exists `project_result`
(
    `id`          int auto_increment primary key,
    `project_id`  varchar(64) not null,
    `kind`        varchar(16) not null, -- project result type: model\fed_table\rule\report_table
    `node_id`     varchar(64) not null, -- node_id
    `ref_id`      varchar(64) not null, -- generated resource Id
    `job_id`      varchar(64), -- create by job id
    `task_id`     varchar(64), -- create by task id
    `is_deleted`  tinyint(1) default '0' not null, -- delete flag
    `gmt_create`  datetime default current_timestamp not null, -- create time
    `gmt_modified` datetime default current_timestamp not null -- modified time
);

create unique index `upk_project_result_kind_node_ref_id` on `project_result` (`project_id`, `kind`, `node_id`, `ref_id`);

create table if not exists `project_job`
(
    `id`            int auto_increment primary key,
    `project_id`    varchar(64) not null,
    `job_id`        varchar(64) not null,
    `name`          varchar(40) not null, -- Job name
    `status`        varchar(32) not null, -- Job status
    `err_msg`       text, -- err_msg
    `graph_id`      varchar(64), -- uniq graph id
    `edges`         text, -- create by graph edges
    `finished_time` datetime default null, -- finished_time
    `is_deleted`    tinyint(1) default '0' not null, -- delete flag
    `gmt_create`    datetime default current_timestamp not null, -- create time
    `gmt_modified`  datetime default current_timestamp not null -- modified time
);

create unique index `upk_project_job_id` on `project_job` (`project_id`, `job_id`);
create unique index `upk_job_id` on `project_job` (`job_id`); -- Kuscia，Job unique


create table if not exists `project_job_task`
(
    `id`            int auto_increment primary key,
    `project_id`    varchar(64) not null,
    `job_id`        varchar(64) not null,
    `task_id`       varchar(64) not null,
    `parties`       text not null,
    `status`        varchar(32) not null, -- Task status
    `err_msg`       text, -- err_msg
    `graph_node_id` varchar(64), -- create by graph node
    `graph_node`    text, -- graph node coordinate，x,y,code_name, label
    `is_deleted`    tinyint(1) default '0' not null, -- delete flag
    `gmt_create`    datetime default current_timestamp not null, -- create time
    `gmt_modified`  datetime default current_timestamp not null -- modified time
);

create unique index `upk_project_job_task_id` on `project_job_task` (`project_id`, `job_id`, `task_id`);

create table if not exists `project_graph`
(
    `id`             int auto_increment primary key,
    `project_id`     varchar(64) not null,
    `graph_id`       varchar(64) not null,
    `name`           varchar(128),
    `edges`          text,
    `owner_id`       varchar(64) default '' not null,
    `node_max_index` int not null,
    `is_deleted`     tinyint(1) default '0' not null, -- delete flag
    `gmt_create`     datetime default current_timestamp not null, -- create time
    `gmt_modified`   datetime default current_timestamp not null -- modified time
);

create unique index `upk_project_graph` on `project_graph` (`project_id`, `graph_id`);

create table if not exists `project_graph_node`
(
    `id`            int auto_increment primary key,
    `project_id`    varchar(64) not null,
    `graph_id`      varchar(64) not null,
    `graph_node_id` varchar(64) not null,
    `code_name`     varchar(64),
    `label`         varchar(64),
    `x`             int,
    `y`             int,
    `inputs`        text,
    `outputs`       text,
    `node_def`      text,
    `is_deleted`    tinyint(1) default '0' not null, -- delete flag
    `gmt_create`    datetime default current_timestamp not null, -- create time
    `gmt_modified`  datetime default current_timestamp not null -- modified time
);

create unique index `upk_project_graph_node` on `project_graph_node` (`project_id`, `graph_id`, `graph_node_id`);

create table if not exists `project_job_task_log`
(
    `id`         int auto_increment primary key,
    `project_id` varchar(64) not null,
    `job_id`     varchar(64) not null,
    `task_id`    varchar(64) not null,
    `log`        text not null,
    `gmt_create` datetime default current_timestamp not null -- create time
);

create index `idx_project_job_task_log` on `project_job_task_log` (`project_id`, `job_id`, `task_id`);

create table if not exists `user_accounts`
(
    `id`                           int auto_increment primary key,
    `name`                         varchar(128) not null, -- username
    `password_hash`                varchar(128) not null, -- password_hash
    `owner_type`                   varchar(16) default 'CENTER' not null,
    `owner_id`                     varchar(64) default 'kuscia-system' not null,
    `passwd_reset_failed_attempts` int default null,
    `gmt_passwd_reset_release`     datetime default null,
    `is_deleted`                   tinyint(1) default '0' not null, -- delete flag
    `gmt_create`                   datetime default current_timestamp not null, -- create time
    `gmt_modified`                 datetime default current_timestamp not null, -- modified time
    `failed_attempts`              int default null,
    `locked_invalid_time`          datetime default null,
    INDEX `idx_name` (`name`)
);

create table if not exists `user_tokens`
(
    `id`           int auto_increment primary key,
    `name`         varchar(128) not null, -- username
    `token`        varchar(64) default null, -- login token
    `gmt_token`    datetime default null, -- token effective time
    `session_data` text default null,
    `is_deleted`   tinyint(1) default '0' not null, -- delete flag
    `gmt_create`   datetime default current_timestamp not null, -- create time
    `gmt_modified` datetime default current_timestamp not null, -- modified time
    CONSTRAINT `fk_name` FOREIGN KEY (`name`) REFERENCES `user_accounts` (`name`)
);
create table if not exists `tee_node_datatable_management`
(
    `id`            int auto_increment primary key,
    `node_id`       varchar(64) not null,
    `tee_node_id`   varchar(64) not null,
    `datatable_id`  varchar(64) not null,
    `datasource_id` varchar(64) not null,
    `kind`          varchar(16) not null, -- datatable manage operate kind
    `job_id`        varchar(64) not null, -- datatable manage TEE job id
    `status`        varchar(32) not null, -- datatable manage TEE job status
    `err_msg`       text, -- datatable manage TEE job errorMsg
    `operate_info`  text, -- datatable manage operate information
    `is_deleted`    tinyint(1) default '0' not null, -- delete flag
    `gmt_create`    datetime default current_timestamp not null, -- create time
    `gmt_modified`  datetime default current_timestamp not null -- modified time
);

create unique index `upk_node_datatable_management` on `tee_node_datatable_management` (`node_id`, `tee_node_id`, `datatable_id`, `job_id`);

create table if not exists `edge_data_sync_log`
(
    `table_name`       varchar(64) not null,
    `last_update_time` varchar(64) not null,
    primary key (`table_name`)
);

create table if not exists `sys_resource`
(
    `id`            int auto_increment primary key,
    `resource_type` varchar(16) not null, -- comment 'INTERFACE|NODE'
    `resource_code` varchar(64) not null unique, -- comment '{Code} or ALL'
    `resource_name` varchar(64),
    `is_deleted`    tinyint(1) default '0' not null, -- delete flag
    `gmt_create`    datetime default current_timestamp not null, -- create time
    `gmt_modified`  datetime default current_timestamp not null -- modified time
);

create index `key_resource_type` on `sys_resource` (`resource_type`);

create table if not exists `sys_role`
(
    `id`         int auto_increment primary key,
    `role_code`  varchar(64) not null unique,
    `role_name`  varchar(64),
    `is_deleted` tinyint(1) default '0' not null, -- delete flag
    `gmt_create` datetime default current_timestamp not null, -- create time
    `gmt_modified` datetime default current_timestamp not null -- modified time
);



create table if not exists `sys_role_resource_rel`
(
    `id`            int auto_increment primary key,
    `role_code`     varchar(64) not null,
    `resource_code` varchar(64) not null,
    `gmt_create`    datetime default current_timestamp not null, -- create time
    `gmt_modified`  datetime default current_timestamp not null -- modified time
);

create unique index `uniq_role_code_resource_code` on `sys_role_resource_rel` (`role_code`, `resource_code`);

create table if not exists `sys_user_permission_rel`
(
    `id`           int auto_increment primary key,
    `user_type`    varchar(16) not null,
    `user_key`     varchar(64) not null,
    `target_type`  varchar(16) not null default 'ROLE',
    `target_code`  varchar(16) not null,
    `gmt_create`   datetime default current_timestamp not null, -- create time
    `gmt_modified` datetime default current_timestamp not null -- modified time
);

create unique index `uniq_user_key_target_code` on `sys_user_permission_rel` (`user_key`, `target_code`);

create table if not exists `sys_user_node_rel`
(
    `id`           int auto_increment primary key,
    `user_id`      varchar(64) not null,
    `node_id`      varchar(64) not null,
    `gmt_create`   datetime default current_timestamp not null, -- create time
    `gmt_modified` datetime default current_timestamp not null -- modified time
);

create unique index `uniq_user_id_node_id` on `sys_user_node_rel` (`user_id`, `node_id`);


create table if not exists `vote_request`
(
    `id`                 int auto_increment primary key,
    `vote_id`            varchar(64) not null, -- unique vote id
    `initiator`          varchar(64) not null, -- vote initiator
    `type`               varchar(16) not null, -- vote type
    `voters`             text not null, -- voter inviters
    `vote_counter`       varchar(64) not null, -- vote counter
    `executors`          text not null, -- vote executors
    `approved_threshold` int not null, -- the number of votes needed to succeed
    `request_msg`        text default null, -- vote msg
    `status`             tinyint(1) not null, -- vote status
    `execute_status`     varchar(16) default 'COMMITTED' not null, -- executors call back status
    `msg`                text default null, -- error msg
    `party_vote_info`    text default null,
    `description`        varchar(64) not null, -- vote desc
    `is_deleted`         tinyint(1) default '0' not null,
    `gmt_create`         datetime default current_timestamp not null,
    `gmt_modified`       datetime default current_timestamp not null
);

create unique index `upk_vote_id` on `vote_request` (`vote_id`);

create table if not exists `tee_download_approval_config`
(
    `id`               int auto_increment primary key,
    `vote_id`          varchar(64) not null, -- unique vote id
    `task_id`          varchar(64) not null, -- the resource task id
    `job_id`           varchar(64) not null, -- the resource job id
    `resource_id`      varchar(64) not null, -- resource id
    `resource_type`    varchar(16) not null, -- resource type
    `project_id`       varchar(64) not null, -- project id
    `graph_id`         varchar(64) not null, -- graph id
    `all_participants` text not null, -- contains initiator and all inviters
    `is_deleted`       tinyint(1) default '0' not null, -- delete flag
    `gmt_create`       datetime default current_timestamp not null,
    `gmt_modified`     datetime default current_timestamp not null
);

create unique index `upk_tee_download_approval_config_vote_id` on `tee_download_approval_config` (`vote_id`);

create table if not exists `node_route_approval_config`
(
    `id`               int auto_increment primary key,
    `vote_id`          varchar(64) not null, -- unique vote id
    `is_single`        tinyint(1) not null, -- single node route flag
    `src_node_id`      varchar(64) not null, -- src node id
    `src_node_addr`    varchar(64) not null, -- src node address
    `des_node_id`      varchar(64) not null, -- destination node id
    `des_node_addr`    varchar(64) not null, -- destination node addr
    `all_participants` text not null, -- contains initiator and all inviters
    `is_deleted`       tinyint(1) default '0' not null, -- delete flag
    `gmt_create`       datetime default current_timestamp not null,
    `gmt_modified`     datetime default current_timestamp not null
);

create unique index `upk_node_route_approval_config_vote_id` on `node_route_approval_config` (`vote_id`);

create table if not exists `project_approval_config`
(
    `id`             int auto_increment primary key,
    `vote_id`        varchar(64) not null, -- unique vote id
    `initiator`      varchar(64) not null, -- vote initiator
    `type`           varchar(16) not null, -- vote type
    `parties`        text default null, -- all parties node id
    `project_id`     varchar(64) default null, -- projectID
    `invite_node_id` varchar(64) default null,
    `is_deleted`     tinyint(1) default '0' not null, -- delete flag
    `gmt_create`     datetime default current_timestamp not null,
    `gmt_modified`   datetime default current_timestamp not null
);

create unique index `upk_project_approval_config_vote_id` on `project_approval_config` (`vote_id`);

create table if not exists `vote_invite`
(
    `id`                  int auto_increment primary key,
    `vote_id`             varchar(64) not null, -- unique vote id
    `initiator`           varchar(64) not null, -- vote initiator
    `vote_participant_id` varchar(64) not null, -- vote invitor id
    `type`                varchar(16) not null, -- vote type
    `vote_msg`            text default null, -- vote msg
    `action`              varchar(16) default 'REVIEWING', -- vote action
    `reason`              varchar(64) default null, -- reject reason
    `description`         varchar(64) not null, -- vote desc
    `is_deleted`          tinyint(1) default '0' not null, -- delete flag
    `gmt_create`          datetime default current_timestamp not null, -- create time
    `gmt_modified`        datetime default current_timestamp not null -- modified time
);

create unique index `upk_vote_invite_participant_id` on `vote_invite` (`vote_id`, `vote_participant_id`);

create table if not exists `project_model_pack`
(
    `id`              int auto_increment primary key,
    `project_id`      varchar(64) not null, -- project ID
    `model_id`        varchar(64) not null, -- modelId
    `initiator`       varchar(64) not null, -- model pack initiator
    `model_name`      varchar(256) not null, -- modelName
    `model_desc`      text default null, -- model description
    `model_stats`     tinyint(1) not null default '0', -- modelStatus online,offline,discard,deleted
    `serving_id`      varchar(64) default null, -- serving id
    `sample_tables`   text not null, -- model training read table participants
    `model_list`      text not null, -- model training participants
    `train_id`        varchar(64) not null, -- model training id
    `model_report_id` varchar(64) not null, -- model_report_id
    `graph_detail`    text default null, -- model graph_detail
    `is_deleted`      tinyint(1) default '0' not null, -- delete flag
    `gmt_create`      datetime default current_timestamp not null, -- create time
    `gmt_modified`    datetime default current_timestamp not null -- modified time
);

create unique index `upk_model_id` on `project_model_pack` (`model_id`);

create table if not exists `project_model_serving`
(
    `id`                   int auto_increment primary key,
    `project_id`           varchar(64) not null, -- project ID
    `serving_id`           varchar(64) not null, -- serving ID
    `initiator`            varchar(64) not null, -- serving initiator
    `serving_input_config` text not null, -- serving input config
    `parties`              text, -- parties info
    `party_endpoints`      text default null, -- all parties endpoints
    `serving_stats`        varchar(16), -- init,success,failed
    `error_msg`            text, -- failed msg
    `is_deleted`           tinyint(1) default '0' not null, -- delete flag
    `gmt_create`           datetime default current_timestamp not null, -- create time
    `gmt_modified`         datetime default current_timestamp not null -- modified time
);

create unique index `upk_serving_id` on `project_model_serving` (`serving_id`);

create table if not exists `feature_table`
(
    `id`                 int auto_increment primary key,
    `feature_table_id`   varchar(8) not null, -- feature table id
    `feature_table_name` varchar(32) not null, -- feature table name
    `node_id`            varchar(64) not null, -- node_id
    `type`               varchar(8) not null default 'HTTP', -- feature datasource type
    `description`        varchar(64) default null, -- feature table desc
    `url`                varchar(64) not null, -- feature table service addr
    `columns`            text not null, -- feature table columns
    `status`             varchar(16) not null, -- Available,Unavailable
    `is_deleted`         tinyint(1) default '0' not null, -- delete flag
    `gmt_create`         datetime default current_timestamp not null, -- create time
    `gmt_modified`       datetime default current_timestamp not null -- modified time
);

create unique index `upk_feature_table_id` on `feature_table` (`feature_table_id`);

create table if not exists `project_feature_table`
(
    `id`               int auto_increment primary key,
    `project_id`       varchar(64) not null,
    `node_id`          varchar(64) not null,
    `feature_table_id` varchar(64) not null,
    `table_configs`    text not null, -- project feature table config
    `source`           varchar(16) not null, -- project feature table from: IMPORTED/CREATED
    `is_deleted`       tinyint(1) default '0' not null, -- delete flag
    `gmt_create`       datetime default current_timestamp not null, -- create time
    `gmt_modified`     datetime default current_timestamp not null -- modified time
);

create unique index `upk_project_feature_table_id` on `project_feature_table` (`project_id`, `node_id`, `feature_table_id`);

create table if not exists `project_graph_node_kuscia_params`
(
    `id`              int auto_increment primary key,
    `project_id`      varchar(64) default null, -- projectID
    `graph_id`        varchar(64), -- uniq graph id
    `graph_node_id`   varchar(64), -- create by graph node
    `job_id`          varchar(64) not null,
    `task_id`         varchar(64) not null,
    `inputs`          text,
    `outputs`         text,
    `node_eval_param` text,
    `is_deleted`      tinyint(1) default '0' not null, -- delete flag
    `gmt_create`      datetime default current_timestamp not null, -- create time
    `gmt_modified`    datetime default current_timestamp not null -- modified time
);

create unique index `upk_project_graph_node_kuscia_params_id` on `project_graph_node_kuscia_params` (`project_id`, `graph_id`, `graph_node_id`);
