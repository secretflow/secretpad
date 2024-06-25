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

package org.secretflow.secretpad.manager.integration.model;

import lombok.*;
import org.secretflow.v1alpha1.kusciaapi.Domaindatasource;

/**
 * @author lufeng
 * @date 2024/5/23
 */
@Data
@Builder
public class DatasourceDTO {
    /**
     * Node id
     */
    private String nodeId;

    /**
     * Datasource id
     */
    private String datasourceId;

    private String datasourceName;

    /**
     * Datasource type
     */
    private String type;

    private OssDataSourceInfoDTO ossDataSourceInfoDTO;

    public static DatasourceDTO fromDomainDatasource(Domaindatasource.DomainDataSource domainDataSource) {
        return DatasourceDTO.builder()
                .nodeId(domainDataSource.getDomainId())
                .datasourceId(domainDataSource.getDatasourceId())
                .datasourceName(domainDataSource.getName())
                .ossDataSourceInfoDTO(OssDataSourceInfoDTO.builder()
                        .endpoint(domainDataSource.getInfo().getOss().getEndpoint())
                        .accessKeyId(domainDataSource.getInfo().getOss().getAccessKeyId())
                        .secretAccessKey(domainDataSource.getInfo().getOss().getAccessKeySecret())
                        .bucket(domainDataSource.getInfo().getOss().getBucket())
                        .prefix(domainDataSource.getInfo().getOss().getPrefix())
                        .build())
                .type(domainDataSource.getType())
                .build();
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OssDataSourceInfoDTO {

        /**
         * OSS endpoint
         */
        private String endpoint;
        /**
         * OSS access key
         */
        private String accessKeyId;
        /**
         * OSS secret access key
         */
        private String secretAccessKey;
        /**
         * OSS bucket
         */
        private String bucket;
        /**
         * OSS prefix
         */
        private String prefix;
        /**
         * OSS virtual host
         */
        private String virtualHost;


    }

    @Setter
    @Getter
    @EqualsAndHashCode
    @ToString
    public static class NodeDatasourceId {

        /**
         * Node id
         */
        private String nodeId;

        /**
         * Datasource id
         */
        private String datasourceId;

        private NodeDatasourceId(String nodeId, String datasourceId) {
            this.nodeId = nodeId;
            this.datasourceId = datasourceId;
        }


        /**
         * Create a new NodeDatatableId class by nodeId and datatableId
         *
         * @param nodeId
         * @param datasourceId
         * @return new NodeDatatableId class
         */
        public static NodeDatasourceId from(String nodeId, String datasourceId) {
            return new NodeDatasourceId(nodeId, datasourceId);
        }

    }


}
