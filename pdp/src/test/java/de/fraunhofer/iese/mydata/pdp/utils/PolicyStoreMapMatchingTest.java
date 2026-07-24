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

package de.fraunhofer.iese.mydata.pdp.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.fraunhofer.iese.mydata.pdp.interfaces.IPolicyStore;
import de.fraunhofer.iese.mydata.pdp.language.model.Policy;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyMechanism;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.ConflictingPolicyException;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * The Class PolicyStoreMapMatchingTest.
 */
@ExtendWith(MockitoExtension.class)
//@MockitoSettings(strictness = Strictness.WARN)
class PolicyStoreMapMatchingTest {

  /**
   * The policy store.
   */
  private IPolicyStore policyStore;

  /**
   * The preventive mechanism.
   */
  @Mock
  private PolicyMechanism preventiveMechanism;

  /**
   * The preventive mechanism 1.
   */
  @Mock
  private PolicyMechanism preventiveMechanism1;

  /**
   * The event.
   */
  @Mock
  private Event event;

  /**
   * Sets the up.
   *
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   */
  @BeforeEach
  void setUp() throws EvaluationUndecidableException {
    this.policyStore = new PolicyStoreMapMatching(10);

  }

  /**
   * Store is empty should result in true.
   */
  @Test
  void storeIsEmpty_ShouldResultInTrue() {
    assertTrue(this.policyStore.isEmpty());
  }

  /**
   * Creates the empty policy.
   *
   * @return the policy
   */
  private Policy createEmptyPolicy() {
    final String policyId = new String("urn:policy:demo:demo");
    final Policy policy = new Policy();
    policy.setId(policyId);

    return policy;
  }

  /**
   * Creates the policy with preventive mechanism.
   *
   * @param  policy     the policy
   * @param  mechanisms the mechanisms
   * @return            the policy
   */
  private Policy createPolicyWithPreventiveMechanism(Policy policy, PolicyMechanism... mechanisms) {
    policy.setMechanisms(Arrays.asList(mechanisms));
    return policy;
  }

  /**
   * Store is not empty should result in false.
   *
   * @throws ConflictingPolicyException the conflicting policy exception
   */
  @Test
  void storeIsNotEmpty_ShouldResultInFalse() throws Exception {
    when(this.preventiveMechanism.getEvent()).thenReturn(new ActionId("urn:action:demo:demo"));
    assertTrue(this.policyStore.storePolicy(this.createPolicyWithPreventiveMechanism(
        this.createEmptyPolicy(), this.preventiveMechanism), ZoneId.of("Europe/Berlin")));
    assertFalse(this.policyStore.isEmpty());
    verify(this.preventiveMechanism, times(2)).getEvent();
  }

  /**
   * Store is empty clear store test should result in no exception.
   */
  @Test
  void storeIsEmptyClearStoreTest_ShouldResultInNoException() {
    Assertions.assertDoesNotThrow(() -> {
      this.policyStore.clear();
    }, "Exception thrown during policy store clearing: ");
  }

  /**
   * Clear store test should result in clear storage.
   */
  @Test
  void clearStoreTest_ShouldResultInClearStorage() {
    when(this.preventiveMechanism.getEvent()).thenReturn(new ActionId("urn:action:demo:demo"));
    Assertions.assertDoesNotThrow(() -> {
      this.policyStore.clear();
      assertTrue(this.policyStore.isEmpty());
      assertTrue(this.policyStore.storePolicy(this.createPolicyWithPreventiveMechanism(
          this.createEmptyPolicy(), this.preventiveMechanism), ZoneId.of("Europe/Berlin")));
      assertFalse(this.policyStore.isEmpty());
      verify(this.preventiveMechanism, times(2)).getEvent();
      assertFalse(this.policyStore.isEmpty());
      this.policyStore.clear();
      assertTrue(this.policyStore.isEmpty());
    }, "Exception thrown during policy store clearing: ");
  }

  /**
   * Store policy test null parameter should result in false.
   *
   * @throws ConflictingPolicyException the conflicting policy exception
   */
  @Test
  void storePolicyTestNullParameter_ShouldResultInFalse() throws Exception {
    final boolean result = this.policyStore.storePolicy(null, ZoneId.of("Europe/Berlin"));
    assertFalse(result);
  }

  /**
   * Store policy test null parameter attribute should result in false.
   *
   * @throws ConflictingPolicyException the conflicting policy exception
   */
  @Test
  void storePolicyTestNullParameterAttribute_ShouldResultInFalse() throws Exception {
    final Policy pol = new Policy();
    pol.setId(null);
    assertFalse(this.policyStore.storePolicy(pol, ZoneId.of("Europe/Berlin")));
  }

  /**
   * Store policy doubled test should result in false.
   *
   * @throws ConflictingPolicyException the conflicting policy exception
   */
  @Test
  void storePolicyDoubledTest_ShouldResultInFalse() throws Exception {
    when(this.preventiveMechanism.getEvent()).thenReturn(new ActionId("urn:action:demo:demo"));
    assertTrue(this.policyStore.isEmpty());
    assertTrue(this.policyStore.storePolicy(this.createPolicyWithPreventiveMechanism(
        this.createEmptyPolicy(), this.preventiveMechanism), ZoneId.of("Europe/Berlin")));
    assertFalse(this.policyStore.isEmpty());
    assertFalse(this.policyStore.storePolicy(this.createPolicyWithPreventiveMechanism(
        this.createEmptyPolicy(), this.preventiveMechanism), ZoneId.of("Europe/Berlin")));
  }

  /**
   * Store policy double action test should result in true.
   *
   * @throws ConflictingPolicyException the conflicting policy exception
   */
  @Test
  void storePolicyDoubleActionTest_ShouldResultInTrue() throws Exception {
    final Policy pol = this.createPolicyWithPreventiveMechanism(this.createEmptyPolicy(),
        this.preventiveMechanism, this.preventiveMechanism1);
    when(this.preventiveMechanism.getEvent()).thenReturn(new ActionId("urn:action:demo:demo"));
    assertTrue(this.policyStore.isEmpty());
    assertTrue(this.policyStore.storePolicy(pol, ZoneId.of("Europe/Berlin")));
    assertFalse(this.policyStore.isEmpty());
    pol.setId(new String("urn:policy:demo:demo1"));
    assertFalse(this.policyStore.storePolicy(this.createPolicyWithPreventiveMechanism(
        this.createEmptyPolicy(), this.preventiveMechanism), ZoneId.of("Europe/Berlin")));
  }

  /**
   * Removes the policy component_id existent test should result in true.
   *
   * @throws ConflictingPolicyException the conflicting policy exception
   */
  @Test
  void removePolicyIdExistentTest_ShouldResultInTrue() throws Exception {
    when(this.preventiveMechanism.getEvent()).thenReturn(new ActionId("urn:action:demo:demo"));
    assertTrue(this.policyStore.isEmpty());
    assertTrue(this.policyStore.storePolicy(this.createPolicyWithPreventiveMechanism(
        this.createEmptyPolicy(), this.preventiveMechanism), ZoneId.of("Europe/Berlin")));
    assertFalse(this.policyStore.isEmpty());
    verify(this.preventiveMechanism, times(2)).getEvent();
    final Policy pol = this.createEmptyPolicy();
    assertTrue(this.policyStore.removePolicy(pol.getPolicyId()));
    assertTrue(this.policyStore.isEmpty());
  }

  /**
   * Removes the policy component_id non existent test should result in false.
   *
   * @throws ConflictingPolicyException the conflicting policy exception
   */
  @Test
  void removePolicyIdNonExistentTest_ShouldResultInFalse() throws Exception {
    when(this.preventiveMechanism.getEvent()).thenReturn(new ActionId("urn:action:demo:demo"));
    assertTrue(this.policyStore.isEmpty());
    assertTrue(this.policyStore.storePolicy(this.createPolicyWithPreventiveMechanism(
        this.createEmptyPolicy(), this.preventiveMechanism), ZoneId.of("Europe/Berlin")));
    assertFalse(this.policyStore.isEmpty());
    verify(this.preventiveMechanism, times(2)).getEvent();
    final Policy pol = this.createEmptyPolicy();
    final PolicyId polId = new PolicyId("urn:policy:demo1:demo2");
    assertFalse(this.policyStore.removePolicy(polId));
    assertFalse(this.policyStore.isEmpty());
  }

  /**
   * Removes the policy component_id null parameter test should result in false.
   *
   * @throws ConflictingPolicyException the conflicting policy exception
   */
  @Test
  void removePolicyIdNullParameterTest_ShouldResultInFalse() throws Exception {
    assertTrue(this.policyStore.isEmpty());
    assertTrue(this.policyStore.storePolicy(this.createPolicyWithPreventiveMechanism(
        this.createEmptyPolicy(), this.preventiveMechanism), ZoneId.of("Europe/Berlin")));
    assertFalse(this.policyStore.isEmpty());
    verify(this.preventiveMechanism, times(2)).getEvent();
    assertFalse(this.policyStore.removePolicy(null));
    assertFalse(this.policyStore.isEmpty());
  }

  /**
   * Gets the policy by component_id existent component_id test should result in correct policy.
   *
   * @throws ConflictingPolicyException the conflicting policy exception
   */
  @Test
  void getPolicyByIdExistentIdTest_ShouldResultInCorrectPolicy() throws Exception {
    when(this.preventiveMechanism.getEvent()).thenReturn(new ActionId("urn:action:demo:demo"));
    final Policy pol = this.createEmptyPolicy();
    final PolicyId polId = pol.getPolicyId();
    assertTrue(this.policyStore.isEmpty());
    assertNull(this.policyStore.getPolicyByPolicyId(polId));
    assertTrue(this.policyStore.storePolicy(this.createPolicyWithPreventiveMechanism(
        this.createEmptyPolicy(), this.preventiveMechanism), ZoneId.of("Europe/Berlin")));
    assertFalse(this.policyStore.isEmpty());
    assertNotNull(this.policyStore.getPolicyByPolicyId(polId));
    assertEquals(this.policyStore.getPolicyByPolicyId(polId).getPolicyId().toString(),
        pol.getPolicyId().toString());
  }

  /**
   * Gets the policy by component_id non existent component_id test should result in correct policy.
   *
   * @throws ConflictingPolicyException the conflicting policy exception
   */
  @Test
  void getPolicyByIdNonExistentIdTest_ShouldResultInCorrectPolicy() throws Exception {
    Policy pol = this.createEmptyPolicy();
    PolicyId polId = pol.getPolicyId();
    when(this.preventiveMechanism.getEvent()).thenReturn(new ActionId("urn:action:demo:demo"));
    assertTrue(this.policyStore.isEmpty());
    pol = this.policyStore.getPolicyByPolicyId(polId);
    assertNull(pol);
    assertTrue(this.policyStore.storePolicy(this.createPolicyWithPreventiveMechanism(
        this.createEmptyPolicy(), this.preventiveMechanism), ZoneId.of("Europe/Berlin")));
    assertFalse(this.policyStore.isEmpty());
    polId = new PolicyId("urn:policy:demo1:demo2");
    pol = this.policyStore.getPolicyByPolicyId(polId);
    assertNull(pol);
  }

  /**
   * Gets the policy by component_id null parameter test should result in null.
   *
   * @throws ConflictingPolicyException the conflicting policy exception
   */
  @Test
  void getPolicyByIdNullParameterTest_ShouldResultInNull() throws Exception {
    assertTrue(this.policyStore.isEmpty());
    final Policy pol = this.policyStore.getPolicyByPolicyId(null);
    assertNull(pol);
  }

  /**
   * Check policy already deployed test should result in true.
   *
   * @throws ConflictingPolicyException the conflicting policy exception
   */
  @Test
  void checkPolicyAlreadyDeployedTest_ShouldResultInTrue() throws Exception {
    final Policy pol = this.createEmptyPolicy();
    final PolicyId polId = pol.getPolicyId();
    when(this.preventiveMechanism.getEvent()).thenReturn(new ActionId("urn:action:demo:demo"));
    assertTrue(this.policyStore.isEmpty());
    assertTrue(this.policyStore.storePolicy(this.createPolicyWithPreventiveMechanism(
        this.createEmptyPolicy(), this.preventiveMechanism), ZoneId.of("Europe/Berlin")));
    assertFalse(this.policyStore.isEmpty());
    assertTrue(this.policyStore.checkIfPolicyIsAlreadyDeployed(polId));
  }

  /**
   * Check policy already deployed test should result in false.
   *
   * @throws ConflictingPolicyException the conflicting policy exception
   */
  @Test
  void checkPolicyAlreadyDeployedTest_ShouldResultInFalse() throws Exception {
    final Policy pol = this.createEmptyPolicy();
    final PolicyId polId = pol.getPolicyId();
    assertTrue(this.policyStore.isEmpty());
    assertFalse(this.policyStore.checkIfPolicyIsAlreadyDeployed(polId));
  }

  /**
   * Check policy already deployed null parameter test should result in false.
   *
   * @throws ConflictingPolicyException the conflicting policy exception
   */
  @Test
  void checkPolicyAlreadyDeployedNullParameterTest_ShouldResultInFalse() throws Exception {
    assertTrue(this.policyStore.isEmpty());
    final boolean result = this.policyStore.checkIfPolicyIsAlreadyDeployed(null);
    assertFalse(result);
  }

  /**
   * Gets the deployed policy ids test should result in empty list.
   *
   * @throws ConflictingPolicyException the conflicting policy exception
   */
  @Test
  void getDeployedPolicyIdsTest_ShouldResultInEmptyList() throws Exception {
    assertTrue(this.policyStore.isEmpty());
    assertNotNull(this.policyStore.getDeployedPolicyIds());
    assertEquals(0, this.policyStore.getDeployedPolicyIds().size());
  }

  /**
   * Gets the deployed policy ids test should result in filled list.
   *
   * @throws ConflictingPolicyException the conflicting policy exception
   */
  @Test
  void getDeployedPolicyIdsTest_ShouldResultInFilledList() throws Exception {
    final Policy pol = this.createEmptyPolicy();
    final PolicyId polId = pol.getPolicyId();
    when(this.preventiveMechanism.getEvent()).thenReturn(new ActionId("urn:action:demo:demo"));
    assertTrue(this.policyStore.isEmpty());
    assertTrue(this.policyStore.storePolicy(this.createPolicyWithPreventiveMechanism(
        this.createEmptyPolicy(), this.preventiveMechanism), ZoneId.of("Europe/Berlin")));
    assertFalse(this.policyStore.isEmpty());
    final Set<String> deployedIds = this.policyStore.getDeployedPolicyIds();
    assertEquals(1, deployedIds.size());
    assertEquals(deployedIds.iterator().next(), polId.getUrn());
  }

  /**
   * Gets the matching mechanisms null parameter test should result in empty set.
   *
   * @throws ConflictingPolicyException the conflicting policy exception
   */
  @Test
  void getMatchingMechanismsNullParameterTest_ShouldResultInEmptySet() throws Exception {
    assertTrue(this.policyStore.isEmpty());
    final Set<Entry<PolicyId, Set<PolicyMechanism>>> matchingMechanisms = this.policyStore
        .getMatchingMechanisms(null);
    assertEquals(0, matchingMechanisms.size());
  }

  /**
   * Gets the matching mechanisms existent action component_id test should result in set.
   *
   * @throws ConflictingPolicyException the conflicting policy exception
   */
  @Test
  void getMatchingMechanismsExistentActionIdTest_ShouldResultInSet() throws Exception {
    final Policy pol = this.createPolicyWithPreventiveMechanism(this.createEmptyPolicy(),
        this.preventiveMechanism);
    final PolicyId polId = pol.getPolicyId();
    when(this.preventiveMechanism.getEvent()).thenReturn(new ActionId("urn:action:demo:demo"));
    assertTrue(this.policyStore.isEmpty());
    assertTrue(this.policyStore.storePolicy(pol, ZoneId.of("Europe/Berlin")));
    assertFalse(this.policyStore.isEmpty());
    final Set<Entry<PolicyId, Set<PolicyMechanism>>> entrySet = this.policyStore
        .getMatchingMechanisms(new ActionId("urn:action:demo:demo"));
    assertNotNull(entrySet);
    assertEquals(1, entrySet.size());
    for (final Entry<PolicyId, Set<PolicyMechanism>> entry : entrySet) {
      assertEquals(entry.getKey(), polId);
      assertEquals(1, entry.getValue().size());
      assertEquals(entry.getValue().iterator().next(), pol.getMechanisms().iterator().next());
    }
  }

  /**
   * Gets the matching mechanisms non existent action component_id test should result in empty set.
   *
   * @throws ConflictingPolicyException the conflicting policy exception
   */
  @Test
  void getMatchingMechanismsNonExistentActionIdTest_ShouldResultInEmptySet() throws Exception {
    when(this.preventiveMechanism.getEvent()).thenReturn(new ActionId("urn:action:demo:demo"));
    assertTrue(this.policyStore.isEmpty());
    assertTrue(this.policyStore.storePolicy(this.createPolicyWithPreventiveMechanism(
        this.createEmptyPolicy(), this.preventiveMechanism), ZoneId.of("Europe/Berlin")));
    assertFalse(this.policyStore.isEmpty());
    final Set<Entry<PolicyId, Set<PolicyMechanism>>> entrySet = this.policyStore
        .getMatchingMechanisms(new ActionId("urn:action:demo:demoo"));
    assertEquals(0, entrySet.size());
  }

  /**
   * Lock read write first test.
   *
   * @throws InterruptedException   the interrupted exception
   * @throws BrokenBarrierException the broken barrier exception
   */
  @Test
  void lockReadWriteFirstTest() throws Exception {
    final PolicyStoreMapMatching mockStore = Mockito.mock(PolicyStoreMapMatching.class);
    final PolicyStoreMapMatching realStore = new PolicyStoreMapMatching(10);
    final Policy policy = this.createEmptyPolicy();
    final ActionId actionId = new ActionId("urn:action:demo:demo");

    final CyclicBarrier barrier = new CyclicBarrier(9);

    final Runnable firstThreadStorePolicy = new Runnable() {

      @Override
      public void run() {
        realStore.storePolicy(policy, ZoneId.of("Europe/Berlin"));
        mockStore.storePolicy(policy, ZoneId.of("Europe/Berlin"));

        try {
          barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
          e.printStackTrace();
        }
      }
    };

    final Runnable secondThreadGetMechanisms = new Runnable() {
      @Override
      public void run() {
        try {
          mockStore.readLockStore();
          realStore.readLockStore();
          mockStore.getMatchingMechanisms(actionId);
          realStore.getMatchingMechanisms(actionId);
          Thread.sleep(3000); // simulate processing time
        } catch (final InterruptedException e) {
          e.printStackTrace();
        } finally {
          mockStore.readUnlockStore();
          realStore.readUnlockStore();
        }
        try {
          barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
          e.printStackTrace();
        }

      }
    };

    new Thread(firstThreadStorePolicy).start();
    Thread.sleep(2000);
    new Thread(secondThreadGetMechanisms).start();
    new Thread(secondThreadGetMechanisms).start();
    new Thread(secondThreadGetMechanisms).start();
    new Thread(secondThreadGetMechanisms).start();
    new Thread(secondThreadGetMechanisms).start();
    new Thread(secondThreadGetMechanisms).start();
    new Thread(secondThreadGetMechanisms).start();

    barrier.await();

    final InOrder orderVerifier = Mockito.inOrder(mockStore);

    orderVerifier.verify(mockStore).storePolicy(ArgumentMatchers.eq(policy),
        (ArgumentMatchers.eq(ZoneId.of("Europe/Berlin"))));
    orderVerifier.verify(mockStore, times(7)).getMatchingMechanisms(ArgumentMatchers.eq(actionId));
  }

  /**
   * Lock read read first test.
   *
   * @throws InterruptedException   the interrupted exception
   * @throws BrokenBarrierException the broken barrier exception
   */
  @Test
  void lockReadReadFirstTest() throws Exception {
    final PolicyStoreMapMatching mockStore = Mockito.mock(PolicyStoreMapMatching.class);
    final PolicyStoreMapMatching realStore = new PolicyStoreMapMatching(10);
    final Policy policy = this.createEmptyPolicy();
    final ActionId actionId = new ActionId("urn:action:demo:demo");

    final CyclicBarrier barrier = new CyclicBarrier(8);

    final Runnable firstThreadStorePolicy = new Runnable() {

      @Override
      public void run() {
        try {
          realStore.storePolicy(policy, ZoneId.of("Europe/Berlin"));
          mockStore.storePolicy(policy, ZoneId.of("Europe/Berlin"));
          barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
          e.printStackTrace();
        }
      }
    };

    final Runnable secondThreadGetMechanisms = new Runnable() {
      @Override
      public void run() {
        mockStore.readLockStore();
        realStore.readLockStore();
        try {
          mockStore.getMatchingMechanisms(actionId);
          realStore.getMatchingMechanisms(actionId);
          Thread.sleep(3000);
          mockStore.readUnlockStore();
          realStore.readUnlockStore();
          barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
          e.printStackTrace();
        }

      }
    };

    new Thread(secondThreadGetMechanisms).start();
    new Thread(secondThreadGetMechanisms).start();
    new Thread(secondThreadGetMechanisms).start();
    new Thread(secondThreadGetMechanisms).start();
    new Thread(secondThreadGetMechanisms).start();
    new Thread(secondThreadGetMechanisms).start();
    Thread.sleep(2000);
    new Thread(firstThreadStorePolicy).start();

    barrier.await();

    final InOrder orderVerifier = Mockito.inOrder(mockStore);

    orderVerifier.verify(mockStore, times(6)).getMatchingMechanisms(ArgumentMatchers.eq(actionId));
    orderVerifier.verify(mockStore).storePolicy(ArgumentMatchers.eq(policy),
        ArgumentMatchers.eq(ZoneId.of("Europe/Berlin")));
  }
}
