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

package org.secretflow.secretpad.web.controller;

import org.secretflow.secretpad.service.model.auth.ResetNodeUserPwdRequest;
import org.secretflow.secretpad.service.model.common.SecretPadResponse;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author beiwei
 * @date 2023/9/14
 */
public interface NodeUserControllerInterface {

    SecretPadResponse<String> resetPwd(@Valid @RequestBody ResetNodeUserPwdRequest userRequest);

}
