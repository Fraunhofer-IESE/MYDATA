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

package de.fraunhofer.iese.mydata.policy.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.fraunhofer.iese.mydata.policy.parameter.Parameter;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;

import org.junit.jupiter.api.Test;

import java.time.Instant;

class EventTest {

  @Test
  void newEvent_whenActionIdAndInstant_thenOk() {
    final ActionId actionId = new ActionId("urn:action:test-solution:action1234");
    final Instant instant = Instant.now();
    final Event event = new Event(actionId, instant);

    assertEquals((Long) instant.toEpochMilli(), event.getTimestamp());
  }

  @Test
  void newEvent_whenActionIdAndParams_thenOk() {
    final ActionId actionId = new ActionId("urn:action:test-solution:action1234");
    final Parameter param1 = new Parameter<>("eventType", true);
    final Parameter param2 = new Parameter<>("id", Integer.valueOf("123"));

    final Event event = new Event(actionId, param1, param2);

    assertEquals(2, event.getParameters().size());
    assertEquals(param1, event.getParameterForName(param1.getName()));
    assertEquals(param2, event.getParameterForName(param2.getName()));
  }

  @Test
  void newEvent_whenActionIdIsNull_throwException() {
    assertThrows(IllegalArgumentException.class, () -> {
      new Event(null);
    });
  }

  @Test
  void addParameterOk() {
    final ActionId actionId = new ActionId("urn:action:test-solution:action1234");
    final Instant instant = Instant.now();
    final Event event = new Event(actionId, instant);

    final Parameter<Boolean> param1 = new Parameter<>("eventType", true);
    final Parameter<Integer> param2 = new Parameter<>("id", Integer.valueOf("123"));

    event.addParameter(param1);
    event.addParameter(param2);

    assertEquals(2, event.getParameters().size());
    assertEquals(param1, event.getParameterForName(param1.getName()));
    assertEquals(param2, event.getParameterForName(param2.getName()));
  }

  @Test
  void addParameterAsKeyValueOk() {
    final ActionId actionId = new ActionId("urn:action:test-solution:action1234");
    final Instant instant = Instant.now();
    final Event event = new Event(actionId, instant);

    event.addParameter("eventType", true);
    event.addParameter("id", Integer.valueOf("123"));

    assertEquals(2, event.getParameters().size());
    assertEquals(true, event.getParameterValue("eventType", Boolean.class));
    assertEquals(Integer.valueOf("123"), event.getParameterValue("id", Integer.class));
  }

  @Test
  void clearParamater() {
    final ActionId actionId = new ActionId("urn:action:test-solution:action1234");
    final Instant instant = Instant.now();
    final Event event = new Event(actionId, instant);

    final Parameter<Boolean> param1 = new Parameter<>("eventType", true);
    final Parameter<Integer> param2 = new Parameter<>("id", Integer.valueOf("123"));

    event.addParameter(param1);
    event.addParameter(param2);

    assertEquals(2, event.getParameters().size());

    event.clearParameters();

    assertTrue(event.getParameters().isEmpty());
  }

  @Test
  void getValueForName() {
    final ActionId actionId = new ActionId("urn:action:test-solution:action1234");
    final Instant instant = Instant.now();
    final Event event = new Event(actionId, instant);

    final Parameter<Boolean> param1 = new Parameter<>("eventType", true);
    final Parameter<Integer> param2 = new Parameter<>("id", Integer.valueOf("123"));

    event.addParameter(param1);
    event.addParameter(param2);

    assertEquals(param1.getValue(), event.getValueForName(param1.getName()));
    assertEquals(param2.getValue(), event.getValueForName(param2.getName()));
  }

  @Test
  void getValueForName_whenParameterDoesNotExist_thenReturnNull() {
    final ActionId actionId = new ActionId("urn:action:test-solution:action1234");
    final Instant instant = Instant.now();
    final Event event = new Event(actionId, instant);

    assertNull(event.getValueForName("doesnotexist"));
  }

  @Test
  void removeParameter() {
    final ActionId actionId = new ActionId("urn:action:test-solution:action1234");
    final Instant instant = Instant.now();
    final Event event = new Event(actionId, instant);

    final Parameter<Boolean> param1 = new Parameter<>("eventType", true);
    final Parameter<Integer> param2 = new Parameter<>("id", Integer.valueOf("123"));

    event.addParameter(param1);
    event.addParameter(param2);

    assertEquals(2, event.getParameters().size());

    event.removeParameter(param1.getName());

    assertEquals(1, event.getParameters().size());
    assertEquals(param2, event.getParameterForName(param2.getName()));
    assertNull(event.getValueForName(param1.getName()));
  }

  @Test
  void setParameters() {
    final ActionId actionId = new ActionId("urn:action:test-solution:action1234");
    final Instant instant = Instant.now();
    final Event event = new Event(actionId, instant);

    final Parameter<Boolean> param1 = new Parameter<>("eventType", true);
    final Parameter<Integer> param2 = new Parameter<>("id", Integer.valueOf("123"));

    final ParameterList parameterList = new ParameterList();
    parameterList.add(param1);
    parameterList.add(param2);

    event.setParameters(parameterList);

    assertEquals(parameterList, event.getParameters());
    assertEquals(2, event.getParameters().size());
    assertEquals(param1, event.getParameterForName(param1.getName()));
    assertEquals(param2, event.getParameterForName(param2.getName()));
  }

  @Test
  void setTimestamp() {
    final ActionId actionId = new ActionId("urn:action:test-solution:action1234");
    final Instant instant = Instant.now();
    final Event event = new Event(actionId);

    event.setTimestamp(instant);

    assertEquals((Long) instant.toEpochMilli(), event.getTimestamp());
  }

  @Test
  void getMillisencondSinceEpoch() {
    final ActionId actionId = new ActionId("urn:action:test-solution:action1234");
    final Instant instant = Instant.now();
    final Event event = new Event(actionId, instant);

    assertEquals((Long) instant.toEpochMilli(), event.getMillisecondSinceEpoch());
  }

  @Test
  void whenEqualThenEqualsAndHashCodeOk() {
    final ActionId actionId1 = new ActionId("urn:action:test-solution:action1234");
    final Instant instant1 = Instant.now();
    final Event event1 = new Event(actionId1, instant1);

    final Parameter<Boolean> param1_1 = new Parameter<>("eventType", true);
    final Parameter<Integer> param1_2 = new Parameter<>("id", Integer.valueOf("123"));

    event1.addParameter(param1_1);
    event1.addParameter(param1_2);

    final ActionId actionId2 = new ActionId("urn:action:test-solution:action1234");
    final Instant instant2 = Instant.now();
    final Event event2 = new Event(actionId2, instant2);

    final Parameter<Boolean> param2_1 = new Parameter<>("eventType", true);
    final Parameter<Integer> param2_2 = new Parameter<>("id", Integer.valueOf("123"));

    event2.addParameter(param2_1);
    event2.addParameter(param2_2);

    assertEquals(event1, event2);
    assertEquals(event1.hashCode(), event2.hashCode());
  }

  @Test
  void whenParametersAreDifferent_thenEqualsAndHashCodeFail() {
    final ActionId actionId1 = new ActionId("urn:action:test-solution:action1234");
    final Instant instant1 = Instant.now();
    final Event event1 = new Event(actionId1, instant1);

    final Parameter<Boolean> param1_1 = new Parameter<>("eventType", true);
    final Parameter<Integer> param1_2 = new Parameter<>("id", Integer.valueOf("123"));

    event1.addParameter(param1_1);
    event1.addParameter(param1_2);

    final ActionId actionId2 = new ActionId("urn:action:test-solution:action1234");
    final Instant instant2 = Instant.now();
    final Event event2 = new Event(actionId2, instant2);

    final Parameter<Boolean> param2_1 = new Parameter<>("eventType", true);
    final Parameter<Boolean> param2_2 = new Parameter<>("otherparam", false);

    event2.addParameter(param2_1);
    event2.addParameter(param2_2);

    assertNotEquals(event1, event2);
    assertNotEquals(event1.hashCode(), event2.hashCode());
  }

  @Test
  void whenParameterListSizeIsDifferentThenEqualsAndHashCodeFail() {
    final ActionId actionId1 = new ActionId("urn:action:test-solution:action1234");
    final Instant instant1 = Instant.now();
    final Event event1 = new Event(actionId1, instant1);

    final Parameter<Boolean> param1_1 = new Parameter<>("eventType", true);
    final Parameter<Integer> param1_2 = new Parameter<>("id", Integer.valueOf("123"));

    event1.addParameter(param1_1);
    event1.addParameter(param1_2);

    final ActionId actionId2 = new ActionId("urn:action:test-solution:action1234");
    final Instant instant2 = Instant.now();
    final Event event2 = new Event(actionId2, instant2);

    final Parameter<Boolean> param2_1 = new Parameter<>("eventType", true);

    event2.addParameter(param2_1);

    assertNotEquals(event1, event2);
    assertNotEquals(event1.hashCode(), event2.hashCode());
  }
}
