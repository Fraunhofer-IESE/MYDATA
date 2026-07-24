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

package de.fraunhofer.iese.mydata.client.dto;

import de.fraunhofer.iese.mydata.client.ClientId;
import de.fraunhofer.iese.mydata.client.SyncNotification;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.timer.TimerId;

import lombok.Data;

import java.util.Map;

@Data
public class SyncNotificationDTO {

  private ClientId clientId;

  private Long syncTime;

  private Long nextSyncTime;

  private boolean inFailureMode;

  private Map<PolicyId, Long> deployedPolicyVersions;

  private Map<TimerId, Long> deployedTimerVersions;

  // private ClientId libraryClient_clientId;

  SyncNotificationDTO(SyncNotification syncNotification) {
    this.clientId = syncNotification.getClientId();
    this.syncTime = syncNotification.getSyncTime();
    this.nextSyncTime = syncNotification.getNextSyncTime();
    this.inFailureMode = syncNotification.isInFailureMode();
    this.deployedPolicyVersions = syncNotification.getDeployedPolicyVersions();
    this.deployedTimerVersions = syncNotification.getDeployedTimerVersions();
    // this.libraryClient_clientId = syncNotification.getLibraryClient().getClientId(); // FIXME this throws a null pointer exception
  }

}
