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

import org.secretflow.secretpad.common.constant.resource.ApiResourceCodeConstants;
import org.secretflow.secretpad.common.enums.DataSourceTypeEnum;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.persistence.entity.ProjectDO;
import org.secretflow.secretpad.persistence.entity.ProjectGraphDO;
import org.secretflow.secretpad.persistence.entity.ProjectJobDO;
import org.secretflow.secretpad.persistence.entity.ProjectResultDO;
import org.secretflow.secretpad.persistence.repository.ProjectGraphRepository;
import org.secretflow.secretpad.persistence.repository.ProjectJobRepository;
import org.secretflow.secretpad.persistence.repository.ProjectRepository;
import org.secretflow.secretpad.persistence.repository.ProjectResultRepository;
import org.secretflow.secretpad.service.model.data.CreateDataRequest;
import org.secretflow.secretpad.service.model.data.DownloadDataRequest;
import org.secretflow.secretpad.web.utils.FakerUtils;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.secretflow.v1alpha1.kusciaapi.Domaindata;
import org.secretflow.v1alpha1.kusciaapi.Domaindatasource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.secretflow.secretpad.manager.integration.datatable.AbstractDatatableManager.DATA_TYPE_TABLE;
import static org.secretflow.secretpad.manager.integration.datatable.AbstractDatatableManager.DATA_VENDOR_MANUAL;

/**
 * DataController test
 *
 * @author guyu
 * @date 2023/8/1
 */
@TestPropertySource(properties = {
        "secretpad.data.dir-path=./tmp/"
})
class DataControllerTest extends ControllerTest {

    @MockBean
    private ProjectResultRepository projectResultRepository;

    @MockBean
    private ProjectJobRepository projectJobRepository;

    @MockBean
    private ProjectGraphRepository projectGraphRepository;

    @MockBean
    private ProjectRepository projectRepository;
    @MockBean
    private KusciaGrpcClientAdapter kusciaGrpcClientAdapter;

    @BeforeEach
    public void setUp() {
        Domaindatasource.QueryDomainDataSourceResponse response = Domaindatasource.QueryDomainDataSourceResponse.newBuilder()
                .setData(Domaindatasource.DomainDataSource.newBuilder()
                        .setType(StringUtils.toRootLowerCase(DataSourceTypeEnum.OSS.name()))
                        .setDatasourceId("datasource")
                        .build())
                .build();
        Mockito.when(kusciaGrpcClientAdapter.queryDomainDataSource(Mockito.any(Domaindatasource.QueryDomainDataSourceRequest.class))).thenReturn(response);
    }

    @Test
    void upload() throws Exception {
        assertMultipartResponse(() -> {
            String nodeId = FakerUtils.fake(String.class);
            MockMultipartFile file = new MockMultipartFile("file", "test.csv", MediaType.APPLICATION_JSON_VALUE, "some xml".getBytes());
            return MockMvcRequestBuilders.multipart(getMappingUrl(DataController.class, "upload", String.class, MultipartFile.class))
                    .file(file).contentType(MediaType.MULTIPART_FORM_DATA_VALUE).param("Node-Id", nodeId);
        });
    }

    @Test
    void createData() throws Exception {
        assertResponse(() -> {
            CreateDataRequest createDataRequest = FakerUtils.fake(CreateDataRequest.class);
            createDataRequest.setNodeId("alice");
            createDataRequest.setDatasourceType("local");
            createDataRequest.setDatasourceName("本地数据源");
            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.DATA_CREATE));

            Domaindata.ListDomainDataResponse response = Domaindata.ListDomainDataResponse.newBuilder()
                    .setData(
                            Domaindata.DomainDataList.newBuilder().build()
                    )
                    .build();
            Mockito.when(kusciaGrpcClientAdapter.listDomainData(Domaindata.ListDomainDataRequest.newBuilder()
                            .setData(
                                    Domaindata.ListDomainDataRequestData.newBuilder()
                                            .setDomaindataType(DATA_TYPE_TABLE)
                                            .setDomaindataVendor(DATA_VENDOR_MANUAL)
                                            .setDomainId(createDataRequest.getNodeId())
                                            .build()
                            )
                            .build()))
                    .thenReturn(response);

            Domaindata.CreateDomainDataResponse domainData = Domaindata.CreateDomainDataResponse.newBuilder()
                    .setData(
                            Domaindata.CreateDomainDataResponseData.newBuilder()
                                    .setDomaindataId(createDataRequest.getNodeId())
                                    .build()
                    )
                    .build();
            Mockito.when(kusciaGrpcClientAdapter.createDomainData(Mockito.any()))
                    .thenReturn(domainData);

            return MockMvcRequestBuilders.post(getMappingUrl(DataController.class, "createData", CreateDataRequest.class))
                    .content(JsonUtils.toJSONString(createDataRequest));
        });
    }

    @Test
    void downloadFileExists() throws Exception {
        assertResponseWithEmptyContent(() -> {
            String userAgent = FakerUtils.fake(String.class);
            DownloadDataRequest request = FakerUtils.fake(DownloadDataRequest.class);
            request.setNodeId("mockMvcNodeId");

            ProjectResultDO projectResultDO = FakerUtils.fake(ProjectResultDO.class);
            projectResultDO.setGmtCreate(LocalDateTime.now());
            Mockito.when(projectResultRepository.findByNodeIdAndRefId(request.getNodeId(), request.getDomainDataId()))
                    .thenReturn(Optional.of(projectResultDO));

            ProjectDO projectDO = FakerUtils.fake(ProjectDO.class);
            Mockito.when(projectRepository.findById(projectResultDO.getUpk().getProjectId()))
                    .thenReturn(Optional.of(projectDO));

            ProjectJobDO projectJobDO = FakerUtils.fake(ProjectJobDO.class);
            Mockito.when(projectJobRepository.findByJobId(projectResultDO.getJobId()))
                    .thenReturn(Optional.of(projectJobDO));

            ProjectGraphDO projectGraphDO = FakerUtils.fake(ProjectGraphDO.class);
            Mockito.when(projectGraphRepository.findByGraphId(projectJobDO.getGraphId(), projectDO.getProjectId()))
                    .thenReturn(Optional.of(projectGraphDO));

            Domaindata.QueryDomainDataResponse domainDataResponse = Domaindata.QueryDomainDataResponse.newBuilder()
                    .setData(
                            Domaindata.DomainData.newBuilder()
                                    .setRelativeUri("mockFile")
                                    .build()
                    )
                    .build();
            Mockito.when(kusciaGrpcClientAdapter.queryDomainData(Domaindata.QueryDomainDataRequest.newBuilder()
                            .setData(Domaindata.QueryDomainDataRequestData.newBuilder()
                                    .setDomainId(request.getNodeId())
                                    .setDomaindataId(request.getDomainDataId())
                                    .build())
                            .build()))
                    .thenReturn(domainDataResponse);

            return MockMvcRequestBuilders.post(getMappingUrl(DataController.class, "download", HttpServletResponse.class, DownloadDataRequest.class))
                    .header("User-Agent", userAgent).content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void downloadFiLeNotExists() throws Exception {
        assertResponseWithEmptyContent(() -> {
            String userAgent = FakerUtils.fake(String.class);
            DownloadDataRequest request = FakerUtils.fake(DownloadDataRequest.class);
            request.setNodeId("mockMvcNodeId");

            ProjectResultDO projectResultDO = FakerUtils.fake(ProjectResultDO.class);
            projectResultDO.setGmtCreate(LocalDateTime.now());
            Mockito.when(projectResultRepository.findByNodeIdAndRefId(request.getNodeId(), request.getDomainDataId()))
                    .thenReturn(Optional.of(projectResultDO));

            ProjectDO projectDO = FakerUtils.fake(ProjectDO.class);
            Mockito.when(projectRepository.findById(projectResultDO.getUpk().getProjectId()))
                    .thenReturn(Optional.of(projectDO));

            ProjectJobDO projectJobDO = FakerUtils.fake(ProjectJobDO.class);
            Mockito.when(projectJobRepository.findByJobId(projectResultDO.getJobId()))
                    .thenReturn(Optional.of(projectJobDO));

            ProjectGraphDO projectGraphDO = FakerUtils.fake(ProjectGraphDO.class);
            Mockito.when(projectGraphRepository.findByGraphId(projectJobDO.getGraphId(), projectDO.getProjectId()))
                    .thenReturn(Optional.of(projectGraphDO));

            Domaindata.QueryDomainDataResponse domainDataResponse = Domaindata.QueryDomainDataResponse.newBuilder()
                    .setData(
                            Domaindata.DomainData.newBuilder()
                                    .setRelativeUri(FakerUtils.fake(String.class))
                                    .build()
                    )
                    .build();
            Mockito.when(kusciaGrpcClientAdapter.queryDomainData(Domaindata.QueryDomainDataRequest.newBuilder()
                            .setData(Domaindata.QueryDomainDataRequestData.newBuilder()
                                    .setDomainId(request.getNodeId())
                                    .setDomaindataId(request.getDomainDataId())
                                    .build())
                            .build()))
                    .thenReturn(domainDataResponse);

            return MockMvcRequestBuilders.post(getMappingUrl(DataController.class, "download", HttpServletResponse.class, DownloadDataRequest.class))
                    .header("User-Agent", userAgent).content(JsonUtils.toJSONString(request));
        });
    }
}