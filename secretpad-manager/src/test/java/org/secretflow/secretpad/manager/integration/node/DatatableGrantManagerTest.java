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

package org.secretflow.secretpad.manager.integration.node;

import org.secretflow.secretpad.common.errorcode.DatatableErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.manager.integration.datatablegrant.DatatableGrantManager;
import org.secretflow.secretpad.manager.integration.model.DatatableGrantDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.Domaindatagrant;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author lufeng
 * @date 2024/8/6
 */
@ExtendWith(MockitoExtension.class)

public class DatatableGrantManagerTest {
    @InjectMocks
    private DatatableGrantManager datatableGrantManager;

    @Mock
    private KusciaGrpcClientAdapter kusciaGrpcClientAdapter;

    /**
     * test create domain grant with AUTONOMY
     */
    @Test
    public void testCreateDomainGrant_NormalCase() {
        String nodeId = "node1";
        String grantNodeId = "grantNode1";
        String domainDataId = "domainData1";
        String domainDataGrantId = "domainGrant1";
        ReflectionTestUtils.setField(datatableGrantManager, "plaformType", "AUTONOMY");
        Domaindatagrant.CreateDomainDataGrantResponse response = Domaindatagrant.CreateDomainDataGrantResponse.newBuilder()
                .setStatus(Common.Status.newBuilder().setCode(0).setMessage("success"))
                .setData(Domaindatagrant.CreateDomainDataGrantResponseData.newBuilder().setDomaindatagrantId(domainDataGrantId))
                .build();
        when(kusciaGrpcClientAdapter.createDomainDataGrant(any(), anyString())).thenReturn(response);

        String result = datatableGrantManager.createDomainGrant(nodeId, grantNodeId, domainDataId, domainDataGrantId);

        assertEquals("domainGrant1", result);
    }
    /**
     * test create domain grant with CENTER
     */
    @Test
    public void testCreateDomainGrant_NormalCase2() {
        String nodeId = "node1";
        String grantNodeId = "grantNode1";
        String domainDataId = "domainData1";
        String domainDataGrantId = "domainGrant1";
        ReflectionTestUtils.setField(datatableGrantManager, "plaformType", "CENTER");
        Domaindatagrant.CreateDomainDataGrantResponse response = Domaindatagrant.CreateDomainDataGrantResponse.newBuilder()
                .setStatus(Common.Status.newBuilder().setCode(0).setMessage("success"))
                .setData(Domaindatagrant.CreateDomainDataGrantResponseData.newBuilder().setDomaindatagrantId(domainDataGrantId))
                .build();
        when(kusciaGrpcClientAdapter.createDomainDataGrant(any())).thenReturn(response);

        String result = datatableGrantManager.createDomainGrant(nodeId, grantNodeId, domainDataId, domainDataGrantId);

        assertEquals("domainGrant1", result);
    }
    /**
     * test query domain grant with AUTONOMY
     */
    @Test
    public void testQueryDomainGrant_Success() {
        String nodeId = "node1";
        String grantNodeId = "grant1";
        ReflectionTestUtils.setField(datatableGrantManager, "plaformType", "AUTONOMY");
        Domaindatagrant.QueryDomainDataGrantResponse response = Domaindatagrant.QueryDomainDataGrantResponse.newBuilder()
                .setStatus(Common.Status.newBuilder().setCode(0).setMessage("Success"))
                .setData(Domaindatagrant.DomainDataGrant.newBuilder().setData(Domaindatagrant.DomainDataGrantData.newBuilder()))
                .build();
        when(kusciaGrpcClientAdapter.queryDomainDataGrant(any(), any())).thenReturn(response);

        DatatableGrantDTO result = datatableGrantManager.queryDomainGrant(nodeId, grantNodeId);

        assertEquals(result.getDomainDataGrantId(), "");
    }
    /**
     * test query domain grant with CENTER
     */
    @Test
    public void testQueryDomainGrant_Success2() {
        String nodeId = "node1";
        String grantNodeId = "grant1";
        ReflectionTestUtils.setField(datatableGrantManager, "plaformType", "CENTER");
        Domaindatagrant.QueryDomainDataGrantResponse response = Domaindatagrant.QueryDomainDataGrantResponse.newBuilder()
                .setStatus(Common.Status.newBuilder().setCode(0).setMessage("Success"))
                .setData(Domaindatagrant.DomainDataGrant.newBuilder().setData(Domaindatagrant.DomainDataGrantData.newBuilder()))
                .build();
        when(kusciaGrpcClientAdapter.queryDomainDataGrant(any())).thenReturn(response);

        DatatableGrantDTO result = datatableGrantManager.queryDomainGrant(nodeId, grantNodeId);

        assertEquals(result.getDomainDataGrantId(), "");
    }
    /**
     * test query domain grant with QUERY_DATATABLE_GRANT_FAILED
     */
    @Test
    public void testQueryDomainGrant_Success3() {
        String nodeId = "node1";
        String grantNodeId = "grant1";
        ReflectionTestUtils.setField(datatableGrantManager, "plaformType", "CENTER");
        Domaindatagrant.QueryDomainDataGrantResponse response = Domaindatagrant.QueryDomainDataGrantResponse.newBuilder()
                .setStatus(Common.Status.newBuilder().setCode(1).setMessage("Success"))
                .setData(Domaindatagrant.DomainDataGrant.newBuilder().setData(Domaindatagrant.DomainDataGrantData.newBuilder()))
                .build();
        when(kusciaGrpcClientAdapter.queryDomainDataGrant(any())).thenReturn(response);

        try {
            datatableGrantManager.queryDomainGrant("node2", "grant2");
        } catch (SecretpadException e) {
            assertEquals(DatatableErrorCode.QUERY_DATATABLE_GRANT_FAILED, e.getErrorCode());
        }
    }

    /**
     * test batch query domain grant
     */
    @Test
    public void testBatchQueryDomainGrant_NormalCase() {
        String nodeId = "node1";
        List<String> domainDataGrantIds = Arrays.asList("grant1", "grant2");

        Domaindatagrant.BatchQueryDomainDataGrantResponse response = Domaindatagrant.BatchQueryDomainDataGrantResponse.newBuilder()
                .addData(Domaindatagrant.DomainDataGrant.newBuilder().setData(Domaindatagrant.DomainDataGrantData.newBuilder().setGrantDomain("data1")))
                .addData(Domaindatagrant.DomainDataGrant.newBuilder().setData(Domaindatagrant.DomainDataGrantData.newBuilder().setGrantDomain("data2")))
                .build();
        List<Domaindatagrant.QueryDomainDataGrantRequestData> batchQueryDataList = new ArrayList<>();
        domainDataGrantIds.forEach(domainDataGrantId -> {
            Domaindatagrant.QueryDomainDataGrantRequestData.Builder builder = Domaindatagrant.QueryDomainDataGrantRequestData.newBuilder()
                    .setDomainId(nodeId).setDomaindatagrantId(domainDataGrantId);
            batchQueryDataList.add(builder.build());
        });
        when(kusciaGrpcClientAdapter.batchQueryDomainDataGrant(Domaindatagrant.BatchQueryDomainDataGrantRequest.newBuilder()
                        .addAllData(batchQueryDataList).build())).thenReturn(response);

        List<DatatableGrantDTO> result = datatableGrantManager.batchQueryDomainGrant(nodeId, domainDataGrantIds);

        assertEquals(2, result.size());
        assertEquals("data1", result.get(0).getGrantDomain());
        assertEquals("data2", result.get(1).getGrantDomain());
    }
    /**
     * test batch query domain grant with domainDataGrantIds is empty
     */
    @Test
    public void testBatchQueryDomainGrant_NormalCase2() {
        String nodeId = "node1";
        List<String> domainDataGrantIds = Arrays.asList();

        List<DatatableGrantDTO> result = datatableGrantManager.batchQueryDomainGrant(nodeId, domainDataGrantIds);

        assertEquals(0, result.size());
    }

    /**
     * test batch query domain grant with DatatableErrorCode.QUERY_DATATABLE_GRANT_FAILED
     */
    @Test
    public void testBatchQueryDomainGrant_NormalCase3() {
        String nodeId = "node1";
        List<String> domainDataGrantIds = Arrays.asList("grant1", "grant2");

        Domaindatagrant.BatchQueryDomainDataGrantResponse response = Domaindatagrant.BatchQueryDomainDataGrantResponse.newBuilder()
                .setStatus(Common.Status.newBuilder().setCode(1).setMessage("Failed"))
                .build();
        List<Domaindatagrant.QueryDomainDataGrantRequestData> batchQueryDataList = new ArrayList<>();
        domainDataGrantIds.forEach(domainDataGrantId -> {
            Domaindatagrant.QueryDomainDataGrantRequestData.Builder builder = Domaindatagrant.QueryDomainDataGrantRequestData.newBuilder()
                    .setDomainId(nodeId).setDomaindatagrantId(domainDataGrantId);
            batchQueryDataList.add(builder.build());
        });
        when(kusciaGrpcClientAdapter.batchQueryDomainDataGrant(Domaindatagrant.BatchQueryDomainDataGrantRequest.newBuilder()
                        .addAllData(batchQueryDataList).build())).thenReturn(response);

        try {
            datatableGrantManager.batchQueryDomainGrant(nodeId, domainDataGrantIds);
        } catch (SecretpadException e) {
            assertEquals(DatatableErrorCode.QUERY_DATATABLE_GRANT_FAILED, e.getErrorCode());
        }
    }

    /**
     * test create domain grant with DatatableErrorCode.CREATE_DATATABLE_GRANT_FAILED
     */
    @Test
    public void testCreateDomainGrant_FailedStatus() {
        String nodeId = "node1";
        String grantNodeId = "grantNode1";
        String domainDataId = "domainData1";
        String domainDataGrantId = "domainDataGrant1";
        ReflectionTestUtils.setField(datatableGrantManager, "plaformType", "AUTONOMY");
        Domaindatagrant.CreateDomainDataGrantResponse response = Domaindatagrant.CreateDomainDataGrantResponse.newBuilder()
                .setStatus(Common.Status.newBuilder().setCode(1).setMessage("failed"))
                .build();
        when(kusciaGrpcClientAdapter.createDomainDataGrant(any(), anyString())).thenReturn(response);

        assertThrows(SecretpadException.class, () -> datatableGrantManager.createDomainGrant(nodeId, grantNodeId, domainDataId, domainDataGrantId));
    }


}
