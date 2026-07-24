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

package de.fraunhofer.iese.mydata.pdp.language.model.function.bool;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;
import de.fraunhofer.iese.mydata.policy.time.TimeUtil;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;

/**
 * The Class AndOperatorTest.
 */
public class DayFunctionTest {
  public Object expectedResult;
  public String day;
  public Event event;

  // 15th of May of 2018 is a Tuesday !
  public static ZonedDateTime zdt = TimeUtil.getZonedDateTime("15.05.2018 18:23:10", ZoneId.of("Europe/Berlin"));

  public static ParameterList pl = new ParameterList();

  @MethodSource("data")
  @SuppressWarnings({
      "rawtypes", "unchecked"
  })
  @ParameterizedTest
  public void DayFunctionBasicTests(Object expectedResult, String day, Event event) throws Exception {

    initDayFunctionTest(expectedResult, day, event);

    final DayFunction operator = new DayFunction(this.day);

    this.event.setZoneId(ZoneId.of("Europe/Berlin"));

    final DataObject<Boolean> result = operator.evaluate(this.event);
    assertEquals(this.expectedResult, result.getValue());
  }

  /**
   * Data.
   *
   * @return the collection
   * 
   */
  public static Collection<Object[]> data()  {
    return Arrays.asList(new Object[][] {
        {
            true, "Monday,Tuesday,Sunday", new Event(new ActionId("urn:action:1:2"), zdt.toInstant(), pl)
        }, {
            false, "Monday,Wednesday,Sunday", new Event(new ActionId("urn:action:1:2"), zdt.toInstant(), pl)
        }, {
            false, "Monday,Wednesday,Saturday,", new Event(new ActionId("urn:action:1:2"), zdt.toInstant(), pl)
        }, {
            true, "Monday,Tuesday,Saturday,", new Event(new ActionId("urn:action:1:2"), zdt.toInstant(), pl)
        }, {
            true, "Tuesday,", new Event(new ActionId("urn:action:1:2"), zdt.toInstant(), pl)
        }, {
            false, ",,", new Event(new ActionId("urn:action:1:2"), zdt.toInstant(), pl)
        }, {
            false, "...", new Event(new ActionId("urn:action:1:2"), zdt.toInstant(), pl)
        }, {
            false, "blah,blahh", new Event(new ActionId("urn:action:1:2"), zdt.toInstant(), pl)
        }
    });
  }

  public void initDayFunctionTest(Object expectedResult, String day, Event event) {
    this.expectedResult = expectedResult;
    this.day = day;
    this.event = event;
  }

}
