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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import de.fraunhofer.iese.mydata.pdp.PolicyDecisionPoint;
import de.fraunhofer.iese.mydata.pdp.interfaces.AbstractEventRepository;
import de.fraunhofer.iese.mydata.pdp.language.model.EventOccurrence;
import de.fraunhofer.iese.mydata.pdp.language.model.When;
import de.fraunhofer.iese.mydata.pdp.language.model.function.IFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.number.CountFunction;
import de.fraunhofer.iese.mydata.policy.Policy;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.event.history.HistoricEvent;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;
import de.fraunhofer.iese.mydata.policy.time.TimeUtil;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The Class AndOperatorTest.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CountOccurrenceTest {

  public String id;

  public String changedTo;

  public Event event;

  public static ZonedDateTime zdt = TimeUtil.getZonedDateTime("06.04.2018 12:10:10", ZoneId.of("Europe/Berlin"));

  public static ParameterList pl = new ParameterList();

  public static String block_id = "block_id";

  public static PolicyId pId;

  @Spy
  @InjectMocks
  private final PolicyDecisionPoint pdp = Mockito.spy(PolicyDecisionPoint.getInstance());

  /**
   * The logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(CountOccurrenceTest.class);

  @BeforeEach
  void setup() throws IOException {
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
  void CountOccurrenceBasicTests() throws Exception {

    final Field f = PolicyDecisionPoint.class.getDeclaredField("pdpInstance");
    f.setAccessible(true);
    f.set(null, this.pdp);

    final AbstractEventRepository repo = Mockito.mock(AbstractEventRepository.class);
    when(this.pdp.getEventRepository()).thenReturn(Optional.of(repo));

    // now is April 6th
    Event event = new Event(new ActionId("urn:action:1:2"), zdt.toInstant(), pl);
    final List<HistoricEvent> eventsFiltredByParam = Mockito.spy(new ArrayList<>());
    HistoricEvent he = new HistoricEvent(event, null);
    eventsFiltredByParam.add(he);

    // event yesterday
    event = new Event(new ActionId("urn:action:1:2"), zdt.toInstant().minus(1, ChronoUnit.DAYS), pl);
    he = new HistoricEvent(event, null);
    eventsFiltredByParam.add(he);

    // event last week
    event = new Event(new ActionId("urn:action:1:2"), zdt.toInstant().minus(8, ChronoUnit.DAYS), pl);
    he = new HistoricEvent(event, null);
    eventsFiltredByParam.add(he);

    // event last month
    event = new Event(new ActionId("urn:action:1:2"), zdt.toInstant().minus(30, ChronoUnit.DAYS), pl);
    he = new HistoricEvent(event, null);
    eventsFiltredByParam.add(he);

    when(repo.findByActionIdAndHistoricEventParametersAndOccurredAtMsBetween(any(ActionId.class), any(List.class), any(long.class), any(long.class))).thenReturn(eventsFiltredByParam);
    when(repo.findByActionIdAndOccurredAtMsBetweenParamIndependant(any(ActionId.class), any(long.class), any(long.class))).thenReturn(eventsFiltredByParam);
    doNothing().when(repo).setValueChanged(any(Policy.class), any(String.class), any(String.class));

    final CountFunction operator = new CountFunction();
    final List<IFunction> params = new ArrayList<>();
    final EventOccurrence eventOccurrence = new EventOccurrence();
    final When when = new When();
    when.setFixedTime("today");
    eventOccurrence.setEvent("urn:action:1:eventOcc");
    params.add(eventOccurrence);
    params.add(when);
    operator.setParameters(params);

    // zdt = Instant.now().atZone(ZoneId.of("Europe/Berlin"));
    event = new Event(new ActionId("urn:action:1:2"), zdt.toInstant(), pl);
    event.setPolicyId(pId);
    event.setZoneId(ZoneId.of("Europe/Berlin"));

    doReturn(filterByFixedTime(eventsFiltredByParam, "today")).when(eventsFiltredByParam).size();
    DataObject<?> result = operator.evaluate(event);
    assertEquals(1, result.getValue());

    doReturn(filterByFixedTime(eventsFiltredByParam, "thisWeek")).when(eventsFiltredByParam).size();
    params.clear();
    params.add(eventOccurrence);
    when.setFixedTime("thisWeek");
    params.add(when);
    operator.setParameters(params);
    result = operator.evaluate(event);
    assertEquals(2, result.getValue());

    doReturn(filterByFixedTime(eventsFiltredByParam, "thisMonth")).when(eventsFiltredByParam).size();
    params.clear();
    params.add(eventOccurrence);
    when.setFixedTime("thisMonth");
    params.add(when);
    operator.setParameters(params);
    result = operator.evaluate(event);
    assertEquals(2, result.getValue());

    doReturn(filterByFixedTime(eventsFiltredByParam, "lastMonth")).when(eventsFiltredByParam).size();
    params.clear();
    params.add(eventOccurrence);
    when.setFixedTime("lastMonth");
    params.add(when);
    operator.setParameters(params);
    result = operator.evaluate(event);
    // because last week was also in the previous month
    assertEquals(2, result.getValue());

    params.clear();
    operator.setParameters(params);
    final Event evt = event;
    assertThatThrownBy(() -> operator.evaluate(evt).getValue()).isInstanceOf(IllegalArgumentException.class);
  }

  private Integer filterByFixedTime(List<HistoricEvent> eventsFiltredByParam, String fixedTime) throws IllegalArgumentException {

    final ZonedDateTime start = TimeUtil.getInterval(fixedTime, zdt).getStartTime();
    final ZonedDateTime end = TimeUtil.getInterval(fixedTime, zdt).getEndTime();
    final List<HistoricEvent> filter = eventsFiltredByParam.stream()
        .filter(a -> a.getOccurredAtMs() <= end.toInstant().toEpochMilli() && a.getOccurredAtMs() >= start.toInstant().toEpochMilli()).collect(Collectors.toList());
    return filter.size();
  }

}
