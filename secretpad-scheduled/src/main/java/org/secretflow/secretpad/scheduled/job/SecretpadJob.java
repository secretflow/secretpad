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

package org.secretflow.secretpad.scheduled.job;

import org.secretflow.secretpad.common.constant.ScheduledConstants;
import org.secretflow.secretpad.common.enums.ScheduledStatus;
import org.secretflow.secretpad.manager.integration.job.AbstractJobManager;
import org.secretflow.secretpad.persistence.entity.ProjectScheduleTaskDO;
import org.secretflow.secretpad.persistence.repository.ProjectScheduleTaskRepository;
import org.secretflow.secretpad.scheduled.event.ScheduledJobStartEvent;

import com.google.protobuf.InvalidProtocolBufferException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.secretflow.v1alpha1.kusciaapi.Job;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Base64;

/**
 * SecretpadJob
 *
 * @author yutu
 * @date 2024/08/21
 */
@Slf4j
@Component
public class SecretpadJob extends QuartzJobBean {

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    @Resource
    private AbstractJobManager jobManager;

    @Resource
    private ProjectScheduleTaskRepository projectScheduleTaskRepository;

    /**
     * Execute the actual job. The job data map will already have been
     * applied as bean property values by execute. The contract is
     * exactly the same as for the standard Quartz execute method.
     *
     * @param context context of the Quartz job
     * @see #execute
     */
    @Override
    protected void executeInternal(JobExecutionContext context) {
        ProjectScheduleTaskDO o = null;
        try {
            log.info("SecretpadJob execute group:{} args:{} trigger by :{}", context.getJobDetail().getKey().getGroup(), context.getJobDetail().getJobDataMap(), context.getTrigger().getJobDataMap());
            JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
            o = (ProjectScheduleTaskDO) jobDataMap.get(ScheduledConstants.SCHEDULED_TASK_DAG_JOB_KEY);
            if (o == null || !projectScheduleTaskRepository.existsById(o.getScheduleTaskId())) {
                throw new RuntimeException("SecretpadJob execute data is null");
            }
            o = projectScheduleTaskRepository.findById(o.getScheduleTaskId()).orElseThrow();
            if (o.getStatus() == ScheduledStatus.RUNNING
                    || o.getStatus() == ScheduledStatus.FAILED
                    || o.getStatus() == ScheduledStatus.SUCCEED
                    || o.getStatus() == ScheduledStatus.STOPPED
            ) {
                log.info("SecretpadJob execute skip data:{} status:{}", o, o.getStatus());
                return;
            }
            log.info("SecretpadJob execute data:{} ", o);
            Job.CreateJobRequest request = null;
            try {
                request = Job.CreateJobRequest.parseFrom(Base64.getDecoder().decode(o.getJobRequest()));
            } catch (InvalidProtocolBufferException e) {
                log.error("SecretpadJob execute data:{} ", o, e);
            }
            jobManager.createJob(request);
            o.setScheduleTaskStartTime(LocalDateTime.now());
            o.setStatus(ScheduledStatus.RUNNING);
            projectScheduleTaskRepository.save(o);
        } catch (Exception e) {
            log.error("SecretpadJob execute error", e);
            if (ObjectUtils.isNotEmpty(o)) {
                projectScheduleTaskRepository.updateStatus(o.getScheduleTaskId(), ScheduledStatus.FAILED.name());
            }
        }
        applicationEventPublisher.publishEvent(new ScheduledJobStartEvent(this, context));
    }
}