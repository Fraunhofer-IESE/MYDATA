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

package de.fraunhofer.iese.mydata.connector.java;

import de.fraunhofer.iese.mydata.IMyDataEnvironment;
import de.fraunhofer.iese.mydata.MyDataEnvironmentManager;
import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.ComponentType;
import de.fraunhofer.iese.mydata.component.connector.Authentication;
import de.fraunhofer.iese.mydata.component.connector.Connector;
import de.fraunhofer.iese.mydata.component.health.HealthStatus;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyDecisionPoint;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.exception.ResourceUpdateException;
import de.fraunhofer.iese.mydata.internal.TechnicalAccessGranter;
import de.fraunhofer.iese.mydata.policy.Policy;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.policy.decision.AuthorizationDecision;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.ConflictingPolicyException;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.jspecify.annotations.Nullable;

@Connector(protocol = {
    "java"
}, type = ComponentType.PDP)
public class PdpJavaConnector implements IPolicyDecisionPoint {

  private static final Type listOfEventType = new TypeToken<List<Event>>() {
  }.getType();

  private static final Type setOfEventType = new TypeToken<Set<Event>>() {
  }.getType();

  private final IPolicyDecisionPoint pdp;

  public PdpJavaConnector(IPolicyDecisionPoint pdp) {
    this.pdp = pdp;
  }

  public PdpJavaConnector(URI uri) {
    // java://de.fraunhofer.iese.mydata.MyDataEnvironmentManager/{envId}/local-component/{componentId}
    final String environmentType = uri.getAuthority();
    if (!MyDataEnvironmentManager.class.getCanonicalName().equals(environmentType)) {
      throw new RuntimeException("Unsupported EnvironmentType " + environmentType);
    }
    final String[] pathSplit = uri.getPath().split("/");
    if (pathSplit.length != 4) {
      throw new IllegalArgumentException("Malformed URI");
    }
    final String environmentId = pathSplit[1];
    final String solution = pathSplit[2];
    final ComponentId componentId = new ComponentId(pathSplit[3]);
    final Optional<IMyDataEnvironment> myDataEnvironmentOptional = MyDataEnvironmentManager
        .getEnvironment(environmentId);
    if (!myDataEnvironmentOptional.isPresent()) {
      throw new RuntimeException("IMyDataEnvironment with id " + environmentId + " not available.");
    }
    final IMyDataEnvironment myDataEnvironment = myDataEnvironmentOptional.get();
    final Optional<IPolicyDecisionPoint> pdpOptional = TechnicalAccessGranter
        .getTechnicalAccess(myDataEnvironment).getPdp();
    if (pdpOptional.isPresent()) {
      this.pdp = pdpOptional.get();
    } else {
      this.pdp = null;
      throw new RuntimeException(
          "Cannot establish connection to the PDP as the reference in the environment is null");
    }
  }

  public PdpJavaConnector(URI uri, Authentication auth) {
    this(uri);
  }

  @Override
  public ComponentId getId() throws IOException {
    return this.pdp.getId();
  }

  @Override
  public boolean reset() throws IOException, NoSuchEntityException {
    return this.pdp.reset();
  }

  @Override
  public HealthStatus getHealth() throws IOException {
    return this.pdp.getHealth();
  }

  @Override
  public boolean clearAllCaches(@Nullable
  SolutionId solutionId) throws IOException {
    return this.pdp.clearAllCaches(solutionId);
  }

  @Override
  public AuthorizationDecision decisionRequest(final Event event)
      throws IOException, EvaluationUndecidableException {
    // cloned because PEPs event should not be shared with PDP (see IND2UCE-2931 and IND2UCE-2250)
    final Event eventToSubmit = MyDataEntity.getGson()
        .fromJson(MyDataEntity.getGson().toJson(event), Event.class);
    return this.pdp.decisionRequest(eventToSubmit);
  }

  @Override
  public Set<String> listDeployedPolicies() throws IOException {
    return this.pdp.listDeployedPolicies();
  }

  @Override
  public boolean addToBlacklist(Set<SolutionId> ids) throws IOException {
    return this.pdp.addToBlacklist(ids);
  }

  @Override
  public List<AuthorizationDecision> decisionRequests(final List<Event> events) throws IOException {
    final List<Event> eventsToSubmit = MyDataEntity.getGson()
        .fromJson(MyDataEntity.getGson().toJson(events), listOfEventType);
    return this.pdp.decisionRequests(eventsToSubmit);
  }

  @Override
  public boolean deploy(Policy policy, ZoneId zoneIdOfSolution)
      throws IOException, ConflictingPolicyException {
    return this.pdp.deploy(policy, zoneIdOfSolution);
  }

  @Override
  public boolean evaluate(Event event) throws IOException {
    final Event eventToSubmit = MyDataEntity.getGson()
        .fromJson(MyDataEntity.getGson().toJson(event), Event.class);
    return this.pdp.evaluate(eventToSubmit);
  }

  @Override
  public boolean evaluate(Set<Event> events) throws IOException {
    final Set<Event> eventsToSubmit = MyDataEntity.getGson()
        .fromJson(MyDataEntity.getGson().toJson(events), setOfEventType);
    return this.pdp.evaluate(eventsToSubmit);
  }

  @Override
  public boolean removeFromBlacklist(Set<SolutionId> ids) throws IOException {
    return this.pdp.removeFromBlacklist(ids);
  }

  @Override
  public boolean revokePolicy(PolicyId policyId) throws IOException, ResourceUpdateException {
    return this.pdp.revokePolicy(policyId);
  }

  @Override
  public void setFailureMode(boolean active) throws IOException {
    this.pdp.setFailureMode(active);
  }

  @Override
  public boolean updatePolicy(Policy policy, ZoneId zoneIdOfSolution)
      throws IOException, ResourceUpdateException {
    return this.pdp.updatePolicy(policy, zoneIdOfSolution);
  }

  @Override
  public boolean updatePolicyAndId(Policy policyWithNewId, ZoneId zoneId, PolicyId oldPolicyId)
      throws IOException, ResourceUpdateException {
    return this.pdp.updatePolicyAndId(policyWithNewId, zoneId, oldPolicyId);
  }

  @Override
  public boolean isWhitelistModeEnabled() {
    return this.pdp.isWhitelistModeEnabled();
  }

  @Override
  public boolean isInFailureMode() {
    return this.pdp.isInFailureMode();
  }

}
