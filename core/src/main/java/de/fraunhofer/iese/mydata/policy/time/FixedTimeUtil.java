/*
 * =================================LICENSE_START=================================
 * MYDATA Control Technologies
 *
 * Copyright (C) 2016 - present Fraunhofer-Gesellschaft zur Foerderung der
 * angewandten Forschung e.V. acting on behalf of its Fraunhofer Institute
 * for Experimental Software Engineering (IESE)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =================================LICENSE_END===================================
 */

package de.fraunhofer.iese.mydata.policy.time;

import lombok.experimental.UtilityClass;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;

@UtilityClass
public class FixedTimeUtil {

  public static final String START = "start";

  public static final String END = "end";

  public static TimeInterval getInterval(String fixedExpression, ZonedDateTime timeRelativeTo,
      String sDayofWeek) {
    if (!isValidFixedExpression(fixedExpression)) {
      throw new IllegalArgumentException();
    }

    final TimeInterval timeIntervall = new TimeInterval();
    final ZonedDateTime startTime = getStartZonedDateTimeFromFixed(fixedExpression, timeRelativeTo,
        sDayofWeek);
    final ZonedDateTime endTime = getEndZonedDateTimeFromFixed(fixedExpression, timeRelativeTo,
        sDayofWeek);

    timeIntervall.setStartTime(startTime);
    timeIntervall.setEndTime(endTime);

    return timeIntervall;

  }

  public static TimeInterval getInterval(String fixedExpression, ZonedDateTime timeRelativeTo) {
    return getInterval(fixedExpression, timeRelativeTo, DayOfWeek.MONDAY.toString());
  }

  private static ZonedDateTime getEndZonedDateTimeFromFixed(String fixedExpression,
      ZonedDateTime timeRelativeTo, String sDayofWeek) {
    return getZonedDateTimeFromFixed(fixedExpression, timeRelativeTo, sDayofWeek, END);
  }

  private static ZonedDateTime getStartZonedDateTimeFromFixed(String fixedExpression,
      ZonedDateTime timeRelativeTo, String sDayofWeek) {
    return getZonedDateTimeFromFixed(fixedExpression, timeRelativeTo, sDayofWeek, START);
  }

  private static boolean isValidFixedExpression(String fixedExpression) {
    boolean bResult = false;
    switch (fixedExpression) {
      case TimeUtil.THIS_MINUTE:
      case TimeUtil.LAST_MINUTE:
      case TimeUtil.THIS_HOUR:
      case TimeUtil.LAST_HOUR:
      case TimeUtil.TODAY:
      case TimeUtil.YESTERDAY:
      case TimeUtil.THIS_MONTH:
      case TimeUtil.LAST_MONTH:
      case TimeUtil.THIS_YEAR:
      case TimeUtil.LAST_YEAR:
      case TimeUtil.THIS_WEEK:
      case TimeUtil.LAST_WEEK:
      case TimeUtil.THIS_SUN_WEEK:
      case TimeUtil.LAST_SUN_WEEK:
      case TimeUtil.ALWAYS:
        bResult = true;
        break;

      default:
        bResult = false;
        break;
    }
    return bResult;
  }

  public static ZonedDateTime getZonedDateTimeFromFixed(String fixedExpression,
      ZonedDateTime timeRelativeTo, String sDayofWeek, String startOrEnd) {
    ZonedDateTime resultingZonedDateTime = ZonedDateTime.now(timeRelativeTo.getZone());
    switch (fixedExpression) {
      case TimeUtil.THIS_MINUTE:
        if (START.equals(startOrEnd)) {
          resultingZonedDateTime = TimeUtil.parseExpressionToZonedDateTime("*.*.* *:*:00",
              timeRelativeTo);
        }
        if (END.equals(startOrEnd)) {
          resultingZonedDateTime = timeRelativeTo;
        }
        break;
      case TimeUtil.LAST_MINUTE:
        if (START.equals(startOrEnd)) {
          resultingZonedDateTime = TimeUtil.parseExpressionToZonedDateTime("*.*.* *:-1:00",
              timeRelativeTo);
        }
        if (END.equals(startOrEnd)) {
          resultingZonedDateTime = TimeUtil.parseExpressionToZonedDateTime("*.*.* *:-1:59",
              timeRelativeTo);
        }
        break;
      case TimeUtil.THIS_HOUR:
        if (START.equals(startOrEnd)) {
          resultingZonedDateTime = TimeUtil.parseExpressionToZonedDateTime("*.*.* *:00:00",
              timeRelativeTo);
        }
        if (END.equals(startOrEnd)) {
          resultingZonedDateTime = timeRelativeTo;
        }
        break;
      case TimeUtil.LAST_HOUR:
        if (START.equals(startOrEnd)) {
          resultingZonedDateTime = TimeUtil.parseExpressionToZonedDateTime("*.*.* -1:00:00",
              timeRelativeTo);
        }
        if (END.equals(startOrEnd)) {
          resultingZonedDateTime = TimeUtil.parseExpressionToZonedDateTime("*.*.* -1:59:59",
              timeRelativeTo);
        }
        break;
      case TimeUtil.TODAY:
        if (START.equals(startOrEnd)) {
          resultingZonedDateTime = TimeUtil.parseExpressionToZonedDateTime("*.*.* 00:00:00",
              timeRelativeTo);
        }
        if (END.equals(startOrEnd)) {
          resultingZonedDateTime = timeRelativeTo;
        }
        break;
      case TimeUtil.YESTERDAY:
        if (START.equals(startOrEnd)) {
          resultingZonedDateTime = TimeUtil.parseExpressionToZonedDateTime("-1.*.* 00:00:00",
              timeRelativeTo);
        }
        if (END.equals(startOrEnd)) {
          resultingZonedDateTime = TimeUtil.parseExpressionToZonedDateTime("-1.*.* 23:59:59",
              timeRelativeTo);
        }
        break;
      case TimeUtil.THIS_MONTH:
        if (START.equals(startOrEnd)) {
          resultingZonedDateTime = TimeUtil.parseExpressionToZonedDateTime("01.*.* 00:00:00",
              timeRelativeTo);
        }
        if (END.equals(startOrEnd)) {
          resultingZonedDateTime = timeRelativeTo;
        }
        break;
      case TimeUtil.LAST_MONTH:
        if (START.equals(startOrEnd)) {
          resultingZonedDateTime = TimeUtil.parseExpressionToZonedDateTime("01.-1.* 00:00:00",
              timeRelativeTo);
        }
        if (END.equals(startOrEnd)) {
          resultingZonedDateTime = getEndofLastMonth(timeRelativeTo);
        }
        break;
      case TimeUtil.THIS_YEAR:
        if (START.equals(startOrEnd)) {
          resultingZonedDateTime = TimeUtil.parseExpressionToZonedDateTime("01.01.* 00:00:00",
              timeRelativeTo);
        }
        if (END.equals(startOrEnd)) {
          resultingZonedDateTime = timeRelativeTo;
        }
        break;
      case TimeUtil.LAST_YEAR:
        if (START.equals(startOrEnd)) {
          resultingZonedDateTime = TimeUtil.parseExpressionToZonedDateTime("01.01.-1 00:00:00",
              timeRelativeTo);
        }
        if (END.equals(startOrEnd)) {
          resultingZonedDateTime = TimeUtil.parseExpressionToZonedDateTime("31.12.-1 23:59:59",
              timeRelativeTo);
        }
        break;
      case TimeUtil.THIS_WEEK:
        if (START.equals(startOrEnd)) {
          resultingZonedDateTime = getStartOfWeek(timeRelativeTo, sDayofWeek);
        }
        if (END.equals(startOrEnd)) {
          resultingZonedDateTime = timeRelativeTo;
        }
        break;
      case TimeUtil.LAST_WEEK:
        if (START.equals(startOrEnd)) {
          resultingZonedDateTime = getStartOfLastWeek(timeRelativeTo, sDayofWeek);
        }
        if (END.equals(startOrEnd)) {
          resultingZonedDateTime = getEndOfLastWeek(timeRelativeTo, sDayofWeek);
        }
        break;
      case TimeUtil.ALWAYS:
        if (START.equals(startOrEnd)) {
          resultingZonedDateTime = ZonedDateTime.ofInstant(Instant.EPOCH, timeRelativeTo.getZone());
        }
        if (END.equals(startOrEnd)) {
          resultingZonedDateTime = timeRelativeTo;
        }
        break;
      default:
        break;
    }
    return resultingZonedDateTime;

  }

  private static ZonedDateTime getStartOfWeek(ZonedDateTime timeRelativeTo, String sDayofWeek) {
    final DayOfWeek dayOfWeekStart = DayOfWeek.valueOf(sDayofWeek.toUpperCase());
    final DayOfWeek timeDayOfWeek = timeRelativeTo.getDayOfWeek();

    int daysToRemoveFromDate = timeDayOfWeek.getValue() - dayOfWeekStart.getValue();
    daysToRemoveFromDate = Math.floorMod(daysToRemoveFromDate, 7);

    final ZonedDateTime tmpZDT = timeRelativeTo.minusDays(daysToRemoveFromDate);

    return ZonedDateTime.of(tmpZDT.getYear(), tmpZDT.getMonthValue(), tmpZDT.getDayOfMonth(), 0, 0,
        0, 0, tmpZDT.getZone());
  }

  private static ZonedDateTime getEndOfLastWeek(ZonedDateTime timeRelativeTo, String sDayofWeek) {
    return getStartOfWeek(timeRelativeTo, sDayofWeek).minusSeconds(1);
  }

  private static ZonedDateTime getStartOfLastWeek(ZonedDateTime timeRelativeTo, String sDayofWeek) {
    return getStartOfWeek(timeRelativeTo, sDayofWeek).minusWeeks(1);
  }

  private static ZonedDateTime getLastDayofMonthDate(ZonedDateTime timeRelativeTo) {
    return timeRelativeTo.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
  }

  private static ZonedDateTime getEndofLastMonth(ZonedDateTime timeRelativeTo) {
    final ZonedDateTime zdtLastDay = getLastDayofMonthDate(timeRelativeTo);
    return ZonedDateTime.of(zdtLastDay.getYear(), zdtLastDay.getMonthValue(),
        zdtLastDay.getDayOfMonth(), 23, 59, 59, 0, zdtLastDay.getZone());
  }

}
