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

package org.secretflow.secretpad.common.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * @author yutu
 * @date 2023/08/23
 */
@Slf4j
public class DateTimesTest {

    @Test
    public void test() {
        LocalDateTime utc = LocalDateTime.ofInstant(Instant.parse("2023-08-23T10:07:09Z"), ZoneId.of("Asia/Shanghai"));
        String s = DateTimes.toRfc3339(utc);
        log.info("s:{}", s);
        Assertions.assertThrows(DateTimeParseException.class, () -> DateTimes.toLocalDateTime(s));
        LocalDateTime localDateTime = DateTimes.toLocalDateTime("2023-08-23 10:07:09");
        log.info("localDateTime:{}", localDateTime);
        Assertions.assertNull(DateTimes.toLocalDateTimeString(null));
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime localDateTime1 = now.minusHours(8);
        String format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(localDateTime1);
        Assertions.assertEquals(format, DateTimes.toLocalDateTimeString(now));
    }
}