/*
 * Copyright 2024 Ant Group Co., Ltd.
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

package org.secretflow.secretpad.persistence.entity;

import org.secretflow.secretpad.persistence.converter.BaseObjectListJsonConverter;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * @author chenmingliang
 * @date 2024/01/18
 */
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "project_model_serving")
public class ProjectModelServingDO extends BaseAggregationRoot<ProjectModelServingDO> {

    @Id
    @Column(name = "serving_id", nullable = false, length = 64)
    private String servingId;

    @Column(name = "initiator", nullable = false, length = 64)
    private String initiator;

    @Column(name = "serving_input_config")
    private String servingInputConfig;

    @Column(name = "party_endpoints")
    @Convert(converter = PartyEndpointsConverter.class)
    private List<PartyEndpoints> partyEndpoints;

    @Column(name = "parties")
    private String parties;

    @Column(name = "serving_stats", nullable = false, length = 16)
    private String servingStats;

    @Column(name = "error_msg")
    private String errorMsg;

    @Column(name = "project_id", nullable = false, length = 64)
    private String projectId;


    @Getter
    @Setter
    @ToString
    public static class PartyEndpoints {

        private String nodeId;

        private String endpoints;
    }

    @Converter
    public static class PartyEndpointsConverter extends BaseObjectListJsonConverter<PartyEndpoints> {
        public PartyEndpointsConverter() {
            super(PartyEndpoints.class);
        }
    }

}
