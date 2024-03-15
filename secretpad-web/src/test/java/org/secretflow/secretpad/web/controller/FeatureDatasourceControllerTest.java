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

package org.secretflow.secretpad.web.controller;

import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.persistence.entity.FeatureTableDO;
import org.secretflow.secretpad.persistence.entity.ProjectFeatureTableDO;
import org.secretflow.secretpad.persistence.repository.FeatureTableRepository;
import org.secretflow.secretpad.persistence.repository.ProjectFeatureTableRepository;
import org.secretflow.secretpad.service.model.datasource.feature.CreateFeatureDatasourceRequest;
import org.secretflow.secretpad.service.model.datasource.feature.ListProjectFeatureDatasourceRequest;
import org.secretflow.secretpad.web.utils.FakerUtils;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;

import static org.secretflow.secretpad.common.errorcode.FeatureTableErrorCode.FEATURE_TABLE_IP_NOT_KNOWN;

/**
 * @author chenmingliang
 * @date 2024/02/19
 */
public class FeatureDatasourceControllerTest extends ControllerTest {

    @MockBean
    private ProjectFeatureTableRepository projectFeatureTableRepository;

    @MockBean
    private FeatureTableRepository featureTableRepository;

    @Test
    public void createFeatureDatasource() throws Exception {
        assertResponseWithEmptyData(() -> {
            CreateFeatureDatasourceRequest createFeatureDatasourceRequest = FakerUtils.fake(CreateFeatureDatasourceRequest.class);
            createFeatureDatasourceRequest.setType("HTTP");
            createFeatureDatasourceRequest.setUrl("http://12.0.0.1");
            return MockMvcRequestBuilders.post(getMappingUrl(FeatureDatasourceController.class, "createFeatureDatasource", CreateFeatureDatasourceRequest.class))
                    .content(JsonUtils.toJSONString(createFeatureDatasourceRequest));
        });
    }

    @Test
    public void createFeatureDatasourceSSRF() throws Exception {
        assertErrorCode(() -> {
            CreateFeatureDatasourceRequest createFeatureDatasourceRequest = FakerUtils.fake(CreateFeatureDatasourceRequest.class);
            createFeatureDatasourceRequest.setType("HTTP");
            createFeatureDatasourceRequest.setUrl("http://10。0。0。1");
            return MockMvcRequestBuilders.post(getMappingUrl(FeatureDatasourceController.class, "createFeatureDatasource", CreateFeatureDatasourceRequest.class))
                    .content(JsonUtils.toJSONString(createFeatureDatasourceRequest));
        }, FEATURE_TABLE_IP_NOT_KNOWN);
    }

    @Test
    public void projectFeatureTableListEmpty() throws Exception {
        assertResponse(() -> {
            ListProjectFeatureDatasourceRequest listProjectFeatureDatasourceRequest = FakerUtils.fake(ListProjectFeatureDatasourceRequest.class);
            return MockMvcRequestBuilders.post(getMappingUrl(FeatureDatasourceController.class, "projectFeatureTableList", ListProjectFeatureDatasourceRequest.class))
                    .content(JsonUtils.toJSONString(listProjectFeatureDatasourceRequest));
        });
    }

    @Test
    public void projectFeatureTableList() throws Exception {
        assertResponse(() -> {
            ListProjectFeatureDatasourceRequest listProjectFeatureDatasourceRequest = FakerUtils.fake(ListProjectFeatureDatasourceRequest.class);
            ProjectFeatureTableDO projectFeatureTableDO = FakerUtils.fake(ProjectFeatureTableDO.class);
            FeatureTableDO featureTableDO = FakerUtils.fake(FeatureTableDO.class);
            featureTableDO.setUpk(new FeatureTableDO.UPK(projectFeatureTableDO.getUpk().getFeatureTableId(), projectFeatureTableDO.getNodeId()));
            Mockito.when(projectFeatureTableRepository.findByNodeIdAndProjectId(listProjectFeatureDatasourceRequest.getNodeId(), listProjectFeatureDatasourceRequest.getProjectId())).thenReturn(Collections.singletonList(projectFeatureTableDO));
            Mockito.when(featureTableRepository.findByFeatureTableIdIn(Lists.newArrayList(projectFeatureTableDO.getUpk().getFeatureTableId()))).thenReturn(Collections.singletonList(featureTableDO));
            return MockMvcRequestBuilders.post(getMappingUrl(FeatureDatasourceController.class, "projectFeatureTableList", ListProjectFeatureDatasourceRequest.class))
                    .content(JsonUtils.toJSONString(listProjectFeatureDatasourceRequest));
        });
    }
}
