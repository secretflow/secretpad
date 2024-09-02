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

package org.secretflow.secretpad.service.model.graph;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Get component request
 *
 * @author yansi
 * @date 2023/5/29
 */
@Data
public class GetComponentRequest {
    /**
     * app of the component, it can not be blank
     */
    @NotBlank
    private String app;
    /**
     * Namespace of the component, it can not be blank
     */
    @NotBlank
    private String domain;
    /**
     * Component name, it can not be blank
     */
    @NotBlank
    private String name;

    /**
     * Build a new ComponentKey via get component request
     *
     * @param request get component request
     * @return a new ComponentKey
     */
    public static ComponentKey toComponentKey(GetComponentRequest request) {
        return new ComponentKey(request.getApp(), request.getDomain(), request.getName());
    }

    /**
     * Batch build a new ComponentKey via get component request
     *
     * @param requests get component request list
     * @return ComponentKey list
     */
    public static List<ComponentKey> toComponentKeyList(List<GetComponentRequest> requests) {
        return requests.stream().map(GetComponentRequest::toComponentKey).collect(Collectors.toList());
    }
}
