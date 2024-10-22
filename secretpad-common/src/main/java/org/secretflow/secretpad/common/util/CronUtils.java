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

import lombok.extern.slf4j.Slf4j;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.util.*;

/**
 * @author yutu
 * @date 2024/08/28
 */
@Slf4j
public class CronUtils {

    public static List<String> getMatchingDates(String cronExpression, String startTime, String endTime) {
        log.info("getMatchingDates, cronExpression:{}, startTime:{}, endTime:{}", cronExpression, startTime, endTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date nextValidTime;
        Date endDate;
        try {
            nextValidTime = sdf.parse(startTime);
            endDate = sdf.parse(endTime);
        } catch (Exception e) {
            log.error("getMatchingDates, startTime:{}, endTime:{}", startTime, endTime, e);
            throw new IllegalArgumentException("Invalid startTime format. must be yyyy-MM-dd HH:mm:ss", e);
        }
        CronExpression cron;
        try {
            cron = new CronExpression(cronExpression);
        } catch (ParseException e) {
            log.error("getMatchingDates, cronExpression:{}", cronExpression, e);
            throw new IllegalArgumentException("Invalid cron expression", e);
        }
        List<String> matchingDates = new ArrayList<>();
        Date now;
        while (nextValidTime.before(endDate) || nextValidTime.equals(endDate)) {
            nextValidTime = cron.getNextValidTimeAfter(nextValidTime);
            now = new Date();
            if (nextValidTime != null &&
                    (!nextValidTime.after(endDate) || nextValidTime.equals(endDate)) &&
                    (nextValidTime.after(now) || nextValidTime.equals(now))) {
                matchingDates.add(sdf.format(nextValidTime));
            } else {
                break;
            }
        }
        log.info("getMatchingDates, matchingDates:{}", matchingDates);
        return matchingDates;
    }

    /**
     * building a cron expression
     *
     * @param scheduleCycle scheduleCycle
     * @param scheduleDate  scheduleDate
     * @param scheduleTime  scheduleTime
     * @param startTime     startTime
     * @param endTime       endTime
     * @return cron expression List
     */
    public static List<String> buildCronExpression(
            String scheduleCycle,
            String scheduleDate,
            String scheduleTime,
            String startTime,
            String endTime
    ) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        Date parsedStartTime;
        Date parsedEndTime;
        try {
            parsedStartTime = sdf.parse(startTime);
            parsedEndTime = sdf.parse(endTime);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid time format. must be yyyy-MM-dd HH:mm:ss", e);
        }

        if (parsedEndTime.before(parsedStartTime)) {
            throw new IllegalArgumentException("End time must be greater than start time.");
        }

        String[] timeParts = scheduleTime.split(":");
        String second = "0";
        String minute = timeParts[1];
        String hour = timeParts[0];

        List<String> cronExpressions = new ArrayList<>();

        if (ScheduledConstants.SCHEDULED_CYCLE_DAY.equals(scheduleCycle)) {
            long startMillis = parsedStartTime.getTime();
            long endMillis = parsedEndTime.getTime();
            long oneDayMillis = 24 * 60 * 60 * 1000;

            for (long time = startMillis; time <= endMillis; time += oneDayMillis) {
                Date currentDay = new Date(time);
                String dateInCron = new SimpleDateFormat("dd").format(currentDay);
                cronExpressions.add(String.format("%s %s %s %s * ?", second, minute, hour, dateInCron));
            }
        } else if (ScheduledConstants.SCHEDULED_CYCLE_WEEK.equals(scheduleCycle)) {
            String[] weekDays = scheduleDate.split(",");
            for (String dayStr : weekDays) {
                DayOfWeek dayOfWeek = DayOfWeek.of(Integer.parseInt(dayStr));
                cronExpressions.add(String.format("%s %s %s ? * %s", second, minute, hour, dayOfWeek.name().substring(0, 3).toLowerCase(Locale.getDefault())));
            }
        } else if (ScheduledConstants.SCHEDULED_CYCLE_MONTH.equals(scheduleCycle)) {
            String[] monthDays = scheduleDate.split(",");
            for (String day : monthDays) {
                String cronExpression = String.format("%s %s %s %s * ?", second, minute, hour, "end".equals(day) ? "L" : day);
                cronExpressions.add(cronExpression);
            }
        } else {
            throw new IllegalArgumentException("Invalid schedule cycle: " + scheduleCycle);
        }
        log.info("buildCronExpression, cronExpressions:{}", cronExpressions);
        return cronExpressions;
    }
}