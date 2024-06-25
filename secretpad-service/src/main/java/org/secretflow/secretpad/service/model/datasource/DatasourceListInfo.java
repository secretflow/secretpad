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

package org.secretflow.secretpad.service.model.datasource;

import org.secretflow.secretpad.common.constant.Constants;

import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.secretflow.v1alpha1.kusciaapi.Domaindatasource;

import java.util.List;

/**
 * @author chenmingliang
 * @date 2024/05/24
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DatasourceListInfo {

    private String nodeId;

    private String datasourceId;

    private String name;

    private String type;

    private String status;

    private List<String> relatedDatas;

    public static DatasourceListInfo from(Domaindatasource.DomainDataSource datasourceInfo) {
        return DatasourceListInfo.builder()
                .nodeId(datasourceInfo.getDomainId())
                .datasourceId(datasourceInfo.getDatasourceId())
                .name(datasourceInfo.getName())
                .type(StringUtils.toRootUpperCase(datasourceInfo.getType()))
                .status(StringUtils.isBlank(datasourceInfo.getStatus()) ? Constants.STATUS_AVAILABLE : datasourceInfo.getStatus())
                .build();
    }

}
