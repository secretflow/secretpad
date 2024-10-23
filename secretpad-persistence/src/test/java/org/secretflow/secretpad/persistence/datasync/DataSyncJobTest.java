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

package org.secretflow.secretpad.persistence.datasync;

import org.secretflow.secretpad.common.dto.SecretPadResponse;
import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.persistence.datasync.buffer.p2p.P2PDataSyncDataBufferTemplate;
import org.secretflow.secretpad.persistence.datasync.event.P2pDataSyncSendEvent;
import org.secretflow.secretpad.persistence.datasync.job.DataSyncJob;
import org.secretflow.secretpad.persistence.datasync.listener.EntityChangeListener;
import org.secretflow.secretpad.persistence.datasync.producer.p2p.P2pDataSyncProducerTemplate;
import org.secretflow.secretpad.persistence.datasync.producer.p2p.P2pPaddingNodeServiceImpl;
import org.secretflow.secretpad.persistence.datasync.rest.DataSyncRestTemplate;
import org.secretflow.secretpad.persistence.datasync.rest.p2p.P2pDataSyncRestService;
import org.secretflow.secretpad.persistence.datasync.rest.p2p.P2pDataSyncRestTemplate;
import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.model.DataSyncConfig;
import org.secretflow.secretpad.persistence.model.DbChangeAction;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.ProjectApprovalConfigRepository;
import org.secretflow.secretpad.persistence.repository.ProjectInstRepository;
import org.secretflow.secretpad.persistence.repository.VoteRequestRepository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yutu
 * @date 2024/08/05
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
public class DataSyncJobTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private ProjectInstRepository projectInstRepository;

    @Mock
    private ProjectApprovalConfigRepository projectApprovalConfigRepository;

    @Mock
    private VoteRequestRepository voteRequestRepository;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private NodeRepository nodeRepository;

    @Mock
    private P2pDataSyncRestService p2pDataSyncRestService;

    @Test
    void testDataSyncJobWithNoEvent() {
        DataSyncRestTemplate dataSyncRestTemplate = new P2pDataSyncRestTemplate();
        P2PDataSyncDataBufferTemplate dataSyncDataBufferTemplate = new P2PDataSyncDataBufferTemplate(applicationEventPublisher);
        dataSyncRestTemplate.setDataSyncDataBufferTemplate(dataSyncDataBufferTemplate);
        DataSyncJob dataSyncJob = new DataSyncJob();
        dataSyncJob.setDataSyncRestTemplate(dataSyncRestTemplate);
        dataSyncJob.onApplicationEvent(new P2pDataSyncSendEvent(this, "alice"));
    }

    @Test
    void testDataSyncJobError() throws IOException {
        P2pDataSyncProducerTemplate.instId = "instId";
        P2pDataSyncProducerTemplate.nodeIds.add("alice");
        SecretPadResponse<EntityChangeListener.DbChangeEvent<BaseAggregationRoot>> success = SecretPadResponse.success();
        SecretPadResponse<EntityChangeListener.DbChangeEvent<BaseAggregationRoot>> error = new SecretPadResponse<>();
        error.setStatus(SecretPadResponse.SecretPadResponseStatus.builder().code(500).build());
        Mockito.when(p2pDataSyncRestService.sync(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(success);

        DataSyncRestTemplate dataSyncRestTemplate = new P2pDataSyncRestTemplate();
        P2PDataSyncDataBufferTemplate dataSyncDataBufferTemplate = new P2PDataSyncDataBufferTemplate(applicationEventPublisher);
        dataSyncDataBufferTemplate.setSyncPath("./config/test");

        P2pPaddingNodeServiceImpl p2pPaddingNodeService = new P2pPaddingNodeServiceImpl(projectInstRepository, projectApprovalConfigRepository, voteRequestRepository, cacheManager, nodeRepository);
        /*
         * case "ProjectDO"
         * case "ProjectGraphDO"
         * case "ProjectGraphNodeDO"
         * case "ProjectJobDO"
         * case "ProjectDatatableDO"
         * case "VoteRequestDO"
         * case "VoteInviteDO"
         * case "ProjectApprovalConfigDO"
         * case "ProjectNodeDO"
         * case "ProjectGraphNodeKusciaParamsDO"
         * case "ProjectModelServingDO"
         * case "ProjectModelPackDO"
         * case "ProjectFeatureTableDO"
         * case "ProjectGraphDomainDatasourceDO"
         * case "ProjectInstDO"
         *
         * case "ProjectScheduleDO"
         * case "ProjectScheduleJobDO"
         * case "ProjectScheduleTaskDO"
         */
        dataSyncDataBufferTemplate.push(buildProjectDO());
        Mockito.when(p2pDataSyncRestService.sync(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(error);


        DataSyncConfig dataSyncConfig = new DataSyncConfig();
        List<String> list = new ArrayList<>();
        list.add("org.secretflow.secretpad.persistence.entity.ProjectDO");
        list.add("org.secretflow.secretpad.persistence.entity.ProjectGraphDO");
        list.add("org.secretflow.secretpad.persistence.entity.ProjectGraphNodeDO");
        list.add("org.secretflow.secretpad.persistence.entity.ProjectJobDO");
        list.add("org.secretflow.secretpad.persistence.entity.ProjectDatatableDO");
        list.add("org.secretflow.secretpad.persistence.entity.VoteRequestDO");
        list.add("org.secretflow.secretpad.persistence.entity.VoteInviteDO");
        list.add("org.secretflow.secretpad.persistence.entity.ProjectApprovalConfigDO");
        list.add("org.secretflow.secretpad.persistence.entity.ProjectNodeDO");
        list.add("org.secretflow.secretpad.persistence.entity.ProjectGraphNodeKusciaParamsDO");
        list.add("org.secretflow.secretpad.persistence.entity.ProjectModelServingDO");
        list.add("org.secretflow.secretpad.persistence.entity.ProjectModelPackDO");
        list.add("org.secretflow.secretpad.persistence.entity.ProjectFeatureTableDO");
        list.add("org.secretflow.secretpad.persistence.entity.ProjectGraphDomainDatasourceDO");
        list.add("org.secretflow.secretpad.persistence.entity.ProjectInstDO");
        list.add("org.secretflow.secretpad.persistence.entity.ProjectScheduleDO");
        list.add("org.secretflow.secretpad.persistence.entity.ProjectScheduleJobDO");
        list.add("org.secretflow.secretpad.persistence.entity.ProjectScheduleTaskDO");
        dataSyncConfig.setSync(list);
        P2pDataSyncProducerTemplate p2pDataSyncProducerTemplate = new P2pDataSyncProducerTemplate(dataSyncConfig, dataSyncDataBufferTemplate, p2pPaddingNodeService);
        p2pDataSyncProducerTemplate.setPlatformType(PlatformTypeEnum.AUTONOMY.name());
        p2pDataSyncProducerTemplate.push(buildProjectDO());
        p2pDataSyncProducerTemplate.push(buildProjectGraphDO());
        p2pDataSyncProducerTemplate.push(buildProjectGraphNodeDO());
        p2pDataSyncProducerTemplate.push(buildProjectInstDO());
        p2pDataSyncProducerTemplate.push(buildProjectJobDO());
        p2pDataSyncProducerTemplate.push(buildProjectDatatableDO());
        p2pDataSyncProducerTemplate.push(buildVoteRequestDO());
        p2pDataSyncProducerTemplate.push(buildVoteInviteDO());
        p2pDataSyncProducerTemplate.push(buildProjectApprovalConfigDO());
        p2pDataSyncProducerTemplate.push(buildProjectNodeDO());
        p2pDataSyncProducerTemplate.push(buildProjectGraphNodeKusciaParamsDO());
        p2pDataSyncProducerTemplate.push(buildProjectModelServingDO());
        p2pDataSyncProducerTemplate.push(buildProjectModelPackDO());
        p2pDataSyncProducerTemplate.push(buildProjectFeatureTableDO());
        p2pDataSyncProducerTemplate.push(buildProjectGraphDomainDatasourceDO());
        p2pDataSyncProducerTemplate.push(buildProjectInstDO());
        p2pDataSyncProducerTemplate.push(buildProjectScheduleDO());
        p2pDataSyncProducerTemplate.push(buildProjectScheduleJobDO());
        p2pDataSyncProducerTemplate.push(buildProjectScheduleTaskDO());

        dataSyncRestTemplate.setDataSyncDataBufferTemplate(dataSyncDataBufferTemplate);
        dataSyncRestTemplate.setP2pPaddingNodeService(p2pPaddingNodeService);
        dataSyncRestTemplate.setP2pDataSyncRestService(p2pDataSyncRestService);
        DataSyncJob dataSyncJob = new DataSyncJob();
        dataSyncJob.setDataSyncRestTemplate(dataSyncRestTemplate);
        dataSyncJob.onApplicationEvent(new P2pDataSyncSendEvent(this, "alice"));
    }

    @Test
    void testDataSyncJob() throws IOException {
        P2pDataSyncProducerTemplate.instId = "instId";
        P2pDataSyncProducerTemplate.nodeIds.add("alice");
        SecretPadResponse<EntityChangeListener.DbChangeEvent<BaseAggregationRoot>> success = SecretPadResponse.success();
        SecretPadResponse<EntityChangeListener.DbChangeEvent<BaseAggregationRoot>> error = new SecretPadResponse<>();
        error.setStatus(SecretPadResponse.SecretPadResponseStatus.builder().code(500).build());
        Mockito.when(p2pDataSyncRestService.sync(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(success);

        DataSyncRestTemplate dataSyncRestTemplate = new P2pDataSyncRestTemplate();
        P2PDataSyncDataBufferTemplate dataSyncDataBufferTemplate = new P2PDataSyncDataBufferTemplate(applicationEventPublisher);
        dataSyncDataBufferTemplate.setSyncPath("./config/test");

        P2pPaddingNodeServiceImpl p2pPaddingNodeService = new P2pPaddingNodeServiceImpl(projectInstRepository, projectApprovalConfigRepository, voteRequestRepository, cacheManager, nodeRepository);
        /*
         * case "ProjectDO"
         * case "ProjectGraphDO"
         * case "ProjectGraphNodeDO"
         * case "ProjectJobDO"
         * case "ProjectDatatableDO"
         * case "VoteRequestDO"
         * case "VoteInviteDO"
         * case "ProjectApprovalConfigDO"
         * case "ProjectNodeDO"
         * case "ProjectGraphNodeKusciaParamsDO"
         * case "ProjectModelServingDO"
         * case "ProjectModelPackDO"
         * case "ProjectFeatureTableDO"
         * case "ProjectGraphDomainDatasourceDO"
         * case "ProjectInstDO"
         */
        dataSyncDataBufferTemplate.push(buildProjectDO());
        Mockito.when(p2pDataSyncRestService.sync(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(error);
        dataSyncDataBufferTemplate.push(buildProjectGraphDO());
        Mockito.when(p2pDataSyncRestService.sync(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(success);
        dataSyncDataBufferTemplate.push(buildProjectGraphNodeDO());
        dataSyncDataBufferTemplate.push(buildProjectInstDO());
        dataSyncDataBufferTemplate.push(buildProjectJobDO());
        dataSyncDataBufferTemplate.push(buildProjectDatatableDO());
        dataSyncDataBufferTemplate.push(buildVoteRequestDO());
        dataSyncDataBufferTemplate.push(buildVoteInviteDO());
        dataSyncDataBufferTemplate.push(buildProjectApprovalConfigDO());
        dataSyncDataBufferTemplate.push(buildProjectNodeDO());
        dataSyncDataBufferTemplate.push(buildProjectGraphNodeKusciaParamsDO());
        dataSyncDataBufferTemplate.push(buildProjectModelServingDO());
        dataSyncDataBufferTemplate.push(buildProjectModelPackDO());
        dataSyncDataBufferTemplate.push(buildProjectFeatureTableDO());
        dataSyncDataBufferTemplate.push(buildProjectGraphDomainDatasourceDO());
        dataSyncDataBufferTemplate.push(buildProjectInstDO());
        dataSyncDataBufferTemplate.serializableRead("alice");

        DataSyncConfig dataSyncConfig = new DataSyncConfig();
        List<String> list = new ArrayList<>();
        list.add("org.secretflow.secretpad.persistence.entity.ProjectDO");
        list.add("org.secretflow.secretpad.persistence.entity.ProjectGraphDO");
        list.add("org.secretflow.secretpad.persistence.entity.ProjectGraphNodeDO");
        list.add("org.secretflow.secretpad.persistence.entity.ProjectJobDO");
        list.add("org.secretflow.secretpad.persistence.entity.ProjectDatatableDO");
        list.add("org.secretflow.secretpad.persistence.entity.VoteRequestDO");
        list.add("org.secretflow.secretpad.persistence.entity.VoteInviteDO");
        list.add("org.secretflow.secretpad.persistence.entity.ProjectApprovalConfigDO");
        list.add("org.secretflow.secretpad.persistence.entity.ProjectNodeDO");
        list.add("org.secretflow.secretpad.persistence.entity.ProjectGraphNodeKusciaParamsDO");
        list.add("org.secretflow.secretpad.persistence.entity.ProjectModelServingDO");
        list.add("org.secretflow.secretpad.persistence.entity.ProjectModelPackDO");
        list.add("org.secretflow.secretpad.persistence.entity.ProjectFeatureTableDO");
        list.add("org.secretflow.secretpad.persistence.entity.ProjectGraphDomainDatasourceDO");
        list.add("org.secretflow.secretpad.persistence.entity.ProjectInstDO");
        list.add("org.secretflow.secretpad.persistence.entity.ProjectScheduleDO");
        list.add("org.secretflow.secretpad.persistence.entity.ProjectScheduleJobDO");
        list.add("org.secretflow.secretpad.persistence.entity.ProjectScheduleTaskDO");
        dataSyncConfig.setSync(list);
        P2pDataSyncProducerTemplate p2pDataSyncProducerTemplate = new P2pDataSyncProducerTemplate(dataSyncConfig, dataSyncDataBufferTemplate, p2pPaddingNodeService);
        p2pDataSyncProducerTemplate.setPlatformType(PlatformTypeEnum.AUTONOMY.name());
        p2pDataSyncProducerTemplate.push(buildProjectDO());
        p2pDataSyncProducerTemplate.push(buildProjectGraphDO());
        p2pDataSyncProducerTemplate.push(buildProjectGraphNodeDO());
        p2pDataSyncProducerTemplate.push(buildProjectInstDO());
        p2pDataSyncProducerTemplate.push(buildProjectJobDO());
        p2pDataSyncProducerTemplate.push(buildProjectDatatableDO());
        p2pDataSyncProducerTemplate.push(buildVoteRequestDO());
        p2pDataSyncProducerTemplate.push(buildVoteInviteDO());
        p2pDataSyncProducerTemplate.push(buildProjectApprovalConfigDO());
        p2pDataSyncProducerTemplate.push(buildProjectNodeDO());
        p2pDataSyncProducerTemplate.push(buildProjectGraphNodeKusciaParamsDO());
        p2pDataSyncProducerTemplate.push(buildProjectModelServingDO());
        p2pDataSyncProducerTemplate.push(buildProjectModelPackDO());
        p2pDataSyncProducerTemplate.push(buildProjectFeatureTableDO());
        p2pDataSyncProducerTemplate.push(buildProjectGraphDomainDatasourceDO());
        p2pDataSyncProducerTemplate.push(buildProjectInstDO());
        p2pDataSyncProducerTemplate.push(buildProjectScheduleDO());
        p2pDataSyncProducerTemplate.push(buildProjectScheduleJobDO());
        p2pDataSyncProducerTemplate.push(buildProjectScheduleTaskDO());


        dataSyncRestTemplate.setDataSyncDataBufferTemplate(dataSyncDataBufferTemplate);
        dataSyncRestTemplate.setP2pPaddingNodeService(p2pPaddingNodeService);
        dataSyncRestTemplate.setP2pDataSyncRestService(p2pDataSyncRestService);
        DataSyncJob dataSyncJob = new DataSyncJob();
        dataSyncJob.setDataSyncRestTemplate(dataSyncRestTemplate);
        dataSyncJob.onApplicationEvent(new P2pDataSyncSendEvent(this, "alice"));
    }

    private EntityChangeListener.DbChangeEvent<BaseAggregationRoot> buildProjectDO() {
        EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event = new EntityChangeListener.DbChangeEvent<>();
        event.setAction(DbChangeAction.CREATE.val);
        ProjectDO projectDO = ProjectDO.builder().build();
        projectDO.setId(1L);
        projectDO.setProjectId("projectId");
        event.setSource(projectDO);
        event.setDType(ProjectDO.class.getTypeName());
        event.setNodeIds(List.of("alice", "bob", "test"));
        return event;
    }

    private EntityChangeListener.DbChangeEvent<BaseAggregationRoot> buildProjectGraphDO() {
        EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event = new EntityChangeListener.DbChangeEvent<>();
        event.setAction(DbChangeAction.CREATE.val);
        ProjectGraphDO projectGraphDO = ProjectGraphDO.builder().build();
        projectGraphDO.setId(1L);
        projectGraphDO.setUpk(new ProjectGraphDO.UPK("projectId", "graphId"));
        event.setSource(projectGraphDO);
        event.setDType(ProjectGraphDO.class.getTypeName());
        event.setNodeIds(List.of("alice", "bob", "test"));
        return event;
    }

    private EntityChangeListener.DbChangeEvent<BaseAggregationRoot> buildProjectGraphNodeDO() {
        EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event = new EntityChangeListener.DbChangeEvent<>();
        event.setAction(DbChangeAction.CREATE.val);
        ProjectGraphNodeDO projectGraphNodeDO = ProjectGraphNodeDO.builder().build();
        projectGraphNodeDO.setId(1L);
        projectGraphNodeDO.setUpk(new ProjectGraphNodeDO.UPK("projectId", "graphId", "nodeId"));
        event.setSource(projectGraphNodeDO);
        event.setDType(ProjectGraphNodeDO.class.getTypeName());
        event.setNodeIds(List.of("alice", "bob", "test"));
        return event;
    }

    private EntityChangeListener.DbChangeEvent<BaseAggregationRoot> buildProjectJobDO() {
        EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event = new EntityChangeListener.DbChangeEvent<>();
        event.setAction(DbChangeAction.CREATE.val);
        ProjectJobDO projectJobDO = ProjectJobDO.builder().build();
        projectJobDO.setId(1L);
        projectJobDO.setUpk(new ProjectJobDO.UPK("projectId", "jobId"));
        event.setSource(projectJobDO);
        event.setDType(ProjectJobDO.class.getTypeName());
        event.setNodeIds(List.of("alice", "bob", "test"));
        return event;
    }

    private EntityChangeListener.DbChangeEvent<BaseAggregationRoot> buildProjectDatatableDO() {
        EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event = new EntityChangeListener.DbChangeEvent<>();
        event.setAction(DbChangeAction.CREATE.val);
        ProjectDatatableDO projectDatatableDO = ProjectDatatableDO.builder().build();
        projectDatatableDO.setId(1L);
        projectDatatableDO.setUpk(new ProjectDatatableDO.UPK("projectId", "nodeId", "datatableId"));
        projectDatatableDO.setTableConfig(List.of(new ProjectDatatableDO.TableColumnConfig()));
        event.setSource(projectDatatableDO);
        event.setDType(ProjectDatatableDO.class.getTypeName());
        event.setNodeIds(List.of("alice", "bob", "test"));
        return event;
    }

    private EntityChangeListener.DbChangeEvent<BaseAggregationRoot> buildVoteRequestDO() {
        EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event = new EntityChangeListener.DbChangeEvent<>();
        event.setAction(DbChangeAction.CREATE.val);
        VoteRequestDO voteRequestDO = VoteRequestDO.builder().build();
        voteRequestDO.setId(1L);
        voteRequestDO.setInitiator("alice");
        event.setSource(voteRequestDO);
        event.setDType(VoteRequestDO.class.getTypeName());
        event.setNodeIds(List.of("alice", "bob", "test"));
        return event;
    }

    private EntityChangeListener.DbChangeEvent<BaseAggregationRoot> buildVoteInviteDO() {
        EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event = new EntityChangeListener.DbChangeEvent<>();
        event.setAction(DbChangeAction.CREATE.val);
        VoteInviteDO voteInviteDO = VoteInviteDO.builder().build();
        voteInviteDO.setId(1L);
        voteInviteDO.setInitiator("alice");
        event.setSource(voteInviteDO);
        event.setDType(VoteInviteDO.class.getTypeName());
        event.setNodeIds(List.of("alice", "bob", "test"));
        return event;
    }

    private EntityChangeListener.DbChangeEvent<BaseAggregationRoot> buildProjectApprovalConfigDO() {
        EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event = new EntityChangeListener.DbChangeEvent<>();
        event.setAction(DbChangeAction.CREATE.val);
        ProjectApprovalConfigDO projectApprovalConfigDO = ProjectApprovalConfigDO.builder().build();
        projectApprovalConfigDO.setId(1L);
        projectApprovalConfigDO.setProjectId("projectId");
        event.setSource(projectApprovalConfigDO);
        event.setDType(ProjectApprovalConfigDO.class.getTypeName());
        event.setNodeIds(List.of("alice", "bob", "test"));
        return event;
    }

    private EntityChangeListener.DbChangeEvent<BaseAggregationRoot> buildProjectNodeDO() {
        EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event = new EntityChangeListener.DbChangeEvent<>();
        event.setAction(DbChangeAction.CREATE.val);
        ProjectNodeDO projectNodeDO = ProjectNodeDO.builder().build();
        projectNodeDO.setId(1L);
        projectNodeDO.setUpk(new ProjectNodeDO.UPK("projectId", "nodeId"));
        event.setSource(projectNodeDO);
        event.setDType(ProjectNodeDO.class.getTypeName());
        event.setNodeIds(List.of("alice", "bob", "test"));
        return event;
    }

    private EntityChangeListener.DbChangeEvent<BaseAggregationRoot> buildProjectGraphNodeKusciaParamsDO() {
        EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event = new EntityChangeListener.DbChangeEvent<>();
        event.setAction(DbChangeAction.CREATE.val);
        ProjectGraphNodeKusciaParamsDO projectGraphNodeKusciaParamsDO = ProjectGraphNodeKusciaParamsDO.builder().build();
        projectGraphNodeKusciaParamsDO.setId(1L);
        projectGraphNodeKusciaParamsDO.setUpk(new ProjectGraphNodeKusciaParamsDO.UPK("projectId", "graphId", "nodeId"));
        event.setSource(projectGraphNodeKusciaParamsDO);
        event.setDType(ProjectGraphNodeKusciaParamsDO.class.getTypeName());
        event.setNodeIds(List.of("alice", "bob", "test"));
        return event;
    }

    private EntityChangeListener.DbChangeEvent<BaseAggregationRoot> buildProjectModelServingDO() {
        EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event = new EntityChangeListener.DbChangeEvent<>();
        event.setAction(DbChangeAction.CREATE.val);
        ProjectModelServingDO projectModelServingDO = ProjectModelServingDO.builder().build();
        projectModelServingDO.setId(1L);
        projectModelServingDO.setInitiator("alice");
        event.setSource(projectModelServingDO);
        event.setDType(ProjectModelServingDO.class.getTypeName());
        event.setNodeIds(List.of("alice", "bob", "test"));
        return event;
    }

    private EntityChangeListener.DbChangeEvent<BaseAggregationRoot> buildProjectModelPackDO() {
        EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event = new EntityChangeListener.DbChangeEvent<>();
        event.setAction(DbChangeAction.CREATE.val);
        ProjectModelPackDO projectModelPackDO = ProjectModelPackDO.builder().build();
        projectModelPackDO.setId(1L);
        projectModelPackDO.setInitiator("alice");
        event.setSource(projectModelPackDO);
        event.setDType(ProjectModelPackDO.class.getTypeName());
        event.setNodeIds(List.of("alice", "bob", "test"));
        return event;
    }

    private EntityChangeListener.DbChangeEvent<BaseAggregationRoot> buildProjectFeatureTableDO() {
        EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event = new EntityChangeListener.DbChangeEvent<>();
        event.setAction(DbChangeAction.CREATE.val);
        ProjectFeatureTableDO projectFeatureTableDO = ProjectFeatureTableDO.builder().build();
        projectFeatureTableDO.setId(1L);
        projectFeatureTableDO.setUpk(new ProjectFeatureTableDO.UPK("projectId", "nodeId", "featureTableId", "datasourceId"));
        event.setSource(projectFeatureTableDO);
        event.setDType(ProjectFeatureTableDO.class.getTypeName());
        event.setNodeIds(List.of("alice", "bob", "test"));
        return event;
    }

    private EntityChangeListener.DbChangeEvent<BaseAggregationRoot> buildProjectGraphDomainDatasourceDO() {
        EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event = new EntityChangeListener.DbChangeEvent<>();
        event.setAction(DbChangeAction.CREATE.val);
        ProjectGraphDomainDatasourceDO projectGraphDomainDatasourceDO = ProjectGraphDomainDatasourceDO.builder().build();
        projectGraphDomainDatasourceDO.setId(1L);
        projectGraphDomainDatasourceDO.setUpk(new ProjectGraphDomainDatasourceDO.UPK("projectId", "graphId", "domainId"));
        event.setSource(projectGraphDomainDatasourceDO);
        event.setDType(ProjectGraphDomainDatasourceDO.class.getTypeName());
        event.setNodeIds(List.of("alice", "bob", "test"));
        return event;
    }


    /**
     * case "ProjectDO"
     * case "ProjectGraphDO"
     * case "ProjectGraphNodeDO"
     * case "ProjectJobDO"
     * case "ProjectDatatableDO"
     * case "VoteRequestDO"
     * case "VoteInviteDO"
     * case "ProjectApprovalConfigDO"
     * case "ProjectNodeDO"
     * case "ProjectGraphNodeKusciaParamsDO"
     * case "ProjectModelServingDO"
     * case "ProjectModelPackDO"
     * case "ProjectFeatureTableDO"
     * case "ProjectGraphDomainDatasourceDO"
     * case "ProjectInstDO"
     * ProjectScheduleDO
     * ProjectScheduleJobDO
     * ProjectScheduleTaskDO
     */

    private EntityChangeListener.DbChangeEvent<BaseAggregationRoot> buildProjectInstDO() {
        EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event = new EntityChangeListener.DbChangeEvent<>();
        event.setAction(DbChangeAction.CREATE.val);
        ProjectInstDO projectInstDO = ProjectInstDO.builder().build();
        projectInstDO.setId(1L);
        projectInstDO.setUpk(new ProjectInstDO.UPK("projectId", "instId"));
        event.setSource(projectInstDO);
        event.setDType(ProjectInstDO.class.getTypeName());
        event.setNodeIds(List.of("alice", "bob", "test"));
        return event;
    }

    private EntityChangeListener.DbChangeEvent<BaseAggregationRoot> buildProjectScheduleDO() {
        EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event = new EntityChangeListener.DbChangeEvent<>();
        event.setAction(DbChangeAction.CREATE.val);
        ProjectScheduleDO projectScheduleDO = ProjectScheduleDO.builder().build();
        projectScheduleDO.setId(1L);
        projectScheduleDO.setScheduleId("scheduleId");
        projectScheduleDO.setProjectId("projectId");
        projectScheduleDO.setCreator("alice");
        projectScheduleDO.setOwner("alice");
        event.setSource(projectScheduleDO);
        event.setDType(ProjectScheduleDO.class.getTypeName());
        event.setNodeIds(List.of("alice", "bob", "test"));
        return event;
    }

    private EntityChangeListener.DbChangeEvent<BaseAggregationRoot> buildProjectScheduleJobDO() {
        EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event = new EntityChangeListener.DbChangeEvent<>();
        event.setAction(DbChangeAction.CREATE.val);
        ProjectScheduleJobDO projectScheduleJobDO = ProjectScheduleJobDO.builder().build();
        projectScheduleJobDO.setId(1L);
        projectScheduleJobDO.setUpk(new ProjectScheduleJobDO.UPK("projectId", "jobId"));
        event.setSource(projectScheduleJobDO);
        event.setDType(ProjectScheduleJobDO.class.getTypeName());
        event.setNodeIds(List.of("alice", "bob", "test"));
        return event;
    }

    private EntityChangeListener.DbChangeEvent<BaseAggregationRoot> buildProjectScheduleTaskDO() {
        EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event = new EntityChangeListener.DbChangeEvent<>();
        event.setAction(DbChangeAction.CREATE.val);
        ProjectScheduleTaskDO projectScheduleTaskDO = ProjectScheduleTaskDO.builder().build();
        projectScheduleTaskDO.setId(1L);
        projectScheduleTaskDO.setScheduleId("scheduleId");
        projectScheduleTaskDO.setProjectId("projectId");
        projectScheduleTaskDO.setCreator("alice");
        projectScheduleTaskDO.setOwner("alice");
        event.setSource(projectScheduleTaskDO);
        event.setDType(ProjectScheduleTaskDO.class.getTypeName());
        event.setNodeIds(List.of("alice", "bob", "test"));
        return event;
    }


}