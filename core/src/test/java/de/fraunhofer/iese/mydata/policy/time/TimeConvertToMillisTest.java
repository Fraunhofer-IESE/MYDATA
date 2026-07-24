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

import de.fraunhofer.iese.mydata.solution.Timezone;

import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

/**
 * The Class TimeFixedTest.
 */
class TimeConvertToMillisTest {

  /** The tz. */
  Timezone tz;

  /** The zone component_id. */
  String zoneIdBerlin = "Europe/Berlin";

  /** The zone component_id hawai. */
  String zoneIdHawai = "US/Hawaii";

  /** The zone component_id japan. */
  String zoneIdJapan = "Asia/Tokyo";

  /**
   * Simple Conversion in same TimeZone.
   *
   * @throws Exception the exception
   */
  @Test
  void SimpleConvertionTest() throws Exception {
    final long millis = 1519210028000L;
    final String stringDate = "21.02.2018 11:47:08";

    final ZonedDateTime zdt = TimeUtil.getZonedDateTime(stringDate, this.zoneIdBerlin);
    final long dateAsMillis = TimeUtil.getMillisFromZonedDateTime(zdt, this.zoneIdBerlin);

    assertEquals(millis, dateAsMillis);
  }

  /**
   * Simple Conversion in same TimeZone.
   *
   * @throws Exception the exception
   */
  @Test
  void HawaiiToBerlinConvertionTest() throws Exception {
    final long millis = 1519210028000L;
    final String stringDate = "21.02.2018 00:47:08";

    final ZonedDateTime zdt = TimeUtil.getZonedDateTime(stringDate, this.zoneIdHawai);
    final long dateAsMillis = TimeUtil.getMillisFromZonedDateTime(zdt, this.zoneIdBerlin);

    assertEquals(millis, dateAsMillis);
  }

}
