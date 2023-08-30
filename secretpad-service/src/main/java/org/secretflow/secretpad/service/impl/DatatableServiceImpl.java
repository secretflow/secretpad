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

package org.secretflow.secretpad.service.impl;

import org.secretflow.secretpad.common.errorcode.DatatableErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.manager.integration.datatable.AbstractDatatableManager;
import org.secretflow.secretpad.manager.integration.model.DatatableDTO;
import org.secretflow.secretpad.manager.integration.model.DatatableListDTO;
import org.secretflow.secretpad.persistence.entity.ProjectDO;
import org.secretflow.secretpad.persistence.entity.ProjectDatatableDO;
import org.secretflow.secretpad.persistence.repository.ProjectDatatableRepository;
import org.secretflow.secretpad.persistence.repository.ProjectRepository;
import org.secretflow.secretpad.service.DatatableService;
import org.secretflow.secretpad.service.model.datatable.*;

import com.google.common.collect.Lists;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Datatable service implementation class
 *
 * @author xiaonan
 * @date 2023/6/7
 */
@Service
public class DatatableServiceImpl implements DatatableService {

    private final static Logger LOGGER = LoggerFactory.getLogger(DatatableServiceImpl.class);

    @Autowired
    private AbstractDatatableManager datatableManager;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectDatatableRepository projectDatatableRepository;

    @Override
    public DatatableListVO listDatatablesByNodeId(ListDatatableRequest request) {
        LOGGER.info("List data table by nodeId = {}", request.getNodeId());
        DatatableListDTO dataTableListDTO = datatableManager.findByNodeId(
                request.getNodeId(), request.getPageSize(), request.getPageNumber(), request.getStatusFilter(), request.getDatatableNameFilter()
        );
        LOGGER.info("Try get a map with datatableId: DatatableDTO");
        Map<Object, DatatableDTO> datatables = dataTableListDTO.getDatatableDTOList()
                .stream().collect(Collectors.toMap(DatatableDTO::getDatatableId, Function.identity()));
        LOGGER.info("Try get auth project pairs with Map<DatatableID, List<Pair<ProjectDatatableDO, ProjectDO>>>");
        Map<String, List<Pair<ProjectDatatableDO, ProjectDO>>> datatableAuthPairs = getAuthProjectPairs(
                request.getNodeId(),
                Lists.newArrayList(datatables.values().stream().map(DatatableDTO::getDatatableId).collect(Collectors.toList()))
        );
        LOGGER.info("get datatable VO list from datatableListDTO and with datatabl auth pairs.");
        List<DatatableVO> datatableVOList = dataTableListDTO.getDatatableDTOList().stream().map(
                it -> {
                    List<Pair<ProjectDatatableDO, ProjectDO>> pairs = datatableAuthPairs.get(it.getDatatableId());
                    List<AuthProjectVO> authProjectVOList = null;
                    if (pairs != null) {
                        authProjectVOList = AuthProjectVO.fromPairs(pairs);
                    }
                    DatatableDTO datatableDTO = datatables.get(it.getDatatableId());
                    return DatatableVO.from(datatableDTO, authProjectVOList);
                }
        ).collect(Collectors.toList());

        return DatatableListVO.builder()
                .datatableVOList(datatableVOList)
                .totalDatatableNums(dataTableListDTO.getTotalDatatableNums())
                .build();
    }

    @Override
    public DatatableVO getDatatable(GetDatatableRequest request) {
        LOGGER.info("Get datatable detail with nodeID = {}, datatable id = {}", request.getNodeId(), request.getDatatableId());
        Optional<DatatableDTO> datatableOpt = datatableManager.findById(DatatableDTO.NodeDatatableId.from(request.getNodeId(), request.getDatatableId()));
        if (datatableOpt.isEmpty()) {
            LOGGER.error("Datatable not exists when get datatable detail.");
            throw SecretpadException.of(DatatableErrorCode.DATATABLE_NOT_EXISTS);
        }
        DatatableDTO dto = datatableOpt.get();

        Map<String, List<Pair<ProjectDatatableDO, ProjectDO>>> datatableAuthPairs =
                getAuthProjectPairs(request.getNodeId(), Lists.newArrayList(dto.getDatatableId()));
        return DatatableVO.from(dto, datatableAuthPairs.containsKey(dto.getDatatableId()) ?
                AuthProjectVO.fromPairs(datatableAuthPairs.get(dto.getDatatableId())) : null);
    }

    @Override
    public void deleteDatatable(DeleteDatatableRequest request) {
        LOGGER.info("Delete datatable with node id = {}, datatable id = {}", request.getNodeId(), request.getDatatableId());
        Map<String, List<Pair<ProjectDatatableDO, ProjectDO>>> authProjectPairs = getAuthProjectPairs(request.getNodeId(), Arrays.asList(request.getDatatableId()));
        if (!CollectionUtils.isEmpty(authProjectPairs)) {
            throw SecretpadException.of(DatatableErrorCode.DATATABLE_DUPLICATED_AUTHORIZED);
        }
        datatableManager.deleteDataTable(DatatableDTO.NodeDatatableId.from(request.getNodeId(), request.getDatatableId()));
    }

    /**
     * Query auth project pairs by nodeId and datatableIds then collect to Map
     *
     * @param nodeId       target nodeId
     * @param datatableIds target datatableId list
     * @return Map of datatableId and auth project pair list
     */
    private Map<String, List<Pair<ProjectDatatableDO, ProjectDO>>> getAuthProjectPairs(String nodeId, List<String> datatableIds) {
        List<ProjectDatatableDO> authProjectDatatables = projectDatatableRepository.authProjectDatatablesByDatatableIds(nodeId,
                datatableIds);
        List<String> projectIds = authProjectDatatables.stream().map(it -> it.getUpk().getProjectId()).collect(Collectors.toList());
        Map<String, ProjectDO> projectMap = projectRepository.findAllById(projectIds).stream().collect(
                Collectors.toMap(ProjectDO::getProjectId, Function.identity()));
        return authProjectDatatables.stream().map(
                        // List<Pair>
                        it -> new Pair<>(it, projectMap.getOrDefault(it.getUpk().getProjectId(), null)))
                .filter(it -> it.getValue1() != null)
                // Map<datatable, List<Pair>>
                .collect(Collectors.groupingBy(it -> it.getValue0().getUpk().getDatatableId()));
    }


}
