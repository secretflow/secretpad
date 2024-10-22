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

package org.secretflow.secretpad.service.listener;

import org.secretflow.secretpad.persistence.entity.ProjectJobDO;
import org.secretflow.secretpad.persistence.entity.ProjectJobTaskLogDO;
import org.secretflow.secretpad.persistence.entity.ProjectTaskDO;
import org.secretflow.secretpad.persistence.repository.ProjectJobTaskLogRepository;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Job task log event listener
 *
 * @author yansi
 * @date 2023/6/12
 */
@Slf4j
@Component
public class JobTaskLogEventListener {

    @Autowired
    private ProjectJobTaskLogRepository logRepository;

    /**
     * Add task logs via task status transform event
     *
     * @param event task status transform event
     */
    @EventListener
    public void onApplicationEvent(ProjectJobDO.TaskStatusTransformEvent event) {
        addTaskLog(event);
    }

    private void addTaskLog(ProjectJobDO.TaskStatusTransformEvent event) {
        log.info("*** JobTaskLogEventListener {} {} {}", event.getTaskId(), event.getFromStatus(), event.getToStatus());
        List<ProjectJobTaskLogDO> logs = Lists.newArrayList();
        ProjectTaskDO task = event.getSource().getTasks().get(event.getTaskId());
        switch (event.getFromStatus()) {
            case STAGING:
            case INITIALIZED:
                switch (event.getToStatus()) {
                    case RUNNING:
                        logs.add(ProjectJobTaskLogDO.taskStartLog(task));
                        break;
                    case SUCCEED:
                        logs.add(ProjectJobTaskLogDO.taskStartLog(task));
                        logs.add(ProjectJobTaskLogDO.taskSucceedLog(task));
                        break;
                    case STOPPED:
                        logs.add(ProjectJobTaskLogDO.taskStoppedLog(task));
                        break;
                    case FAILED:
                        logs.add(ProjectJobTaskLogDO.taskStartLog(task));
                        // when failed: we catch the party error msg to logs
                        logs.addAll(event.getReasons().stream().map(r -> ProjectJobTaskLogDO.taskFailedLog(task, r)).toList());
                        break;
                    default:
                        // do nothing
                }

            case RUNNING:
                switch (event.getToStatus()) {
                    case STOPPED:
                        logs.add(ProjectJobTaskLogDO.taskStoppedLog(task));
                        break;
                    case SUCCEED:
                        logs.add(ProjectJobTaskLogDO.taskSucceedLog(task));
                        break;
                    case FAILED:
                        // when failed: we catch the party error msg to logs
                        logs.addAll(event.getReasons().stream().map(r -> ProjectJobTaskLogDO.taskFailedLog(task, r)).collect(Collectors.toList()));
                        break;
                    default:
                        // do nothing
                }
            default:
                // do nothing
        }
        // remove duplicated logs
        Set<ProjectJobTaskLogDO> setWithoutDuplicates = new HashSet<>(logs);
        logs = new ArrayList<>(setWithoutDuplicates);
        logRepository.saveAll(logs);
    }
}
