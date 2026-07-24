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

package de.fraunhofer.iese.mydata.pdp.interfaces;

import de.fraunhofer.iese.mydata.pdp.language.model.Policy;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyMechanism;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.exception.ConflictingPolicyException;

import java.time.ZoneId;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Interface that defines the basic methods of a policy store.
 */
public interface IPolicyStore {
  /**
   * Saves (== deploys) a policy to the store. If a policy with the given
   * component_id is already deployed, true is returned.
   *
   * @param policy the policy to be deployed
   * @param zoneId
   * @return TRUE in case the deployment was successful, FALSE otherwise (policy
   *         component_id is null).
   * @throws ConflictingPolicyException In case the policy is in conflict with
   *           another, already deployed, policy
   */
  boolean storePolicy(final Policy policy, final ZoneId zoneId) throws ConflictingPolicyException;

  /**
   * Removes a policy from the store.
   *
   * @param policyId The component_id od the policy to be removed
   * @return TRUE in case the removal was successful, FALSE otherwise (policy is
   *         not deployed, policy component_id is null).
   */
  boolean removePolicy(PolicyId policyId);

  /**
   * Update a policyId from the store.
   *
   * @param oldPolicyId The old policyId
   * @param newPolicyId The old policyId
   * @return TRUE in case the update was successful, FALSE otherwise
   */
  boolean changePolicyId(PolicyId oldPolicyId, PolicyId newPolicyId);

  /**
   * Returns a set of all policy store entries which match the action
   * component_id supplied by the parameter.
   *
   * @param actionId the action component_id which should be matched.
   * @return A SET of all policies that have a trigger which matches the action
   *         component_id. EMPTY SET in case no policy matches the action
   *         component_id (or action component_id is null).
   */
  Set<Entry<PolicyId, Set<PolicyMechanism>>> getMatchingMechanisms(final ActionId actionId);

  /**
   * Returns a (already deployed) policy by its component_id.
   *
   * @param policyId The component_id of the policy to be deployed.
   * @return AN OBJECT that represents the policy with the component_id given by
   *         the method parameter or NULL in case a policy with that
   *         component_id is not deployed.
   */
  Policy getPolicyByPolicyId(PolicyId policyId);

  /**
   * @param policyId
   * @return the zoneId in which the policy runs
   */
  ZoneId getZoneidByPolicyId(PolicyId policyId);

  /**
   * Checks if a policy with a given policy component_id is already deployed.
   *
   * @param policyId The component_id to check the policy deployment
   * @return TRUE if a policy with the given component_id is deployed, FALSE
   *         otherwise (or if policy component_id == null).
   */
  boolean checkIfPolicyIsAlreadyDeployed(PolicyId policyId);

  /**
   * Returns a list which contains the policy ids of all deployed policies.
   *
   * @return A list that contains all deployed policy ids.
   */
  Set<String> getDeployedPolicyIds();

  /**
   * Checks if the policy store is empty.
   *
   * @return TRUE if the store is empty, FALSE otherwise.
   */
  boolean isEmpty();

  /**
   * Clears the policy store, so that all policies are removed.
   */
  void clear();

  /**
   * Locks the store for reading. This method should be invoked prior to any
   * processing and invocation of ({@link #getMatchingMechanisms(ActionId)}).
   * Locking the store during processing ensures, that no new policies are added
   * during processing of existing policies. This avoids inconsistent states.
   * This method and {@link #readUnlockStore()} must be called by the same
   * thread.
   *
   * @see IPolicyStore#readUnlockStore()
   */
  void readLockStore();

  /**
   * Unlocks the store for reading, after is has been locked by
   * {@link #readLockStore()}. Both methods must be called by the same thread.
   *
   * @see IPolicyStore#readLockStore()
   */
  void readUnlockStore();
}
