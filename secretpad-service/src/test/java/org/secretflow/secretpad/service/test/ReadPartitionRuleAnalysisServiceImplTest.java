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

package org.secretflow.secretpad.service.test;

import org.secretflow.secretpad.common.enums.DataSourceTypeEnum;
import org.secretflow.secretpad.service.impl.ReadPartitionRuleAnalysisServiceImpl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

/**
 * @author yutu
 * @date 2024/09/10
 */
@Slf4j
public class ReadPartitionRuleAnalysisServiceImplTest {

    @Test
    public void test() {
        Set<String> partitionColumns = Set.of("dt", "a", "b", "c", "d", "e");
        String scheduleExpectStartDate = "20240910";

        String sql = "dt=maxpt and a=${yyyymmdd} or b=${yyyymmdd-1} or c=${yyyymmdd+2} or d=${yyyymmdd} or e=${yyyymmdd-1}";
        ReadPartitionRuleAnalysisServiceImpl readPartitionRuleAnalysisService = new ReadPartitionRuleAnalysisServiceImpl();
        String s = readPartitionRuleAnalysisService.readPartitionRuleAnalysis("test_table", DataSourceTypeEnum.ODPS, sql, scheduleExpectStartDate, partitionColumns);
        log.info(s);
        Assertions.assertEquals("", readPartitionRuleAnalysisService.readPartitionRuleAnalysis("test_table", DataSourceTypeEnum.OSS, sql, scheduleExpectStartDate, partitionColumns));
        sql = "dt=maxpt and a=${yyyymmdd} or d in('20240101',${yyyymmdd}) or e in (${yyyymmdd-1},${yyyymmdd+1}) or f between ${yyyymmdd-1} and ${yyyymmdd+1}";
        String finalSql = sql;
        Assertions.assertThrows(IllegalArgumentException.class, () -> readPartitionRuleAnalysisService.readPartitionRuleAnalysis("test_table", DataSourceTypeEnum.ODPS, finalSql, scheduleExpectStartDate, partitionColumns));
    }
}