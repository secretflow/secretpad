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

package org.secretflow.secretpad.service.model.approval;

import org.secretflow.secretpad.common.annotation.OneOfType;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * CreateApprovalRequest.
 *
 * @author cml
 * @date 2023/09/19
 */
@Getter
@Setter
public class CreateApprovalRequest {
    @NotBlank
    private String nodeID;

    @OneOfType(types = {"TEE_DOWNLOAD", "NODE_ROUTE", "PROJECT_CREATE", "PROJECT_ARCHIVE"})
    @NotBlank
    private String voteType;

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
            property = "voteType",
            defaultImpl = TeeDownLoadVoteConfig.class
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = NodeRouteVoteConfig.class, name = "NODE_ROUTE"),
            @JsonSubTypes.Type(value = TeeDownLoadVoteConfig.class, name = "TEE_DOWNLOAD"),
            @JsonSubTypes.Type(value = ProjectCreateApprovalConfig.class, name = "PROJECT_CREATE"),
            @JsonSubTypes.Type(value = ProjectArchiveConfig.class, name = "PROJECT_ARCHIVE")

    })
    private @Valid AbstractVoteConfig voteConfig;

}
