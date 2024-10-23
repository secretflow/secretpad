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
create table if not exists `project_schedule`
(
    id            integer primary key autoincrement,
    schedule_id   varchar(64)                          not null,
    schedule_desc varchar(200),
    cron          varchar(64)                          not null,
    request       text                                 not null,
    graph_info    text                                 not null,
    graph_job_id  varchar(64)                          not null,
    job_info      text                                 not null,
    owner         varchar(64)                          not null,
    creator       varchar(64)                          not null,
    status        varchar(32)                          not null,
    project_id    varchar(64)                          not null,
    graph_id      varchar(64)                          not null,
    create_time   datetime   default CURRENT_TIMESTAMP not null,
    is_deleted    tinyint(1) default '0'               not null, -- delete flag
    gmt_create    datetime   default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified  datetime   default CURRENT_TIMESTAMP not null  -- modified time
);

create table if not exists `project_schedule_task`
(
    id                              integer primary key autoincrement,
    project_id                      varchar(64)                          not null,
    graph_id                        varchar(64)                          not null,
    schedule_job_id                 varchar(64)                          not null,
    schedule_id                     varchar(64)                          not null,
    schedule_task_id                varchar(64)                          not null,
    cron                            varchar(64)                          not null,
    schedule_task_expect_start_time datetime                             not null,
    schedule_task_start_time        datetime,
    schedule_task_end_time          datetime,
    status                          varchar(32)                          not null,
    owner                           varchar(64)                          not null,
    creator                         varchar(64)                          not null,
    job_request                     text                                 not null,
    all_re_run                      tinyint(1) default '0'               not null,
    is_deleted                      tinyint(1) default '0'               not null, -- delete flag
    gmt_create                      datetime   default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified                    datetime   default CURRENT_TIMESTAMP not null  -- modified time
);

create table if not exists `project_schedule_job`
(
    id               integer primary key autoincrement,
    project_id       varchar(64)                          not null,
    graph_id         varchar(64),                                   -- uniq graph id
    job_id           varchar(64)                          not null,
    `name`           varchar(40)                          not null, -- Job name
    status           varchar(32)                          not null, -- Job status
    err_msg          text,                                          -- err_msg
    edges            text,                                          -- create by graph edges
    finished_time    datetime   default null,                       -- finished_time
    owner            varchar(64)                          not null,
    schedule_task_id varchar(64)                          not null,
    is_deleted       tinyint(1) default '0'               not null, -- delete flag
    gmt_create       datetime   default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified     datetime   default CURRENT_TIMESTAMP not null  -- modified time
);
create unique index if not exists `upk_project_schedule_job_id` on project_schedule_job (`project_id`, `job_id`);
create unique index if not exists `upk_schedule_job_id` on project_schedule_job (`job_id`); -- Kuscia，Job unique