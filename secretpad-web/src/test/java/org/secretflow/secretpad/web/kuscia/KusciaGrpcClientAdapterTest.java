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

package org.secretflow.secretpad.web.kuscia;

import org.secretflow.secretpad.kuscia.v1alpha1.DynamicKusciaChannelProvider;
import org.secretflow.secretpad.kuscia.v1alpha1.mock.MockKusciaGrpcServer;
import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.secretflow.v1alpha1.kusciaapi.*;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.security.cert.CertificateException;

/**
 * @author yutu
 * @date 2024/06/18
 */
@TestPropertySource(properties = {
        "secretpad.node-id=alice",
        "kuscia.nodes="
})
public class KusciaGrpcClientAdapterTest extends BaseKusciaTest {

    @Resource
    private KusciaGrpcClientAdapter kusciaGrpcClientAdapter;
    @Resource
    private DynamicKusciaChannelProvider dynamicKusciaChannelProvider;


    @Test
    public void testKusciaApiService() {
        String xx = dynamicKusciaChannelProvider.getProtocolByDomainId("xx");
        Assertions.assertEquals(xx, "tls");
        xx = dynamicKusciaChannelProvider.getProtocolByDomainId("alice1");
        Assertions.assertEquals(xx, "tls");
        String domainId = "xx";
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.queryDomain(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.queryDomain(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.createDomain(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.createDomain(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.updateDomain(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.updateDomain(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.deleteDomain(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.deleteDomain(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.batchQueryDomain(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.batchQueryDomain(null, domainId));

        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.queryDomain(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.createDomain(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.updateDomain(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.deleteDomain(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.batchQueryDomain(null, domainId));


        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.createDomainRoute(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.deleteDomainRoute(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.queryDomainRoute(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.batchQueryDomainRouteStatus(null));

        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.createDomainRoute(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.deleteDomainRoute(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.queryDomainRoute(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.batchQueryDomainRouteStatus(null, domainId));

        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.createDomainData(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.updateDomainData(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.deleteDomainData(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.queryDomainData(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.batchQueryDomainData(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.listDomainData(null));

        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.createDomainData(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.updateDomainData(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.deleteDomainData(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.queryDomainData(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.batchQueryDomainData(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.listDomainData(null, domainId));


        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.createDomainDataSource(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.updateDomainDataSource(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.deleteDomainDataSource(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.queryDomainDataSource(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.batchQueryDomainDataSource(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.listDomainDataSource(null));

        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.createDomainDataSource(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.updateDomainDataSource(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.deleteDomainDataSource(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.queryDomainDataSource(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.batchQueryDomainDataSource(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.listDomainDataSource(null, domainId));

        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.createDomainDataGrant(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.updateDomainDataGrant(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.deleteDomainDataGrant(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.queryDomainDataGrant(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.batchQueryDomainDataGrant(null));

        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.createDomainDataGrant(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.updateDomainDataGrant(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.deleteDomainDataGrant(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.queryDomainDataGrant(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.batchQueryDomainDataGrant(null, domainId));

        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.healthZ(null));

        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.healthZ(null, domainId));


        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.createJob(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.queryJob(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.batchQueryJobStatus(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.deleteJob(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.stopJob(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.watchJob(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.approveJob(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.suspendJob(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.restartJob(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.cancelJob(null));

        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.createJob(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.queryJob(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.batchQueryJobStatus(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.deleteJob(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.stopJob(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.watchJob(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.approveJob(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.suspendJob(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.restartJob(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.cancelJob(null, domainId));

        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.createServing(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.updateServing(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.deleteServing(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.queryServing(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.batchQueryServingStatus(null));

        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.createServing(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.updateServing(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.deleteServing(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.queryServing(null, domainId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.batchQueryServingStatus(null, domainId));

        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.generateKeyCerts(null));

        Assertions.assertThrows(IllegalArgumentException.class, () -> kusciaGrpcClientAdapter.generateKeyCerts(null, domainId));
    }

    @Test
    public void testKusciaApiServiceReqNotNull() {

        String domainId = "alice";
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.queryDomain(DomainOuterClass.QueryDomainRequest.newBuilder().build()).getStatus().getCode());

        Assertions.assertEquals(14, kusciaGrpcClientAdapter.createDomain(DomainOuterClass.CreateDomainRequest.newBuilder().build()).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.updateDomain(DomainOuterClass.UpdateDomainRequest.newBuilder().build()).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.deleteDomain(DomainOuterClass.DeleteDomainRequest.newBuilder().build()).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.batchQueryDomain(DomainOuterClass.BatchQueryDomainRequest.newBuilder().build()).getStatus().getCode());

        Assertions.assertEquals(14, kusciaGrpcClientAdapter.queryDomain(DomainOuterClass.QueryDomainRequest.newBuilder().build(), domainId).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.createDomain(DomainOuterClass.CreateDomainRequest.newBuilder().build(), domainId).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.updateDomain(DomainOuterClass.UpdateDomainRequest.newBuilder().build(), domainId).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.deleteDomain(DomainOuterClass.DeleteDomainRequest.newBuilder().build(), domainId).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.batchQueryDomain(DomainOuterClass.BatchQueryDomainRequest.newBuilder().build(), domainId).getStatus().getCode());


        Assertions.assertEquals(14, kusciaGrpcClientAdapter.createDomainRoute(DomainRoute.CreateDomainRouteRequest.newBuilder().build()).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.deleteDomainRoute(DomainRoute.DeleteDomainRouteRequest.newBuilder().build()).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.queryDomainRoute(DomainRoute.QueryDomainRouteRequest.newBuilder().build()).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.batchQueryDomainRouteStatus(DomainRoute.BatchQueryDomainRouteStatusRequest.newBuilder().build()).getStatus().getCode());

        Assertions.assertEquals(14, kusciaGrpcClientAdapter.createDomainRoute(DomainRoute.CreateDomainRouteRequest.newBuilder().build(), domainId).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.deleteDomainRoute(DomainRoute.DeleteDomainRouteRequest.newBuilder().build(), domainId).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.queryDomainRoute(DomainRoute.QueryDomainRouteRequest.newBuilder().build(), domainId).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.batchQueryDomainRouteStatus(DomainRoute.BatchQueryDomainRouteStatusRequest.newBuilder().build(), domainId).getStatus().getCode());

        Assertions.assertEquals(14, kusciaGrpcClientAdapter.createDomainData(Domaindata.CreateDomainDataRequest.newBuilder().build()).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.updateDomainData(Domaindata.UpdateDomainDataRequest.newBuilder().build()).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.deleteDomainData(Domaindata.DeleteDomainDataRequest.newBuilder().build()).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.queryDomainData(Domaindata.QueryDomainDataRequest.newBuilder().build()).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.batchQueryDomainData(Domaindata.BatchQueryDomainDataRequest.newBuilder().build()).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.listDomainData(Domaindata.ListDomainDataRequest.newBuilder().build()).getStatus().getCode());

        Assertions.assertEquals(14, kusciaGrpcClientAdapter.createDomainData(Domaindata.CreateDomainDataRequest.newBuilder().build(), domainId).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.updateDomainData(Domaindata.UpdateDomainDataRequest.newBuilder().build(), domainId).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.deleteDomainData(Domaindata.DeleteDomainDataRequest.newBuilder().build(), domainId).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.queryDomainData(Domaindata.QueryDomainDataRequest.newBuilder().build(), domainId).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.batchQueryDomainData(Domaindata.BatchQueryDomainDataRequest.newBuilder().build(), domainId).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.listDomainData(Domaindata.ListDomainDataRequest.newBuilder().build(), domainId).getStatus().getCode());


        Assertions.assertEquals(14, kusciaGrpcClientAdapter.createDomainDataSource(Domaindatasource.CreateDomainDataSourceRequest.newBuilder().buildPartial()).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.updateDomainDataSource(Domaindatasource.UpdateDomainDataSourceRequest.newBuilder().build()).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.deleteDomainDataSource(Domaindatasource.DeleteDomainDataSourceRequest.newBuilder().build()).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.queryDomainDataSource(Domaindatasource.QueryDomainDataSourceRequest.newBuilder().build()).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.batchQueryDomainDataSource(Domaindatasource.BatchQueryDomainDataSourceRequest.newBuilder().build()).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.listDomainDataSource(Domaindatasource.ListDomainDataSourceRequest.newBuilder().build()).getStatus().getCode());

        Assertions.assertEquals(14, kusciaGrpcClientAdapter.createDomainDataSource(Domaindatasource.CreateDomainDataSourceRequest.newBuilder().build(), domainId).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.updateDomainDataSource(Domaindatasource.UpdateDomainDataSourceRequest.newBuilder().build(), domainId).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.deleteDomainDataSource(Domaindatasource.DeleteDomainDataSourceRequest.newBuilder().build(), domainId).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.queryDomainDataSource(Domaindatasource.QueryDomainDataSourceRequest.newBuilder().build(), domainId).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.batchQueryDomainDataSource(Domaindatasource.BatchQueryDomainDataSourceRequest.newBuilder().build(), domainId).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.listDomainDataSource(Domaindatasource.ListDomainDataSourceRequest.newBuilder().build(), domainId).getStatus().getCode());

        Assertions.assertEquals(14, kusciaGrpcClientAdapter.createDomainDataGrant(Domaindatagrant.CreateDomainDataGrantRequest.newBuilder().build()).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.updateDomainDataGrant(Domaindatagrant.UpdateDomainDataGrantRequest.newBuilder().build()).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.deleteDomainDataGrant(Domaindatagrant.DeleteDomainDataGrantRequest.newBuilder().build()).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.queryDomainDataGrant(Domaindatagrant.QueryDomainDataGrantRequest.newBuilder().build()).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.batchQueryDomainDataGrant(Domaindatagrant.BatchQueryDomainDataGrantRequest.newBuilder().build()).getStatus().getCode());

        Assertions.assertEquals(14, kusciaGrpcClientAdapter.createDomainDataGrant(Domaindatagrant.CreateDomainDataGrantRequest.newBuilder().build(), domainId).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.updateDomainDataGrant(Domaindatagrant.UpdateDomainDataGrantRequest.newBuilder().build(), domainId).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.deleteDomainDataGrant(Domaindatagrant.DeleteDomainDataGrantRequest.newBuilder().build(), domainId).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.queryDomainDataGrant(Domaindatagrant.QueryDomainDataGrantRequest.newBuilder().build(), domainId).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.batchQueryDomainDataGrant(Domaindatagrant.BatchQueryDomainDataGrantRequest.newBuilder().build(), domainId).getStatus().getCode());

        Assertions.assertEquals(14, kusciaGrpcClientAdapter.healthZ(Health.HealthRequest.newBuilder().build()).getStatus().getCode());

        Assertions.assertEquals(14, kusciaGrpcClientAdapter.healthZ(Health.HealthRequest.newBuilder().build(), domainId).getStatus().getCode());


        Assertions.assertEquals(14, kusciaGrpcClientAdapter.createJob(Job.CreateJobRequest.newBuilder().build()).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.queryJob(Job.QueryJobRequest.newBuilder().build()).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.batchQueryJobStatus(Job.BatchQueryJobStatusRequest.newBuilder().build()).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.deleteJob(Job.DeleteJobRequest.newBuilder().build()).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.stopJob(Job.StopJobRequest.newBuilder().build()).getStatus().getCode());
//        Assertions.assertEquals(14,  kusciaApiService.watchJob(Job.WatchJobRequest.newBuilder().build()) .getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.approveJob(Job.ApproveJobRequest.newBuilder().build()).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.suspendJob(Job.SuspendJobRequest.newBuilder().build()).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.restartJob(Job.RestartJobRequest.newBuilder().build()).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.cancelJob(Job.CancelJobRequest.newBuilder().build()).getStatus().getCode());

        Assertions.assertEquals(14, kusciaGrpcClientAdapter.createJob(Job.CreateJobRequest.newBuilder().build(), domainId).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.queryJob(Job.QueryJobRequest.newBuilder().build(), domainId).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.batchQueryJobStatus(Job.BatchQueryJobStatusRequest.newBuilder().build(), domainId).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.deleteJob(Job.DeleteJobRequest.newBuilder().build(), domainId).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.stopJob(Job.StopJobRequest.newBuilder().build(), domainId).getStatus().getCode());
//        Assertions.assertEquals(14,  kusciaApiService.watchJob(Job.WatchJobRequest.newBuilder().build(), domainId) .getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.approveJob(Job.ApproveJobRequest.newBuilder().build(), domainId).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.suspendJob(Job.SuspendJobRequest.newBuilder().build(), domainId).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.restartJob(Job.RestartJobRequest.newBuilder().build(), domainId).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.cancelJob(Job.CancelJobRequest.newBuilder().build(), domainId).getStatus().getCode());

        Assertions.assertEquals(14, kusciaGrpcClientAdapter.createServing(Serving.CreateServingRequest.newBuilder().build()).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.updateServing(Serving.UpdateServingRequest.newBuilder().build()).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.deleteServing(Serving.DeleteServingRequest.newBuilder().build()).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.queryServing(Serving.QueryServingRequest.newBuilder().build()).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.batchQueryServingStatus(Serving.BatchQueryServingStatusRequest.newBuilder().build()).getStatus().getCode());

        Assertions.assertEquals(14, kusciaGrpcClientAdapter.createServing(Serving.CreateServingRequest.newBuilder().build(), domainId).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.updateServing(Serving.UpdateServingRequest.newBuilder().build(), domainId).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.deleteServing(Serving.DeleteServingRequest.newBuilder().build(), domainId).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.queryServing(Serving.QueryServingRequest.newBuilder().build(), domainId).getStatus().getCode());
        Assertions.assertEquals(14, kusciaGrpcClientAdapter.batchQueryServingStatus(Serving.BatchQueryServingStatusRequest.newBuilder().build(), domainId).getStatus().getCode());

        Assertions.assertEquals(14, kusciaGrpcClientAdapter.generateKeyCerts(Certificate.GenerateKeyCertsRequest.newBuilder().build()).getStatus().getCode());

        Assertions.assertEquals(14, kusciaGrpcClientAdapter.generateKeyCerts(Certificate.GenerateKeyCertsRequest.newBuilder().build(), domainId).getStatus().getCode());
    }

    @Test
    void testMockResp() throws IOException, CertificateException, InterruptedException {
        MockKusciaGrpcServer mockKusciaGrpcServer = new MockKusciaGrpcServer();
        mockKusciaGrpcServer.start();
        dynamicKusciaChannelProvider.registerKuscia(mockKusciaGrpcServer.buildKusciaGrpcConfig("alice"));
        dynamicKusciaChannelProvider.registerKuscia(mockKusciaGrpcServer.buildKusciaGrpcConfig("alice"));

        dynamicKusciaChannelProvider.registerKuscia(mockKusciaGrpcServer.buildKusciaGrpcConfig("bob"));
        dynamicKusciaChannelProvider.setNodeId("alice");


        kusciaGrpcClientAdapter.queryDomain(DomainOuterClass.QueryDomainRequest.newBuilder().build());
        kusciaGrpcClientAdapter.createDomain(DomainOuterClass.CreateDomainRequest.newBuilder().build());
        kusciaGrpcClientAdapter.updateDomain(DomainOuterClass.UpdateDomainRequest.newBuilder().build());
        kusciaGrpcClientAdapter.deleteDomain(DomainOuterClass.DeleteDomainRequest.newBuilder().build());
        kusciaGrpcClientAdapter.batchQueryDomain(DomainOuterClass.BatchQueryDomainRequest.newBuilder().build());

        kusciaGrpcClientAdapter.queryDomain(DomainOuterClass.QueryDomainRequest.newBuilder().build(), "alice");
        kusciaGrpcClientAdapter.createDomain(DomainOuterClass.CreateDomainRequest.newBuilder().build(), "alice");
        kusciaGrpcClientAdapter.updateDomain(DomainOuterClass.UpdateDomainRequest.newBuilder().build(), "alice");
        kusciaGrpcClientAdapter.deleteDomain(DomainOuterClass.DeleteDomainRequest.newBuilder().build(), "alice");
        kusciaGrpcClientAdapter.batchQueryDomain(DomainOuterClass.BatchQueryDomainRequest.newBuilder().build(), "alice");

        kusciaGrpcClientAdapter.queryDomainData(Domaindata.QueryDomainDataRequest.newBuilder().build());
        kusciaGrpcClientAdapter.createDomainData(Domaindata.CreateDomainDataRequest.newBuilder().build());
        kusciaGrpcClientAdapter.updateDomainData(Domaindata.UpdateDomainDataRequest.newBuilder().build());
        kusciaGrpcClientAdapter.deleteDomainData(Domaindata.DeleteDomainDataRequest.newBuilder().build());
        kusciaGrpcClientAdapter.batchQueryDomainData(Domaindata.BatchQueryDomainDataRequest.newBuilder().build());
        kusciaGrpcClientAdapter.listDomainData(Domaindata.ListDomainDataRequest.newBuilder().build());

        kusciaGrpcClientAdapter.queryDomainData(Domaindata.QueryDomainDataRequest.newBuilder().build(), "alice");
        kusciaGrpcClientAdapter.createDomainData(Domaindata.CreateDomainDataRequest.newBuilder().build(), "alice");
        kusciaGrpcClientAdapter.updateDomainData(Domaindata.UpdateDomainDataRequest.newBuilder().build(), "alice");
        kusciaGrpcClientAdapter.deleteDomainData(Domaindata.DeleteDomainDataRequest.newBuilder().build(), "alice");
        kusciaGrpcClientAdapter.batchQueryDomainData(Domaindata.BatchQueryDomainDataRequest.newBuilder().build(), "alice");
        kusciaGrpcClientAdapter.listDomainData(Domaindata.ListDomainDataRequest.newBuilder().build(), "alice");

        kusciaGrpcClientAdapter.queryDomainDataGrant(Domaindatagrant.QueryDomainDataGrantRequest.newBuilder().build());
        kusciaGrpcClientAdapter.batchQueryDomainDataGrant(Domaindatagrant.BatchQueryDomainDataGrantRequest.newBuilder().build());
        kusciaGrpcClientAdapter.createDomainDataGrant(Domaindatagrant.CreateDomainDataGrantRequest.newBuilder().build());
        kusciaGrpcClientAdapter.deleteDomainDataGrant(Domaindatagrant.DeleteDomainDataGrantRequest.newBuilder().build());
        kusciaGrpcClientAdapter.updateDomainDataGrant(Domaindatagrant.UpdateDomainDataGrantRequest.newBuilder().build());

        kusciaGrpcClientAdapter.queryDomainDataGrant(Domaindatagrant.QueryDomainDataGrantRequest.newBuilder().build(), "alice");
        kusciaGrpcClientAdapter.batchQueryDomainDataGrant(Domaindatagrant.BatchQueryDomainDataGrantRequest.newBuilder().build(), "alice");
        kusciaGrpcClientAdapter.createDomainDataGrant(Domaindatagrant.CreateDomainDataGrantRequest.newBuilder().build(), "alice");
        kusciaGrpcClientAdapter.deleteDomainDataGrant(Domaindatagrant.DeleteDomainDataGrantRequest.newBuilder().build(), "alice");
        kusciaGrpcClientAdapter.updateDomainDataGrant(Domaindatagrant.UpdateDomainDataGrantRequest.newBuilder().build(), "alice");

        kusciaGrpcClientAdapter.queryDomainDataSource(Domaindatasource.QueryDomainDataSourceRequest.newBuilder().build());
        kusciaGrpcClientAdapter.batchQueryDomainDataSource(Domaindatasource.BatchQueryDomainDataSourceRequest.newBuilder().build());
        kusciaGrpcClientAdapter.createDomainDataSource(Domaindatasource.CreateDomainDataSourceRequest.newBuilder().build());
        kusciaGrpcClientAdapter.deleteDomainDataSource(Domaindatasource.DeleteDomainDataSourceRequest.newBuilder().build());
        kusciaGrpcClientAdapter.updateDomainDataSource(Domaindatasource.UpdateDomainDataSourceRequest.newBuilder().build());
        kusciaGrpcClientAdapter.listDomainDataSource(Domaindatasource.ListDomainDataSourceRequest.newBuilder().build());

        kusciaGrpcClientAdapter.queryDomainDataSource(Domaindatasource.QueryDomainDataSourceRequest.newBuilder().build(), "alice");
        kusciaGrpcClientAdapter.batchQueryDomainDataSource(Domaindatasource.BatchQueryDomainDataSourceRequest.newBuilder().build(), "alice");
        kusciaGrpcClientAdapter.createDomainDataSource(Domaindatasource.CreateDomainDataSourceRequest.newBuilder().build(), "alice");
        kusciaGrpcClientAdapter.deleteDomainDataSource(Domaindatasource.DeleteDomainDataSourceRequest.newBuilder().build(), "alice");
        kusciaGrpcClientAdapter.updateDomainDataSource(Domaindatasource.UpdateDomainDataSourceRequest.newBuilder().build(), "alice");
        kusciaGrpcClientAdapter.listDomainDataSource(Domaindatasource.ListDomainDataSourceRequest.newBuilder().build(), "alice");

        kusciaGrpcClientAdapter.queryDomainRoute(DomainRoute.QueryDomainRouteRequest.newBuilder().build());
        kusciaGrpcClientAdapter.createDomainRoute(DomainRoute.CreateDomainRouteRequest.newBuilder().build());
        kusciaGrpcClientAdapter.deleteDomainRoute(DomainRoute.DeleteDomainRouteRequest.newBuilder().build());
        kusciaGrpcClientAdapter.batchQueryDomainRouteStatus(DomainRoute.BatchQueryDomainRouteStatusRequest.newBuilder().build());

        kusciaGrpcClientAdapter.queryDomainRoute(DomainRoute.QueryDomainRouteRequest.newBuilder().build(), "alice");
        kusciaGrpcClientAdapter.createDomainRoute(DomainRoute.CreateDomainRouteRequest.newBuilder().build(), "alice");
        kusciaGrpcClientAdapter.deleteDomainRoute(DomainRoute.DeleteDomainRouteRequest.newBuilder().build(), "alice");
        kusciaGrpcClientAdapter.batchQueryDomainRouteStatus(DomainRoute.BatchQueryDomainRouteStatusRequest.newBuilder().build(), "alice");

        kusciaGrpcClientAdapter.queryJob(Job.QueryJobRequest.newBuilder().build());
        kusciaGrpcClientAdapter.cancelJob(Job.CancelJobRequest.newBuilder().build());
        kusciaGrpcClientAdapter.stopJob(Job.StopJobRequest.newBuilder().build());
        kusciaGrpcClientAdapter.createJob(Job.CreateJobRequest.newBuilder().build());
        kusciaGrpcClientAdapter.batchQueryJobStatus(Job.BatchQueryJobStatusRequest.newBuilder().build());
        kusciaGrpcClientAdapter.approveJob(Job.ApproveJobRequest.newBuilder().build());
        kusciaGrpcClientAdapter.restartJob(Job.RestartJobRequest.newBuilder().build());
        kusciaGrpcClientAdapter.watchJob(Job.WatchJobRequest.newBuilder().build());
        kusciaGrpcClientAdapter.suspendJob(Job.SuspendJobRequest.newBuilder().build());

        kusciaGrpcClientAdapter.queryJob(Job.QueryJobRequest.newBuilder().build(), "alice");
        kusciaGrpcClientAdapter.cancelJob(Job.CancelJobRequest.newBuilder().build(), "alice");
        kusciaGrpcClientAdapter.stopJob(Job.StopJobRequest.newBuilder().build(), "alice");
        kusciaGrpcClientAdapter.createJob(Job.CreateJobRequest.newBuilder().build(), "alice");
        kusciaGrpcClientAdapter.batchQueryJobStatus(Job.BatchQueryJobStatusRequest.newBuilder().build(), "alice");
        kusciaGrpcClientAdapter.approveJob(Job.ApproveJobRequest.newBuilder().build(), "alice");
        kusciaGrpcClientAdapter.restartJob(Job.RestartJobRequest.newBuilder().build(), "alice");
        kusciaGrpcClientAdapter.watchJob(Job.WatchJobRequest.newBuilder().build(), "alice");
        kusciaGrpcClientAdapter.suspendJob(Job.SuspendJobRequest.newBuilder().build(), "alice");

        kusciaGrpcClientAdapter.queryServing(Serving.QueryServingRequest.newBuilder().build());
        kusciaGrpcClientAdapter.createServing(Serving.CreateServingRequest.newBuilder().build());
        kusciaGrpcClientAdapter.deleteServing(Serving.DeleteServingRequest.newBuilder().build());
        kusciaGrpcClientAdapter.updateServing(Serving.UpdateServingRequest.newBuilder().build());
        kusciaGrpcClientAdapter.batchQueryServingStatus(Serving.BatchQueryServingStatusRequest.newBuilder().build());

        kusciaGrpcClientAdapter.queryServing(Serving.QueryServingRequest.newBuilder().build(), "alice");
        kusciaGrpcClientAdapter.createServing(Serving.CreateServingRequest.newBuilder().build(), "alice");
        kusciaGrpcClientAdapter.deleteServing(Serving.DeleteServingRequest.newBuilder().build(), "alice");
        kusciaGrpcClientAdapter.updateServing(Serving.UpdateServingRequest.newBuilder().build(), "alice");
        kusciaGrpcClientAdapter.batchQueryServingStatus(Serving.BatchQueryServingStatusRequest.newBuilder().build(), "alice");


        kusciaGrpcClientAdapter.healthZ(Health.HealthRequest.newBuilder().build());

        kusciaGrpcClientAdapter.healthZ(Health.HealthRequest.newBuilder().build(), "alice");

        kusciaGrpcClientAdapter.generateKeyCerts(Certificate.GenerateKeyCertsRequest.newBuilder().build());

        kusciaGrpcClientAdapter.generateKeyCerts(Certificate.GenerateKeyCertsRequest.newBuilder().build(), "alice");

        dynamicKusciaChannelProvider.unRegisterKuscia("bob");
        dynamicKusciaChannelProvider.unRegisterKuscia(mockKusciaGrpcServer.buildKusciaGrpcConfig("bob"));
        mockKusciaGrpcServer.shutdown();
        dynamicKusciaChannelProvider.destroy();
    }
}