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

package org.secretflow.secretpad.service.impl;

import org.secretflow.secretpad.common.constant.PartitionConstants;
import org.secretflow.secretpad.common.enums.DataSourceTypeEnum;
import org.secretflow.secretpad.service.ReadPartitionRuleAnalysisService;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.secretflow.secretpad.common.constant.PartitionConstants.ODPS_PARTITION_MAX_PT;
import static org.secretflow.secretpad.common.constant.PartitionConstants.PARTITION_DATE_REG;

/**
 * @author yutu
 * @date 2024/09/10
 */
@Slf4j
@Service
public class ReadPartitionRuleAnalysisServiceImpl implements ReadPartitionRuleAnalysisService {

    /**
     * read partition rule analysis
     *
     * @param tableName tableName
     * @param type      type
     * @param inputRule inputRule
     * @return ReadPartitionRuleSql
     */
    @Override
    public String readPartitionRuleAnalysis(String tableName, DataSourceTypeEnum type, String inputRule, String scheduleExpectStartDate, Set<String> partitionColumns) {
        log.info("read partition rule analysis {} {}", tableName, inputRule);
        String result = "";
        if (StringUtils.isNotEmpty(tableName) && StringUtils.isNotEmpty(inputRule)) {
            if (Objects.requireNonNull(type) == DataSourceTypeEnum.ODPS) {
                result = odpsReadPartitionRuleAnalysis(tableName, inputRule, scheduleExpectStartDate);
                isValidPartitionCondition(result, partitionColumns);
            } else {
                log.error("read partition rule analysis error {}", type);
            }
        }
        return result;
    }


    public String odpsReadPartitionRuleAnalysis(String tableName, String inputRule, String scheduleExpectStartDate) {
        log.info("odps read partition rule analysis {} {}", tableName, inputRule);
        String result;
        if (inputRule.contains("(") || inputRule.contains(")")) {
            throw new IllegalArgumentException("inputRule format error");
        }
        result = odpsMaxPtReadPartitionRuleAnalysis(tableName, inputRule);
        log.info("(maxpt) odps read partition rule analysis result {}", result);
        result = odpsDateReadPartitionRuleAnalysis(tableName, result, scheduleExpectStartDate);
        log.info("(date) odps read partition rule analysis result {}", result);
        return result;
    }

    public String odpsMaxPtReadPartitionRuleAnalysis(String tableName, String inputRule) {
        log.info("odps max pt read partition rule analysis {} {}", tableName, inputRule);
        String result;
        result = inputRule.replaceAll(PartitionConstants.PARTITION_MAX_PT,
                ODPS_PARTITION_MAX_PT + "('" + tableName + "')");
        return result;
    }

    public String odpsDateReadPartitionRuleAnalysis(String tableName, String inputRule, String scheduleExpectStartDate) {
        log.info("odps date read partition rule analysis {} {}", tableName, inputRule);
        Pattern pattern = Pattern.compile(PARTITION_DATE_REG);
        Matcher matcher = pattern.matcher(inputRule);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String nValue = matcher.group(1);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate currentDate = LocalDate.now();
            if (scheduleExpectStartDate != null) {
                currentDate = LocalDate.parse(scheduleExpectStartDate, formatter);
            }
            LocalDate replacementDate;
            if (nValue != null) {
                int days = Integer.parseInt(nValue.substring(1));
                if (nValue.startsWith("+")) {
                    replacementDate = currentDate.plusDays(days);
                } else {
                    replacementDate = currentDate.minusDays(days);
                }
            } else {
                replacementDate = currentDate;
            }
            matcher.appendReplacement(result, "'" + replacementDate.format(formatter) + "'");
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private void isValidPartitionCondition(String partitionCondition, Set<String> partitionColumns) {
        String[] conditions = partitionCondition.split("(?i)\\s+(AND|OR)\\s+");
        for (String condition : conditions) {
            String[] keyValue = condition.trim().split("(?<=[^><!])=|(?<!=)=(?!=)|>=|<=|!=|>|<");
            if (keyValue.length != 2 || !partitionColumns.contains(keyValue[0].trim())) {
                throw new IllegalArgumentException("Invalid partition condition col must partition columns");
            }
            String sqlVal = keyValue[1];
            sqlVal = sqlVal.replaceAll("^'|'$", "");
            if (!sqlVal.contains("max_pt") && !sqlVal.matches("[a-zA-Z0-9_()]+")) {
                throw new IllegalArgumentException("Invalid partition condition value");
            }
        }
    }
}