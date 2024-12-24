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

package org.secretflow.secretpad.service.constant;

import java.util.ArrayList;
import java.util.List;

/**
 * Component Constants
 *
 * @author yansi
 * @date 2023/6/9
 */
public class ComponentConstants {
    public static final String READ_DATA = "read_data";
    public static final String DATA_TABLE = "datatable";
    public static final String COMP_READ_DATA_DATATABLE_ID = "read_data/datatable";
    public static final String COMP_READ_MODEL_ID = "ml.predict/read_model";
    public static final String COMP_ID_DELIMITER = "/";

    public static final List<String> PAD_COMP = new ArrayList<>();
    static {
        PAD_COMP.add(COMP_READ_DATA_DATATABLE_ID);
        PAD_COMP.add(COMP_READ_MODEL_ID);
    }

    public static final String SECRETPAD = "secretpad";
    public static final String SECRETPAD_TEE = "secretpad_tee";
    public static final String SECRETFLOW = "secretflow";
    public static final String TRUSTEDFLOW = "trustedflow";
    public static final String SCQL = "scql";
    public static final String CHECKPOINT_SUB = "_checkpoint";
    public static final String CHECKPOINT_PRE = "ck";

    public static final String DATA_TABLE_DELIMITER = "_";
    public static final String DOMAIN_DATA_TABLE_DELIMITER = "-";

    public static final String DATA_TABLE_PARTITION_DELIMITER = ",";
    public static final String DOMAIN_DATA_TABLE_PARTITION_DELIMITER = ";";
}
