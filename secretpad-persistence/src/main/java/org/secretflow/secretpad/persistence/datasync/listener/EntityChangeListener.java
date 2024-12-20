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

package org.secretflow.secretpad.persistence.datasync.listener;

import org.secretflow.secretpad.common.util.DataSyncConsumerContext;
import org.secretflow.secretpad.common.util.SpringContextUtil;
import org.secretflow.secretpad.persistence.datasync.producer.AbstractDataSyncProducerTemplate;
import org.secretflow.secretpad.persistence.entity.BaseAggregationRoot;
import org.secretflow.secretpad.persistence.entity.ProjectNodesInfo;
import org.secretflow.secretpad.persistence.model.DbChangeAction;

import jakarta.annotation.Resource;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * DataSyncProducerTemplate
 *
 * @author yutu
 * @date 2023/12/06
 */
@Slf4j
@Component
@DependsOn(value = {"springContextUtil", "dataSyncProducerTemplate"})
@SuppressWarnings(value = {"rawtypes"})
public class EntityChangeListener {
    @Resource
    private AbstractDataSyncProducerTemplate dataSyncProducerTemplate;

    @PostUpdate
    public void postUpdate(BaseAggregationRoot o) {
        loadAbstractDataSyncProducerTemplate();
        if (!DataSyncConsumerContext.sync()) {
            log.debug("************************ EntityChangeListener postUpdate {}", o.getClass().getName());
            dataSyncProducerTemplate.push(DbChangeEvent.of(DbChangeAction.UPDATE, o));
        }
    }

    @PostRemove
    public void postRemove(BaseAggregationRoot o) {
        loadAbstractDataSyncProducerTemplate();
        if (!DataSyncConsumerContext.sync()) {
            log.debug("************************ EntityChangeListener postRemove {}", o.getClass().getName());
            dataSyncProducerTemplate.push(DbChangeEvent.of(DbChangeAction.REMOVE, o));
        }
    }

    @PostPersist
    public void postCreate(BaseAggregationRoot o) {
        loadAbstractDataSyncProducerTemplate();
        if (!DataSyncConsumerContext.sync()) {
            log.debug("************************ EntityChangeListener postCreate {}", o.getClass().getName());
            dataSyncProducerTemplate.push(DbChangeEvent.of(DbChangeAction.CREATE, o));
        }
    }

    private void loadAbstractDataSyncProducerTemplate() {
        if (ObjectUtils.isEmpty(dataSyncProducerTemplate)) {
            dataSyncProducerTemplate = SpringContextUtil.getBean(AbstractDataSyncProducerTemplate.class);
            if (ObjectUtils.isEmpty(dataSyncProducerTemplate)) {
                log.warn("*************************dataSyncProducerTemplate is null");
            }
        }
    }

    @ToString
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class DbChangeEvent<T extends ProjectNodesInfo> implements Serializable {
        @Serial
        private static final long serialVersionUID = 8759123498754L;
        /**
         * dist node
         */
        private String dstNode;
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

        private T source;

        private DbChangeEvent(T source) {
            this.source = source;
        }

        public static <T extends ProjectNodesInfo> DbChangeEvent<T> of(DbChangeAction action, T source) {
            if (ObjectUtils.isEmpty(source)) {
                throw new IllegalArgumentException("source can't be empty!");
            }
            DbChangeEvent<T> event = new DbChangeEvent<>(source);
            event.action = action.getVal();
            event.dType = source.getClass().getTypeName();
            event.projectId = source.getProjectId();
            event.nodeIds = source.getNodeIds();
            return event;
        }
    }
}