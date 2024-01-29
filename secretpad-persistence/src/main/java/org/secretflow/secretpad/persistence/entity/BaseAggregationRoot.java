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

package org.secretflow.secretpad.persistence.entity;

import org.secretflow.secretpad.persistence.converter.Boolean2IntConverter;
import org.secretflow.secretpad.persistence.converter.SqliteLocalDateTimeConverter;
import org.secretflow.secretpad.persistence.datasync.listener.EntityChangeListener;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.io.Serial;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Base aggregate root
 *
 * @author jiezi
 * @date 2023/5/30
 */
@Getter
@Setter
@ToString
@MappedSuperclass
@EntityListeners(EntityChangeListener.class)
public abstract class BaseAggregationRoot<A extends AbstractAggregateRoot<A>> extends AbstractAggregateRoot<A> implements ProjectNodesInfo {
    @Serial
    private static final long serialVersionUID = 5005877919773504643L;

    /**
     * The id of the database is automatically added
     */
    @Column(name = "id", unique = true, insertable = false, updatable = false)
    Long id;

    /**
     * Whether to delete tag
     */
    @Column(name = "is_deleted", nullable = false, length = 1)
    @Convert(converter = Boolean2IntConverter.class)
    Boolean isDeleted = false;

    /**
     * Start time
     */
    @Column(name = "gmt_create", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Convert(converter = SqliteLocalDateTimeConverter.class)
    LocalDateTime gmtCreate = parseNow();

    /**
     * Update time
     */
    @Column(name = "gmt_modified", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Convert(converter = SqliteLocalDateTimeConverter.class)
    LocalDateTime gmtModified = parseNow();

    // Default get project id method
    @Override
    public String getProjectId() {
        return null;
    }

    // Default get node id method
    public String getNodeId() {
        return null;
    }

    // Default get node id list method
    @Override
    public List<String> getNodeIds() {
        List<String> nodeIds = new ArrayList<>();

        String nodeId = this.getNodeId();
        if (nodeId != null) {
            nodeIds.add(nodeId);
        }
        return nodeIds;
    }

    private LocalDateTime parseNow() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        DateTimeFormatter dtf3 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String strNow = dtf3.format(now);
        return LocalDateTime.parse(strNow, dtf3);
    }
}
