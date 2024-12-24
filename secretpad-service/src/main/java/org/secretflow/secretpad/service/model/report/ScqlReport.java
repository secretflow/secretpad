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

package org.secretflow.secretpad.service.model.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScqlReport {

    private List<SQLWarning> warnings;

    @JsonProperty("affected_rows")
    private long affectedRows;

    @JsonProperty("cost_time_s")
    private double costTimeS;

    @JsonProperty("out_columns")
    private List<OutColumn> outColumns;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OutColumn {

        private Annotation annotation;

        @JsonProperty("elem_type")
        private String elemType;

        @JsonProperty("int32_data")
        private List<Integer> int32Data;

        @JsonProperty("int64_data")
        private List<Long> int64Data;

        @JsonProperty("float_data")
        private List<Float> floatData;

        @JsonProperty("double_data")
        private List<Double> doubleData;

        @JsonProperty("bool_data")
        private List<Boolean> boolData;

        @JsonProperty("string_data")
        private List<String> stringData;

        /**
         * validity mask for data(int32_data/int64_data/...), size can be zero(all
         * data valid) or the same as data, where item false means NULL, true means
         * valid value
         */
        @JsonProperty("data_validity")
        private List<Boolean> dataValidity;

        /**
         * Tensor reference nums, internally used to delete tensor immediately
         */
        @JsonProperty("ref_num")
        private Integer refNum;

        private String name;

        private Shape shape;
    }

    @Data
    @NoArgsConstructor
    public static class Annotation {
        // This class can be extended if there are specific fields in the annotation
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Shape {
        private List<Dim> dim;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Dim {
        @JsonProperty("dim_value")
        private String dimValue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Status {
        private int code;
        private String message;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SQLWarning {
        private String reason;
    }

}
