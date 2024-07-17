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

package org.secretflow.secretpad.kuscia.v1alpha1.service;

import org.secretflow.v1alpha1.kusciaapi.Job;

import java.util.Iterator;

/**
 * @author yutu
 * @date 2024/06/17
 */
public interface KusciaJobService {

    Job.CreateJobResponse createJob(Job.CreateJobRequest request);

    Job.QueryJobResponse queryJob(Job.QueryJobRequest request);

    Job.BatchQueryJobStatusResponse batchQueryJobStatus(Job.BatchQueryJobStatusRequest request);

    Job.DeleteJobResponse deleteJob(Job.DeleteJobRequest request);

    Job.StopJobResponse stopJob(Job.StopJobRequest request);

    Iterator<Job.WatchJobEventResponse> watchJob(Job.WatchJobRequest request);

    Job.ApproveJobResponse approveJob(Job.ApproveJobRequest request);

    Job.SuspendJobResponse suspendJob(Job.SuspendJobRequest request);

    Job.RestartJobResponse restartJob(Job.RestartJobRequest request);

    Job.CancelJobResponse cancelJob(Job.CancelJobRequest request);


    Job.CreateJobResponse createJob(Job.CreateJobRequest request, String domainId);

    Job.QueryJobResponse queryJob(Job.QueryJobRequest request, String domainId);

    Job.BatchQueryJobStatusResponse batchQueryJobStatus(Job.BatchQueryJobStatusRequest request, String domainId);

    Job.DeleteJobResponse deleteJob(Job.DeleteJobRequest request, String domainId);

    Job.StopJobResponse stopJob(Job.StopJobRequest request, String domainId);

    Iterator<Job.WatchJobEventResponse> watchJob(Job.WatchJobRequest request, String domainId);

    Job.ApproveJobResponse approveJob(Job.ApproveJobRequest request, String domainId);

    Job.SuspendJobResponse suspendJob(Job.SuspendJobRequest request, String domainId);

    Job.RestartJobResponse restartJob(Job.RestartJobRequest request, String domainId);

    Job.CancelJobResponse cancelJob(Job.CancelJobRequest request, String domainId);

}