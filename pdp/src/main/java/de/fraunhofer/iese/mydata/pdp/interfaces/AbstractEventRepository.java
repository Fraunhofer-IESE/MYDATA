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

import de.fraunhofer.iese.mydata.policy.Policy;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.event.history.HistoricEventTrackItem;
import de.fraunhofer.iese.mydata.policy.event.history.IEventRepository;
import de.fraunhofer.iese.mydata.util.SecureXmlUtils;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactoryConfigurationException;

/**
 * Abstract class that needs to be implemented for storage and fetching of events.
 */
public abstract class AbstractEventRepository implements IEventRepository {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(AbstractEventRepository.class);

  /**
   * Events that needs to be stored
   */
  private final Map<ActionId, Set<PolicyId>> eventsToBeStored = new ConcurrentHashMap<>();

  /**
   * Value change block with variable replaced with variable declaration xml block from policy
   */
  private final Map<PolicyId, Map<String, String>> valueChangeBlockWithVariableBlock = new ConcurrentHashMap<>();

  private final Lock eventHistoryTrackingLock = new ReentrantLock(true);

  /**
   * Parts of the event that needs to be stored and the policy that keeps track of them
   */
  private final Map<ActionId, Map<HistoricEventTrackItem, Set<PolicyId>>> historicEventTrackItemsPerAction = new ConcurrentHashMap<>();

  private Set<HistoricEventTrackItem> getHistoricEventTrackItemsForActionId(ActionId actionId) {
    // do not touch my internal state!
    return Collections.unmodifiableSet(this.historicEventTrackItemsPerAction
        .getOrDefault(actionId, Collections.emptyMap()).keySet());
  }

  @Override
  @VisibleForTesting
  public Map<ActionId, Set<PolicyId>> getEventsToBeStored() {
    // do not touch my internal state!
    final Lock lock = this.eventHistoryTrackingLock;
    lock.lock();
    try {
      return Collections.unmodifiableMap(
          this.eventsToBeStored.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
              o -> Collections.unmodifiableSet(new HashSet<>(o.getValue())))));
    } finally {
      lock.unlock();
    }

  }

  @Override
  @VisibleForTesting
  public Map<ActionId, Set<HistoricEventTrackItem>> getHistoricEventTrackItemsPerActionId() {
    // do not touch my internal state!
    return Collections.unmodifiableMap(
        this.historicEventTrackItemsPerAction.entrySet().stream().collect(Collectors
            .toMap(Map.Entry::getKey, o -> Collections.unmodifiableSet(o.getValue().keySet()))));
  }

  @Override
  public void reset() {
    // TODO what about the valueChangeBlock stuff?
    final Lock lock = this.eventHistoryTrackingLock;
    lock.lock();
    try {
      this.eventsToBeStored.clear();
      this.historicEventTrackItemsPerAction.clear();
    } finally {
      lock.unlock();
    }
  }

  /**
   * Method to be called when policy is deployed. It updates events that needs to be stored.
   */
  private void deployPolicyEventStored(final Policy policy) {
    LOG.trace("deployPolicyEventStored");
    final PolicyId policyId = policy.getPolicyId();
    final Set<ActionId> actionIdsToBeStored = this.getActionIdsToBeStored(policy);
    final Map<ActionId, Set<HistoricEventTrackItem>> historicEventTrackItemsFromThisPolicy = this
        .getHistoricEventTrackItemsPerActionId(policy);
    final Lock lock = this.eventHistoryTrackingLock;
    lock.lock();
    try {
      actionIdsToBeStored.forEach(actionId -> {
        final Set<HistoricEventTrackItem> trackItemsToAdd = historicEventTrackItemsFromThisPolicy
            .getOrDefault(actionId, Collections.emptySet());
        if (!trackItemsToAdd.isEmpty()) {
          final Map<HistoricEventTrackItem, Set<PolicyId>> trackItemsWithCause = this.historicEventTrackItemsPerAction
              .computeIfAbsent(actionId, actionId1 -> new ConcurrentHashMap<>());
          for (final HistoricEventTrackItem trackItem : trackItemsToAdd) {
            trackItemsWithCause
                .computeIfAbsent(trackItem, historicEventTrackItem -> new HashSet<>())
                .add(policyId);
          }
        }
        this.eventsToBeStored.computeIfAbsent(actionId, actionId1 -> new HashSet<>()).add(policyId);
      });
    } finally {
      lock.unlock();
    }
  }

  /**
   * Method to be called when policy is deployed. It updates value change block.
   */
  private void deployPolicyValueChange(Policy policy, PolicyId policyId) {
    this.updateValueChangeBlockToBeStored(policy);
    this.saveValueChangeBlock(policy, this.valueChangeBlockWithVariableBlock.get(policyId));
  }

  /**
   * Generates the list of actions that have to be stored for the counts of event occurrences
   *
   * @param  policy policy
   * @return        List of actionId to be stored for given policy
   */
  private Set<ActionId> getActionIdsToBeStored(Policy policy) {
    final Set<ActionId> actionIds = new HashSet<>();

    try {

      final Document document = SecureXmlUtils.parseXml(policy.getContent());
      final XPath xpath = SecureXmlUtils.createSecureXPath();

      // Fetch event data
      final XPathExpression expr = xpath.compile("//eventOccurrence/@event");
      final NodeList names = (NodeList) expr.evaluate(document, XPathConstants.NODESET);

      for (int i = 0; i < names.getLength(); i++) {
        final String s = names.item(i).getNodeValue();
        actionIds.add(new ActionId(s));
      }
    } catch (ParserConfigurationException | IOException | SAXException | XPathExpressionException
        | XPathFactoryConfigurationException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    return actionIds;
  }

  /**
   * Builds a map of actionsID and list of parameters Parse the policy into XML, extract all
   * eventOccurrence nodes. For each eventOccurrence, get the event to be saved and its parameters.
   * The name of the event is used as an identifier example: for the bloc
   * <eventOccurrence event='urn:action:banking-demo:get-userprofile'>
   * <parameter:string name='user'>
   * <event:string eventParameter='user' default='' jsonPathQuery= '.$firstName'/>, it will have to
   * save the with the key "user" the value of the user parameter, independently of the following
   * <event:string line" <eventOccurrence event='urn:action:banking-demo:get-userprofile'>
   * <parameter:object name='user' jsonPathQuery="$.lastName">
   * <event:string eventParameter='user' default='' jsonPathQuery= '.$firstName'/> will save with
   * the keys "user" path="$.lastName" the value of the user.lastName
   * <eventOccurrence event='urn:action:banking-demo:get-userprofile'> *
   * <parameter:object name='user' jsonPathQuery="$.lastName" value="bob"> * <event:string
   * eventParameter='user' default='' jsonPathQuery= * '.$firstName'/> will save with the keys
   * "user" path="$.lastName" the value "bob"
   *
   * @param  policy policy
   * @return        Map of all actionId's and parameters to be saved
   */
  private Map<ActionId, Set<HistoricEventTrackItem>> getHistoricEventTrackItemsPerActionId(
      Policy policy) {
    final Map<ActionId, Set<HistoricEventTrackItem>> resultMap = new HashMap<>();
    try {
      final Document document = SecureXmlUtils.parseXml(policy.getContent());

      final NodeList eventOccNodes = document.getElementsByTagName("eventOccurrence");
      for (int i = 0; i < eventOccNodes.getLength(); i++) {
        final Node parent = eventOccNodes.item(i);
        final ActionId actionId = new ActionId(
            parent.getAttributes().getNamedItem("event").getNodeValue());
        final Set<HistoricEventTrackItem> toSaveList = resultMap.computeIfAbsent(actionId,
            ignored -> new HashSet<>());

        final NodeList children = parent.getChildNodes();
        for (int k = 0; k < children.getLength(); k++) {
          final Node child = children.item(k);
          if (child.getAttributes() != null && child.getAttributes().getNamedItem("name") != null) {
            final String[] objectName = new String[3];
            objectName[0] = child.getAttributes().getNamedItem("name").getNodeValue();
            if (child.getAttributes().getNamedItem("jsonPathQuery") != null) {
              final String jpq = child.getAttributes().getNamedItem("jsonPathQuery").getNodeValue();
              objectName[1] = jpq;
            }
            if (child.getAttributes().getNamedItem("value") != null) {
              final String staticValue = child.getAttributes().getNamedItem("value").getNodeValue();
              objectName[2] = staticValue;
            }
            toSaveList.add(
                HistoricEventTrackItem.of(actionId, objectName[0], objectName[1], objectName[2]));

          }
        }
      }
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    return resultMap;
  }

  /**
   * Return the node serialized as a xml string
   *
   * @param  node node
   * @return      serialized node
   */
  private String getSerializedNode(Node node) {
    try {
      return SecureXmlUtils.serializeNode(node);
    } catch (final Exception e) {
      LOG.debug("The xml node could not be serialized", e);
      return "";
    }
  }

  /**
   * When PDP is notified via event then same call is passed to EventRepository. EventRepository
   * will take care of storing of events if required.
   *
   * @param event notified.
   */
  @Override
  public void notify(Event event) {
    // it is okay to check eventsToBeStored this way as it is a ConcurrentHashMap
    if (this.eventsToBeStored.containsKey(event.getActionId())) {
      final Set<HistoricEventTrackItem> trackItems = this
          .getHistoricEventTrackItemsForActionId(event.getActionId());
      this.saveEventOccurrence(event, trackItems);
    }
  }

  /**
   * When policy is deployed call this method to update event storing map.
   *
   * @param policy to be deployed
   */
  @Override
  public void policyDeployed(Policy policy) {
    this.deployPolicyEventStored(policy);
    this.deployPolicyValueChange(policy, policy.getPolicyId());
  }

  /**
   * When policy is revoked, this method needs to be called to update Map used to filter policy.
   *
   * @param policy policy to be revoked
   */
  @Override
  public void policyRevoked(Policy policy) {
    this.revokePolicyUpdateEventStored(policy.getPolicyId());
    if (this.valueChangeBlockWithVariableBlock.containsKey(policy.getPolicyId())
        && this.valueChangeBlockWithVariableBlock.get(policy.getPolicyId()).size() > 0) {
      this.deleteValueChangeBlock(policy.getPolicyId(),
          this.valueChangeBlockWithVariableBlock.get(policy.getPolicyId()));
    }
    this.valueChangeBlockWithVariableBlock.remove(policy.getPolicyId());
  }

  /**
   * When policy is updated, call this method.
   *
   * @param policy Policy to be updated
   */
  @Override
  public void policyUpdate(Policy policy) {
    this.policyUpdateEventStored(policy.getPolicyId(), policy);

    this.valueChangeBlockWithVariableBlock.remove(policy.getPolicyId());

    this.updateValueChangeBlockToBeStored(policy);
    this.saveValueChangeBlock(policy,
        this.valueChangeBlockWithVariableBlock.get(policy.getPolicyId()));
  }

  /**
   * When policy or the policyId is updated, call this method.
   *
   * @param policy Policy to be updated
   */
  @Override
  public void policyUpdate(Policy policy, PolicyId oldPolicyId) {
    // first update the map with the new policyId

    // do not call both as it duplicates logic
    //    this.updateEventsToBeStored(policy.getPolicyId(), oldPolicyId);

    // handle the events like usual
    this.policyUpdateEventStored(oldPolicyId, policy);

    // update the valueChangeBlockWithVariableBlock with the newId
    this.updateValueChangeBlockWithVariableBlock(policy.getPolicyId(), oldPolicyId);
    // handle the valuechangeblock like usual
    this.valueChangeBlockWithVariableBlock.remove(policy.getPolicyId());

    // handle the valuechange from the policy like usual
    this.updateValueChangeBlockToBeStored(policy);
    // update the policyId in the DB
    this.updateValueChangeBlockPolicyId(policy, oldPolicyId);
    this.saveValueChangeBlock(policy,
        this.valueChangeBlockWithVariableBlock.get(policy.getPolicyId()));
  }

  private void policyUpdateEventStored(final PolicyId idThePolicyIsKnownWith, final Policy policy) {
    // TODO write some tests for this method
    final Set<ActionId> idsOfActionsWhoseHistoryShouldBeDeleted = new HashSet<>();
    final PolicyId policyId = policy.getPolicyId();
    final Set<ActionId> actionIdsToBeStored = this.getActionIdsToBeStored(policy);
    final Map<ActionId, Set<HistoricEventTrackItem>> historicEventTrackItemsFromThisPolicy = this
        .getHistoricEventTrackItemsPerActionId(policy);

    final Lock lock = this.eventHistoryTrackingLock;
    lock.lock();
    try {
      {
        final Set<ActionId> affectedActionIds = this.eventsToBeStored.entrySet().stream()
            .filter(e -> e.getValue().contains(idThePolicyIsKnownWith)).map(Map.Entry::getKey)
            .collect(Collectors.toSet());
        affectedActionIds.addAll(actionIdsToBeStored);
        affectedActionIds.forEach(actionId -> {
          final Set<PolicyId> policiesThatCare = this.eventsToBeStored.get(actionId);
          boolean noPolicyObservesThisAction = false;
          if (null != policiesThatCare) {
            if (actionIdsToBeStored.contains(actionId)) {
              // we are interested in this action
              if (idThePolicyIsKnownWith.equals(policyId)) {
                // make sure we are on the list, maybe we are new
                policiesThatCare.add(policyId);
              } else {
                // make sure we are on the list and old entries get removed
                policiesThatCare.add(policyId);
                policiesThatCare.remove(idThePolicyIsKnownWith);
              }
            } else {
              // we are not interested in this action, make sure our name is not on the list
              policiesThatCare.remove(idThePolicyIsKnownWith);
              if (policiesThatCare.isEmpty()) {
                // we were the last subscriber
                noPolicyObservesThisAction = true;
                assert !actionIdsToBeStored.contains(actionId);
                idsOfActionsWhoseHistoryShouldBeDeleted.add(actionId);
                this.eventsToBeStored.remove(actionId);
              }
            }
          } else if (actionIdsToBeStored.contains(actionId)) {
            // no one is interested in, but we are now
            this.eventsToBeStored.compute(actionId, (actionId1, existingSet) -> {
              assert existingSet == null;
              final Set<PolicyId> policyIds = new HashSet<>();
              policyIds.add(policyId);
              return policyIds;
            });
          } else {
            // really no one is interested in
            noPolicyObservesThisAction = true;
          }
          if (noPolicyObservesThisAction) {
            // shortcut to improve performance for this case
            this.historicEventTrackItemsPerAction.remove(actionId);
          } else {
            final Set<HistoricEventTrackItem> trackItemsToAdd = historicEventTrackItemsFromThisPolicy
                .getOrDefault(actionId, Collections.emptySet());
            final Map<HistoricEventTrackItem, Set<PolicyId>> trackItemsWithCause = this.historicEventTrackItemsPerAction
                .get(actionId);
            if (null != trackItemsWithCause) {
              trackItemsWithCause.values()
                  .forEach(policyIds -> policyIds.remove(idThePolicyIsKnownWith));
              if (!trackItemsToAdd.isEmpty()) {
                for (final HistoricEventTrackItem trackItem : trackItemsToAdd) {
                  trackItemsWithCause
                      .computeIfAbsent(trackItem, historicEventTrackItem -> new HashSet<>())
                      .add(policyId);
                }
              }
              trackItemsWithCause.entrySet().stream()
                  // only those no one cares about
                  .filter(e -> e.getValue().isEmpty())
                  // name them
                  .map(Map.Entry::getKey)
                  // remove them
                  .forEach(trackItemsWithCause::remove);
              if (trackItemsWithCause.isEmpty()) {
                this.historicEventTrackItemsPerAction.remove(actionId);
              }
            } else {
              if (!trackItemsToAdd.isEmpty()) {
                // no one tracks something, but we want to track something
                final Map<HistoricEventTrackItem, Set<PolicyId>> trackItemsWithCauseCreated = this.historicEventTrackItemsPerAction
                    .computeIfAbsent(actionId, actionId1 -> new ConcurrentHashMap<>());
                for (final HistoricEventTrackItem trackItem : trackItemsToAdd) {
                  trackItemsWithCauseCreated
                      .computeIfAbsent(trackItem, historicEventTrackItem -> new HashSet<>())
                      .add(policyId);
                }
              }
            }
          }
        });
      }
      idsOfActionsWhoseHistoryShouldBeDeleted.forEach(this::deleteEventOccurrenceByActionId);
    } finally {
      lock.unlock();
    }
  }

  /**
   * Method to be called when policy is revoked. It updates events that needs to be stored. If event
   * was stored because of one policy then all corresponding events are deleted.
   */
  private void revokePolicyUpdateEventStored(final PolicyId policyId) {
    final Set<ActionId> idsOfActionsWhoseHistoryShouldBeDeleted = new HashSet<>();
    final Lock lock = this.eventHistoryTrackingLock;
    lock.lock();
    try {
      final Set<ActionId> affectedActionIds = this.eventsToBeStored.entrySet().stream()
          .filter(e -> e.getValue().contains(policyId)).map(Map.Entry::getKey)
          .collect(Collectors.toSet());
      affectedActionIds.forEach(actionId -> {
        final Set<PolicyId> policiesThatCare = this.eventsToBeStored.get(actionId);
        boolean noPolicyObservesThisAction = false;
        if (null != policiesThatCare) {
          if (policiesThatCare.size() == 1 && policyId.equals(policiesThatCare.iterator().next())) {
            this.eventsToBeStored.remove(actionId);
            noPolicyObservesThisAction = true;
            idsOfActionsWhoseHistoryShouldBeDeleted.add(actionId);
          } else {
            policiesThatCare.remove(policyId);
          }
        } else {
          noPolicyObservesThisAction = true;
        }
        if (noPolicyObservesThisAction) {
          // shortcut to improve performance for this case
          this.historicEventTrackItemsPerAction.remove(actionId);
        } else {
          final Map<HistoricEventTrackItem, Set<PolicyId>> historicEventTrackItemsForThisAction = this.historicEventTrackItemsPerAction
              .get(actionId);
          if (null != historicEventTrackItemsForThisAction) {
            historicEventTrackItemsForThisAction.values()
                .forEach(policyIds -> policyIds.remove(policyId));
            historicEventTrackItemsForThisAction.entrySet().stream()
                // only those no one cares about
                .filter(e -> e.getValue().isEmpty())
                // name them
                .map(Map.Entry::getKey)
                // remove them
                .forEach(historicEventTrackItemsForThisAction::remove);
            if (historicEventTrackItemsForThisAction.isEmpty()) {
              this.historicEventTrackItemsPerAction.remove(actionId);
            }
          }
        }
      });
    } finally {
      lock.unlock();
    }
    idsOfActionsWhoseHistoryShouldBeDeleted.forEach(this::deleteEventOccurrenceByActionId);
  }

  //  /**
  //   * Method to be called when policy is revoked. It updates events that needs to
  //   * be stored. If event was stored because of one policy then all corresponding
  //   * events are deleted.
  //   */
  //  private void updateEventsToBeStored(PolicyId policyId, PolicyId oldPolicyId) {
  //    final List<ActionId> actionIdToBeUpdated = this.eventsToBeStored.entrySet().stream().filter(e -> e.getValue().contains(oldPolicyId)).map(Map.Entry::getKey).collect(Collectors.toList());
  //    for (final ActionId actionId2 : actionIdToBeUpdated) {
  //      final Set<PolicyId> policyIds = this.eventsToBeStored.get(actionId2);
  //      for (final PolicyId policyId2 : policyIds) {
  //        if (policyId2.equals(oldPolicyId)) {
  //          policyIds.remove(oldPolicyId);
  //          policyIds.add(policyId);
  //        }
  //      }
  //      this.eventsToBeStored.put(actionId2, policyIds);
  //    }
  //  }

  /**
   * Save in a map the name of the declared variable and its declaration in xml for a policyid Save
   * in a map the name of the declared variable and its reference in a valuechange block for a
   * policyid
   *
   * @param policy policy
   */
  private void updateValueChangeBlockToBeStored(Policy policy) {
    final Map<String, String> variableDeclarationMap = new HashMap<>();
    final Map<String, String> variableValueChangedMap = new HashMap<>();
    String variableName;

    try {
      final Document document = SecureXmlUtils.parseXml(policy.getContent());

      NodeList variableDeclarations;
      final List<String> variableTypes = Arrays.asList("boolean", "number", "string", "object",
          "list");
      for (final String variableType : variableTypes) {
        // get all declared variable
        variableDeclarations = document.getElementsByTagName("variableDeclaration:" + variableType);
        for (int i = 0; i < variableDeclarations.getLength(); i++) {
          final Node variableDeclaration = variableDeclarations.item(i);

          if (variableDeclaration.getAttributes().getNamedItem("name") != null) {
            variableName = variableDeclaration.getAttributes().getNamedItem("name").getNodeValue();
            variableDeclarationMap.put(variableName, this.getSerializedNode(variableDeclaration));
          }
        }
      }

      // if a variable contains a reference to another variable, replace the
      // reference by its declared block

      for (final String valueChangedType : variableTypes) {
        final NodeList valueChanged = document
            .getElementsByTagName("valueChanged:" + valueChangedType);
        for (int i = 0; i < valueChanged.getLength(); i++) {
          final String id = valueChanged.item(i).getAttributes().getNamedItem("id").getNodeValue();
          String valueChangedToString = this.getSerializedNode(valueChanged.item(i));
          for (final String variableType : variableTypes) {
            while (valueChangedToString.contains("variable:" + variableType + " reference")) {
              for (final String variableReferenceName : variableDeclarationMap.keySet()) {
                valueChangedToString = valueChangedToString.replaceAll(
                    "<variable:" + variableType + " reference=\"" + variableReferenceName + "\"/>",
                    variableDeclarationMap.get(variableReferenceName));
              }
            }
          }
          variableValueChangedMap.put(id, DigestUtils.sha256Hex(valueChangedToString));
        }
      }

    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }

    this.valueChangeBlockWithVariableBlock.put(policy.getPolicyId(), variableValueChangedMap);
  }

  private void updateValueChangeBlockWithVariableBlock(PolicyId newPolicyId, PolicyId oldPolicyId) {
    final Set<PolicyId> policyIds = this.valueChangeBlockWithVariableBlock.keySet();
    for (final PolicyId policyId : policyIds) {
      if (policyId.equals(oldPolicyId)) {
        final Map<String, String> blocks = this.valueChangeBlockWithVariableBlock.get(policyId);
        this.valueChangeBlockWithVariableBlock.put(newPolicyId, blocks);
        this.valueChangeBlockWithVariableBlock.remove(oldPolicyId);
      }
    }

  }

}
