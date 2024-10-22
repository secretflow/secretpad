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

package org.secretflow.secretpad.service;

import org.secretflow.secretpad.common.enums.DataSourceTypeEnum;

import java.util.Set;

/**
 * @author yutu
 * @date 2024/09/10
 */
public interface ReadPartitionRuleAnalysisService {


    /**
     * read partition rule analysis
     *
     * @param tableName               tableName
     * @param type                    type
     * @param inputRule               inputRule
     * @param scheduleExpectStartDate yyyyMMdd
     * @param partitionColumns        partitionColumns
     * @return ReadPartitionRuleSql
     */
    String readPartitionRuleAnalysis(String tableName, DataSourceTypeEnum type, String inputRule, String scheduleExpectStartDate, Set<String> partitionColumns);

}