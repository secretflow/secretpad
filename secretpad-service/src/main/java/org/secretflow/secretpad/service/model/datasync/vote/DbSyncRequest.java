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

import org.secretflow.secretpad.common.annotation.OneOfType;
import org.secretflow.secretpad.common.constant.DataSyncConstants;
import org.secretflow.secretpad.persistence.entity.*;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * VoteSyncRequest.
 *
 * @author cml
 * @date 2023/11/02
 */
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DbSyncRequest {

    @OneOfType(types = {DataSyncConstants.VOTE_REQUEST,
            DataSyncConstants.VOTE_INVITE,
            DataSyncConstants.NODE_ROUTE,
            DataSyncConstants.TEE_NODE_DATATABLE_MANAGEMENT,
            DataSyncConstants.PROJECT_APPROVAL_CONFIG,
            DataSyncConstants.PROJECT,
            DataSyncConstants.PROJECT_NODE})
    @NotBlank
    private String syncDataType;


    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
            property = "syncDataType",
            defaultImpl = VoteInviteDO.class
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = VoteRequestDO.class, name = DataSyncConstants.VOTE_REQUEST),
            @JsonSubTypes.Type(value = VoteInviteDO.class, name = DataSyncConstants.VOTE_INVITE),
            @JsonSubTypes.Type(value = NodeRouteDO.class, name = DataSyncConstants.NODE_ROUTE),
            @JsonSubTypes.Type(value = TeeNodeDatatableManagementSyncRequest.class, name = DataSyncConstants.TEE_NODE_DATATABLE_MANAGEMENT),
            @JsonSubTypes.Type(value = ProjectDO.class, name = DataSyncConstants.PROJECT),
            @JsonSubTypes.Type(value = ProjectApprovalConfigDO.class, name = DataSyncConstants.PROJECT_APPROVAL_CONFIG),
            @JsonSubTypes.Type(value = ProjectNodeDO.class, name = DataSyncConstants.PROJECT_NODE)
    })
    private ProjectNodesInfo projectNodesInfo;

}
