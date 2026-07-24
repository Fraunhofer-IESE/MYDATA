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
import de.fraunhofer.iese.mydata.pdp.interfaces.AbstractEventRepository;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyConstant;
import de.fraunhofer.iese.mydata.pdp.language.model.function.IFunction;
import de.fraunhofer.iese.mydata.policy.Policy;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;
import de.fraunhofer.iese.mydata.policy.time.TimeUtil;

import org.apache.commons.codec.digest.DigestUtils;
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
public class ValueChangeTest {

  public String id;

  public String changedTo;

  public Event event;

  public static ZonedDateTime zdt = TimeUtil.getZonedDateTime("15.05.2018 00:00:00", ZoneId.of("Europe/Berlin"));

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
  void ValueChangeFunctionBasicTests() throws Exception {
    Field f = PolicyDecisionPoint.class.getDeclaredField("pdpInstance");
    f.setAccessible(true);
    f.set(null, this.pdp);

    final AbstractEventRepository repo = Mockito.mock(AbstractEventRepository.class);
    when(this.pdp.getEventRepository()).thenReturn(Optional.of(repo));
    when(repo.getValueChanged(any(String.class), any(Policy.class))).thenReturn(null);
    doNothing().when(repo).setValueChanged(any(Policy.class), any(String.class), any(String.class));

    final ValueChangedFunction operator = new ValueChangedFunction(this.id, this.changedTo);
    final List<IFunction> params = new ArrayList<>();
    params.add(new PolicyConstant("foo"));
    operator.setParameters(params);

    event = new Event(new ActionId("urn:action:1:2"), zdt.toInstant(), pl);
    event.setPolicyId(pId);
    event.setZoneId(ZoneId.of("Europe/Berlin"));
    operator.setChangedTo("foo");
    operator.setId("block_id");
    DataObject<Boolean> result = operator.evaluate(event);
    assertEquals(true, result.getValue());

    when(repo.getValueChanged(any(String.class), any(Policy.class))).thenReturn(DigestUtils.sha256Hex("foo"));
    params.clear();
    params.add(new PolicyConstant("bar"));
    operator.setParameters(params);
    operator.setChangedTo("bar");
    operator.setId("block_id");
    result = operator.evaluate(event);
    assertEquals(true, result.getValue());

    when(repo.getValueChanged(any(String.class), any(Policy.class))).thenReturn(DigestUtils.sha256Hex("bar"));
    operator.setChangedTo("bar");
    operator.setId("block_id");
    result = operator.evaluate(event);
    assertEquals(false, result.getValue());

    when(repo.getValueChanged(any(String.class), any(Policy.class))).thenReturn(null);
    operator.setChangedTo("bar");
    operator.setId("block_id");
    result = operator.evaluate(event);
    assertEquals(true, result.getValue());

    when(repo.getValueChanged(any(String.class), any(Policy.class))).thenReturn(null);
    operator.setChangedTo(null);
    operator.setId("block_id");
    result = operator.evaluate(event);
    assertEquals(true, result.getValue());

    when(repo.getValueChanged(any(String.class), any(Policy.class))).thenReturn(DigestUtils.sha256Hex("foo"));
    params.clear();
    operator.setParameters(params);
    operator.setChangedTo("bar");
    operator.setId("block_id");
    assertThatThrownBy(() -> operator.evaluate(event).getValue()).isInstanceOf(IllegalArgumentException.class);
  }

}
