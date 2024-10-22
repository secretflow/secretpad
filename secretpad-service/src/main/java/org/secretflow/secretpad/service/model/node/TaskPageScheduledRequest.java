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

package org.secretflow.secretpad.service.model.node;

import org.secretflow.secretpad.common.annotation.JpaQuery;
import org.secretflow.secretpad.service.model.common.SecretPadPageRequest;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author yutu
 * @date 2023/08/03
 */
@Getter
@Setter
@ToString
public class TaskPageScheduledRequest extends SecretPadPageRequest {

    /**
     * scheduleTaskId
     */
    @JpaQuery(type = JpaQuery.Type.INNER_LIKE, blurry = "scheduleTaskId")
    private String search;

    /**
     * scheduleId
     */
    @JpaQuery(type = JpaQuery.Type.EQUAL, blurry = "scheduleId")
    private String scheduleId;

}