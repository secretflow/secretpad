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

package org.secretflow.secretpad.service.model.node;

import org.secretflow.secretpad.manager.integration.model.DatatableDTO;
import org.secretflow.secretpad.manager.integration.model.NodeDTO;
import org.secretflow.secretpad.manager.integration.model.NodeInstanceDTO;
import org.secretflow.secretpad.manager.integration.model.NodeRouteDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Node view object
 *
 * @author jiezi
 * @date 2023/5/31
 */
@Builder
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class NodeVO {
    /**
     * id
     */
    @Schema(description = "nodeId")
    private String nodeId;
    /**
     * nodeName
     */
    @Schema(description = "nodeName")
    private String nodeName;
    /**
     * controlNodeId
     */
    @Schema(description = "controlNodeId")
    private String controlNodeId;
    /**
     * description
     */
    @Schema(description = "description")
    private String description;
    /**
     * netAddress
     */
    @Schema(description = "netAddress")
    private String netAddress;
    /**
     * cert
     */
    @Schema(description = "cert")
    private String cert;
    /**
     * token
     */
    @Schema(description = "token")
    private String token;
    /**
     * tokenStatus used、unused
     */
    @Schema(description = "tokenStatus")
    private String tokenStatus;
    /**
     * nodeRole
     */
    @Schema(description = "nodeRole")
    private String nodeRole;
    /**
     * nodeStatus Pending,  Ready,  NotReady,  Unknown
     */
    @Schema(description = "节点状态")
    private String nodeStatus;
    /**
     * node type embedded
     */
    @Schema(description = "nodeType")
    private String type;
    /**
     * gmtCreate
     */
    @Schema(description = "gmtCreate")
    private String gmtCreate;
    /**
     * gmtModified
     */
    @Schema(description = "gmtModified")
    private String gmtModified;
    /**
     * instance list
     */
    @Schema(description = "instance list")
    private List<NodeInstanceDTO> nodeInstances;

    /**
     * datatables
     */
    @Schema(description = "datatables")
    private List<NodeDatatableVO> datatables;
    /**
     * nodeRoutes
     */
    @Schema(description = "nodeRoutes")
    private List<NodeRouteVO> nodeRoutes;
    /**
     * The count of node results. The detailed result information needs to be obtained through the result management list interface
     */
    @Schema(description = "the count of node results")
    private Long resultCount;

    public static NodeVO from(NodeDTO nodeDTO, List<DatatableDTO> datatables, List<NodeRouteDTO> nodeRoutes,
                              Long resultCount) {
        return NodeVO.builder().nodeId(nodeDTO.getNodeId()).nodeName(nodeDTO.getNodeName())
                .controlNodeId(nodeDTO.getControlNodeId()).description(nodeDTO.getDescription())
                .netAddress(nodeDTO.getNetAddress()).cert(nodeDTO.getCert()).token(nodeDTO.getToken())
                .tokenStatus(nodeDTO.getTokenStatus()).nodeRole(nodeDTO.getNodeRole()).nodeStatus(nodeDTO.getNodeStatus())
                .type(nodeDTO.getType()).gmtCreate(nodeDTO.getGmtCreate()).gmtModified(nodeDTO.getGmtModified())
                .nodeInstances(nodeDTO.getNodeInstances())
                .datatables(CollectionUtils.isEmpty(datatables) ? null
                        : datatables.stream().map(it -> new NodeDatatableVO(it.getDatatableId(), it.getDatatableName()))
                        .collect(Collectors.toList()))
                .nodeRoutes(CollectionUtils.isEmpty(nodeRoutes) ? null : nodeRoutes.stream()
                        .map(NodeRouteVO::fromDto).collect(Collectors.toList()))
                .resultCount(resultCount).build();
    }

    public static NodeVO fromDto(NodeDTO nodeDTO) {
        NodeVO nodeVO = new NodeVO();
        nodeVO.setNodeId(nodeDTO.getNodeId());
        nodeVO.setNodeName(nodeDTO.getNodeName());
        nodeVO.setControlNodeId(nodeDTO.getControlNodeId());
        nodeVO.setDescription(nodeDTO.getDescription());
        nodeVO.setNetAddress(nodeDTO.getNetAddress());
        nodeVO.setCert(nodeDTO.getCert());
        nodeVO.setToken(nodeDTO.getToken());
        nodeVO.setTokenStatus(nodeDTO.getTokenStatus());
        nodeVO.setNodeRole(nodeDTO.getNodeRole());
        nodeVO.setNodeStatus(nodeDTO.getNodeStatus());
        nodeVO.setType(nodeDTO.getType());
        nodeVO.setGmtCreate(nodeDTO.getGmtCreate());
        nodeVO.setGmtModified(nodeDTO.getGmtModified());
        nodeVO.setNodeInstances(nodeDTO.getNodeInstances());
        return nodeVO;
    }

}
