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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import de.fraunhofer.iese.mydata.pdp.PolicyDecisionPoint;
import de.fraunhofer.iese.mydata.pdp.language.model.EventOccurrence;
import de.fraunhofer.iese.mydata.pdp.language.model.When;
import de.fraunhofer.iese.mydata.pdp.language.model.function.IFunction;
import de.fraunhofer.iese.mydata.policy.Policy;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.event.history.HistoricEvent;
import de.fraunhofer.iese.mydata.policy.event.history.IEventRepository;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The Class AndOperatorTest.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ContinuousOccurrenceTest {

  public String id;

  public String changedTo;

  public Event event;

  public static ZonedDateTime zdt = Instant.now().atZone(ZoneId.of("Europe/Berlin"));

  public static ParameterList pl = new ParameterList();

  public static String block_id = "block_id";

  public static PolicyId pId;

  @Spy
  @InjectMocks
  private final PolicyDecisionPoint pdp = Mockito.spy(PolicyDecisionPoint.getInstance());

  @BeforeEach
  void setup() {
    pId = new PolicyId("urn:policy:test:test");
  }

  @AfterAll
  static void cleanUp() throws Exception {
    final Field pdpInstanceField = PolicyDecisionPoint.class.getDeclaredField("pdpInstance");
    pdpInstanceField.setAccessible(true);
    pdpInstanceField.set(null, null);
  }

  @SuppressWarnings({
      "rawtypes", "unchecked"
  })
  @Test
  void ContinuousOccurrenceBasicTests() throws Exception {

    final Field f = PolicyDecisionPoint.class.getDeclaredField("pdpInstance");
    f.setAccessible(true);
    f.set(null, this.pdp);

    final IEventRepository repo = Mockito.mock(IEventRepository.class);
    when(this.pdp.getEventRepository()).thenReturn(Optional.of(repo));

    Event event = new Event(new ActionId("urn:action:1:2"), zdt.toInstant(), pl);
    final List<HistoricEvent> eventsFiltredByParam = new ArrayList<>();
    final HistoricEvent he = new HistoricEvent(event, null);
    eventsFiltredByParam.add(he);

    when(repo.findByActionIdAndHistoricEventParametersAndOccurredAtMsBetween(any(ActionId.class), any(List.class), any(long.class), any(long.class))).thenReturn(eventsFiltredByParam);
    when(repo.findByActionIdAndOccurredAtMsBetweenParamIndependant(any(ActionId.class), any(long.class), any(long.class))).thenReturn(eventsFiltredByParam);
    doNothing().when(repo).setValueChanged(any(Policy.class), any(String.class), any(String.class));

    final ContinuousOccurrenceFunction operator = new ContinuousOccurrenceFunction();
    final List<IFunction> params = new ArrayList<>();
    final EventOccurrence eventOccurrence = new EventOccurrence();
    final When when = new When();
    when.setFixedTime("thisWeek");
    eventOccurrence.setEvent("urn:action:1:eventOcc");
    params.add(eventOccurrence);
    params.add(when);
    operator.setParameters(params);

    zdt = Instant.now().atZone(ZoneId.of("Asia/Singapore"));
    event = new Event(new ActionId("urn:action:1:2"), zdt.toInstant(), pl);
    event.setPolicyId(pId);
    event.setZoneId(ZoneId.of("Asia/Singapore"));
    operator.setMinOccurrences(0);
    operator.setMaxOccurrences(1);
    operator.setInterval("1d");
    DataObject<Boolean> result = operator.evaluate(event);
    assertEquals(true, result.getValue());

    // minOccurrences == -1 && maxOccurrences == -1

    operator.setMinOccurrences(-1);
    operator.setMaxOccurrences(-1);
    result = operator.evaluate(event);
    assertEquals(true, result.getValue());

    operator.setMinOccurrences(2);
    operator.setMaxOccurrences(5);
    result = operator.evaluate(event);
    assertEquals(false, result.getValue());

    operator.setMinOccurrences(5);
    operator.setMaxOccurrences(2);
    result = operator.evaluate(event);
    assertEquals(false, result.getValue());

    eventsFiltredByParam.clear();
    // mock no event for that time
    when(repo.findByActionIdAndHistoricEventParametersAndOccurredAtMsBetween(any(ActionId.class), any(List.class), any(long.class), any(long.class))).thenReturn(eventsFiltredByParam);
    // mock no event for that time
    when(repo.findByActionIdAndOccurredAtMsBetweenParamIndependant(any(ActionId.class), any(long.class), any(long.class))).thenReturn(eventsFiltredByParam);
    operator.setMinOccurrences(1);
    operator.setMaxOccurrences(1);
    operator.setInterval("1d");
    result = operator.evaluate(event);
    assertEquals(false, result.getValue());

    operator.setInterval("1y");
    result = operator.evaluate(event);
    assertEquals(false, result.getValue());

    operator.setInterval("1w");
    result = operator.evaluate(event);
    assertEquals(false, result.getValue());

    operator.setInterval("1h");
    result = operator.evaluate(event);
    assertEquals(false, result.getValue());

    operator.setInterval("1s");
    result = operator.evaluate(event);
    assertEquals(false, result.getValue());

    when.setStart(null);
    when.setEnd(null);
    when.setFixedTime(null);
    params.clear();
    params.add(eventOccurrence);
    params.add(when);
    operator.setParameters(params);
    result = operator.evaluate(event);
    assertEquals(false, result.getValue());
    //
    // when(repo.getValueChanged(any(String.class),
    // any(String.class))).thenReturn(null);
    // operator.setChangedTo("bar");
    // operator.setId("block_id");
    // result = operator.evaluate(event);
    // assertEquals(true, result.getValue());
    //
    // when(repo.getValueChanged(any(String.class),
    // any(String.class))).thenReturn(null);
    // operator.setChangedTo(null);
    // operator.setId("block_id");
    // result = operator.evaluate(event);
    // assertEquals(true, result.getValue());
    //

    params.clear();
    operator.setParameters(params);
    final Event evt = event;
    assertThatThrownBy(() -> operator.evaluate(evt).getValue()).isInstanceOf(IllegalArgumentException.class);
  }

}
