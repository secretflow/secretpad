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

package org.secretflow.secretpad.common.util;

import org.secretflow.secretpad.common.constant.ScheduledConstants;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author yutu
 * @date 2024/08/28
 */
public class CronUtilsTest {


    @Test
    public void testBuildCronExpressionForDay() {
        List<String> cronExpressions = CronUtils.buildCronExpression(
                ScheduledConstants.SCHEDULED_CYCLE_DAY,
                "",
                "12:00:00",
                "2024-09-01 00:00:00",
                "2024-09-10 23:59:59"
        );
        assertEquals(10, cronExpressions.size());
    }

    @Test
    public void testBuildCronExpressionForWeek() {
        List<String> cronExpressions = CronUtils.buildCronExpression(
                ScheduledConstants.SCHEDULED_CYCLE_WEEK,
                "1,2,3,4,5,6,7",
                "12:00:00",
                "2024-09-01 00:00:00",
                "2024-09-10 23:59:59"
        );
        assertEquals(7, cronExpressions.size());
    }

    @Test
    public void testBuildCronExpressionForMonth() {
        List<String> cronExpressions = CronUtils.buildCronExpression(
                ScheduledConstants.SCHEDULED_CYCLE_MONTH,
                "15,30,end",
                "12:00:00",
                "2024-09-01 00:00:00",
                "2024-10-10 23:59:59"
        );
        assertEquals(3, cronExpressions.size());
    }

    @Test
    public void testBuildCronExpression() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> CronUtils.buildCronExpression(
                ScheduledConstants.SCHEDULED_CYCLE_MONTH,
                "15,30,end",
                "12:00:00",
                "2024-09-01 ",
                "2024-10-10 23:59:59"
        ));
        Assertions.assertThrows(IllegalArgumentException.class, () -> CronUtils.buildCronExpression(
                ScheduledConstants.SCHEDULED_CYCLE_MONTH,
                "15,30,end",
                "12:00:00",
                "2024-11-01 00:00:00",
                "2024-10-10 23:59:59"
        ));
        String start = DateTimes.localDateTimeString(LocalDateTime.now().plusMinutes(1));
        String end = DateTimes.localDateTimeString(LocalDateTime.now().plusDays(10));
        Assertions.assertThrows(IllegalArgumentException.class, () -> CronUtils.buildCronExpression(
                "test",
                "15,30,end",
                "12:00:00",
                start,
                end
        ));
    }

    @Test
    void test() {
        String start = DateTimes.localDateTimeString(LocalDateTime.now().plusMinutes(1));
        String end = DateTimes.localDateTimeString(LocalDateTime.now().plusDays(10));
        CronUtils.getMatchingDates("0 00 12 ? * mon", start, end);
        Assertions.assertThrows(IllegalArgumentException.class, () -> CronUtils.getMatchingDates("0 0 0 ? * mon", "2024", end));
        CronUtils.getMatchingDates("0 0 0 ? * mon", "2024-01-01 00:00:00", "2024-01-01 20:00:00");
        Assertions.assertThrows(IllegalArgumentException.class, () -> CronUtils.getMatchingDates("asdkdasjkdh", "2024-01-01 00:00:00", end));
    }
}