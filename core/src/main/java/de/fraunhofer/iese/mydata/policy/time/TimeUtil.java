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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Operations for ZonedDateTime, Zone and MyDataTimeExpressions
 */
@UtilityClass
public class TimeUtil {

  public static final Logger LOG = LoggerFactory.getLogger(TimeUtil.class);

  public static final String NOW = "now";

  public static final String YEAR = "year";

  public static final String MONTH = "month";

  public static final String DAY = "day";

  public static final String HOURS = "hours";

  public static final String MINUTES = "minutes";

  public static final String SECONDS = "seconds";

  public static final String THIS_MINUTE = "thisMinute";

  public static final String LAST_MINUTE = "lastMinute";

  public static final String THIS_HOUR = "thisHour";

  public static final String LAST_HOUR = "lastHour";

  public static final String TODAY = "today";

  public static final String YESTERDAY = "yesterday";

  public static final String THIS_MONTH = "thisMonth";

  public static final String LAST_MONTH = "lastMonth";

  public static final String THIS_YEAR = "thisYear";

  public static final String LAST_YEAR = "lastYear";

  public static final String ALWAYS = "always";

  public static final String THIS_WEEK = "thisWeek";

  public static final String LAST_WEEK = "lastWeek";

  public static final String THIS_SUN_WEEK = "thisSunWeek";

  public static final String LAST_SUN_WEEK = "lastSunWeek";

  /**
   * Parses the expression and returns the zoned date time.
   *
   * @param  expression               e.g., "10.10.2016 00:00:00", "*.-1.* *:*:*",...
   * @param  timeRelativeTo           the time relative to now/event
   * @return                          the zoned date time
   * @throws IllegalArgumentException the invalid format exception
   */
  public static ZonedDateTime parseExpressionToZonedDateTime(String expression,
      ZonedDateTime timeRelativeTo) {

    final Map<String, String> expressionMap = getExpressionMap(expression);

    final ZonedDateTime resultingZonedDateTime = getDynamicZonedDateTime(expressionMap,
        timeRelativeTo);

    // We have no information about the future in PDP
    final ZonedDateTime nowTime = getNow(timeRelativeTo.getZone());
    if (nowTime.isBefore(resultingZonedDateTime)) {
      return nowTime;
    }
    return resultingZonedDateTime;
  }

  public static TimeInterval getInterval(String fixedExpression, ZonedDateTime timeRelativeTo) {
    return TimeUtil.getInterval(fixedExpression, timeRelativeTo, DayOfWeek.MONDAY.toString());
  }

  public static TimeInterval getInterval(String fixedExpression, ZonedDateTime timeRelativeTo,
      String sDayOfWeek) {
    return FixedTimeUtil.getInterval(fixedExpression, timeRelativeTo, sDayOfWeek);
  }

  public static ZonedDateTime getDynamicZonedDateTime(Map<String, String> expressionMap,
      ZonedDateTime timeRelativeTo) {

    final TimeExpression timeExpression = splittMap(expressionMap);
    final Map<String, Integer> relativeChangeMap = timeExpression.getRelativeChangeMap();
    final Map<String, Integer> staticChangeMap = timeExpression.getConcreteMap();
    ZonedDateTime resultingZonedDateTime = timeRelativeTo;

    int year;
    int month;
    int day;
    int hour;
    int minute;
    int second;
    if (relativeChangeMap.containsKey(YEAR)) {
      resultingZonedDateTime = addYear(relativeChangeMap.get(YEAR), resultingZonedDateTime);
    }
    if (relativeChangeMap.containsKey(MONTH)) {
      resultingZonedDateTime = addMonth(relativeChangeMap.get(MONTH), resultingZonedDateTime);
    }
    if (relativeChangeMap.containsKey(DAY)) {
      resultingZonedDateTime = addDay(relativeChangeMap.get(DAY), resultingZonedDateTime);
    }
    if (relativeChangeMap.containsKey(HOURS)) {
      resultingZonedDateTime = addHours(relativeChangeMap.get(HOURS), resultingZonedDateTime);
    }
    if (relativeChangeMap.containsKey(MINUTES)) {
      resultingZonedDateTime = addMinutes(relativeChangeMap.get(MINUTES), resultingZonedDateTime);
    }
    if (relativeChangeMap.containsKey(SECONDS)) {
      resultingZonedDateTime = addSeconds(relativeChangeMap.get(SECONDS), resultingZonedDateTime);
    }
    year = resultingZonedDateTime.getYear();
    month = resultingZonedDateTime.getMonthValue();
    day = resultingZonedDateTime.getDayOfMonth();
    hour = resultingZonedDateTime.getHour();
    minute = resultingZonedDateTime.getMinute();
    second = resultingZonedDateTime.getSecond();

    if (staticChangeMap.containsKey(YEAR)) {
      year = staticChangeMap.get(YEAR);
    }
    if (staticChangeMap.containsKey(MONTH)) {
      month = staticChangeMap.get(MONTH);
    }
    if (staticChangeMap.containsKey(DAY)) {
      day = staticChangeMap.get(DAY);
    }
    if (staticChangeMap.containsKey(HOURS)) {
      hour = staticChangeMap.get(HOURS);
    }
    if (staticChangeMap.containsKey(MINUTES)) {
      minute = staticChangeMap.get(MINUTES);
    }
    if (staticChangeMap.containsKey(SECONDS)) {
      second = staticChangeMap.get(SECONDS);
    }
    return ZonedDateTime.of(year, month, day, hour, minute, second, 00, timeRelativeTo.getZone());
  }

  public static ZonedDateTime addYear(int value, ZonedDateTime aZonedDateTime) {
    return aZonedDateTime.plusYears(value);
  }

  public static ZonedDateTime addMonth(int value, ZonedDateTime aZonedDateTime) {
    return aZonedDateTime.plusMonths(value);
  }

  public static ZonedDateTime addDay(int value, ZonedDateTime aZonedDateTime) {
    return aZonedDateTime.plusDays(value);
  }

  public static ZonedDateTime addHours(int value, ZonedDateTime aZonedDateTime) {
    return aZonedDateTime.plusHours(value);
  }

  public static ZonedDateTime addMinutes(int value, ZonedDateTime aZonedDateTime) {
    return aZonedDateTime.plusMinutes(value);
  }

  public static ZonedDateTime addSeconds(int value, ZonedDateTime aZonedDateTime) {
    return aZonedDateTime.plusSeconds(value);
  }

  public static ZonedDateTime addYear(String expressionValue, ZonedDateTime aZonedDateTime) {
    if ("*".equals(expressionValue)) {
      return aZonedDateTime;
    } else if (expressionValue.contains("+") || expressionValue.contains("-")) {
      return aZonedDateTime.plusYears(Integer.valueOf(expressionValue));
    } else {
      // TODO:
      return null;
    }
  }

  public static Map<String, String> getExpressionMap(String expression) {
    final HashMap<String, String> map = new HashMap<>();
    try {
      final String[] dateAndTime = expression.split(" ");
      final String date = dateAndTime[0];
      final String time = dateAndTime[1];

      final String[] dateArray = date.split("\\.");
      final String[] timeArray = time.split(":");

      final String dayValue = dateArray[0];
      final String monthValue = dateArray[1];
      final String yearValue = dateArray[2];

      final String hoursValue = timeArray[0];
      final String minutesValue = timeArray[1];
      final String secondsValue = timeArray[2];

      map.put(YEAR, yearValue);
      map.put(MONTH, monthValue);
      map.put(DAY, dayValue);

      map.put(HOURS, hoursValue);
      map.put(MINUTES, minutesValue);
      map.put(SECONDS, secondsValue);

      // should not fail as the isValid already ran
      checkRelativeConcreteValidity(dayValue, monthValue, yearValue, hoursValue, minutesValue,
          secondsValue);

    } catch (final Exception e) {
      throw new IllegalArgumentException(e);
    }

    return map;
  }

  private static boolean checkRelativeConcreteValidity(final String dayValue,
      final String monthValue, final String yearValue, final String hoursValue,
      final String minutesValue, final String secondsValue) {
    boolean yearRelative = false; // TODO check missing or not needed?

    boolean yearConcrete = false;
    if (yearValue.contains("+") || yearValue.contains("-")) {
      yearRelative = true;
    } else if (yearValue.matches("[0-9]{4}")) {
      yearConcrete = true;
    }

    boolean monthRelative = false;
    boolean monthConcrete = false;
    if (monthValue.contains("+") || monthValue.contains("-")) {
      monthRelative = true;
    } else if (monthValue.matches("[0-9]{2}")) {
      monthConcrete = true;
    }

    boolean dayRelative = false;
    boolean dayConcrete = false;
    if (dayValue.contains("+") || dayValue.contains("-")) {
      dayRelative = true;
    } else if (dayValue.matches("[0-9]{2}")) {
      dayConcrete = true;
    }

    boolean hoursRelative = false;
    boolean hoursConcrete = false;
    if (hoursValue.contains("+") || hoursValue.contains("-")) {
      hoursRelative = true;
    } else if (hoursValue.matches("[0-9]{2}")) {
      hoursConcrete = true;
    }

    boolean minRelative = false;
    boolean minConcrete = false;
    if (minutesValue.contains("+") || minutesValue.contains("-")) {
      minRelative = true;
    } else if (minutesValue.matches("[0-9]{2}")) {
      minConcrete = true;
    }

    boolean secondsRelative = false;
    if (secondsValue.contains("+") || secondsValue.contains("-")) {
      secondsRelative = true;
    }

    if (secondsRelative
        && (minConcrete || hoursConcrete || dayConcrete || monthConcrete || yearConcrete)) {
      throw new IllegalArgumentException(
          "Seconds can not be relative if a higher time element is concrete");
    }

    if (minRelative && (hoursConcrete || dayConcrete || monthConcrete || yearConcrete)) {
      throw new IllegalArgumentException(
          "Minutes can not be relative if a higher time element is concrete");
    }

    if (hoursRelative && (dayConcrete || monthConcrete || yearConcrete)) {
      throw new IllegalArgumentException(
          "Hours can not be relative if a higher time element is concrete");
    }

    if (dayRelative && (monthConcrete || yearConcrete)) {
      throw new IllegalArgumentException(
          "Days can not be relative if a higher time element is concrete");
    }

    if (monthRelative && (yearConcrete)) {
      throw new IllegalArgumentException(
          "Month can not be relative if a higher time element is concrete");
    }

    return true;
  }

  public static TimeExpression splittMap(Map<String, String> expressionMap) {
    final Map<String, Integer> relativeChangeMap = new HashMap<>();
    final Map<String, Integer> concreteMap = new HashMap<>();
    for (final Entry<String, String> expressionEntry : expressionMap.entrySet()) {
      if ("*".equals(expressionEntry.getValue())) {
        // DO NOT PUT
      } else if (expressionEntry.getValue().contains("+")
          || expressionEntry.getValue().contains("-")) {
        relativeChangeMap.put(expressionEntry.getKey(),
            Integer.valueOf(expressionEntry.getValue()));
      } else {
        concreteMap.put(expressionEntry.getKey(), Integer.valueOf(expressionEntry.getValue()));
      }
    }

    final TimeExpression timeExpression = new TimeExpression();
    timeExpression.setRelativeChangeMap(relativeChangeMap);
    timeExpression.setConcreteMap(concreteMap);

    return timeExpression;
  }

  public static ZonedDateTime getNow(String timezoneid) {
    return getNow(ZoneId.of(timezoneid));
  }

  public static ZonedDateTime getNow(ZoneId zoneId) {
    final ZonedDateTime tmpZDT = ZonedDateTime.now(zoneId);
    return tmpZDT.minusNanos(tmpZDT.getNano());
  }

  public static ZonedDateTime transferToZone(ZonedDateTime zdt, String timeZoneId) {
    return transferToZone(zdt, ZoneId.of(timeZoneId));
  }

  public static ZonedDateTime transferToZone(ZonedDateTime zdt, ZoneId zoneId) {
    return zdt.withZoneSameInstant(zoneId);
  }

  public static String getHighestRelativeChangeUnit(TimeExpression timeExpression) {
    String result = "";
    if (timeExpression.hasReletiveChangeValues()) {
      if (timeExpression.relativeChangeMap.containsKey(YEAR)) {
        result = YEAR;
      } else if (timeExpression.relativeChangeMap.containsKey(MONTH)) {
        result = MONTH;
      } else if (timeExpression.relativeChangeMap.containsKey(DAY)) {
        result = DAY;
      } else if (timeExpression.relativeChangeMap.containsKey(HOURS)) {
        result = HOURS;
      } else if (timeExpression.relativeChangeMap.containsKey(MINUTES)) {
        result = MINUTES;
      } else if (timeExpression.relativeChangeMap.containsKey(SECONDS)) {
        result = SECONDS;
      }
    }
    return result;
  }

  public static String getLowestConcreteUnit(TimeExpression timeExpression) {
    String result = "";
    if (timeExpression.hasConcreteValues()) {
      if (timeExpression.concreteMap.containsKey(SECONDS)) {
        result = SECONDS;
      } else if (timeExpression.concreteMap.containsKey(MINUTES)) {
        result = MINUTES;
      } else if (timeExpression.concreteMap.containsKey(HOURS)) {
        result = HOURS;
      } else if (timeExpression.concreteMap.containsKey(DAY)) {
        result = DAY;
      } else if (timeExpression.concreteMap.containsKey(MONTH)) {
        result = MONTH;
      } else if (timeExpression.concreteMap.containsKey(YEAR)) {
        result = YEAR;
      }
    }
    return result;
  }

  public static int getValenceOfUnitString(String unit) {
    int result = -1;
    switch (unit) {
      case YEAR:
        result = 6;
        break;
      case MONTH:
        result = 5;
        break;
      case DAY:
        result = 4;
        break;
      case HOURS:
        result = 3;
        break;
      case MINUTES:
        result = 2;
        break;
      case SECONDS:
        result = 1;
        break;

      default:
        break;
    }
    return result;
  }

  public static boolean isValid(TimeExpression timeExpression) {
    final int valenceHighest = getValenceOfUnitString(getHighestRelativeChangeUnit(timeExpression));
    final int valenceLowest = getValenceOfUnitString(getLowestConcreteUnit(timeExpression));
    return (valenceHighest <= valenceLowest || (valenceHighest == -1 || valenceLowest == -1));
  }

  public static ZonedDateTime getZonedDateTime(String dateString, String timezoneid) {
    return getZonedDateTime(dateString, ZoneId.of(timezoneid));
  }

  public static ZonedDateTime getZonedDateTime(Timestamp ts, String timezoneid) {
    return ts.toLocalDateTime().atZone(ZoneId.of(timezoneid));
  }

  public static ZonedDateTime getZonedDateTime(String dateString, ZoneId zoneId) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    formatter = formatter.withZone(zoneId);
    return ZonedDateTime.parse(dateString, formatter);
  }

  public static long getMillisFromZonedDateTime(ZonedDateTime zdt, String targetZoneId) {
    final ZonedDateTime targetZonedDateTime = transferToZone(zdt, targetZoneId);
    return targetZonedDateTime.toInstant().toEpochMilli();
  }

}
