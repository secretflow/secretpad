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

package org.secretflow.secretpad.service.model.datasync.vote;

import org.secretflow.secretpad.persistence.entity.ProjectNodesInfo;
import org.secretflow.secretpad.persistence.entity.TeeNodeDatatableManagementDO;
import org.secretflow.secretpad.persistence.model.TeeJobKind;
import org.secretflow.secretpad.persistence.model.TeeJobStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * TeeNodeDatatableManagementSyncRequest
 *
 * @author zhiyin
 * @date 2023/11/4
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeeNodeDatatableManagementSyncRequest implements ProjectNodesInfo {

    private String nodeId;

    private String teeNodeId;

    private String datatableId;

    private String jobId;

    private String datasourceId;

    private TeeJobKind kind;

    private TeeJobStatus status;

    private String errMsg;

    private String operateInfo;

    @JsonIgnore
    @Override
    public String getProjectId() {
        return null;
    }

    @JsonIgnore
    @Override
    public List<String> getNodeIds() {
        List<String> nodeIds = new ArrayList<>();
        nodeIds.add(nodeId);
        return nodeIds;
    }

    /**
     * Parse the TeeNodeDatatableManagementSyncRequest object into a TeeNodeDatatableManagementDO object.
     *
     * @param teeNodeDatatableMngSyncRequest TeeNodeDatatableManagementSyncRequest
     * @return TeeNodeDatatableManagementDO object
     */
    public static TeeNodeDatatableManagementDO parse2DO(TeeNodeDatatableManagementSyncRequest teeNodeDatatableMngSyncRequest) {
        TeeNodeDatatableManagementDO teeNodeDatatableManagementDO = new TeeNodeDatatableManagementDO();
        TeeNodeDatatableManagementDO.UPK upk = new TeeNodeDatatableManagementDO.UPK(teeNodeDatatableMngSyncRequest.getNodeId(),
                teeNodeDatatableMngSyncRequest.getTeeNodeId(), teeNodeDatatableMngSyncRequest.getDatatableId(),
                teeNodeDatatableMngSyncRequest.getJobId());
        teeNodeDatatableManagementDO.setUpk(upk);
        teeNodeDatatableManagementDO.setDatasourceId(teeNodeDatatableMngSyncRequest.getDatasourceId());
        teeNodeDatatableManagementDO.setKind(teeNodeDatatableMngSyncRequest.getKind());
        teeNodeDatatableManagementDO.setStatus(teeNodeDatatableMngSyncRequest.getStatus());
        teeNodeDatatableManagementDO.setErrMsg(teeNodeDatatableMngSyncRequest.getErrMsg());
        teeNodeDatatableManagementDO.setOperateInfo(teeNodeDatatableMngSyncRequest.getOperateInfo());
        return teeNodeDatatableManagementDO;
    }

    /**
     * Parse the TeeNodeDatatableManagementDO object into a TeeNodeDatatableManagementSyncRequest object.
     *
     * @param teeNodeDatatableManagementDO the TeeNodeDatatableManagementDO object
     * @return the corresponding TeeNodeDatatableManagementSyncRequest object
     */
    public static TeeNodeDatatableManagementSyncRequest parse2VO(TeeNodeDatatableManagementDO teeNodeDatatableManagementDO) {
        TeeNodeDatatableManagementSyncRequest request = new TeeNodeDatatableManagementSyncRequest();
        request.setDatasourceId(teeNodeDatatableManagementDO.getDatasourceId());
        request.setKind(teeNodeDatatableManagementDO.getKind());
        request.setStatus(teeNodeDatatableManagementDO.getStatus());
        request.setErrMsg(teeNodeDatatableManagementDO.getErrMsg());
        request.setOperateInfo(teeNodeDatatableManagementDO.getOperateInfo());
        request.setNodeId(teeNodeDatatableManagementDO.getUpk().getNodeId());
        request.setTeeNodeId(teeNodeDatatableManagementDO.getUpk().getTeeNodeId());
        request.setDatatableId(teeNodeDatatableManagementDO.getUpk().getDatatableId());
        request.setJobId(teeNodeDatatableManagementDO.getUpk().getJobId());
        return request;
    }
}
