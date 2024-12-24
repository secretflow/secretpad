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

import org.secretflow.secretpad.common.dto.UserContextDTO;
import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.common.enums.UserOwnerTypeEnum;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.model.GraphEdgeDO;
import org.secretflow.secretpad.persistence.repository.*;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.ProjectGraphDomainDatasourceService;
import org.secretflow.secretpad.service.ReadPartitionRuleAnalysisService;
import org.secretflow.secretpad.service.graph.GraphContext;
import org.secretflow.secretpad.service.graph.converter.KusciaJobConverter;
import org.secretflow.secretpad.service.model.graph.GraphNodeInfo;
import org.secretflow.secretpad.service.model.graph.ProjectJob;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.secretflow.proto.kuscia.TaskConfig;
import org.secretflow.proto.pipeline.Pipeline;
import org.secretflow.v1alpha1.kusciaapi.Job;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.secretflow.secretpad.service.model.datatable.TeeJob.PROJECT_ID;

/**
 * @author lufeng
 * @date 2024/10/22
 */
@ExtendWith(MockitoExtension.class)
public class KusciaJobConverterTest {

    @InjectMocks
    private KusciaJobConverter kusciaJobConverter;
    @Mock
    private ProjectGraphNodeKusciaParamsRepository projectGraphNodeKusciaParamsRepository;;

    @Mock
    private ProjectGraphDomainDatasourceService projectGraphDomainDatasourceService;

    @Mock
    private ProjectDatatableRepository projectDatatableRepository;

    @Mock
    private EnvService envService;

    @Mock
    private ProjectJobTaskRepository taskRepository;

    @Mock
    private ProjectGraphNodeRepository projectGraphNodeRepository;

    @Mock
    private ProjectGraphRepository projectGraphRepository;

    @Mock
    private ReadPartitionRuleAnalysisService readPartitionRuleAnalysisService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        UserContext.setBaseUser(UserContextDTO.builder().ownerId("alice")
                .name("alice")
                .platformType(PlatformTypeEnum.CENTER)
                .platformNodeId("alice")
                .ownerType(UserOwnerTypeEnum.CENTER)
                .ownerId("test")
                .projectIds(Set.of(PROJECT_ID)).build());
    }
    private String spu = "{\n" +
            "    \"runtime_config\": {\n" +
            "        \"protocol\": \"SEMI2K\",\n" +
            "        \"field\": \"FM128\"\n" +
            "    },\n" +
            "    \"link_desc\": {\n" +
            "        \"connect_retry_times\": 60,\n" +
            "        \"connect_retry_interval_ms\": 1000,\n" +
            "        \"brpc_channel_protocol\": \"http\",\n" +
            "        \"brpc_channel_connection_type\": \"pooled\",\n" +
            "        \"recv_timeout_ms\": 1200000,\n" +
            "        \"http_timeout_ms\": 1200000\n" +
            "    }\n" +
            "}";
    private String heu = "{\n" +
            "    \"mode\": \"PHEU\",\n" +
            "    \"schema\": \"paillier\",\n" +
            "    \"key_size\": 2048\n" +
            "}";


    /**
     * test kuscia job converter with platformType center
     *
     */
    @Test
    public void testConverter() {
        // Create a NodeDef object for testing.
        Pipeline.NodeDef nodeDef = Pipeline.NodeDef.newBuilder()
                .setDomain("stats")
                .setName("scql_analysis")
                .setVersion("1.0.0")
                .addAttrPaths("column_config")
                .addAttrPaths("script_input")
                .addAttrs(Struct.newBuilder()
                        .putFields("s", Value.newBuilder()
                                .setStringValue("\n").build()).build())
                .addAttrs(Struct.newBuilder()
                        .putFields("s", Value.newBuilder()
                                .setStringValue("select a.age,b.id2 from alice_sygycijh_node_3_output_0 a, bob_sygycijh_node_3_output_0 b;")
                                .build())
                        .putFields("is_na", Value.newBuilder()
                                .setBoolValue(false).build()
                        ).build())
                .build();

        // Set the platformType and crossSiloCommBackend fields of kusciaJobConverter using ReflectionTestUtils.
        ReflectionTestUtils.setField(kusciaJobConverter, "platformType", "CENTER");
        ReflectionTestUtils.setField(kusciaJobConverter, "crossSiloCommBackend", "brpc_link");

        List<String> parties = new ArrayList<>();
        parties.add("alice");
        parties.add("bob");
        // Create a JobTask object for testing.
        ProjectJob.JobTask task = ProjectJob.JobTask.builder()
                .taskId("xect-sect")
                .parties(parties)
                .build();

        // Create a ProjectJob object for testing.
        ProjectJob job = new ProjectJob();
        job.setJobId("jobId");
        job.setProjectId("testProjectId");
        job.setGraphId("testGraphId");
        job.setMaxParallelism(1);

        // Create a GraphNodeInfo object for testing.
        GraphNodeInfo graphNode = new GraphNodeInfo();
        graphNode.setNodeDef(nodeDef);
        graphNode.setGraphNodeId("sygycijh-node-33");
        graphNode.setLabel("自定义SCQL分析");
        graphNode.setCodeName("stats/scql_analysis");
        graphNode.setInputs(List.of("ltct-sygycijh-node-3-output-0"));
        graphNode.setOutputs(List.of("ebyt-sygycijh-node-33-output-0"));
        task.setNode(graphNode);

        job.setTasks(Arrays.asList(task));
        // Set the deviceConfig field of kusciaJobConverter.
        Map<String, String> deviceConfig = new HashMap<>();
        deviceConfig.put("spu", spu);
        deviceConfig.put("heu", heu);
        kusciaJobConverter.setDeviceConfig(deviceConfig);
        // Create a ProjectGraphDomainDatasourceDO object for testing.
        ProjectGraphDomainDatasourceDO projectGraphDomainDatasourceDO = new ProjectGraphDomainDatasourceDO();
        projectGraphDomainDatasourceDO.setDataSourceId("testDataSourceId");
        projectGraphDomainDatasourceDO.setDataSourceName("testDataSourceName");
        projectGraphDomainDatasourceDO.setEditEnable(true);

        ProjectGraphNodeDO projectGraphNodeDO = new ProjectGraphNodeDO();
        projectGraphNodeDO.setUpk(new ProjectGraphNodeDO.UPK("testProjectId", "testGraphId", "testDomainId"));
        projectGraphNodeDO.setNodeDef(nodeDef);

        GraphEdgeDO graphEdgeDO = new GraphEdgeDO();
        graphEdgeDO.setEdgeId("testEdgeId");
        graphEdgeDO.setSource("testSourceNodeId");
        graphEdgeDO.setTarget("testTargetNodeId");
        ProjectGraphDO projectGraphDO = new ProjectGraphDO();
        projectGraphDO.setEdges(List.of(graphEdgeDO));
        projectGraphDO.setOwnerId("test");
        Mockito.when(projectGraphRepository.findById(Mockito.any())).thenReturn(Optional.of(projectGraphDO));
        projectGraphDomainDatasourceDO.setUpk(new ProjectGraphDomainDatasourceDO.UPK("testProjectId", "testGraphId", "testDomainId"));
        Mockito.when(projectGraphNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(projectGraphNodeDO));
        Mockito.when(projectGraphDomainDatasourceService.getById(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(projectGraphDomainDatasourceDO);

        // Create TableAttr and ColumnAttr objects for testing.
        TaskConfig.TableAttr tableAttr = TaskConfig.TableAttr.newBuilder().
                setTableId("alice-table")
                .addColumnAttrs(TaskConfig.ColumnAttr.newBuilder()
                        .setColName("id1")
                        .setColName("id").build())
                .build();
        // Create ProjectDatatableDO.TableColumnConfig  for testing.
        ProjectDatatableDO.TableColumnConfig tableColumnConfig = new ProjectDatatableDO.TableColumnConfig();
        tableColumnConfig.setAssociateKey(false);
        tableColumnConfig.setGroupKey(false);
        tableColumnConfig.setLabelKey(false);
        tableColumnConfig.setProtection(false);
        tableColumnConfig.setColName("testid");
        tableColumnConfig.setColType("str");
        ProjectDatatableDO projectDatatableDO1 = ProjectDatatableDO.builder()
                .upk(new ProjectDatatableDO.UPK("zqemwvop", "alice", "alice-table"))
                .tableConfig(List.of(tableColumnConfig))
                .build();
        ProjectDatatableDO projectDatatableDO2 = ProjectDatatableDO.builder()
                .upk(new ProjectDatatableDO.UPK("sadasdas", "bob", "bob-table"))
                .tableConfig(List.of(tableColumnConfig))
                .build();
        Mockito.when(projectDatatableRepository.findByDatableId(Mockito.any(),Mockito.any())).thenReturn(List.of(projectDatatableDO1));
        GraphContext.set(new ProjectDO(), new GraphContext.GraphParties(), false);
        GraphContext.set(List.of(tableAttr));
        Job.CreateJobRequest result = kusciaJobConverter.converter(job);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(job.getJobId(), result.getJobId());
    }

    /**
     * test kuscia job converter with platformType AUTONOMY
     *
     */
    @Test
    public void testconverter_autonomy() {
        // Create a NodeDef object for testing.
        Pipeline.NodeDef nodeDef = Pipeline.NodeDef.newBuilder()
                .setDomain("stats")
                .setName("scql_analysis")
                .setVersion("1.0.0")
                .addAttrPaths("column_config")
                .addAttrPaths("script_input")
                .addAttrs(Struct.newBuilder()
                        .putFields("s", Value.newBuilder()
                                .setStringValue("\n").build()).build())
                .addAttrs(Struct.newBuilder()
                        .putFields("s", Value.newBuilder()
                                .setStringValue("select a.age,b.id2 from alice_sygycijh_node_3_output_0 a, bob_sygycijh_node_3_output_0 b;")
                                .build())
                        .putFields("is_na", Value.newBuilder()
                                .setBoolValue(false).build()
                        ).build())
                .build();

        // Set the platformType and crossSiloCommBackend fields of kusciaJobConverter using ReflectionTestUtils.
        ReflectionTestUtils.setField(kusciaJobConverter, "platformType", "AUTONOMY");
        ReflectionTestUtils.setField(kusciaJobConverter, "crossSiloCommBackend", "brpc_link");

        Mockito.when(envService.findLocalNodeId(Mockito.any())).thenReturn("alice");
        // Create a JobTask object for testing.
        List<String> parties = new ArrayList<>();
        parties.add("alice");
        parties.add("bob");
        ProjectJob.JobTask task = ProjectJob.JobTask.builder()
                .taskId("xect-sect")
                .parties(parties)
                .build();

        // Create a ProjectJob object for testing.
        ProjectJob job = new ProjectJob();
        job.setJobId("jobId");
        job.setProjectId("testProjectId");
        job.setGraphId("testGraphId");
        job.setMaxParallelism(1);

        // Create a GraphNodeInfo object for testing.
        GraphNodeInfo graphNode = new GraphNodeInfo();
        graphNode.setNodeDef(nodeDef);
        graphNode.setGraphNodeId("sygycijh-node-33");
        graphNode.setLabel("自定义SCQL分析");
        graphNode.setCodeName("stats/scql_analysis");
        graphNode.setInputs(List.of("ltct-sygycijh-node-3-output-0"));
        graphNode.setOutputs(List.of("ebyt-sygycijh-node-33-output-0"));
        task.setNode(graphNode);
        job.setTasks(List.of(task));
        // Set the deviceConfig field of kusciaJobConverter.
        Map<String, String> deviceConfig = new HashMap<>();
        deviceConfig.put("spu", spu);
        deviceConfig.put("heu", heu);
        kusciaJobConverter.setDeviceConfig(deviceConfig);
        // Create a ProjectGraphDomainDatasourceDO object for testing.
        ProjectGraphDomainDatasourceDO projectGraphDomainDatasourceDO = new ProjectGraphDomainDatasourceDO();
        projectGraphDomainDatasourceDO.setDataSourceId("testDataSourceId");
        projectGraphDomainDatasourceDO.setDataSourceName("testDataSourceName");
        projectGraphDomainDatasourceDO.setEditEnable(true);
        projectGraphDomainDatasourceDO.setUpk(new ProjectGraphDomainDatasourceDO.UPK("testProjectId", "testGraphId", "testDomainId"));
        Mockito.when(projectGraphDomainDatasourceService.getById(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(projectGraphDomainDatasourceDO);

        ProjectGraphNodeDO projectGraphNodeDO = new ProjectGraphNodeDO();
        projectGraphNodeDO.setUpk(new ProjectGraphNodeDO.UPK("testProjectId", "testGraphId", "testDomainId"));
        projectGraphNodeDO.setNodeDef(nodeDef);


        GraphEdgeDO graphEdgeDO = new GraphEdgeDO();
        graphEdgeDO.setEdgeId("testEdgeId");
        graphEdgeDO.setSource("testSourceNodeId");
        graphEdgeDO.setTarget("testTargetNodeId");
        ProjectGraphDO projectGraphDO = new ProjectGraphDO();
        projectGraphDO.setEdges(List.of(graphEdgeDO));
        projectGraphDO.setOwnerId("test");
        Mockito.when(projectGraphRepository.findById(Mockito.any())).thenReturn(Optional.of(projectGraphDO));
        projectGraphDomainDatasourceDO.setUpk(new ProjectGraphDomainDatasourceDO.UPK("testProjectId", "testGraphId", "testDomainId"));
        Mockito.when(projectGraphNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(projectGraphNodeDO));
        Mockito.when(projectGraphDomainDatasourceService.getById(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(projectGraphDomainDatasourceDO);

        // Create TableAttr and ColumnAttr objects for testing.
        TaskConfig.TableAttr tableAttr = TaskConfig.TableAttr.newBuilder().
                setTableId("alice-table")
                .addColumnAttrs(TaskConfig.ColumnAttr.newBuilder()
                        .setColName("id1")
                        .setColName("id").build())
                .build();
        // Create ProjectDatatableDO.TableColumnConfig  for testing.
        ProjectDatatableDO.TableColumnConfig tableColumnConfig = new ProjectDatatableDO.TableColumnConfig();
        tableColumnConfig.setAssociateKey(false);
        tableColumnConfig.setGroupKey(false);
        tableColumnConfig.setLabelKey(false);
        tableColumnConfig.setProtection(false);
        tableColumnConfig.setColName("testid");
        tableColumnConfig.setColType("str");
        ProjectDatatableDO projectDatatableDO1 = ProjectDatatableDO.builder()
                .upk(new ProjectDatatableDO.UPK("zqemwvop", "alice", "alice-table"))
                .tableConfig(List.of(tableColumnConfig))
                .build();
        ProjectDatatableDO projectDatatableDO2 = ProjectDatatableDO.builder()
                .upk(new ProjectDatatableDO.UPK("sadasdas", "bob", "bob-table"))
                .tableConfig(List.of(tableColumnConfig))
                .build();
        Mockito.when(projectDatatableRepository.findByDatableId(Mockito.any(),Mockito.any())).thenReturn(List.of(projectDatatableDO1));
        GraphContext.set(new ProjectDO(), new GraphContext.GraphParties(), false);
        GraphContext.set(List.of(tableAttr));
        Job.CreateJobRequest result = kusciaJobConverter.converter(job);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(job.getJobId(), result.getJobId());
    }

    /**
     * test kuscia job converter with platformType AUTONOMY and GraphContext.isBreakpoint()
     *
     */

    @Test
    public void testconverter_autonomy_breakpoint() {
        // Create a NodeDef object for testing.
        Pipeline.NodeDef nodeDef = Pipeline.NodeDef.newBuilder()
                .setDomain("stats")
                .setName("scql_analysis")
                .setVersion("1.0.0")
                .addAttrPaths("column_config")
                .addAttrPaths("script_input")
                .addAttrs(Struct.newBuilder()
                        .putFields("s", Value.newBuilder()
                                .setStringValue("\n").build()).build())
                .addAttrs(Struct.newBuilder()
                        .putFields("s", Value.newBuilder()
                                .setStringValue("select a.age,b.id2 from alice_sygycijh_node_3_output_0 a, bob_sygycijh_node_3_output_0 b;")
                                .build())
                        .putFields("is_na", Value.newBuilder()
                                .setBoolValue(false).build()
                        ).build())
                .build();

        // Set the platformType and crossSiloCommBackend fields of kusciaJobConverter using ReflectionTestUtils.
        ReflectionTestUtils.setField(kusciaJobConverter, "platformType", "AUTONOMY");
        ReflectionTestUtils.setField(kusciaJobConverter, "crossSiloCommBackend", "brpc_link");

        Mockito.when(envService.findLocalNodeId(Mockito.any())).thenReturn("alice");
        // Create a JobTask object for testing.
        List<String> parties = new ArrayList<>();
        parties.add("alice");
        parties.add("bob");
        ProjectJob.JobTask task = ProjectJob.JobTask.builder()
                .taskId("xect-sect")
                .parties(parties)
                .build();

        // Create a ProjectJob object for testing.
        ProjectJob job = new ProjectJob();
        job.setJobId("jobId");
        job.setProjectId("testProjectId");
        job.setGraphId("testGraphId");
        job.setMaxParallelism(1);

        // Create a GraphNodeInfo object for testing.
        GraphNodeInfo graphNode = new GraphNodeInfo();
        graphNode.setNodeDef(nodeDef);
        graphNode.setGraphNodeId("sygycijh-node-33");
        graphNode.setLabel("自定义SCQL分析");
        graphNode.setCodeName("stats/scql_analysis");
        graphNode.setInputs(List.of("ltct-sygycijh-node-3-output-0"));
        graphNode.setOutputs(List.of("ebyt-sygycijh-node-33-output-0"));
        task.setNode(graphNode);
        job.setTasks(List.of(task));
        // Set the deviceConfig field of kusciaJobConverter.
        Map<String, String> deviceConfig = new HashMap<>();
        deviceConfig.put("spu", spu);
        deviceConfig.put("heu", heu);
        kusciaJobConverter.setDeviceConfig(deviceConfig);
        // Create a ProjectGraphDomainDatasourceDO object for testing.
        ProjectGraphDomainDatasourceDO projectGraphDomainDatasourceDO = new ProjectGraphDomainDatasourceDO();
        projectGraphDomainDatasourceDO.setDataSourceId("testDataSourceId");
        projectGraphDomainDatasourceDO.setDataSourceName("testDataSourceName");
        projectGraphDomainDatasourceDO.setEditEnable(true);
        projectGraphDomainDatasourceDO.setUpk(new ProjectGraphDomainDatasourceDO.UPK("testProjectId", "testGraphId", "testDomainId"));
        Mockito.when(projectGraphDomainDatasourceService.getById(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(projectGraphDomainDatasourceDO);

        ProjectGraphNodeDO projectGraphNodeDO = new ProjectGraphNodeDO();
        projectGraphNodeDO.setUpk(new ProjectGraphNodeDO.UPK("testProjectId", "testGraphId", "testDomainId"));
        projectGraphNodeDO.setNodeDef(nodeDef);

        GraphEdgeDO graphEdgeDO = new GraphEdgeDO();
        graphEdgeDO.setEdgeId("testEdgeId");
        graphEdgeDO.setSource("testSourceNodeId");
        graphEdgeDO.setTarget("testTargetNodeId");
        ProjectGraphDO projectGraphDO = new ProjectGraphDO();
        projectGraphDO.setEdges(List.of(graphEdgeDO));
        projectGraphDO.setOwnerId("test");
        Mockito.when(projectGraphRepository.findById(Mockito.any())).thenReturn(Optional.of(projectGraphDO));
        projectGraphDomainDatasourceDO.setUpk(new ProjectGraphDomainDatasourceDO.UPK("testProjectId", "testGraphId", "testDomainId"));
        Mockito.when(projectGraphNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(projectGraphNodeDO));
        Mockito.when(projectGraphDomainDatasourceService.getById(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(projectGraphDomainDatasourceDO);


        // Create TableAttr and ColumnAttr objects for testing.
        TaskConfig.TableAttr tableAttr = TaskConfig.TableAttr.newBuilder().
                setTableId("alice-table")
                .addColumnAttrs(TaskConfig.ColumnAttr.newBuilder()
                        .setColName("id1")
                        .setColName("id").build())
                .build();
        // Create ProjectDatatableDO.TableColumnConfig  for testing.
        ProjectDatatableDO.TableColumnConfig tableColumnConfig = new ProjectDatatableDO.TableColumnConfig();
        tableColumnConfig.setAssociateKey(false);
        tableColumnConfig.setGroupKey(false);
        tableColumnConfig.setLabelKey(false);
        tableColumnConfig.setProtection(false);
        tableColumnConfig.setColName("testid");
        tableColumnConfig.setColType("str");
        ProjectDatatableDO projectDatatableDO1 = ProjectDatatableDO.builder()
                .upk(new ProjectDatatableDO.UPK("zqemwvop", "alice", "alice-table"))
                .tableConfig(List.of(tableColumnConfig))
                .build();
        ProjectDatatableDO projectDatatableDO2 = ProjectDatatableDO.builder()
                .upk(new ProjectDatatableDO.UPK("sadasdas", "bob", "bob-table"))
                .tableConfig(List.of(tableColumnConfig))
                .build();
        Mockito.when(projectDatatableRepository.findByDatableId(Mockito.any(),Mockito.any())).thenReturn(List.of(projectDatatableDO1));
        GraphContext.set(new ProjectDO(), new GraphContext.GraphParties(), true);
        ProjectTaskDO projectTaskDO = new ProjectTaskDO();
        projectTaskDO.setUpk(new ProjectTaskDO.UPK("testProjectId", "testGraphId", "testTaskId"));
        projectTaskDO.setParties(List.of("alice", "bob"));
        projectTaskDO.setGraphNodeId("sygycijh-node-33");
        Mockito.when(taskRepository.findLastTimeTasks(Mockito.any(), Mockito.any())).thenReturn(List.of(projectTaskDO));

        GraphContext.set(List.of(tableAttr));
        Job.CreateJobRequest result = kusciaJobConverter.converter(job);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(job.getJobId(), result.getJobId());
    }
    /**
     * test kuscia job converter with platformType AUTONOMY and !STATS_SCQL_ANALYSIS.equalsIgnoreCase(graphNode.getCodeName())
     *
     */
    @Test
    public void testconverter_autonomy_stats_scql_analysis() {
        // Create a NodeDef object for testing.
        Pipeline.NodeDef nodeDef = Pipeline.NodeDef.newBuilder()
                .setDomain("stats")
                .setName("scql_analysis")
                .setVersion("1.0.0")
                .addAttrPaths("column_config")
                .addAttrPaths("script_input")
                .addAttrs(Struct.newBuilder()
                        .putFields("s", Value.newBuilder()
                                .setStringValue("\n").build()).build())
                .addAttrs(Struct.newBuilder()
                        .putFields("s", Value.newBuilder()
                                .setStringValue("select a.age,b.id2 from alice_sygycijh_node_3_output_0 a, bob_sygycijh_node_3_output_0 b;")
                                .build())
                        .putFields("is_na", Value.newBuilder()
                                .setBoolValue(false).build()
                        ).build())
                .build();

        // Set the platformType and crossSiloCommBackend fields of kusciaJobConverter using ReflectionTestUtils.
        ReflectionTestUtils.setField(kusciaJobConverter, "platformType", "AUTONOMY");
        ReflectionTestUtils.setField(kusciaJobConverter, "crossSiloCommBackend", "brpc_link");

        Mockito.when(envService.findLocalNodeId(Mockito.any())).thenReturn("alice");
        // Create a JobTask object for testing.
        ProjectJob.JobTask task = ProjectJob.JobTask.builder()
                .taskId("xect-sect")
                .parties(List.of("alice", "bob"))
                .build();

        // Create a ProjectJob object for testing.
        ProjectJob job = new ProjectJob();
        job.setJobId("jobId");
        job.setProjectId("testProjectId");
        job.setGraphId("testGraphId");
        job.setMaxParallelism(1);

        // Create a GraphNodeInfo object for testing.
        GraphNodeInfo graphNode = new GraphNodeInfo();
        graphNode.setNodeDef(nodeDef);
        graphNode.setGraphNodeId("sygycijh-node-33");
        graphNode.setLabel("自定义SCQL分析");
        graphNode.setCodeName("stats/table_statistics");
        graphNode.setInputs(List.of("ltct-sygycijh-node-3-output-0"));
        graphNode.setOutputs(List.of("ebyt-sygycijh-node-33-output-0"));
        task.setNode(graphNode);
        job.setTasks(List.of(task));
        // Set the deviceConfig field of kusciaJobConverter.
        Map<String, String> deviceConfig = new HashMap<>();
        deviceConfig.put("spu", spu);
        deviceConfig.put("heu", heu);
        kusciaJobConverter.setDeviceConfig(deviceConfig);
        // Create a ProjectGraphDomainDatasourceDO object for testing.
        ProjectGraphDomainDatasourceDO projectGraphDomainDatasourceDO = new ProjectGraphDomainDatasourceDO();
        projectGraphDomainDatasourceDO.setDataSourceId("testDataSourceId");
        projectGraphDomainDatasourceDO.setDataSourceName("testDataSourceName");
        projectGraphDomainDatasourceDO.setEditEnable(true);
        projectGraphDomainDatasourceDO.setUpk(new ProjectGraphDomainDatasourceDO.UPK("testProjectId", "testGraphId", "testDomainId"));
        Mockito.when(projectGraphDomainDatasourceService.getById(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(projectGraphDomainDatasourceDO);
        // Create TableAttr and ColumnAttr objects for testing.
        TaskConfig.TableAttr tableAttr = TaskConfig.TableAttr.newBuilder().
                setTableId("alice-table")
                .addColumnAttrs(TaskConfig.ColumnAttr.newBuilder()
                        .setColName("id1")
                        .setColName("id").build())
                .build();
        // Create ProjectDatatableDO.TableColumnConfig  for testing.
        ProjectDatatableDO.TableColumnConfig tableColumnConfig = new ProjectDatatableDO.TableColumnConfig();
        tableColumnConfig.setAssociateKey(false);
        tableColumnConfig.setGroupKey(false);
        tableColumnConfig.setLabelKey(false);
        tableColumnConfig.setProtection(false);
        tableColumnConfig.setColName("testid");
        tableColumnConfig.setColType("str");
        ProjectDatatableDO projectDatatableDO1 = ProjectDatatableDO.builder()
                .upk(new ProjectDatatableDO.UPK("zqemwvop", "alice", "alice-table"))
                .tableConfig(List.of(tableColumnConfig))
                .build();
        ProjectDatatableDO projectDatatableDO2 = ProjectDatatableDO.builder()
                .upk(new ProjectDatatableDO.UPK("sadasdas", "bob", "bob-table"))
                .tableConfig(List.of(tableColumnConfig))
                .build();
        GraphContext.set(new ProjectDO(), new GraphContext.GraphParties(), true);
        ProjectTaskDO projectTaskDO = new ProjectTaskDO();
        projectTaskDO.setUpk(new ProjectTaskDO.UPK("testProjectId", "testGraphId", "testTaskId"));
        projectTaskDO.setParties(List.of("alice", "bob"));
        projectTaskDO.setGraphNodeId("sygycijh-node-33");
        Mockito.when(taskRepository.findLastTimeTasks(Mockito.any(), Mockito.any())).thenReturn(List.of(projectTaskDO));
        Mockito.when(readPartitionRuleAnalysisService.readPartitionRuleAnalysis(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any())).thenReturn("test");
        GraphContext.set(List.of(tableAttr));
        Job.CreateJobRequest result = kusciaJobConverter.converter(job);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(job.getJobId(), result.getJobId());
    }

    /**
     * tableColumnConfig is Protection
     */
    @Test
    public void testconverter_tableColumnConfigIsProtection() {
        // Create a NodeDef object for testing.
        Pipeline.NodeDef nodeDef = Pipeline.NodeDef.newBuilder()
                .setDomain("stats")
                .setName("scql_analysis")
                .setVersion("1.0.0")
                .addAttrPaths("column_config")
                .addAttrPaths("script_input")
                .addAttrs(Struct.newBuilder()
                        .putFields("s", Value.newBuilder()
                                .setStringValue("\n").build()).build())
                .addAttrs(Struct.newBuilder()
                        .putFields("s", Value.newBuilder()
                                .setStringValue("select a.age,b.id2 from alice_sygycijh_node_3_output_0 a, bob_sygycijh_node_3_output_0 b;")
                                .build())
                        .putFields("is_na", Value.newBuilder()
                                .setBoolValue(false).build()
                        ).build())
                .build();

        // Set the platformType and crossSiloCommBackend fields of kusciaJobConverter using ReflectionTestUtils.
        ReflectionTestUtils.setField(kusciaJobConverter, "platformType", "AUTONOMY");
        ReflectionTestUtils.setField(kusciaJobConverter, "crossSiloCommBackend", "brpc_link");

        Mockito.when(envService.findLocalNodeId(Mockito.any())).thenReturn("alice");
        // Create a JobTask object for testing.
        ProjectJob.JobTask task = ProjectJob.JobTask.builder()
                .taskId("xect-sect")
                .parties(List.of("alice", "bob"))
                .build();

        // Create a ProjectJob object for testing.
        ProjectJob job = new ProjectJob();
        job.setJobId("jobId");
        job.setProjectId("testProjectId");
        job.setGraphId("testGraphId");
        job.setMaxParallelism(1);

        // Create a GraphNodeInfo object for testing.
        GraphNodeInfo graphNode = new GraphNodeInfo();
        graphNode.setNodeDef(nodeDef);
        graphNode.setGraphNodeId("sygycijh-node-33");
        graphNode.setLabel("自定义SCQL分析");
        graphNode.setCodeName("stats/table_statistics");
        graphNode.setInputs(List.of("ltct-sygycijh-node-3-output-0"));
        graphNode.setOutputs(List.of("ebyt-sygycijh-node-33-output-0"));
        task.setNode(graphNode);
        job.setTasks(List.of(task));
        // Set the deviceConfig field of kusciaJobConverter.
        Map<String, String> deviceConfig = new HashMap<>();
        deviceConfig.put("spu", spu);
        deviceConfig.put("heu", heu);
        kusciaJobConverter.setDeviceConfig(deviceConfig);
        // Create a ProjectGraphDomainDatasourceDO object for testing.
        ProjectGraphDomainDatasourceDO projectGraphDomainDatasourceDO = new ProjectGraphDomainDatasourceDO();
        projectGraphDomainDatasourceDO.setDataSourceId("testDataSourceId");
        projectGraphDomainDatasourceDO.setDataSourceName("testDataSourceName");
        projectGraphDomainDatasourceDO.setEditEnable(true);
        projectGraphDomainDatasourceDO.setUpk(new ProjectGraphDomainDatasourceDO.UPK("testProjectId", "testGraphId", "testDomainId"));
        Mockito.when(projectGraphDomainDatasourceService.getById(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(projectGraphDomainDatasourceDO);
        // Create TableAttr and ColumnAttr objects for testing.
        TaskConfig.TableAttr tableAttr = TaskConfig.TableAttr.newBuilder().
                setTableId("alice-table")
                .addColumnAttrs(TaskConfig.ColumnAttr.newBuilder()
                        .setColName("id1")
                        .setColName("id").build())
                .build();
        // Create ProjectDatatableDO.TableColumnConfig  for testing.
        ProjectDatatableDO.TableColumnConfig tableColumnConfig = new ProjectDatatableDO.TableColumnConfig();
        tableColumnConfig.setAssociateKey(false);
        tableColumnConfig.setGroupKey(false);
        tableColumnConfig.setLabelKey(false);
        tableColumnConfig.setProtection(true);
        tableColumnConfig.setColName("testid");
        tableColumnConfig.setColType("str");
        ProjectDatatableDO projectDatatableDO1 = ProjectDatatableDO.builder()
                .upk(new ProjectDatatableDO.UPK("zqemwvop", "alice", "alice-table"))
                .tableConfig(List.of(tableColumnConfig))
                .build();
        ProjectDatatableDO projectDatatableDO2 = ProjectDatatableDO.builder()
                .upk(new ProjectDatatableDO.UPK("sadasdas", "bob", "bob-table"))
                .tableConfig(List.of(tableColumnConfig))
                .build();
        GraphContext.set(new ProjectDO(), new GraphContext.GraphParties(), true);
        ProjectTaskDO projectTaskDO = new ProjectTaskDO();
        projectTaskDO.setUpk(new ProjectTaskDO.UPK("testProjectId", "testGraphId", "testTaskId"));
        projectTaskDO.setParties(List.of("alice", "bob"));
        projectTaskDO.setGraphNodeId("sygycijh-node-33");
        Mockito.when(taskRepository.findLastTimeTasks(Mockito.any(), Mockito.any())).thenReturn(List.of(projectTaskDO));
        Mockito.when(readPartitionRuleAnalysisService.readPartitionRuleAnalysis(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any())).thenReturn("test");
        GraphContext.set(List.of(tableAttr));
        Job.CreateJobRequest result = kusciaJobConverter.converter(job);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(job.getJobId(), result.getJobId());
    }

    /**
     * tableColumnConfig is Protection and AssociateKey
     */
    @Test
    public void testconverter_tableColumnConfigIsAssociateKey() {
        // Create a NodeDef object for testing.
        Pipeline.NodeDef nodeDef = Pipeline.NodeDef.newBuilder()
                .setDomain("stats")
                .setName("scql_analysis")
                .setVersion("1.0.0")
                .addAttrPaths("column_config")
                .addAttrPaths("script_input")
                .addAttrs(Struct.newBuilder()
                        .putFields("s", Value.newBuilder()
                                .setStringValue("\n").build()).build())
                .addAttrs(Struct.newBuilder()
                        .putFields("s", Value.newBuilder()
                                .setStringValue("select a.age,b.id2 from alice_sygycijh_node_3_output_0 a, bob_sygycijh_node_3_output_0 b;")
                                .build())
                        .putFields("is_na", Value.newBuilder()
                                .setBoolValue(false).build()
                        ).build())
                .build();

        // Set the platformType and crossSiloCommBackend fields of kusciaJobConverter using ReflectionTestUtils.
        ReflectionTestUtils.setField(kusciaJobConverter, "platformType", "AUTONOMY");
        ReflectionTestUtils.setField(kusciaJobConverter, "crossSiloCommBackend", "brpc_link");

        Mockito.when(envService.findLocalNodeId(Mockito.any())).thenReturn("alice");
        // Create a JobTask object for testing.
        ProjectJob.JobTask task = ProjectJob.JobTask.builder()
                .taskId("xect-sect")
                .parties(List.of("alice", "bob"))
                .build();

        // Create a ProjectJob object for testing.
        ProjectJob job = new ProjectJob();
        job.setJobId("jobId");
        job.setProjectId("testProjectId");
        job.setGraphId("testGraphId");
        job.setMaxParallelism(1);

        // Create a GraphNodeInfo object for testing.
        GraphNodeInfo graphNode = new GraphNodeInfo();
        graphNode.setNodeDef(nodeDef);
        graphNode.setGraphNodeId("sygycijh-node-33");
        graphNode.setLabel("自定义SCQL分析");
        graphNode.setCodeName("stats/table_statistics");
        graphNode.setInputs(List.of("ltct-sygycijh-node-3-output-0"));
        graphNode.setOutputs(List.of("ebyt-sygycijh-node-33-output-0"));
        task.setNode(graphNode);
        job.setTasks(List.of(task));
        // Set the deviceConfig field of kusciaJobConverter.
        Map<String, String> deviceConfig = new HashMap<>();
        deviceConfig.put("spu", spu);
        deviceConfig.put("heu", heu);
        kusciaJobConverter.setDeviceConfig(deviceConfig);
        // Create a ProjectGraphDomainDatasourceDO object for testing.
        ProjectGraphDomainDatasourceDO projectGraphDomainDatasourceDO = new ProjectGraphDomainDatasourceDO();
        projectGraphDomainDatasourceDO.setDataSourceId("testDataSourceId");
        projectGraphDomainDatasourceDO.setDataSourceName("testDataSourceName");
        projectGraphDomainDatasourceDO.setEditEnable(true);
        projectGraphDomainDatasourceDO.setUpk(new ProjectGraphDomainDatasourceDO.UPK("testProjectId", "testGraphId", "testDomainId"));
        Mockito.when(projectGraphDomainDatasourceService.getById(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(projectGraphDomainDatasourceDO);
        // Create TableAttr and ColumnAttr objects for testing.
        TaskConfig.TableAttr tableAttr = TaskConfig.TableAttr.newBuilder().
                setTableId("alice-table")
                .addColumnAttrs(TaskConfig.ColumnAttr.newBuilder()
                        .setColName("id1")
                        .setColName("id").build())
                .build();
        // Create ProjectDatatableDO.TableColumnConfig  for testing.
        ProjectDatatableDO.TableColumnConfig tableColumnConfig = new ProjectDatatableDO.TableColumnConfig();
        tableColumnConfig.setAssociateKey(true);
        tableColumnConfig.setGroupKey(false);
        tableColumnConfig.setLabelKey(false);
        tableColumnConfig.setProtection(true);
        tableColumnConfig.setColName("testid");
        tableColumnConfig.setColType("str");
        ProjectDatatableDO projectDatatableDO1 = ProjectDatatableDO.builder()
                .upk(new ProjectDatatableDO.UPK("zqemwvop", "alice", "alice-table"))
                .tableConfig(List.of(tableColumnConfig))
                .build();
        ProjectDatatableDO projectDatatableDO2 = ProjectDatatableDO.builder()
                .upk(new ProjectDatatableDO.UPK("sadasdas", "bob", "bob-table"))
                .tableConfig(List.of(tableColumnConfig))
                .build();
        GraphContext.set(new ProjectDO(), new GraphContext.GraphParties(), true);
        ProjectTaskDO projectTaskDO = new ProjectTaskDO();
        projectTaskDO.setUpk(new ProjectTaskDO.UPK("testProjectId", "testGraphId", "testTaskId"));
        projectTaskDO.setParties(List.of("alice", "bob"));
        projectTaskDO.setGraphNodeId("sygycijh-node-33");
        Mockito.when(taskRepository.findLastTimeTasks(Mockito.any(), Mockito.any())).thenReturn(List.of(projectTaskDO));
        Mockito.when(readPartitionRuleAnalysisService.readPartitionRuleAnalysis(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any())).thenReturn("test");
        GraphContext.set(List.of(tableAttr));
        Job.CreateJobRequest result = kusciaJobConverter.converter(job);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(job.getJobId(), result.getJobId());
    }

    /**
     * tableColumnConfig is Protection and GroupKey
     */
    @Test
    public void testconverter_tableColumnConfigIsGroupKey() {
        // Create a NodeDef object for testing.
        Pipeline.NodeDef nodeDef = Pipeline.NodeDef.newBuilder()
                .setDomain("stats")
                .setName("scql_analysis")
                .setVersion("1.0.0")
                .addAttrPaths("column_config")
                .addAttrPaths("script_input")
                .addAttrs(Struct.newBuilder()
                        .putFields("s", Value.newBuilder()
                                .setStringValue("\n").build()).build())
                .addAttrs(Struct.newBuilder()
                        .putFields("s", Value.newBuilder()
                                .setStringValue("select a.age,b.id2 from alice_sygycijh_node_3_output_0 a, bob_sygycijh_node_3_output_0 b;")
                                .build())
                        .putFields("is_na", Value.newBuilder()
                                .setBoolValue(false).build()
                        ).build())
                .build();

        // Set the platformType and crossSiloCommBackend fields of kusciaJobConverter using ReflectionTestUtils.
        ReflectionTestUtils.setField(kusciaJobConverter, "platformType", "AUTONOMY");
        ReflectionTestUtils.setField(kusciaJobConverter, "crossSiloCommBackend", "brpc_link");

        Mockito.when(envService.findLocalNodeId(Mockito.any())).thenReturn("alice");
        // Create a JobTask object for testing.
        ProjectJob.JobTask task = ProjectJob.JobTask.builder()
                .taskId("xect-sect")
                .parties(List.of("alice", "bob"))
                .build();

        // Create a ProjectJob object for testing.
        ProjectJob job = new ProjectJob();
        job.setJobId("jobId");
        job.setProjectId("testProjectId");
        job.setGraphId("testGraphId");
        job.setMaxParallelism(1);

        // Create a GraphNodeInfo object for testing.
        GraphNodeInfo graphNode = new GraphNodeInfo();
        graphNode.setNodeDef(nodeDef);
        graphNode.setGraphNodeId("sygycijh-node-33");
        graphNode.setLabel("自定义SCQL分析");
        graphNode.setCodeName("stats/table_statistics");
        graphNode.setInputs(List.of("ltct-sygycijh-node-3-output-0"));
        graphNode.setOutputs(List.of("ebyt-sygycijh-node-33-output-0"));
        task.setNode(graphNode);
        job.setTasks(List.of(task));
        // Set the deviceConfig field of kusciaJobConverter.
        Map<String, String> deviceConfig = new HashMap<>();
        deviceConfig.put("spu", spu);
        deviceConfig.put("heu", heu);
        kusciaJobConverter.setDeviceConfig(deviceConfig);
        // Create a ProjectGraphDomainDatasourceDO object for testing.
        ProjectGraphDomainDatasourceDO projectGraphDomainDatasourceDO = new ProjectGraphDomainDatasourceDO();
        projectGraphDomainDatasourceDO.setDataSourceId("testDataSourceId");
        projectGraphDomainDatasourceDO.setDataSourceName("testDataSourceName");
        projectGraphDomainDatasourceDO.setEditEnable(true);
        projectGraphDomainDatasourceDO.setUpk(new ProjectGraphDomainDatasourceDO.UPK("testProjectId", "testGraphId", "testDomainId"));
        Mockito.when(projectGraphDomainDatasourceService.getById(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(projectGraphDomainDatasourceDO);
        // Create TableAttr and ColumnAttr objects for testing.
        TaskConfig.TableAttr tableAttr = TaskConfig.TableAttr.newBuilder().
                setTableId("alice-table")
                .addColumnAttrs(TaskConfig.ColumnAttr.newBuilder()
                        .setColName("id1")
                        .setColName("id").build())
                .build();
        // Create ProjectDatatableDO.TableColumnConfig  for testing.
        ProjectDatatableDO.TableColumnConfig tableColumnConfig = new ProjectDatatableDO.TableColumnConfig();
        tableColumnConfig.setAssociateKey(false);
        tableColumnConfig.setGroupKey(true);
        tableColumnConfig.setLabelKey(false);
        tableColumnConfig.setProtection(true);
        tableColumnConfig.setColName("testid");
        tableColumnConfig.setColType("str");
        ProjectDatatableDO projectDatatableDO1 = ProjectDatatableDO.builder()
                .upk(new ProjectDatatableDO.UPK("zqemwvop", "alice", "alice-table"))
                .tableConfig(List.of(tableColumnConfig))
                .build();
        ProjectDatatableDO projectDatatableDO2 = ProjectDatatableDO.builder()
                .upk(new ProjectDatatableDO.UPK("sadasdas", "bob", "bob-table"))
                .tableConfig(List.of(tableColumnConfig))
                .build();
        GraphContext.set(new ProjectDO(), new GraphContext.GraphParties(), true);
        ProjectTaskDO projectTaskDO = new ProjectTaskDO();
        projectTaskDO.setUpk(new ProjectTaskDO.UPK("testProjectId", "testGraphId", "testTaskId"));
        projectTaskDO.setParties(List.of("alice", "bob"));
        projectTaskDO.setGraphNodeId("sygycijh-node-33");
        Mockito.when(taskRepository.findLastTimeTasks(Mockito.any(), Mockito.any())).thenReturn(List.of(projectTaskDO));
        Mockito.when(readPartitionRuleAnalysisService.readPartitionRuleAnalysis(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any())).thenReturn("test");
        GraphContext.set(List.of(tableAttr));
        Job.CreateJobRequest result = kusciaJobConverter.converter(job);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(job.getJobId(), result.getJobId());
    }

    /**
     * tableColumnConfig is Protection and LabelKey
     */
    @Test
    public void testconverter_tableColumnConfigIsLabelKey() {
        // Create a NodeDef object for testing.
        Pipeline.NodeDef nodeDef = Pipeline.NodeDef.newBuilder()
                .setDomain("stats")
                .setName("scql_analysis")
                .setVersion("1.0.0")
                .addAttrPaths("column_config")
                .addAttrPaths("script_input")
                .addAttrs(Struct.newBuilder()
                        .putFields("s", Value.newBuilder()
                                .setStringValue("\n").build()).build())
                .addAttrs(Struct.newBuilder()
                        .putFields("s", Value.newBuilder()
                                .setStringValue("select a.age,b.id2 from alice_sygycijh_node_3_output_0 a, bob_sygycijh_node_3_output_0 b;")
                                .build())
                        .putFields("is_na", Value.newBuilder()
                                .setBoolValue(false).build()
                        ).build())
                .build();

        // Set the platformType and crossSiloCommBackend fields of kusciaJobConverter using ReflectionTestUtils.
        ReflectionTestUtils.setField(kusciaJobConverter, "platformType", "AUTONOMY");
        ReflectionTestUtils.setField(kusciaJobConverter, "crossSiloCommBackend", "brpc_link");

        Mockito.when(envService.findLocalNodeId(Mockito.any())).thenReturn("alice");
        // Create a JobTask object for testing.
        ProjectJob.JobTask task = ProjectJob.JobTask.builder()
                .taskId("xect-sect")
                .parties(List.of("alice", "bob"))
                .build();

        // Create a ProjectJob object for testing.
        ProjectJob job = new ProjectJob();
        job.setJobId("jobId");
        job.setProjectId("testProjectId");
        job.setGraphId("testGraphId");
        job.setMaxParallelism(1);

        // Create a GraphNodeInfo object for testing.
        GraphNodeInfo graphNode = new GraphNodeInfo();
        graphNode.setNodeDef(nodeDef);
        graphNode.setGraphNodeId("sygycijh-node-33");
        graphNode.setLabel("自定义SCQL分析");
        graphNode.setCodeName("stats/table_statistics");
        graphNode.setInputs(List.of("ltct-sygycijh-node-3-output-0"));
        graphNode.setOutputs(List.of("ebyt-sygycijh-node-33-output-0"));
        task.setNode(graphNode);
        job.setTasks(List.of(task));
        // Set the deviceConfig field of kusciaJobConverter.
        Map<String, String> deviceConfig = new HashMap<>();
        deviceConfig.put("spu", spu);
        deviceConfig.put("heu", heu);
        kusciaJobConverter.setDeviceConfig(deviceConfig);
        // Create a ProjectGraphDomainDatasourceDO object for testing.
        ProjectGraphDomainDatasourceDO projectGraphDomainDatasourceDO = new ProjectGraphDomainDatasourceDO();
        projectGraphDomainDatasourceDO.setDataSourceId("testDataSourceId");
        projectGraphDomainDatasourceDO.setDataSourceName("testDataSourceName");
        projectGraphDomainDatasourceDO.setEditEnable(true);
        projectGraphDomainDatasourceDO.setUpk(new ProjectGraphDomainDatasourceDO.UPK("testProjectId", "testGraphId", "testDomainId"));
        Mockito.when(projectGraphDomainDatasourceService.getById(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(projectGraphDomainDatasourceDO);
        // Create TableAttr and ColumnAttr objects for testing.
        TaskConfig.TableAttr tableAttr = TaskConfig.TableAttr.newBuilder().
                setTableId("alice-table")
                .addColumnAttrs(TaskConfig.ColumnAttr.newBuilder()
                        .setColName("id1")
                        .setColName("id").build())
                .build();
        // Create ProjectDatatableDO.TableColumnConfig  for testing.
        ProjectDatatableDO.TableColumnConfig tableColumnConfig = new ProjectDatatableDO.TableColumnConfig();
        tableColumnConfig.setAssociateKey(false);
        tableColumnConfig.setGroupKey(false);
        tableColumnConfig.setLabelKey(true);
        tableColumnConfig.setProtection(true);
        tableColumnConfig.setColName("testid");
        tableColumnConfig.setColType("str");
        ProjectDatatableDO projectDatatableDO1 = ProjectDatatableDO.builder()
                .upk(new ProjectDatatableDO.UPK("zqemwvop", "alice", "alice-table"))
                .tableConfig(List.of(tableColumnConfig))
                .build();
        ProjectDatatableDO projectDatatableDO2 = ProjectDatatableDO.builder()
                .upk(new ProjectDatatableDO.UPK("sadasdas", "bob", "bob-table"))
                .tableConfig(List.of(tableColumnConfig))
                .build();
        GraphContext.set(new ProjectDO(), new GraphContext.GraphParties(), true);
        ProjectTaskDO projectTaskDO = new ProjectTaskDO();
        projectTaskDO.setUpk(new ProjectTaskDO.UPK("testProjectId", "testGraphId", "testTaskId"));
        projectTaskDO.setParties(List.of("alice", "bob"));
        projectTaskDO.setGraphNodeId("sygycijh-node-33");
        Mockito.when(taskRepository.findLastTimeTasks(Mockito.any(), Mockito.any())).thenReturn(List.of(projectTaskDO));
        Mockito.when(readPartitionRuleAnalysisService.readPartitionRuleAnalysis(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn("test");
        GraphContext.set(List.of(tableAttr));
        Job.CreateJobRequest result = kusciaJobConverter.converter(job);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(job.getJobId(), result.getJobId());
    }


}
