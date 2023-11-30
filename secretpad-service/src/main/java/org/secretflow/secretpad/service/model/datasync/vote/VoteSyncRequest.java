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
import org.secretflow.secretpad.persistence.entity.NodeRouteDO;
import org.secretflow.secretpad.persistence.entity.ProjectNodesInfo;
import org.secretflow.secretpad.persistence.entity.VoteInviteDO;
import org.secretflow.secretpad.persistence.entity.VoteRequestDO;

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
public class VoteSyncRequest {

    @OneOfType(types = {"VOTE_REQUEST", "VOTE_INVITE", "NODE_ROUTE", "TEE_NODE_DATATABLE_MANAGEMENT"})
    @NotBlank
    private String syncDataType;


    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
            property = "syncDataType",
            defaultImpl = VoteInviteDO.class
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = VoteRequestDO.class, name = "VOTE_REQUEST"),
            @JsonSubTypes.Type(value = VoteInviteDataSyncRequest.class, name = "VOTE_INVITE"),
            @JsonSubTypes.Type(value = NodeRouteDO.class, name = "NODE_ROUTE"),
            @JsonSubTypes.Type(value = TeeNodeDatatableManagementSyncRequest.class, name = "TEE_NODE_DATATABLE_MANAGEMENT")

    })
    private ProjectNodesInfo projectNodesInfo;

}
