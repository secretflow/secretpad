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

package org.secretflow.secretpad.service;

import org.secretflow.secretpad.service.model.graph.*;
import org.secretflow.secretpad.service.model.project.GetProjectJobTaskOutputRequest;

import org.secretflow.proto.component.Comp;

import java.util.List;

/**
 * Graph service interface
 *
 * @author yansi
 * @date 2023/5/29
 */
public interface GraphService {

    /**
     * List components
     *
     * @return component list view object
     */
    CompListVO listComponents();

    /**
     * Query component by get component request
     *
     * @param request get component request
     * @return componentDef message
     */
    Comp.ComponentDef getComponent(GetComponentRequest request);

    /**
     * Batch query component by get component request
     *
     * @param request get component request
     * @return componentDef message list
     */
    List<Comp.ComponentDef> batchGetComponent(List<GetComponentRequest> request);

    /**
     * List components from international location file then collect to map
     *
     * @return
     */
    Object listComponentI18n();

    /**
     * Create graph by create graph request
     *
     * @param request create graph request
     * @return create graph view object
     */
    CreateGraphVO createGraph(CreateGraphRequest request);

    /**
     * Delete graph by delete graph request
     *
     * @param request delete graph request
     */
    void deleteGraph(DeleteGraphRequest request);

    /**
     * List graph meta list by list graph request
     *
     * @param request list graph request
     * @return graph meta view object list
     */
    List<GraphMetaVO> listGraph(ListGraphRequest request);

    /**
     * Update graph meta by update graph meta request
     *
     * @param request update graph meta request
     */
    void updateGraphMeta(UpdateGraphMetaRequest request);

    /**
     * Fully update the graph by full update graph request
     *
     * @param request full update graph request
     */
    void fullUpdateGraph(FullUpdateGraphRequest request);

    /**
     * Update graph node by update graph node request
     *
     * @param request update graph node request
     */
    void updateGraphNode(UpdateGraphNodeRequest request);

    /**
     * Query result output by nodeId and resultId
     *
     * @param nodeId   target nodeId
     * @param resultId target resultId
     * @return result output view object
     */
    GraphNodeOutputVO getResultOutputVO(String nodeId, String resultId);

    /**
     * Start graph
     *
     * @param request start graph request
     * @return start graph view object
     */
    StartGraphVO startGraph(StartGraphRequest request);

    /**
     * List graph node status by list graph node status request
     *
     * @param request list graph node status request
     * @return graph node status
     */
    GraphStatus listGraphNodeStatus(ListGraphNodeStatusRequest request);

    /**
     * Stop graph node by stop graph node request
     *
     * @param request stop graph node request
     */
    void stopGraphNode(StopGraphNodeRequest request);

    /**
     * Query graph detail by get graph detail request
     *
     * @param request get graph detail request
     * @return graph detail view object
     */
    GraphDetailVO getGraphDetail(GetGraphRequest request);

    /**
     * Query graph node output by graph node output request
     *
     * @param request graph node output request
     * @return graph node output view object
     */
    GraphNodeOutputVO getGraphNodeOutput(GraphNodeOutputRequest request);

    /**
     * Query graph node logs from graph node logs request
     *
     * @param request graph node logs request
     * @return graph node task logs view object
     */
    GraphNodeTaskLogsVO getGraphNodeLogs(GraphNodeLogsRequest request);

    /**
     * Query graph node task output by get project job task output request
     *
     * @param request get project job task output request
     * @return graph node task output view object
     */
    GraphNodeOutputVO getGraphNodeTaskOutputVO(GetProjectJobTaskOutputRequest request);

}
