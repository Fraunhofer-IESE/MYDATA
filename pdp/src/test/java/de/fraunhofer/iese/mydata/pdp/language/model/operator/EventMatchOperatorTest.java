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

package de.fraunhofer.iese.mydata.pdp.language.model.operator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.fraunhofer.iese.mydata.pdp.language.model.PolicyParameter;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.EventMatchOperator;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;

import org.junit.jupiter.api.Test;

/**
 * The Class EventMatchOperatorTest.
 */
class EventMatchOperatorTest {

  /**
   * When same events then match.
   *
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   */
  @SuppressWarnings({
      "rawtypes", "unchecked"
  })
  @Test
  void whenSameEventsThenMatch() throws Exception {

    final de.fraunhofer.iese.mydata.policy.parameter.Parameter<?> p1 = new de.fraunhofer.iese.mydata.policy.parameter.Parameter<>("bla", true);
    final de.fraunhofer.iese.mydata.policy.parameter.Parameter<?> p2 = new de.fraunhofer.iese.mydata.policy.parameter.Parameter<>("blubb", "blub");

    final Event e = new Event(new ActionId("urn:action:1:2"), p1, p2);

    final EventMatchOperator emo = new EventMatchOperator();
    emo.setAction("urn:action:1:2");
    emo.addParameter(new PolicyParameter(p1.getName(), p1.getValue(), p1.getType()));
    emo.addParameter(new PolicyParameter(p2.getName(), p2.getValue(), p2.getType()));

    assertEquals(true, emo.evaluate(e).getValue());
  }

  /**
   * When different action then false.
   *
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   */
  @Test
  void whenDifferentActionThenFalse() throws Exception {

    final de.fraunhofer.iese.mydata.policy.parameter.Parameter<?> p1 = new de.fraunhofer.iese.mydata.policy.parameter.Parameter<>("bla", true);
    final de.fraunhofer.iese.mydata.policy.parameter.Parameter<?> p2 = new de.fraunhofer.iese.mydata.policy.parameter.Parameter<>("blubb", "blub");

    final Event e = new Event(new ActionId("urn:action:1:2"), p1, p2);

    final EventMatchOperator emo = new EventMatchOperator();
    emo.setAction("urn:action:4:6");
    // emo.setTry(false);
    emo.addParameter(new PolicyParameter(p1.getName(), p1.getValue(), p1.getType()));
    emo.addParameter(new PolicyParameter(p2.getName(), p2.getValue(), p2.getType()));

    assertEquals(false, emo.evaluate(e).getValue());
  }

  /**
   * When missing parameter then false.
   *
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   */
  @Test
  void whenMissingParameterThenFalse() throws Exception {

    final de.fraunhofer.iese.mydata.policy.parameter.Parameter<?> p1 = new de.fraunhofer.iese.mydata.policy.parameter.Parameter<>("bla", true);

    final Event e = new Event(new ActionId("urn:action:1:2"), p1);

    final EventMatchOperator emo = new EventMatchOperator();
    emo.setAction("urn:action:1:2");
    // emo.setTry(false);
    emo.addParameter(new PolicyParameter(p1.getName(), p1.getValue(), p1.getType()));
    emo.addParameter(new PolicyParameter("hans", "wurst"));

    assertEquals(false, emo.evaluate(e).getValue());
  }

}
