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
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.fraunhofer.iese.mydata.pdp.language.model.PolicyConstant;
import de.fraunhofer.iese.mydata.pdp.language.model.function.EMultiFunctionMode;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;

/**
 * The Class ExistsFunctionTest.
 */
public class ExistsFunctionTest {
  public String[] key;

  public Event event;

  public Object expectedResult;

  public Class<? extends Exception> expectedException;

  public EMultiFunctionMode mode;

  /**
   * Parameter basic tests.
   *
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   */
  @MethodSource("data")
  @ParameterizedTest
  public void parameterBasicTests(String[] key, Event event, Object expectedResult,
      Class<? extends Exception> expectedException, EMultiFunctionMode mode)
      throws EvaluationUndecidableException {
    this.initExistsFunctionTest(key, event, expectedResult, expectedException, mode);
    if (this.expectedException != null) {
      assertThrows(this.expectedException, () -> {

        final ExistsFunction function = new ExistsFunction(this.mode);
        for (final String s : this.key) {
          function.addParameter(new PolicyConstant<>(s, String.class));
        }

        final DataObject<?> result = function.evaluate(this.event);
        assertEquals(this.expectedResult, result.getValue());
      });
      return;
    }

    final ExistsFunction function = new ExistsFunction(this.mode);
    for (final String s : this.key) {
      function.addParameter(new PolicyConstant<>(s, String.class));
    }

    final DataObject<?> result = function.evaluate(this.event);
    assertEquals(this.expectedResult, result.getValue());
  }

  /**
   * Data.
   *
   * @return the collection
   */
  public static Collection<Object[]> data() {

    return Arrays.asList(new Object[][] {
        {
            new String[] {
                "bla"
            },
            new Event(new ActionId("urn:action:te:st"),
                new de.fraunhofer.iese.mydata.policy.parameter.Parameter<>("bla", "blubb")),
            true, null, EMultiFunctionMode.EXACTLY_ONE
        }, {
            new String[] {
                "blubb"
            },
            new Event(new ActionId("urn:action:te:st"),
                new de.fraunhofer.iese.mydata.policy.parameter.Parameter<>("bla", "blubb")),
            false, null, EMultiFunctionMode.ALL
        }, {
            new String[] {
                "blubb"
            }, null, false, null, EMultiFunctionMode.EXACTLY_ONE
        },

        {
            new String[] {
                "blubb", "bla"
            },
            new Event(new ActionId("urn:action:te:st"),
                new de.fraunhofer.iese.mydata.policy.parameter.Parameter<>("bla", "blubb")),
            true, null, EMultiFunctionMode.EXACTLY_ONE
        }, {
            new String[] {
                "blubb", "bla", "bla"
            },
            new Event(new ActionId("urn:action:te:st"),
                new de.fraunhofer.iese.mydata.policy.parameter.Parameter<>("bla", "blubb")),
            false, null, EMultiFunctionMode.EXACTLY_ONE
        },

        {
            new String[] {
                "blubb", "bla"
            },
            new Event(new ActionId("urn:action:te:st"),
                new de.fraunhofer.iese.mydata.policy.parameter.Parameter<>("bla", "blubb")),
            true, null, EMultiFunctionMode.AT_LEAST_ONE
        }, {
            new String[] {
                "blubb", "234", "123"
            },
            new Event(new ActionId("urn:action:te:st"),
                new de.fraunhofer.iese.mydata.policy.parameter.Parameter<>("bla", "blubb")),
            false, null, EMultiFunctionMode.AT_LEAST_ONE
        }, {
            new String[] {
                "blubb", "bla"
            },
            new Event(new ActionId("urn:action:te:st"),
                new de.fraunhofer.iese.mydata.policy.parameter.Parameter<>("bla", "blubb")),
            false, null, EMultiFunctionMode.NONE
        }, {
            new String[] {
                "blubb", "234"
            },
            new Event(new ActionId("urn:action:te:st"),
                new de.fraunhofer.iese.mydata.policy.parameter.Parameter<>("bla", "blubb")),
            true, null, EMultiFunctionMode.NONE
        },
    });
  }

  public void initExistsFunctionTest(String[] key, Event event, Object expectedResult,
      Class<? extends Exception> expectedException, EMultiFunctionMode mode) {
    this.key = key;
    this.event = event;
    this.expectedResult = expectedResult;
    this.expectedException = expectedException;
    this.mode = mode;
  }

}
