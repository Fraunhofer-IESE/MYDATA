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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

/**
 * The Class TimeRelativeToEventRelativeToEventTest.
 */
class TimeRelativeToEventRelativeToEventTest {

  /** The zone component_id. */
  String zoneId = "Europe/Berlin";

  /**
   * Start event before end event. First/Last time that B occurred is "10.02.2017 00:00:00".
   * First/Last time that C occurred is "10.02.2018 00:00:00". This is a valid time interval.
   *
   * @throws Exception the exception
   */
  @Test
  void startEventBeforeEndEvent() throws Exception {
    // <start time="*.*.* *:*:*">
    // <eventOccurrence event="B" mode="FIRST/LAST"/>
    // </start>
    //
    // <end time="*.*.* *:*:*">
    // <eventOccurrence event="C" mode="FIRST/LAST"/>
    // </end>

    final String eventB = "10.02.2017 00:00:00";
    final ZonedDateTime startEventZDT = TimeUtil.getZonedDateTime(eventB, this.zoneId);
    final ZonedDateTime startRelativetoEventTime = TimeUtil
        .parseExpressionToZonedDateTime("*.*.* *:*:*", startEventZDT);

    final String eventC = "10.02.2018 00:00:00";
    final ZonedDateTime endEventZDT = TimeUtil.getZonedDateTime(eventC, this.zoneId);
    final ZonedDateTime endRelativetoEventTime = TimeUtil
        .parseExpressionToZonedDateTime("*.*.* *:*:*", endEventZDT);

    assertTrue(startRelativetoEventTime.isBefore(endRelativetoEventTime));
  }

  /**
   * Start event before end event start minus one. First/Last time that B occurred is "10.02.2017
   * 00:00:00". First/Last time that C occurred is "10.02.2018 00:00:00". This is a valid time
   * interval.
   *
   * @throws Exception the exception
   */
  @Test
  void startEventBeforeEndEventStartMinusOne() throws Exception {
    // <start time="*.*.-1 *:*:*">
    // <eventOccurrence event="B" mode="FIRST/LAST"/>
    // </start>
    //
    // <end time="*.*.* *:*:*">
    // <eventOccurrence event="C" mode="FIRST/LAST"/>
    // </end>

    final String eventB = "10.02.2017 00:00:00";
    final ZonedDateTime startEventZDT = TimeUtil.getZonedDateTime(eventB, this.zoneId);
    final ZonedDateTime startRelativetoEventTime = TimeUtil
        .parseExpressionToZonedDateTime("*.*.-1 *:*:*", startEventZDT);

    final String eventC = "10.02.2018 00:00:00";
    final ZonedDateTime endEventZDT = TimeUtil.getZonedDateTime(eventC, this.zoneId);
    final ZonedDateTime endRelativetoEventTime = TimeUtil
        .parseExpressionToZonedDateTime("*.*.* *:*:*", endEventZDT);

    assertTrue(startRelativetoEventTime.isBefore(endRelativetoEventTime));
  }

  /**
   * Start event before end event end plus one. First/Last time that B occurred is "10.02.2017
   * 00:00:00". First/Last time that C occurred is "10.02.2018 00:00:00". This is a valid time
   * interval.
   *
   * @throws Exception the exception
   */
  @Test
  void startEventBeforeEndEventEndPlusOne() throws Exception {
    // <start time="*.*.* *:*:*">
    // <eventOccurrence event="B" mode="FIRST/LAST"/>
    // </start>
    //
    // <end time="*.*.+1 *:*:*">
    // <eventOccurrence event="C" mode="FIRST/LAST"/>
    // </end>

    final String eventB = "10.02.2017 00:00:00";
    final ZonedDateTime startEventZDT = TimeUtil.getZonedDateTime(eventB, this.zoneId);
    final ZonedDateTime startRelativetoEventTime = TimeUtil
        .parseExpressionToZonedDateTime("*.*.* *:*:*", startEventZDT);

    final String eventC = "10.02.2018 00:00:00";
    final ZonedDateTime endEventZDT = TimeUtil.getZonedDateTime(eventC, this.zoneId);
    final ZonedDateTime endRelativetoEventTime = TimeUtil
        .parseExpressionToZonedDateTime("*.*.+1 *:*:*", endEventZDT);

    assertTrue(startRelativetoEventTime.isBefore(endRelativetoEventTime));
  }

  /**
   * Start event before end event start plus two. First/Last time that B occurred is "10.02.2017
   * 00:00:00". First/Last time that C occurred is "10.02.3017 00:00:00". This is an invalid time
   * interval.
   *
   * @throws Exception the exception
   */
  @Test
  void startEventBeforeEndEventStartPlusTen() throws Exception {
    // <start time="*.*.+10 *:*:*">
    // <eventOccurrence event="B" mode="FIRST/LAST"/>
    // </start>
    //
    // <end time="*.*.* *:*:*">
    // <eventOccurrence event="C" mode="FIRST/LAST"/>
    // </end>

    final String eventB = "10.02.2017 00:00:00";
    final ZonedDateTime startEventZDT = TimeUtil.getZonedDateTime(eventB, this.zoneId);
    final ZonedDateTime startRelativetoEventTime = TimeUtil
        .parseExpressionToZonedDateTime("*.*.+10 *:*:*", startEventZDT);

    final String eventC = "10.02.3017 00:00:00";
    final ZonedDateTime endEventZDT = TimeUtil.getZonedDateTime(eventC, this.zoneId);
    final ZonedDateTime endRelativetoEventTime = TimeUtil
        .parseExpressionToZonedDateTime("*.*.* *:*:*", endEventZDT);

    assertTrue(startRelativetoEventTime.isEqual(endRelativetoEventTime));
  }

  /**
   * Start event before end event end minus two. First/Last time that B occurred is "10.02.2017
   * 00:00:00". First/Last time that C occurred is "10.02.2018 00:00:00". This is an invalid time
   * interval.
   *
   * @throws Exception the exception
   */
  @Test
  void startEventBeforeEndEventEndMinusTwo() throws Exception {
    // <start time="*.*.* *:*:*">
    // <eventOccurrence event="B" mode="FIRST/LAST"/>
    // </start>
    //
    // <end time="*.*.-2 *:*:*">
    // <eventOccurrence event="C" mode="FIRST/LAST"/>
    // </end>

    final String eventB = "10.02.2017 00:00:00";
    final ZonedDateTime startEventZDT = TimeUtil.getZonedDateTime(eventB, this.zoneId);
    final ZonedDateTime startRelativetoEventTime = TimeUtil
        .parseExpressionToZonedDateTime("*.*.* *:*:*", startEventZDT);

    final String eventC = "10.02.2018 00:00:00";
    final ZonedDateTime endEventZDT = TimeUtil.getZonedDateTime(eventC, this.zoneId);
    final ZonedDateTime endRelativetoEventTime = TimeUtil
        .parseExpressionToZonedDateTime("*.*.-2 *:*:*", endEventZDT);

    assertFalse(startRelativetoEventTime.isBefore(endRelativetoEventTime));
  }

  /**
   * Start event after end event. First/Last time that B occurred is "10.02.2018 00:00:00".
   * First/Last time that C occurred is "10.02.2017 00:00:00". This is an invalid time interval.
   *
   * @throws Exception the exception
   */
  @Test
  void startEventAfterEndEvent() throws Exception {
    // <start time="*.*.* *:*:*">
    // <eventOccurrence event="B" mode="FIRST/LAST"/>
    // </start>
    //
    // <end time="*.*.* *:*:*">
    // <eventOccurrence event="C" mode="FIRST/LAST"/>
    // </end>

    final String eventB = "10.02.2018 00:00:00";
    final ZonedDateTime startEventZDT = TimeUtil.getZonedDateTime(eventB, this.zoneId);
    final ZonedDateTime startRelativetoEventTime = TimeUtil
        .parseExpressionToZonedDateTime("*.*.* *:*:*", startEventZDT);

    final String eventC = "10.02.2017 00:00:00";
    final ZonedDateTime endEventZDT = TimeUtil.getZonedDateTime(eventC, this.zoneId);
    final ZonedDateTime endRelativetoEventTime = TimeUtil
        .parseExpressionToZonedDateTime("*.*.* *:*:*", endEventZDT);

    assertFalse(startRelativetoEventTime.isBefore(endRelativetoEventTime));
  }

  /**
   * Start event after end event start minus two. First/Last time that B occurred is "10.02.2018
   * 00:00:00". First/Last time that C occurred is "10.02.2017 00:00:00". This is a valid time
   * interval.
   *
   * @throws Exception the exception
   */
  @Test
  void startEventAfterEndEventStartMinusTwo() throws Exception {
    // <start time="*.*.-2 *:*:*">
    // <eventOccurrence event="B" mode="FIRST/LAST"/>
    // </start>
    //
    // <end time="*.*.* *:*:*">
    // <eventOccurrence event="C" mode="FIRST/LAST"/>
    // </end>

    final String eventB = "10.02.2018 00:00:00";
    final ZonedDateTime startEventZDT = TimeUtil.getZonedDateTime(eventB, this.zoneId);
    final ZonedDateTime startRelativetoEventTime = TimeUtil
        .parseExpressionToZonedDateTime("*.*.-2 *:*:*", startEventZDT);

    final String eventC = "10.02.2017 00:00:00";
    final ZonedDateTime endEventZDT = TimeUtil.getZonedDateTime(eventC, this.zoneId);
    final ZonedDateTime endRelativetoEventTime = TimeUtil
        .parseExpressionToZonedDateTime("*.*.* *:*:*", endEventZDT);

    assertTrue(startRelativetoEventTime.isBefore(endRelativetoEventTime));
  }

  /**
   * Start event after end event end plus two. First/Last time that B occurred is "10.01.2018
   * 00:00:00". First/Last time that C occurred is "10.02.2017 00:00:00".
   *
   * @throws Exception the exception
   */
  @Test
  void startEventAfterEndEventEndPlusTwo() throws Exception {
    // <start time="*.*.* *:*:*">
    // <eventOccurrence event="B" mode="FIRST/LAST"/>
    // </start>
    //
    // <end time="*.*.+2 *:*:*">
    // <eventOccurrence event="C" mode="FIRST/LAST"/>
    // </end>

    final String eventB = "10.01.2018 00:00:00";
    final ZonedDateTime startEventZDT = TimeUtil.getZonedDateTime(eventB, this.zoneId);
    final ZonedDateTime startRelativetoEventTime = TimeUtil
        .parseExpressionToZonedDateTime("*.*.* *:*:*", startEventZDT);

    final String eventC = "10.02.2017 00:00:00";
    final ZonedDateTime endEventZDT = TimeUtil.getZonedDateTime(eventC, this.zoneId);
    final ZonedDateTime endRelativetoEventTime = TimeUtil
        .parseExpressionToZonedDateTime("*.*.+2 *:*:*", endEventZDT);

    assertTrue(startRelativetoEventTime.isBefore(endRelativetoEventTime));
  }

  /**
   * Start event after end event start plus one. First/Last time that B occurred is "10.02.2018
   * 00:00:00". First/Last time that C occurred is "10.02.2017 00:00:00". This is an invalid time
   * interval.
   *
   * @throws Exception the exception
   */
  @Test
  void startEventAfterEndEventStartPlusOne() throws Exception {
    // <start time="*.*.+1 *:*:*">
    // <eventOccurrence event="B" mode="FIRST/LAST"/>
    // </start>
    //
    // <end time="*.*.* *:*:*">
    // <eventOccurrence event="C" mode="FIRST/LAST"/>
    // </end>

    final String eventB = "10.02.2018 00:00:00";
    final ZonedDateTime startEventZDT = TimeUtil.getZonedDateTime(eventB, this.zoneId);
    final ZonedDateTime startRelativetoEventTime = TimeUtil
        .parseExpressionToZonedDateTime("*.*.+1 *:*:*", startEventZDT);

    final String eventC = "10.02.2017 00:00:00";
    final ZonedDateTime endEventZDT = TimeUtil.getZonedDateTime(eventC, this.zoneId);
    final ZonedDateTime endRelativetoEventTime = TimeUtil
        .parseExpressionToZonedDateTime("*.*.* *:*:*", endEventZDT);

    assertFalse(startRelativetoEventTime.isBefore(endRelativetoEventTime));
  }

  /**
   * Start event after end event end minus one. First/Last time that B occurred is "10.02.2018
   * 00:00:00". First/Last time that C occurred is "10.02.2017 00:00:00". This is an invalid time
   * interval.
   *
   * @throws Exception the exception
   */
  @Test
  void startEventAfterEndEventEndMinusOne() throws Exception {
    // <start time="*.*.* *:*:*">
    // <eventOccurrence event="B" mode="FIRST/LAST"/>
    // </start>
    //
    // <end time="*.*.-1 *:*:*">
    // <eventOccurrence event="C" mode="FIRST/LAST"/>
    // </end>

    final String eventB = "10.02.2018 00:00:00";
    final ZonedDateTime startEventZDT = TimeUtil.getZonedDateTime(eventB, this.zoneId);
    final ZonedDateTime startRelativetoEventTime = TimeUtil
        .parseExpressionToZonedDateTime("*.*.* *:*:*", startEventZDT);

    final String eventC = "10.02.2017 00:00:00";
    final ZonedDateTime endEventZDT = TimeUtil.getZonedDateTime(eventC, this.zoneId);
    final ZonedDateTime endRelativetoEventTime = TimeUtil
        .parseExpressionToZonedDateTime("*.*.-1 *:*:*", endEventZDT);

    assertFalse(startRelativetoEventTime.isBefore(endRelativetoEventTime));
  }
}
