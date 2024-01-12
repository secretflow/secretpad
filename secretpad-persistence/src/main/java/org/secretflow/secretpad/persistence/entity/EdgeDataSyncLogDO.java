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

package org.secretflow.secretpad.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.io.Serializable;

/**
 * @author yutu
 * @date 2023/10/26
 */
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "edge_data_sync_log")
@ToString
@Getter
@Setter
public class EdgeDataSyncLogDO implements Serializable {

    @Id
    @Column(name = "table_name", nullable = false)
    private String tableName;

    @Column(name = "last_update_time", nullable = false)
    private String lastUpdateTime;
}