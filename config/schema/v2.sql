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
    is_deleted      tinyint(1)  default '0' not null,               -- delete flag
    gmt_create      datetime    default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified    datetime    default CURRENT_TIMESTAMP not null, -- modified time
    create_by       varchar(128) not null,                          -- create by user name
    update_by       varchar(128) not null                           -- update by user name
);
create unique index `upk_node_id` on node (`node_id`);
create index `key_node_name` on node (`name`);

drop table `node_route`;
create table if not exists `node_route`
(
    id              integer primary key autoincrement,
    src_node_id     varchar(64)  not null,
    dst_node_id     varchar(64)  not null,
    src_net_address varchar(100),                                  -- node net address
    dst_net_address varchar(100),                                  -- cooperate node net address
    is_deleted      tinyint(1) default '0' not null,               -- delete flag
    gmt_create      datetime   default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified    datetime   default CURRENT_TIMESTAMP not null, -- modified time
    create_by       varchar(128) not null,                         -- create by user name
    update_by       varchar(128) not null                          -- update by user name
);
create unique index `upk_route_src_dst` on node_route (`src_node_id`, `dst_node_id`);
create index `key_router_src` on node_route (`src_node_id`);
create index `key_router_dst` on node_route (`dst_node_id`);


-- project - new project and project info add compute_mode enum: pipeline,hub
begin;
alter table 'project'
    add 'compute_mode' varchar(64) default 'pipeline' not null; -- compute_mode: pipeline,hub
commit;


-------- add mock data --------------
insert into main.node (node_id, name, control_node_id, auth, description, type, is_deleted, create_by, update_by,
                       net_address)
values ('alice', 'alice', 'alice', 'alice', 'alice', 'embedded', 0, 'admin', 'admin', '127.0.0.1:28080'),
       ('bob', 'bob', 'bob', 'bob', 'bob', 'embedded', 0, 'admin', 'admin', '127.0.0.1:38080');

insert into main.node_route (src_node_id, dst_node_id, is_deleted, create_by, update_by,
                             src_net_address, dst_net_address)
values ('alice', 'bob', 0, 'admin', 'admin', '127.0.0.1:28080',
        '127.0.0.1:38080'),
       ('bob', 'alice', 0, 'admin', 'admin', '127.0.0.1:38080',
        '127.0.0.1:28080');