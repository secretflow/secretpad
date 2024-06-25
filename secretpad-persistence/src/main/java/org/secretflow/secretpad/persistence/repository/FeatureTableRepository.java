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

package org.secretflow.secretpad.persistence.repository;

import org.secretflow.secretpad.persistence.entity.FeatureTableDO;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * @author chenmingliang
 * @date 2024/01/24
 */
@Repository
public interface FeatureTableRepository extends BaseRepository<FeatureTableDO, FeatureTableDO.UPK> {

    @Query("from FeatureTableDO ft where ft.upk.nodeId=:nodeId")
    List<FeatureTableDO> findByNodeId(String nodeId);

    @Query("from FeatureTableDO ft where ft.upk.featureTableId in :featureTableIds")
    List<FeatureTableDO> findByFeatureTableIdIn(@Param("featureTableIds") Collection<String> featureTableIds);

    List<FeatureTableDO> findByUpkDatasourceId(String datasourceId);


}
