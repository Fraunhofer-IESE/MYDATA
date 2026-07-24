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
 * The Class TimeConcreteRelativeToNowTest.
 */
class TimeConcreteRelativeToNowTest {

  /** The zone component_id. */
  String zoneId = "Europe/Berlin";

  /**
   * This example starts in a concrete time in the past and finishes in a correct relative time to
   * now. This is a valid time interval.
   *
   * @throws Exception the exception
   */
  @Test
  void startCorrectConcreteTimeAndEndsCorrectRelativeTimeToNow() throws Exception {
    // <start time="10.12.2016 00:00:00" />
    // <end time="*.*.-10 *:*:*" />

    final String ConcreteTime = "10.12.2016 00:00:00";
    final ZonedDateTime startConcreteZDT = TimeUtil.getZonedDateTime(ConcreteTime, this.zoneId);
    final ZonedDateTime startConcreteTime = TimeUtil.parseExpressionToZonedDateTime(ConcreteTime,
        startConcreteZDT);

    final ZonedDateTime nowZDT = TimeUtil.getNow(this.zoneId);
    final ZonedDateTime endRelativetoNowTime = TimeUtil
        .parseExpressionToZonedDateTime("*.*.* *:-8:*", nowZDT);

    assertTrue(startConcreteTime.isBefore(endRelativetoNowTime));
  }

  /**
   * This example starts in a concrete time in the future and finishes in a correct relative time to
   * now. The problem is that the start time is after end time and therefore, This is an invalid
   * time interval.
   *
   * @throws Exception the exception
   */
  @Test
  void startFutureConcreteTimeAndEndsCorrectPastRelativeTimeToNow() throws Exception {
    // <start time="28.05.2018 00:00:00" />
    // <end time="*.*.* *:-8:*" />

    final String ConcreteTime = "28.05.2018 00:00:00";
    final ZonedDateTime startConcreteZDT = TimeUtil.getZonedDateTime(ConcreteTime, this.zoneId);
    final ZonedDateTime startConcreteTime = TimeUtil.parseExpressionToZonedDateTime(ConcreteTime,
        startConcreteZDT);

    final ZonedDateTime nowZDT = TimeUtil.getNow(this.zoneId);
    final ZonedDateTime endRelativetoNowTime = TimeUtil
        .parseExpressionToZonedDateTime("*.*.-10 *:*:*", nowZDT);

    assertFalse(startConcreteTime.isBefore(endRelativetoNowTime));
  }

  /**
   * This example starts in a concrete time in the past which does not exists and finishes in a
   * correct relative time to now. This is an invalid time interval.
   *
   * @throws Exception the exception
   */
  @Test
  void startWrongConcreteTimeAndEndsCorrectRelativeTimeToNow() throws Exception {
    final String ConcreteTime = "30.02.2017 00:00:00";
    final ZonedDateTime startConcreteZDT = TimeUtil.getZonedDateTime(ConcreteTime, this.zoneId);
    final ZonedDateTime nowZDT = TimeUtil.getNow(this.zoneId);
    assertThrows(DateTimeException.class, () -> {
      TimeUtil.parseExpressionToZonedDateTime(ConcreteTime, startConcreteZDT);

    });
    TimeUtil.parseExpressionToZonedDateTime("*.*.* *:-8:*", nowZDT);

  }

  /**
   * This example starts in a concrete time and finishes in a future relative time to now. PDP must
   * evaluate the time up to now. This is a valid time interval.
   *
   * @throws Exception the exception
   */
  @Test
  void startConcreteTimeAndEndsNearFutureRelativeTimeToNow() throws Exception {
    // <start time="28.02.2017 00:00:00" />
    // <end time="*.*.+2 *:*:*" />

    final String ConcreteTime = "28.02.2017 00:00:00";
    final ZonedDateTime startConcreteZDT = TimeUtil.getZonedDateTime(ConcreteTime, this.zoneId);
    final ZonedDateTime startConcreteTime = TimeUtil.parseExpressionToZonedDateTime(ConcreteTime,
        startConcreteZDT);

    final ZonedDateTime nowZDT = TimeUtil.getNow(this.zoneId);
    final ZonedDateTime endRelativetoNowTime = TimeUtil
        .parseExpressionToZonedDateTime("*.*.+2 *:*:*", nowZDT);

    assertTrue(startConcreteTime.isBefore(endRelativetoNowTime));
  }

  /**
   * This example starts in a concrete time and finishes in a future relative time to now. PDP must
   * evaluate the time up to now. This is a valid time interval.
   *
   * @throws Exception the exception
   */
  @Test
  void startConcreteTimeAndEndsVeryFutureRelativeTimeToNow() throws Exception {
    // <start time="28.02.2017 00:00:00" />
    // <end time="*.*.+5000 *:*:*" />

    final String ConcreteTime = "28.02.2017 00:00:00";
    final ZonedDateTime startConcreteZDT = TimeUtil.getZonedDateTime(ConcreteTime, this.zoneId);
    final ZonedDateTime startConcreteTime = TimeUtil.parseExpressionToZonedDateTime(ConcreteTime,
        startConcreteZDT);

    final ZonedDateTime nowZDT = TimeUtil.getNow(this.zoneId);
    final ZonedDateTime endRelativetoNowTime = TimeUtil
        .parseExpressionToZonedDateTime("*.*.+5000 *:*:*", nowZDT);

    assertTrue(startConcreteTime.isBefore(endRelativetoNowTime));
  }
}
