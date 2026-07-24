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

import de.fraunhofer.iese.mydata.common.Hide;
import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.timer.TimerId;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Entity
@Getter
@Setter
public class SyncNotification extends MyDataEntity {

  @EmbeddedId
  @NotNull
  @Valid
  private ClientId clientId;

  @NotNull
  @Min(1)
  private Long syncTime = 0L;

  @Min(1)
  private Long nextSyncTime;

  @NotNull
  private boolean inFailureMode = false;

  @ElementCollection
  @MapKeyColumn(name = "policy_id")
  @CollectionTable(name = "deployed_policy_versions", joinColumns = @JoinColumn(name = "client_id"))
  @Column(name = "modification_time")
  @NonNull
  private Map<PolicyId, Long> deployedPolicyVersions = new HashMap<>();

  @ElementCollection
  @MapKeyColumn(name = "timer_id")
  @CollectionTable(name = "deployed_timer_versions", joinColumns = @JoinColumn(name = "client_id"))
  @Column(name = "modification_time")
  @NonNull
  private Map<TimerId, Long> deployedTimerVersions = new HashMap<>();

  @Hide
  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "client_id")
  private LibraryClient libraryClient;

  /**
   * assign a clientId to the current SynNotification
   *
   * @param clientId
   */
  public SyncNotification(ClientId clientId) {
    super();
    this.clientId = clientId;
  }

  /**
   * default constructor
   */
  public SyncNotification() {
    // Needed for JPA
  }

  /**
   * Add in the map of policyIds the current version
   *
   * @param policyId
   * @param policyVersion
   */
  public void addDeployedPolicyVersion(@NonNull PolicyId policyId, @NonNull Long policyVersion) {
    this.getDeployedPolicyVersions().put(policyId, policyVersion);
  }

  /**
   * @return the map of policyId-policyVersion
   */
  public Map<PolicyId, Long> getDeployedPolicyVersions() {
    if (null == this.deployedPolicyVersions) {
      this.deployedPolicyVersions = new HashMap<>();
    }
    return this.deployedPolicyVersions;
  }

  /**
   * Add in the map of TimerIds the current version
   *
   * @param timerId
   * @param timerVersion
   */
  public void addDeployedTimerVersion(@NonNull TimerId timerId, @NonNull Long timerVersion) {
    this.getDeployedTimerVersions().put(timerId, timerVersion);
  }

  /**
   * @return the map of timerId-policyVersion
   */
  public Map<TimerId, Long> getDeployedTimerVersions() {
    if (null == this.deployedTimerVersions) {
      this.deployedTimerVersions = new HashMap<>();
    }
    return this.deployedTimerVersions;
  }

}
