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

package de.fraunhofer.iese.mydata.pdp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.connector.ConnectorFactory;
import de.fraunhofer.iese.mydata.component.connector.OAuthCredentials;
import de.fraunhofer.iese.mydata.component.information.PipComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PxpComponentInformation;
import de.fraunhofer.iese.mydata.component.information.method.MethodInterfaceDescription;
import de.fraunhofer.iese.mydata.component.interfaces.IBasicManagementService;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyExecutionPoint;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyInformationPoint;
import de.fraunhofer.iese.mydata.exception.ConflictingResourceException;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.exception.ResourceUpdateException;
import de.fraunhofer.iese.mydata.pdp.utils.ConnectorCache;
import de.fraunhofer.iese.mydata.policy.PipRequest;
import de.fraunhofer.iese.mydata.policy.Policy;
import de.fraunhofer.iese.mydata.policy.decision.AuthorizationDecision;
import de.fraunhofer.iese.mydata.policy.decision.ExecuteAction;
import de.fraunhofer.iese.mydata.policy.decision.Modifier;
import de.fraunhofer.iese.mydata.policy.decision.ModifierEngine;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.event.InfoId;
import de.fraunhofer.iese.mydata.policy.exception.ConflictingPolicyException;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.exception.InformationUndeterminableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;
import org.w3c.dom.DOMException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * The Class BlackboxTest.
 */
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("javadoc")
class BlackboxTest {

  /**
   * The pdp instance.
   */
  private PolicyDecisionPoint pdpInstance;

  /**
   * The pip.
   */
  private IPolicyInformationPoint pip;

  /**
   * The pxp.
   */
  private IPolicyExecutionPoint pxp;

  @AfterAll
  static void cleanUp() throws Exception {
    PolicyDecisionPoint.getInstance().reset();
    final Field pdpInstanceField = PolicyDecisionPoint.class.getDeclaredField("pdpInstance");
    pdpInstanceField.setAccessible(true);
    pdpInstanceField.set(null, null);
  }

  /**
   * Checks the variable cache
   *
   * @throws IllegalArgumentException       the illegal argument exception
   * @throws IOException                    Signals that an I/O exception has occurred.
   * @throws ConflictingPolicyException     the conflicting policy exception
   * @throws URISyntaxException             the URI syntax exception
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   * @throws DOMException
   * @throws InvalidEntityException
   */
  @Test
  void threadVariableDeclarationCacheTest()
      throws Exception {
    final boolean res = this.pdpInstance.deploy(
        new Policy(this.readResourceFile("policyVariable.xml")), ZoneId.of("Europe/Berlin"));

    assertThat(res, is(true));

    Event e = new Event(new ActionId("urn:action:a:cr-writeData"));
    e.addParameter(new Parameter<>("superString", "laueft."));

    assertFalse(this.pdpInstance.decisionRequest(e).isEventAllowed());
    assertFalse(this.pdpInstance.decisionRequest(e).isEventAllowed());
    assertFalse(this.pdpInstance.decisionRequest(e).isEventAllowed());
    assertFalse(this.pdpInstance.decisionRequest(e).isEventAllowed());

    e = new Event(new ActionId("urn:action:a:cr-writeData"));
    e.addParameter(new Parameter<>("superString", "geht."));
    assertTrue(this.pdpInstance.decisionRequest(e).isEventAllowed());

  }

  /**
   * Decide.
   *
   * @throws IllegalArgumentException       the illegal argument exception
   * @throws IOException                    Signals that an I/O exception has occurred.
   * @throws ConflictingPolicyException     the conflicting policy exception
   * @throws URISyntaxException             the URI syntax exception
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   * @throws DOMException
   * @throws InvalidEntityException
   */
  @Test
  void decide() throws Exception {
    final boolean res = this.pdpInstance
        .deploy(new Policy(this.readResourceFile("policyShort.xml")), ZoneId.of("Europe/Berlin"));

    assertThat(res, is(true));

    final Event e = new Event(new ActionId("urn:action:test:cr-writeData"));
    e.addParameter(new Parameter<>("eventType", true));
    e.addParameter(new Parameter<>("superString", "laueft."));
    assertTrue(this.pdpInstance.decisionRequest(e).isEventAllowed());

    e.addParameter(new Parameter<>("attribute/longitude", "5/2"));
    final AuthorizationDecision decision = this.pdpInstance.decisionRequest(e);
    assertThat(decision.isEventAllowed(), is(true));
    assertEquals(1, decision.getModifiers().size());
    assertEquals("latitude", decision.getModifiers().get(0).getName());
    assertThat(decision.getModifiers().get(0).getEngine(), allOf(notNullValue(), hasSize(1)));

    final ModifierEngine engine = decision.getModifiers().get(0).getEngine().get(0);
    assertEquals("delete", engine.getMethod());
    assertThat(engine.getParameters(), allOf(notNullValue(), hasSize(1)));
    assertEquals("laueft.", (String) engine.getParameters().get(0).getValue());

  }

  /**
   * Decide.
   *
   * @throws IllegalArgumentException       the illegal argument exception
   * @throws IOException                    Signals that an I/O exception has occurred.
   * @throws ConflictingPolicyException     the conflicting policy exception
   * @throws URISyntaxException             the URI syntax exception
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   * @throws DOMException
   * @throws InvalidEntityException
   */
  @Test
  void decideInFailureMode()
      throws Exception {
    final boolean res = this.pdpInstance
        .deploy(new Policy(this.readResourceFile("policyShort.xml")), ZoneId.of("Europe/Berlin"));

    assertThat(res, is(true));

    final Event e = new Event(new ActionId("urn:action:ppe:cr-writeData"));
    e.addParameter(new Parameter<>("eventType", true));
    e.addParameter(new Parameter<>("superString", "laueft."));
    assertTrue(this.pdpInstance.decisionRequest(e).isEventAllowed());

    this.pdpInstance.setFailureMode(true);
    assertFalse(this.pdpInstance.decisionRequest(e).isEventAllowed());
  }

  /**
   * Detective event execute action.
   *
   * @throws IllegalArgumentException           the illegal argument exception
   * @throws IOException                        Signals that an I/O exception has occurred.
   * @throws ConflictingPolicyException         the conflicting policy exception
   * @throws URISyntaxException                 the URI syntax exception
   * @throws EvaluationUndecidableException     the evaluation undecidable exception
   * @throws InformationUndeterminableException the information undeterminable exception
   * @throws InterruptedException               the interrupted exception
   * @throws DOMException
   * @throws InvalidEntityException
   */
  @Test
  void detectiveEventExecuteAction()
      throws Exception {
    final boolean res = this.pdpInstance.deploy(
        new Policy(this.readResourceFile("policyDetectiveEventExecuteAction.xml")),
        ZoneId.of("Europe/Berlin"));
    assertThat(res, is(true));
    Mockito.when(this.pxp.execute(any(ExecuteAction.class))).thenReturn(true);
    final Event e = new Event(new ActionId("urn:action:test:showEmployee"));
    this.pdpInstance.decisionRequest(e);
    Thread.sleep(1000);
    Mockito.verify(this.pxp, Mockito.times(2)).execute(any(ExecuteAction.class));
    final ExecuteAction exec2 = new ExecuteAction("urn:action:test:alertOnSite");
    exec2.addParameter(new Parameter<>("duration", 60.0));
    Mockito.verify(this.pxp).execute(ArgumentMatchers.eq(exec2));
    final ExecuteAction exec = new ExecuteAction("urn:action:test:fireEmployee");
    exec.addParameter(new Parameter<>("messageToTell", "you are fired!"));
    exec.addParameter(new Parameter<>("daysUntilFiring", 5.0));
    Mockito.verify(this.pxp).execute(ArgumentMatchers.eq(exec));

  }

  /**
   * Prev allow.
   *
   * @throws IllegalArgumentException       the illegal argument exception
   * @throws IOException                    Signals that an I/O exception has occurred.
   * @throws ConflictingPolicyException     the conflicting policy exception
   * @throws URISyntaxException             the URI syntax exception
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   * @throws DOMException
   * @throws InvalidEntityException
   */
  @Test
  void prevAllow() throws Exception {
    final boolean res = this.pdpInstance.deploy(
        new Policy(this.readResourceFile("policyPrevAllow.xml")), ZoneId.of("Europe/Berlin"));
    assertThat(res, is(true));
    final Event e = new Event(new ActionId("urn:action:test:showEmployee"));
    assertTrue(this.pdpInstance.decisionRequest(e).isEventAllowed());
  }

  /**
   * Prev allow modify.
   *
   * @throws IllegalArgumentException       the illegal argument exception
   * @throws IOException                    Signals that an I/O exception has occurred.
   * @throws ConflictingPolicyException     the conflicting policy exception
   * @throws URISyntaxException             the URI syntax exception
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   * @throws DOMException
   * @throws InvalidEntityException
   */
  @Test
  void prevAllowModify()
      throws Exception {
    final boolean res = this.pdpInstance.deploy(
        new Policy(this.readResourceFile("policyPrevAllowModify.xml")), ZoneId.of("Europe/Berlin"));
    assertThat(res, is(true));
    final Event e = new Event(new ActionId("urn:action:test:showEmployee"));
    e.addParameter(new Parameter<>("role", "employee"));
    final AuthorizationDecision decision = this.pdpInstance.decisionRequest(e);
    assertThat(decision.isEventAllowed(), is(true));
    assertEquals(2, decision.getModifiers().size());
    assertEquals("role", decision.getModifiers().get(0).getName());
    assertThat(decision.getModifiers().get(0).getEngine(), allOf(notNullValue(), hasSize(1)));
    assertEquals("changeTo", decision.getModifiers().get(0).getEngine().get(0).getMethod());
    assertEquals("role", decision.getModifiers().get(0).getEngine().get(0).getParameters().get(0).getName());
    assertEquals("ConstructionSiteManager", decision.getModifiers().get(0).getEngine().get(0).getParameters().get(0).getValue()
        .toString());
    assertEquals("id", decision.getModifiers().get(1).getName());
    assertThat(decision.getModifiers().get(1).getEngine(), allOf(notNullValue(), hasSize(1)));
    assertEquals("anonymize", decision.getModifiers().get(1).getEngine().get(0).getMethod());
  }

  /**
   * Prev condition false.
   *
   * @throws IllegalArgumentException       the illegal argument exception
   * @throws IOException                    Signals that an I/O exception has occurred.
   * @throws ConflictingPolicyException     the conflicting policy exception
   * @throws URISyntaxException             the URI syntax exception
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   * @throws DOMException
   * @throws InvalidEntityException
   */
  @Test
  void prevConditionFalse()
      throws Exception {
    final boolean res = this.pdpInstance.deploy(
        new Policy(this.readResourceFile("policyConditionFalse.xml")), ZoneId.of("Europe/Berlin"));
    assertThat(res, is(true));
    final Event e = new Event(new ActionId("urn:action:test:showEmployee"));
    assertTrue(this.pdpInstance.decisionRequest(e).isEventAllowed());
  }

  /**
   * Prev condition operator test and.
   *
   * @throws IllegalArgumentException       the illegal argument exception
   * @throws IOException                    Signals that an I/O exception has occurred.
   * @throws ConflictingPolicyException     the conflicting policy exception
   * @throws URISyntaxException             the URI syntax exception
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   * @throws DOMException
   * @throws InvalidEntityException
   */
  @Test
  void prevConditionOperatorTestAnd()
      throws Exception {
    final boolean res = this.pdpInstance.deploy(
        new Policy(this.readResourceFile("policyConditionOperatorTestAnd.xml")),
        ZoneId.of("Europe/Berlin"));
    assertThat(res, is(true));
    Event e = new Event(new ActionId("urn:action:test:showEmployee"));
    assertTrue(this.pdpInstance.decisionRequest(e).isEventAllowed());
    e = new Event(new ActionId("urn:action:test:createConstructionSite"));
    assertFalse(this.pdpInstance.decisionRequest(e).isEventAllowed());
  }

  /**
   * Prev condition operator test implies.
   *
   * @throws IllegalArgumentException       the illegal argument exception
   * @throws IOException                    Signals that an I/O exception has occurred.
   * @throws ConflictingPolicyException     the conflicting policy exception
   * @throws URISyntaxException             the URI syntax exception
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   * @throws DOMException
   * @throws InvalidEntityException
   */
  @Test
  void prevConditionOperatorTestImplies()
      throws Exception {
    final boolean res = this.pdpInstance.deploy(
        new Policy(this.readResourceFile("policyConditionOperatorTestImplies.xml")),
        ZoneId.of("Europe/Berlin"));
    assertThat(res, is(true));
    Event e = new Event(new ActionId("urn:action:test:showEmployee"));
    assertTrue(this.pdpInstance.decisionRequest(e).isEventAllowed());
    e = new Event(new ActionId("urn:action:test:createConstructionSite"));
    assertFalse(this.pdpInstance.decisionRequest(e).isEventAllowed());
  }

  /**
   * Prev condition operator test not.
   *
   * @throws IllegalArgumentException       the illegal argument exception
   * @throws IOException                    Signals that an I/O exception has occurred.
   * @throws ConflictingPolicyException     the conflicting policy exception
   * @throws URISyntaxException             the URI syntax exception
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   * @throws DOMException
   * @throws InvalidEntityException
   */
  @Test
  void prevConditionOperatorTestNot()
      throws Exception {
    final boolean res = this.pdpInstance.deploy(
        new Policy(this.readResourceFile("policyConditionOperatorTestNot.xml")),
        ZoneId.of("Europe/Berlin"));
    assertThat(res, is(true));
    final Event e = new Event(new ActionId("urn:action:test:showEmployee"));
    assertFalse(this.pdpInstance.decisionRequest(e).isEventAllowed());
  }

  /**
   * Prev condition operator test or.
   *
   * @throws IllegalArgumentException       the illegal argument exception
   * @throws IOException                    Signals that an I/O exception has occurred.
   * @throws ConflictingPolicyException     the conflicting policy exception
   * @throws URISyntaxException             the URI syntax exception
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   * @throws DOMException
   * @throws InvalidEntityException
   */
  @Test
  void prevConditionOperatorTestOr()
      throws Exception {
    final boolean res = this.pdpInstance.deploy(
        new Policy(this.readResourceFile("policyConditionOperatorTestOr.xml")),
        ZoneId.of("Europe/Berlin"));
    assertThat(res, is(true));
    Event e = new Event(new ActionId("urn:action:test:showEmployee"));
    assertTrue(this.pdpInstance.decisionRequest(e).isEventAllowed());
    e = new Event(new ActionId("urn:action:test:createConstructionSite"));
    assertFalse(this.pdpInstance.decisionRequest(e).isEventAllowed());
  }

  /**
   * Prev condition operator test pip.
   *
   * @throws IllegalArgumentException           the illegal argument exception
   * @throws IOException                        Signals that an I/O exception has occurred.
   * @throws ConflictingPolicyException         the conflicting policy exception
   * @throws URISyntaxException                 the URI syntax exception
   * @throws EvaluationUndecidableException     the evaluation undecidable exception
   * @throws InformationUndeterminableException the information undeterminable exception
   * @throws DOMException
   * @throws InvalidEntityException
   */
  @Test
  void prevConditionOperatorTestPip() throws Exception {
    final boolean res = this.pdpInstance.deploy(
        new Policy(this.readResourceFile("policyConditionOperatorTestPip.xml")),
        ZoneId.of("Europe/Berlin"));
    assertThat(res, is(true));
    Event e = new Event(new ActionId("urn:action:test:showEmployee"));
    Mockito.doReturn(new DataObject<>(false)).when(this.pip).evaluate(any(PipRequest.class));
    assertTrue(this.pdpInstance.decisionRequest(e).isEventAllowed());
    final PipRequest pipRequest = new PipRequest(
        new InfoId("urn:info:test:isConstructionSiteManager"));
    pipRequest.addParameter(new Parameter<>("id", "123"));
    Mockito.verify(this.pip).evaluate(ArgumentMatchers.eq(pipRequest));
    e = new Event(new ActionId("urn:action:test:showEmployee"));
    Mockito.doReturn(new DataObject<>(true)).when(this.pip).evaluate(any(PipRequest.class));
    assertFalse(this.pdpInstance.decisionRequest(e).isEventAllowed());
    Mockito.verify(this.pip, Mockito.times(2)).evaluate(ArgumentMatchers.eq(pipRequest));
  }

  /**
   * Prev decision aggregation.
   *
   * @throws IllegalArgumentException           the illegal argument exception
   * @throws IOException                        Signals that an I/O exception has occurred.
   * @throws ConflictingPolicyException         the conflicting policy exception
   * @throws URISyntaxException                 the URI syntax exception
   * @throws EvaluationUndecidableException     the evaluation undecidable exception
   * @throws InformationUndeterminableException the information undeterminable exception
   * @throws DOMException
   * @throws InvalidEntityException
   */
  @Test
  void prevDecisionAggregation() throws Exception {
    final boolean res = this.pdpInstance.deploy(
        new Policy(this.readResourceFile("policyDecisionAggregation.xml")),
        ZoneId.of("Europe/Berlin"));
    assertThat(res, is(true));
    final Event e = new Event(new ActionId("urn:action:test:showEmployee"));
    assertFalse(this.pdpInstance.decisionRequest(e).isEventAllowed());
  }

  /**
   * Prev decision and modifier aggregation.
   *
   * @throws IllegalArgumentException           the illegal argument exception
   * @throws IOException                        Signals that an I/O exception has occurred.
   * @throws ConflictingPolicyException         the conflicting policy exception
   * @throws URISyntaxException                 the URI syntax exception
   * @throws EvaluationUndecidableException     the evaluation undecidable exception
   * @throws InformationUndeterminableException the information undeterminable exception
   * @throws DOMException
   * @throws InvalidEntityException
   */
  // @Ignore(value = "FIX ME")
  @Test
  void prevDecisionAndModifierAggregation() throws Exception {
    final boolean res = this.pdpInstance.deploy(
        new Policy(this.readResourceFile("policyDecisionAndModifierAggregation.xml")),
        ZoneId.of("Europe/Berlin"));
    assertThat(res, is(true));
    final Event e = new Event(new ActionId("urn:action:test:showEmployee"));
    Mockito.doReturn(new DataObject<>(false)).when(this.pip).evaluate(any(PipRequest.class));
    final AuthorizationDecision decision = this.pdpInstance.decisionRequest(e);
    assertThat(decision.isEventAllowed(), is(true));
    assertEquals(6, decision.getModifiers().size());
    int correctEngines = 0;
    for (final Modifier modifier : decision.getModifiers()) {
      for (final ModifierEngine engine : modifier.getEngine()) {
        if ("id".equals(modifier.getName())) {
          if ("engine1".equals(engine.getMethod())
              && "test1".equals(engine.getParameterValue("testParam", String.class))) {
            correctEngines++;
          }
          if ("engine1".equals(engine.getMethod())
              && "test2".equals(engine.getParameterValue("testParam", String.class))) {
            correctEngines++;
          }
          if ("engine3".equals(engine.getMethod())) {
            correctEngines++;
          }
        } else if ("role".equals(modifier.getName())) {
          if ("engine2".equals(engine.getMethod())) {
            correctEngines++;
          }
          if ("engine4".equals(engine.getMethod())) {
            correctEngines++;
          }
          if ("engine5".equals(engine.getMethod())
              && "blub".equals(engine.getParameterValue("testParamString", String.class))
              && Double.valueOf(8).equals(engine.getParameterValue("testParamInt", Double.class))
              && Double.valueOf("-98461687987")
              .equals(engine.getParameterValue("testParamLong", Double.class))
              && Boolean.valueOf(true)
              .equals(engine.getParameterValue("testParamBoolean", Boolean.class))
              && Double.valueOf("64.54")
              .equals(engine.getParameterValue("testParamFloat", Double.class))
              && Double.valueOf("-6546461.9874651648")
              .equals(engine.getParameterValue("testParamDouble", Double.class))) {
            correctEngines++;
          }
        }
      }
    }
    assertThat(correctEngines, is(6));
  }

  /**
   * Prev event filtering.
   *
   * @throws IllegalArgumentException       the illegal argument exception
   * @throws IOException                    Signals that an I/O exception has occurred.
   * @throws ConflictingPolicyException     the conflicting policy exception
   * @throws URISyntaxException             the URI syntax exception
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   * @throws DOMException
   * @throws InvalidEntityException
   */
  @Test
  void prevEventFiltering()
      throws Exception {
    final boolean res = this.pdpInstance.deploy(
        new Policy(this.readResourceFile("policyEventFiltering.xml")), ZoneId.of("Europe/Berlin"));
    assertThat(res, is(true));
    Event e = new Event(new ActionId("urn:action:test:showEmployee"));
    e.addParameter(new Parameter<>("id", Integer.valueOf("123")));
    e.addParameter("role", "ConstructionSiteManager");
    e.addParameter(new Parameter<>("timestamp", Long.valueOf("123456789012345")));
    e.addParameter(new Parameter<>("temperature", Float.valueOf("37.5")));
    e.addParameter(
        new Parameter<>("magicNumber", Double.valueOf("342342354654766778678.3454354353")));
    e.addParameter(new Parameter<>("isBigBoss", Boolean.valueOf(true)));
    assertFalse(this.pdpInstance.decisionRequest(e).isEventAllowed());
    e = new Event(new ActionId("urn:action:test:showEmployee"));
    e.addParameter(new Parameter<>("id", Integer.valueOf("123")));
    e.addParameter("role", "ConstructionSiteManager");
    e.addParameter(new Parameter<>("timestamp", Long.valueOf("123456789012345")));
    e.addParameter(new Parameter<>("temperature", Float.valueOf("37.5")));
    e.addParameter(
        new Parameter<>("magicNumber", Double.valueOf("342342354654766778678.3454354353")));
    e.addParameter(new Parameter<>("isBigBoss", Boolean.valueOf(false)));
    assertTrue(this.pdpInstance.decisionRequest(e).isEventAllowed());
  }

  /**
   * Prev execute in allow returns false.
   *
   * @throws IllegalArgumentException       the illegal argument exception
   * @throws IOException                    Signals that an I/O exception has occurred.
   * @throws ConflictingPolicyException     the conflicting policy exception
   * @throws URISyntaxException             the URI syntax exception
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   * @throws DOMException
   * @throws InvalidEntityException
   */
  // @Ignore(value = "FIX ME")
  @Test
  void prevExecuteInAllowReturnsFalse()
      throws Exception {
    final boolean res = this.pdpInstance.deploy(
        new Policy(this.readResourceFile("policyAllowExecuteInAllow.xml")),
        ZoneId.of("Europe/Berlin"));
    assertThat(res, is(true));
    final Event e = new Event(new ActionId("urn:action:test:showEmployee"));
    Mockito.when(this.pxp.execute(any(ExecuteAction.class))).thenReturn(false);
    assertFalse(this.pdpInstance.decisionRequest(e).isEventAllowed());
    final ExecuteAction exec = new ExecuteAction("urn:action:test:log");
    exec.addParameter(new Parameter<>("message", "test"));
    Mockito.verify(this.pxp).execute(ArgumentMatchers.eq(exec));
  }

  /**
   * Prev execute in allow returns true.
   *
   * @throws IllegalArgumentException       the illegal argument exception
   * @throws IOException                    Signals that an I/O exception has occurred.
   * @throws ConflictingPolicyException     the conflicting policy exception
   * @throws URISyntaxException             the URI syntax exception
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   * @throws DOMException
   * @throws InvalidEntityException
   */
  @Test
  void prevExecuteInAllowReturnsTrue()
      throws Exception {

    final boolean res = this.pdpInstance.deploy(
        new Policy(this.readResourceFile("policyAllowExecuteInAllow.xml")),
        ZoneId.of("Europe/Berlin"));
    assertThat(res, is(true));
    final Event e = new Event(new ActionId("urn:action:test:showEmployee"));
    Mockito.when(this.pxp.execute(any(ExecuteAction.class))).thenReturn(true);
    assertTrue(this.pdpInstance.decisionRequest(e).isEventAllowed());
    final ExecuteAction exec = new ExecuteAction("urn:action:test:log");
    exec.addParameter(new Parameter<>("message", "test"));
    Mockito.verify(this.pxp).execute(ArgumentMatchers.eq(exec));
  }

  /**
   * Prev execute in auth decision returns false.
   *
   * @throws IllegalArgumentException       the illegal argument exception
   * @throws IOException                    Signals that an I/O exception has occurred.
   * @throws ConflictingPolicyException     the conflicting policy exception
   * @throws URISyntaxException             the URI syntax exception
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   * @throws InterruptedException           the interrupted exception
   * @throws DOMException
   * @throws InvalidEntityException
   */
  @Test
  void prevExecuteInAuthDecisionReturnsFalse()
      throws Exception {
    final boolean res = this.pdpInstance.deploy(
        new Policy(this.readResourceFile("policyAllowExecuteInDecision.xml")),
        ZoneId.of("Europe/Berlin"));
    assertThat(res, is(true));
    final Event e = new Event(new ActionId("urn:action:test:showEmployee"));
    Mockito.when(this.pxp.execute(any(ExecuteAction.class))).thenReturn(false);
    assertTrue(this.pdpInstance.decisionRequest(e).isEventAllowed());
    Thread.sleep(1000);
    final ExecuteAction exec = new ExecuteAction("urn:action:test:log");
    exec.addParameter(new Parameter<>("message", "test"));
    Mockito.verify(this.pxp).execute(ArgumentMatchers.eq(exec));
  }

  /**
   * Prev fallback allow modify execute false.
   *
   * @throws IllegalArgumentException       the illegal argument exception
   * @throws IOException                    Signals that an I/O exception has occurred.
   * @throws ConflictingPolicyException     the conflicting policy exception
   * @throws URISyntaxException             the URI syntax exception
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   * @throws DOMException
   * @throws InvalidEntityException
   */
  @Test
  void prevFallbackAllowModifyExecuteFalse()
      throws Exception {
    final boolean res = this.pdpInstance.deploy(
        new Policy(this.readResourceFile("policyFallbackAllowModify.xml")),
        ZoneId.of("Europe/Berlin"));
    assertThat(res, is(true));
    final Event e = new Event(new ActionId("urn:action:test:showEmployee"));
    Mockito.when(this.pxp.execute(any(ExecuteAction.class))).thenReturn(false);
    final AuthorizationDecision decision = this.pdpInstance.decisionRequest(e);
    assertTrue(decision.isEventAllowed());
    assertEquals(1, decision.getModifiers().size());
    assertEquals("$.id", decision.getModifiers().get(0).getExpression());
    final ExecuteAction exec = new ExecuteAction("urn:action:test:log");
    exec.addParameter(new Parameter<>("message", "test"));
    Mockito.verify(this.pxp).execute(ArgumentMatchers.eq(exec));
  }

  /**
   * Prev fallback allow modify execute true.
   *
   * @throws IllegalArgumentException       the illegal argument exception
   * @throws IOException                    Signals that an I/O exception has occurred.
   * @throws ConflictingPolicyException     the conflicting policy exception
   * @throws URISyntaxException             the URI syntax exception
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   * @throws DOMException
   * @throws InvalidEntityException
   */
  @Test
  void prevFallbackAllowModifyExecuteTrue()
      throws Exception {
    final boolean res = this.pdpInstance.deploy(
        new Policy(this.readResourceFile("policyFallbackAllowModify.xml")),
        ZoneId.of("Europe/Berlin"));
    assertThat(res, is(true));
    final Event e = new Event(new ActionId("urn:action:test:showEmployee"));
    Mockito.when(this.pxp.execute(any(ExecuteAction.class))).thenReturn(true);
    final AuthorizationDecision authorizationDecision = this.pdpInstance.decisionRequest(e);
    assertTrue(authorizationDecision.isEventAllowed());
    assertEquals(0, authorizationDecision.getModifiers().size());
    final ExecuteAction exec = new ExecuteAction("urn:action:test:log");
    exec.addParameter(new Parameter<>("message", "test"));
    Mockito.verify(this.pxp).execute(ArgumentMatchers.eq(exec));
  }

  /**
   * Prev fallback inhibit execute false.
   *
   * @throws IllegalArgumentException       the illegal argument exception
   * @throws IOException                    Signals that an I/O exception has occurred.
   * @throws ConflictingPolicyException     the conflicting policy exception
   * @throws URISyntaxException             the URI syntax exception
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   * @throws DOMException
   * @throws InvalidEntityException
   */
  // @Ignore(value = "FIX ME")
  @Test
  void prevFallbackInhibitExecuteFalse()
      throws Exception {
    final boolean res = this.pdpInstance.deploy(
        new Policy(this.readResourceFile("policyFallbackInhibit.xml")), ZoneId.of("Europe/Berlin"));
    assertThat(res, is(true));
    final Event e = new Event(new ActionId("urn:action:test:showEmployee"));
    Mockito.when(this.pxp.execute(any(ExecuteAction.class))).thenReturn(false);
    assertFalse(this.pdpInstance.decisionRequest(e).isEventAllowed());
    final ExecuteAction exec = new ExecuteAction("urn:action:test:log");
    exec.addParameter(new Parameter<>("message", "test"));
    Mockito.verify(this.pxp).execute(ArgumentMatchers.eq(exec));
  }

  /**
   * Prev fallback inhibit execute true.
   *
   * @throws IllegalArgumentException       the illegal argument exception
   * @throws IOException                    Signals that an I/O exception has occurred.
   * @throws ConflictingPolicyException     the conflicting policy exception
   * @throws URISyntaxException             the URI syntax exception
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   * @throws DOMException
   * @throws InvalidEntityException
   */
  @Test
  void prevFallbackInhibitExecuteTrue()
      throws Exception {
    final boolean res = this.pdpInstance.deploy(
        new Policy(this.readResourceFile("policyFallbackInhibit.xml")), ZoneId.of("Europe/Berlin"));
    assertThat(res, is(true));
    final Event e = new Event(new ActionId("urn:action:test:showEmployee"));
    Mockito.when(this.pxp.execute(any(ExecuteAction.class))).thenReturn(true);
    assertTrue(this.pdpInstance.decisionRequest(e).isEventAllowed());
    final ExecuteAction exec = new ExecuteAction("urn:action:test:log");
    exec.addParameter(new Parameter<>("message", "test"));
    Mockito.verify(this.pxp).execute(ArgumentMatchers.eq(exec));
  }

  /**
   * Prev inhibit.
   *
   * @throws IllegalArgumentException       the illegal argument exception
   * @throws IOException                    Signals that an I/O exception has occurred.
   * @throws ConflictingPolicyException     the conflicting policy exception
   * @throws URISyntaxException             the URI syntax exception
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   * @throws DOMException
   * @throws InvalidEntityException
   */
  @Test
  void prevInhibit()
      throws Exception {
    final boolean res = this.pdpInstance.deploy(
        new Policy(this.readResourceFile("policyPrevInhibit.xml")), ZoneId.of("Europe/Berlin"));
    assertThat(res, is(true));
    final Event e = new Event(new ActionId("urn:action:test:showEmployee"));
    assertFalse(this.pdpInstance.decisionRequest(e).isEventAllowed());
  }

  /**
   * Prev inhibit delay.
   *
   * @throws IllegalArgumentException       the illegal argument exception
   * @throws IOException                    Signals that an I/O exception has occurred.
   * @throws ConflictingPolicyException     the conflicting policy exception
   * @throws URISyntaxException             the URI syntax exception
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   * @throws DOMException
   * @throws InvalidEntityException
   */
  @Disabled(value = "not yet implemented")
  @Test
  void prevInhibitDelay()
      throws Exception {
    final boolean res = this.pdpInstance.deploy(
        new Policy(this.readResourceFile("policyPrevInhibitDelay.xml")),
        ZoneId.of("Europe/Berlin"));
    assertThat(res, is(true));
    final Event e = new Event(new ActionId("urn:action:test:showEmployee"));
    final AuthorizationDecision decision = this.pdpInstance.decisionRequest(e);
    assertThat(decision.isEventAllowed(), is(false));
  }

  @Test
  void countPipCalls() throws Exception {
    final boolean res = this.pdpInstance.deploy(
        new Policy(this.readResourceFile("policyPipInPxp.xml")), ZoneId.of("Europe/Berlin"));
    assertTrue(res);

    final Event e = new Event(new ActionId("urn:action:test:showEmployee"));
    final CountDownLatch cdl = new CountDownLatch(1);

    Mockito.when(this.pxp.execute(any(ExecuteAction.class))).thenAnswer(new Answer<Boolean>() {

      @Override
      public Boolean answer(InvocationOnMock invocation) throws Throwable {
        cdl.countDown();
        return true;
      }
    });

    Mockito.when(this.pip.evaluate(any(PipRequest.class))).thenReturn(new DataObject(42));

    this.pdpInstance.decisionRequest(e);
    assertTrue(cdl.await(10, TimeUnit.SECONDS));
    // check if the pip is called only once
    Mockito.verify(this.pip, Mockito.times(1)).evaluate(any(PipRequest.class));
  }

  // IND2UCE-3367
  @Test
  void unconditionalPxpExecution() throws Exception {
    final boolean res = this.pdpInstance.deploy(
        new Policy(this.readResourceFile("IND2UCE-3367-unconditional-pxp-execution.xml")),
        ZoneId.of("Europe/Berlin"));
    assertTrue(res);
    final Event e = new Event(new ActionId("urn:action:test:test"));
    final CountDownLatch cdl = new CountDownLatch(2);
    Mockito.when(this.pxp.execute(any(ExecuteAction.class)))
        .thenAnswer((Answer<Boolean>) invocation -> {
          cdl.countDown();
          return true;
        });

    // We will execute it twice to make sure that the underlying policy model is not modified
    // (could result in more invocations as expected)
    {
      final AuthorizationDecision authorizationDecision = this.pdpInstance.decisionRequest(e);
      assertTrue(authorizationDecision.isEventAllowed());
    }
    {
      final AuthorizationDecision authorizationDecision = this.pdpInstance.decisionRequest(e);
      assertTrue(authorizationDecision.isEventAllowed());
    }
    assertTrue(cdl.await(10, TimeUnit.SECONDS));
    Mockito.verify(this.pxp, Mockito.times(2)).execute(any(ExecuteAction.class));
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

  /**
   * Reset.
   *
   * @throws IOException                  Signals that an I/O exception has occurred.
   * @throws ResourceUpdateException
   * @throws ConflictingResourceException
   * @throws InvalidEntityException
   * @throws IllegalAccessException
   * @throws SecurityException
   * @throws NoSuchFieldException
   */
  @BeforeEach
  void reset() throws IOException, ConflictingResourceException, ResourceUpdateException,
      IllegalAccessException, NoSuchFieldException, SecurityException, InvalidEntityException,
      NoSuchEntityException {
    try {

      this.pdpInstance = PolicyDecisionPoint.getInstance();
      this.pdpInstance.reset();
      this.pdpInstance.initialize(new ComponentId("urn:component:test:pdp:decisionservice"),
          URI.create("http://localhost:8080/ws/pmp"), 4, false, null);
      final ConnectorFactory factoryMock = Mockito.mock(ConnectorFactory.class);
      final Field factory = ConnectorCache.class.getDeclaredField("connectorFactory");
      factory.setAccessible(true);
      factory.set(PolicyDecisionPoint.getInstance().getConnectorCache(), factoryMock);

      final IBasicManagementService pmp = Mockito.mock(IBasicManagementService.class);
      Mockito.when(factoryMock.getPmpClient((URI) any())).thenReturn(pmp);
      Mockito.when(factoryMock.getPmpClient((URI) any(), (OAuthCredentials) any())).thenReturn(pmp);

      Mockito.when(pmp.addPxp(any(PxpComponentInformation.class))).thenReturn(new ComponentId());

      PolicyDecisionPoint.getInstance().getConnectorCache().setOAuthCredentials("PDP", "secret123",
          "http://localhost:8081/oauth/token");

      final List<URI> dummyUris = new ArrayList<URI>();
      dummyUris.add(URI.create("http://dummy"));

      final PxpComponentInformation dummyPxp = new PxpComponentInformation(
          new ComponentId("urn:component:test:pxp:test"), dummyUris);
      final Set<PxpComponentInformation> pxpComps = new HashSet<>();
      pxpComps.add(dummyPxp);
      Mockito.when(pmp.lookupPxp(any(SolutionId.class), any(MethodInterfaceDescription.class)))
          .thenReturn(pxpComps);
      this.pxp = Mockito.mock(IPolicyExecutionPoint.class);
      Mockito.when(factoryMock.getPxp(ArgumentMatchers.isA(URI.class))).thenReturn(this.pxp);

      final PipComponentInformation dummyPip = new PipComponentInformation(
          new ComponentId("urn:component:test:pip:test"), dummyUris);
      final Set<PipComponentInformation> pipComps = new HashSet<>();
      pipComps.add(dummyPip);
      Mockito.when(pmp.lookupPip(any(SolutionId.class), any(MethodInterfaceDescription.class)))
          .thenReturn(pipComps);
      this.pip = Mockito.mock(IPolicyInformationPoint.class);
      Mockito.when(factoryMock.getPip(ArgumentMatchers.isA(URI.class))).thenReturn(this.pip);

    } catch (final IOException e) {
      e.printStackTrace();
    } catch (final IllegalArgumentException e) {
      e.printStackTrace();
    }
  }
}
