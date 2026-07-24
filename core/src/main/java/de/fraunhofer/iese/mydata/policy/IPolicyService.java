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

package de.fraunhofer.iese.mydata.policy;

import de.fraunhofer.iese.mydata.exception.ConflictingResourceException;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.exception.ResourceUpdateException;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import java.io.IOException;
import java.util.Set;

/**
 * The Interface IPolicyService.
 */
public interface IPolicyService {

  /**
   * Adds a policy and add it to the list of policies of the corresponding solution
   *
   * @param  policy                       the policy
   * @return                              policy id of the added policy, will never be null
   * @throws IOException                  Signals that an I/O exception has occurred.
   * @throws ResourceUpdateException      if add is not successful
   * @throws ConflictingResourceException if the policy id already exists
   * @throws InvalidEntityException       if the policy is not valid
   * @throws NoSuchEntityException        if the solution does not exist
   */
  PolicyId addPolicy(Policy policy) throws IOException, ConflictingResourceException,
      ResourceUpdateException, InvalidEntityException, NoSuchEntityException;

  boolean policyExists(PolicyId policyId) throws IOException, InvalidEntityException;

  /**
   * Gets the policy.
   *
   * @param  policyId               the policy component_id
   * @return                        the latest policy
   * @throws IOException            Signals that an I/O exception has occurred.
   * @throws NoSuchEntityException  if there is no policy with the id given
   * @throws InvalidEntityException if the policy id is not valid
   */
  Policy getPolicy(PolicyId policyId)
      throws IOException, NoSuchEntityException, InvalidEntityException;

  /**
   * Gets all policy.
   *
   * @param  solutionId             the solutionId
   * @return                        all policy
   * @throws IOException            Signals that an I/O exception has occurred.
   * @throws InvalidEntityException if the solution id is not valid
   * @throws NoSuchEntityException  if the solution does not exist
   */
  Set<Policy> getPolicies(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException;

  /**
   * Gets all policy ids.
   *
   * @param  solutionId             the solutionid
   * @return                        all policy IDs
   * @throws IOException            Signals that an I/O exception has occurred.
   * @throws InvalidEntityException if the solution id is not valid
   * @throws NoSuchEntityException  if the solution does not exist
   */
  Set<PolicyId> listPolicies(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException;

  /**
   * Gets all deployed policies. needed for PDP
   *
   * @return             all policy
   * @throws IOException Signals that an I/O exception has occurred.
   */
  Set<Policy> getDeployedPolicies() throws IOException;

  /**
   * Gets all deployed policy for a solution.
   *
   * @param  solutionId             the solutionId
   * @return                        all policy
   * @throws IOException            Signals that an I/O exception has occurred.
   * @throws InvalidEntityException if the solution id is not valid
   * @throws NoSuchEntityException  if the solution does not exist
   */
  Set<Policy> getDeployedPolicies(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException;

  /**
   * Gets all revoked policy for a solution.
   *
   * @param  solutionId             the solutionId
   * @return                        all policy
   * @throws IOException            Signals that an I/O exception has occurred.
   * @throws InvalidEntityException if the solution id is not valid
   * @throws NoSuchEntityException  if the solution does not exist
   */
  Set<Policy> getRevokedPolicies(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException;

  /**
   * Provides a list of currently deployed policies.
   *
   * @param  solutionId
   * @return                        list of currently deployed policy ids
   * @throws IOException            communication failure
   * @throws InvalidEntityException if the solution id is not valid
   * @throws NoSuchEntityException  if the solution does not exist
   */
  Set<PolicyId> listDeployedPolicies(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException;

  /**
   * Provides a list of currently revoked policies.
   *
   * @param  solutionId
   * @return                        list of currently revoked policy ids
   * @throws IOException            communication failure
   * @throws InvalidEntityException if the solution id is not valid
   * @throws NoSuchEntityException
   */
  Set<PolicyId> listRevokedPolicies(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException;

  /**
   * Checks if a policy is deployed.
   *
   * @param  policyId               the policy component_id
   * @return                        true, if is deployed
   * @throws IOException            Signals that an I/O exception has occurred.
   * @throws NoSuchEntityException  if there is no policy with the given id
   * @throws InvalidEntityException if the policy id is not valid
   */
  boolean isPolicyDeployed(PolicyId policyId)
      throws IOException, NoSuchEntityException, InvalidEntityException;

  /**
   * Updates a currently deployed policy.
   *
   * @param  policy                  the policy to be updated
   * @return                         true, if the policy was updated successfully, false otherwise
   * @throws IOException             if connection problem occurs
   * @throws ResourceUpdateException if update is not successful
   * @throws NoSuchEntityException   if there is no policy
   * @throws InvalidEntityException  if the policy is not valid
   */
  PolicyId updatePolicy(Policy policy)
      throws IOException, ResourceUpdateException, NoSuchEntityException, InvalidEntityException;

  /**
   * Deploys a policy at the local PDP of the solution.
   *
   * @param  policyId                the component_id of the policy to be deployed. Will be
   *                                   retrieved via the PRP
   * @throws IOException             if connection problem occurs
   * @throws ResourceUpdateException if deploy was not successful
   * @throws NoSuchEntityException   if there is no policy with the given id
   * @throws InvalidEntityException  if the policyid to be deployed is not valid
   */
  void deployPolicy(PolicyId policyId)
      throws IOException, NoSuchEntityException, ResourceUpdateException, InvalidEntityException;

  /**
   * Revokes a currently deployed policy.
   *
   * @param  policyId                the ID of the policy to be revoked
   * @throws IOException             if connection problem occurs
   * @throws NoSuchEntityException   if there is no policy with the given id
   * @throws ResourceUpdateException if revoke is not successful
   * @throws InvalidEntityException  if the given policy id is not valid
   */
  void revokePolicy(PolicyId policyId)
      throws IOException, NoSuchEntityException, ResourceUpdateException, InvalidEntityException;

  /**
   * Removes the policy.
   *
   * @param  policyId                     the policyid to delete
   * @throws IOException                  Signals that an I/O exception has occurred.
   * @throws ResourceUpdateException      if delete is not successful
   * @throws NoSuchEntityException        if there is no policy with the given id
   * @throws InvalidEntityException       if the policy id is not valid
   * @throws ConflictingResourceException if the policy to be deleted is deployed
   */
  void deletePolicy(PolicyId policyId) throws IOException, ResourceUpdateException,
      NoSuchEntityException, InvalidEntityException, ConflictingResourceException;
}
