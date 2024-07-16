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

import org.secretflow.secretpad.common.constant.DomainDatasourceConstants;
import org.secretflow.secretpad.common.enums.ModelStatsEnum;
import org.secretflow.secretpad.common.errorcode.ProjectErrorCode;
import org.secretflow.secretpad.common.errorcode.SystemErrorCode;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.persistence.entity.FeatureTableDO;
import org.secretflow.secretpad.persistence.entity.NodeDO;
import org.secretflow.secretpad.persistence.entity.ProjectModelPackDO;
import org.secretflow.secretpad.persistence.entity.ProjectModelServingDO;
import org.secretflow.secretpad.persistence.repository.FeatureTableRepository;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.ProjectModelPackRepository;
import org.secretflow.secretpad.persistence.repository.ProjectModelServiceRepository;
import org.secretflow.secretpad.service.model.model.*;
import org.secretflow.secretpad.service.model.serving.ResourceVO;
import org.secretflow.secretpad.web.utils.FakerUtils;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.Domaindata;
import org.secretflow.v1alpha1.kusciaapi.Serving;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * @author chenmingliang
 * @date 2024/02/20
 */
public class ModelManagementControllerTest extends ControllerTest {

    @MockBean
    private ProjectModelPackRepository projectModelPackRepository;

    @MockBean
    private KusciaGrpcClientAdapter kusciaGrpcClientAdapter;

    @MockBean
    private FeatureTableRepository featureTableRepository;

    @MockBean
    private ProjectModelServiceRepository projectModelServiceRepository;

    @MockBean
    private NodeRepository nodeRepository;

    @Test
    public void modelPackPage() throws Exception {
        assertResponse(() -> {
            QueryModelPageRequest queryModelPageRequest = FakerUtils.fake(QueryModelPageRequest.class);
            queryModelPageRequest.setSort(null);
            queryModelPageRequest.setPage(1);
            queryModelPageRequest.setSize(100);


            ProjectModelPackDO projectModelPackDO = FakerUtils.fake(ProjectModelPackDO.class);
            projectModelPackDO.setModelStats(ModelStatsEnum.PUBLISHING.getCode());
            Page<ProjectModelPackDO> page = new PageImpl<>(Lists.newArrayList(projectModelPackDO));
            Mockito.when(projectModelPackRepository.findAll(Mockito.<Specification<ProjectModelPackDO>>any(), Mockito.eq(queryModelPageRequest.of()))).thenReturn(page);


            Serving.QueryServingResponse queryServingResponse = Serving.QueryServingResponse.newBuilder().setData(Serving.QueryServingResponseData.newBuilder().setStatus(Serving.ServingStatusDetail.newBuilder().setState("Available")).build()).setStatus(Common.Status.newBuilder().setCode(0).build()).build();
            Mockito.when(kusciaGrpcClientAdapter.queryServing(Serving.QueryServingRequest.newBuilder().setServingId(projectModelPackDO.getServingId()).build())).thenReturn(queryServingResponse);

            ProjectModelServingDO projectModelServingDO = FakerUtils.fake(ProjectModelServingDO.class);
            Mockito.when(projectModelServiceRepository.findById(projectModelPackDO.getServingId())).thenReturn(Optional.of(projectModelServingDO));
            queryModelPageRequest.setModelStats(ModelStatsEnum.PUBLISHED.name());
            return MockMvcRequestBuilders.post(getMappingUrl(ModelManagementController.class, "modelPackPage", QueryModelPageRequest.class))
                    .content(JsonUtils.toJSONString(queryModelPageRequest));
        });
    }


    @Test
    public void modelPackPageServingStatusFail() throws Exception {
        assertResponse(() -> {
            QueryModelPageRequest queryModelPageRequest = FakerUtils.fake(QueryModelPageRequest.class);
            queryModelPageRequest.setSort(null);
            queryModelPageRequest.setPage(1);
            queryModelPageRequest.setSize(100);


            ProjectModelPackDO projectModelPackDO = FakerUtils.fake(ProjectModelPackDO.class);
            projectModelPackDO.setModelStats(ModelStatsEnum.PUBLISHING.getCode());
            Page<ProjectModelPackDO> page = new PageImpl<>(Lists.newArrayList(projectModelPackDO));
            Mockito.when(projectModelPackRepository.findAll(Mockito.<Specification<ProjectModelPackDO>>any(), Mockito.eq(queryModelPageRequest.of()))).thenReturn(page);


            Serving.QueryServingResponse queryServingResponse = Serving.QueryServingResponse.newBuilder().setData(Serving.QueryServingResponseData.newBuilder().setStatus(Serving.ServingStatusDetail.newBuilder().setState("Failed")).build()).setStatus(Common.Status.newBuilder().setCode(0).build()).build();
            Mockito.when(kusciaGrpcClientAdapter.queryServing(Serving.QueryServingRequest.newBuilder().setServingId(projectModelPackDO.getServingId()).build())).thenReturn(queryServingResponse);

            ProjectModelServingDO projectModelServingDO = FakerUtils.fake(ProjectModelServingDO.class);
            Mockito.when(projectModelServiceRepository.findById(projectModelPackDO.getServingId())).thenReturn(Optional.of(projectModelServingDO));
            queryModelPageRequest.setModelStats(ModelStatsEnum.PUBLISHED.name());
            return MockMvcRequestBuilders.post(getMappingUrl(ModelManagementController.class, "modelPackPage", QueryModelPageRequest.class))
                    .content(JsonUtils.toJSONString(queryModelPageRequest));
        });
    }

    @Test
    public void modelPackDetailError() throws Exception {
        assertErrorCode(() -> {
            QueryModelDetailRequest queryModelDetailRequest = FakerUtils.fake(QueryModelDetailRequest.class);
            return MockMvcRequestBuilders.post(getMappingUrl(ModelManagementController.class, "modelPackDetail", QueryModelDetailRequest.class))
                    .content(JsonUtils.toJSONString(queryModelDetailRequest));
        }, ProjectErrorCode.PROJECT_MODEL_NOT_FOUND);
    }

    @Test
    public void modelPackDetail() throws Exception {
        assertResponse(() -> {
            QueryModelDetailRequest queryModelDetailRequest = FakerUtils.fake(QueryModelDetailRequest.class);
            ProjectModelPackDO projectModelPackDO = FakerUtils.fake(ProjectModelPackDO.class);
            HashMap<String, String> sampleTables = new HashMap<>();
            sampleTables.put("alice", "alice");
            projectModelPackDO.setSampleTables(JsonUtils.toJSONString(sampleTables));
            String s = """
                                        {
                                              "name": "qbjl-fxlt-model-export-report",
                                              "type": "sf.report",
                                              "systemInfo": {
                                              "app": ""
                                              },
                                              "meta": {
                                              "@type": "type.googleapis.com/secretflow.spec.v1.Report",
                                              "name": "used schemas",
                                              "desc": "duration,job_student,job_unemployed,education,job_entrepreneur,balance,default,job_housemaid,pdays,day,job_blue-collar,job_management,job_self-employed,job_technician,campaign,previous,marital_married,job_retired,housing,marital_divorced,loan,age,job_services,marital_single,month_jul,month_may,poutcome_unknown,month_mar,poutcome_failure,poutcome_success,contact_cellular,month_oct,month_nov,month_jun,month_aug,poutcome_other,contact_unknown,month_feb,month_sep,contact_telephone,month_jan,month_dec,month_apr",
                                              "tabs": [],
                                              "errCode": 0,
                                              "errDetail": ""
                                              },
                                              "dataRefs": []
                                              }
                    """;
            Common.DataColumn dataColumn = Common.DataColumn.newBuilder().setName("age").setType("string").setComment("a").build();
            Domaindata.DomainData domainData = Domaindata.DomainData.newBuilder().putAttributes("dist_data", s).addColumns(dataColumn).build();
            Mockito.when(projectModelPackRepository.findById(queryModelDetailRequest.getModelId())).thenReturn(Optional.of(projectModelPackDO));

            Domaindata.QueryDomainDataResponse queryDomainDataResponse = Domaindata.QueryDomainDataResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(0).build()).setData(domainData).build();
            Mockito.when(kusciaGrpcClientAdapter.queryDomainData(Mockito.any())).thenReturn(queryDomainDataResponse);

            return MockMvcRequestBuilders.post(getMappingUrl(ModelManagementController.class, "modelPackDetail", QueryModelDetailRequest.class))
                    .content(JsonUtils.toJSONString(queryModelDetailRequest));
        });
    }

    @Test
    public void testModelPackInfo() throws Exception {
        assertResponse(() -> {
            List<NodeDO> nodeDOList = buildNodeList();
            ProjectModelPackDO projectModelPackDO = buildModelPack();
            Mockito.when(projectModelPackRepository.findById("cirv-zrhi-model-export")).thenReturn(Optional.of(projectModelPackDO));
            Mockito.when(nodeRepository.findByNodeIdIn(Lists.newArrayList("alice", "bob"))).thenReturn(nodeDOList);
            QueryModelDetailRequest request = new QueryModelDetailRequest();
            request.setModelId("cirv-zrhi-model-export");
            request.setProjectId("abcjfvnx");
            return MockMvcRequestBuilders.post(getMappingUrl(ModelManagementController.class, "modelPackInfo", QueryModelDetailRequest.class))
                    .content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    public void testModelPackInfo1() throws Exception {
        assertResponse(() -> {
            List<NodeDO> nodeDOList = buildNodeList();
            ProjectModelPackDO projectModelPackDO = buildModelPack();
            Mockito.when(projectModelPackRepository.findById("cirv-zrhi-model-export")).thenReturn(Optional.of(projectModelPackDO));
            Mockito.when(nodeRepository.findByNodeIdIn(Lists.newArrayList("alice", "bob"))).thenReturn(nodeDOList);
            QueryModelDetailRequest request = new QueryModelDetailRequest();
            request.setModelId("cirv-zrhi-model-export");
            request.setProjectId("abcjfvnx");
            projectModelPackDO.setServingId("chovyquu");
            ProjectModelServingDO projectModelServingDO = buildProjectModelServing();
            Mockito.when(projectModelServiceRepository.findById("chovyquu")).thenReturn(Optional.of(projectModelServingDO));
            return MockMvcRequestBuilders.post(getMappingUrl(ModelManagementController.class, "modelPackInfo", QueryModelDetailRequest.class))
                    .content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    public void createServingErr() throws Exception {
        assertErrorCode(() -> {
            CreateModelServingRequest createModelServingRequest = FakerUtils.fake(CreateModelServingRequest.class);
            return MockMvcRequestBuilders.post(getMappingUrl(ModelManagementController.class, "createServing", CreateModelServingRequest.class))
                    .content(JsonUtils.toJSONString(createModelServingRequest));
        }, ProjectErrorCode.PROJECT_MODEL_NOT_FOUND);

    }


    @Test
    public void createServing() throws Exception {
        assertResponseWithEmptyData(() -> {

            CreateModelServingRequest createModelServingRequest = FakerUtils.fake(CreateModelServingRequest.class);

            ResourceVO resourceVO = new ResourceVO();
            resourceVO.setMinCPU("2");
            resourceVO.setMaxCPU("3");
            resourceVO.setMinMemory("100Gi");
            resourceVO.setMaxMemory("200Gi");
            createModelServingRequest.getPartyConfigs().get(0).setResources(List.of(resourceVO));

            ProjectModelPackDO projectModelPackDO = FakerUtils.fake(ProjectModelPackDO.class);
            Mockito.when(projectModelPackRepository.findById(createModelServingRequest.getModelId())).thenReturn(Optional.of(projectModelPackDO));
            String nodeId = createModelServingRequest.getPartyConfigs().get(0).getNodeId();
            Domaindata.DomainData domainData = Domaindata.DomainData.newBuilder().setRelativeUri("s").build();
            Domaindata.QueryDomainDataResponse queryDomainDataResponse = Domaindata.QueryDomainDataResponse.newBuilder().setData(domainData).build();
            Mockito.when(kusciaGrpcClientAdapter.queryDomainData(Domaindata.QueryDomainDataRequest.newBuilder().setData(Domaindata.QueryDomainDataRequestData.newBuilder().setDomainId(nodeId).setDomaindataId(createModelServingRequest.getModelId()).build()).build())).thenReturn(queryDomainDataResponse);
            String featureTableId = createModelServingRequest.getPartyConfigs().get(0).getFeatureTableId();

            FeatureTableDO featureTableDO = FakerUtils.fake(FeatureTableDO.class);
            FeatureTableDO.UPK upk = new FeatureTableDO.UPK(featureTableId, nodeId, DomainDatasourceConstants.DEFAULT_HTTP_DATASOURCE_ID);

            featureTableDO.setUpk(upk);
            Mockito.when(featureTableRepository.findById(upk)).thenReturn(Optional.of(featureTableDO));

            Mockito.when(kusciaGrpcClientAdapter.createServing(Mockito.any())).thenReturn(Serving.CreateServingResponse.newBuilder().build());
            return MockMvcRequestBuilders.post(getMappingUrl(ModelManagementController.class, "createServing", CreateModelServingRequest.class))
                    .content(JsonUtils.toJSONString(createModelServingRequest));
        });
    }

    @Test
    public void createServingResourceCpuUnset() throws Exception {
        assertResponseWithEmptyData(() -> {

            CreateModelServingRequest createModelServingRequest = FakerUtils.fake(CreateModelServingRequest.class);

            ResourceVO resourceVO = new ResourceVO();
            resourceVO.setMinCPU("-4.3");
            resourceVO.setMaxCPU("3.2");
            resourceVO.setMinMemory("100.4Gi");
            resourceVO.setMaxMemory("200.5Gi");
            createModelServingRequest.getPartyConfigs().get(0).setResources(List.of(resourceVO));

            ProjectModelPackDO projectModelPackDO = FakerUtils.fake(ProjectModelPackDO.class);
            Mockito.when(projectModelPackRepository.findById(createModelServingRequest.getModelId())).thenReturn(Optional.of(projectModelPackDO));
            String nodeId = createModelServingRequest.getPartyConfigs().get(0).getNodeId();
            Domaindata.DomainData domainData = Domaindata.DomainData.newBuilder().setRelativeUri("s").build();
            Domaindata.QueryDomainDataResponse queryDomainDataResponse = Domaindata.QueryDomainDataResponse.newBuilder().setData(domainData).build();
            Mockito.when(kusciaGrpcClientAdapter.queryDomainData(Domaindata.QueryDomainDataRequest.newBuilder().setData(Domaindata.QueryDomainDataRequestData.newBuilder().setDomainId(nodeId).setDomaindataId(createModelServingRequest.getModelId()).build()).build())).thenReturn(queryDomainDataResponse);
            String featureTableId = createModelServingRequest.getPartyConfigs().get(0).getFeatureTableId();

            FeatureTableDO featureTableDO = FakerUtils.fake(FeatureTableDO.class);
            FeatureTableDO.UPK upk = new FeatureTableDO.UPK(featureTableId, nodeId, DomainDatasourceConstants.DEFAULT_HTTP_DATASOURCE_ID);

            featureTableDO.setUpk(upk);
            Mockito.when(featureTableRepository.findById(upk)).thenReturn(Optional.of(featureTableDO));

            Mockito.when(kusciaGrpcClientAdapter.createServing(Mockito.any())).thenReturn(Serving.CreateServingResponse.newBuilder().build());
            return MockMvcRequestBuilders.post(getMappingUrl(ModelManagementController.class, "createServing", CreateModelServingRequest.class))
                    .content(JsonUtils.toJSONString(createModelServingRequest));
        });
    }

    @Test
    public void createServingResourceCpuFormatError() throws Exception {
        assertErrorCode(() -> {

            CreateModelServingRequest createModelServingRequest = FakerUtils.fake(CreateModelServingRequest.class);

            ResourceVO resourceVO = new ResourceVO();
            resourceVO.setMinCPU("1.3");
            resourceVO.setMaxCPU("illegal");
            resourceVO.setMinMemory("100.1Gi");
            resourceVO.setMaxMemory("200.2Gi");
            createModelServingRequest.getPartyConfigs().get(0).setResources(List.of(resourceVO));

            ProjectModelPackDO projectModelPackDO = FakerUtils.fake(ProjectModelPackDO.class);
            Mockito.when(projectModelPackRepository.findById(createModelServingRequest.getModelId())).thenReturn(Optional.of(projectModelPackDO));
            String nodeId = createModelServingRequest.getPartyConfigs().get(0).getNodeId();
            Domaindata.DomainData domainData = Domaindata.DomainData.newBuilder().setRelativeUri("s").build();
            Domaindata.QueryDomainDataResponse queryDomainDataResponse = Domaindata.QueryDomainDataResponse.newBuilder().setData(domainData).build();
            Mockito.when(kusciaGrpcClientAdapter.queryDomainData(Domaindata.QueryDomainDataRequest.newBuilder().setData(Domaindata.QueryDomainDataRequestData.newBuilder().setDomainId(nodeId).setDomaindataId(createModelServingRequest.getModelId()).build()).build())).thenReturn(queryDomainDataResponse);
            String featureTableId = createModelServingRequest.getPartyConfigs().get(0).getFeatureTableId();

            FeatureTableDO featureTableDO = FakerUtils.fake(FeatureTableDO.class);
            FeatureTableDO.UPK upk = new FeatureTableDO.UPK(featureTableId, nodeId, DomainDatasourceConstants.DEFAULT_HTTP_DATASOURCE_ID);

            featureTableDO.setUpk(upk);
            Mockito.when(featureTableRepository.findById(upk)).thenReturn(Optional.of(featureTableDO));

            Mockito.when(kusciaGrpcClientAdapter.createServing(Mockito.any())).thenReturn(Serving.CreateServingResponse.newBuilder().build());
            return MockMvcRequestBuilders.post(getMappingUrl(ModelManagementController.class, "createServing", CreateModelServingRequest.class))
                    .content(JsonUtils.toJSONString(createModelServingRequest));
        }, SystemErrorCode.VALIDATION_ERROR);
    }

    @Test
    public void createServingResourceCpuValueError() throws Exception {
        assertErrorCode(() -> {

            CreateModelServingRequest createModelServingRequest = FakerUtils.fake(CreateModelServingRequest.class);

            ResourceVO resourceVO = new ResourceVO();
            resourceVO.setMinCPU("3.3");
            resourceVO.setMaxCPU("2.2");
            resourceVO.setMinMemory("300.3Gi");
            resourceVO.setMaxMemory("200.2Gi");
            createModelServingRequest.getPartyConfigs().get(0).setResources(List.of(resourceVO));

            ProjectModelPackDO projectModelPackDO = FakerUtils.fake(ProjectModelPackDO.class);
            Mockito.when(projectModelPackRepository.findById(createModelServingRequest.getModelId())).thenReturn(Optional.of(projectModelPackDO));
            String nodeId = createModelServingRequest.getPartyConfigs().get(0).getNodeId();
            Domaindata.DomainData domainData = Domaindata.DomainData.newBuilder().setRelativeUri("s").build();
            Domaindata.QueryDomainDataResponse queryDomainDataResponse = Domaindata.QueryDomainDataResponse.newBuilder().setData(domainData).build();
            Mockito.when(kusciaGrpcClientAdapter.queryDomainData(Domaindata.QueryDomainDataRequest.newBuilder().setData(Domaindata.QueryDomainDataRequestData.newBuilder().setDomainId(nodeId).setDomaindataId(createModelServingRequest.getModelId()).build()).build())).thenReturn(queryDomainDataResponse);
            String featureTableId = createModelServingRequest.getPartyConfigs().get(0).getFeatureTableId();

            FeatureTableDO featureTableDO = FakerUtils.fake(FeatureTableDO.class);
            FeatureTableDO.UPK upk = new FeatureTableDO.UPK(featureTableId, nodeId, DomainDatasourceConstants.DEFAULT_HTTP_DATASOURCE_ID);

            featureTableDO.setUpk(upk);
            Mockito.when(featureTableRepository.findById(upk)).thenReturn(Optional.of(featureTableDO));

            Mockito.when(kusciaGrpcClientAdapter.createServing(Mockito.any())).thenReturn(Serving.CreateServingResponse.newBuilder().build());
            return MockMvcRequestBuilders.post(getMappingUrl(ModelManagementController.class, "createServing", CreateModelServingRequest.class))
                    .content(JsonUtils.toJSONString(createModelServingRequest));
        }, SystemErrorCode.VALIDATION_ERROR);
    }

    @Test
    public void createServingResourceMemoryUnset() throws Exception {
        assertResponseWithEmptyData(() -> {

            CreateModelServingRequest createModelServingRequest = FakerUtils.fake(CreateModelServingRequest.class);

            ResourceVO resourceVO = new ResourceVO();
            resourceVO.setMinCPU("2.1");
            resourceVO.setMaxCPU("3.3");
            resourceVO.setMinMemory("-300.2Gi");
            resourceVO.setMaxMemory("200.3Gi");
            createModelServingRequest.getPartyConfigs().get(0).setResources(List.of(resourceVO));

            ProjectModelPackDO projectModelPackDO = FakerUtils.fake(ProjectModelPackDO.class);
            Mockito.when(projectModelPackRepository.findById(createModelServingRequest.getModelId())).thenReturn(Optional.of(projectModelPackDO));
            String nodeId = createModelServingRequest.getPartyConfigs().get(0).getNodeId();
            Domaindata.DomainData domainData = Domaindata.DomainData.newBuilder().setRelativeUri("s").build();
            Domaindata.QueryDomainDataResponse queryDomainDataResponse = Domaindata.QueryDomainDataResponse.newBuilder().setData(domainData).build();
            Mockito.when(kusciaGrpcClientAdapter.queryDomainData(Domaindata.QueryDomainDataRequest.newBuilder().setData(Domaindata.QueryDomainDataRequestData.newBuilder().setDomainId(nodeId).setDomaindataId(createModelServingRequest.getModelId()).build()).build())).thenReturn(queryDomainDataResponse);
            String featureTableId = createModelServingRequest.getPartyConfigs().get(0).getFeatureTableId();

            FeatureTableDO featureTableDO = FakerUtils.fake(FeatureTableDO.class);
            FeatureTableDO.UPK upk = new FeatureTableDO.UPK(featureTableId, nodeId, DomainDatasourceConstants.DEFAULT_HTTP_DATASOURCE_ID);

            featureTableDO.setUpk(upk);
            Mockito.when(featureTableRepository.findById(upk)).thenReturn(Optional.of(featureTableDO));

            Mockito.when(kusciaGrpcClientAdapter.createServing(Mockito.any())).thenReturn(Serving.CreateServingResponse.newBuilder().build());
            return MockMvcRequestBuilders.post(getMappingUrl(ModelManagementController.class, "createServing", CreateModelServingRequest.class))
                    .content(JsonUtils.toJSONString(createModelServingRequest));
        });
    }

    @Test
    public void createServingResourceMemoryFormatError() throws Exception {
        assertErrorCode(() -> {

            CreateModelServingRequest createModelServingRequest = FakerUtils.fake(CreateModelServingRequest.class);

            ResourceVO resourceVO = new ResourceVO();
            resourceVO.setMinCPU("2");
            resourceVO.setMaxCPU("3");
            resourceVO.setMinMemory("illegal");
            resourceVO.setMaxMemory("200Gi");
            createModelServingRequest.getPartyConfigs().get(0).setResources(List.of(resourceVO));

            ProjectModelPackDO projectModelPackDO = FakerUtils.fake(ProjectModelPackDO.class);
            Mockito.when(projectModelPackRepository.findById(createModelServingRequest.getModelId())).thenReturn(Optional.of(projectModelPackDO));
            String nodeId = createModelServingRequest.getPartyConfigs().get(0).getNodeId();
            Domaindata.DomainData domainData = Domaindata.DomainData.newBuilder().setRelativeUri("s").build();
            Domaindata.QueryDomainDataResponse queryDomainDataResponse = Domaindata.QueryDomainDataResponse.newBuilder().setData(domainData).build();
            Mockito.when(kusciaGrpcClientAdapter.queryDomainData(Domaindata.QueryDomainDataRequest.newBuilder().setData(Domaindata.QueryDomainDataRequestData.newBuilder().setDomainId(nodeId).setDomaindataId(createModelServingRequest.getModelId()).build()).build())).thenReturn(queryDomainDataResponse);
            String featureTableId = createModelServingRequest.getPartyConfigs().get(0).getFeatureTableId();

            FeatureTableDO featureTableDO = FakerUtils.fake(FeatureTableDO.class);
            FeatureTableDO.UPK upk = new FeatureTableDO.UPK(featureTableId, nodeId, DomainDatasourceConstants.DEFAULT_HTTP_DATASOURCE_ID);

            featureTableDO.setUpk(upk);
            Mockito.when(featureTableRepository.findById(upk)).thenReturn(Optional.of(featureTableDO));

            Mockito.when(kusciaGrpcClientAdapter.createServing(Mockito.any())).thenReturn(Serving.CreateServingResponse.newBuilder().build());
            return MockMvcRequestBuilders.post(getMappingUrl(ModelManagementController.class, "createServing", CreateModelServingRequest.class))
                    .content(JsonUtils.toJSONString(createModelServingRequest));
        }, SystemErrorCode.VALIDATION_ERROR);
    }

    @Test
    public void createServingResourceMemoryValueError() throws Exception {
        assertErrorCode(() -> {

            CreateModelServingRequest createModelServingRequest = FakerUtils.fake(CreateModelServingRequest.class);

            ResourceVO resourceVO = new ResourceVO();
            resourceVO.setMinCPU("2.2");
            resourceVO.setMaxCPU("3.3");
            resourceVO.setMinMemory("300.3Gi");
            resourceVO.setMaxMemory("200.2Gi");
            createModelServingRequest.getPartyConfigs().get(0).setResources(List.of(resourceVO));

            ProjectModelPackDO projectModelPackDO = FakerUtils.fake(ProjectModelPackDO.class);
            Mockito.when(projectModelPackRepository.findById(createModelServingRequest.getModelId())).thenReturn(Optional.of(projectModelPackDO));
            String nodeId = createModelServingRequest.getPartyConfigs().get(0).getNodeId();
            Domaindata.DomainData domainData = Domaindata.DomainData.newBuilder().setRelativeUri("s").build();
            Domaindata.QueryDomainDataResponse queryDomainDataResponse = Domaindata.QueryDomainDataResponse.newBuilder().setData(domainData).build();
            Mockito.when(kusciaGrpcClientAdapter.queryDomainData(Domaindata.QueryDomainDataRequest.newBuilder().setData(Domaindata.QueryDomainDataRequestData.newBuilder().setDomainId(nodeId).setDomaindataId(createModelServingRequest.getModelId()).build()).build())).thenReturn(queryDomainDataResponse);
            String featureTableId = createModelServingRequest.getPartyConfigs().get(0).getFeatureTableId();

            FeatureTableDO featureTableDO = FakerUtils.fake(FeatureTableDO.class);
            FeatureTableDO.UPK upk = new FeatureTableDO.UPK(featureTableId, nodeId, DomainDatasourceConstants.DEFAULT_HTTP_DATASOURCE_ID);

            featureTableDO.setUpk(upk);
            Mockito.when(featureTableRepository.findById(upk)).thenReturn(Optional.of(featureTableDO));

            Mockito.when(kusciaGrpcClientAdapter.createServing(Mockito.any())).thenReturn(Serving.CreateServingResponse.newBuilder().build());
            return MockMvcRequestBuilders.post(getMappingUrl(ModelManagementController.class, "createServing", CreateModelServingRequest.class))
                    .content(JsonUtils.toJSONString(createModelServingRequest));
        }, SystemErrorCode.VALIDATION_ERROR);
    }

    @Test
    public void modelServingErr() throws Exception {
        assertErrorCode(() -> {
            QueryModelServingRequest queryModelServingRequest = FakerUtils.fake(QueryModelServingRequest.class);
            return MockMvcRequestBuilders.post(getMappingUrl(ModelManagementController.class, "modelServing", QueryModelServingRequest.class))
                    .content(JsonUtils.toJSONString(queryModelServingRequest));
        }, ProjectErrorCode.PROJECT_SERVING_NOT_FOUND);
    }

    @Test
    public void modelServing() throws Exception {
        assertResponse(() -> {
            QueryModelServingRequest queryModelServingRequest = FakerUtils.fake(QueryModelServingRequest.class);
            ProjectModelServingDO projectModelServingDO = FakerUtils.fake(ProjectModelServingDO.class);
            String servingInputConfig = "{\n" +
                    "  \"party_configs\": {\n" +
                    "    \"alice\": {\n" +
                    "      \"server_config\": {\n" +
                    "        \"feature_mapping\": {\n" +
                    "          \"marital_divorced\": \"marital_divorced\",\n" +
                    "          \"loan\": \"loan\",\n" +
                    "          \"education\": \"education\",\n" +
                    "          \"previous\": \"previous\",\n" +
                    "          \"housing\": \"housing\",\n" +
                    "          \"job_retired\": \"job_retired\",\n" +
                    "          \"job_unemployed\": \"job_unemployed\",\n" +
                    "          \"marital_single\": \"marital_single\",\n" +
                    "          \"job_services\": \"job_services\",\n" +
                    "          \"duration\": \"duration\",\n" +
                    "          \"job_housemaid\": \"job_housemaid\",\n" +
                    "          \"job_self-employed\": \"job_self-employed\",\n" +
                    "          \"default\": \"default\",\n" +
                    "          \"job_student\": \"job_student\",\n" +
                    "          \"balance\": \"balance\",\n" +
                    "          \"job_technician\": \"job_technician\",\n" +
                    "          \"campaign\": \"campaign\",\n" +
                    "          \"job_blue-collar\": \"job_blue-collar\",\n" +
                    "          \"job_entrepreneur\": \"job_entrepreneur\",\n" +
                    "          \"day\": \"day\",\n" +
                    "          \"age\": \"age\",\n" +
                    "          \"pdays\": \"pdays\",\n" +
                    "          \"job_management\": \"job_management\",\n" +
                    "          \"marital_married\": \"marital_married\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"model_config\": {\n" +
                    "        \"model_id\": \"csou-fmvc-model-export\",\n" +
                    "        \"base_path\": \"/\",\n" +
                    "        \"source_path\": \"/home/kuscia/var/storage/data/1\",\n" +
                    "        \"source_type\": \"ST_FILE\"\n" +
                    "      },\n" +
                    "      \"feature_source_config\": {\n" +
                    "        \"mock_opts\": {\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"channel_desc\": {\n" +
                    "        \"protocol\": \"http\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    \"bob\": {\n" +
                    "      \"server_config\": {\n" +
                    "        \"feature_mapping\": {\n" +
                    "          \"loan\": \"loan\",\n" +
                    "          \"education\": \"education\",\n" +
                    "          \"housing\": \"housing\",\n" +
                    "          \"contact_telephone\": \"contact_telephone\",\n" +
                    "          \"month_dec\": \"month_dec\",\n" +
                    "          \"month_jul\": \"month_jul\",\n" +
                    "          \"poutcome_unknown\": \"poutcome_unknown\",\n" +
                    "          \"job_services\": \"job_services\",\n" +
                    "          \"month_feb\": \"month_feb\",\n" +
                    "          \"month_jun\": \"month_jun\",\n" +
                    "          \"duration\": \"duration\",\n" +
                    "          \"default\": \"default\",\n" +
                    "          \"month_jan\": \"month_jan\",\n" +
                    "          \"poutcome_other\": \"poutcome_other\",\n" +
                    "          \"balance\": \"balance\",\n" +
                    "          \"month_mar\": \"month_mar\",\n" +
                    "          \"month_sep\": \"month_sep\",\n" +
                    "          \"job_blue-collar\": \"job_blue-collar\",\n" +
                    "          \"job_entrepreneur\": \"job_entrepreneur\",\n" +
                    "          \"day\": \"day\",\n" +
                    "          \"contact_unknown\": \"contact_unknown\",\n" +
                    "          \"month_nov\": \"month_nov\",\n" +
                    "          \"pdays\": \"pdays\",\n" +
                    "          \"marital_divorced\": \"marital_divorced\",\n" +
                    "          \"previous\": \"previous\",\n" +
                    "          \"job_retired\": \"job_retired\",\n" +
                    "          \"job_unemployed\": \"job_unemployed\",\n" +
                    "          \"marital_single\": \"marital_single\",\n" +
                    "          \"month_oct\": \"month_oct\",\n" +
                    "          \"contact_cellular\": \"contact_cellular\",\n" +
                    "          \"month_may\": \"month_may\",\n" +
                    "          \"month_apr\": \"month_apr\",\n" +
                    "          \"job_housemaid\": \"job_housemaid\",\n" +
                    "          \"job_self-employed\": \"job_self-employed\",\n" +
                    "          \"poutcome_failure\": \"poutcome_failure\",\n" +
                    "          \"poutcome_success\": \"poutcome_success\",\n" +
                    "          \"job_student\": \"job_student\",\n" +
                    "          \"job_technician\": \"job_technician\",\n" +
                    "          \"campaign\": \"campaign\",\n" +
                    "          \"age\": \"age\",\n" +
                    "          \"job_management\": \"job_management\",\n" +
                    "          \"marital_married\": \"marital_married\",\n" +
                    "          \"month_aug\": \"month_aug\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"model_config\": {\n" +
                    "        \"model_id\": \"csou-fmvc-model-export\",\n" +
                    "        \"base_path\": \"/\",\n" +
                    "        \"source_path\": \"/home/kuscia/var/storage/data/1\",\n" +
                    "        \"source_type\": \"ST_FILE\"\n" +
                    "      },\n" +
                    "      \"feature_source_config\": {\n" +
                    "        \"mock_opts\": {\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"channel_desc\": {\n" +
                    "        \"protocol\": \"http\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  }\n" +
                    "}";

            String partiesJson = """
                    [
                      "{
                      \\"domain_id\\": \\"bob\\",
                      \\"app_image\\": \\"sf-serving-image\\",
                      \\"resources\\": [{
                          \\"min_cpu\\": \\"1\\",
                          \\"max_cpu\\": \\"2\\",
                          \\"min_memory\\": \\"1Gi\\",
                          \\"max_memory\\": \\"2Gi\\"
                        }]
                      }",
                      "{
                      \\"domain_id\\": \\"alice\\",
                      \\"app_image\\": \\"sf-serving-image\\",
                      \\"resources\\": [{
                          \\"min_cpu\\": \\"3\\",
                          \\"max_cpu\\": \\"4\\",
                          \\"min_memory\\": \\"3Gi\\",
                          \\"max_memory\\": \\"4Gi\\"
                        }]
                      }"
                    ]
                    """;
            projectModelServingDO.setParties(partiesJson);
            ProjectModelServingDO.PartyEndpoints partyEndpoints = new ProjectModelServingDO.PartyEndpoints();
            partyEndpoints.setEndpoints("127.0.0.1");
            partyEndpoints.setNodeId("alice");
            projectModelServingDO.setPartyEndpoints(Lists.newArrayList(partyEndpoints));
            projectModelServingDO.setServingInputConfig(servingInputConfig);
            Mockito.when(projectModelServiceRepository.findById(queryModelServingRequest.getServingId())).thenReturn(Optional.of(projectModelServingDO));
            NodeDO nodeDO = FakerUtils.fake(NodeDO.class);
            Mockito.when(nodeRepository.findByNodeId("alice")).thenReturn(nodeDO);
            Mockito.when(nodeRepository.findByNodeId("bob")).thenReturn(nodeDO);

            return MockMvcRequestBuilders.post(getMappingUrl(ModelManagementController.class, "modelServing", QueryModelServingRequest.class))
                    .content(JsonUtils.toJSONString(queryModelServingRequest));
        });
    }


    @Test
    public void deleteModelServing() throws Exception {
        assertResponseWithEmptyData(() -> {
            DeleteModelServingRequest deleteModelServingRequest = FakerUtils.fake(DeleteModelServingRequest.class);
            ProjectModelServingDO projectModelServingDO = FakerUtils.fake(ProjectModelServingDO.class);
            projectModelServingDO.setServingStats("success");
            ProjectModelPackDO projectModelPackDO = FakerUtils.fake(ProjectModelPackDO.class);
            Mockito.when(projectModelPackRepository.findByServingId(deleteModelServingRequest.getServingId())).thenReturn(Optional.of(projectModelPackDO));
            projectModelPackDO.setModelStats(ModelStatsEnum.PUBLISHED.getCode());
            Mockito.when(projectModelServiceRepository.findById(deleteModelServingRequest.getServingId())).thenReturn(Optional.of(projectModelServingDO));

            Mockito.when(kusciaGrpcClientAdapter.deleteServing(Mockito.any())).thenReturn(Serving.DeleteServingResponse.newBuilder().build());

            return MockMvcRequestBuilders.post(getMappingUrl(ModelManagementController.class, "deleteModelServing", DeleteModelServingRequest.class))
                    .content(JsonUtils.toJSONString(deleteModelServingRequest));
        });

    }

    @Test
    public void discardModelPack() throws Exception {
        assertResponseWithEmptyData(() -> {
            DiscardModelPackRequest discardModelPackRequest = FakerUtils.fake(DiscardModelPackRequest.class);
            ProjectModelPackDO projectModelPackDO = FakerUtils.fake(ProjectModelPackDO.class);
            Mockito.when(projectModelPackRepository.findById(discardModelPackRequest.getModelId())).thenReturn(Optional.of(projectModelPackDO));
            projectModelPackDO.setModelStats(ModelStatsEnum.OFFLINE.getCode());
            return MockMvcRequestBuilders.post(getMappingUrl(ModelManagementController.class, "discardModelPack", DiscardModelPackRequest.class))
                    .content(JsonUtils.toJSONString(discardModelPackRequest));
        });
    }

    @Test
    public void discardModelPackErr() throws Exception {
        assertErrorCode(() -> {
            DiscardModelPackRequest discardModelPackRequest = FakerUtils.fake(DiscardModelPackRequest.class);
            ProjectModelPackDO projectModelPackDO = FakerUtils.fake(ProjectModelPackDO.class);
            projectModelPackDO.setModelStats(ModelStatsEnum.OFFLINE.getCode());
            return MockMvcRequestBuilders.post(getMappingUrl(ModelManagementController.class, "discardModelPack", DiscardModelPackRequest.class))
                    .content(JsonUtils.toJSONString(discardModelPackRequest));
        }, ProjectErrorCode.PROJECT_MODEL_NOT_FOUND);
    }

    @Test
    public void discardModelPackErr2() throws Exception {
        assertErrorCode(() -> {
            DiscardModelPackRequest discardModelPackRequest = FakerUtils.fake(DiscardModelPackRequest.class);
            ProjectModelPackDO projectModelPackDO = FakerUtils.fake(ProjectModelPackDO.class);
            Mockito.when(projectModelPackRepository.findById(discardModelPackRequest.getModelId())).thenReturn(Optional.of(projectModelPackDO));
            return MockMvcRequestBuilders.post(getMappingUrl(ModelManagementController.class, "discardModelPack", DiscardModelPackRequest.class))
                    .content(JsonUtils.toJSONString(discardModelPackRequest));
        }, ProjectErrorCode.PROJECT_SERVING_NOT_OFFLINE);
    }


    @Test
    public void deleteModelPack() throws Exception {
        assertResponseWithEmptyData(() -> {
            DeleteModelPackRequest deleteModelPackRequest = FakerUtils.fake(DeleteModelPackRequest.class);
            ProjectModelPackDO projectModelPackDO = FakerUtils.fake(ProjectModelPackDO.class);
            Mockito.when(projectModelPackRepository.findById(deleteModelPackRequest.getModelId())).thenReturn(Optional.of(projectModelPackDO));
            projectModelPackDO.setModelStats(ModelStatsEnum.DISCARDED.getCode());
            return MockMvcRequestBuilders.post(getMappingUrl(ModelManagementController.class, "deleteModelPack", DeleteModelPackRequest.class))
                    .content(JsonUtils.toJSONString(deleteModelPackRequest));
        });
    }

    @Test
    public void deleteModelPackErr() throws Exception {
        assertErrorCode(() -> {
            DeleteModelPackRequest deleteModelPackRequest = FakerUtils.fake(DeleteModelPackRequest.class);
            return MockMvcRequestBuilders.post(getMappingUrl(ModelManagementController.class, "deleteModelPack", DeleteModelPackRequest.class))
                    .content(JsonUtils.toJSONString(deleteModelPackRequest));
        }, ProjectErrorCode.PROJECT_MODEL_NOT_FOUND);
    }

    @Test
    public void deleteModelPackErr2() throws Exception {
        assertErrorCode(() -> {
            DeleteModelPackRequest deleteModelPackRequest = FakerUtils.fake(DeleteModelPackRequest.class);
            ProjectModelPackDO projectModelPackDO = FakerUtils.fake(ProjectModelPackDO.class);
            Mockito.when(projectModelPackRepository.findById(deleteModelPackRequest.getModelId())).thenReturn(Optional.of(projectModelPackDO));
            return MockMvcRequestBuilders.post(getMappingUrl(ModelManagementController.class, "deleteModelPack", DeleteModelPackRequest.class))
                    .content(JsonUtils.toJSONString(deleteModelPackRequest));
        }, ProjectErrorCode.PROJECT_SERVING_NOT_OFFLINE);
    }


    private List<NodeDO> buildNodeList() {
        String nodeList = """
                 [
                     {
                         "auth": "alice",
                         "controlNodeId": "alice",
                         "description": "alice",
                         "gmtCreate": "2024-03-20 08:52:18",
                         "gmtModified": "2024-03-20 08:52:18",
                         "id": 1,
                         "isDeleted": false,
                         "masterNodeId": "master",
                         "mode": 1,
                         "name": "alice",
                         "netAddress": "127.0.0.1:28080",
                         "nodeId": "alice",
                         "nodeIds": [
                             "alice"
                         ],
                         "token": "xx",
                         "type": "embedded"
                     },
                     {
                         "auth": "bob",
                         "controlNodeId": "bob",
                         "description": "bob",
                         "gmtCreate": "2024-03-20 08:52:18",
                         "gmtModified": "2024-03-20 08:52:18",
                         "id": 2,
                         "isDeleted": false,
                         "masterNodeId": "master",
                         "mode": 1,
                         "name": "bob",
                         "netAddress": "127.0.0.1:38080",
                         "nodeId": "bob",
                         "nodeIds": [
                             "bob"
                         ],
                         "token": "xx",
                         "type": "embedded"
                     }
                 ]
                """;
        return JsonUtils.toJavaList(nodeList, NodeDO.class);
    }

    private ProjectModelPackDO buildModelPack() {
        String modelPackStr = """
                {
                    "gmtCreate": "2024-03-25 06:12:08",
                    "gmtModified": "2024-03-25 06:12:08",
                    "graphDetail": "{\\"edges\\":[{\\"edgeId\\":\\"drjgnbzr-node-1-output-0__drjgnbzr-node-3-input-0\\",\\"source\\":\\"drjgnbzr-node-1\\",\\"sourceAnchor\\":\\"drjgnbzr-node-1-output-0\\",\\"target\\":\\"drjgnbzr-node-3\\",\\"targetAnchor\\":\\"drjgnbzr-node-3-input-0\\"},{\\"edgeId\\":\\"drjgnbzr-node-2-output-0__drjgnbzr-node-3-input-1\\",\\"source\\":\\"drjgnbzr-node-2\\",\\"sourceAnchor\\":\\"drjgnbzr-node-2-output-0\\",\\"target\\":\\"drjgnbzr-node-3\\",\\"targetAnchor\\":\\"drjgnbzr-node-3-input-1\\"},{\\"edgeId\\":\\"drjgnbzr-node-3-output-0__drjgnbzr-node-4-input-0\\",\\"source\\":\\"drjgnbzr-node-3\\",\\"sourceAnchor\\":\\"drjgnbzr-node-3-output-0\\",\\"target\\":\\"drjgnbzr-node-4\\",\\"targetAnchor\\":\\"drjgnbzr-node-4-input-0\\"},{\\"edgeId\\":\\"drjgnbzr-node-5-output-0__drjgnbzr-node-7-input-0\\",\\"source\\":\\"drjgnbzr-node-5\\",\\"sourceAnchor\\":\\"drjgnbzr-node-5-output-0\\",\\"target\\":\\"drjgnbzr-node-7\\",\\"targetAnchor\\":\\"drjgnbzr-node-7-input-0\\"},{\\"edgeId\\":\\"drjgnbzr-node-6-output-0__drjgnbzr-node-7-input-1\\",\\"source\\":\\"drjgnbzr-node-6\\",\\"sourceAnchor\\":\\"drjgnbzr-node-6-output-0\\",\\"target\\":\\"drjgnbzr-node-7\\",\\"targetAnchor\\":\\"drjgnbzr-node-7-input-1\\"},{\\"edgeId\\":\\"drjgnbzr-node-7-output-0__drjgnbzr-node-8-input-0\\",\\"source\\":\\"drjgnbzr-node-7\\",\\"sourceAnchor\\":\\"drjgnbzr-node-7-output-0\\",\\"target\\":\\"drjgnbzr-node-8\\",\\"targetAnchor\\":\\"drjgnbzr-node-8-input-0\\"},{\\"edgeId\\":\\"drjgnbzr-node-3-output-0__drjgnbzr-node-9-input-0\\",\\"source\\":\\"drjgnbzr-node-3\\",\\"sourceAnchor\\":\\"drjgnbzr-node-3-output-0\\",\\"target\\":\\"drjgnbzr-node-9\\",\\"targetAnchor\\":\\"drjgnbzr-node-9-input-0\\"}],\\"graphId\\":\\"drjgnbzr\\",\\"name\\":\\"联合圈人模板\\",\\"nodes\\":[{\\"codeName\\":\\"read_data/datatable\\",\\"graphNodeId\\":\\"drjgnbzr-node-1\\",\\"inputs\\":[],\\"jobId\\":\\"mtzx\\",\\"label\\":\\"样本表\\",\\"nodeDef\\":{\\"attrPaths\\":[\\"datatable_selected\\"],\\"attrs\\":[{\\"is_na\\":false,\\"s\\":\\"alice-table\\"}],\\"domain\\":\\"read_data\\",\\"name\\":\\"datatable\\",\\"version\\":\\"0.0.1\\"},\\"outputs\\":[\\"drjgnbzr-node-1-output-0\\"],\\"status\\":\\"SUCCEED\\",\\"taskId\\":\\"mtzx-drjgnbzr-node-1\\",\\"x\\":-390,\\"y\\":-210},{\\"codeName\\":\\"read_data/datatable\\",\\"graphNodeId\\":\\"drjgnbzr-node-2\\",\\"inputs\\":[],\\"jobId\\":\\"mtzx\\",\\"label\\":\\"样本表\\",\\"nodeDef\\":{\\"attrPaths\\":[\\"datatable_selected\\"],\\"attrs\\":[{\\"is_na\\":false,\\"s\\":\\"bob-table\\"}],\\"domain\\":\\"read_data\\",\\"name\\":\\"datatable\\",\\"version\\":\\"0.0.1\\"},\\"outputs\\":[\\"drjgnbzr-node-2-output-0\\"],\\"status\\":\\"SUCCEED\\",\\"taskId\\":\\"mtzx-drjgnbzr-node-2\\",\\"x\\":-150,\\"y\\":-210},{\\"codeName\\":\\"data_prep/psi\\",\\"graphNodeId\\":\\"drjgnbzr-node-3\\",\\"inputs\\":[\\"drjgnbzr-node-1-output-0\\",\\"drjgnbzr-node-2-output-0\\"],\\"jobId\\":\\"jvvw\\",\\"label\\":\\"隐私求交\\",\\"nodeDef\\":{\\"attrPaths\\":[\\"input/receiver_input/key\\",\\"input/sender_input/key\\"],\\"attrs\\":[{\\"is_na\\":false,\\"ss\\":[\\"id1\\"]},{\\"is_na\\":false,\\"ss\\":[\\"id2\\"]}],\\"domain\\":\\"data_prep\\",\\"name\\":\\"psi\\",\\"version\\":\\"0.0.2\\"},\\"outputs\\":[\\"drjgnbzr-node-3-output-0\\"],\\"status\\":\\"SUCCEED\\",\\"taskId\\":\\"jvvw-drjgnbzr-node-3\\",\\"x\\":-260,\\"y\\":-100},{\\"codeName\\":\\"stats/table_statistics\\",\\"graphNodeId\\":\\"drjgnbzr-node-4\\",\\"inputs\\":[\\"drjgnbzr-node-3-output-0\\"],\\"jobId\\":\\"mtzx\\",\\"label\\":\\"全表统计\\",\\"nodeDef\\":{\\"attrPaths\\":[\\"input/input_data/features\\"],\\"attrs\\":[{\\"is_na\\":false,\\"ss\\":[\\"age\\",\\"education\\",\\"default\\",\\"balance\\",\\"housing\\",\\"loan\\",\\"day\\",\\"duration\\",\\"campaign\\",\\"pdays\\",\\"previous\\",\\"job_blue-collar\\",\\"job_entrepreneur\\",\\"job_housemaid\\",\\"job_management\\",\\"job_retired\\",\\"job_self-employed\\",\\"job_services\\",\\"job_student\\",\\"job_technician\\",\\"job_unemployed\\",\\"marital_divorced\\",\\"marital_married\\",\\"marital_single\\",\\"contact_cellular\\",\\"contact_telephone\\",\\"contact_unknown\\",\\"month_apr\\",\\"month_aug\\",\\"month_dec\\",\\"month_feb\\",\\"month_jan\\",\\"month_jul\\",\\"month_jun\\",\\"month_mar\\",\\"month_may\\",\\"month_nov\\",\\"month_oct\\",\\"month_sep\\",\\"poutcome_failure\\",\\"poutcome_other\\",\\"poutcome_success\\",\\"poutcome_unknown\\"]}],\\"domain\\":\\"stats\\",\\"name\\":\\"table_statistics\\",\\"version\\":\\"0.0.2\\"},\\"outputs\\":[\\"drjgnbzr-node-4-output-0\\"],\\"status\\":\\"SUCCEED\\",\\"taskId\\":\\"mtzx-drjgnbzr-node-4\\",\\"x\\":-260,\\"y\\":20},{\\"codeName\\":\\"read_data/datatable\\",\\"graphNodeId\\":\\"drjgnbzr-node-5\\",\\"inputs\\":[],\\"label\\":\\"样本表\\",\\"nodeDef\\":{\\"attrPaths\\":[\\"datatable_selected\\"],\\"attrs\\":[{\\"is_na\\":false,\\"s\\":\\"alice-table\\"}],\\"domain\\":\\"read_data\\",\\"name\\":\\"datatable\\",\\"version\\":\\"0.0.1\\"},\\"outputs\\":[\\"drjgnbzr-node-5-output-0\\"],\\"status\\":\\"STAGING\\",\\"x\\":-810,\\"y\\":-240},{\\"codeName\\":\\"read_data/datatable\\",\\"graphNodeId\\":\\"drjgnbzr-node-6\\",\\"inputs\\":[],\\"label\\":\\"样本表\\",\\"nodeDef\\":{\\"attrPaths\\":[\\"datatable_selected\\"],\\"attrs\\":[{\\"is_na\\":false,\\"s\\":\\"bob-table\\"}],\\"domain\\":\\"read_data\\",\\"name\\":\\"datatable\\",\\"version\\":\\"0.0.1\\"},\\"outputs\\":[\\"drjgnbzr-node-6-output-0\\"],\\"status\\":\\"STAGING\\",\\"x\\":-570,\\"y\\":-240},{\\"codeName\\":\\"data_prep/psi\\",\\"graphNodeId\\":\\"drjgnbzr-node-7\\",\\"inputs\\":[\\"drjgnbzr-node-5-output-0\\",\\"drjgnbzr-node-6-output-0\\"],\\"label\\":\\"隐私求交\\",\\"nodeDef\\":{\\"attrPaths\\":[\\"input/receiver_input/key\\",\\"input/sender_input/key\\"],\\"attrs\\":[{\\"is_na\\":false,\\"ss\\":[\\"id1\\"]},{\\"is_na\\":false,\\"ss\\":[\\"id2\\"]}],\\"domain\\":\\"data_prep\\",\\"name\\":\\"psi\\",\\"version\\":\\"0.0.2\\"},\\"outputs\\":[\\"drjgnbzr-node-7-output-0\\"],\\"status\\":\\"STAGING\\",\\"x\\":-680,\\"y\\":-130},{\\"codeName\\":\\"stats/table_statistics\\",\\"graphNodeId\\":\\"drjgnbzr-node-8\\",\\"inputs\\":[\\"drjgnbzr-node-7-output-0\\"],\\"label\\":\\"全表统计\\",\\"nodeDef\\":{\\"attrPaths\\":[\\"input/input_data/features\\"],\\"attrs\\":[{\\"is_na\\":false,\\"ss\\":[\\"age\\",\\"education\\",\\"default\\",\\"balance\\",\\"housing\\",\\"loan\\",\\"day\\",\\"duration\\",\\"campaign\\",\\"pdays\\",\\"previous\\",\\"job_blue-collar\\",\\"job_entrepreneur\\",\\"job_housemaid\\",\\"job_management\\",\\"job_retired\\",\\"job_self-employed\\",\\"job_services\\",\\"job_student\\",\\"job_technician\\",\\"job_unemployed\\",\\"marital_divorced\\",\\"marital_married\\",\\"marital_single\\",\\"contact_cellular\\",\\"contact_telephone\\",\\"contact_unknown\\",\\"month_apr\\",\\"month_aug\\",\\"month_dec\\",\\"month_feb\\",\\"month_jan\\",\\"month_jul\\",\\"month_jun\\",\\"month_mar\\",\\"month_may\\",\\"month_nov\\",\\"month_oct\\",\\"month_sep\\",\\"poutcome_failure\\",\\"poutcome_other\\",\\"poutcome_success\\",\\"poutcome_unknown\\"]}],\\"domain\\":\\"stats\\",\\"name\\":\\"table_statistics\\",\\"version\\":\\"0.0.2\\"},\\"outputs\\":[\\"drjgnbzr-node-8-output-0\\"],\\"status\\":\\"STAGING\\",\\"x\\":-680,\\"y\\":-10},{\\"codeName\\":\\"ml.train/ss_sgd_train\\",\\"graphNodeId\\":\\"drjgnbzr-node-9\\",\\"inputs\\":[\\"drjgnbzr-node-3-output-0\\"],\\"jobId\\":\\"wcce\\",\\"label\\":\\"逻辑回归训练\\",\\"nodeDef\\":{\\"attrPaths\\":[\\"input/train_dataset/feature_selects\\",\\"input/train_dataset/label\\",\\"epochs\\",\\"learning_rate\\",\\"batch_size\\",\\"sig_type\\",\\"reg_type\\",\\"penalty\\",\\"l2_norm\\",\\"eps\\"],\\"attrs\\":[{\\"is_na\\":false,\\"ss\\":[\\"contact_cellular\\",\\"contact_telephone\\",\\"contact_unknown\\",\\"month_apr\\",\\"month_aug\\",\\"month_dec\\",\\"month_feb\\",\\"month_jan\\",\\"month_jul\\",\\"month_jun\\",\\"month_mar\\",\\"month_may\\",\\"month_nov\\",\\"month_oct\\",\\"month_sep\\",\\"poutcome_failure\\",\\"poutcome_other\\",\\"poutcome_success\\",\\"poutcome_unknown\\",\\"age\\",\\"education\\",\\"default\\",\\"balance\\",\\"housing\\",\\"loan\\",\\"day\\",\\"duration\\",\\"campaign\\",\\"pdays\\",\\"previous\\",\\"job_blue-collar\\",\\"job_entrepreneur\\",\\"job_housemaid\\",\\"job_management\\",\\"job_retired\\",\\"job_self-employed\\",\\"job_services\\",\\"job_student\\",\\"job_technician\\",\\"job_unemployed\\",\\"marital_divorced\\",\\"marital_married\\",\\"marital_single\\"]},{\\"is_na\\":false,\\"ss\\":[\\"y\\"]},{\\"i64\\":10,\\"is_na\\":false},{\\"f\\":0.1,\\"is_na\\":false},{\\"i64\\":1024,\\"is_na\\":false},{\\"is_na\\":false,\\"s\\":\\"t1\\"},{\\"is_na\\":false,\\"s\\":\\"logistic\\"},{\\"is_na\\":false,\\"s\\":\\"None\\"},{\\"f\\":0.5,\\"is_na\\":false},{\\"f\\":0.001,\\"is_na\\":false}],\\"domain\\":\\"ml.train\\",\\"name\\":\\"ss_sgd_train\\",\\"version\\":\\"0.0.1\\"},\\"outputs\\":[\\"drjgnbzr-node-9-output-0\\"],\\"status\\":\\"SUCCEED\\",\\"taskId\\":\\"wcce-drjgnbzr-node-9\\",\\"x\\":-470,\\"y\\":30}],\\"projectId\\":\\"abcjfvnx\\"}",
                    "id": 1,
                    "initiator": "kuscia-system",
                    "isDeleted": false,
                    "modelDesc": "ssz",
                    "modelId": "cirv-zrhi-model-export",
                    "modelList": [
                        "drjgnbzr-node-9"
                    ],
                    "modelName": "tests",
                    "modelReportId": "cirv-zrhi-model-export-report",
                    "modelStats": 0,
                    "nodeIds": [

                    ],
                    "projectId": "abcjfvnx",
                    "sampleTables": "{\\"alice\\":\\"alice-table\\",\\"bob\\":\\"bob-table\\"}",
                    "trainId": "wcce-drjgnbzr-node-9-output-0",
                    "partyDataSources":[{"partyId":"alice","datasource":"alice-table"},{"partyId":"bob","datasource":"bob-table"}]
                }
                """;

        return JsonUtils.toJavaObject(modelPackStr, ProjectModelPackDO.class);
    }

    private ProjectModelServingDO buildProjectModelServing() {
        String servingStr = """
                {
                    "gmtCreate": "2024-03-25 06:36:04",
                    "gmtModified": "2024-03-25 06:36:04",
                    "id": 1,
                    "initiator": "alice",
                    "isDeleted": false,
                    "nodeIds": [

                    ],
                    "parties": "[\\"{\\\\n  \\\\\\"domain_id\\\\\\": \\\\\\"alice\\\\\\",\\\\n  \\\\\\"app_image\\\\\\": \\\\\\"sf-serving-image\\\\\\"\\\\n}\\",\\"{\\\\n  \\\\\\"domain_id\\\\\\": \\\\\\"bob\\\\\\",\\\\n  \\\\\\"app_image\\\\\\": \\\\\\"sf-serving-image\\\\\\"\\\\n}\\"]",
                    "partyEndpoints": [
                        {
                            "endpoints": "chovyquu-service.alice.svc:53508",
                            "nodeId": "alice"
                        },
                        {
                            "endpoints": "chovyquu-service.bob.svc:53508",
                            "nodeId": "bob"
                        }
                    ],
                    "projectId": "abcjfvnx",
                    "servingId": "chovyquu",
                    "servingInputConfig": "{\\n  \\"party_configs\\": {\\n    \\"alice\\": {\\n      \\"server_config\\": {\\n        \\"feature_mapping\\": {\\n          \\"marital_divorced\\": \\"marital_divorced\\",\\n          \\"loan\\": \\"loan\\",\\n          \\"education\\": \\"education\\",\\n          \\"previous\\": \\"previous\\",\\n          \\"housing\\": \\"housing\\",\\n          \\"job_retired\\": \\"job_retired\\",\\n          \\"job_unemployed\\": \\"job_unemployed\\",\\n          \\"marital_single\\": \\"marital_single\\",\\n          \\"job_services\\": \\"job_services\\",\\n          \\"job_self-employed\\": \\"job_self-employed\\",\\n          \\"job_housemaid\\": \\"job_housemaid\\",\\n          \\"duration\\": \\"duration\\",\\n          \\"default\\": \\"default\\",\\n          \\"job_student\\": \\"job_student\\",\\n          \\"balance\\": \\"balance\\",\\n          \\"job_technician\\": \\"job_technician\\",\\n          \\"campaign\\": \\"campaign\\",\\n          \\"job_blue-collar\\": \\"job_blue-collar\\",\\n          \\"job_entrepreneur\\": \\"job_entrepreneur\\",\\n          \\"day\\": \\"day\\",\\n          \\"job_management\\": \\"job_management\\",\\n          \\"age\\": \\"age\\",\\n          \\"pdays\\": \\"pdays\\",\\n          \\"marital_married\\": \\"marital_married\\"\\n        }\\n      },\\n      \\"model_config\\": {\\n        \\"model_id\\": \\"cirv-zrhi-model-export\\",\\n        \\"base_path\\": \\"/\\",\\n        \\"source_path\\": \\"/home/kuscia/var/storage/data/ty_1711347106663\\",\\n        \\"source_type\\": \\"ST_FILE\\"\\n      },\\n      \\"feature_source_config\\": {\\n        \\"mock_opts\\": {\\n        }\\n      },\\n      \\"channel_desc\\": {\\n        \\"protocol\\": \\"http\\"\\n      }\\n    },\\n    \\"bob\\": {\\n      \\"server_config\\": {\\n        \\"feature_mapping\\": {\\n          \\"contact_telephone\\": \\"contact_telephone\\",\\n          \\"month_dec\\": \\"month_dec\\",\\n          \\"month_jul\\": \\"month_jul\\",\\n          \\"month_oct\\": \\"month_oct\\",\\n          \\"poutcome_unknown\\": \\"poutcome_unknown\\",\\n          \\"contact_cellular\\": \\"contact_cellular\\",\\n          \\"month_may\\": \\"month_may\\",\\n          \\"month_apr\\": \\"month_apr\\",\\n          \\"month_feb\\": \\"month_feb\\",\\n          \\"month_jun\\": \\"month_jun\\",\\n          \\"poutcome_failure\\": \\"poutcome_failure\\",\\n          \\"poutcome_success\\": \\"poutcome_success\\",\\n          \\"month_jan\\": \\"month_jan\\",\\n          \\"poutcome_other\\": \\"poutcome_other\\",\\n          \\"month_sep\\": \\"month_sep\\",\\n          \\"month_mar\\": \\"month_mar\\",\\n          \\"month_nov\\": \\"month_nov\\",\\n          \\"contact_unknown\\": \\"contact_unknown\\",\\n          \\"month_aug\\": \\"month_aug\\"\\n        }\\n      },\\n      \\"model_config\\": {\\n        \\"model_id\\": \\"cirv-zrhi-model-export\\",\\n        \\"base_path\\": \\"/\\",\\n        \\"source_path\\": \\"/home/kuscia/var/storage/data/ty_1711347106663\\",\\n        \\"source_type\\": \\"ST_FILE\\"\\n      },\\n      \\"feature_source_config\\": {\\n        \\"mock_opts\\": {\\n        }\\n      },\\n      \\"channel_desc\\": {\\n        \\"protocol\\": \\"http\\"\\n      }\\n    }\\n  }\\n}",
                    "servingStats": "success"
                }
                """;
        return JsonUtils.toJavaObject(servingStr, ProjectModelServingDO.class);
    }
}
