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

package org.secretflow.secretpad.common.errorcode;

/**
 * Graph errorCode
 *
 * @author yansi
 * @date 2023/5/30
 */
public enum GraphErrorCode implements ErrorCode {
    /**
     * Component does not exist
     */
    COMPONENT_NOT_EXISTS(202011701),
    /**
     * Graph does not exist
     */
    GRAPH_NOT_EXISTS(202011702),
    /**
     * Graph already exists
     */
    GRAPH_EXISTS(202011703),
    /**
     * The graph node does not exist
     */
    GRAPH_NODE_NOT_EXISTS(202011704),
    /**
     * The graph data table is empty
     */
    GRAPH_DATATABLE_EMPTY(202011705),
    /**
     * Component internationalization error
     */
    COMPONENT_18N_ERROR(202011706),
    /**
     * Invalid graph job
     */
    GRAPH_JOB_INVALID(202011707),
    /**
     * Graph node output does not exist
     */
    GRAPH_NODE_OUTPUT_NOT_EXISTS(202011708),
    /**
     * The output result type is not supported
     */
    RESULT_TYPE_NOT_SUPPORTED(202011709),
    /**
     * The graph dependent node is not started
     */
    GRAPH_DEPENDENT_NODE_NOT_RUN(202011710),
    GRAPH_NODE_ROUTE_NOT_EXISTS(202011711),
    GRAPH_NOT_OWNER_CANNOT_UPDATE(202011712),
    NON_OUR_CREATION_CAN_VIEWED(202011713)
    ;
    private final int code;

    GraphErrorCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessageKey() {
        return "graph." + this.name();
    }

    @Override
    public Integer getCode() {
        return code;
    }
}
