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

package de.fraunhofer.iese.mydata.solution;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;

class TimezoneTest {

  private static final String EUROPE_BERLIN_ZONE_ID = "Europe/Berlin";

  @Test
  void constructors() {
    final Timezone tz = new Timezone();
    tz.setZoneid(EUROPE_BERLIN_ZONE_ID);
    Assertions.assertEquals(EUROPE_BERLIN_ZONE_ID, tz.getZoneid());
    Assertions.assertEquals(ZoneId.of(EUROPE_BERLIN_ZONE_ID), ZoneId.of(tz.getZoneid()));
  }
}
