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

package org.secretflow.secretpad.service;

import org.secretflow.secretpad.service.model.auth.NodeUserCreateRequest;
import org.secretflow.secretpad.service.model.auth.NodeUserListByNodeIdRequest;
import org.secretflow.secretpad.service.model.auth.ResetNodeUserPwdRequest;
import org.secretflow.secretpad.service.model.auth.UserVO;

import java.util.List;

/**
 * @author beiwei
 * @date 2023/9/14
 */
public interface NodeUserService {
    /**
     * create node user for center platform
     *
     * @param request request
     */
    void create(NodeUserCreateRequest request);

    /**
     * reset node user password for center platform
     *
     * @param userRequest request
     */
    void resetPassword(ResetNodeUserPwdRequest userRequest);

    /**
     * query node user for center platform
     *
     * @param createAccountRequest request
     * @return users
     */
    List<UserVO> listByNodeId(NodeUserListByNodeIdRequest createAccountRequest);
}
