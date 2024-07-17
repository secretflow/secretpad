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

package org.secretflow.secretpad.kuscia.v1alpha1.service.impl;

import org.secretflow.secretpad.kuscia.v1alpha1.DynamicKusciaChannelProvider;
import org.secretflow.secretpad.kuscia.v1alpha1.service.*;

import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.secretflow.v1alpha1.kusciaapi.*;
import org.springframework.stereotype.Service;

import java.util.Iterator;

/**
 * kuscia grpc api service
 *
 * @author yutu
 * @date 2024/06/17
 */
@Setter
@Getter
@Slf4j
@Service
public class KusciaGrpcClientAdapter implements
        DomainService, DomainRouteService, DomainDataService, DomainDataSourceService, DomainDataGrantService
        , HealthService, KusciaJobService, ServingService, CertificateService {

    @Resource
    private DynamicKusciaChannelProvider dynamicKusciaChannelProvider;

    @Override
    public Certificate.GenerateKeyCertsResponse generateKeyCerts(Certificate.GenerateKeyCertsRequest request) {
        return dynamicKusciaChannelProvider.currentStub(CertificateServiceGrpc.CertificateServiceBlockingStub.class).generateKeyCerts(request);
    }

    @Override
    public Certificate.GenerateKeyCertsResponse generateKeyCerts(Certificate.GenerateKeyCertsRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, CertificateServiceGrpc.CertificateServiceBlockingStub.class).generateKeyCerts(request);
    }

    @Override
    public Domaindatagrant.CreateDomainDataGrantResponse createDomainDataGrant(Domaindatagrant.CreateDomainDataGrantRequest request) {
        return dynamicKusciaChannelProvider.currentStub(DomainDataGrantServiceGrpc.DomainDataGrantServiceBlockingStub.class).createDomainDataGrant(request);
    }

    @Override
    public Domaindatagrant.UpdateDomainDataGrantResponse updateDomainDataGrant(Domaindatagrant.UpdateDomainDataGrantRequest request) {
        return dynamicKusciaChannelProvider.currentStub(DomainDataGrantServiceGrpc.DomainDataGrantServiceBlockingStub.class).updateDomainDataGrant(request);
    }

    @Override
    public Domaindatagrant.DeleteDomainDataGrantResponse deleteDomainDataGrant(Domaindatagrant.DeleteDomainDataGrantRequest request) {
        return dynamicKusciaChannelProvider.currentStub(DomainDataGrantServiceGrpc.DomainDataGrantServiceBlockingStub.class).deleteDomainDataGrant(request);
    }

    @Override
    public Domaindatagrant.QueryDomainDataGrantResponse queryDomainDataGrant(Domaindatagrant.QueryDomainDataGrantRequest request) {
        return dynamicKusciaChannelProvider.currentStub(DomainDataGrantServiceGrpc.DomainDataGrantServiceBlockingStub.class).queryDomainDataGrant(request);
    }

    @Override
    public Domaindatagrant.BatchQueryDomainDataGrantResponse batchQueryDomainDataGrant(Domaindatagrant.BatchQueryDomainDataGrantRequest request) {
        return dynamicKusciaChannelProvider.currentStub(DomainDataGrantServiceGrpc.DomainDataGrantServiceBlockingStub.class).batchQueryDomainDataGrant(request);
    }

    @Override
    public Domaindatagrant.CreateDomainDataGrantResponse createDomainDataGrant(Domaindatagrant.CreateDomainDataGrantRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, DomainDataGrantServiceGrpc.DomainDataGrantServiceBlockingStub.class).createDomainDataGrant(request);
    }

    @Override
    public Domaindatagrant.UpdateDomainDataGrantResponse updateDomainDataGrant(Domaindatagrant.UpdateDomainDataGrantRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, DomainDataGrantServiceGrpc.DomainDataGrantServiceBlockingStub.class).updateDomainDataGrant(request);
    }

    @Override
    public Domaindatagrant.DeleteDomainDataGrantResponse deleteDomainDataGrant(Domaindatagrant.DeleteDomainDataGrantRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, DomainDataGrantServiceGrpc.DomainDataGrantServiceBlockingStub.class).deleteDomainDataGrant(request);
    }

    @Override
    public Domaindatagrant.QueryDomainDataGrantResponse queryDomainDataGrant(Domaindatagrant.QueryDomainDataGrantRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, DomainDataGrantServiceGrpc.DomainDataGrantServiceBlockingStub.class).queryDomainDataGrant(request);
    }

    @Override
    public Domaindatagrant.BatchQueryDomainDataGrantResponse batchQueryDomainDataGrant(Domaindatagrant.BatchQueryDomainDataGrantRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, DomainDataGrantServiceGrpc.DomainDataGrantServiceBlockingStub.class).batchQueryDomainDataGrant(request);
    }

    @Override
    public Domaindata.CreateDomainDataResponse createDomainData(Domaindata.CreateDomainDataRequest request) {
        return dynamicKusciaChannelProvider.currentStub(DomainDataServiceGrpc.DomainDataServiceBlockingStub.class).createDomainData(request);
    }

    @Override
    public Domaindata.UpdateDomainDataResponse updateDomainData(Domaindata.UpdateDomainDataRequest request) {
        return dynamicKusciaChannelProvider.currentStub(DomainDataServiceGrpc.DomainDataServiceBlockingStub.class).updateDomainData(request);
    }

    @Override
    public Domaindata.DeleteDomainDataResponse deleteDomainData(Domaindata.DeleteDomainDataRequest request) {
        return dynamicKusciaChannelProvider.currentStub(DomainDataServiceGrpc.DomainDataServiceBlockingStub.class).deleteDomainData(request);
    }

    @Override
    public Domaindata.QueryDomainDataResponse queryDomainData(Domaindata.QueryDomainDataRequest request) {
        return dynamicKusciaChannelProvider.currentStub(DomainDataServiceGrpc.DomainDataServiceBlockingStub.class).queryDomainData(request);
    }

    @Override
    public Domaindata.BatchQueryDomainDataResponse batchQueryDomainData(Domaindata.BatchQueryDomainDataRequest request) {
        return dynamicKusciaChannelProvider.currentStub(DomainDataServiceGrpc.DomainDataServiceBlockingStub.class).batchQueryDomainData(request);
    }

    @Override
    public Domaindata.ListDomainDataResponse listDomainData(Domaindata.ListDomainDataRequest request) {
        return dynamicKusciaChannelProvider.currentStub(DomainDataServiceGrpc.DomainDataServiceBlockingStub.class).listDomainData(request);
    }

    @Override
    public Domaindata.CreateDomainDataResponse createDomainData(Domaindata.CreateDomainDataRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, DomainDataServiceGrpc.DomainDataServiceBlockingStub.class).createDomainData(request);
    }

    @Override
    public Domaindata.UpdateDomainDataResponse updateDomainData(Domaindata.UpdateDomainDataRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, DomainDataServiceGrpc.DomainDataServiceBlockingStub.class).updateDomainData(request);
    }

    @Override
    public Domaindata.DeleteDomainDataResponse deleteDomainData(Domaindata.DeleteDomainDataRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, DomainDataServiceGrpc.DomainDataServiceBlockingStub.class).deleteDomainData(request);
    }

    @Override
    public Domaindata.QueryDomainDataResponse queryDomainData(Domaindata.QueryDomainDataRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, DomainDataServiceGrpc.DomainDataServiceBlockingStub.class).queryDomainData(request);
    }

    @Override
    public Domaindata.BatchQueryDomainDataResponse batchQueryDomainData(Domaindata.BatchQueryDomainDataRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, DomainDataServiceGrpc.DomainDataServiceBlockingStub.class).batchQueryDomainData(request);
    }

    @Override
    public Domaindata.ListDomainDataResponse listDomainData(Domaindata.ListDomainDataRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, DomainDataServiceGrpc.DomainDataServiceBlockingStub.class).listDomainData(request);
    }

    @Override
    public Domaindatasource.CreateDomainDataSourceResponse createDomainDataSource(Domaindatasource.CreateDomainDataSourceRequest request) {
        return dynamicKusciaChannelProvider.currentStub(DomainDataSourceServiceGrpc.DomainDataSourceServiceBlockingStub.class).createDomainDataSource(request);
    }

    @Override
    public Domaindatasource.UpdateDomainDataSourceResponse updateDomainDataSource(Domaindatasource.UpdateDomainDataSourceRequest request) {
        return dynamicKusciaChannelProvider.currentStub(DomainDataSourceServiceGrpc.DomainDataSourceServiceBlockingStub.class).updateDomainDataSource(request);
    }

    @Override
    public Domaindatasource.DeleteDomainDataSourceResponse deleteDomainDataSource(Domaindatasource.DeleteDomainDataSourceRequest request) {
        return dynamicKusciaChannelProvider.currentStub(DomainDataSourceServiceGrpc.DomainDataSourceServiceBlockingStub.class).deleteDomainDataSource(request);
    }

    @Override
    public Domaindatasource.QueryDomainDataSourceResponse queryDomainDataSource(Domaindatasource.QueryDomainDataSourceRequest request) {
        return dynamicKusciaChannelProvider.currentStub(DomainDataSourceServiceGrpc.DomainDataSourceServiceBlockingStub.class).queryDomainDataSource(request);
    }

    @Override
    public Domaindatasource.BatchQueryDomainDataSourceResponse batchQueryDomainDataSource(Domaindatasource.BatchQueryDomainDataSourceRequest request) {
        return dynamicKusciaChannelProvider.currentStub(DomainDataSourceServiceGrpc.DomainDataSourceServiceBlockingStub.class).batchQueryDomainDataSource(request);
    }

    @Override
    public Domaindatasource.ListDomainDataSourceResponse listDomainDataSource(Domaindatasource.ListDomainDataSourceRequest request) {
        return dynamicKusciaChannelProvider.currentStub(DomainDataSourceServiceGrpc.DomainDataSourceServiceBlockingStub.class).listDomainDataSource(request);
    }

    @Override
    public Domaindatasource.CreateDomainDataSourceResponse createDomainDataSource(Domaindatasource.CreateDomainDataSourceRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, DomainDataSourceServiceGrpc.DomainDataSourceServiceBlockingStub.class).createDomainDataSource(request);
    }

    @Override
    public Domaindatasource.UpdateDomainDataSourceResponse updateDomainDataSource(Domaindatasource.UpdateDomainDataSourceRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, DomainDataSourceServiceGrpc.DomainDataSourceServiceBlockingStub.class).updateDomainDataSource(request);
    }

    @Override
    public Domaindatasource.DeleteDomainDataSourceResponse deleteDomainDataSource(Domaindatasource.DeleteDomainDataSourceRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, DomainDataSourceServiceGrpc.DomainDataSourceServiceBlockingStub.class).deleteDomainDataSource(request);
    }

    @Override
    public Domaindatasource.QueryDomainDataSourceResponse queryDomainDataSource(Domaindatasource.QueryDomainDataSourceRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, DomainDataSourceServiceGrpc.DomainDataSourceServiceBlockingStub.class).queryDomainDataSource(request);
    }

    @Override
    public Domaindatasource.BatchQueryDomainDataSourceResponse batchQueryDomainDataSource(Domaindatasource.BatchQueryDomainDataSourceRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, DomainDataSourceServiceGrpc.DomainDataSourceServiceBlockingStub.class).batchQueryDomainDataSource(request);
    }

    @Override
    public Domaindatasource.ListDomainDataSourceResponse listDomainDataSource(Domaindatasource.ListDomainDataSourceRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, DomainDataSourceServiceGrpc.DomainDataSourceServiceBlockingStub.class).listDomainDataSource(request);
    }

    @Override
    public DomainRoute.CreateDomainRouteResponse createDomainRoute(DomainRoute.CreateDomainRouteRequest request) {
        return dynamicKusciaChannelProvider.currentStub(DomainRouteServiceGrpc.DomainRouteServiceBlockingStub.class).createDomainRoute(request);
    }

    @Override
    public DomainRoute.DeleteDomainRouteResponse deleteDomainRoute(DomainRoute.DeleteDomainRouteRequest request) {
        return dynamicKusciaChannelProvider.currentStub(DomainRouteServiceGrpc.DomainRouteServiceBlockingStub.class).deleteDomainRoute(request);
    }

    @Override
    public DomainRoute.QueryDomainRouteResponse queryDomainRoute(DomainRoute.QueryDomainRouteRequest request) {
        return dynamicKusciaChannelProvider.currentStub(DomainRouteServiceGrpc.DomainRouteServiceBlockingStub.class).queryDomainRoute(request);
    }

    @Override
    public DomainRoute.BatchQueryDomainRouteStatusResponse batchQueryDomainRouteStatus(DomainRoute.BatchQueryDomainRouteStatusRequest request) {
        return dynamicKusciaChannelProvider.currentStub(DomainRouteServiceGrpc.DomainRouteServiceBlockingStub.class).batchQueryDomainRouteStatus(request);
    }

    @Override
    public DomainRoute.CreateDomainRouteResponse createDomainRoute(DomainRoute.CreateDomainRouteRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, DomainRouteServiceGrpc.DomainRouteServiceBlockingStub.class).createDomainRoute(request);
    }

    @Override
    public DomainRoute.DeleteDomainRouteResponse deleteDomainRoute(DomainRoute.DeleteDomainRouteRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, DomainRouteServiceGrpc.DomainRouteServiceBlockingStub.class).deleteDomainRoute(request);
    }

    @Override
    public DomainRoute.QueryDomainRouteResponse queryDomainRoute(DomainRoute.QueryDomainRouteRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, DomainRouteServiceGrpc.DomainRouteServiceBlockingStub.class).queryDomainRoute(request);
    }

    @Override
    public DomainRoute.BatchQueryDomainRouteStatusResponse batchQueryDomainRouteStatus(DomainRoute.BatchQueryDomainRouteStatusRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, DomainRouteServiceGrpc.DomainRouteServiceBlockingStub.class).batchQueryDomainRouteStatus(request);
    }

    @Override
    public DomainOuterClass.CreateDomainResponse createDomain(DomainOuterClass.CreateDomainRequest request) {
        return dynamicKusciaChannelProvider.currentStub(DomainServiceGrpc.DomainServiceBlockingStub.class).createDomain(request);
    }

    @Override
    public DomainOuterClass.UpdateDomainResponse updateDomain(DomainOuterClass.UpdateDomainRequest request) {
        return dynamicKusciaChannelProvider.currentStub(DomainServiceGrpc.DomainServiceBlockingStub.class).updateDomain(request);
    }

    @Override
    public DomainOuterClass.DeleteDomainResponse deleteDomain(DomainOuterClass.DeleteDomainRequest request) {
        return dynamicKusciaChannelProvider.currentStub(DomainServiceGrpc.DomainServiceBlockingStub.class).deleteDomain(request);
    }

    @Override
    public DomainOuterClass.QueryDomainResponse queryDomain(DomainOuterClass.QueryDomainRequest request) {
        return dynamicKusciaChannelProvider.currentStub(DomainServiceGrpc.DomainServiceBlockingStub.class).queryDomain(request);
    }

    @Override
    public DomainOuterClass.BatchQueryDomainResponse batchQueryDomain(DomainOuterClass.BatchQueryDomainRequest request) {
        return dynamicKusciaChannelProvider.currentStub(DomainServiceGrpc.DomainServiceBlockingStub.class).batchQueryDomain(request);
    }

    @Override
    public DomainOuterClass.CreateDomainResponse createDomain(DomainOuterClass.CreateDomainRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, DomainServiceGrpc.DomainServiceBlockingStub.class).createDomain(request);
    }

    @Override
    public DomainOuterClass.UpdateDomainResponse updateDomain(DomainOuterClass.UpdateDomainRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, DomainServiceGrpc.DomainServiceBlockingStub.class).updateDomain(request);
    }

    @Override
    public DomainOuterClass.DeleteDomainResponse deleteDomain(DomainOuterClass.DeleteDomainRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, DomainServiceGrpc.DomainServiceBlockingStub.class).deleteDomain(request);
    }

    @Override
    public DomainOuterClass.QueryDomainResponse queryDomain(DomainOuterClass.QueryDomainRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, DomainServiceGrpc.DomainServiceBlockingStub.class).queryDomain(request);
    }

    @Override
    public DomainOuterClass.BatchQueryDomainResponse batchQueryDomain(DomainOuterClass.BatchQueryDomainRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, DomainServiceGrpc.DomainServiceBlockingStub.class).batchQueryDomain(request);
    }

    @Override
    public Health.HealthResponse healthZ(Health.HealthRequest request) {
        return dynamicKusciaChannelProvider.currentStub(HealthServiceGrpc.HealthServiceBlockingStub.class).healthZ(request);
    }

    @Override
    public Health.HealthResponse healthZ(Health.HealthRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, HealthServiceGrpc.HealthServiceBlockingStub.class).healthZ(request);
    }

    @Override
    public Job.CreateJobResponse createJob(Job.CreateJobRequest request) {
        return dynamicKusciaChannelProvider.currentStub(JobServiceGrpc.JobServiceBlockingStub.class).createJob(request);
    }

    @Override
    public Job.QueryJobResponse queryJob(Job.QueryJobRequest request) {
        return dynamicKusciaChannelProvider.currentStub(JobServiceGrpc.JobServiceBlockingStub.class).queryJob(request);
    }

    @Override
    public Job.BatchQueryJobStatusResponse batchQueryJobStatus(Job.BatchQueryJobStatusRequest request) {
        return dynamicKusciaChannelProvider.currentStub(JobServiceGrpc.JobServiceBlockingStub.class).batchQueryJobStatus(request);
    }

    @Override
    public Job.DeleteJobResponse deleteJob(Job.DeleteJobRequest request) {
        return dynamicKusciaChannelProvider.currentStub(JobServiceGrpc.JobServiceBlockingStub.class).deleteJob(request);
    }

    @Override
    public Job.StopJobResponse stopJob(Job.StopJobRequest request) {
        return dynamicKusciaChannelProvider.currentStub(JobServiceGrpc.JobServiceBlockingStub.class).stopJob(request);
    }

    @Override
    public Iterator<Job.WatchJobEventResponse> watchJob(Job.WatchJobRequest request) {
        return dynamicKusciaChannelProvider.currentStub(JobServiceGrpc.JobServiceBlockingStub.class).watchJob(request);
    }

    @Override
    public Job.ApproveJobResponse approveJob(Job.ApproveJobRequest request) {
        return dynamicKusciaChannelProvider.currentStub(JobServiceGrpc.JobServiceBlockingStub.class).approveJob(request);
    }

    @Override
    public Job.SuspendJobResponse suspendJob(Job.SuspendJobRequest request) {
        return dynamicKusciaChannelProvider.currentStub(JobServiceGrpc.JobServiceBlockingStub.class).suspendJob(request);
    }

    @Override
    public Job.RestartJobResponse restartJob(Job.RestartJobRequest request) {
        return dynamicKusciaChannelProvider.currentStub(JobServiceGrpc.JobServiceBlockingStub.class).restartJob(request);
    }

    @Override
    public Job.CancelJobResponse cancelJob(Job.CancelJobRequest request) {
        return dynamicKusciaChannelProvider.currentStub(JobServiceGrpc.JobServiceBlockingStub.class).cancelJob(request);
    }

    @Override
    public Job.CreateJobResponse createJob(Job.CreateJobRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, JobServiceGrpc.JobServiceBlockingStub.class).createJob(request);
    }

    @Override
    public Job.QueryJobResponse queryJob(Job.QueryJobRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, JobServiceGrpc.JobServiceBlockingStub.class).queryJob(request);
    }

    @Override
    public Job.BatchQueryJobStatusResponse batchQueryJobStatus(Job.BatchQueryJobStatusRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, JobServiceGrpc.JobServiceBlockingStub.class).batchQueryJobStatus(request);
    }

    @Override
    public Job.DeleteJobResponse deleteJob(Job.DeleteJobRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, JobServiceGrpc.JobServiceBlockingStub.class).deleteJob(request);
    }

    @Override
    public Job.StopJobResponse stopJob(Job.StopJobRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, JobServiceGrpc.JobServiceBlockingStub.class).stopJob(request);
    }

    @Override
    public Iterator<Job.WatchJobEventResponse> watchJob(Job.WatchJobRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, JobServiceGrpc.JobServiceBlockingStub.class).watchJob(request);
    }

    @Override
    public Job.ApproveJobResponse approveJob(Job.ApproveJobRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, JobServiceGrpc.JobServiceBlockingStub.class).approveJob(request);
    }

    @Override
    public Job.SuspendJobResponse suspendJob(Job.SuspendJobRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, JobServiceGrpc.JobServiceBlockingStub.class).suspendJob(request);
    }

    @Override
    public Job.RestartJobResponse restartJob(Job.RestartJobRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, JobServiceGrpc.JobServiceBlockingStub.class).restartJob(request);
    }

    @Override
    public Job.CancelJobResponse cancelJob(Job.CancelJobRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, JobServiceGrpc.JobServiceBlockingStub.class).cancelJob(request);
    }

    @Override
    public Serving.CreateServingResponse createServing(Serving.CreateServingRequest request) {
        return dynamicKusciaChannelProvider.currentStub(ServingServiceGrpc.ServingServiceBlockingStub.class).createServing(request);
    }

    @Override
    public Serving.UpdateServingResponse updateServing(Serving.UpdateServingRequest request) {
        return dynamicKusciaChannelProvider.currentStub(ServingServiceGrpc.ServingServiceBlockingStub.class).updateServing(request);
    }

    @Override
    public Serving.DeleteServingResponse deleteServing(Serving.DeleteServingRequest request) {
        return dynamicKusciaChannelProvider.currentStub(ServingServiceGrpc.ServingServiceBlockingStub.class).deleteServing(request);
    }

    @Override
    public Serving.QueryServingResponse queryServing(Serving.QueryServingRequest request) {
        return dynamicKusciaChannelProvider.currentStub(ServingServiceGrpc.ServingServiceBlockingStub.class).queryServing(request);
    }

    @Override
    public Serving.BatchQueryServingStatusResponse batchQueryServingStatus(Serving.BatchQueryServingStatusRequest request) {
        return dynamicKusciaChannelProvider.currentStub(ServingServiceGrpc.ServingServiceBlockingStub.class).batchQueryServingStatus(request);
    }

    @Override
    public Serving.CreateServingResponse createServing(Serving.CreateServingRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, ServingServiceGrpc.ServingServiceBlockingStub.class).createServing(request);
    }

    @Override
    public Serving.UpdateServingResponse updateServing(Serving.UpdateServingRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, ServingServiceGrpc.ServingServiceBlockingStub.class).updateServing(request);
    }

    @Override
    public Serving.DeleteServingResponse deleteServing(Serving.DeleteServingRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, ServingServiceGrpc.ServingServiceBlockingStub.class).deleteServing(request);
    }

    @Override
    public Serving.QueryServingResponse queryServing(Serving.QueryServingRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, ServingServiceGrpc.ServingServiceBlockingStub.class).queryServing(request);
    }

    @Override
    public Serving.BatchQueryServingStatusResponse batchQueryServingStatus(Serving.BatchQueryServingStatusRequest request, String domainId) {
        return dynamicKusciaChannelProvider.createStub(domainId, ServingServiceGrpc.ServingServiceBlockingStub.class).batchQueryServingStatus(request);
    }
}