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

package org.secretflow.secretpad.common.dto;

import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.common.enums.UserOwnerTypeEnum;
import org.secretflow.secretpad.common.util.JsonUtils;

import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Set;

/**
 * @author beiwei
 * @date 2023/9/12
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class UserContextDTO {
    private String token;
    private String name;
    private PlatformTypeEnum platformType;
    private String platformNodeId;
    private UserOwnerTypeEnum ownerType;
    private String ownerId;

    private Set<String> projectIds;
    private Set<String> apiResources;

    /**
     * only for edge platform rpc.
     */
    private Boolean virtualUserForNode;

    /**
     * deploy-mode:${DEPLOY_MODE:MPC} # MPC TEE ALL-IN-ONE
     *
     * @see application.yaml  secretpad:deploy-mode
     */
    private String deployMode;

    public static UserContextDTO fromJson(String jsonStr) {
        return JsonUtils.toJavaObject(jsonStr, UserContextDTO.class);
    }

    public boolean containProjectId(String projectId) {
        if (StringUtils.isBlank(projectId)) {
            return false;
        }
        if (CollectionUtils.isEmpty(projectIds)) {
            return false;
        }
        return projectIds.contains(projectId);
    }

    public boolean containInterfaceResource(String resourceCode) {
        if (StringUtils.isBlank(resourceCode)) {
            return false;
        }
        if (CollectionUtils.isEmpty(apiResources)) {
            return false;
        }
        return apiResources.contains(resourceCode);
    }

    public String toJsonStr() {
        return JsonUtils.toJSONString(this);
    }
}
