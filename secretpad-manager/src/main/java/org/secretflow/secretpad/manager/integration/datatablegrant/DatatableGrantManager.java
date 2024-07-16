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

package org.secretflow.secretpad.manager.integration.datatablegrant;

import org.secretflow.secretpad.common.errorcode.DatatableErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.manager.integration.model.DatatableGrantDTO;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.secretflow.v1alpha1.kusciaapi.Domaindatagrant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manager datatable operation class
 *
 * @author xujiening
 * @date 2023/9/18
 */
@Component
@RequiredArgsConstructor
public class DatatableGrantManager extends AbstractDatatableGrantManager {

    private final static Logger LOGGER = LoggerFactory.getLogger(DatatableGrantManager.class);

    private final KusciaGrpcClientAdapter kusciaGrpcClientAdapter;

    @Override
    public DatatableGrantDTO queryDomainGrant(String nodeId, String domainDataGrantId) {
        Domaindatagrant.QueryDomainDataGrantResponse response = kusciaGrpcClientAdapter.queryDomainDataGrant(
                Domaindatagrant.QueryDomainDataGrantRequest.newBuilder().setDomainId(nodeId).setDomaindatagrantId(domainDataGrantId).build());
        if (response.getStatus().getCode() != 0) {
            LOGGER.error("query domain grant from kusciaapi failed: code={}, message={}, nodeId={}, domainDataGrantId={}",
                    response.getStatus().getCode(), response.getStatus().getMessage(), nodeId, domainDataGrantId);
            throw SecretpadException.of(DatatableErrorCode.QUERY_DATATABLE_GRANT_FAILED);
        }
        return DatatableGrantDTO.fromDomainDataGrant(response.getData().getData());
    }

    @Override
    public List<DatatableGrantDTO> batchQueryDomainGrant(String nodeId, List<String> domainDataGrantIds) {
        if (CollectionUtils.isEmpty(domainDataGrantIds)) {
            return Collections.emptyList();
        }
        List<Domaindatagrant.QueryDomainDataGrantRequestData> batchQueryDataList = new ArrayList<>();
        domainDataGrantIds.forEach(domainDataGrantId -> {
            Domaindatagrant.QueryDomainDataGrantRequestData.Builder builder = Domaindatagrant.QueryDomainDataGrantRequestData.newBuilder()
                    .setDomainId(nodeId).setDomaindatagrantId(domainDataGrantId);
            batchQueryDataList.add(builder.build());
        });
        Domaindatagrant.BatchQueryDomainDataGrantResponse response = kusciaGrpcClientAdapter.batchQueryDomainDataGrant(
                Domaindatagrant.BatchQueryDomainDataGrantRequest.newBuilder().addAllData(batchQueryDataList).build());
        if (response.getStatus().getCode() != 0) {
            LOGGER.error("batch query domain grant from kusciaapi failed: code={}, message={}, nodeId={}, domainDataGrantIds={}",
                    response.getStatus().getCode(), response.getStatus().getMessage(), nodeId, domainDataGrantIds);
            throw SecretpadException.of(DatatableErrorCode.QUERY_DATATABLE_GRANT_FAILED);
        }
        return response.getDataList().stream().map(t -> DatatableGrantDTO.fromDomainDataGrant(t.getData())).collect(Collectors.toList());
    }

    @Override
    public String createDomainGrant(String nodeId, String grantNodeId, String domainDataId, String domainDataGrantId) {
        Domaindatagrant.CreateDomainDataGrantRequest.Builder builder = Domaindatagrant.CreateDomainDataGrantRequest.newBuilder()
                .setGrantDomain(grantNodeId).setDomaindataId(domainDataId).setDomainId(nodeId);
        if (StringUtils.isNotBlank(domainDataGrantId)) {
            builder.setDomaindatagrantId(domainDataGrantId);
        }
        Domaindatagrant.CreateDomainDataGrantResponse response = kusciaGrpcClientAdapter.createDomainDataGrant(
                builder.build());
        if (response.getStatus().getCode() != 0) {
            LOGGER.error("create domain grant from kusciaapi failed: code={}, message={}, nodeId={}, grantNodeId={}, domainDataId={}",
                    response.getStatus().getCode(), response.getStatus().getMessage(), nodeId, grantNodeId, domainDataId);
            throw SecretpadException.of(DatatableErrorCode.CREATE_DATATABLE_GRANT_FAILED);
        }
        return response.getData().getDomaindatagrantId();
    }
}
