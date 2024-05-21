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

package org.secretflow.secretpad.service.test;

import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.persistence.entity.ProjectGraphNodeKusciaParamsDO;
import org.secretflow.secretpad.persistence.repository.ProjectGraphNodeKusciaParamsRepository;
import org.secretflow.secretpad.service.impl.ModelExportServiceImpl;
import org.secretflow.secretpad.service.model.model.export.ModelComponent;
import org.secretflow.secretpad.service.model.model.export.ModelExportPackageRequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

/**
 * @author yutu
 * @date 2024/02/26
 */
@ExtendWith(MockitoExtension.class)
public class ModelExportServiceImplTest {

    @Mock
    ProjectGraphNodeKusciaParamsRepository projectGraphNodeKusciaParamsRepository;

    @Test
    void buildNodeEvalParamTest() {
        ModelExportServiceImpl modelExportService = new ModelExportServiceImpl();
        modelExportService.setProjectGraphNodeKusciaParamsRepository(projectGraphNodeKusciaParamsRepository);
        ModelExportPackageRequest request = ModelExportPackageRequest.builder()
                .modelName("test")
                .projectId("test")
                .graphId("test")
                .graphNodeOutPutId("test")
                .modelComponent(List.of(ModelComponent.builder().domain("preprocessing").version("0.0.1").name("vert_bin_substitution").graphNodeId("kdnuqtkw-node-11").build()))
                .build();
        ProjectGraphNodeKusciaParamsDO projectGraphNodeKusciaParamsDO = ProjectGraphNodeKusciaParamsDO.builder()
                .inputs(JsonUtils.toJSONString(List.of("1")))
                .outputs(JsonUtils.toJSONString(List.of("1")))
                .nodeEvalParam("")
                .build();
        when(projectGraphNodeKusciaParamsRepository.findByUpk(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(projectGraphNodeKusciaParamsDO));
        modelExportService.buildNodeEvalParam(request);
    }
}