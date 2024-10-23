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

import org.apache.commons.lang3.ObjectUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

/**
 * DateTime utils
 *
 * @author yansi
 * @date 2023/5/10
 */
public class DateTimes {

    private static final DateTimeFormatter RFC_3339_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss+08:00")
            .withZone(ZoneId.of("Asia/Shanghai"));

    private static final DateTimeFormatter LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter LOCAL_DATE_TIME_FORMATTER_NO_DELIMITER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * Convert localDateTime to string with GMT+8
     *
     * @return String
     */
    public static String toRfc3339(LocalDateTime ldt) {
        if (ObjectUtils.isEmpty(ldt)) {
            return null;
        }
        return RFC_3339_FORMATTER.format(ldt);
    }

    /**
     * Return string from now localDateTime with GMT+8
     *
     * @return String yyyyMMddHHmmss
     */
    public static String localTimeNoDelimiter() {
        return LOCAL_DATE_TIME_FORMATTER_NO_DELIMITER.format(LocalDateTime.now());
    }

    /**
     * Return string from now localDateTime with GMT+8
     *
     * @return String
     */
    public static String nowRfc3339() {
        return RFC_3339_FORMATTER.format(LocalDateTime.now());
    }

    /**
     * Convert string to long with GMT+8
     *
     * @param rfc3339
     * @return long
     */
    public static long rfc3339ToLong(String rfc3339) {
        return RFC_3339_FORMATTER.parse(rfc3339).getLong(ChronoField.INSTANT_SECONDS);
    }

    /**
     * Convert string to localDateTime
     *
     * @param rfc3339
     * @return LocalDateTime
     */
    public static LocalDateTime utcFromRfc3339(String rfc3339) {
        return LocalDateTime.ofInstant(Instant.parse(rfc3339), ZoneId.of("UTC"));
    }

    /**
     * Convert string to localDateTime by zone eight
     *
     * @param rfc3339
     * @return LocalDateTime
     */
    public static LocalDateTime eightUtcFromRfc3339(String rfc3339) {
        TemporalAccessor parse = RFC_3339_FORMATTER.parse(rfc3339);
        return LocalDateTime.from(parse);
    }

    /**
     * Convert localDateTime to string
     *
     * @param ldt
     * @return String
     */
    public static String localDateTimeString(LocalDateTime ldt) {
        return LOCAL_DATE_TIME_FORMATTER.format(ldt);
    }


    /**
     * Convert rfc3339 string time to ctt string time
     *
     * @param rfc3339 rfc3339 string time
     * @return ctt string time
     */
    public static String cttFromRfc3339(String rfc3339) {
        LocalDateTime utc = LocalDateTime.ofInstant(Instant.parse(rfc3339), ZoneId.of("Asia/Shanghai"));
        return toRfc3339(utc);
    }

    /**
     * Convert rfc3339 string time to ctt string time
     *
     * @param rfc3339 rfc3339 string time
     * @return Z->+08:00
     */
    public static String rfc3339ToGmt8(String rfc3339) {
        return rfc3339.replace("Z", "+08:00");
    }

    public static LocalDateTime toLocalDateTime(String time) {
        return LocalDateTime.parse(time, LOCAL_DATE_TIME_FORMATTER);
    }

    public static String toLocalDateTimeString(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        localDateTime = localDateTime.minusHours(8);
        return LOCAL_DATE_TIME_FORMATTER.format(localDateTime);
    }
}
