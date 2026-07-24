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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

/**
 * The Class TimeConcreteTest.
 */
class TimeConcreteTest {

  /** The zone component_id. */
  String zoneId = "Europe/Berlin";

  /**
   * Wrong start test.
   *
   * @throws Exception the exception
   */
  @Test
  void WrongStartTest() throws Exception {
    final String startDate = "42.12.2016 00:00:00";
    assertThrows(DateTimeParseException.class, () -> {

      final ZonedDateTime sD = TimeUtil.getZonedDateTime(startDate, this.zoneId);
    });
  }

  /**
   * Wrong end test.
   *
   * @throws Exception the exception
   */
  @Test
  void WrongEndTest() throws Exception {
    final String startDate = "12.01.2017 14:00:00";
    int testFails = 0;

    final String endDate1 = "42.12.2016 00:00:00";
    try {
      TimeUtil.getZonedDateTime(endDate1, this.zoneId);
    } catch (final DateTimeParseException e) {
      testFails++;
    }
    final String endDate2 = "12.12.2016 25:00:00";
    try {
      TimeUtil.getZonedDateTime(endDate2, this.zoneId);
    } catch (final DateTimeParseException e) {
      testFails++;
    }
    assertEquals(2, testFails);
  }

  /**
   * Start after end test.
   *
   * @throws Exception the exception
   */
  @Test
  void StartAfterEndTest() throws Exception {
    final String startDate = "12.01.2017 14:00:00";
    final String endDate = "12.01.2017 11:00:00";
    final ZonedDateTime tz1 = TimeUtil.getZonedDateTime(startDate, this.zoneId);
    final ZonedDateTime tz2 = TimeUtil.getZonedDateTime(endDate, this.zoneId);
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
    final String startDate = "12.01.2017 14:00:00";
    final String endDate = "12.01.2017 14:00:00";
    final ZonedDateTime tz1 = TimeUtil.getZonedDateTime(startDate, this.zoneId);
    final ZonedDateTime tz2 = TimeUtil.getZonedDateTime(endDate, this.zoneId);
    boolean result = tz1.isBefore(tz2);
    assertFalse(result);
    result = tz1.isAfter(tz2);
    assertFalse(result);
    result = tz1.equals(tz2);
    assertTrue(result);
  }

  /**
   * Wrong format test.
   *
   * @throws Exception the exception
   */
  @Test
  void WrongFormatTest() throws Exception {
    final String startDate = "12.31.2017 14:00:00";
    assertThrows(DateTimeParseException.class, () -> {
      final ZonedDateTime tz1 = TimeUtil.getZonedDateTime(startDate, this.zoneId);
    });
  }

}
