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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.time.DateTimeException;
import java.time.ZonedDateTime;

/**
 * The Class TimeConcreteRelativeToEventTest.
 */
class TimeConcreteRelativeToEventTest {

  /** The zone component_id. */
  String zoneId = "Europe/Berlin";

  /**
   * This example ends one month before the first/last time event B occurred. First time that B
   * occurred is "10.12.2016 00:00:00". This is a valid time interval.
   *
   * @throws Exception the exception
   */
  @Test
  void startCorrectConcreteTimeAndEndsCorrectRelativeTimeToEvent() throws Exception {
    // <start time="10.10.2016 00:00:00" />
    // <end time="*.-1.* *:*:*">
    // <eventOccurrence event="B" mode="FIRST/LAST"/>
    // </end>

    final String ConcreteTime = "10.10.2016 00:00:00";
    final ZonedDateTime startConcreteZDT = TimeUtil.getZonedDateTime(ConcreteTime, this.zoneId);
    final ZonedDateTime startConcreteTime = TimeUtil.parseExpressionToZonedDateTime(ConcreteTime,
        startConcreteZDT);

    final String eventB = "10.12.2016 00:00:00";
    final ZonedDateTime EventZDT = TimeUtil.getZonedDateTime(eventB, this.zoneId);
    final ZonedDateTime endRelativetoEventTime = TimeUtil
        .parseExpressionToZonedDateTime("*.-1.* *:*:*", EventZDT);

    assertTrue(startConcreteTime.isBefore(endRelativetoEventTime));
  }

  /**
   * This example ends two years before the first/last time event B occurred. First time that B
   * occurred is "10.02.2017 00:00:00". Then end time is before the concrete start time. This is an
   * invalid time interval.
   *
   * @throws Exception the exception
   */
  @Test
  void startCorrectConcreteTimeAndEndsPastRelativeTimeToEvent() throws Exception {
    // <start time="10.12.2016 00:00:00" />
    // <end time="*.*.-2 *:*:*">
    // <eventOccurrence event="B" mode="FIRST/LAST"/>
    // </end>

    final String ConcreteTime = "10.12.2016 00:00:00";
    final ZonedDateTime startConcreteZDT = TimeUtil.getZonedDateTime(ConcreteTime, this.zoneId);
    final ZonedDateTime startConcreteTime = TimeUtil.parseExpressionToZonedDateTime(ConcreteTime,
        startConcreteZDT);

    final String eventB = "10.02.2017 00:00:00";
    final ZonedDateTime EventZDT = TimeUtil.getZonedDateTime(eventB, this.zoneId);
    final ZonedDateTime endRelativetoEventTime = TimeUtil
        .parseExpressionToZonedDateTime("*.*.-2 *:*:*", EventZDT);

    assertFalse(startConcreteTime.isBefore(endRelativetoEventTime));
  }

  /**
   * This example ends 8 minutes before the first time event B occurred. First time that B occurred
   * is "10.02.2017 00:00:00". Start Concrete time is invalid. This is a valid time interval.
   *
   * @throws Exception the exception
   */
  @Test
  void startWrongConcreteTimeAndEndsRelativeTimeToEvent() throws Exception {
    final String ConcreteTime = "30.02.2016 00:00:00";
    final ZonedDateTime startConcreteZDT = TimeUtil.getZonedDateTime(ConcreteTime, this.zoneId);
    final String eventB = "10.02.2017 00:00:00";
    final ZonedDateTime EventZDT = TimeUtil.getZonedDateTime(eventB, this.zoneId);
    assertThrows(DateTimeException.class, () -> {
      TimeUtil.parseExpressionToZonedDateTime(ConcreteTime, startConcreteZDT);
    });
    TimeUtil.parseExpressionToZonedDateTime("*.*.* *:-8:*", EventZDT);
  }

  /**
   * This example ends two years after the first time event B occurred. First time that B occurred
   * is "10.02.2017 00:00:00". PDP must evaluate the time up to now. This is a valid time interval.
   *
   * @throws Exception the exception
   */
  @Test
  void startConcreteTimeAndEndsNearFutureRelativeTimeToEvent() throws Exception {
    // <start time="28.02.2016 00:00:00" />
    // <end time="*.*.+2 *:*:*">
    // <eventOccurrence event="B" mode="FIRST"/>
    // </end>

    final String ConcreteTime = "28.02.2016 00:00:00";
    final ZonedDateTime startConcreteZDT = TimeUtil.getZonedDateTime(ConcreteTime, this.zoneId);
    final ZonedDateTime startConcreteTime = TimeUtil.parseExpressionToZonedDateTime(ConcreteTime,
        startConcreteZDT);

    final String eventB = "10.02.2017 00:00:00";
    final ZonedDateTime EventZDT = TimeUtil.getZonedDateTime(eventB, this.zoneId);
    final ZonedDateTime endRelativetoEventTime = TimeUtil
        .parseExpressionToZonedDateTime("*.*.+2 *:*:*", EventZDT);

    assertTrue(startConcreteTime.isBefore(endRelativetoEventTime));
  }

  /**
   * This example ends 5000 years after the first time event B occurred. First time that B occurred
   * is "10.02.2017 00:00:00". PDP must evaluate the time up to now. This is a valid time interval.
   *
   * @throws Exception the exception
   */
  @Test
  void startConcreteTimeAndEndsFarFutureRelativeTimeToEvent() throws Exception {
    // <start time="28.02.2016 00:00:00" />
    // <end time="*.*.+5000 *:*:*">
    // <eventOccurrence event="B" mode="FIRST"/>
    // </end>

    final String ConcreteTime = "28.02.2016 00:00:00";
    final ZonedDateTime startConcreteZDT = TimeUtil.getZonedDateTime(ConcreteTime, this.zoneId);
    final ZonedDateTime startConcreteTime = TimeUtil.parseExpressionToZonedDateTime(ConcreteTime,
        startConcreteZDT);

    final String eventB = "10.02.2017 00:00:00";
    final ZonedDateTime EventZDT = TimeUtil.getZonedDateTime(eventB, this.zoneId);
    final ZonedDateTime endRelativetoEventTime = TimeUtil
        .parseExpressionToZonedDateTime("*.*.+5000 *:*:*", EventZDT);

    assertTrue(startConcreteTime.isBefore(endRelativetoEventTime));
  }
}
