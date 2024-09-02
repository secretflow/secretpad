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

import org.secretflow.secretpad.service.model.inst.InstRegisterRequest;
import org.secretflow.secretpad.service.model.inst.InstRequest;
import org.secretflow.secretpad.service.model.inst.InstTokenVO;
import org.secretflow.secretpad.service.model.inst.InstVO;
import org.secretflow.secretpad.service.model.node.CreateNodeRequest;
import org.secretflow.secretpad.service.model.node.NodeIdRequest;
import org.secretflow.secretpad.service.model.node.NodeTokenRequest;
import org.secretflow.secretpad.service.model.node.NodeVO;

import java.util.List;
import java.util.Set;

/**
 * Institution service interface
 *
 * @author yansi
 * @date 2023/5/4
 */
public interface InstService {

    /**
     * get current instVO
     */
    InstVO getInst(InstRequest request);

    /**
     * list by current ownerId （instId）
     */
    List<NodeVO> listNode();

    /**
     * list all nodeId in this inst by instId
     *
     * @return nodeId list
     */
    Set<String> listNodeIds();

    /**
     * token is not generate by node but by inst
     */
    InstTokenVO createNode(CreateNodeRequest request);

    /**
     * query exist token
     */
    InstTokenVO getToken(NodeTokenRequest request);


    /**
     * create new token
     * ratelimiter
     */
    InstTokenVO newToken(NodeTokenRequest request);


    /**
     * inst node delete
     */
    void deleteNode(NodeIdRequest request);


    void registerNode(InstRegisterRequest request);


    boolean checkNodeInInst(String instId, String nodeId);

    boolean checkNodesInInst(String instId, List<String> nodeIds);
}
