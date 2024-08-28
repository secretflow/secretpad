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

import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.manager.integration.model.DatatableDTO;
import org.secretflow.secretpad.manager.integration.model.NodeDTO;
import org.secretflow.secretpad.manager.integration.model.NodeInstanceDTO;
import org.secretflow.secretpad.manager.integration.model.NodeRouteDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Base64;
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

    @Schema(description = "instId")
    private String instId;

    @Schema(description = "instName")
    private String instName;
    /**
     * controlNodeId
     */
    @Schema(description = "controlNodeId")
    private String controlNodeId;
    /**
     * masterNodeId
     */
    @Schema(description = "masterNodeId")
    private String masterNodeId;
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
     * real cert, base64
     */
    @Schema(description = "certText")
    private String certText;
    /**
     * node authentication code
     */
    @Schema(description = "nodeAuthenticationCode")
    private String nodeAuthenticationCode;
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
    @Schema(description = "nodeStatus")
    private String nodeStatus;
    /**
     * node type embedded
     */
    @Schema(description = "nodeType")
    private String type;
    /**
     * node feature indicates by bit, bit0 - mpc | bit1 - tee | bit2 mpc&tee
     */
    @Schema(description = "node feature indicates by bit, bit0 - mpc | bit1 - tee | bit2 mpc&tee")
    private Integer mode;
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

    /**
     * kuscia api protocol
     */
    @Schema(description = "protocol")
    private String protocol;

    @Schema(description = "instToken")
    private String instToken;

    //now for p2p
    @Schema(description = "this node allow or not  to be deleted")
    private Boolean allowDeletion = false;

    @Schema(description = "the main node for network communication")
    private Boolean isMainNode = false;

    /**
     * simple copy
     */
    public static NodeVO from(NodeDTO nodeDTO) {
        return NodeVO.builder().
                nodeId(nodeDTO.getNodeId()).
                nodeName(nodeDTO.getNodeName()).
                nodeStatus(nodeDTO.getNodeStatus()).
                isMainNode(nodeDTO.getIsMainNode())
                .build();
    }


    public static NodeVO from(NodeDTO nodeDTO, List<DatatableDTO> datatables, List<NodeRouteDTO> nodeRoutes,
                              Long resultCount) {
        return NodeVO.builder().nodeId(nodeDTO.getNodeId()).nodeName(nodeDTO.getNodeName())
                .instId(nodeDTO.getInstId()).instName(nodeDTO.getInstName())
                .controlNodeId(nodeDTO.getControlNodeId()).description(nodeDTO.getDescription())
                .netAddress(nodeDTO.getNetAddress()).cert(nodeDTO.getCert()).token(nodeDTO.getToken())
                .tokenStatus(nodeDTO.getTokenStatus()).nodeRole(nodeDTO.getNodeRole()).nodeStatus(nodeDTO.getNodeStatus())
                .type(nodeDTO.getType()).gmtCreate(nodeDTO.getGmtCreate()).gmtModified(nodeDTO.getGmtModified())
                .nodeInstances(nodeDTO.getNodeInstances()).mode(nodeDTO.getMode())
                .masterNodeId(nodeDTO.getMasterNodeId())
                .certText(nodeDTO.getCertText())
                .nodeAuthenticationCode(buildNodeAuthenticationCode(nodeDTO))
                .datatables(CollectionUtils.isEmpty(datatables) ? null
                        : datatables.stream().map(it -> new NodeDatatableVO(it.getDatatableId(), it.getDatatableName()))
                        .collect(Collectors.toList()))
                .nodeRoutes(CollectionUtils.isEmpty(nodeRoutes) ? null : nodeRoutes.stream()
                        .map(NodeRouteVO::fromDto).collect(Collectors.toList()))
                .protocol(nodeDTO.getProtocol())
                .resultCount(resultCount)
                .instToken(nodeDTO.getInstToken())
                .isMainNode(nodeDTO.getIsMainNode())
                .allowDeletion(nodeDTO.getAllowDeletion())
                .build();
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
        nodeVO.setMode(nodeDTO.getMode());
        nodeVO.setMasterNodeId(nodeDTO.getMasterNodeId());
        nodeVO.setCertText(nodeDTO.getCertText());
        nodeVO.setNodeAuthenticationCode(buildNodeAuthenticationCode(nodeDTO));
        nodeVO.setInstToken(nodeDTO.getInstToken());
        return nodeVO;
    }

    /**
     * Build node authentication code
     *
     * @param nodeDTO node data transfer object
     * @return node authentication code
     */
    private static String buildNodeAuthenticationCode(NodeDTO nodeDTO) {
        NodeAuthenticationCode nodeAuthenticationCode = NodeAuthenticationCode.builder()
                .masterNodeId(nodeDTO.getMasterNodeId())
                .dstNodeId(nodeDTO.getNodeId())
                .dstNetAddress(nodeDTO.getNetAddress())
                .name(nodeDTO.getNodeName())
                .certText(nodeDTO.getCertText()).build();
        if (StringUtils.isNotBlank(nodeDTO.getInstId()) && StringUtils.isNotBlank(nodeDTO.getInstName())) {
            nodeAuthenticationCode.setInstId(nodeDTO.getInstId());
            nodeAuthenticationCode.setInstName(nodeDTO.getInstName());
        }
        return Base64.getEncoder().encodeToString(JsonUtils.toJSONString(nodeAuthenticationCode).getBytes());
    }
}
