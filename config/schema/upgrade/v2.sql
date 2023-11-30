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
drop table `node`;
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
    mode            integer     default '0' not null,               -- node feature indicates  0 - mpc | 1 - tee | 2 mpc&tee
    is_deleted      tinyint(1)  default '0' not null,               -- delete flag
    gmt_create      datetime    default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified    datetime    default CURRENT_TIMESTAMP not null  -- modified time
);
create unique index `upk_node_id` on node (`node_id`);
create index `key_node_name` on node (`name`);

drop table `node_route`;
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
begin;
alter table 'project'
    add 'compute_mode' varchar(64) default 'mpc' not null; -- compute_mode: mpc,tee
alter table 'project'
    add project_info text default '' not null; -- tee dag runtimeParams
commit;


-------- add mock data --------------
insert into main.node (node_id, name, control_node_id, auth, description, type, is_deleted,net_address, mode)
values ('alice', 'alice', 'alice', 'alice', 'alice', 'embedded', 0, '127.0.0.1:28080', 1),
       ('bob', 'bob', 'bob', 'bob', 'bob', 'embedded', 0,  '127.0.0.1:38080', 1),
       ('tee', 'tee', 'tee', 'tee', 'tee', 'embedded', 0,  '127.0.0.1:48080', 0);

insert into main.node_route (route_id, src_node_id, dst_node_id, is_deleted, src_net_address, dst_net_address)
values (1,'alice', 'bob', 0,  '127.0.0.1:28080','127.0.0.1:38080'),
       (2, 'bob', 'alice', 0, '127.0.0.1:38080','127.0.0.1:28080'),
       (3, 'alice', 'tee', 0,  '127.0.0.1:28080','127.0.0.1:48080'),
       (4, 'tee', 'alice', 0,  '127.0.0.1:48080','127.0.0.1:28080'),
       (5, 'bob', 'tee', 0,  '127.0.0.1:38080','127.0.0.1:48080'),
       (6, 'tee', 'bob', 0,  '127.0.0.1:48080','127.0.0.1:38080');

create table if not exists `edge_data_sync_log`
(
    table_name       varchar(64) primary key not null,
    last_update_time varchar(64)             not null
);