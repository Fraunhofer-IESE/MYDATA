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
 * The Class TimeRelativeToNowConcreteTest.
 */
class TimeRelativeToNowConcreteTest {

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
    final ZonedDateTime nowZDT = TimeUtil.getNow(this.zoneId);
    final String ConcreteTime = "28.05.2018 00:00:00";
    final ZonedDateTime endConcreteZDT = TimeUtil.getZonedDateTime(ConcreteTime, this.zoneId);
    assertThrows(DateTimeException.class, () -> {
      TimeUtil.parseExpressionToZonedDateTime("-1.*.* 25:00:00", nowZDT);
    });
    TimeUtil.parseExpressionToZonedDateTime(ConcreteTime, endConcreteZDT);
  }

  /**
   * Wrong end test.
   *
   * @throws Exception the exception
   */
  @Test
  void WrongEndTest() throws Exception {
    final ZonedDateTime nowZDT = TimeUtil.getNow(this.zoneId);
    final ZonedDateTime startRelativetoNowTime = TimeUtil
        .parseExpressionToZonedDateTime("*.*.-5 23:00:00", nowZDT);
    final String ConcreteTime = "42.12.2016 00:00:00";
    assertThrows(DateTimeParseException.class, () -> {
      final ZonedDateTime endConcreteZDT = TimeUtil.getZonedDateTime(ConcreteTime, this.zoneId);
    });
  }

  /**
   * Start after end test.
   *
   * @throws Exception the exception
   */
  @Test
  void StartAfterEndTest() throws Exception {
    // start date ="*.*.+4 *:*:*" === relativ to now
    // end date ="12.12.2016 21:00:00"

    final ZonedDateTime nowZDT = TimeUtil.getNow(this.zoneId);
    final ZonedDateTime startRelativetoNowTime = TimeUtil
        .parseExpressionToZonedDateTime("*.*.+4 *:*:*", nowZDT);

    final String ConcreteTime = "12.12.2016 21:00:00";
    final ZonedDateTime endConcreteZDT = TimeUtil.getZonedDateTime(ConcreteTime, this.zoneId);
    final ZonedDateTime endConcreteTime = TimeUtil.parseExpressionToZonedDateTime(ConcreteTime,
        endConcreteZDT);

    assertFalse(startRelativetoNowTime.isBefore(endConcreteTime));
  }

  /**
   * Start equals end test.
   *
   * @throws Exception the exception
   */
  @Test
  void StartEqualsEndTest() throws Exception {
    // set fake now date to 12.12.2017 21:00:00
    // start date ="*.*.-1 *:*:*"
    // end date ="12.12.2016 21:00:00"

    final String fakeNowTime = "12.12.2017 21:00:00";
    final ZonedDateTime fakeNowZDT = TimeUtil.getZonedDateTime(fakeNowTime, this.zoneId);
    final ZonedDateTime startRelativetoNowTime = TimeUtil
        .parseExpressionToZonedDateTime("*.*.-1 *:*:*", fakeNowZDT);

    final String ConcreteTime = "12.12.2016 21:00:00";
    final ZonedDateTime endConcreteZDT = TimeUtil.getZonedDateTime(ConcreteTime, this.zoneId);
    final ZonedDateTime endConcreteTime = TimeUtil.parseExpressionToZonedDateTime(ConcreteTime,
        endConcreteZDT);

    assertTrue(startRelativetoNowTime.isEqual(endConcreteTime));

  }

  /**
   * Wrong format test.
   *
   * @throws Exception the exception
   */
  @Test
  void WrongFormatTest() throws Exception {
    final ZonedDateTime nowZDT = TimeUtil.getNow(this.zoneId);
    assertThrows(IllegalArgumentException.class, () -> {
      final ZonedDateTime startRelativetoNowTime = TimeUtil
          .parseExpressionToZonedDateTime("-1.2017 14:00:00", nowZDT);

    });

  }

}
