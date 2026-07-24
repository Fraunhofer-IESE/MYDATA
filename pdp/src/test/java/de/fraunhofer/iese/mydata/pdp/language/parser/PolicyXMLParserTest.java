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

package de.fraunhofer.iese.mydata.pdp.language.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.fraunhofer.iese.mydata.pdp.language.model.NumberPolicyConstant;
import de.fraunhofer.iese.mydata.pdp.language.model.Policy;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyIfBlock;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyMechanism;
import de.fraunhofer.iese.mydata.pdp.language.model.function.IFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.AndFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.PipOperator;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

class PolicyXMLParserTest {

  private final PolicyXmlParser testCandidate = new PolicyXmlParser();

  @Test
  void simplePolicy() throws Exception {
    testCandidate.parsePolicyText(this.readResourceFile("policyConditionOperatorTestAnd.xml"));
  }

  /**
   * Tests if the value can be read after parsing the policy
   *
   * @throws URISyntaxException
   * @throws IOException
   */
  @Test
  void varDeclExecutePolicyTest() throws Exception {
    final Policy policy = testCandidate.parsePolicyText(this.readResourceFile("policyVariableDeclarationDecisionExecution.xml"));
    final Event event = new Event(new ActionId("urn:action:test:executeWithVar"));
    Assertions.assertDoesNotThrow(() -> {
      policy.getMechanisms().get(0).setPolicy(policy);
      final NumberPolicyConstant numberPolicyConstant = new NumberPolicyConstant();
      numberPolicyConstant.setValue(14.0);
      assertEquals(numberPolicyConstant.getValue(),
          ((NumberPolicyConstant) policy.getMechanisms().get(0).getIfBlock().getDecision().getExecuteActions().get(0).getParameters().get(0).evaluate(event)).getValue());
    });
  }

  // IND2UCE-3367
  @Test
  void varDeclUnconditionalExecutePolicyTest() throws Exception {
    final Policy policy = testCandidate.parsePolicyText(this.readResourceFile("IND2UCE-3367-policyVariableDeclarationUnconditionalExecution.xml"));
    final Event event = new Event(new ActionId("urn:action:test:executeWithVar"));
    Assertions.assertDoesNotThrow(() -> {
      policy.getMechanisms().get(0).setPolicy(policy);
      final NumberPolicyConstant numberPolicyConstant = new NumberPolicyConstant();
      numberPolicyConstant.setValue(14.0);
      assertEquals(numberPolicyConstant.getValue(),
          ((NumberPolicyConstant) policy.getMechanisms().get(0).getExecuteActions().get(0).getParameters().get(0).evaluate(event)).getValue());
    });
  }

  // IND2UCE-3437
  @Test
  void whenParsePolicyText_withPolicyContainingPipWithTtl_thenTtlShouldBeInTheResultingPolicyModel() throws Exception {
    final Policy policy = testCandidate.parsePolicyText(this.readResourceFile("IND2UCE-3437-pip-with-ttl.xml"));
    final PolicyMechanism policyMechanism = policy.getMechanisms().get(0);
    final PolicyIfBlock ifBlock = policyMechanism.getIfBlock();
    final AndFunction andFunction = (AndFunction) ifBlock.getOperator();
    final List<IFunction> andFunctionParameters = andFunction.getParameters();
    assertEquals(3, andFunctionParameters.size());

    final PipOperator<?> pipOperatorWithTtl = (PipOperator<?>) andFunctionParameters.get(0);
    assertNotNull(pipOperatorWithTtl);
    assertEquals("123s", pipOperatorWithTtl.getTtl());
    assertEquals(123000L, pipOperatorWithTtl.getTimeToLive());

    final PipOperator<?> pipOperatorWithEmptyStringTtl = (PipOperator<?>) andFunctionParameters.get(1);
    assertNotNull(pipOperatorWithEmptyStringTtl);
    assertEquals("", pipOperatorWithEmptyStringTtl.getTtl());
    assertEquals(0L, pipOperatorWithEmptyStringTtl.getTimeToLive());

    final PipOperator<?> pipOperatorWithoutTtl = (PipOperator<?>) andFunctionParameters.get(2);
    assertNotNull(pipOperatorWithoutTtl);
    assertNull(pipOperatorWithoutTtl.getTtl());
    assertEquals(0L, pipOperatorWithoutTtl.getTimeToLive());
  }

  /**
   * Read resource file.
   *
   * @param file the file
   * @return the string
   * @throws IOException        Signals that an I/O exception has occurred.
   * @throws URISyntaxException the URI syntax exception
   */
  private String readResourceFile(String file) throws IOException, URISyntaxException {
    return new String(Files.readAllBytes(Paths.get(this.getClass().getClassLoader().getResource(file).toURI())));
  }
}
