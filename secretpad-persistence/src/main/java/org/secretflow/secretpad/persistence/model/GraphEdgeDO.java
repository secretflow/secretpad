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

package org.secretflow.secretpad.persistence.model;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * Graph edge data object
 *
 * @author yansi
 * @date 2023/5/30
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class GraphEdgeDO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * Edge id
     */
    private String edgeId;
    /**
     * Graph edge source attribute
     */
    private String source;
    /**
     * Graph edge sourceAnchor attribute
     */
    private String sourceAnchor;
    /**
     * Graph edge target attribute
     */
    private String target;
    /**
     * Graph edge targetAnchor attribute
     */
    private String targetAnchor;
}
