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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

/**
 * The Class TimeRelativeToNowRelativeToNowTest.
 */
class TimeRelativeToNowRelativeToNowTest {

  /** The zone component_id. */
  String zoneId = "Europe/Berlin";

  /**
   * This example starts in a relative time to now and finishes in a correct
   * relative time to now. This is a valid time interval.
   *
   * @throws Exception the exception
   */
  @Test
  void startCorrectRelativeTimeToNowAndEndsCorrectRelativeTimeToNow() throws Exception {
    // <start time="*.-8.* *:*:*" />
    // <end time="*.*.* *:-8:*" />

    ZonedDateTime nowZDT = TimeUtil.getNow(zoneId);
    ZonedDateTime startRelativetoNowTime = TimeUtil.parseExpressionToZonedDateTime("*.-8.* *:*:*", nowZDT);

    ZonedDateTime endRelativetoNowTime = TimeUtil.parseExpressionToZonedDateTime("*.*.* *:-8:*", nowZDT);

    assertTrue(startRelativetoNowTime.isBefore(endRelativetoNowTime));
  }

  /**
   * Start after end relative time to now.
   *
   * @throws Exception the exception
   */
  @Test
  void startAfterEndRelativeTimeToNow() throws Exception {
    // <start time="*.*.* *:-8:*"/>
    // <end time="*.-8.* *:*:*"/>

    ZonedDateTime nowZDT = TimeUtil.getNow(zoneId);
    ZonedDateTime startRelativetoNowTime = TimeUtil.parseExpressionToZonedDateTime("*.*.* *:-8:*", nowZDT);

    ZonedDateTime endRelativetoNowTime = TimeUtil.parseExpressionToZonedDateTime("*.-8.* *:*:*", nowZDT);

    assertFalse(startRelativetoNowTime.isBefore(endRelativetoNowTime));
  }

  /**
   * Start in a wrong relative time to now and ends in a correct relative time
   * to now.
   *
   * @throws Exception the exception
   */
  @Test
  void startWrongRelativeTimeToNowAndEndsCorrectRelativeTimeToNow() throws Exception {
    // <start time="*.*.-3000 *:*:*" />
    // <end time="*.*.* *:-8:*" />
    // This is a valid time interval.

    ZonedDateTime nowZDT = TimeUtil.getNow(zoneId);
    ZonedDateTime startRelativetoNowTime = TimeUtil.parseExpressionToZonedDateTime("*.*.-3000 *:*:*", nowZDT);

    ZonedDateTime endRelativetoNowTime = TimeUtil.parseExpressionToZonedDateTime("*.*.* *:-8:*", nowZDT);

    assertTrue(startRelativetoNowTime.isBefore(endRelativetoNowTime));
  }

  /**
   * Start relative time to now and ends near future relative time to now.
   *
   * @throws Exception the exception
   */
  @Test
  void startRelativeTimeToNowAndEndsNearFutureRelativeTimeToNow() throws Exception {
    // <start time="*.*.* *:*:*" />
    // <end time="*.*.+2 *:*:*" />

    ZonedDateTime nowZDT = TimeUtil.getNow(zoneId);
    ZonedDateTime startRelativetoNowTime = TimeUtil.parseExpressionToZonedDateTime("*.*.* *:*:*", nowZDT);

    ZonedDateTime endRelativetoNowTime = TimeUtil.parseExpressionToZonedDateTime("*.*.+2 *:*:*", nowZDT);

    assertEquals(startRelativetoNowTime.getYear(), endRelativetoNowTime.getYear());
    assertTrue(startRelativetoNowTime.isEqual(endRelativetoNowTime));
  }

  /**
   * Starts in a relative time to now and ends in a very future relative time to
   * now.
   *
   * @throws Exception the exception
   */
  @Test
  void startRelativeTimeToNowAndEndsVeryFutureRelativeTimeToNow() throws Exception {
    // <start time="*.-8.* *:*:*" />
    // <end time="*.*.+5000 *:*:*" />

    ZonedDateTime nowZDT = TimeUtil.getNow(zoneId);
    ZonedDateTime startRelativetoNowTime = TimeUtil.parseExpressionToZonedDateTime("*.-18.* *:*:*", nowZDT);

    ZonedDateTime endRelativetoNowTime = TimeUtil.parseExpressionToZonedDateTime("*.*.+5000 *:*:*", nowZDT);

    assertTrue(startRelativetoNowTime.getYear() < endRelativetoNowTime.getYear());
    assertTrue(startRelativetoNowTime.isBefore(endRelativetoNowTime));
  }
}
