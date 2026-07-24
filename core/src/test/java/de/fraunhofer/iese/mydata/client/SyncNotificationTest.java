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

package de.fraunhofer.iese.mydata.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.timer.TimerId;

import org.junit.jupiter.api.Test;

class SyncNotificationTest {

  @Test
  void constructorTess() {
    final SyncNotification latestSyncNotification = new SyncNotification(
        new ClientId("urn:client:solution:test"));

    assertNotNull(latestSyncNotification.getDeployedPolicyVersions());
    assertNotNull(latestSyncNotification.getDeployedTimerVersions());
  }

  @Test
  void whenSetDeployedPolicyVersionIsNull_thenThrowException() {
    final SyncNotification latestSyncNotification = new SyncNotification(
          new ClientId("urn:client:solution:test"));
    assertThrows(IllegalArgumentException.class, () ->
      latestSyncNotification.setDeployedPolicyVersions(null));
  }

  @Test
  void addDeployedPolicyVersionOk() {
    final SyncNotification latestSyncNotification = new SyncNotification(
        new ClientId("urn:client:solution:test"));
    final PolicyId policyId = new PolicyId("urn:policy:solution:test");
    final Long policyVersion = 1234L;
    latestSyncNotification.addDeployedPolicyVersion(policyId, policyVersion);

    assertEquals(policyVersion,
        latestSyncNotification.getDeployedPolicyVersions().get(policyId));
  }

  @Test
  void addDeployedPolicyVersion_whenPolicyIdIsNull_thenThrowException() {
    final SyncNotification latestSyncNotification = new SyncNotification(
          new ClientId("urn:client:solution:test"));
    assertThrows(IllegalArgumentException.class, () ->
      latestSyncNotification.addDeployedPolicyVersion(null, 1234L));
  }

  @Test
  void addDeployedPolicyVersion_whenPolicyVersionIsNull_thenThrowException() {
    final SyncNotification latestSyncNotification = new SyncNotification(
          new ClientId("urn:client:solution:test"));
    PolicyId x = new PolicyId("urn:policy:solution:test");
    assertThrows(IllegalArgumentException.class, () ->
      latestSyncNotification.addDeployedPolicyVersion(x, null));
  }

  @Test
  void addDeployedTimerVersionOk() {
    final SyncNotification latestSyncNotification = new SyncNotification(
        new ClientId("urn:client:solution:test"));
    final TimerId timerId = new TimerId("urn:timer:solution:test");
    final Long timerVersion = 1234L;
    latestSyncNotification.addDeployedTimerVersion(timerId, timerVersion);

    assertEquals(timerVersion,
        latestSyncNotification.getDeployedTimerVersions().get(timerId));
  }

  @Test
  void addDeployedTimerVersion_whenTimerIdIsNull_thenThrowException() {
    final SyncNotification latestSyncNotification = new SyncNotification(
          new ClientId("urn:client:solution:test"));
    assertThrows(IllegalArgumentException.class, () ->
      latestSyncNotification.addDeployedTimerVersion(null, 1234L));
  }

  @Test
  void addDeployedTimerVersion_whenTimerVersionIsNull_thenThrowException() {
    final SyncNotification latestSyncNotification = new SyncNotification(
          new ClientId("urn:client:solution:test"));
    TimerId x = new TimerId("urn:timer:solution:test");
    assertThrows(IllegalArgumentException.class, () ->
      latestSyncNotification.addDeployedTimerVersion(x, null));
  }

}
