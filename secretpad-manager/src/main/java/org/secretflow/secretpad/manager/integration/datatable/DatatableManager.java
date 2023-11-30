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

package org.secretflow.secretpad.manager.integration.datatable;

import org.secretflow.secretpad.common.errorcode.DatatableErrorCode;
import org.secretflow.secretpad.common.errorcode.SystemErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.manager.integration.model.DatatableDTO;
import org.secretflow.secretpad.manager.integration.model.DatatableListDTO;

import org.secretflow.v1alpha1.kusciaapi.DomainDataServiceGrpc;
import org.secretflow.v1alpha1.kusciaapi.Domaindata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.secretflow.secretpad.manager.integration.model.Constants.STATUS_AVAILABLE;
import static org.secretflow.secretpad.manager.integration.model.Constants.STATUS_UNAVAILABLE;

/**
 * Manager datatable operation
 *
 * @author yansi
 * @date 2023/5/23
 */
public class DatatableManager extends AbstractDatatableManager {

    private final static Logger LOGGER = LoggerFactory.getLogger(DatatableManager.class);

    /**
     * Domain data service blocking stub
     */
    private final DomainDataServiceGrpc.DomainDataServiceBlockingStub dataStub;

    public DatatableManager(DomainDataServiceGrpc.DomainDataServiceBlockingStub dataStub) {
        this.dataStub = dataStub;
    }

    @Override
    public Optional<DatatableDTO> findById(DatatableDTO.NodeDatatableId nodeDatatableId) {
        Domaindata.QueryDomainDataResponse response = dataStub.queryDomainData(
                Domaindata.QueryDomainDataRequest.newBuilder()
                        .setData(Domaindata.QueryDomainDataRequestData.newBuilder()
                                .setDomainId(nodeDatatableId.getNodeId())
                                .setDomaindataId(nodeDatatableId.getDatatableId())
                                .build())
                        .build());
        if (response.getStatus().getCode() != 0) {
            LOGGER.error("lock up from kusciaapi failed: code={}, message={}, request={}",
                    response.getStatus().getCode(), response.getStatus().getMessage(), JsonUtils.toJSONString(nodeDatatableId));
            throw SecretpadException.of(DatatableErrorCode.QUERY_DATATABLE_FAILED);
        }
        return Optional.of(DatatableDTO.fromDomainData(response.getData()));
    }

    @Override
    public Map<DatatableDTO.NodeDatatableId, DatatableDTO> findByIds(List<DatatableDTO.NodeDatatableId> nodeDatatableIds) {
        Domaindata.BatchQueryDomainDataResponse responses = dataStub.batchQueryDomainData(
                Domaindata.BatchQueryDomainDataRequest.newBuilder()
                        .addAllData(nodeDatatableIds.stream().map(
                                it -> Domaindata.QueryDomainDataRequestData.newBuilder()
                                        .setDomainId(it.getNodeId()).setDomaindataId(it.getDatatableId()).build()).collect(Collectors.toList()))
                        .build());
        if (responses.getStatus().getCode() != 0) {
            LOGGER.error("lock up from kusciaapi failed: code={}, message={}, request={}",
                    responses.getStatus().getCode(), responses.getStatus().getMessage(), JsonUtils.toJSONString(nodeDatatableIds));
            throw SecretpadException.of(DatatableErrorCode.QUERY_DATATABLE_FAILED);
        }
        Map<DatatableDTO.NodeDatatableId, DatatableDTO> result = responses.getData().getDomaindataListList().stream().map(DatatableDTO::fromDomainData)
                .collect(Collectors.toMap(it -> DatatableDTO.NodeDatatableId.from(it.getNodeId(), it.getDatatableId()), Function.identity()));
        LOGGER.info("request table size={}, and response table size={}", nodeDatatableIds.size(), result.size());
        return result;
    }

    @Override
    public DatatableListDTO findByNodeId(
            String nodeId,
            Integer pageSize,
            Integer pageNumber,
            String statusFilter,
            String datatableNameFilter
    ) {
        LOGGER.info("Find datatable with kuscia api with node id = {}, filter by usermanul vendor.", nodeId);
        List<DatatableDTO> datatableDTOList = findByNodeId(nodeId, DATA_VENDOR_MANUAL);
        LOGGER.info("The datatable list len = {}, now filter by status = {}", datatableDTOList.size(), statusFilter);
        datatableDTOList = filterByStatus(datatableDTOList, statusFilter);
        LOGGER.info("After filter by status the datatable list len = {}, now filter by datatable name = {}", datatableDTOList.size(), datatableNameFilter);
        datatableDTOList = filterByDatatableName(datatableDTOList, datatableNameFilter);
        LOGGER.info("After filter by name, the datatable list len = {}, now paging.", datatableDTOList.size());
        int startIndex = pageSize * (pageNumber - 1);
        if (startIndex > datatableDTOList.size()) {
            LOGGER.error("When find by node id, the page start index {} > datatableDtolist len {}", startIndex, datatableDTOList.size());
            throw SecretpadException.of(SystemErrorCode.OUT_OF_RANGE_ERROR, "page start index > datatable list length.");
        }
        int endIndex = Math.min((startIndex + pageSize), datatableDTOList.size());
        LOGGER.info("After page, we show from {} to {}", startIndex, endIndex);
        return DatatableListDTO.builder()
                .datatableDTOList(datatableDTOList.subList(startIndex, endIndex))
                .totalDatatableNums(datatableDTOList.size())
                .build();
    }

    @Override
    public List<DatatableDTO> findByNodeId(String nodeId, @Nullable String vendor) {
        Domaindata.ListDomainDataRequestData.Builder builder = Domaindata.ListDomainDataRequestData.newBuilder()
                .setDomaindataType(DATA_TYPE_TABLE)
                .setDomainId(nodeId);
        if (vendor != null) {
            builder.setDomaindataVendor(vendor);
        }
        Domaindata.ListDomainDataResponse responses = dataStub.listDomainData(
                Domaindata.ListDomainDataRequest.newBuilder()
                        .setData(builder.build()).build());
        if (responses.getStatus().getCode() != 0) {
            LOGGER.error("lock up from kusciaapi failed: code={}, message={}, nodeId={}, vendor={}",
                    responses.getStatus().getCode(), responses.getStatus().getMessage(), nodeId, vendor);
            throw SecretpadException.of(DatatableErrorCode.QUERY_DATATABLE_FAILED);
        }
        return responses.getData().getDomaindataListList().stream().map(DatatableDTO::fromDomainData).collect(Collectors.toList());
    }

    @Override
    public void deleteDataTable(DatatableDTO.NodeDatatableId nodeDatatableId) {
        Domaindata.DeleteDomainDataRequest.Builder builder = Domaindata.DeleteDomainDataRequest.newBuilder()
                .setDomainId(nodeDatatableId.getNodeId())
                .setDomaindataId(nodeDatatableId.getDatatableId());
        Domaindata.DeleteDomainDataResponse response = dataStub.deleteDomainData(builder.build());
        if (response.getStatus().getCode() != 0) {
            LOGGER.error("delete datatable failed: code={}, message={}, nodeId={}, datatableId={}",
                    response.getStatus().getCode(), response.getStatus().getMessage(), nodeDatatableId.getNodeId(), nodeDatatableId.getDatatableId());
            throw SecretpadException.of(DatatableErrorCode.DELETE_DATATABLE_FAILED);
        }
    }

    /**
     * Filter datatableDTO list by status
     *
     * @param datatableDTOList
     * @param statusFilter
     * @return DatatableDTO list
     */
    private List<DatatableDTO> filterByStatus(List<DatatableDTO> datatableDTOList, String statusFilter) {
        if ((!STATUS_AVAILABLE.equalsIgnoreCase(statusFilter) && !STATUS_UNAVAILABLE.equalsIgnoreCase(statusFilter))) {
            return datatableDTOList;
        }
        return datatableDTOList.stream().filter(
                it -> statusFilter.equalsIgnoreCase(it.getStatus())
        ).collect(Collectors.toList());
    }

    /**
     * Filter datatableDTO list by datatable name
     *
     * @param datatableDTOList
     * @param datatableNameFilter
     * @return DatatableDTO list
     */
    private List<DatatableDTO> filterByDatatableName(List<DatatableDTO> datatableDTOList, String datatableNameFilter) {
        if (datatableNameFilter == null) {
            return datatableDTOList;
        }
        return datatableDTOList.stream().filter(
                it -> it.getDatatableName().contains(datatableNameFilter)
        ).collect(Collectors.toList());
    }

}

