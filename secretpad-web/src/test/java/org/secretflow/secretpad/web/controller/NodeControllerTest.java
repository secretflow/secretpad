package org.secretflow.secretpad.web.controller;

import org.secretflow.secretpad.common.errorcode.*;
import org.secretflow.secretpad.common.util.DateTimes;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.model.ResultKind;
import org.secretflow.secretpad.persistence.repository.*;
import org.secretflow.secretpad.service.model.node.CreateNodeRequest;
import org.secretflow.secretpad.service.model.node.GetNodeResultDetailRequest;
import org.secretflow.secretpad.service.model.node.ListNodeResultRequest;
import org.secretflow.secretpad.service.model.node.NodeIdRequest;
import org.secretflow.secretpad.web.utils.FakerUtils;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.Domain;
import org.secretflow.v1alpha1.kusciaapi.DomainDataServiceGrpc;
import org.secretflow.v1alpha1.kusciaapi.DomainServiceGrpc;
import org.secretflow.v1alpha1.kusciaapi.Domaindata;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.*;

/**
 * Node controller test
 *
 * @author xjn
 * @date 2023/8/2
 */
class NodeControllerTest extends ControllerTest {

    private static final String PROJECT_ID = "projectagdasvacaghyhbvscvyjnba";
    private static final String GRAPH_ID = "graphagdasvacaghyhbvscvyjnba";

    @MockBean
    private ProjectResultRepository resultRepository;

    @MockBean
    private ProjectRepository projectRepository;

    @MockBean
    private ProjectJobRepository projectJobRepository;

    @MockBean
    private ProjectGraphRepository projectGraphRepository;

    @MockBean
    private ProjectDatatableRepository datatableRepository;

    @MockBean
    private NodeRepository nodeRepository;

    @MockBean
    private DomainDataServiceGrpc.DomainDataServiceBlockingStub dataStub;

    @MockBean
    private DomainServiceGrpc.DomainServiceBlockingStub domainServiceStub;

    private List<NodeDO> buildNodeDOList() {
        List<NodeDO> nodeDOList = new ArrayList<>();
        nodeDOList.add(NodeDO.builder().nodeId("alice").name("alice").description("alice").auth("alice").build());
        return nodeDOList;
    }

    private List<ProjectResultDO> buildProjectResultDOList() {
        List<ProjectResultDO> projectResultDOList = new ArrayList<>();
        ProjectResultDO.UPK upk = new ProjectResultDO.UPK();
        upk.setKind(ResultKind.FedTable);
        upk.setNodeId("alice");
        upk.setRefId("alice-ref1");
        upk.setProjectId(PROJECT_ID);
        ProjectResultDO projectResultDO = ProjectResultDO.builder().upk(upk).taskId("task-dabgvasfasdasdas").jobId("op-psiv3-dabgvasfasdasdas").build();
        projectResultDO.setGmtCreate(DateTimes.utcFromRfc3339("2023-08-02T08:30:15.235+08:00"));
        projectResultDO.setGmtModified(DateTimes.utcFromRfc3339("2023-08-02T16:30:15.235+08:00"));
        projectResultDOList.add(projectResultDO);
        return projectResultDOList;
    }

    private ProjectJobDO buildProjectJobDO(boolean isTaskEmpty) {
        ProjectJobDO.UPK upk = new ProjectJobDO.UPK();
        upk.setProjectId(PROJECT_ID);
        upk.setJobId("op-psiv3-dabgvasfasdasdas");
        ProjectJobDO projectJobDO = ProjectJobDO.builder().upk(upk).graphId(GRAPH_ID).edges(Collections.emptyList()).build();
        Map<String, ProjectTaskDO> projectTaskDOMap = new HashMap<>();
        ProjectTaskDO.UPK taskUpk = new ProjectTaskDO.UPK();
        if (!isTaskEmpty) {
            taskUpk.setTaskId("task-dabgvasfasdasdas");
            projectTaskDOMap.put("task-dabgvasfasdasdas", ProjectTaskDO.builder().upk(taskUpk).graphNode(buildProjectGraphNodeDO()).build());
        } else {
            taskUpk.setTaskId("task-dabgvasfasdasdasssss");
            projectTaskDOMap.put("task-dabgvasfasdasdasssss", ProjectTaskDO.builder().upk(taskUpk).graphNode(buildProjectGraphNodeDO()).build());
        }
        projectJobDO.setTasks(projectTaskDOMap);
        projectJobDO.setGmtCreate(DateTimes.utcFromRfc3339("2023-08-02T08:30:15.235+08:00"));
        projectJobDO.setGmtModified(DateTimes.utcFromRfc3339("2023-08-02T16:30:15.235+08:00"));
        return projectJobDO;
    }


    private ProjectGraphNodeDO buildProjectGraphNodeDO() {
        ProjectGraphNodeDO.UPK upk = new ProjectGraphNodeDO.UPK();
        upk.setGraphNodeId("alice");
        return ProjectGraphNodeDO.builder().upk(upk).build();
    }

    private ProjectGraphDO buildProjectGraphDO() {
        ProjectGraphDO.UPK upk = new ProjectGraphDO.UPK();
        upk.setProjectId(PROJECT_ID);
        return ProjectGraphDO.builder().upk(upk).build();
    }

    private ProjectDatatableDO buildProjectDatatableDO() {
        ProjectDatatableDO.UPK upk = new ProjectDatatableDO.UPK();
        upk.setDatatableId("alice-ref1");
        upk.setNodeId("alice");
        upk.setProjectId(PROJECT_ID);
        List<ProjectDatatableDO.TableColumnConfig> tableConfig = new ArrayList<>();
        ProjectDatatableDO.TableColumnConfig config = new ProjectDatatableDO.TableColumnConfig();
        config.setColType("id1");
        config.setColType("string");
        tableConfig.add(config);
        return ProjectDatatableDO.builder().upk(upk).tableConfig(tableConfig).build();
    }

    private Domaindata.ListDomainDataResponse buildListDomainDataResponse(Integer code) {
        Common.Status status = Common.Status.newBuilder().setCode(code).build();
        return Domaindata.ListDomainDataResponse.newBuilder().setStatus(status).build();
    }

    private Domain.QueryDomainResponse buildQueryDomainResponse(Integer code) {
        return Domain.QueryDomainResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).build();
    }

    private Domain.CreateDomainResponse buildCreateDomainResponse(Integer code) {
        return Domain.CreateDomainResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).build();
    }

    private Domain.DeleteDomainResponse buildDeleteDomainResponse(Integer code) {
        return Domain.DeleteDomainResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).build();
    }

    private Domaindata.BatchQueryDomainDataResponse buildBatchQueryDomainDataResponse(Integer code) {
        return Domaindata.BatchQueryDomainDataResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).setData(
                Domaindata.DomainDataList.newBuilder().addDomaindataList(Domaindata.DomainData.newBuilder().setDomainId("alice").
                        setDomaindataId("alice-ref1").setType("2").setRelativeUri("dmds://psi_125676513").build()).build()
        ).build();
    }

    private Domaindata.QueryDomainDataResponse buildQueryDomainDataResponse(Integer code) {
        return Domaindata.QueryDomainDataResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).setData(
                Domaindata.DomainData.newBuilder().setDomainId("alice").
                        setDomaindataId("alice-ref1").setType("2").setRelativeUri("dmds://psi_125676513").setDatasourceId("alice-datasource-ref1").build()
        ).build();
    }

    private Domaindata.QueryDomainDataResponse buildEmptyQueryDomainDataResponse(Integer code) {
        return Domaindata.QueryDomainDataResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).build();
    }

    @Test
    void listNode() throws Exception {
        assertResponse(() -> {
            Domain.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(0);
            Mockito.when(domainServiceStub.queryDomain(Mockito.any())).thenReturn(queryDomainResponse);
            Mockito.when(nodeRepository.findAll()).thenReturn(buildNodeDOList());
            Domaindata.ListDomainDataResponse response = buildListDomainDataResponse(0);
            Mockito.when(dataStub.listDomainData(Mockito.any())).thenReturn(response);
            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "listNode"));
        });
    }

    @Test
    void listNodeByQueryDatatableFailedException() throws Exception {
        assertErrorCode(() -> {
            Domain.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(0);
            Mockito.when(domainServiceStub.queryDomain(Mockito.any())).thenReturn(queryDomainResponse);
            Mockito.when(nodeRepository.findAll()).thenReturn(buildNodeDOList());
            Domaindata.ListDomainDataResponse response = buildListDomainDataResponse(1);
            Mockito.when(dataStub.listDomainData(Mockito.any())).thenReturn(response);
            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "listNode"));
        }, DatatableErrorCode.QUERY_DATATABLE_FAILED);
    }

    @Test
    void createNode() throws Exception {
        assertResponse(() -> {
            CreateNodeRequest request = FakerUtils.fake(CreateNodeRequest.class);
            Domain.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(1);
            Mockito.when(domainServiceStub.queryDomain(Mockito.any())).thenReturn(queryDomainResponse);

            Domain.CreateDomainResponse createDomainResponse = buildCreateDomainResponse(0);
            Mockito.when(domainServiceStub.createDomain(Mockito.any())).thenReturn(createDomainResponse);
            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "createNode", CreateNodeRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void createNodeByNodeAlreadyExistsException() throws Exception {
        assertErrorCode(() -> {
            CreateNodeRequest request = FakerUtils.fake(CreateNodeRequest.class);
            Domain.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(0);
            Mockito.when(domainServiceStub.queryDomain(Mockito.any())).thenReturn(queryDomainResponse);

            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "createNode", CreateNodeRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, NodeErrorCode.NODE_CREATE_ERROR);
    }

    @Test
    void createNodeByNodeCreateFailedException() throws Exception {
        assertErrorCode(() -> {
            CreateNodeRequest request = FakerUtils.fake(CreateNodeRequest.class);
            Domain.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(1);
            Mockito.when(domainServiceStub.queryDomain(Mockito.any())).thenReturn(queryDomainResponse);

            Domain.CreateDomainResponse createDomainResponse = buildCreateDomainResponse(1);
            Mockito.when(domainServiceStub.createDomain(Mockito.any())).thenReturn(createDomainResponse);
            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "createNode", CreateNodeRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, NodeErrorCode.NODE_CREATE_ERROR);
    }

    @Test
    void deleteNode() throws Exception {
        assertResponseWithEmptyData(() -> {
            String nodeId = FakerUtils.fake(String.class);
            NodeIdRequest request = new NodeIdRequest(nodeId);
            Domain.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(0);
            Mockito.when(domainServiceStub.queryDomain(Mockito.any())).thenReturn(queryDomainResponse);

            Domain.DeleteDomainResponse deleteDomainResponse = buildDeleteDomainResponse(0);
            Mockito.when(domainServiceStub.deleteDomain(Mockito.any())).thenReturn(deleteDomainResponse);

            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "deleteNode", NodeIdRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void deleteNodeByNodeNotExistsException() throws Exception {
        assertResponseWithEmptyData(() -> {
            String nodeId = FakerUtils.fake(String.class);
            NodeIdRequest request = new NodeIdRequest(nodeId);
            Domain.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(1);
            Mockito.when(domainServiceStub.queryDomain(Mockito.any())).thenReturn(queryDomainResponse);

            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "deleteNode", NodeIdRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void deleteNodeByNodeDeleteFailedException() throws Exception {
        assertErrorCode(() -> {
            String nodeId = FakerUtils.fake(String.class);
            NodeIdRequest request = new NodeIdRequest(nodeId);
            Domain.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(0);
            Mockito.when(domainServiceStub.queryDomain(Mockito.any())).thenReturn(queryDomainResponse);

            Domain.DeleteDomainResponse deleteDomainResponse = buildDeleteDomainResponse(1);
            Mockito.when(domainServiceStub.deleteDomain(Mockito.any())).thenReturn(deleteDomainResponse);

            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "deleteNode", NodeIdRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, NodeErrorCode.NODE_DELETE_ERROR);
    }

    @Test
    void listResults() throws Exception {
        assertResponse(() -> {
            ListNodeResultRequest request = FakerUtils.fake(ListNodeResultRequest.class);
            request.setKindFilters(Collections.singletonList("table"));
            request.setNameFilter("alice");
            request.setPageSize(20);
            request.setPageNumber(1);

            List<ProjectResultDO> projectResultDOS = buildProjectResultDOList();
            Mockito.when(resultRepository.findByNodeId(Mockito.anyString())).thenReturn(projectResultDOS);

            Domaindata.BatchQueryDomainDataResponse batchQueryDomainDataResponse = buildBatchQueryDomainDataResponse(0);
            Mockito.when(dataStub.batchQueryDomainData(Mockito.any())).thenReturn(batchQueryDomainDataResponse);

            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "listResults", ListNodeResultRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void listResultsByDomainDataNotExistsException() throws Exception {
        assertErrorCode(() -> {
            ListNodeResultRequest request = FakerUtils.fake(ListNodeResultRequest.class);
            request.setKindFilters(Collections.singletonList("table"));
            request.setNameFilter("alice");
            request.setPageSize(20);
            request.setPageNumber(1);

            List<ProjectResultDO> projectResultDOS = buildProjectResultDOList();
            Mockito.when(resultRepository.findByNodeId(Mockito.anyString())).thenReturn(projectResultDOS);

            Domaindata.BatchQueryDomainDataResponse batchQueryDomainDataResponse = buildBatchQueryDomainDataResponse(1);
            Mockito.when(dataStub.batchQueryDomainData(Mockito.any())).thenReturn(batchQueryDomainDataResponse);

            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "listResults", ListNodeResultRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, NodeErrorCode.DOMAIN_DATA_NOT_EXISTS);
    }

    @Test
    void listResultsByOutOfRangeException() throws Exception {
        assertErrorCode(() -> {
            ListNodeResultRequest request = FakerUtils.fake(ListNodeResultRequest.class);
            request.setKindFilters(Collections.singletonList("table"));
            request.setNameFilter("alice");
            request.setPageSize(20);
            request.setPageNumber(2);

            List<ProjectResultDO> projectResultDOS = buildProjectResultDOList();
            Mockito.when(resultRepository.findByNodeId(Mockito.anyString())).thenReturn(projectResultDOS);

            Domaindata.BatchQueryDomainDataResponse batchQueryDomainDataResponse = buildBatchQueryDomainDataResponse(0);
            Mockito.when(dataStub.batchQueryDomainData(Mockito.any())).thenReturn(batchQueryDomainDataResponse);

            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "listResults", ListNodeResultRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, SystemErrorCode.OUT_OF_RANGE_ERROR);
    }

    @Test
    void getNodeResultDetail() throws Exception {
        assertResponse(() -> {
            GetNodeResultDetailRequest request = FakerUtils.fake(GetNodeResultDetailRequest.class);
            request.setNodeId("alice");
            request.setDomainDataId("alice-ref1");

            Domaindata.QueryDomainDataResponse queryDomainDataResponse = buildQueryDomainDataResponse(0);
            Mockito.when(dataStub.queryDomainData(Mockito.any())).thenReturn(queryDomainDataResponse);

            Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(Optional.of(ProjectDO.builder().projectId(PROJECT_ID).build()));

            List<ProjectResultDO> projectResultDOS = buildProjectResultDOList();
            Mockito.when(resultRepository.findByNodeIdAndRefId(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(projectResultDOS.get(0)));

            Mockito.when(projectJobRepository.findByJobId(Mockito.anyString())).thenReturn(Optional.of(buildProjectJobDO(false)));
            Mockito.when(projectJobRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectJobDO(false)));

            Mockito.when(projectGraphRepository.findByGraphId(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(buildProjectGraphDO()));

            Mockito.when(resultRepository.findByProjectJobId(Mockito.anyString(), Mockito.anyString())).thenReturn(projectResultDOS);

            Mockito.when(datatableRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectDatatableDO()));

            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "getNodeResultDetail", GetNodeResultDetailRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void getNodeResultDetailByProjectResultNotFoundException() throws Exception {
        assertErrorCode(() -> {
            GetNodeResultDetailRequest request = FakerUtils.fake(GetNodeResultDetailRequest.class);
            request.setNodeId("alice");
            request.setDomainDataId("alice-ref1");

            Domaindata.QueryDomainDataResponse queryDomainDataResponse = buildQueryDomainDataResponse(0);
            Mockito.when(dataStub.queryDomainData(Mockito.any())).thenReturn(queryDomainDataResponse);

            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "getNodeResultDetail", GetNodeResultDetailRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, ProjectErrorCode.PROJECT_RESULT_NOT_FOUND);
    }

    @Test
    void getNodeResultDetailByProjectNotExistsException() throws Exception {
        assertErrorCode(() -> {
            GetNodeResultDetailRequest request = FakerUtils.fake(GetNodeResultDetailRequest.class);
            request.setNodeId("alice");
            request.setDomainDataId("alice-ref1");

            Domaindata.QueryDomainDataResponse queryDomainDataResponse = buildQueryDomainDataResponse(0);
            Mockito.when(dataStub.queryDomainData(Mockito.any())).thenReturn(queryDomainDataResponse);

            List<ProjectResultDO> projectResultDOS = buildProjectResultDOList();
            Mockito.when(resultRepository.findByNodeIdAndRefId(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(projectResultDOS.get(0)));

            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "getNodeResultDetail", GetNodeResultDetailRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, ProjectErrorCode.PROJECT_NOT_EXISTS);
    }

    @Test
    void getNodeResultDetailByProjectJobNotExistsException() throws Exception {
        assertErrorCode(() -> {
            GetNodeResultDetailRequest request = FakerUtils.fake(GetNodeResultDetailRequest.class);
            request.setNodeId("alice");
            request.setDomainDataId("alice-ref1");

            Domaindata.QueryDomainDataResponse queryDomainDataResponse = buildQueryDomainDataResponse(0);
            Mockito.when(dataStub.queryDomainData(Mockito.any())).thenReturn(queryDomainDataResponse);

            List<ProjectResultDO> projectResultDOS = buildProjectResultDOList();
            Mockito.when(resultRepository.findByNodeIdAndRefId(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(projectResultDOS.get(0)));

            Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(Optional.of(ProjectDO.builder().projectId(PROJECT_ID).build()));

            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "getNodeResultDetail", GetNodeResultDetailRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, JobErrorCode.PROJECT_JOB_NOT_EXISTS);
    }

    @Test
    void getNodeResultDetailByQueryDatatableFailedException() throws Exception {
        assertErrorCode(() -> {
            GetNodeResultDetailRequest request = FakerUtils.fake(GetNodeResultDetailRequest.class);
            request.setNodeId("alice");
            request.setDomainDataId("alice-ref1");

            List<ProjectResultDO> projectResultDOS = buildProjectResultDOList();
            Mockito.when(resultRepository.findByNodeIdAndRefId(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(projectResultDOS.get(0)));

            Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(Optional.of(ProjectDO.builder().projectId(PROJECT_ID).build()));

            Mockito.when(projectJobRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectJobDO(false)));

            Domaindata.QueryDomainDataResponse queryDomainDataResponse = buildQueryDomainDataResponse(1);
            Mockito.when(dataStub.queryDomainData(Mockito.any())).thenReturn(queryDomainDataResponse);

            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "getNodeResultDetail", GetNodeResultDetailRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, DatatableErrorCode.QUERY_DATATABLE_FAILED);
    }

    @Test
    void getNodeResultDetailByProjectJobNotExistsExceptionInGraphService() throws Exception {
        assertErrorCode(() -> {
            GetNodeResultDetailRequest request = FakerUtils.fake(GetNodeResultDetailRequest.class);
            request.setNodeId("alice");
            request.setDomainDataId("alice-ref1");

            List<ProjectResultDO> projectResultDOS = buildProjectResultDOList();
            Mockito.when(resultRepository.findByNodeIdAndRefId(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(projectResultDOS.get(0)));

            Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(Optional.of(ProjectDO.builder().projectId(PROJECT_ID).build()));

            Mockito.when(projectJobRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectJobDO(false)));

            Domaindata.QueryDomainDataResponse queryDomainDataResponse = buildEmptyQueryDomainDataResponse(0);
            Mockito.when(dataStub.queryDomainData(Mockito.any())).thenReturn(queryDomainDataResponse);

            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "getNodeResultDetail", GetNodeResultDetailRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, JobErrorCode.PROJECT_JOB_NOT_EXISTS);
    }

    @Test
    void getNodeResultDetailByProjectJobTaskNotExistsException() throws Exception {
        assertErrorCode(() -> {
            GetNodeResultDetailRequest request = FakerUtils.fake(GetNodeResultDetailRequest.class);
            request.setNodeId("alice");
            request.setDomainDataId("alice-ref1");

            List<ProjectResultDO> projectResultDOS = buildProjectResultDOList();
            Mockito.when(resultRepository.findByNodeIdAndRefId(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(projectResultDOS.get(0)));

            Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(Optional.of(ProjectDO.builder().projectId(PROJECT_ID).build()));

            Mockito.when(projectJobRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectJobDO(false)));
            Mockito.when(projectJobRepository.findByJobId(Mockito.anyString())).thenReturn(Optional.of(buildProjectJobDO(true)));

            Domaindata.QueryDomainDataResponse queryDomainDataResponse = buildEmptyQueryDomainDataResponse(0);
            Mockito.when(dataStub.queryDomainData(Mockito.any())).thenReturn(queryDomainDataResponse);

            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "getNodeResultDetail", GetNodeResultDetailRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, JobErrorCode.PROJECT_JOB_TASK_NOT_EXISTS);
    }

    @Test
    void getNodeResultDetailByDatatableNotExistsException() throws Exception {
        assertErrorCode(() -> {
            GetNodeResultDetailRequest request = FakerUtils.fake(GetNodeResultDetailRequest.class);
            request.setNodeId("alice");
            request.setDomainDataId("alice-ref1");

            List<ProjectResultDO> projectResultDOS = buildProjectResultDOList();
            Mockito.when(resultRepository.findByNodeIdAndRefId(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(projectResultDOS.get(0)));

            Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(Optional.of(ProjectDO.builder().projectId(PROJECT_ID).build()));

            Mockito.when(projectJobRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectJobDO(false)));
            Mockito.when(projectJobRepository.findByJobId(Mockito.anyString())).thenReturn(Optional.of(buildProjectJobDO(false)));

            Domaindata.QueryDomainDataResponse queryDomainDataResponse = buildEmptyQueryDomainDataResponse(0);
            Mockito.when(dataStub.queryDomainData(Mockito.any())).thenReturn(queryDomainDataResponse);

            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "getNodeResultDetail", GetNodeResultDetailRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, DatatableErrorCode.DATATABLE_NOT_EXISTS);
    }
}