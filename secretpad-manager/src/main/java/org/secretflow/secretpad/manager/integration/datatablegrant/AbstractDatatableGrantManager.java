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

import org.secretflow.secretpad.manager.integration.model.DatatableGrantDTO;

import java.util.List;

/**
 * Manager datatable operation abstract class
 *
 * @author xujiening
 * @date 2023/9/18
 */
public abstract class AbstractDatatableGrantManager {

    /**
     * Query domain data grant by nodeId and domainDataGrantId
     *
     * @param nodeId            target nodeId
     * @param domainDataGrantId target domain data grant id
     * @return
     */
    public abstract DatatableGrantDTO queryDomainGrant(String nodeId, String domainDataGrantId);

    /**
     * Batch query domain data grant
     *
     * @param nodeId
     * @param domainDataGrantIds
     * @return
     */
    public abstract Object batchQueryDomainGrant(String nodeId, List<String> domainDataGrantIds);

    /**
     * Create domain grant
     *
     * @param nodeId            target nodeId
     * @param grantNodeId       target grant node id
     * @param domainDataId      target domain data id
     * @param domainDataGrantId target domain data grant id
     * @return domain data grant id
     */
    public abstract String createDomainGrant(String nodeId, String grantNodeId, String domainDataId, String domainDataGrantId);
}
