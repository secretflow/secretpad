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

package org.secretflow.secretpad.persistence.listener;

import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.common.util.UniqueLinkedBlockingQueue;
import org.secretflow.secretpad.persistence.entity.BaseAggregationRoot;
import org.secretflow.secretpad.persistence.entity.ProjectJobDO;
import org.secretflow.secretpad.persistence.entity.ProjectNodesInfo;
import org.secretflow.secretpad.persistence.entity.ProjectTaskDO;
import org.secretflow.secretpad.persistence.model.DataSyncConfig;
import org.secretflow.secretpad.persistence.model.DbChangeAction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Resource;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.Getter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Entity change listener
 *
 * @author zhiyin
 * @date 2023/10/19
 */
@Component
@SuppressWarnings(value = {"rawtypes"})
public class EntityChangeListener {
    @Value("${secretpad.platform-type}")
    private String platformType;

    @Resource
    private  DataSyncConfig dataSyncConfig;

    public static UniqueLinkedBlockingQueue<DbChangeEvent> queue = new UniqueLinkedBlockingQueue<>();

    @PostUpdate
    public void postUpdate(BaseAggregationRoot o) {
        DbChangeEvent event = DbChangeEvent.of(DbChangeAction.UPDATE, o);
        push(event);
    }

    @PostRemove
    public void postRemove(BaseAggregationRoot o) {
        DbChangeEvent event = DbChangeEvent.of(DbChangeAction.REMOVE, o);
        push(event);
    }

    @PostPersist
    public void postCreate(BaseAggregationRoot o) {
        DbChangeEvent event = DbChangeEvent.of(DbChangeAction.CREATE, o);
        push(event);
    }

    public void push(DbChangeEvent event) {
        if (PlatformTypeEnum.valueOf(platformType).equals(PlatformTypeEnum.CENTER)) {
            if (!ignore(event)) {
                queue.add(event);
            }
        }
    }

    /**
     * if return ture ignore db event to sync
     *
     * @param event db event
     * @return true ignore | false sync data
     */
    private boolean ignore(DbChangeEvent event) {
        List<String> sync = dataSyncConfig.getSync();
        String dType = event.getDType();
        if (!sync.contains(dType)) {
            return true;
        }
        ProjectNodesInfo source = event.getSource();
        if (source instanceof ProjectTaskDO) {
            if (DbChangeAction.UPDATE.getVal().equals(event.getAction())) {
                return true;
            }
        }
        if(source instanceof ProjectJobDO){
            return DbChangeAction.UPDATE.getVal().equals(event.getAction());
        }
        return false;
    }

    @ToString
    @Getter
    @SuppressWarnings(value = {"rawtypes"})
    public static class DbChangeEvent<T extends ProjectNodesInfo> {
        /**
         * Action
         */
        private String action;
        /**
         * Datatype
         */
        private String dType;
        /**
         * Event project id
         */
        private String projectId;
        /**
         * Event nodeId
         */
        private List<String> nodeIds;

        @JsonIgnore
        private T source;

        private DbChangeEvent(T source) {
            this.source = source;
        }

        public static <T extends ProjectNodesInfo> DbChangeEvent<T> of(DbChangeAction action, T source) {
            DbChangeEvent event = new DbChangeEvent(source);
            event.action = action.getVal();
            event.dType = source.getClass().getTypeName();
            event.projectId = source.getProjectId();
            event.nodeIds = source.getNodeIds();
            return event;
        }
    }
}
