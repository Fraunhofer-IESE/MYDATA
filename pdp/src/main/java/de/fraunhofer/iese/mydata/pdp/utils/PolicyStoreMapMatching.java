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

import de.fraunhofer.iese.mydata.pdp.interfaces.IPolicyStore;
import de.fraunhofer.iese.mydata.pdp.language.model.Policy;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyMechanism;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.policy.event.ActionId;

import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.concurrent.GuardedBy;

/**
 * This class implements a policy store which uses a two map strategy to
 * increase the matching speed for policies and events.
 * <p>
 * The basic idea is to have to maps. One map maintains the relationship
 * String-&gt;Policy. Another map maintains the relationship
 * ActionId-&gt;map(String-&gt;List(Mechanism)). The advantage of this approach
 * is, that it supports the typical use cases. If an event is received,
 * mechanisms which will match to a high probability can be retrieved quite
 * fast. Also if policies need to be checked for deployment or if policies will
 * be removed, this is quickly possible. The drawback of this approach is, that
 * during deployment, the policy must be scanned for ActionIds, which later on
 * must be integrated into the second map. This happens step by step and an
 * cause an inconsistent policy state if the store is not completely locked
 * during this process. So if someone uses the policy store, it is highly
 * recommended to use the read lock during usage (see {@link #readLockStore()}
 * and {@link #readUnlockStore()}).
 * </p>
 */
public class PolicyStoreMapMatching implements IPolicyStore {

  /**
   * The deployed policies by policy component_id.
   */
  @GuardedBy("policyMapLock")
  private final Map<PolicyId, Policy> deployedPoliciesByPolicyId;

  /**
   * The deployed policies by action component_id.
   */
  @GuardedBy("policyMapLock")
  private final Map<ActionId, Map<PolicyId, Set<PolicyMechanism>>> deployedPoliciesByActionId;

  private final Map<PolicyId, ZoneId> zoneIdsMap;

  /**
   * The policy map lock.
   */
  private final ReentrantReadWriteLock policyMapLock = new ReentrantReadWriteLock(true);

  /**
   * Instantiates a new policy store map matching.
   *
   * @param numOfInitialSpace the num of initial space
   */
  public PolicyStoreMapMatching(final int numOfInitialSpace) {
    this.deployedPoliciesByPolicyId = new HashMap<>(numOfInitialSpace);
    this.deployedPoliciesByActionId = new HashMap<>(numOfInitialSpace);
    this.zoneIdsMap = new HashMap<>(numOfInitialSpace);
  }

  /*
   * (non-Javadoc)
   * @see IPolicyStore#isEmpty()
   */
  @Override
  public boolean isEmpty() {
    final ReentrantReadWriteLock.ReadLock lock = this.policyMapLock.readLock();
    lock.lock();
    try {
      return this.deployedPoliciesByPolicyId.isEmpty();
    } finally {
      lock.unlock();
    }
  }

  /*
   * (non-Javadoc)
   * @see IPolicyStore#clear()
   */
  @Override
  public void clear() {
    final ReentrantReadWriteLock.WriteLock lock = this.policyMapLock.writeLock();
    lock.lock();
    try {
      this.deployedPoliciesByPolicyId.clear();
      this.deployedPoliciesByActionId.clear();
    } finally {
      lock.unlock();
    }
  }

  /*
   * (non-Javadoc)
   * @see IPolicyStore#storePolicy(de.
   * fraunhofer.iese.mydata.pdp.java.language.model.Policy)
   */
  @Override
  public boolean storePolicy(final Policy policy, final ZoneId zoneId) {
    if (policy == null) {
      return false;
    }

    final PolicyId idObj = policy.getPolicyId();
    // check if the policy id (urn) is null or empty
    if (idObj.getUrn() == null || idObj.getUrn().isEmpty()) {
      return false;
    }

    final ReentrantReadWriteLock.ReadLock lock = this.policyMapLock.readLock();
    boolean containsKey = true;

    lock.lock();
    try {
      containsKey = this.deployedPoliciesByPolicyId.containsKey(idObj);
    } finally {
      lock.unlock();
    }

    if (!containsKey) {
      this.addPolicyToStore(policy, zoneId);
      return true;
    }

    return false;
  }

  /**
   * Adds a policy to the internal policy store. First, it parses the mechanisms
   * for contained action ids and adds the mapping 'actionId->(String,
   * Mechanism)' to the second internal map. Second, it adds the policy as a
   * whole to the first map.
   *
   * @param policy Policy to be deployed
   */
  private void addPolicyToStore(final Policy policy, final ZoneId zoneId) {
    final Map<ActionId, HashSet<PolicyMechanism>> actionIds = this.getPolicyActionIds(policy);
    final ReentrantReadWriteLock.WriteLock lock = this.policyMapLock.writeLock();
    lock.lock();
    try {
      final Iterator<Entry<ActionId, HashSet<PolicyMechanism>>> iterator = actionIds.entrySet().iterator();

      while (iterator.hasNext()) {
        final Entry<ActionId, HashSet<PolicyMechanism>> entry = iterator.next();

        Map<PolicyId, Set<PolicyMechanism>> subMap = this.deployedPoliciesByActionId.get(entry.getKey());
        if (subMap == null) {
          subMap = new HashMap<>(1);
          this.deployedPoliciesByActionId.put(entry.getKey(), subMap);
        }

        zoneIdsMap.put(policy.getPolicyId(), zoneId);

        final Set<PolicyMechanism> sublist = subMap.get(policy.getPolicyId());
        if (sublist != null) {
          sublist.addAll(entry.getValue());
        } else {
          subMap.put(policy.getPolicyId(), entry.getValue());
        }
      }

      this.deployedPoliciesByPolicyId.put(policy.getPolicyId(), policy);
    } finally {
      lock.unlock();
    }
  }

  /*
   * (non-Javadoc)
   * @see IPolicyStore#removePolicy(de
   * .fraunhofer.iese.mydata.internal.policy.identifier.String)
   */
  @Override
  public boolean removePolicy(final PolicyId policyId) {
    if (policyId == null) {
      return false;
    }
    final ReentrantReadWriteLock.WriteLock lock = this.policyMapLock.writeLock();
    lock.lock();
    try {
      final Policy policy = this.deployedPoliciesByPolicyId.get(policyId);
      if (policy == null) {
        return false;
      }
      final Map<ActionId, HashSet<PolicyMechanism>> actionIds = this.getPolicyActionIds(policy);
      final Iterator<Entry<ActionId, HashSet<PolicyMechanism>>> iterator = actionIds.entrySet().iterator();

      while (iterator.hasNext()) {
        final Entry<ActionId, HashSet<PolicyMechanism>> entry = iterator.next();
        final Map<PolicyId, Set<PolicyMechanism>> subMap = this.deployedPoliciesByActionId.get(entry.getKey());
        if (subMap != null) {
          subMap.remove(policyId);
        }
      }
      zoneIdsMap.remove(policyId);
      this.deployedPoliciesByPolicyId.remove(policyId);

      return true;
    } finally {
      lock.unlock();
    }
  }

  /*
   * (non-Javadoc)
   * @see IPolicyStore#changePolicyId(de
   * .fraunhofer.iese.mydata.internal.policy.identifier.String)
   */
  @Override
  public boolean changePolicyId(final PolicyId oldPolicyId, PolicyId newPolicyId) {
    if (oldPolicyId == null || newPolicyId == null) {
      return false;
    }
    final ReentrantReadWriteLock.WriteLock lock = this.policyMapLock.writeLock();
    lock.lock();
    try {
      final Policy policy = this.deployedPoliciesByPolicyId.get(oldPolicyId);
      if (policy == null) {
        return false;
      }
      final Map<ActionId, HashSet<PolicyMechanism>> actionIds = this.getPolicyActionIds(policy);
      final Iterator<Entry<ActionId, HashSet<PolicyMechanism>>> iterator = actionIds.entrySet().iterator();

      while (iterator.hasNext()) {
        final Entry<ActionId, HashSet<PolicyMechanism>> entry = iterator.next();
        final Map<PolicyId, Set<PolicyMechanism>> subMap = this.deployedPoliciesByActionId.get(entry.getKey());
        if (subMap != null) {
          final Set<PolicyMechanism> content = subMap.get(oldPolicyId);
          subMap.remove(oldPolicyId);
          subMap.put(newPolicyId, content);
        }
      }
      final ZoneId content = zoneIdsMap.get(oldPolicyId);
      zoneIdsMap.remove(oldPolicyId);
      zoneIdsMap.put(newPolicyId, content);

      this.deployedPoliciesByPolicyId.remove(oldPolicyId);
      // change the id in the policy object
      policy.setId(newPolicyId.toString());
      this.deployedPoliciesByPolicyId.put(newPolicyId, policy);

      return true;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Creates a Map which contains all mappings of (actionId->Mechanisms) of the
   * policy given as parameter.
   *
   * @param policy The policy for which the mapping (actionId->Mechanism) should
   *          be created.
   * @return A map that contains all actionIds of a policy as key and the
   *         corresponding mechanisms as list as value
   */
  private Map<ActionId, HashSet<PolicyMechanism>> getPolicyActionIds(final Policy policy) {
    final Map<ActionId, HashSet<PolicyMechanism>> actionIdsAndMechs = new HashMap<>(10);
    for (final PolicyMechanism mech : policy.getMechanisms()) {
      HashSet<PolicyMechanism> mechanismList = actionIdsAndMechs.get(mech.getEvent());
      if (mechanismList == null) {
        mechanismList = new HashSet<PolicyMechanism>();
        actionIdsAndMechs.put(mech.getEvent(), mechanismList);
      }
      mechanismList.add(mech);
    }
    return actionIdsAndMechs;
  }

  /*
   * (non-Javadoc)
   * @see IPolicyStore#
   * getPolicyByPolicyId(de.fraunhofer.iese.mydata.api.policy.identifier.
   * String)
   */
  @Override
  public Policy getPolicyByPolicyId(PolicyId policyId) {
    if (null == policyId) {
      return null;
    }
    final ReentrantReadWriteLock.ReadLock lock = this.policyMapLock.readLock();
    lock.lock();
    try {
      return this.deployedPoliciesByPolicyId.get(policyId);
    } finally {
      lock.unlock();
    }
  }

  /*
   * (non-Javadoc)
   * @see IPolicyStore#
   * getPolicyByPolicyId(de.fraunhofer.iese.mydata.api.policy.identifier.
   * String)
   */
  @Override
  public ZoneId getZoneidByPolicyId(PolicyId policyId) {
    if (null == policyId) {
      return null;
    }
    return this.zoneIdsMap.get(policyId);
  }

  /*
   * (non-Javadoc)
   * @see IPolicyStore#
   * checkIfPolicyIsAlreadyDeployed(de.fraunhofer.iese.mydata.api.policy.
   * identifier.String)
   */
  @Override
  public boolean checkIfPolicyIsAlreadyDeployed(PolicyId policyId) {
    if (null == policyId) {
      return false;
    }
    final ReentrantReadWriteLock.ReadLock lock = this.policyMapLock.readLock();
    lock.lock();
    try {
      return this.deployedPoliciesByPolicyId.containsKey(policyId);
    } finally {
      lock.unlock();
    }
  }

  /*
   * (non-Javadoc)
   * @see IPolicyStore# getDeployedPolicyIds()
   */
  @Override
  public Set<String> getDeployedPolicyIds() {
    final ReentrantReadWriteLock.ReadLock lock = this.policyMapLock.readLock();
    Set<Entry<PolicyId, Policy>> pIds = Collections.emptySet();
    lock.lock();
    try {
      pIds = this.deployedPoliciesByPolicyId.entrySet();

    } finally {
      lock.unlock();
    }

    final Set<String> deployedPolicyIds = new HashSet<String>();
    for (final Entry<PolicyId, Policy> entry : pIds) {
      deployedPolicyIds.add(entry.getKey().getUrn());
    }
    return deployedPolicyIds;
  }

  /*
   * (non-Javadoc)
   * @see IPolicyStore#
   * getMatchingMechanisms(de.fraunhofer.iese.mydata.api.policy.identifier.
   * ActionId)
   */
  @Override
  public Set<Entry<PolicyId, Set<PolicyMechanism>>> getMatchingMechanisms(final ActionId actionId) {
    if (actionId == null) {
      return Collections.emptySet();
    }
    final ReentrantReadWriteLock.ReadLock lock = this.policyMapLock.readLock();
    lock.lock();
    try {
      final Map<PolicyId, Set<PolicyMechanism>> matchingPolicies = this.deployedPoliciesByActionId.get(actionId);
      if (matchingPolicies != null) {
        return matchingPolicies.entrySet();
      } else {
        return Collections.emptySet();
      }
    } finally {
      lock.unlock();
    }
  }

  /*
   * (non-Javadoc)
   * @see IPolicyStore#readLockStore()
   */
  @Override
  public void readLockStore() {
    this.policyMapLock.readLock().lock();
  }

  /*
   * (non-Javadoc)
   * @see IPolicyStore#readUnlockStore ()
   */
  @Override
  public void readUnlockStore() {
    this.policyMapLock.readLock().unlock();
  }
}
