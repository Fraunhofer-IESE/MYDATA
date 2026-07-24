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
 * The Class TimeRelativeToEventConcreteTest.
 */
class TimeRelativeToEventConcreteTest {

  /** The zone component_id. */
  String zoneId = "Europe/Berlin";

  /**
   * Started 8 months before the first/last occurrence of B. First/Last time
   * that B occurred is "10.02.2017 00:00:00". This is a valid time interval
   *
   * @throws Exception the exception
   */
  @Test
  void startCorrectRelativeTimeToEventAndEndsCorrectConcreteTime() throws Exception {

    // <start time="*.-8.* *:*:*" />
    // <eventOccurrence event="B" mode="FIRST/LAST"/>
    // </start>
    // <end time="12.01.2018 14:00:00" />

    final String eventB = "10.02.2017 00:00:00";
    final ZonedDateTime EventZDT = TimeUtil.getZonedDateTime(eventB, zoneId);
    final ZonedDateTime startRelativetoEventTime = TimeUtil.parseExpressionToZonedDateTime("*.-8.* *:*:*", EventZDT);

    final String ConcreteTime = "12.01.2018 14:00:00";
    final ZonedDateTime endConcreteZDT = TimeUtil.getZonedDateTime(ConcreteTime, zoneId);
    final ZonedDateTime endConcreteTime = TimeUtil.parseExpressionToZonedDateTime(ConcreteTime, endConcreteZDT);

    assertTrue(startRelativetoEventTime.isBefore(endConcreteTime));

  }

  /**
   * Started 8 months after the first/last occurrence of B. First/Last time that
   * B occurred is "10.02.2017 00:00:00". This is an invalid time interval
   *
   * @throws Exception the exception
   */
  @Test
  void startRelativeTimeToEventIsAfterEndConcreteTime() throws Exception {
    // <start time="*.+8.* *:*:*" />
    // <eventOccurrence event="B" mode="FIRST/LAST"/>
    // </start>
    // <end time="12.04.2017 14:00:00" />

    final String eventB = "10.02.2017 00:00:00";
    final ZonedDateTime EventZDT = TimeUtil.getZonedDateTime(eventB, zoneId);
    final ZonedDateTime startRelativetoEventTime = TimeUtil.parseExpressionToZonedDateTime("*.+8.* *:*:*", EventZDT);

    final String ConcreteTime = "12.04.2017 14:00:00";
    final ZonedDateTime endConcreteZDT = TimeUtil.getZonedDateTime(ConcreteTime, zoneId);
    final ZonedDateTime endConcreteTime = TimeUtil.parseExpressionToZonedDateTime(ConcreteTime, endConcreteZDT);

    assertFalse(startRelativetoEventTime.isBefore(endConcreteTime));
  }

  /**
   * Started 3000 years before the first/last occurrence of B. First/Last time
   * that B occurred is "10.02.2017 00:00:00".
   *
   * @throws Exception the exception
   */
  @Test
  void startWrongRelativeTimeToEventAndEndConcreteTime() throws Exception {
    // <start time="*.*.-3000 *:*:*" />
    // <eventOccurrence event="B" mode="FIRST/LAST"/>
    // </start>
    // <end time="12.01.2018 14:00:00" />

    final String eventB = "10.02.2017 00:00:00";
    final ZonedDateTime EventZDT = TimeUtil.getZonedDateTime(eventB, zoneId);
    final ZonedDateTime startRelativetoEventTime = TimeUtil.parseExpressionToZonedDateTime("*.*.-3000 *:*:*", EventZDT);

    final String ConcreteTime = "12.01.2018 14:00:00";
    final ZonedDateTime endConcreteZDT = TimeUtil.getZonedDateTime(ConcreteTime, zoneId);
    final ZonedDateTime endConcreteTime = TimeUtil.parseExpressionToZonedDateTime(ConcreteTime, endConcreteZDT);

    assertTrue(startRelativetoEventTime.isBefore(endConcreteTime));
  }

  /**
   * Started 8 months before the first/last occurrence of B. First/Last time
   * that B occurred is "10.02.2017 00:00:00". This is an invalid time interval
   *
   * @throws Exception the exception
   */
  @Test
  void startRelativeTimeToEventAndEndWrongConcreteTime() throws Exception {
    final String eventB = "10.02.2017 00:00:00";
    final ZonedDateTime EventZDT = TimeUtil.getZonedDateTime(eventB, zoneId);
    final ZonedDateTime startRelativetoEventTime = TimeUtil.parseExpressionToZonedDateTime("*.-8.* *:*:*", EventZDT);
    final String ConcreteTime = "30.02.2018 14:00:00";
    final ZonedDateTime endConcreteZDT = TimeUtil.getZonedDateTime(ConcreteTime, zoneId);
    assertThrows(DateTimeException.class, () -> {
      final ZonedDateTime endConcreteTime = TimeUtil.parseExpressionToZonedDateTime(ConcreteTime, endConcreteZDT);
    });
  }

  /**
   * Started 8 months before the first/last occurrence of B. First/Last time
   * that B occurred is "10.02.2017 00:00:00". This is a valid time interval
   * when we assume the end time equal to "now".
   *
   * @throws Exception the exception
   */
  @Test
  void startRelativeTimeToEventAndNotInLifetimeEndConcreteTime() throws Exception {
    // <start time="*.-8.* *:*:*" />
    // <eventOccurrence event="B" mode="FIRST/LAST"/>
    // </start>
    // <end time="28.02.5018 14:00:00" />

    final String eventB = "10.02.2017 00:00:00";
    final ZonedDateTime EventZDT = TimeUtil.getZonedDateTime(eventB, zoneId);
    final ZonedDateTime startRelativetoEventTime = TimeUtil.parseExpressionToZonedDateTime("*.-8.* *:*:*", EventZDT);

    final String ConcreteTime = "28.02.5018 14:00:00";
    final ZonedDateTime endConcreteZDT = TimeUtil.getZonedDateTime(ConcreteTime, zoneId);
    final ZonedDateTime endConcreteTime = TimeUtil.parseExpressionToZonedDateTime(ConcreteTime, endConcreteZDT);

    assertTrue(startRelativetoEventTime.isBefore(endConcreteTime));
  }

}
