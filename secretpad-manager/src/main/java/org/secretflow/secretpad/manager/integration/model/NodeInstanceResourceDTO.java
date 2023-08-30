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

package org.secretflow.secretpad.manager.integration.model;

import lombok.Builder;
import lombok.Data;
import org.secretflow.v1alpha1.kusciaapi.Domain;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * NodeInstanceDTO
 *
 * @author yutu
 * @date 2023/08/07
 */
@Data
@Builder
public class NodeInstanceResourceDTO implements Serializable {
    /**
     * cpu|memory|storage|pods
     */
    private String name;
    /**
     * allocatable resource, unit: Ki|Mi|Gi or empty is byte
     */
    private String allocatable;
    /**
     * capacity resource, unit: Ki|Mi|Gi or empty is byte
     */
    private String capacity;

    public static List<NodeInstanceResourceDTO> formDomainNodeResources(List<Domain.NodeResource> resources) {
        List<NodeInstanceResourceDTO> list = new ArrayList<>();
        if (!CollectionUtils.isEmpty(resources)) {
            list = resources.stream().map(NodeInstanceResourceDTO::formDomainNodeResource).collect(Collectors.toList());
        }
        return list;
    }

    public static NodeInstanceResourceDTO formDomainNodeResource(Domain.NodeResource resource) {
        return NodeInstanceResourceDTO.builder()
                .name(resource.getName())
                .allocatable(resource.getAllocatable())
                .capacity(resource.getCapacity())
                .build();
    }
}