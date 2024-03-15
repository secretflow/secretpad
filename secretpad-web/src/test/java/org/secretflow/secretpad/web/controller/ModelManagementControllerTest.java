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

import org.secretflow.secretpad.common.enums.ModelStatsEnum;
import org.secretflow.secretpad.common.errorcode.ProjectErrorCode;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.persistence.entity.FeatureTableDO;
import org.secretflow.secretpad.persistence.entity.NodeDO;
import org.secretflow.secretpad.persistence.entity.ProjectModelPackDO;
import org.secretflow.secretpad.persistence.entity.ProjectModelServingDO;
import org.secretflow.secretpad.persistence.repository.FeatureTableRepository;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.ProjectModelPackRepository;
import org.secretflow.secretpad.persistence.repository.ProjectModelServiceRepository;
import org.secretflow.secretpad.service.model.model.*;
import org.secretflow.secretpad.web.utils.FakerUtils;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.DomainDataServiceGrpc;
import org.secretflow.v1alpha1.kusciaapi.Domaindata;
import org.secretflow.v1alpha1.kusciaapi.Serving;
import org.secretflow.v1alpha1.kusciaapi.ServingServiceGrpc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.HashMap;
import java.util.Optional;

/**
 * @author chenmingliang
 * @date 2024/02/20
 */
public class ModelManagementControllerTest extends ControllerTest {

    @MockBean
    private ProjectModelPackRepository projectModelPackRepository;

    @MockBean
    private DomainDataServiceGrpc.DomainDataServiceBlockingStub dataStub;

    @MockBean
    private FeatureTableRepository featureTableRepository;

    @MockBean
    private ServingServiceGrpc.ServingServiceBlockingStub servingServiceBlockingStub;

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
            Mockito.when(servingServiceBlockingStub.queryServing(Serving.QueryServingRequest.newBuilder().setServingId(projectModelPackDO.getServingId()).build())).thenReturn(queryServingResponse);

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
            Mockito.when(servingServiceBlockingStub.queryServing(Serving.QueryServingRequest.newBuilder().setServingId(projectModelPackDO.getServingId()).build())).thenReturn(queryServingResponse);

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
            Mockito.when(dataStub.queryDomainData(Mockito.any())).thenReturn(queryDomainDataResponse);

            return MockMvcRequestBuilders.post(getMappingUrl(ModelManagementController.class, "modelPackDetail", QueryModelDetailRequest.class))
                    .content(JsonUtils.toJSONString(queryModelDetailRequest));
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
            ProjectModelPackDO projectModelPackDO = FakerUtils.fake(ProjectModelPackDO.class);
            Mockito.when(projectModelPackRepository.findById(createModelServingRequest.getModelId())).thenReturn(Optional.of(projectModelPackDO));
            String nodeId = createModelServingRequest.getPartyConfigs().get(0).getNodeId();
            Domaindata.DomainData domainData = Domaindata.DomainData.newBuilder().setRelativeUri("s").build();
            Domaindata.QueryDomainDataResponse queryDomainDataResponse = Domaindata.QueryDomainDataResponse.newBuilder().setData(domainData).build();
            Mockito.when(dataStub.queryDomainData(Domaindata.QueryDomainDataRequest.newBuilder().setData(Domaindata.QueryDomainDataRequestData.newBuilder().setDomainId(nodeId).setDomaindataId(createModelServingRequest.getModelId()).build()).build())).thenReturn(queryDomainDataResponse);
            String featureTableId = createModelServingRequest.getPartyConfigs().get(0).getFeatureTableId();

            FeatureTableDO featureTableDO = FakerUtils.fake(FeatureTableDO.class);
            FeatureTableDO.UPK upk = new FeatureTableDO.UPK(featureTableId, nodeId);

            featureTableDO.setUpk(upk);
            Mockito.when(featureTableRepository.findById(upk)).thenReturn(Optional.of(featureTableDO));

            Mockito.when(servingServiceBlockingStub.createServing(Mockito.any())).thenReturn(Serving.CreateServingResponse.newBuilder().build());
            return MockMvcRequestBuilders.post(getMappingUrl(ModelManagementController.class, "createServing", CreateModelServingRequest.class))
                    .content(JsonUtils.toJSONString(createModelServingRequest));
        });

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

            Mockito.when(servingServiceBlockingStub.deleteServing(Mockito.any())).thenReturn(Serving.DeleteServingResponse.newBuilder().build());

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
}
