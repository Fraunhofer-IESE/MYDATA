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

package de.fraunhofer.iese.mydata.pep.enforce;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.fraunhofer.iese.mydata.pep.modifiers.basic.ReplaceModifierMethod;
import de.fraunhofer.iese.mydata.policy.decision.Modifier;
import de.fraunhofer.iese.mydata.policy.decision.ModifierEngine;
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * When wrapper Type (e.g. Long.class) is used primitive evaluation does not work.
 */
public class ProblemWithWrapperTypesTest {

  private static final String PARAMETER_NAME = "someNumber";

  private static final Long REPLACE_VALUE = 200L;

  private final JsonPathDecisionEnforcer testCandidate = new JsonPathDecisionEnforcer();

  @BeforeEach
  void setModifiers() {
    this.testCandidate.addModificationMethod(new ReplaceModifierMethod());
  }

  @Test
  void testLong() throws IOException {
    final ParameterList params = new ParameterList();
    params.addParameter("replaceWith", REPLACE_VALUE);
    final List<ModifierEngine> modifierMethod = Collections
        .singletonList(new ModifierEngine("replace", params));
    final Modifier modifier = new Modifier(PARAMETER_NAME, modifierMethod);
    final ParameterList result = new ParameterList();
    ReflectionTestUtils.invokeMethod(this.testCandidate, "doModificationForParameter", result,
        modifier, new Parameter<>(PARAMETER_NAME, 100L));
    assertEquals(REPLACE_VALUE.toString(),
        result.getParameterForName(PARAMETER_NAME).getValue().toString());
  }
}
