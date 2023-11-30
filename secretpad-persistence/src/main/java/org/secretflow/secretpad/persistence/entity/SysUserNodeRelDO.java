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

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

/**
 * system user and node rel
 * @author beiwei
 * @date 2023/9/13
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "sys_user_node_rel")
public class SysUserNodeRelDO extends SuperBaseAggregationRoot<SysUserNodeRelDO> {

    @EmbeddedId
    private UPK upk;

    /**
     * Project task unique primary key
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Embeddable
    public static class UPK implements Serializable {
        @Column(name = "user_id", nullable = false, length = 64)
        private String userId;

        @Column(name = "node_id", nullable = false, length = 64)
        private String nodeId;

    }
}
