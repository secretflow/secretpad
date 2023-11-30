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

import org.secretflow.secretpad.common.constant.resource.ApiResourceCodeConstants;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.service.auth.ApiResourceAuth;
import org.springframework.stereotype.Service;

/**
 * default api resource auth check
 *
 * @author beiwei
 * @date 2023/9/11
 */
@Service
public class DefaultApiResourceAuth implements ApiResourceAuth {
    /**
     * @param resourceCode from InterfaceResourceCode
     * @return boolean
     */
    @Override
    public boolean check(String resourceCode) {
        if (UserContext.getUser().containInterfaceResource(ApiResourceCodeConstants.ALL_INTERFACE_RESOURCE)){
            return true;
        }
        return UserContext.getUser().containInterfaceResource(resourceCode);
    }
}
