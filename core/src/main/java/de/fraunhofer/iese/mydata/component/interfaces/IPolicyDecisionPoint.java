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

package de.fraunhofer.iese.mydata.component.interfaces;

import de.fraunhofer.iese.mydata.exception.ResourceUpdateException;
import de.fraunhofer.iese.mydata.policy.Policy;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.policy.decision.AuthorizationDecision;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.ConflictingPolicyException;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import org.w3c.dom.DOMException;

import java.io.IOException;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import org.jspecify.annotations.Nullable;

/**
 * The PolicyDecisionPoint draws decisions based on the currently deployed policies.
 */
public interface IPolicyDecisionPoint extends IMyDataComponent {

  /**
   * Adds a solution the to blacklist.
   *
   * @param  ids         the ids
   * @return             true, if successful
   * @throws IOException Signals that an I/O exception has occurred.
   */
  boolean addToBlacklist(Set<SolutionId> ids) throws IOException;

  /**
   * Clears all caches of the PDP (response and connector caches) about data related to the
   * specified solution or any if solutionId==null.
   *
   * @param  solutionId  can be null to clear any data from the caches
   * @return             true, if cache was cleaned
   * @throws IOException if there occurs a connection problem.
   */
  boolean clearAllCaches(@Nullable SolutionId solutionId) throws IOException;

  /**
   * Notifies the PDP about a certain event. Based on the event, the policies are evaluated and
   * decision is drawn.
   *
   * @param  event                          the occurred event
   * @return                                the {@link AuthorizationDecision} with respect to the
   *                                        event and deployed policies
   * @throws IOException                    if there occurs a connection problem.
   * @throws EvaluationUndecidableException if PDP can't evaluate the event due to internal
   *                                          ambiguity.
   */
  AuthorizationDecision decisionRequest(Event event)
      throws IOException, EvaluationUndecidableException;

  /**
   * Notifies the PDP about multiple events. Based on the events, the policies are evaluated and
   * decision is drawn.
   *
   * @param  events      the occurred events
   * @return             the {@link AuthorizationDecision}s with respect to the event and deployed
   *                     policies
   * @throws IOException if there occurs a connection problem.
   */
  List<AuthorizationDecision> decisionRequests(List<Event> events) throws IOException;

  /**
   * Deploys a policy.
   *
   * @param  policy                     the policy to deploy
   * @param  zoneIdOfSolution           zoneId of solution
   * @return                            true if the policy is deployed, false otherwise
   * @throws IOException                if there occurs a connection problem.
   * @throws ConflictingPolicyException if multiple policy with same component_id are found in PMP
   */
  // TODO make impl throw ConflictingPolicyException or remove it from the
  // Interface
  boolean deploy(Policy policy, ZoneId zoneIdOfSolution)
      throws IOException, ConflictingPolicyException;

  /**
   * Evaluate an event.
   *
   * @param  event       the event
   * @return             true, if successful
   * @throws IOException Signals that an I/O exception has occurred.
   */
  boolean evaluate(Event event) throws IOException;

  /**
   * Evaluate multiple events.
   *
   * @param  events      the events
   * @return             true, if successful
   * @throws IOException Signals that an I/O exception has occurred.
   */
  boolean evaluate(Set<Event> events) throws IOException;

  /**
   * Provides a list of currently deployed policy names.
   *
   * @return             list of currently deployed policy names
   * @throws IOException if there occurs a connection problem.
   */
  Set<String> listDeployedPolicies() throws IOException;

  /**
   * Removes a solution from the blacklist.
   *
   * @param  ids         the ids
   * @return             true, if successful
   * @throws IOException Signals that an I/O exception has occurred.
   */
  boolean removeFromBlacklist(Set<SolutionId> ids) throws IOException;

  /**
   * Revokes a policy.
   *
   * @param  policyId                the name of the policy to be revoked
   * @return                         true if the policy is revoked, false otherwise
   * @throws IOException             if there occurs a connection problem.
   * @throws ResourceUpdateException fails to revoke the policy
   */
  boolean revokePolicy(PolicyId policyId) throws IOException, ResourceUpdateException;

  boolean isInFailureMode();

  /**
   * Sets the failure mode.
   *
   * @param  active      the new failure mode
   * @throws IOException Signals that an I/O exception has occurred.
   */
  void setFailureMode(boolean active) throws IOException;

  /**
   * Updates a policy.
   *
   * @param  policy                   the policy to be updated
   * @param  zoneIdOfSolution         zoneId of solution
   * @return                          true if the policy is updated, false otherwise
   * @throws IOException              if there occurs a connection problem.
   * @throws ResourceUpdateException  fails to update the policy
   * @throws DOMException
   * @throws IllegalArgumentException
   */
  boolean updatePolicy(Policy policy, ZoneId zoneIdOfSolution)
      throws IOException, ResourceUpdateException;

  /**
   * Update policyId
   *
   * @param  policyWithNewId         the policy with the new id
   * @param  zoneIdOfSolution        zoneId of solution
   * @param  oldPolicyId             the previous policy id
   * @return                         true, if successful
   * @throws IOException             Signals that an I/O exception has occurred.
   * @throws ResourceUpdateException the policy update exception
   */
  boolean updatePolicyAndId(Policy policyWithNewId, ZoneId zoneIdOfSolution, PolicyId oldPolicyId)
      throws IOException, ResourceUpdateException;

  /**
   * Whitelist mode
   *
   * @return true if whitelist mode is enabled, false is blacklist mode
   */
  boolean isWhitelistModeEnabled();

}
