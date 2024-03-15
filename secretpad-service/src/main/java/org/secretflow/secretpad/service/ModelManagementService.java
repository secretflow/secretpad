/*
 * Copyright 2024 Ant Group Co., Ltd.
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

import org.secretflow.secretpad.service.model.model.*;
import org.secretflow.secretpad.service.model.serving.ServingDetailVO;

import java.util.List;

/**
 * @author chenmingliang
 * @date 2024/01/18
 */
public interface ModelManagementService {
    ModelPackListVO modelPackPage(QueryModelPageRequest queryModelPageRequest);

    ModelPackDetailVO modelPackDetail(String modelId, String projectId);

    ModelPackInfoVO modelPackInfo(String modelId, String projectId);

    ModelPartiesVO modelParties(String projectId, String outputId, String graphNodeId);

    void createModelServing(String projectId, String modelId, List<CreateModelServingRequest.PartyConfig> partyConfigs);

    ServingDetailVO queryModelServingDetail(String servingId);

    void deleteModelServing(String servingId);

    void discardModelPack(String modelId);

    void deleteModelPack(String nodeId, String modelId);
}
