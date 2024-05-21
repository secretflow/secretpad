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

import lombok.*;
import org.apache.commons.lang3.ObjectUtils;

import java.util.List;
import java.util.Objects;

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

    public static String getTeeNodeId() {
        if (isTee()) {
            return Objects.requireNonNull(getProject()).getProjectInfo().getTeeDomainId();
        }
        return null;
    }

    public static void set(ProjectDO projectDO, GraphParties parties) {
        GRAPH_CONTEXT_BEAN_THREAD_LOCAL.set(new GraphContextBean(projectDO, parties));
    }

    public static void set(ProjectDO projectDO, GraphParties parties, Boolean breakpoint) {
        GRAPH_CONTEXT_BEAN_THREAD_LOCAL.set(new GraphContextBean(projectDO, parties, breakpoint));
    }

    public static void remove() {
        GRAPH_CONTEXT_BEAN_THREAD_LOCAL.remove();
    }

    public static class GraphContextBean {
        ProjectDO projectDO;
        GraphParties graphParties;
        Boolean breakpoint = false;


        public GraphContextBean(ProjectDO projectDO, GraphParties graphParties) {
            this.projectDO = projectDO;
            this.graphParties = graphParties;
        }

        public GraphContextBean(ProjectDO projectDO, GraphParties graphParties, Boolean breakpoint) {
            this.projectDO = projectDO;
            this.graphParties = graphParties;
            this.breakpoint = breakpoint;
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
}