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

package org.secretflow.secretpad.persistence.model;

/**
 * Result kind enum
 *
 * @author yansi
 * @date 2023/5/30
 */
public enum ResultKind {
    /**
     * Federal table
     */
    FedTable("table"),
    /**
     * model
     */
    Model("model"),
    /**
     * rule
     */
    Rule("rule"),
    /**
     * report
     */
    Report("report");

    private final String name;

    ResultKind(String name) {
        this.name = name;
    }

    /**
     * Convert result kind from datatable type
     *
     * @param type
     * @return
     */
    public static ResultKind fromDatatable(String type) {
        if ("table".equalsIgnoreCase(type)) {
            return FedTable;
        }
        if ("model".equalsIgnoreCase(type)) {
            return Model;
        }
        if ("rule".equalsIgnoreCase(type)) {
            return Rule;
        }
        if ("report".equals(type)) {
            return Report;
        }
        return null;
    }

    public String getName() {
        return this.name;
    }

}
