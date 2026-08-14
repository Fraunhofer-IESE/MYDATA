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

package de.fraunhofer.iese.mydata.policy.event.history;

import de.fraunhofer.iese.mydata.policy.Policy;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IEventRepository {

  void deleteEventOccurrenceByActionId(ActionId actionId);

  /**
   * Provide concrete implementation for deletion of value changed .
   *
   * @param policyId PolicyId
   * @param variableValueChangeBlock Value change block with variable replaced
   *          with variable declaration xml block from policy
   */
  void deleteValueChangeBlock(PolicyId policyId, Map<String, String> variableValueChangeBlock);

  List<HistoricEvent> findAll();

  List<HistoricEvent> findByActionId(ActionId actionId);

  List<HistoricEvent> findByActionIdAndHistoricEventParametersAndOccurredAtMsBetween(ActionId actionId, List<HistoricEventParameter> hep, long start, long end);

  HistoricEvent findByActionIdAndMode(ActionId actionId, String mode, List<HistoricEventParameter> policyEventParameters);

  List<HistoricEvent> findByActionIdAndOccurredAtMsBetweenParamIndependant(ActionId actionId, long start, long end);

  long findByOccurredAtMsAfterAndActionId(long start, ActionId actionId);

  long findByOccurredAtMsBeforeAndActionId(long end, ActionId actionId);

  List<HistoricEvent> findByOccurredAtMsBetweenAndActionId(long start, long end, ActionId actionId);

  /* VisibleForTesting */
  /**
   * Returns {@code Map<ActionId, List<String>>}, The parameters to save for that event
   *
   * @return Parameters of the event that needs to be stored
   */
  Map<ActionId, Set<HistoricEventTrackItem>> getHistoricEventTrackItemsPerActionId();

  /* VisibleForTesting */
  /**
   * Returns {@code Map<ActionId, List<String>>}, Events with ActionId are to be stored.
   *
   * @return @{@link Map}
   */
  Map<ActionId, Set<PolicyId>> getEventsToBeStored();

  String getValueChanged(String blockId, Policy policy);

  /**
   * When PDP is notified via event then same call is passed to EventRepository.
   * EventRepository will take care of storing of events if required.
   *
   * @param event notified.
   */
  void notify(Event event);

  /**
   * When policy is deployed call this method to update event storing map.
   *
   * @param policy to be deployed
   */
  void policyDeployed(Policy policy);

  /**
   * When policy is revoked, this method needs to be called to update Map used
   * to filter policy.
   *
   * @param policy policy to be revoked
   */
  void policyRevoked(Policy policy);

  /**
   * When policy is updated, call this method.
   *
   * @param policy Policy to be updated
   */
  void policyUpdate(Policy policy);

  /**
   * When policy or the policyId is updated, call this method.
   *
   * @param policy Policy to be updated
   * @param oldPolicyId the previous/old policyId
   */
  void policyUpdate(Policy policy, PolicyId oldPolicyId);

  /**
   * Provide concrete implementation for storing of events.
   *
   * @param event event to be stored
   * @param trackItems elements of the event that needs to be stored
   */
  void saveEventOccurrence(Event event, Set<HistoricEventTrackItem> trackItems);

  /**
   * Provide concrete implementation for storing of value changed .
   *
   * @param policy the policy
   * @param variableValueChangeBlock Value change block with variable replaced
   *          with variable declaration xml block from policy
   */
  void saveValueChangeBlock(Policy policy, Map<String, String> variableValueChangeBlock);

  void setValueChanged(Policy policy, String blockId, String valueInPolicy) throws Exception;

  /**
   * Provide concrete policyID update for the stored value changed .
   *
   * @param newPolicy the new policy after policy edition
   * @param oldPolicyId the old policyID
   */
  void updateValueChangeBlockPolicyId(Policy newPolicy, PolicyId oldPolicyId);

  /**
   * Reset internal settings what events to save but does not clear the database
   */
  void reset();

}
