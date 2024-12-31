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

package org.secretflow.secretpad.common.constant;

/**
 * @author yutu
 * @date 2023/11/24
 */
public class ComponentConstants {

    public static final String CUSTOM_PROTOBUF_CLS = "custom_protobuf_cls";

    public static final String CUSTOM_VALUE = "custom_value";

    public static final String ATTRIBUTE_S = "s";
    public static final String ATTRIBUTE_SS = "ss";

    /**
     * binning_modifications
     */
    public static final String BINNING_MODIFICATIONS = "binning_modifications";
    public static final String BINNING_MODIFICATIONS_CODENAME = "feature/binning_modifications";


    public static final String IO_READ_DATA = "read_data";

    public static final String IO_WRITE_DATA = "write_data";
    public static final String IO_IDENTITY = "identity";
    /**
     * model_param_modifications
     */
    public static final String MODEL_PARAM_MODIFICATIONS = "model_param_modifications";
    public static final String MODEL_PARAM_MODIFICATIONS_CODENAME = "preprocessing/model_param_modifications";

    public static final String DATA_PREP_UNION = "data_prep/union";

    public static final String DATA_FILTER_SAMPLE = "data_filter/sample";

    public static final String DATA_FILTER_EXPR_CONDITION_FILTER = "data_filter/expr_condition_filter";

    public static final String STATS_SCQL_ANALYSIS = "stats/scql_analysis";

    public static final String SCRIPT_INPUT = "script_input";

    public static final String TABLE_SEPARATOR = "_";

    public static final String DATA_PREP_UNBALANCE_PSI_CACHE = "data_prep/unbalance_psi_cache";

    public static final String TASK_INITIATOR = "task_initiator";

}