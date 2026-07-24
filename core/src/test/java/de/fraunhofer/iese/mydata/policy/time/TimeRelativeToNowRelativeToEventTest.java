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

import de.fraunhofer.iese.mydata.solution.Timezone;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

/**
 * The Class TimeRelativeToNowRelativeToEventTest.
 */
class TimeRelativeToNowRelativeToEventTest {

  /** The tz. */
  Timezone tz;

  /** The zone component_id. */
  String zoneId = "Europe/Berlin";

  /** The event. */
  final ZonedDateTime event = TimeUtil.getZonedDateTime("01.02.2015 01:00:00", this.zoneId);

  /**
   * Wrong start test.
   *
   * @throws Exception the exception
   */
  @Test
  void WrongStartTest() throws Exception {
    final String startDate = "-1.*.* 25:00:00";
    assertThrows(DateTimeParseException.class, () -> {
      final ZonedDateTime tz1 = TimeUtil.getZonedDateTime(startDate, this.zoneId);
      // final long eventTime = eventDate.getTime(); does not matter
      // start date ="-1.*.* 25:00:00"
      // end date does not matter
    });
    // final long eventTime = eventDate.getTime(); does not matter
    // start date ="-1.*.* 25:00:00"
    // end date does not matter
  }

  /**
   * Wrong end test.
   */
  @Test
  void WrongEndTest() {
    final String endDate = "-1.12.2016 25:00:00";
    assertThrows(DateTimeParseException.class, () -> {
      final ZonedDateTime tz1 = TimeUtil.getZonedDateTime(endDate, this.zoneId);
    });
  }

  /**
   * Start after end test.
   *
   * @throws IllegalArgumentException the exception
   */
  @Test
  void StartAfterEndTest() throws Exception {
    final String startDate = "*.*.* *:*:*";// === relativ to now "now"
    final String endDate = "*.-5.* *:*:*";// relative to eventTime
    final ZonedDateTime tz1 = TimeUtil.parseExpressionToZonedDateTime(startDate,
        ZonedDateTime.now(ZoneId.of(this.zoneId)));
    final ZonedDateTime tz2 = TimeUtil.parseExpressionToZonedDateTime(endDate, this.event);
    final boolean result = tz1.isBefore(tz2);
    assertFalse(result);

  }

  /**
   * Start equals end test.
   *
   * @throws Exception the exception
   */
  @Test
  void StartEqualsEndTest() throws Exception {
    final String startDate = "*.*.* *:*:*";// === relativ to now "now"
    final String endDate = "*.*.* *:*:*"; // === relativ to event "that happened
                                         // at the same
    final ZonedDateTime now = ZonedDateTime.now(ZoneId.of(this.zoneId));
    final ZonedDateTime fakeEvent = now;
    final ZonedDateTime tz1 = TimeUtil.parseExpressionToZonedDateTime(startDate, now);
    final ZonedDateTime tz2 = TimeUtil.parseExpressionToZonedDateTime(endDate, fakeEvent);
    final boolean result = tz1.isEqual(tz2);
    assertTrue(result);
  }

  /**
   * Wrong format test.
   *
   * @throws Exception the exception
   */
  @Test
  void WrongFormatTest() throws Exception {
    final String startDate = "*.+2.2018 *:*:*";
    final String endDate = "*.*.* *:*:*";

    final ZonedDateTime tz1 = ZonedDateTime.now(ZoneId.of(this.zoneId));

    assertThrows(IllegalArgumentException.class, () -> {
      TimeUtil.parseExpressionToZonedDateTime(startDate, tz1);
    });

    TimeUtil.parseExpressionToZonedDateTime(endDate, this.event);

  }

}
