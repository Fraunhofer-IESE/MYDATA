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

import de.fraunhofer.iese.mydata.solution.Timezone;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class TimeFixedTest.
 */
class TimeFixedTest {

  /** The tz. */
  Timezone tz;

  /** The zone component_id. */
  String zoneId = "Europe/Berlin";

  /** The zone component_id hawai. */
  String zoneIdHawai = "US/Hawaii";

  /** The zone component_id japan. */
  String zoneIdJapan = "Asia/Tokyo";

  /** The events from DB. */
  List<ZonedDateTime> eventsFromDB = new ArrayList<>();

  /** The events in servertimezone. */
  List<ZonedDateTime> eventsInServertimezone = new ArrayList<>();

  /**
   * Today test.
   *
   * @throws Exception the exception
   */
  @Test
  void TodayTest() throws Exception {
    this.eventsFromDB = new ArrayList<>();
    this.eventsInServertimezone = new ArrayList<>();

    final String now = "06.02.2018 23:35:40";
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("06.02.2018 00:35:10", this.zoneId));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("05.02.2014 12:35:10", this.zoneId));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("06.02.2018 12:35:10", this.zoneIdHawai));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("06.02.2018 22:35:10", this.zoneIdHawai));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("05.02.2018 01:35:10", this.zoneIdHawai));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("07.02.2018 05:35:10", this.zoneIdJapan));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("08.02.2018 01:35:10", this.zoneIdJapan));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("07.02.2018 15:35:10", this.zoneIdJapan));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("06.02.2018 01:35:10", this.zoneIdJapan));

    for (final ZonedDateTime event : this.eventsFromDB) {
      this.eventsInServertimezone.add(TimeUtil.transferToZone(event, this.zoneId));
    }
    final ZonedDateTime zdNow = TimeUtil.getZonedDateTime(now, this.zoneId);
    final TimeInterval timeInterval = TimeUtil.getInterval(TimeUtil.TODAY, zdNow);

    assertTrue(timeInterval.contains(this.eventsInServertimezone.get(0)));
    assertTrue(timeInterval.contains(this.eventsInServertimezone.get(2)));
    assertTrue(timeInterval.contains(this.eventsInServertimezone.get(5)));

    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(1)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(3)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(4)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(6)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(7)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(8)));
  }

  /**
   * This minute test.
   *
   * @throws Exception the exception
   */
  @Test
  void ThisMinuteTest() throws Exception {
    this.eventsFromDB = new ArrayList<>();
    this.eventsInServertimezone = new ArrayList<>();

    final String now = "06.02.2018 23:35:40";
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("06.02.2018 23:35:10", this.zoneId));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("05.02.2014 12:35:10", this.zoneId));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("06.02.2018 12:35:10", this.zoneIdHawai));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("06.02.2018 22:35:10", this.zoneIdHawai));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("05.02.2018 01:35:10", this.zoneIdHawai));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("07.02.2018 07:35:10", this.zoneIdJapan));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("08.02.2018 01:35:10", this.zoneIdJapan));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("07.02.2018 15:35:10", this.zoneIdJapan));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("06.02.2018 01:35:10", this.zoneIdJapan));

    for (final ZonedDateTime event : this.eventsFromDB) {
      this.eventsInServertimezone.add(TimeUtil.transferToZone(event, this.zoneId));
    }
    final ZonedDateTime zdNow = TimeUtil.getZonedDateTime(now, this.zoneId);
    final TimeInterval timeInterval = TimeUtil.getInterval(TimeUtil.THIS_MINUTE, zdNow);

    assertTrue(timeInterval.contains(this.eventsInServertimezone.get(0)));
    assertTrue(timeInterval.contains(this.eventsInServertimezone.get(2)));
    assertTrue(timeInterval.contains(this.eventsInServertimezone.get(5)));

    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(1)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(3)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(4)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(6)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(7)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(8)));
  }

  /**
   * Last minute test.
   *
   * @throws Exception the exception
   */
  @Test
  void LastMinuteTest() throws Exception {
    this.eventsFromDB = new ArrayList<>();
    this.eventsInServertimezone = new ArrayList<>();

    final String now = "06.02.2018 23:35:40";
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("06.02.2018 23:34:10", this.zoneId));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("05.02.2014 12:35:10", this.zoneId));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("06.02.2018 12:34:10", this.zoneIdHawai));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("06.02.2018 22:35:10", this.zoneIdHawai));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("05.02.2018 01:35:10", this.zoneIdHawai));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("07.02.2018 07:34:10", this.zoneIdJapan));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("08.02.2018 01:35:10", this.zoneIdJapan));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("07.02.2018 15:35:10", this.zoneIdJapan));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("06.02.2018 01:35:10", this.zoneIdJapan));

    for (final ZonedDateTime event : this.eventsFromDB) {
      this.eventsInServertimezone.add(TimeUtil.transferToZone(event, this.zoneId));
    }
    final ZonedDateTime zdNow = TimeUtil.getZonedDateTime(now, this.zoneId);
    final TimeInterval timeInterval = TimeUtil.getInterval(TimeUtil.LAST_MINUTE, zdNow);

    assertTrue(timeInterval.contains(this.eventsInServertimezone.get(0)));
    assertTrue(timeInterval.contains(this.eventsInServertimezone.get(2)));
    assertTrue(timeInterval.contains(this.eventsInServertimezone.get(5)));

    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(1)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(3)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(4)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(6)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(7)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(8)));
  }

  /**
   * This hour test.
   *
   * @throws Exception the exception
   */
  @Test
  void ThisHourTest() throws Exception {
    this.eventsFromDB = new ArrayList<>();
    this.eventsInServertimezone = new ArrayList<>();

    final String now = "06.02.2018 23:35:40";
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("06.02.2018 23:34:10", this.zoneId));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("05.02.2014 12:35:10", this.zoneId));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("06.02.2018 12:34:10", this.zoneIdHawai));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("06.02.2018 22:35:10", this.zoneIdHawai));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("05.02.2018 01:35:10", this.zoneIdHawai));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("07.02.2018 07:34:10", this.zoneIdJapan));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("08.02.2018 01:35:10", this.zoneIdJapan));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("07.02.2018 15:35:10", this.zoneIdJapan));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("06.02.2018 01:35:10", this.zoneIdJapan));

    for (final ZonedDateTime event : this.eventsFromDB) {
      this.eventsInServertimezone.add(TimeUtil.transferToZone(event, this.zoneId));
    }
    final ZonedDateTime zdNow = TimeUtil.getZonedDateTime(now, this.zoneId);
    final TimeInterval timeInterval = TimeUtil.getInterval(TimeUtil.THIS_HOUR, zdNow);

    assertTrue(timeInterval.contains(this.eventsInServertimezone.get(0)));
    assertTrue(timeInterval.contains(this.eventsInServertimezone.get(2)));
    assertTrue(timeInterval.contains(this.eventsInServertimezone.get(5)));

    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(1)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(3)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(4)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(6)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(7)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(8)));
  }

  /**
   * Last hour test.
   *
   * @throws Exception the exception
   */
  @Test
  void LastHourTest() throws Exception {
    this.eventsFromDB = new ArrayList<>();
    this.eventsInServertimezone = new ArrayList<>();

    final String now = "06.02.2018 23:35:40";
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("06.02.2018 22:34:10", this.zoneId));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("05.02.2014 12:35:10", this.zoneId));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("06.02.2018 11:34:10", this.zoneIdHawai));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("06.02.2018 22:35:10", this.zoneIdHawai));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("05.02.2018 01:35:10", this.zoneIdHawai));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("07.02.2018 06:34:10", this.zoneIdJapan));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("08.02.2018 01:35:10", this.zoneIdJapan));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("07.02.2018 15:35:10", this.zoneIdJapan));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("06.02.2018 01:35:10", this.zoneIdJapan));

    for (final ZonedDateTime event : this.eventsFromDB) {
      this.eventsInServertimezone.add(TimeUtil.transferToZone(event, this.zoneId));
    }
    final ZonedDateTime zdNow = TimeUtil.getZonedDateTime(now, this.zoneId);
    final TimeInterval timeInterval = TimeUtil.getInterval(TimeUtil.LAST_HOUR, zdNow);

    assertTrue(timeInterval.contains(this.eventsInServertimezone.get(0)));
    assertTrue(timeInterval.contains(this.eventsInServertimezone.get(2)));
    assertTrue(timeInterval.contains(this.eventsInServertimezone.get(5)));

    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(1)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(3)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(4)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(6)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(7)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(8)));
  }

  /**
   * Month test.
   *
   * @throws Exception the exception
   */
  @Test
  void MonthTest() throws Exception {
    this.eventsFromDB = new ArrayList<>();
    this.eventsInServertimezone = new ArrayList<>();

    final String now = "06.02.2018 23:35:40";
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("06.02.2018 00:35:10", this.zoneId));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("05.02.2014 12:35:10", this.zoneId));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("31.01.2018 22:35:10", this.zoneIdHawai));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("06.02.2018 22:35:10", this.zoneIdHawai));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("28.02.2018 01:35:10", this.zoneIdHawai));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("01.03.2018 02:35:10", this.zoneIdJapan));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("01.02.2018 01:35:10", this.zoneIdJapan));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("28.02.2018 15:35:10", this.zoneIdJapan));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("01.03.2018 01:35:10", this.zoneIdJapan));

    for (final ZonedDateTime event : this.eventsFromDB) {
      this.eventsInServertimezone.add(TimeUtil.transferToZone(event, this.zoneId));
    }
    final ZonedDateTime zdNow = TimeUtil.getZonedDateTime(now, this.zoneId);
    final TimeInterval timeInterval = TimeUtil.getInterval(TimeUtil.THIS_MONTH, zdNow);

    assertTrue(timeInterval.contains(this.eventsInServertimezone.get(0)));
    assertTrue(timeInterval.contains(this.eventsInServertimezone.get(2)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(5)));
    // not occurred yet ;)

    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(1)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(3)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(4)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(6)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(7)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(8)));
  }

  /**
   * Yesturday test.
   *
   * @throws Exception the exception
   */
  @Test
  void YesterdayTest() throws Exception {
    this.eventsFromDB = new ArrayList<>();
    this.eventsInServertimezone = new ArrayList<>();

    final String now = "06.02.2018 23:35:40";
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("05.02.2018 00:35:10", this.zoneId));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("05.02.2014 12:35:10", this.zoneId));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("04.02.2018 22:35:10", this.zoneIdHawai));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("06.02.2018 22:35:10", this.zoneIdHawai));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("04.02.2018 01:35:10", this.zoneIdHawai));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("06.02.2018 02:35:10", this.zoneIdJapan));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("01.02.2018 01:35:10", this.zoneIdJapan));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("28.02.2018 15:35:10", this.zoneIdJapan));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("01.03.2018 01:35:10", this.zoneIdJapan));

    for (final ZonedDateTime event : this.eventsFromDB) {
      this.eventsInServertimezone.add(TimeUtil.transferToZone(event, this.zoneId));
    }
    final ZonedDateTime zdNow = TimeUtil.getZonedDateTime(now, this.zoneId);
    final TimeInterval timeInterval = TimeUtil.getInterval(TimeUtil.YESTERDAY, zdNow);

    assertTrue(timeInterval.contains(this.eventsInServertimezone.get(0)));
    assertTrue(timeInterval.contains(this.eventsInServertimezone.get(2)));
    assertTrue(timeInterval.contains(this.eventsInServertimezone.get(5)));

    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(1)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(3)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(4)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(6)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(7)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(8)));
  }

  /**
   * Last month test.
   *
   * @throws Exception the exception
   */
  @Test
  void LastMonthTest() throws Exception {
    this.eventsFromDB = new ArrayList<>();
    this.eventsInServertimezone = new ArrayList<>();

    final String now = "06.02.2018 23:35:40";
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("06.01.2018 00:35:10", this.zoneId));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("05.02.2014 12:35:10", this.zoneId));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("31.01.2018 12:35:10", this.zoneIdHawai));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("06.02.2018 22:35:10", this.zoneIdHawai));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("28.02.2018 01:35:10", this.zoneIdHawai));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("31.01.2018 05:35:10", this.zoneIdJapan));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("01.01.2018 01:35:10", this.zoneIdJapan));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("28.02.2018 15:35:10", this.zoneIdJapan));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("01.03.2018 01:35:10", this.zoneIdJapan));

    for (final ZonedDateTime event : this.eventsFromDB) {
      this.eventsInServertimezone.add(TimeUtil.transferToZone(event, this.zoneId));
    }
    final ZonedDateTime zdNow = TimeUtil.getZonedDateTime(now, this.zoneId);
    final TimeInterval timeInterval = TimeUtil.getInterval(TimeUtil.LAST_MONTH, zdNow);

    assertTrue(timeInterval.contains(this.eventsInServertimezone.get(0)));
    assertTrue(timeInterval.contains(this.eventsInServertimezone.get(2)));
    assertTrue(timeInterval.contains(this.eventsInServertimezone.get(5)));

    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(1)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(3)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(4)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(6)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(7)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(8)));
  }

  /**
   * This year test.
   *
   * @throws Exception the exception
   */
  @Test
  void ThisYearTest() throws Exception {
    this.eventsFromDB = new ArrayList<>();
    this.eventsInServertimezone = new ArrayList<>();

    final String now = "06.02.2018 23:35:40";
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("06.02.2018 00:35:10", this.zoneId));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("05.02.2014 12:35:10", this.zoneId));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("31.12.2017 22:35:10", this.zoneIdHawai));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("06.02.2018 22:35:10", this.zoneIdHawai));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("28.02.2018 01:35:10", this.zoneIdHawai));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("31.01.2018 05:35:10", this.zoneIdJapan));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("01.02.2017 01:35:10", this.zoneIdJapan));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("01.01.2018 00:35:10", this.zoneIdJapan));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("01.03.2018 01:35:10", this.zoneIdJapan));

    for (final ZonedDateTime event : this.eventsFromDB) {
      this.eventsInServertimezone.add(TimeUtil.transferToZone(event, this.zoneId));
    }
    final ZonedDateTime zdNow = TimeUtil.getZonedDateTime(now, this.zoneId);
    final TimeInterval timeInterval = TimeUtil.getInterval(TimeUtil.THIS_YEAR, zdNow);

    assertTrue(timeInterval.contains(this.eventsInServertimezone.get(0)));
    assertTrue(timeInterval.contains(this.eventsInServertimezone.get(2)));
    assertTrue(timeInterval.contains(this.eventsInServertimezone.get(5)));

    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(1)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(3)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(4)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(6)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(7)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(8)));
  }

  /**
   * Last year test.
   *
   * @throws Exception the exception
   */
  @Test
  void LastYearTest() throws Exception {
    this.eventsFromDB = new ArrayList<>();
    this.eventsInServertimezone = new ArrayList<>();

    final String now = "06.02.2018 23:35:40";
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("06.02.2017 00:35:10", this.zoneId));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("05.02.2014 12:35:10", this.zoneId));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("31.12.2017 02:35:10", this.zoneIdHawai));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("06.02.2018 22:35:10", this.zoneIdHawai));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("28.02.2018 01:35:10", this.zoneIdHawai));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("31.01.2017 05:35:10", this.zoneIdJapan));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("01.01.2017 01:35:10", this.zoneIdJapan));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("01.01.2019 00:35:10", this.zoneIdJapan));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("01.03.2018 01:35:10", this.zoneIdJapan));

    for (final ZonedDateTime event : this.eventsFromDB) {
      this.eventsInServertimezone.add(TimeUtil.transferToZone(event, this.zoneId));
    }
    final ZonedDateTime zdNow = TimeUtil.getZonedDateTime(now, this.zoneId);
    final TimeInterval timeInterval = TimeUtil.getInterval(TimeUtil.LAST_YEAR, zdNow);

    assertTrue(timeInterval.contains(this.eventsInServertimezone.get(0)));
    assertTrue(timeInterval.contains(this.eventsInServertimezone.get(2)));
    assertTrue(timeInterval.contains(this.eventsInServertimezone.get(5)));

    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(1)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(3)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(4)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(6)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(7)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(8)));
  }

  /**
   * This week test. Beginning of this week (Monday) up to now.
   *
   * @throws Exception the exception
   */
  @Test
  void ThisWeekTest() throws Exception {
    this.eventsFromDB = new ArrayList<>();
    this.eventsInServertimezone = new ArrayList<>();

    final String now = "06.02.2018 23:35:40";
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("06.02.2018 00:35:10", this.zoneId));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("05.02.2018 12:35:10", this.zoneId));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("31.01.2018 12:35:10", this.zoneIdHawai));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("06.02.2018 22:35:10", this.zoneIdHawai));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("28.02.2018 01:35:10", this.zoneIdHawai));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("11.02.2018 05:35:10", this.zoneIdJapan));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("01.02.2018 01:35:10", this.zoneIdJapan));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("10.02.2018 15:35:10", this.zoneIdJapan));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("11.02.2018 00:00:00", this.zoneIdJapan));

    for (final ZonedDateTime event : this.eventsFromDB) {
      this.eventsInServertimezone.add(TimeUtil.transferToZone(event, this.zoneId));
    }
    final ZonedDateTime zdNow = TimeUtil.getZonedDateTime(now, this.zoneId);
    final TimeInterval timeInterval = TimeUtil.getInterval(TimeUtil.THIS_WEEK, zdNow);

    assertTrue(timeInterval.contains(this.eventsInServertimezone.get(0)));
    assertTrue(timeInterval.contains(this.eventsInServertimezone.get(1)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(5)));

    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(2)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(3)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(4)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(6)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(7)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(8)));
  }

  /**
   * Last week test.
   *
   * @throws Exception the exception
   */
  @Test
  void LastWeekTest() throws Exception {
    this.eventsFromDB = new ArrayList<>();
    this.eventsInServertimezone = new ArrayList<>();

    final String now = "06.02.2018 23:35:40";
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("06.02.2018 00:35:10", this.zoneId));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("05.02.2014 12:35:10", this.zoneId));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("29.01.2018 12:35:10", this.zoneIdHawai));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("06.02.2018 22:35:10", this.zoneIdHawai));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("28.02.2018 01:35:10", this.zoneIdHawai));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("31.01.2018 05:35:10", this.zoneIdJapan));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("01.02.2018 01:35:10", this.zoneIdJapan));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("28.02.2018 15:35:10", this.zoneIdJapan));
    this.eventsFromDB.add(TimeUtil.getZonedDateTime("01.03.2018 01:35:10", this.zoneIdJapan));

    for (final ZonedDateTime event : this.eventsFromDB) {
      this.eventsInServertimezone.add(TimeUtil.transferToZone(event, this.zoneId));
    }
    final ZonedDateTime zdNow = TimeUtil.getZonedDateTime(now, this.zoneId);
    final TimeInterval timeInterval = TimeUtil.getInterval(TimeUtil.LAST_WEEK, zdNow);

    assertTrue(timeInterval.contains(this.eventsInServertimezone.get(2)));
    assertTrue(timeInterval.contains(this.eventsInServertimezone.get(5)));
    assertTrue(timeInterval.contains(this.eventsInServertimezone.get(6)));

    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(0)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(1)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(3)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(4)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(7)));
    assertFalse(timeInterval.contains(this.eventsInServertimezone.get(8)));
  }

  /**
   * TestCase Expression thisMinute | Date(Now) 06.02.2018 10:35:30 | Resulting Start: 06.02.2018
   * 10:35:00 | Resulting End: 06.02.2018 10:35:30 (NOW)
   *
   * @throws Exception
   */
  @Test
  void IntervalTestThisMinute() throws Exception {
    final String now = "06.02.2018 10:35:30";
    final String start = "06.02.2018 10:35:00";

    final ZonedDateTime zdtNow = TimeUtil.getZonedDateTime(now, this.zoneId);
    final ZonedDateTime zdtStart = TimeUtil.getZonedDateTime(start, this.zoneId);

    final TimeInterval timeInterval = TimeUtil.getInterval(TimeUtil.THIS_MINUTE, zdtNow);

    assertTrue(zdtStart.isEqual(timeInterval.getStartTime()));
    assertTrue(zdtNow.isEqual(timeInterval.getEndTime()));
  }

  /**
   * TestCase Expression lastMinute | Date(Now) 06.02.2018 10:35:30 | Resulting Start: 06.02.2018
   * 10:34:00 | Resulting End: 06.02.2018 10:34:59
   *
   * @throws Exception
   */
  @Test
  void IntervalTestLastMinute() throws Exception {
    final String now = "06.02.2018 10:35:30";
    final String start = "06.02.2018 10:34:00";
    final String end = "06.02.2018 10:34:59";

    final ZonedDateTime zdtNow = TimeUtil.getZonedDateTime(now, this.zoneId);
    final ZonedDateTime zdtStart = TimeUtil.getZonedDateTime(start, this.zoneId);
    final ZonedDateTime zdtEnd = TimeUtil.getZonedDateTime(end, this.zoneId);

    final TimeInterval timeInterval = TimeUtil.getInterval(TimeUtil.LAST_MINUTE, zdtNow);

    assertTrue(zdtStart.isEqual(timeInterval.getStartTime()));
    assertTrue(zdtEnd.isEqual(timeInterval.getEndTime()));
  }

  /**
   * TestCase Expression thisHour | Date(Now) 06.02.2018 10:35:30 | Resulting Start: 06.02.2018
   * 10:35:00 |Resulting End: 06.02.2018 10:35:30 (NOW)
   *
   * @throws Exception
   */
  @Test
  void IntervalTestThisHour() throws Exception {
    final String now = "06.02.2018 10:35:30";
    final String start = "06.02.2018 10:00:00";

    final ZonedDateTime zdtNow = TimeUtil.getZonedDateTime(now, this.zoneId);
    final ZonedDateTime zdtStart = TimeUtil.getZonedDateTime(start, this.zoneId);

    final TimeInterval timeInterval = TimeUtil.getInterval(TimeUtil.THIS_HOUR, zdtNow);

    assertTrue(zdtStart.isEqual(timeInterval.getStartTime()));
    assertTrue(zdtNow.isEqual(timeInterval.getEndTime()));
  }

  /**
   * TestCase Expression lastHour | Date(Now) 06.02.2018 10:35:30 | Resulting Start: 06.02.2018
   * 09:00:00 | Resulting End: 06.02.2018 09:59:59
   *
   * @throws Exception
   */
  @Test
  void IntervalTestLastHour() throws Exception {
    final String now = "06.02.2018 10:35:30";
    final String start = "06.02.2018 09:00:00";
    final String end = "06.02.2018 09:59:59";

    final ZonedDateTime zdtNow = TimeUtil.getZonedDateTime(now, this.zoneId);
    final ZonedDateTime zdtStart = TimeUtil.getZonedDateTime(start, this.zoneId);
    final ZonedDateTime zdtEnd = TimeUtil.getZonedDateTime(end, this.zoneId);

    final TimeInterval timeInterval = TimeUtil.getInterval(TimeUtil.LAST_HOUR, zdtNow);

    assertTrue(zdtStart.isEqual(timeInterval.getStartTime()));
    assertTrue(zdtEnd.isEqual(timeInterval.getEndTime()));
  }

  /**
   * TestCase Expression today Date(Now) | 06.02.2018 10:35:30 | Resulting Start: 06.02.2018
   * 00:00:00 | Resulting End: 06.02.2018 10:35:30 (NOW)
   *
   * @throws Exception
   */
  @Test
  void IntervalTestToday() throws Exception {
    final String now = "06.02.2018 10:35:30";
    final String start = "06.02.2018 00:00:00";

    final ZonedDateTime zdtNow = TimeUtil.getZonedDateTime(now, this.zoneId);
    final ZonedDateTime zdtStart = TimeUtil.getZonedDateTime(start, this.zoneId);

    final TimeInterval timeInterval = TimeUtil.getInterval(TimeUtil.TODAY, zdtNow);

    assertTrue(zdtStart.isEqual(timeInterval.getStartTime()));
    assertTrue(zdtNow.isEqual(timeInterval.getEndTime()));
  }

  /**
   * TestCase Expression lastHour | Date(Now) 06.02.2018 10:35:30 | Resulting Start: 06.02.2018
   * 09:00:00 | Resulting End: 06.02.2018 09:59:59
   *
   * @throws Exception
   */
  @Test
  void IntervalTestYesterday() throws Exception {
    final String now = "06.02.2018 10:35:30";
    final String start = "05.02.2018 00:00:00";
    final String end = "05.02.2018 23:59:59";

    final ZonedDateTime zdtNow = TimeUtil.getZonedDateTime(now, this.zoneId);
    final ZonedDateTime zdtStart = TimeUtil.getZonedDateTime(start, this.zoneId);
    final ZonedDateTime zdtEnd = TimeUtil.getZonedDateTime(end, this.zoneId);

    final TimeInterval timeInterval = TimeUtil.getInterval(TimeUtil.YESTERDAY, zdtNow);

    assertTrue(zdtStart.isEqual(timeInterval.getStartTime()));
    assertTrue(zdtEnd.isEqual(timeInterval.getEndTime()));
  }

  /**
   * TestCase Expression thisMonth Date(Now) | 06.02.2018 10:35:30 | Resulting Start: 01.02.2018
   * 00:00:00 | Resulting End: 06.02.2018 10:35:30 (NOW)
   *
   * @throws Exception
   */
  @Test
  void IntervalTestThisMonth() throws Exception {
    final String now = "06.02.2018 10:35:30";
    final String start = "01.02.2018 00:00:00";

    final ZonedDateTime zdtNow = TimeUtil.getZonedDateTime(now, this.zoneId);
    final ZonedDateTime zdtStart = TimeUtil.getZonedDateTime(start, this.zoneId);

    final TimeInterval timeInterval = TimeUtil.getInterval(TimeUtil.THIS_MONTH, zdtNow);

    assertTrue(zdtStart.isEqual(timeInterval.getStartTime()));
    assertTrue(zdtNow.isEqual(timeInterval.getEndTime()));
  }

  /**
   * TestCase Expression lastMonth | Date(Now) 06.02.2018 10:35:30 | Resulting Start: 01.01.2018
   * 00:00:00 | Resulting End: 31.01.2018 23:59:59
   *
   * @throws Exception
   */
  @Test
  void IntervalTestLastMonth1() throws Exception {
    final String now = "06.02.2018 10:35:30";
    final String start = "01.01.2018 00:00:00";
    final String end = "31.01.2018 23:59:59";

    final ZonedDateTime zdtNow = TimeUtil.getZonedDateTime(now, this.zoneId);
    final ZonedDateTime zdtStart = TimeUtil.getZonedDateTime(start, this.zoneId);
    final ZonedDateTime zdtEnd = TimeUtil.getZonedDateTime(end, this.zoneId);

    final TimeInterval timeInterval = TimeUtil.getInterval(TimeUtil.LAST_MONTH, zdtNow);

    assertTrue(zdtStart.isEqual(timeInterval.getStartTime()));
    assertTrue(zdtEnd.isEqual(timeInterval.getEndTime()));
  }

  /**
   * TestCase Expression lastMonth | Date(Now) 06.03.2018 10:35:30 | Resulting Start: 01.02.2018
   * 00:00:00 | Resulting End: 28.02.2018 23:59:59
   *
   * @throws Exception
   */
  @Test
  void IntervalTestLastMonth2() throws Exception {
    final String now = "06.03.2018 10:35:30";
    final String start = "01.02.2018 00:00:00";
    final String end = "28.02.2018 23:59:59";

    final ZonedDateTime zdtNow = TimeUtil.getZonedDateTime(now, this.zoneId);
    final ZonedDateTime zdtStart = TimeUtil.getZonedDateTime(start, this.zoneId);
    final ZonedDateTime zdtEnd = TimeUtil.getZonedDateTime(end, this.zoneId);

    final TimeInterval timeInterval = TimeUtil.getInterval(TimeUtil.LAST_MONTH, zdtNow);

    assertTrue(zdtStart.isEqual(timeInterval.getStartTime()));
    assertTrue(zdtEnd.isEqual(timeInterval.getEndTime()));
  }

  /**
   * TestCase Expression lastMonth | Date(Now) 06.03.2016 10:35:30 | Resulting Start:01.02.2016
   * 00:00:00 | Resulting End: 29.02.2016 23:59:59
   *
   * @throws Exception
   */
  @Test
  void IntervalTestLastMonth3() throws Exception {
    final String now = "06.03.2016 10:35:30";
    final String start = "01.02.2016 00:00:00";
    final String end = "29.02.2016 23:59:59";

    final ZonedDateTime zdtNow = TimeUtil.getZonedDateTime(now, this.zoneId);
    final ZonedDateTime zdtStart = TimeUtil.getZonedDateTime(start, this.zoneId);
    final ZonedDateTime zdtEnd = TimeUtil.getZonedDateTime(end, this.zoneId);

    final TimeInterval timeInterval = TimeUtil.getInterval(TimeUtil.LAST_MONTH, zdtNow);

    assertTrue(zdtStart.isEqual(timeInterval.getStartTime()));
    assertTrue(zdtEnd.isEqual(timeInterval.getEndTime()));
  }

  /**
   * TestCase Expression thisYear Date(Now) | 06.02.2018 10:35:30 | Resulting Start: 01.01.2018
   * 00:00:00 | Resulting End: 06.02.2018 10:35:30 (NOW)
   *
   * @throws Exception
   */
  @Test
  void IntervalTestThisYear() throws Exception {
    final String now = "06.02.2018 10:35:30";
    final String start = "01.01.2018 00:00:00";

    final ZonedDateTime zdtNow = TimeUtil.getZonedDateTime(now, this.zoneId);
    final ZonedDateTime zdtStart = TimeUtil.getZonedDateTime(start, this.zoneId);

    final TimeInterval timeInterval = TimeUtil.getInterval(TimeUtil.THIS_YEAR, zdtNow);

    assertTrue(zdtStart.isEqual(timeInterval.getStartTime()));
    assertTrue(zdtNow.isEqual(timeInterval.getEndTime()));
  }

  /**
   * TestCase Expression lastMonth | Date(Now) 06.02.2018 10:35:30 | Resulting Start: 01.01.2017
   * 00:00:00 | Resulting End: 31.12.2017 23:59:59
   *
   * @throws Exception
   */
  @Test
  void IntervalTestAlways() throws Exception {
    final String now = "06.02.2018 10:35:30";
    final String start = "01.01.1970 00:00:00";

    final ZonedDateTime zdtNow = TimeUtil.getZonedDateTime(now, this.zoneId);
    final ZonedDateTime zdtStart = TimeUtil.getZonedDateTime(start, "UTC");

    final TimeInterval timeInterval = TimeUtil.getInterval(TimeUtil.ALWAYS, zdtNow);

    assertTrue(zdtStart.isEqual(timeInterval.getStartTime()));
    assertEquals(0,
        TimeUtil.getMillisFromZonedDateTime(timeInterval.getStartTime(), this.zoneId));
    assertTrue(zdtNow.isEqual(timeInterval.getEndTime()));
  }

  /**
   * TestCase Expression lastMonth | Date(Now) 06.02.2018 10:35:30 | Resulting Start: 01.01.2017
   * 00:00:00 | Resulting End: 31.12.2017 23:59:59
   *
   * @throws Exception
   */
  @Test
  void IntervalTestLastYear() throws Exception {
    final String now = "06.02.2018 10:35:30";
    final String start = "01.01.2017 00:00:00";
    final String end = "31.12.2017 23:59:59";

    final ZonedDateTime zdtNow = TimeUtil.getZonedDateTime(now, this.zoneId);
    final ZonedDateTime zdtStart = TimeUtil.getZonedDateTime(start, this.zoneId);
    final ZonedDateTime zdtEnd = TimeUtil.getZonedDateTime(end, this.zoneId);

    final TimeInterval timeInterval = TimeUtil.getInterval(TimeUtil.LAST_YEAR, zdtNow);

    assertTrue(zdtStart.isEqual(timeInterval.getStartTime()));
    assertTrue(zdtEnd.isEqual(timeInterval.getEndTime()));
  }

  /**
   * TestCase Expression thisWeek Date(Now) | 06.02.2018 10:35:30 | Resulting Start: 05.02.2018
   * 00:00:00 | Resulting End: 06.02.2018 10:35:30 (NOW)
   *
   * @throws Exception
   */
  @Test
  void IntervalTestThisWeek() throws Exception {
    final String now = "06.02.2018 10:35:30";
    final String start = "05.02.2018 00:00:00";

    final ZonedDateTime zdtNow = TimeUtil.getZonedDateTime(now, this.zoneId);
    final ZonedDateTime zdtStart = TimeUtil.getZonedDateTime(start, this.zoneId);

    final TimeInterval timeInterval = TimeUtil.getInterval(TimeUtil.THIS_WEEK, zdtNow);

    assertTrue(zdtStart.isEqual(timeInterval.getStartTime()));
    assertTrue(zdtNow.isEqual(timeInterval.getEndTime()));
  }

  /**
   * TestCase Expression lastWeek | Date(Now) 06.02.2018 10:35:30 | Resulting Start: 29.01.2018
   * 00:00:00 | Resulting End: 04.02.2018 23:59:59
   *
   * @throws Exception
   */
  @Test
  void IntervalTestLastWeek() throws Exception {
    final String now = "06.02.2018 10:35:30";
    final String start = "29.01.2018 00:00:00";
    final String end = "04.02.2018 23:59:59";

    final ZonedDateTime zdtNow = TimeUtil.getZonedDateTime(now, this.zoneId);
    final ZonedDateTime zdtStart = TimeUtil.getZonedDateTime(start, this.zoneId);
    final ZonedDateTime zdtEnd = TimeUtil.getZonedDateTime(end, this.zoneId);

    final TimeInterval timeInterval = TimeUtil.getInterval(TimeUtil.LAST_WEEK, zdtNow);

    assertTrue(zdtStart.isEqual(timeInterval.getStartTime()));
    assertTrue(zdtEnd.isEqual(timeInterval.getEndTime()));
  }

  /**
   * TestCase Expression thisWeek Date(Now) | 06.02.2018 10:35:30 | Resulting Start: 04.02.2018
   * 00:00:00 | Resulting End: 06.02.2018 10:35:30 (NOW)
   *
   * @throws Exception
   */
  @Test
  void IntervalTestThisWeekSunday() throws Exception {
    final String now = "06.02.2018 10:35:30";
    final String start = "04.02.2018 00:00:00";

    final ZonedDateTime zdtNow = TimeUtil.getZonedDateTime(now, this.zoneId);
    final ZonedDateTime zdtStart = TimeUtil.getZonedDateTime(start, this.zoneId);

    final TimeInterval timeInterval = TimeUtil.getInterval(TimeUtil.THIS_WEEK, zdtNow, "Sunday");

    assertTrue(zdtStart.isEqual(timeInterval.getStartTime()));
    assertTrue(zdtNow.isEqual(timeInterval.getEndTime()));
  }

  /**
   * TestCase Expression lastWeek | Date(Now) 06.02.2018 10:35:30 | Resulting Start: 28.01.2018
   * 00:00:00 | Resulting End: 03.02.2018 23:59:59
   *
   * @throws Exception
   */
  @Test
  void IntervalTestLastWeekSunday() throws Exception {
    final String now = "06.02.2018 10:35:30";
    final String start = "28.01.2018 00:00:00";
    final String end = "03.02.2018 23:59:59";

    final ZonedDateTime zdtNow = TimeUtil.getZonedDateTime(now, this.zoneId);
    final ZonedDateTime zdtStart = TimeUtil.getZonedDateTime(start, this.zoneId);
    final ZonedDateTime zdtEnd = TimeUtil.getZonedDateTime(end, this.zoneId);

    final TimeInterval timeInterval = TimeUtil.getInterval(TimeUtil.LAST_WEEK, zdtNow, "SUNDAY");

    assertTrue(zdtStart.isEqual(timeInterval.getStartTime()));
    assertTrue(zdtEnd.isEqual(timeInterval.getEndTime()));
  }

}
