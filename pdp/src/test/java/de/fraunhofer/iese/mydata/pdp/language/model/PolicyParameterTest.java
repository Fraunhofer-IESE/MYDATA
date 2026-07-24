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

package de.fraunhofer.iese.mydata.pdp.language.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collection;

/**
 * The Class ParameterTest.
 */
public class PolicyParameterTest {
  public PolicyEvent<?> function;

  public Event event;

  public Object expectedResult;

  public Class<? extends Exception> expectedException;

  /**
   * Parameter basic tests.
   *
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   */
  @MethodSource("data")
  @ParameterizedTest
  public void parameterBasicTests(PolicyEvent<?> function, Event event, Object expectedResult,
      Class<? extends Exception> expectedException) throws EvaluationUndecidableException {
    this.initPolicyParameterTest(function, event, expectedResult, expectedException);
    if (this.expectedException != null) {
      assertThrows(this.expectedException, () -> {
        final DataObject<?> result = this.function.evaluate(this.event);
        final Object resultValue = result.getValue();
        assertEquals(this.expectedResult, resultValue);
      });
      return;
    }
    final DataObject<?> result = this.function.evaluate(this.event);
    final Object resultValue = result.getValue();
    assertEquals(this.expectedResult, resultValue);
  }

  /**
   * Data.
   *
   * @return                       the collection
   * @throws MalformedURLException
   */
  public static Collection<Object[]> data() throws MalformedURLException {
    return Arrays.asList(new Object[][] {
        {
            new NumberEvent("name", 34d), new Event(new ActionId("urn:action:te:st"),
                new de.fraunhofer.iese.mydata.policy.parameter.Parameter<>("name", 34)),
            34, null
        }, {
            new NumberEvent("name", 34d), new Event(new ActionId("urn:action:te:st")), 34.0, null
        }, {
            new NumberEvent("name", 34d), null, 34.0, null
        }, {
            new NumberEvent("name", null), new Event(new ActionId("urn:action:te:st"),
                new de.fraunhofer.iese.mydata.policy.parameter.Parameter<>("name", 34)),
            34, null
        }, {
            new StringEvent("name"),
            new Event(new ActionId("urn:action:te:st"),
                new de.fraunhofer.iese.mydata.policy.parameter.Parameter<>("name", 34)),
            34.0, EvaluationUndecidableException.class
        }, {
            new StringEvent("firstname"),
            new Event(new ActionId("urn:action:te:st"),
                new de.fraunhofer.iese.mydata.policy.parameter.Parameter<>("name", 34)),
            34, EvaluationUndecidableException.class
        }
    });
  }

  public void initPolicyParameterTest(PolicyEvent<?> function, Event event, Object expectedResult,
      Class<? extends Exception> expectedException) {
    this.function = function;
    this.event = event;
    this.expectedResult = expectedResult;
    this.expectedException = expectedException;
  }

}
