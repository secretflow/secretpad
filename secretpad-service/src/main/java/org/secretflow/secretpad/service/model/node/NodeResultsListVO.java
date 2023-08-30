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

import org.secretflow.secretpad.manager.integration.model.NodeResultListDTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Node results list view object
 *
 * @author : xiaonan.fhn
 * @date 2023/06/08
 */
@Builder
@Setter
@Getter
@ToString
public class NodeResultsListVO {

    /**
     * Node results view object
     */
    List<NodeResultsVO> nodeResultsVOList;

    /**
     * The count of node results
     */
    Integer totalResultNums;

    /**
     * Build node results list view object from node result list data transfer object
     *
     * @param nodeResultListDTO node result list data transfer object
     * @return node results list view object
     */
    public static NodeResultsListVO fromDTO(NodeResultListDTO nodeResultListDTO) {
        return NodeResultsListVO.builder()
                .totalResultNums(nodeResultListDTO.getTotalResultNums())
                .nodeResultsVOList(nodeResultListDTO.getNodeResultDTOList().stream().map(NodeResultsVO::fromNodeResultDTO).collect(Collectors.toList()))
                .build();
    }

}
