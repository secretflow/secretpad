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

import org.secretflow.secretpad.persistence.model.TeeJobKind;
import org.secretflow.secretpad.persistence.model.TeeJobStatus;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.io.Serial;
import java.io.Serializable;

/**
 * Tee node datatable management data object
 *
 * @author xujiening
 * @date 2023/9/15
 */
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Setter
@Getter
@Table(name = "tee_node_datatable_management")
@SQLDelete(sql = "update tee_node_datatable_management set is_deleted = 1 where job_id = ?")
@Where(clause = "is_deleted = 0")
public class TeeNodeDatatableManagementDO extends BaseAggregationRoot<TeeNodeDatatableManagementDO> {

    /**
     * Tee node datatable management unique primary key
     */
    @EmbeddedId
    private UPK upk;

    /**
     * Datasource id
     */
    @Column(name = "datasource_id", nullable = false, length = 64)
    private String datasourceId;

    /**
     * Tee datatable manage operate type
     */
    @Column(name = "kind", nullable = false, length = 16)
    @Enumerated(value = EnumType.STRING)
    private TeeJobKind kind;


    /**
     * Tee datatable manage operate status
     * When created, it must be running.
     */
    @Column(name = "status", nullable = false, length = 32)
    @Enumerated(value = EnumType.STRING)
    private TeeJobStatus status;

    /**
     * Project job error message
     */
    @Column(name = "err_msg", nullable = true)
    private String errMsg;

    /**
     * Operate information for different operate type
     */
    @Column(name = "operate_info", nullable = true)
    private String operateInfo;

    /**
     * Tee node datatable management unique primary key
     */
    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class UPK implements Serializable {
        @Serial
        private static final long serialVersionUID = 291568296509217011L;

        /**
         * Node id
         */
        @Column(name = "node_id", nullable = false, length = 64)
        private String nodeId;
        /**
         * Tee node id
         */
        @Column(name = "tee_node_id", nullable = false, length = 64)
        private String teeNodeId;
        /**
         * Datatable id
         */
        @Column(name = "datatable_id", nullable = false, length = 64)
        private String datatableId;
        /**
         * Tee node datatable management job id
         */
        @Column(name = "job_id", nullable = false, length = 64)
        private String jobId;
    }

    /**
     * Whether the tee job status is finished
     *
     * @return whether finished
     */
    public boolean isFinished() {
        return this.status == TeeJobStatus.FAILED || this.status == TeeJobStatus.SUCCESS;
    }

    /**
     * Get Node id
     */
    @Override
    public String getNodeId() {
        return this.upk.nodeId;
    }
}
