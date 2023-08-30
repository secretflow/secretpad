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

insert into main.node (node_id, name, control_node_id, auth, description, type, is_deleted, gmt_create, gmt_modified,
                       create_by, update_by, net_address)
values ('alice', 'alice', 'alice', 'alice', 'alice', 'embedded', 0, '2023-05-11 11:54:08', '2023-05-11 11:54:08',
        'admin', 'admin', '127.0.0.1:28080'),
       ('bob', 'bob', 'bob', 'bob', 'bob', 'embedded', 0, '2023-05-11 11:54:08', '2023-05-11 11:54:08', 'admin',
        'admin', '127.0.0.1:38080');

insert into main.node_route (src_node_id, dst_node_id, is_deleted, gmt_create, gmt_modified, create_by, update_by,
                             src_net_address, dst_net_address)
values ('alice', 'bob', 0, '2023-05-11 11:54:08', '2023-05-11 11:54:08', 'admin', 'admin', '127.0.0.1:28080',
        '127.0.0.1:38080'),
       ('bob', 'alice', 0, '2023-05-11 11:54:08', '2023-05-11 11:54:08', 'admin', 'admin', '127.0.0.1:38080',
        '127.0.0.1:28080');
