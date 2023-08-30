/*
 *   Copyright 2023 Ant Group Co., Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.secretflow.secretpad.web.utils;

import org.apache.commons.lang3.RandomUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Data utils
 *
 * @author cml
 * @date 2023/07/28
 * @since 4.3
 */
public class DataUtils {
    public static long fakeLong() {
        return fakeLong(10000000) + 10000000;
    }

    public static long fakeLong(long range) {
        return RandomUtils.nextLong(0, range);
    }

    public static int fakeInt() {
        return fakeInt(100);
    }

    public static int fakeInt(int range) {
        return RandomUtils.nextInt(0, range);
    }

    public static Date fakeDate(int atMost, TimeUnit timeUnit) {
        Date now = new Date();
        Date aBitEarlierThanNow = new Date(now.getTime() - 1000L);
        return fakeDate(atMost, timeUnit, aBitEarlierThanNow);
    }


    public static Date fakeDate(int atMost, TimeUnit unit, Date referenceDate) {
        long upperBound = unit.toMillis(atMost);
        long futureMillis = referenceDate.getTime();
        futureMillis -= 1L + fakeLong(upperBound - 1L);
        return new Date(futureMillis);
    }

    public static String fakeHex(int length) {
        char[] hexValues = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        StringBuilder hexString = new StringBuilder();

        for (int i = 0; i < length; ++i) {
            hexString.append(hexValues[fakeInt(hexValues.length)]);
        }
        return hexString.toString();
    }
}
