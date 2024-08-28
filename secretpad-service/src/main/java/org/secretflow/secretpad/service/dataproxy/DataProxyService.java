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

package org.secretflow.secretpad.service.dataproxy;

import org.secretflow.secretpad.common.constant.DomainConstants;
import org.secretflow.secretpad.kuscia.v1alpha1.constant.KusciaAPIConstants;
import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.persistence.entity.NodeDO;
import org.secretflow.secretpad.persistence.repository.NodeRepository;

import jakarta.annotation.Resource;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.secretflow.v1alpha1.kusciaapi.Domaindatasource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yutu
 * @date 2024/08/01
 */
@Slf4j
@Service
public class DataProxyService {

    @Resource
    @Setter
    private KusciaGrpcClientAdapter kusciaGrpcClientAdapter;

    @Value("${secretpad.node-id}")
    @Setter
    private String localNodeId;

    @Value("${secretpad.data-proxy.enabled}")
    @Setter
    private boolean dataProxyEnabled;

    @Resource
    @Setter
    private NodeRepository nodeRepository;

    public void updateDataSourceUseDataProxyInMaster() {
        if (dataProxyEnabled) {
            List<NodeDO> all = nodeRepository.findAll();
            log.info("all: {}", all);
            List<NodeDO> nodeDOS = new ArrayList<>();
            for (NodeDO nodeDO : all) {
                if (nodeDO.getNodeId().equals(DomainConstants.DomainEmbeddedNodeEnum.alice.name()) || nodeDO.getNodeId().equals(DomainConstants.DomainEmbeddedNodeEnum.bob.name())) {
                    nodeDOS.add(nodeDO);
                }
            }
            nodeDOS.forEach(nodeDO -> updateDataSourceUseDataProxyByDomainId(nodeDO.getNodeId(), nodeDO.getNodeId()));
        }
    }

    public void updateDataSourceUseDataProxyInP2p(String instId) {
        if (dataProxyEnabled) {
            List<NodeDO> nodeDOList = nodeRepository.findByInstId(instId);
            nodeDOList.forEach(nodeDO -> updateDataSourceUseDataProxyByDomainId(nodeDO.getNodeId(), nodeDO.getNodeId()));
        }
    }

    public void updateDataSourceUseDataProxyByDomainId(String domainId, String executionNodeId) {
        Domaindatasource.ListDomainDataSourceResponse listDomainDataSourceResponse =
                kusciaGrpcClientAdapter.listDomainDataSource(buildListDomainDataSourceRequest(domainId), executionNodeId);
        log.info("listDomainDataSourceResponse: {}", listDomainDataSourceResponse);
        if (listDomainDataSourceResponse.getStatus().getCode() == KusciaAPIConstants.OK) {
            Domaindatasource.DomainDataSourceList data = listDomainDataSourceResponse.getData();
            if (ObjectUtils.isNotEmpty(data) && ObjectUtils.isNotEmpty(data.getDatasourceListList())) {
                List<Domaindatasource.DomainDataSource> datasourceListList = data.getDatasourceListList();
                for (Domaindatasource.DomainDataSource domainDataSource : datasourceListList) {
                    if (domainDataSource.getAccessDirectly()) {
                        Domaindatasource.UpdateDomainDataSourceResponse updateResponse = kusciaGrpcClientAdapter.updateDomainDataSource(buildUpdateDomainDataSourceRequest(domainDataSource), executionNodeId);
                        log.info("updateDataSourceUseDataProxyByDomainId response: {}", updateResponse);
                    }
                }
            }
        }
    }


    private Domaindatasource.ListDomainDataSourceRequest buildListDomainDataSourceRequest(String domainId) {
        return Domaindatasource.ListDomainDataSourceRequest.newBuilder()
                .setDomainId(domainId)
                .build();
    }

    public Domaindatasource.UpdateDomainDataSourceRequest buildUpdateDomainDataSourceRequest(Domaindatasource.DomainDataSource domainDataSource) {
        return Domaindatasource.UpdateDomainDataSourceRequest.newBuilder()
                .setDomainId(domainDataSource.getDomainId())
                .setDatasourceId(domainDataSource.getDatasourceId())
                .setAccessDirectly(Boolean.FALSE)
                .build();
    }

}