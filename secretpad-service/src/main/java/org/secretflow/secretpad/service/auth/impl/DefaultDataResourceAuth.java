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

package org.secretflow.secretpad.service.auth.impl;

import lombok.extern.slf4j.Slf4j;
import org.secretflow.secretpad.common.enums.UserOwnerTypeEnum;
import org.secretflow.secretpad.common.enums.DataResourceTypeEnum;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.service.auth.DataResourceAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author beiwei
 * @date 2023/9/11
 */
@Service
@Slf4j
public class DefaultDataResourceAuth implements DataResourceAuth {

    @Autowired
    private DataResourceProjectAuth dataResourceProjectAuth;
    /**
     *
     * @param resourceType resource type
     * @param resourceId resource id
     * @return result
     */
    @Override
    public boolean check(DataResourceTypeEnum resourceType, String resourceId) {
        UserOwnerTypeEnum ownerType = UserContext.getUser().getOwnerType();
        if (UserOwnerTypeEnum.CENTER.equals(ownerType)){
            // Center user has all data permission
            return true;
        }

        if(DataResourceTypeEnum.NODE_ID.equals(resourceType)){
            return UserContext.getUser().getOwnerId().equals(resourceId);
        }

        if(DataResourceTypeEnum.PROJECT_ID.equals(resourceType)){
            return dataResourceProjectAuth.check(resourceId);
        }
        return false;
    }
}
