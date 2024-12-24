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

package org.secretflow.secretpad.service.model.graph;

import org.secretflow.secretpad.common.errorcode.GraphErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.persistence.model.ResultKind;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Graph node output view object
 *
 * @author yansi
 * @date 2023/5/25
 */
@Data
@Builder
public class GraphNodeOutputVO {
    /**
     * Graph result type
     */
    private String type;
    /**
     * Graph code name
     */
    private String codeName;
    /**
     * Graph node output tabs
     */
    private Object tabs;
    /**
     * Graph node output file meta
     */
    private FileMeta meta;

    /**
     * Graph node output jobId
     */
    private String jobId;

    /**
     * Graph node output taskId
     */
    private String taskId;

    /**
     * this output produced by this graph
     */
    private String graphID;

    /**
     * warning
     */
    private List<String> warning;
    /**
     * Graph start time
     */
    private String gmtCreate;
    /**
     * Graph update time
     */
    private String gmtModified;

    /**
     * Convert graph result type from result kind
     *
     * @param resultKind result kind
     * @return graph result type
     */
    public static String typeFromResultKind(ResultKind resultKind) {
        switch (resultKind) {
            case Report:
                return "report";
            case FedTable:
                return "table";
            case Model:
                return "model";
            case Rule:
                return "rule";
            case READ_DATA:
                return "read_data";
            default:
                throw SecretpadException.of(GraphErrorCode.RESULT_TYPE_NOT_SUPPORTED);
        }
    }

    /**
     * Graph node output file meta
     */
    @Data
    @Builder
    public static class FileMeta {
        /**
         * Graph node output file headers
         */
        private Object headers;
        /**
         * Graph node output file rows
         */
        private Object rows;
    }

    /**
     * Graph node output result
     */
    @Data
    @Builder
    public static class OutputResult {
        /**
         * Datatable relative uri
         */
        private String path;
        /**
         * Project result nodeId
         */
        private String nodeId;
        /**
         * Project result nameName
         */
        private String nodeName;
        /**
         * node type
         */
        private String type;

        /**
         * Build table column name list to string
         */
        private String fields;
        /**
         * Build table column type list to string
         */
        private String fieldTypes;
        /**
         * Datatable id
         */
        private String tableId;
        /**
         * The data source id which it belongs to
         */
        private String dsId;

        /**
         * The data source type
         * see {@link org.secretflow.secretpad.common.enums.DataSourceTypeEnum}
         */
        private String datasourceType;
    }

}