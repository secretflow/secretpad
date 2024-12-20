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

package org.secretflow.secretpad.service.graph;

import org.secretflow.secretpad.common.constant.ProjectConstants;
import org.secretflow.secretpad.common.errorcode.GraphErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.persistence.entity.ProjectDO;
import org.secretflow.secretpad.persistence.entity.ProjectJobDO;
import lombok.*;
import org.apache.commons.lang3.ObjectUtils;
import org.secretflow.proto.kuscia.TaskConfig;
import org.secretflow.v1alpha1.kusciaapi.Job;

import java.util.*;

/**
 * @author yutu
 * @date 2023/08/09
 */
public final class GraphContext {
    private static final ThreadLocal<GraphContextBean> GRAPH_CONTEXT_BEAN_THREAD_LOCAL = new ThreadLocal<>();

    private GraphContext() {
    }

    public static String getComputeMode() {
        if (getProject() == null) {
            return ProjectConstants.ComputeModeEnum.MPC.name();
        }
        return getProject().getComputeMode();
    }

    public static boolean isTee() {
        return getComputeMode().equals(ProjectConstants.ComputeModeEnum.TEE.name());
    }

    public static boolean isBreakpoint() {
        if (GRAPH_CONTEXT_BEAN_THREAD_LOCAL.get() == null) {
            return false;
        }
        return GRAPH_CONTEXT_BEAN_THREAD_LOCAL.get().breakpoint;
    }

    public static ProjectDO getProject() {
        if (GRAPH_CONTEXT_BEAN_THREAD_LOCAL.get() == null) {
            return null;
        }
        ProjectDO projectDO = GRAPH_CONTEXT_BEAN_THREAD_LOCAL.get().projectDO;
        if (ObjectUtils.isEmpty(projectDO)) {
            return null;
        }
        return projectDO;
    }

    public static GraphParties getGraphParties() {
        GraphParties graphParties = GRAPH_CONTEXT_BEAN_THREAD_LOCAL.get().graphParties;
        if (ObjectUtils.isEmpty(graphParties)) {
            throw SecretpadException.of(GraphErrorCode.GRAPH_NOT_EXISTS);
        }
        return graphParties;
    }

    public static Map<String, PartitionInfo> getTablePartitionRule() {
        return GRAPH_CONTEXT_BEAN_THREAD_LOCAL.get().table_partition_rule;
    }

    public static List<TaskConfig.TableAttr> getTableAttrs() {
        return GRAPH_CONTEXT_BEAN_THREAD_LOCAL.get().tableAttrs;
    }

    public static String getTablePartitionRule(String datableId) {
        Map<String, PartitionInfo> table_partition_rule = getTablePartitionRule();
        if (ObjectUtils.isEmpty(table_partition_rule)) {
            return null;
        }
        PartitionInfo partitionInfo = table_partition_rule.get(datableId);
        return ObjectUtils.isEmpty(partitionInfo) ? null : partitionInfo.getReadRule();
    }

    public static String getTablePartitionTableName(String datableId) {
        Map<String, PartitionInfo> table_partition_rule = getTablePartitionRule();
        if (ObjectUtils.isEmpty(table_partition_rule)) {
            return null;
        }
        PartitionInfo partitionInfo = table_partition_rule.get(datableId);
        return ObjectUtils.isEmpty(partitionInfo) ? null : partitionInfo.getTableName();
    }

    public static String getTeeNodeId() {
        if (isTee()) {
            return Objects.requireNonNull(getProject()).getProjectInfo().getTeeDomainId();
        }
        return null;
    }

    public static void set(ProjectDO projectDO, GraphParties parties) {
        GraphContextBean graphContextBean = GRAPH_CONTEXT_BEAN_THREAD_LOCAL.get();
        if (ObjectUtils.isNotEmpty(graphContextBean)) {
            graphContextBean.projectDO = projectDO;
            graphContextBean.graphParties = parties;
        } else {
            GRAPH_CONTEXT_BEAN_THREAD_LOCAL.set(new GraphContextBean(projectDO, parties));
        }
    }

    public static void set(ProjectDO projectDO, GraphParties parties, Boolean breakpoint) {
        GraphContextBean graphContextBean = GRAPH_CONTEXT_BEAN_THREAD_LOCAL.get();
        if (ObjectUtils.isNotEmpty(graphContextBean)) {
            graphContextBean.projectDO = projectDO;
            graphContextBean.graphParties = parties;
            graphContextBean.breakpoint = breakpoint;
        } else {
            GRAPH_CONTEXT_BEAN_THREAD_LOCAL.set(new GraphContextBean(projectDO, parties, breakpoint));
        }
    }

    public static void set(ProjectDO projectDO, GraphParties parties, Boolean breakpoint, HashMap<String, PartitionInfo> table_partition_rule) {
        GraphContextBean graphContextBean = GRAPH_CONTEXT_BEAN_THREAD_LOCAL.get();
        if (ObjectUtils.isNotEmpty(graphContextBean)) {
            graphContextBean.projectDO = projectDO;
            graphContextBean.graphParties = parties;
            graphContextBean.breakpoint = breakpoint;
            graphContextBean.table_partition_rule = table_partition_rule;
        } else {
            GRAPH_CONTEXT_BEAN_THREAD_LOCAL.set(new GraphContextBean(projectDO, parties, breakpoint, table_partition_rule));
        }
    }

    public static void set(HashMap<String, PartitionInfo> table_partition_rule) {
        GraphContextBean graphContextBean = GRAPH_CONTEXT_BEAN_THREAD_LOCAL.get();
        if (ObjectUtils.isNotEmpty(graphContextBean)) {
            Map<String, PartitionInfo> tablePartitionRule = graphContextBean.table_partition_rule;
            if (ObjectUtils.isEmpty(tablePartitionRule)) {
                graphContextBean.table_partition_rule = table_partition_rule;
            } else {
                tablePartitionRule.putAll(table_partition_rule);
            }
        }
    }

    public static void setIsScheduled(Boolean isScheduled) {
        GraphContextBean graphContextBean = GRAPH_CONTEXT_BEAN_THREAD_LOCAL.get();
        if (ObjectUtils.isNotEmpty(graphContextBean)) {
            graphContextBean.isScheduled = isScheduled;
        } else {
            GRAPH_CONTEXT_BEAN_THREAD_LOCAL.set(new GraphContextBean(isScheduled));
        }
    }

    public static Boolean isScheduled() {
        GraphContextBean graphContextBean = GRAPH_CONTEXT_BEAN_THREAD_LOCAL.get();
        if (ObjectUtils.isNotEmpty(graphContextBean)) {
            return graphContextBean.isScheduled;
        }
        return false;
    }

    public static Job.CreateJobRequest getRequest() {
        if (GRAPH_CONTEXT_BEAN_THREAD_LOCAL.get() == null) {
            return null;
        }
        return GRAPH_CONTEXT_BEAN_THREAD_LOCAL.get().request;
    }

    public static void setRequest(Job.CreateJobRequest request) {
        GraphContextBean graphContextBean = GRAPH_CONTEXT_BEAN_THREAD_LOCAL.get();
        if (ObjectUtils.isNotEmpty(graphContextBean)) {
            graphContextBean.request = request;
        } else {
            GRAPH_CONTEXT_BEAN_THREAD_LOCAL.set(new GraphContextBean(request));
        }
    }

    public static String getScheduleExpectStartDate() {
        if (GRAPH_CONTEXT_BEAN_THREAD_LOCAL.get() == null) {
            return null;
        }
        return GRAPH_CONTEXT_BEAN_THREAD_LOCAL.get().scheduleExpectStartDate;
    }

    public static void setScheduleExpectStartDate(String scheduleExpectStartDate) {
        GraphContextBean graphContextBean = GRAPH_CONTEXT_BEAN_THREAD_LOCAL.get();
        if (ObjectUtils.isNotEmpty(graphContextBean)) {
            graphContextBean.scheduleExpectStartDate = scheduleExpectStartDate;
        } else {
            GRAPH_CONTEXT_BEAN_THREAD_LOCAL.set(new GraphContextBean(scheduleExpectStartDate));
        }
    }

    public static ProjectJobDO getProjectJobDO() {
        if (GRAPH_CONTEXT_BEAN_THREAD_LOCAL.get() == null) {
            return null;
        }
        return GRAPH_CONTEXT_BEAN_THREAD_LOCAL.get().projectJobDO;
    }

    public static void setProjectJobDO(ProjectJobDO projectJobDO) {
        GraphContextBean graphContextBean = GRAPH_CONTEXT_BEAN_THREAD_LOCAL.get();
        if (ObjectUtils.isNotEmpty(graphContextBean)) {
            graphContextBean.projectJobDO = projectJobDO;
        } else {
            GRAPH_CONTEXT_BEAN_THREAD_LOCAL.set(new GraphContextBean(projectJobDO));
        }
    }

    public static void set(List<TaskConfig.TableAttr> tableAttrs) {
        GraphContextBean graphContextBean = GRAPH_CONTEXT_BEAN_THREAD_LOCAL.get();
        if (ObjectUtils.isNotEmpty(graphContextBean)) {
            graphContextBean.tableAttrs = tableAttrs;
        } else {
            GRAPH_CONTEXT_BEAN_THREAD_LOCAL.set(new GraphContextBean(null, null, null, null, tableAttrs));
        }
    }


    public static void remove() {
        GRAPH_CONTEXT_BEAN_THREAD_LOCAL.remove();
    }

    public static class GraphContextBean {
        ProjectDO projectDO;
        GraphParties graphParties;
        Boolean breakpoint = false;
        Boolean isScheduled = false;
        HashMap<String, PartitionInfo> table_partition_rule;
        Job.CreateJobRequest request;
        String scheduleExpectStartDate;
        ProjectJobDO projectJobDO;

        public GraphContextBean(String scheduleExpectStartDate) {
            this.scheduleExpectStartDate = scheduleExpectStartDate;
        }

        List<TaskConfig.TableAttr> tableAttrs;


        public GraphContextBean(ProjectDO projectDO, GraphParties graphParties) {
            this.projectDO = projectDO;
            this.graphParties = graphParties;
        }

        public GraphContextBean(Job.CreateJobRequest request) {
            this.request = request;
        }

        public GraphContextBean(Boolean isScheduled) {
            this.isScheduled = isScheduled;
        }

        public GraphContextBean(ProjectDO projectDO, GraphParties graphParties, Boolean breakpoint) {
            this.projectDO = projectDO;
            this.graphParties = graphParties;
            this.breakpoint = breakpoint;
        }

        public GraphContextBean(ProjectDO projectDO, GraphParties graphParties, Boolean breakpoint, HashMap<String, PartitionInfo> table_partition_rule) {
            this.projectDO = projectDO;
            this.graphParties = graphParties;
            this.breakpoint = breakpoint;
            this.table_partition_rule = table_partition_rule;
        }

        public GraphContextBean(ProjectJobDO projectJobDO) {
            this.projectJobDO = projectJobDO;
        }

        public GraphContextBean(ProjectDO projectDO, GraphParties graphParties, Boolean breakpoint, HashMap<String, PartitionInfo> table_partition_rule, List<TaskConfig.TableAttr> partyLists) {
            this.projectDO = projectDO;
            this.graphParties = graphParties;
            this.breakpoint = breakpoint;
            this.table_partition_rule = table_partition_rule;
            this.tableAttrs = partyLists;
        }
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Setter
    @Getter
    public static class GraphParties {
        List<GraphParty> parties;
    }

    @Builder
    @AllArgsConstructor
    @Setter
    @Getter
    public static class GraphParty {
        String node;
        String datatableId;
    }

    @Builder
    @AllArgsConstructor
    @Setter
    @Getter
    @ToString
    @NoArgsConstructor
    public static class PartitionInfo {
        private String readRule;
        private String tableName;
        private Set<String> partitionColumns;
    }
}