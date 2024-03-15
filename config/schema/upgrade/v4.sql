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

 alter table project add column owner_id varchar(64)      not null default '';
 alter table project add column status   tinyint(1)    default '0' not null;
 alter table project add column compute_func varchar(64)  default '' not null;             -- compute_func: DAG,PSI,ALL
 alter table vote_request add column party_vote_info text default null;
 alter table project_graph add column owner_id varchar(64) not null default '';
--  node --
alter table node add column master_node_id varchar(64) default 'master';

--  role --
insert into sys_role(role_code, role_name) values('P2P_NODE', 'P2P 用户');

 create table if not exists `project_approval_config`
 (
   id                    integer         primary key autoincrement,
   vote_id               varchar(64)     not null,                           --unique vote id
   type                  varchar(16)     not null,
   parties				 text      	     default null,                       --all parties node id
   project_id			 varchar(64)	 default null,                       --projectID
   invite_node_id		 varchar(64)     default null,
   is_deleted            tinyint(1)      default '0' not null,               -- delete flag
   gmt_create            datetime        default CURRENT_TIMESTAMP not null, -- create time
   gmt_modified          datetime        default CURRENT_TIMESTAMP not null  -- modified time
 );
 create unique index `upk_project_approval_config_vote_id` on project_approval_config (`vote_id`);

-- user_accounts --
 alter table user_accounts add column failed_attempts integer default null;
 alter table user_accounts add column locked_invalid_time datetime default null;
 alter table user_accounts add column passwd_reset_failed_attempts integer default null;
 alter table user_accounts add column gmt_passwd_reset_release datetime default null;

-- project_graph_node_kuscia_params --
create table if not exists `project_graph_node_kuscia_params`
(
    id                            integer primary key autoincrement,
    project_id			          varchar(64)  default null,                       -- projectID
    graph_id                      varchar(64),                                     -- uniq graph id
    graph_node_id                 varchar(64),                                     -- create by graph node
    job_id                        varchar(64)  not null,
    task_id                       varchar(64)  not null,
    inputs                        text,
    outputs                       text,
    node_eval_param               text,
    is_deleted                    tinyint(1)   default '0' not null,               -- delete flag
    gmt_create                    datetime     default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified                  datetime     default CURRENT_TIMESTAMP not null  -- modified time
);
create unique index `upk_project_graph_node_kuscia_params_id` on project_graph_node_kuscia_params (`project_id`, `graph_id`,`graph_node_id`);
