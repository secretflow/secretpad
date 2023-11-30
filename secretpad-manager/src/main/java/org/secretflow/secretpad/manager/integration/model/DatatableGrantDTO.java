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

package org.secretflow.secretpad.manager.integration.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.secretflow.v1alpha1.kusciaapi.Domaindatagrant;

import java.util.Map;

/**
 * Datatable grant data transfer object
 *
 * @author xujiening
 * @date 2023/09/18
 */
@Getter
@Setter
@Builder
public class DatatableGrantDTO {

    /**
     * Domain data grant id
     */
    private String domainDataGrantId;

    /**
     * Grant author
     */
    private String author;

    /**
     * Domain data id
     */
    private String domainDataId;

    /**
     * Grant domain
     */
    private String grantDomain;

    /**
     * Datatable grant limit data transfer object
     */
    private DatatableGrantLimitDTO limitDTO;

    /**
     * description
     */
    private Map<String, String> description;

    /**
     * signature
     */
    private String signature;

    /**
     * Domain id
     */
    private String domainId;

    /**
     * Convert datatable grant data transfer object from domain data grant data
     *
     * @param domainDataGrant domain data grant data
     * @return datatable grant data transfer object
     */
    public static DatatableGrantDTO fromDomainDataGrant(Domaindatagrant.DomainDataGrantData domainDataGrant) {
        return DatatableGrantDTO.builder().domainDataGrantId(domainDataGrant.getDomaindatagrantId())
                .author(domainDataGrant.getAuthor())
                .domainDataId(domainDataGrant.getDomaindataId())
                .grantDomain(domainDataGrant.getGrantDomain())
                .description(domainDataGrant.getDescriptionMap())
                .signature(domainDataGrant.getSignature())
                .domainId(domainDataGrant.getDomainId()).build();
    }
}
