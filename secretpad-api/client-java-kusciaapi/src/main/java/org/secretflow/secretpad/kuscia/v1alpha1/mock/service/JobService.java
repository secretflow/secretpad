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

package org.secretflow.secretpad.kuscia.v1alpha1.mock.service;

import io.grpc.stub.StreamObserver;
import org.secretflow.v1alpha1.kusciaapi.Job;
import org.secretflow.v1alpha1.kusciaapi.JobServiceGrpc;

/**
 * @author yutu
 * @date 2024/06/19
 */
public class JobService extends JobServiceGrpc.JobServiceImplBase implements CommonService {


    @Override
    public void queryJob(Job.QueryJobRequest request, StreamObserver<Job.QueryJobResponse> responseObserver) {
        Job.QueryJobResponse resp = Job.QueryJobResponse.newBuilder().setStatus(getStatus()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void createJob(Job.CreateJobRequest request, StreamObserver<Job.CreateJobResponse> responseObserver) {
        Job.CreateJobResponse resp = Job.CreateJobResponse.newBuilder().setStatus(getStatus()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void batchQueryJobStatus(Job.BatchQueryJobStatusRequest request, StreamObserver<Job.BatchQueryJobStatusResponse> responseObserver) {
        Job.BatchQueryJobStatusResponse resp = Job.BatchQueryJobStatusResponse.newBuilder().setStatus(getStatus()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void stopJob(Job.StopJobRequest request, StreamObserver<Job.StopJobResponse> responseObserver) {
        Job.StopJobResponse resp = Job.StopJobResponse.newBuilder().setStatus(getStatus()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void restartJob(Job.RestartJobRequest request, StreamObserver<Job.RestartJobResponse> responseObserver) {
        Job.RestartJobResponse resp = Job.RestartJobResponse.newBuilder().setStatus(getStatus()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void cancelJob(Job.CancelJobRequest request, StreamObserver<Job.CancelJobResponse> responseObserver) {
        Job.CancelJobResponse resp = Job.CancelJobResponse.newBuilder().setStatus(getStatus()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void approveJob(Job.ApproveJobRequest request, StreamObserver<Job.ApproveJobResponse> responseObserver) {
        Job.ApproveJobResponse resp = Job.ApproveJobResponse.newBuilder().setStatus(getStatus()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void watchJob(Job.WatchJobRequest request, StreamObserver<Job.WatchJobEventResponse> responseObserver) {
        Job.WatchJobEventResponse resp = Job.WatchJobEventResponse.newBuilder().build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void suspendJob(Job.SuspendJobRequest request, StreamObserver<Job.SuspendJobResponse> responseObserver) {
        Job.SuspendJobResponse resp = Job.SuspendJobResponse.newBuilder().setStatus(getStatus()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }
}