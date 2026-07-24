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

import static de.fraunhofer.iese.mydata.policy.decision.AuthorizationDecision.DECISION_ALLOW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.interfaces.IManagementService;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.pdp.interfaces.IPolicyStore;
import de.fraunhofer.iese.mydata.pdp.language.model.Policy;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyDecision;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyMechanism;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.EventMatchOperator;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.ExecuteFunction;
import de.fraunhofer.iese.mydata.pdp.utils.PolicyStoreMapMatching;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.ConflictingPolicyException;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutorService;

/**
 * Test for Decision Maker.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
class DecisionMakerTest {

  /**
   * The event.
   */
  @Mock
  private Event event;

  /**
   * The pmp.
   */
  @Mock
  private IManagementService pmp;

  /**
   * The preventive mechanism.
   */
  @Mock
  private PolicyMechanism policyMechanism;

  /**
   * The preventive mechanism 2.
   */
  @Mock
  private PolicyMechanism policyMechanism2;

  /**
   * The inhibit action.
   */
  @Mock
  private PolicyDecision inhibitAction;

  /**
   * The allow action.
   */
  @Mock
  private PolicyDecision allowAction;

  /**
   * The event match operator demo.
   */
  @Mock
  private EventMatchOperator eventMatchOperatorDemo;

  /**
   * The event match operator demo demo.
   */
  @Mock
  private EventMatchOperator eventMatchOperatorDemoDemo;

  /**
   * The action.
   */
  @Mock
  private ExecuteFunction action;

  /**
   * The authorization action.
   */
  @Mock
  private PolicyDecision authorizationAction;

  /**
   * The thread pool.
   */
  @Mock
  private ExecutorService threadPool;

  /**
   * The test candidate.
   */
  private DecisionMaker testCandidate;

  /**
   * The policies.
   */
  private IPolicyStore policies;

  /**
   * Creates the test candidate.
   *
   * @throws Exception
   * @throws ConflictingPolicyException the conflicting policy exception
   */
  @BeforeEach
  void createTestCandidate() throws Exception {
    this.policies = new PolicyStoreMapMatching(10);
    this.testCandidate = new DecisionMaker(this.event, this.policies, this.threadPool);
    final PolicyDecisionPoint pdp = PolicyDecisionPoint.getInstance();
    pdp.reset();
    pdp.initialize(new ComponentId("urn:component:test:pdp:decisionservice"), URI.create("http://localhost:8080/ws/pmp"), 4, false, null);
    // TODO implement a whitelist variant?

  }

  /**
   * Creates the demo policy.
   *
   * @return the policy
   */
  private Policy createDemoPolicy() {
    final Policy pol = new Policy();
    pol.setId(new String("urn:policy:demo:demo"));
    return pol;
  }

  /**
   * Empty policy should result in allow.
   *
   * @throws Exception the exception
   */
  @Test
  void emptyPolicy_ShouldResultInAllow() throws Exception {
    when(this.event.getActionId()).thenReturn(new ActionId("urn:action:blubber:blubb"));
    this.testCandidate = new DecisionMaker(this.event, new PolicyStoreMapMatching(10), this.threadPool);
    assertEquals(DECISION_ALLOW, this.testCandidate.call());
  }

  /**
   * No policy matches should result in allow.
   *
   * @throws Exception the exception
   */
  @Test
  void noPolicyMatches_ShouldResultInAllow() throws Exception {
    when(this.event.getActionId()).thenReturn(new ActionId("urn:action:blubber:blubb"));
    when(this.policyMechanism.getEvent()).thenReturn(new ActionId("urn:action:demo:demo"));
    // when(this.eventMatchOperatorDemo.getAction()).thenReturn("urn:action:blubber:blubb");
    // when(this.policyMechanism.evaluate(this.event)).thenReturn(new
    // PolicyDecision(false));

    this.createEmptyMechanismAndStore(this.createDemoPolicy(), this.policyMechanism);

    assertTrue(this.testCandidate.call().isEventAllowed());
    verify(this.policyMechanism, times(0)).evaluate(this.event);
  }

  /**
   * Creates the empty mechanism and store.
   *
   * @param policy the policy
   * @param toSet  the to set
   * @throws ConflictingPolicyException the conflicting policy exception
   * @throws IOException
   * @throws InvalidEntityException
   * @throws NoSuchEntityException
   */
  private void createEmptyMechanismAndStore(Policy policy, PolicyMechanism... toSet) throws ConflictingPolicyException, IOException, NoSuchEntityException, InvalidEntityException {
    policy.setMechanisms(Arrays.asList(toSet));
    this.policies.clear();
    this.policies.storePolicy(policy, this.pmp.getZoneId(SolutionId.fromPolicyId(policy.getPolicyId())));
  }

  /**
   * One preventive policy matches but returns not allowed should result in
   * inhibit.
   *
   * @throws Exception the exception
   */
  @Test
  void onePreventivePolicyMatchesButReturnsNotAllowed_ShouldResultInInhibit() throws Exception {
    when(this.event.getActionId()).thenReturn(new ActionId("urn:action:demo:demo"));
    // when(this.eventMatchOperatorDemo.getAction()).thenReturn("urn:action:demo:demo");
    when(this.policyMechanism.evaluate(this.event)).thenReturn(getDecisionWIthOneAction(false));

    this.createPolicyWithEmptyInhibitAction();

    assertFalse(this.testCandidate.call().isEventAllowed());
    verify(this.policyMechanism).evaluate(this.event);
    verify(this.threadPool).submit(any(PxpExecutor.class));
  }

  private PolicyDecision getDecisionWIthOneAction(boolean allowed) {
    final PolicyDecision policyDecision = new PolicyDecision(allowed);
    policyDecision.setExecuteActions(Arrays.asList(this.action));
    return policyDecision;
  }

  /**
   * Two matching mechanism one returns not allowed should result in inhibit.
   *
   * @throws Exception the exception
   */
  @Test
  void twoMatchingMechanismOneReturnsNotAllowed_ShouldResultInInhibit() throws Exception {
    when(this.event.getActionId()).thenReturn(new ActionId("urn:action:demo:demo"));
    when(this.policyMechanism.getEvent()).thenReturn(new ActionId("urn:action:demo:demo"));
    when(this.policyMechanism2.getEvent()).thenReturn(new ActionId("urn:action:demo:demo"));
    // when(this.eventMatchOperatorDemo.getAction()).thenReturn("urn:action:demo:demo");
    when(this.policyMechanism.evaluate(this.event)).thenReturn(getDecisionWIthOneAction(true));
    when(this.policyMechanism2.evaluate(this.event)).thenReturn(getDecisionWIthOneAction(false));

    this.createTwoPoliciesWithInhibitAndAllowAction();

    assertFalse(this.testCandidate.call().isEventAllowed());
    verify(this.policyMechanism).evaluate(this.event);
    verify(this.policyMechanism2).evaluate(this.event);
    verify(this.threadPool, times(2)).submit(any(PxpExecutor.class));
  }

  /**
   * One out of two matching mechanism should return the result of matching one.
   *
   * @throws Exception the exception
   */
  @Test
  void oneOutOfTwoMatchingMechanism_ShouldReturnTheResultOfMatchingOne() throws Exception {
    when(this.event.getActionId()).thenReturn(new ActionId("urn:action:demo:demo"));
    when(this.policyMechanism.getEvent()).thenReturn(new ActionId("urn:action:demo:demo"));
    when(this.policyMechanism2.getEvent()).thenReturn(new ActionId("urn:action:demo:demo"));
    // when(this.eventMatchOperatorDemo.getAction()).thenReturn("urn:action:demo:demo");
    when(this.policyMechanism.evaluate(this.event)).thenReturn(getDecisionWIthOneAction(true));
    when(this.policyMechanism2.evaluate(this.event)).thenReturn(getDecisionWIthOneAction(false));

    this.createTwoPoliciesWithInhibitAndAllowAction();

    assertFalse(this.testCandidate.call().isEventAllowed());
    verify(this.policyMechanism).evaluate(this.event);
    verify(this.policyMechanism2).evaluate(this.event);
    verify(this.policyMechanism2, times(2)).getEvent();
    verify(this.threadPool, times(2)).submit(any(PxpExecutor.class));
    verify(policyMechanism).accept(any(ClearVariableDeclarationCacheVisitor.class));
    verify(policyMechanism2).accept(any(ClearVariableDeclarationCacheVisitor.class));
    verify(policyMechanism2).getPolicy();
    verify(policyMechanism2).setPolicy(any());
    verify(policyMechanism2).setZoneId(any());
    verify(policyMechanism2).getExecuteActions();
    verifyNoMoreInteractions(this.policyMechanism2);
  }

  // IND2UCE-3367
  @Test
  void givenThereIsMatchingMechanismWithUnconditionalExecuteAction_whenCall_thenExecuteActionWillBeTriggered() throws Exception {
    when(this.event.getActionId()).thenReturn(new ActionId("urn:action:demo:demo"));
    when(this.policyMechanism.getEvent()).thenReturn(new ActionId("urn:action:demo:demo"));
    when(this.policyMechanism.getExecuteActions()).thenReturn(Collections.singletonList(this.action));
    final PolicyDecision policyDecision = new PolicyDecision();
    when(this.policyMechanism.evaluate(this.event)).thenReturn(policyDecision);
    this.createEmptyMechanismAndStore(this.createDemoPolicy(), this.policyMechanism);

    this.testCandidate.call();

    verify(this.threadPool, times(1)).submit(any(PxpExecutor.class));
  }

  /**
   * Creates the policy with empty inhibit action.
   *
   * @throws ConflictingPolicyException the conflicting policy exception
   * @throws IOException
   * @throws InvalidEntityException
   * @throws NoSuchEntityException
   */
  private void createPolicyWithEmptyInhibitAction() throws ConflictingPolicyException, IOException, NoSuchEntityException, InvalidEntityException {
    when(this.policyMechanism.getEvent()).thenReturn(new ActionId("urn:action:demo:demo"));
    this.createEmptyMechanismAndStore(this.createDemoPolicy(), this.policyMechanism);
  }

  /**
   * Creates the two policies with inhibit and allow action.
   *
   * @throws ConflictingPolicyException the conflicting policy exception
   * @throws IOException
   * @throws InvalidEntityException
   * @throws NoSuchEntityException
   */
  private void createTwoPoliciesWithInhibitAndAllowAction() throws ConflictingPolicyException, IOException, NoSuchEntityException, InvalidEntityException {
    this.createEmptyMechanismAndStore(this.createDemoPolicy(), this.policyMechanism2, this.policyMechanism);
  }

}
