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

package org.secretflow.secretpad.scheduled.service.impl;

import org.secretflow.secretpad.common.constant.ScheduledConstants;
import org.secretflow.secretpad.common.util.DateTimes;
import org.secretflow.secretpad.common.util.UUIDUtils;
import org.secretflow.secretpad.persistence.entity.ProjectScheduleTaskDO;
import org.secretflow.secretpad.scheduled.job.SecretpadJob;
import org.secretflow.secretpad.scheduled.model.ScheduledIdRequest;
import org.secretflow.secretpad.scheduled.service.ISecretpadScheduledService;

import jakarta.annotation.Resource;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * @author yutu
 * @date 2024/08/21
 */
@Slf4j
@Service
public class SecretpadScheduledServiceImpl implements ISecretpadScheduledService {

    @Resource
    @Setter
    private Scheduler scheduler;

    /**
     * add scheduler
     */
    @Override
    public boolean addScheduler(String name, String group, ProjectScheduleTaskDO data, String cron) {
        try {
            ZoneId zoneId = ZoneId.systemDefault();
            JobKey jobKey = JobKey.jobKey(name, group);
            if (scheduler.checkExists(jobKey)) {
                return true;
            }
            LocalDateTime scheduleTaskExpectStartTime = data.getScheduleTaskExpectStartTime();
            ZonedDateTime zonedDateTime = scheduleTaskExpectStartTime.atZone(zoneId);
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put(ScheduledConstants.SCHEDULED_TASK_DAG_JOB_KEY, data);
            JobDetail jobDetail = JobBuilder.newJob(SecretpadJob.class)
                    .withIdentity(name, group)
                    .usingJobData(jobDataMap)
                    .build();
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(name, group)
                    .forJob(jobDetail)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                    .startAt(Date.from(zonedDateTime.toInstant()))
                    .build();
            scheduler.scheduleJob(jobDetail, trigger);
            return true;
        } catch (SchedulerException e) {
            log.error("addScheduler error", e);
            return false;
        }
    }

    /**
     * pause scheduler
     */
    @Override
    public boolean pauseScheduler(String name, String group) {
        JobKey jobKey = JobKey.jobKey(name, group);
        try {
            scheduler.pauseJob(jobKey);
        } catch (SchedulerException e) {
            log.error("pauseScheduler error", e);
            return false;
        }
        return true;
    }

    /**
     * pause scheduler
     */
    @Override
    public boolean pauseScheduler(String group) {
        try {
            scheduler.pauseJobs(GroupMatcher.jobGroupEquals(group));
        } catch (SchedulerException e) {
            log.error("pauseScheduler error", e);
            return false;
        }
        return true;
    }

    /**
     * resume scheduler
     */
    @Override
    public boolean resumeScheduler(String jobDetailName, String jobDetailGroup) {
        return false;
    }

    /**
     * build scheduler id
     *
     * @param scheduledIdRequest scheduledIdRequest
     * @return scheduler id
     */
    @Override
    public String buildSchedulerId(ScheduledIdRequest scheduledIdRequest) {
        String random = UUIDUtils.random(4);
        String time_str = DateTimes.localTimeNoDelimiter();
        return random + ScheduledConstants.SCHEDULED_ID_DELIMITER + time_str;
    }
}