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

    /**
     * OSS data source info dto
     */
    private OssDataSourceInfoDTO ossDataSourceInfoDTO;

    /**
     * Database data source info
     */
    private DatabaseDataSourceInfo databaseDataSourceInfo;

    /**
     * odps data source info dto
     */
    private OdpsDataSourceInfoDTO odpsDataSourceInfo;


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
    public static DatasourceDTO fromOssDomainDatasource(Domaindatasource.DomainDataSource domainDataSource) {
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

    /**
     * A data transfer object (DTO) for storing information about an ODPS (Alibaba Cloud MaxCompute) data source.
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OdpsDataSourceInfoDTO {


        /**
         * The access ID for the ODPS data source.
         */
        private String accessId;
        /**
         * The access key for the ODPS data source.
         */
        private String accessKey;
        /**
         * The project name for the ODPS data source.
         */
        private String project;
        /**
         * The endpoint for the ODPS data source.
         */
        private String endpoint;

    }
    public static DatasourceDTO fromOdpsDomainDatasource(Domaindatasource.DomainDataSource domainDataSource) {
        return DatasourceDTO.builder()
                .nodeId(domainDataSource.getDomainId())
                .datasourceId(domainDataSource.getDatasourceId())
                .datasourceName(domainDataSource.getName())
                .odpsDataSourceInfo(OdpsDataSourceInfoDTO.builder()
                        .accessId(domainDataSource.getInfo().getOdps().getAccessKeyId())
                        .accessKey(domainDataSource.getInfo().getOdps().getAccessKeySecret())
                        .project(domainDataSource.getInfo().getOdps().getProject())
                        .endpoint(domainDataSource.getInfo().getOdps().getEndpoint())
                        .build())
                .type(domainDataSource.getType())
                .build();
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DatabaseDataSourceInfo {

        /**
         * The name of the database.
         */
        private String database;
        /**
         * The username used to connect to the database.
         */
        private String user;
        /**
         * The password used to connect to the database.
         */
        private String password;
        /**
         * The endpoint of the database server.
         */
        private String endpoint;
    }
    public static DatasourceDTO fromDatabaseDomainDatasource(Domaindatasource.DomainDataSource domainDataSource) {
        return DatasourceDTO.builder()
                .nodeId(domainDataSource.getDomainId())
                .datasourceId(domainDataSource.getDatasourceId())
                .datasourceName(domainDataSource.getName())
                .databaseDataSourceInfo(DatabaseDataSourceInfo.builder()
                        .database(domainDataSource.getInfo().getDatabase().getDatabase())
                        .user(domainDataSource.getInfo().getDatabase().getUser())
                        .password(domainDataSource.getInfo().getDatabase().getPassword())
                        .endpoint(domainDataSource.getInfo().getDatabase().getEndpoint())
                        .build())
                .type(domainDataSource.getType())
                .build();
    }

    @Setter
    @Getter
    @EqualsAndHashCode
    @ToString
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NodeDatasourceId {

        /**
         * Node id
         */
        private String nodeId;

        /**
         * Datasource id
         */
        private String datasourceId;

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
