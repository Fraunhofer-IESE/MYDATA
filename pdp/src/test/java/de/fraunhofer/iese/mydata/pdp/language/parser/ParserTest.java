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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.pdp.language.model.BooleanEvent;
import de.fraunhofer.iese.mydata.pdp.language.model.Policy;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyConstant;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyDecision;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyEvent;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyMechanism;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyParameter;
import de.fraunhofer.iese.mydata.pdp.language.model.function.EMultiFunctionMode;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.AndFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.ContainsFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.EqualsFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.ExistsFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.GreaterEqualFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.GreaterFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.ImpliesFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.LessEqualFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.LessFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.NotFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.OrFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.PipOperator;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.RegexFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.number.SizeFunction;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * The Class ParserTest.
 */
class ParserTest {

  @Test
  void policyWithPipObject() throws Exception {
    final Policy p = new PolicyXmlParser()
        .parsePolicyText(this.readResourceFile("policyPipObject.xml"));

    assertEquals("urn:policy:spring:encryptCustomer", p.getPolicyId().getUrn());
  }

  /**
   * Test parse.
   *
   * @throws URISyntaxException             the URI syntax exception
   * @throws IllegalArgumentException       the illegal argument exception
   * @throws IOException                    Signals that an I/O exception has occurred.
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   * @throws InvalidEntityException
   */
  @Test
  void parse40() throws Exception {
    final Policy p = new PolicyXmlParser()
        .parsePolicyText(this.readResourceFile("policyOk_4_0.xml"));

    // Policy
    assertNotNull(p.getPolicyId());
    assertEquals("urn:policy:test:b", p.getPolicyId().toString());
    assertEquals("Policy for Fleet PoC Use Case 1", p.getDescription());
    assertEquals(2, p.getMechanisms().size());

    // First Mechanism

    final PolicyMechanism mechanim1 = p.getMechanisms().get(0);

    assertThat(mechanim1.getId(),
        allOf(notNullValue(), equalTo("decideOnStorageOfPositionUpdates")));
    assertNotNull(mechanim1.getEvent());
    assertEquals("urn:action:test:cr-writeData", mechanim1.getEvent().toString());
    final AndFunction andFunction = (AndFunction) mechanim1.getIfBlock().getOperator();
    assertThat(andFunction.getParameters(), allOf(notNullValue(), hasSize(15)));
    this.checkFirstParameter(andFunction);
    this.checkContainsFunction(andFunction);
    this.checkEqualsFunctionWithConstant(andFunction);
    this.checkEventBoolean(andFunction);
    this.checkEventHasParameter(andFunction);

    // NOT
    final NotFunction m1pNotOperator = (NotFunction) andFunction.getParameters().get(13);
    assertInstanceOf(EqualsFunction.class, m1pNotOperator.getParameters().get(0));
    final EqualsFunction m1pEqualsFunction = (EqualsFunction) m1pNotOperator.getParameters().get(0);
    assertEquals(10, m1pEqualsFunction.getParameters().size());

    assertInstanceOf(ContainsFunction.class, m1pEqualsFunction.getParameters().get(0));
    final ContainsFunction m1pContainsFunction = (ContainsFunction) m1pEqualsFunction
        .getParameters().get(0);
    assertEquals(EMultiFunctionMode.NONE, m1pContainsFunction.getFunctionMode());

    assertInstanceOf(PipOperator.class, m1pContainsFunction.getParameters().get(0));
    assertEquals("urn:info:test:bla", ((PipOperator) m1pContainsFunction.getParameters().get(0)).getMethodName());
    final DataObject<?> defaultValue = ((PipOperator) m1pContainsFunction.getParameters().get(0))
        .getDefaultReturnValue();
    assertEquals(ArrayList.class, defaultValue.getType());
    assertEquals(2, ((ArrayList<Object>) defaultValue.getValue()).size());
    assertThat((ArrayList<Object>) defaultValue.getValue(), hasItem(2.0));

    assertInstanceOf(PolicyConstant.class, m1pContainsFunction.getParameters().get(1));
    assertEquals(2.0d, ((PolicyConstant) m1pContainsFunction.getParameters().get(1)).getValue());

    assertInstanceOf(PolicyConstant.class, m1pContainsFunction.getParameters().get(2));
    assertEquals(false, ((PolicyConstant) m1pContainsFunction.getParameters().get(2)).getValue());

    assertInstanceOf(PolicyConstant.class, m1pContainsFunction.getParameters().get(3));
    assertEquals(Arrays.asList(1.0, 2.0),
        ((PolicyConstant) m1pContainsFunction.getParameters().get(3)).getValue());

    assertInstanceOf(PolicyConstant.class, m1pContainsFunction.getParameters().get(4));
    assertEquals("blubb", ((PolicyConstant) m1pContainsFunction.getParameters().get(4)).getValue());

    // Condition, exists 1
    final ExistsFunction m1pExistsFunction = (ExistsFunction) m1pEqualsFunction.getParameters()
        .get(1);
    assertThat(m1pExistsFunction.getParameters(), allOf(notNullValue(), hasSize(1)));
    assertInstanceOf(PolicyConstant.class, m1pExistsFunction.getParameters().get(0));
    assertEquals("name", m1pExistsFunction.getParameters().get(0).evaluate(null).getValue());

    // Condition, exists 2
    final ExistsFunction m1pExistsFunction2 = (ExistsFunction) m1pEqualsFunction.getParameters()
        .get(2);
    assertThat(m1pExistsFunction2.getParameters(), allOf(notNullValue(), hasSize(2)));
    assertInstanceOf(PolicyConstant.class, m1pExistsFunction2.getParameters().get(0));
    assertEquals(EMultiFunctionMode.AT_LEAST_ONE, m1pExistsFunction2.getFunctionMode());
    assertEquals("bla", m1pExistsFunction2.getParameters().get(0).evaluate(null).getValue());
    assertEquals("blubb", m1pExistsFunction2.getParameters().get(1).evaluate(null).getValue());

    // Condition, greater
    final GreaterFunction m1pGreaterFunction = (GreaterFunction) m1pEqualsFunction.getParameters()
        .get(3);
    assertThat(m1pGreaterFunction.getParameters(), allOf(notNullValue(), hasSize(2)));
    assertInstanceOf(SizeFunction.class, m1pGreaterFunction.getParameters().get(0));
    assertEquals(1, ((SizeFunction) m1pGreaterFunction.getParameters().get(0)).getParameters().size());
    assertInstanceOf(PolicyConstant.class, m1pGreaterFunction.getParameters().get(1));
    assertEquals(234,
        (Double) (m1pGreaterFunction.getParameters().get(1).evaluate(null).getValue()), 0.000001);

    // Condition, greaterEqual
    final GreaterEqualFunction m1pGreaterEqualFunction = (GreaterEqualFunction) m1pEqualsFunction
        .getParameters().get(4);
    assertThat(m1pGreaterEqualFunction.getParameters(), allOf(notNullValue(), hasSize(2)));
    assertInstanceOf(SizeFunction.class, m1pGreaterEqualFunction.getParameters().get(0));
    assertEquals(1, ((SizeFunction) m1pGreaterEqualFunction.getParameters().get(0)).getParameters().size());
    assertInstanceOf(PolicyConstant.class, m1pGreaterEqualFunction.getParameters().get(1));
    assertEquals(234,
        (Double) (m1pGreaterEqualFunction.getParameters().get(1).evaluate(null).getValue()),
        0.000001);

    // Condition, less
    final LessFunction m1pLessFunction = (LessFunction) m1pEqualsFunction.getParameters().get(5);
    assertThat(m1pLessFunction.getParameters(), allOf(notNullValue(), hasSize(2)));
    assertInstanceOf(SizeFunction.class, m1pLessFunction.getParameters().get(0));
    assertEquals(1, ((SizeFunction) m1pLessFunction.getParameters().get(0)).getParameters().size());
    assertInstanceOf(PolicyConstant.class, m1pLessFunction.getParameters().get(1));
    assertEquals(234, (Double) (m1pLessFunction.getParameters().get(1).evaluate(null).getValue()),
        0.000001);

    // Condition, less
    final LessEqualFunction m1pLessEqualFunction = (LessEqualFunction) m1pEqualsFunction
        .getParameters().get(6);
    assertThat(m1pLessEqualFunction.getParameters(), allOf(notNullValue(), hasSize(2)));
    assertInstanceOf(SizeFunction.class, m1pLessEqualFunction.getParameters().get(0));
    assertEquals(1, ((SizeFunction) m1pLessEqualFunction.getParameters().get(0)).getParameters().size());
    assertInstanceOf(PolicyConstant.class, m1pLessEqualFunction.getParameters().get(1));
    assertEquals(234,
        (Double) (m1pLessEqualFunction.getParameters().get(1).evaluate(null).getValue()), 0.000001);

    // Condition, Regex1
    final RegexFunction m1pRegexFunction1 = (RegexFunction) m1pEqualsFunction.getParameters()
        .get(7);
    assertThat(m1pRegexFunction1.getParameters(), allOf(notNullValue(), hasSize(1)));
    assertInstanceOf(PolicyEvent.class, m1pRegexFunction1.getParameters().get(0));
    assertEquals(".*\\.docx", m1pRegexFunction1.getRegex());
    assertEquals(EMultiFunctionMode.ALL, m1pRegexFunction1.getFunctionMode());

    // Condition, Regex2
    final RegexFunction m1pRegexFunction2 = (RegexFunction) m1pEqualsFunction.getParameters()
        .get(8);
    assertThat(m1pRegexFunction2.getParameters(), allOf(notNullValue(), hasSize(2)));
    assertInstanceOf(PolicyEvent.class, m1pRegexFunction2.getParameters().get(0));
    assertInstanceOf(PolicyEvent.class, m1pRegexFunction2.getParameters().get(1));
    assertEquals(".*\\.docx", m1pRegexFunction2.getRegex());
    assertEquals(EMultiFunctionMode.EXACTLY_ONE, m1pRegexFunction2.getFunctionMode());

    // OR
    final OrFunction m1pOrOperator = (OrFunction) andFunction.getParameters().get(14);
    assertThat(m1pOrOperator.getParameters(), allOf(notNullValue(), hasSize(3)));

    assertThat(m1pOrOperator.getParameters().get(0),
        allOf(notNullValue(), instanceOf(PolicyConstant.class)));
    assertEquals(false, m1pOrOperator.getParameters().get(0).evaluate(null).getValue());

    assertThat(m1pOrOperator.getParameters().get(1),
        allOf(notNullValue(), instanceOf(PolicyConstant.class)));
    assertEquals(true, m1pOrOperator.getParameters().get(1).evaluate(null).getValue());

    assertThat(m1pOrOperator.getParameters().get(2),
        allOf(notNullValue(), instanceOf(ImpliesFunction.class)));
    final ImpliesFunction m1pImpliesOperator = (ImpliesFunction) m1pOrOperator.getParameters()
        .get(2);

    assertEquals(2, m1pImpliesOperator.getParameters().size());

    assertInstanceOf(PipOperator.class, m1pImpliesOperator.getParameters().get(1));

    // DECISION
    final PolicyDecision allow = mechanim1.getIfBlock().getDecision();
    assertNotNull(allow);
    assertEquals(false, allow.isAllowed().isPresent());

    assertThat(allow.getModifiers(), allOf(notNullValue(), hasSize(2)));
    assertThat(allow.getModifiers().get(0).getEventParameter(),
        allOf(notNullValue(), equalTo("latitude")));
    assertEquals(2, allow.getModifiers().size());
    assertEquals("delete", allow.getModifiers().get(0).getMethod());

    final PolicyDecision allowFallback = mechanim1.getElseBlock();
    assertNotNull(allowFallback);
    assertEquals(false, allowFallback.isAllowed().isPresent());

    assertThat(allowFallback.getModifiers(), allOf(notNullValue(), hasSize(2)));
    assertThat(allowFallback.getModifiers().get(0).getEventParameter(),
        allOf(notNullValue(), equalTo("latitude")));
    assertEquals("blur", allowFallback.getModifiers().get(0).getMethod());

    // Optional Execute Actions
    assertEquals(1, mechanim1.getExecuteActions().size());
    assertEquals("urn:action:test:sendmail", mechanim1.getExecuteActions().get(0).getName());
    assertEquals(1, mechanim1.getExecuteActions().get(0).getParameters().size());
    assertInstanceOf(PolicyParameter.class, mechanim1.getExecuteActions().get(0).getParameters().get(0));
    assertEquals("msg", mechanim1.getExecuteActions().get(0).getParameters().get(0).getName());
    assertEquals("hallo", (String) mechanim1.getExecuteActions().get(0).getParameters().get(0).evaluate(null)
        .getValue());
  }

  /**
   * Test parse.
   *
   * @throws URISyntaxException             the URI syntax exception
   * @throws IllegalArgumentException       the illegal argument exception
   * @throws IOException                    Signals that an I/O exception has occurred.
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   */
  @Test
  void parse32() throws Exception {
    final Policy p = new PolicyXmlParser()
        .parsePolicyText(this.readResourceFile("policyOk_3_2.xml"));

    // Policy
    assertNotNull(p.getPolicyId());
    assertEquals("urn:policy:test:b", p.getPolicyId().toString());
    assertEquals("Policy for Fleet PoC Use Case 1", p.getDescription());
    assertEquals(2, p.getMechanisms().size());

    // First Mechanism

    final PolicyMechanism mechanim1 = p.getMechanisms().get(0);

    assertThat(mechanim1.getId(),
        allOf(notNullValue(), equalTo("decideOnStorageOfPositionUpdates")));
    assertNotNull(mechanim1.getEvent());
    assertEquals("urn:action:test:cr-writeData", mechanim1.getEvent().toString());
    final AndFunction andFunction = (AndFunction) mechanim1.getIfBlock().getOperator();
    assertThat(andFunction.getParameters(), allOf(notNullValue(), hasSize(15)));
    this.checkFirstParameter(andFunction);
    this.checkContainsFunction(andFunction);
    this.checkEqualsFunctionWithConstant(andFunction);
    this.checkEventBoolean(andFunction);
    this.checkEventHasParameter(andFunction);

    // NOT
    final NotFunction m1pNotOperator = (NotFunction) andFunction.getParameters().get(13);
    assertInstanceOf(EqualsFunction.class, m1pNotOperator.getParameters().get(0));
    final EqualsFunction m1pEqualsFunction = (EqualsFunction) m1pNotOperator.getParameters().get(0);
    assertEquals(10, m1pEqualsFunction.getParameters().size());

    assertInstanceOf(ContainsFunction.class, m1pEqualsFunction.getParameters().get(0));
    final ContainsFunction m1pContainsFunction = (ContainsFunction) m1pEqualsFunction
        .getParameters().get(0);
    assertEquals(EMultiFunctionMode.NONE, m1pContainsFunction.getFunctionMode());

    assertInstanceOf(PipOperator.class, m1pContainsFunction.getParameters().get(0));
    assertEquals("urn:info:test:bla", ((PipOperator) m1pContainsFunction.getParameters().get(0)).getMethodName());
    final DataObject<?> defaultValue = ((PipOperator) m1pContainsFunction.getParameters().get(0))
        .getDefaultReturnValue();
    assertEquals(ArrayList.class, defaultValue.getType());
    assertEquals(2, ((ArrayList<Object>) defaultValue.getValue()).size());
    assertThat((ArrayList<Object>) defaultValue.getValue(), hasItem(2.0));

    assertInstanceOf(PolicyConstant.class, m1pContainsFunction.getParameters().get(1));
    assertEquals(2.0d, ((PolicyConstant) m1pContainsFunction.getParameters().get(1)).getValue());

    assertInstanceOf(PolicyConstant.class, m1pContainsFunction.getParameters().get(2));
    assertEquals(false, ((PolicyConstant) m1pContainsFunction.getParameters().get(2)).getValue());

    assertInstanceOf(PolicyConstant.class, m1pContainsFunction.getParameters().get(3));
    assertEquals(Arrays.asList(1.0, 2.0),
        ((PolicyConstant) m1pContainsFunction.getParameters().get(3)).getValue());

    assertInstanceOf(PolicyConstant.class, m1pContainsFunction.getParameters().get(4));
    assertEquals("blubb", ((PolicyConstant) m1pContainsFunction.getParameters().get(4)).getValue());

    // Condition, exists 1
    final ExistsFunction m1pExistsFunction = (ExistsFunction) m1pEqualsFunction.getParameters()
        .get(1);
    assertThat(m1pExistsFunction.getParameters(), allOf(notNullValue(), hasSize(1)));
    assertInstanceOf(PolicyConstant.class, m1pExistsFunction.getParameters().get(0));
    assertEquals("name", m1pExistsFunction.getParameters().get(0).evaluate(null).getValue());

    // Condition, exists 2
    final ExistsFunction m1pExistsFunction2 = (ExistsFunction) m1pEqualsFunction.getParameters()
        .get(2);
    assertThat(m1pExistsFunction2.getParameters(), allOf(notNullValue(), hasSize(2)));
    assertInstanceOf(PolicyConstant.class, m1pExistsFunction2.getParameters().get(0));
    assertEquals(EMultiFunctionMode.AT_LEAST_ONE, m1pExistsFunction2.getFunctionMode());
    assertEquals("bla", m1pExistsFunction2.getParameters().get(0).evaluate(null).getValue());
    assertEquals("blubb", m1pExistsFunction2.getParameters().get(1).evaluate(null).getValue());

    // Condition, greater
    final GreaterFunction m1pGreaterFunction = (GreaterFunction) m1pEqualsFunction.getParameters()
        .get(3);
    assertThat(m1pGreaterFunction.getParameters(), allOf(notNullValue(), hasSize(2)));
    assertInstanceOf(SizeFunction.class, m1pGreaterFunction.getParameters().get(0));
    assertEquals(1, ((SizeFunction) m1pGreaterFunction.getParameters().get(0)).getParameters().size());
    assertInstanceOf(PolicyConstant.class, m1pGreaterFunction.getParameters().get(1));
    assertEquals(234,
        (Double) (m1pGreaterFunction.getParameters().get(1).evaluate(null).getValue()), 0.000001);

    // Condition, greaterEqual
    final GreaterEqualFunction m1pGreaterEqualFunction = (GreaterEqualFunction) m1pEqualsFunction
        .getParameters().get(4);
    assertThat(m1pGreaterEqualFunction.getParameters(), allOf(notNullValue(), hasSize(2)));
    assertInstanceOf(SizeFunction.class, m1pGreaterEqualFunction.getParameters().get(0));
    assertEquals(1, ((SizeFunction) m1pGreaterEqualFunction.getParameters().get(0)).getParameters().size());
    assertInstanceOf(PolicyConstant.class, m1pGreaterEqualFunction.getParameters().get(1));
    assertEquals(234,
        (Double) (m1pGreaterEqualFunction.getParameters().get(1).evaluate(null).getValue()),
        0.000001);

    // Condition, less
    final LessFunction m1pLessFunction = (LessFunction) m1pEqualsFunction.getParameters().get(5);
    assertThat(m1pLessFunction.getParameters(), allOf(notNullValue(), hasSize(2)));
    assertInstanceOf(SizeFunction.class, m1pLessFunction.getParameters().get(0));
    assertEquals(1, ((SizeFunction) m1pLessFunction.getParameters().get(0)).getParameters().size());
    assertInstanceOf(PolicyConstant.class, m1pLessFunction.getParameters().get(1));
    assertEquals(234, (Double) (m1pLessFunction.getParameters().get(1).evaluate(null).getValue()),
        0.000001);

    // Condition, less
    final LessEqualFunction m1pLessEqualFunction = (LessEqualFunction) m1pEqualsFunction
        .getParameters().get(6);
    assertThat(m1pLessEqualFunction.getParameters(), allOf(notNullValue(), hasSize(2)));
    assertInstanceOf(SizeFunction.class, m1pLessEqualFunction.getParameters().get(0));
    assertEquals(1, ((SizeFunction) m1pLessEqualFunction.getParameters().get(0)).getParameters().size());
    assertInstanceOf(PolicyConstant.class, m1pLessEqualFunction.getParameters().get(1));
    assertEquals(234,
        (Double) (m1pLessEqualFunction.getParameters().get(1).evaluate(null).getValue()), 0.000001);

    // Condition, Regex1
    final RegexFunction m1pRegexFunction1 = (RegexFunction) m1pEqualsFunction.getParameters()
        .get(7);
    assertThat(m1pRegexFunction1.getParameters(), allOf(notNullValue(), hasSize(1)));
    assertInstanceOf(PolicyEvent.class, m1pRegexFunction1.getParameters().get(0));
    assertEquals(".*\\.docx", m1pRegexFunction1.getRegex());
    assertEquals(EMultiFunctionMode.ALL, m1pRegexFunction1.getFunctionMode());

    // Condition, Regex2
    final RegexFunction m1pRegexFunction2 = (RegexFunction) m1pEqualsFunction.getParameters()
        .get(8);
    assertThat(m1pRegexFunction2.getParameters(), allOf(notNullValue(), hasSize(2)));
    assertInstanceOf(PolicyEvent.class, m1pRegexFunction2.getParameters().get(0));
    assertInstanceOf(PolicyEvent.class, m1pRegexFunction2.getParameters().get(1));
    assertEquals(".*\\.docx", m1pRegexFunction2.getRegex());
    assertEquals(EMultiFunctionMode.EXACTLY_ONE, m1pRegexFunction2.getFunctionMode());

    // OR
    final OrFunction m1pOrOperator = (OrFunction) andFunction.getParameters().get(14);
    assertThat(m1pOrOperator.getParameters(), allOf(notNullValue(), hasSize(3)));

    assertThat(m1pOrOperator.getParameters().get(0),
        allOf(notNullValue(), instanceOf(PolicyConstant.class)));
    assertEquals(false, m1pOrOperator.getParameters().get(0).evaluate(null).getValue());

    assertThat(m1pOrOperator.getParameters().get(1),
        allOf(notNullValue(), instanceOf(PolicyConstant.class)));
    assertEquals(true, m1pOrOperator.getParameters().get(1).evaluate(null).getValue());

    assertThat(m1pOrOperator.getParameters().get(2),
        allOf(notNullValue(), instanceOf(ImpliesFunction.class)));
    final ImpliesFunction m1pImpliesOperator = (ImpliesFunction) m1pOrOperator.getParameters()
        .get(2);

    assertEquals(2, m1pImpliesOperator.getParameters().size());

    assertInstanceOf(PipOperator.class, m1pImpliesOperator.getParameters().get(1));

    // DECISION
    final PolicyDecision allow = mechanim1.getIfBlock().getDecision();
    assertNotNull(allow);
    assertEquals(false, allow.isAllowed().isPresent());

    assertThat(allow.getModifiers(), allOf(notNullValue(), hasSize(2)));
    assertThat(allow.getModifiers().get(0).getEventParameter(),
        allOf(notNullValue(), equalTo("latitude")));
    assertEquals(2, allow.getModifiers().size());
    assertEquals("delete", allow.getModifiers().get(0).getMethod());

    final PolicyDecision allowFallback = mechanim1.getElseBlock();
    assertNotNull(allowFallback);
    assertEquals(false, allowFallback.isAllowed().isPresent());

    assertThat(allowFallback.getModifiers(), allOf(notNullValue(), hasSize(2)));
    assertThat(allowFallback.getModifiers().get(0).getEventParameter(),
        allOf(notNullValue(), equalTo("latitude")));
    assertEquals("blur", allowFallback.getModifiers().get(0).getMethod());

    // Optional Execute Actions
    assertEquals(1, mechanim1.getExecuteActions().size());
    assertEquals("urn:action:test:sendmail", mechanim1.getExecuteActions().get(0).getName());
    assertEquals(1, mechanim1.getExecuteActions().get(0).getParameters().size());
    assertInstanceOf(PolicyParameter.class, mechanim1.getExecuteActions().get(0).getParameters().get(0));
    assertEquals("msg", mechanim1.getExecuteActions().get(0).getParameters().get(0).getName());
    assertEquals("hallo", (String) mechanim1.getExecuteActions().get(0).getParameters().get(0).evaluate(null)
        .getValue());
  }

  private void checkEventHasParameter(AndFunction andFunction) {
    final ExistsFunction existsFunction = (ExistsFunction) andFunction.getParameters().get(7);
    final PolicyConstant parameterOfExists = (PolicyConstant) existsFunction.getParameters().get(0);
    assertEquals(String.class, parameterOfExists.getType());
    assertEquals("i", parameterOfExists.getValue());
  }

  private void checkEventBoolean(AndFunction andFunction) {
    final BooleanEvent booleanEvent = (BooleanEvent) andFunction.getParameters().get(6);
    assertEquals("h", booleanEvent.getName());
    assertEquals(false, booleanEvent.getDefault());
  }

  private void checkEqualsFunctionWithConstant(AndFunction andFunction) {
    final EqualsFunction equalsFunction = (EqualsFunction) andFunction.getParameters().get(2);
    assertEquals(Number.class, equalsFunction.getParameters().get(0).getType());
    final PolicyEvent policyEvent = (PolicyEvent) equalsFunction.getParameters().get(0);
    assertEquals("b", policyEvent.getName());
    final PolicyConstant constant = (PolicyConstant) equalsFunction.getParameters().get(1);
    assertEquals(2.0, (Double) constant.getValue());
  }

  private void checkContainsFunction(AndFunction andFunction)
      throws EvaluationUndecidableException {
    final ContainsFunction containsFunction = (ContainsFunction) andFunction.getParameters().get(1);
    assertThat(containsFunction.getParameters(), allOf(notNullValue(), hasSize(2)));
    assertEquals("attribute/longitude", (String) containsFunction.getParameters().get(1).evaluate(null).getValue());
  }

  private void checkFirstParameter(AndFunction andFunction) {
    final EqualsFunction firstEquals = (EqualsFunction) andFunction.getParameters().get(0);
    assertEquals(Boolean.class, firstEquals.getParameters().get(0).getType());
    assertEquals("basdf", ((PolicyEvent) firstEquals.getParameters().get(0)).getName());
  }

  /**
   * Read resource file.
   *
   * @param  file               the file
   * @return                    the string
   * @throws IOException        Signals that an I/O exception has occurred.
   * @throws URISyntaxException the URI syntax exception
   */
  private String readResourceFile(String file) throws IOException, URISyntaxException {
    return new String(
        Files.readAllBytes(Paths.get(this.getClass().getClassLoader().getResource(file).toURI())));
  }

}
