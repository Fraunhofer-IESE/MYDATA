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
import java.time.format.DateTimeParseException;
import java.util.Date;

/**
 * The Class TimeRelativeToEventRelativeToNowTest.
 */
class TimeRelativeToEventRelativeToNowTest {

  /** The zone component_id. */
  String zoneId = "Europe/Berlin";

  /** The event date. */
  Date eventDate = new Date();

  /**
   * Wrong start test.
   *
   * @throws Exception the exception
   */
  @Test
  void WrongStartTest() throws Exception {
    final String eventB = "10.02.2017 00:00:00";
    final ZonedDateTime startEventZDT = TimeUtil.getZonedDateTime(eventB, this.zoneId);
    assertThrows(DateTimeException.class, () -> {
      final ZonedDateTime startRelativetoEventTime = TimeUtil
          .parseExpressionToZonedDateTime("+1.*.* 25:00:00", startEventZDT);

    });

  }

  /**
   * Wrong end test.
   *
   * @throws Exception the exception
   */
  @Test
  void WrongEndTest() throws Exception {
    final String eventB = "10.02.2017 00:00:00";
    final ZonedDateTime startEventZDT = TimeUtil.getZonedDateTime(eventB, this.zoneId);
    TimeUtil.parseExpressionToZonedDateTime("*.*.-5 *:*:*", startEventZDT);
    final String fakeNowTime = "42.-1.2016 00:00:00";
    assertThrows(DateTimeParseException.class, () -> {
      TimeUtil.getZonedDateTime(fakeNowTime, this.zoneId);

    });

  }

  /**
   * Start after end test.
   *
   * @throws Exception the exception
   */
  @Test
  void StartAfterEndTest() throws Exception {
    // start date ="+6.*.* *:*:*" relativ to "event"
    // end date ="*.-5.* *:*:/" relativ to now

    final String eventB = "10.02.2017 00:00:00";
    final ZonedDateTime startEventZDT = TimeUtil.getZonedDateTime(eventB, this.zoneId);
    final ZonedDateTime startRelativetoEventTime = TimeUtil
        .parseExpressionToZonedDateTime("*.*.+2 *:*:*", startEventZDT);

    final ZonedDateTime nowZDT = TimeUtil.getNow(this.zoneId);
    final ZonedDateTime endRelativetoNowTime = TimeUtil
        .parseExpressionToZonedDateTime("*.*.-10 *:-8:*", nowZDT);

    assertFalse(startRelativetoEventTime.isBefore(endRelativetoNowTime));
  }

  /**
   * Start equals end test.
   *
   * @throws Exception the exception
   */
  @Test
  void StartEqualsEndTest() throws Exception {
    // start date ="*.*.* *:*:*" === event time
    // end date ="*.*.* *:*:*" === now
    // time -- is it possible?"

    final String eventB = "10.02.2017 00:00:00";
    final ZonedDateTime startEventZDT = TimeUtil.getZonedDateTime(eventB, this.zoneId);
    final ZonedDateTime startRelativetoEventTime = TimeUtil
        .parseExpressionToZonedDateTime("*.*.* *:*:*", startEventZDT);

    final String fakeNowTime = "10.02.2017 00:00:00";
    final ZonedDateTime fakeNowZDT = TimeUtil.getZonedDateTime(fakeNowTime, this.zoneId);
    final ZonedDateTime endFakeTime = TimeUtil.parseExpressionToZonedDateTime("*.*.* *:*:*",
        fakeNowZDT);

    assertTrue(startRelativetoEventTime.isEqual(endFakeTime));
  }

  /**
   * Wrong format test.
   *
   * @throws Exception the exception
   */
  @Test
  void WrongFormatTest() throws Exception {
    final String eventB = "-1.2017 14:00:00";
    assertThrows(DateTimeParseException.class, () -> {
      TimeUtil.getZonedDateTime(eventB, this.zoneId);
    });
  }

}
