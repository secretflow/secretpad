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

package org.secretflow.secretpad.persistence.datasync.buffer.p2p;

import org.secretflow.secretpad.common.util.UniqueLinkedBlockingQueue;
import org.secretflow.secretpad.persistence.datasync.buffer.DataSyncDataBufferTemplate;
import org.secretflow.secretpad.persistence.datasync.event.P2pDataSyncSendEvent;
import org.secretflow.secretpad.persistence.datasync.listener.EntityChangeListener;
import org.secretflow.secretpad.persistence.datasync.producer.p2p.P2pDataSyncProducerTemplate;
import org.secretflow.secretpad.persistence.entity.BaseAggregationRoot;
import org.secretflow.secretpad.persistence.model.DbChangeAction;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.util.Iterator;
import java.util.List;

/**
 * @author yutu
 * @date 2023/12/10
 */
@Slf4j
@RequiredArgsConstructor
public class P2PDataSyncDataBufferTemplate extends DataSyncDataBufferTemplate {

    private final Object lock = new Object();
    private final ApplicationEventPublisher applicationEventPublisher;
    @Value("${secretpad.sync-path:./config/sync/}")
    @Setter
    private String syncPath;

    /**
     * push data at end of buffer
     *
     * @param event
     */
    @Override
    public void push(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        List<String> nodeIds = event.getNodeIds();
        if (CollectionUtils.isEmpty(nodeIds)) {
            return;
        }
        nodeIds.forEach(nodeId -> {
            if (nodeId.equals(P2pDataSyncProducerTemplate.instId)) {
                return;
            }
            event.setDstNode(nodeId);
            log.debug("p2pDayaSyncDataBufferTemplate push data {} {}", nodeId, event);
            UniqueLinkedBlockingQueue<EntityChangeListener.DbChangeEvent<BaseAggregationRoot>> queue = QUEUE_MAP.getOrDefault(nodeId, new UniqueLinkedBlockingQueue<>());
            Iterator<EntityChangeListener.DbChangeEvent<BaseAggregationRoot>> iterator = queue.iterator();
            while (iterator.hasNext()) {
                EntityChangeListener.DbChangeEvent<BaseAggregationRoot> next = iterator.next();
                BaseAggregationRoot nextSource = next.getSource();
                BaseAggregationRoot eventSource = event.getSource();
                if (ObjectUtils.isEmpty(nextSource.getId()) || ObjectUtils.isEmpty(eventSource.getId())) {
                    continue;
                }
                if (next.getAction().equals(DbChangeAction.UPDATE.val) && next.getDType().equals(event.getDType()) && nextSource.getId().equals(eventSource.getId())) {
                    iterator.remove();
                    log.debug("data sync queue remove some update db action {}", iterator);
                }
            }
            try {
                queue.put(event);
            } catch (InterruptedException e) {
                log.error("p2pDayaSyncDataBufferTemplate push data error", e);
            }
            QUEUE_MAP.put(nodeId, queue);
            endurance(nodeId);
            applicationEventPublisher.publishEvent(new P2pDataSyncSendEvent(this, nodeId));
        });
    }

    /**
     * pop data at first of buffer
     */
    @Override
    public EntityChangeListener.DbChangeEvent<BaseAggregationRoot> peek(String nodeId) throws InterruptedException {
        UniqueLinkedBlockingQueue<EntityChangeListener.DbChangeEvent<BaseAggregationRoot>> queue =
                QUEUE_MAP.getOrDefault(nodeId, null);
        return ObjectUtils.isEmpty(queue) ? null : queue.peek();
    }

    /**
     * pop data at first of buffer
     *
     * @param nodeId
     */
    @Override
    public EntityChangeListener.DbChangeEvent<BaseAggregationRoot> poll(String nodeId) throws InterruptedException {
        UniqueLinkedBlockingQueue<EntityChangeListener.DbChangeEvent<BaseAggregationRoot>> queue =
                QUEUE_MAP.getOrDefault(nodeId, null);
        return ObjectUtils.isEmpty(queue) ? null : queue.poll();
    }

    @Override
    public int size(String nodeId) {
        UniqueLinkedBlockingQueue<EntityChangeListener.DbChangeEvent<BaseAggregationRoot>> queue =
                QUEUE_MAP.getOrDefault(nodeId, null);
        return ObjectUtils.isEmpty(queue) ? 0 : queue.size();
    }

    @Override
    public void commit(String nodeId, EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        UniqueLinkedBlockingQueue<EntityChangeListener.DbChangeEvent<BaseAggregationRoot>> queue =
                QUEUE_MAP.getOrDefault(nodeId, null);
        queue.remove(event);
        endurance(nodeId);
        log.info("{} commit {}", nodeId, event);
    }

    @Override
    public void endurance(String nodeId) {
        UniqueLinkedBlockingQueue<EntityChangeListener.DbChangeEvent<BaseAggregationRoot>> queue =
                QUEUE_MAP.getOrDefault(nodeId, null);
        synchronized (lock) {
            if (queue != null) {
                try {
                    serializableWrite(nodeId, queue);
                } catch (Exception e) {
                    log.error("serializableWrite error", e);
                }
            }
        }
    }

    @PostConstruct
    public void init() throws IOException {
        File file = ResourceUtils.getFile(syncPath);
        File[] files = file.listFiles();
        if (files == null) {
            return;
        }
        for (File f : files) {
            if (f.isFile()) {
                String nodeId = f.getName();
                QUEUE_MAP.put(nodeId, serializableRead(nodeId));
                applicationEventPublisher.publishEvent(new P2pDataSyncSendEvent(this, nodeId));
            }
        }
    }

    public void serializableWrite(String nodeId, UniqueLinkedBlockingQueue queue) throws IOException {
        log.info("serializableWrite ---{}", queue.size());
        ObjectOutputStream os = null;
        File file = ResourceUtils.getFile(syncPath + nodeId);
        if (!file.exists() && file.getParentFile().mkdirs() && !file.createNewFile()) {
            log.error("create serializableWrite file error");
        }
        try {
            os = new ObjectOutputStream(new FileOutputStream(file));
            os.writeObject(queue);
        } finally {
            IOUtils.closeQuietly(os);
        }
    }

    public UniqueLinkedBlockingQueue serializableRead(String nodeId) throws IOException {
        ObjectInputStream in = null;
        UniqueLinkedBlockingQueue queue = new UniqueLinkedBlockingQueue();
        File file = ResourceUtils.getFile(syncPath + nodeId);
        if (!file.exists()) {
            return null;
        }
        try {
            in = new ObjectInputStream(new FileInputStream(file));
            queue = (UniqueLinkedBlockingQueue) in.readObject();
        } catch (Exception e) {
            log.error("serializableRead error ", e);
        } finally {
            IOUtils.closeQuietly(in);
        }
        log.info("serializableRead ---{} {}", nodeId, queue.size());
        return queue;
    }

}