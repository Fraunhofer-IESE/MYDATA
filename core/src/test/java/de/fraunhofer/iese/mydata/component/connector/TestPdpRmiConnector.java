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

package de.fraunhofer.iese.mydata.component.connector;

import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.ComponentType;
import de.fraunhofer.iese.mydata.component.health.HealthStatus;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyDecisionPoint;
import de.fraunhofer.iese.mydata.exception.ResourceUpdateException;
import de.fraunhofer.iese.mydata.policy.Policy;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.policy.decision.AuthorizationDecision;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.ConflictingPolicyException;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import java.io.IOException;
import java.net.URI;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

@Connector(protocol = {
    "rmi"
}, type = ComponentType.PDP)
public class TestPdpRmiConnector implements IPolicyDecisionPoint {

  public TestPdpRmiConnector(URI uri) {

  }

  @Override
  public HealthStatus getHealth() throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ComponentId getId() throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean reset() throws IOException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean addToBlacklist(Set<SolutionId> ids) throws IOException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean clearAllCaches(SolutionId solutionId) throws IOException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public AuthorizationDecision decisionRequest(Event event)
      throws IOException, EvaluationUndecidableException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<AuthorizationDecision> decisionRequests(List<Event> events) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean evaluate(Event event) throws IOException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean evaluate(Set<Event> events) throws IOException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Set<String> listDeployedPolicies() throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean removeFromBlacklist(Set<SolutionId> ids) throws IOException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean revokePolicy(PolicyId policyId) throws IOException, ResourceUpdateException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void setFailureMode(boolean active) throws IOException {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean deploy(Policy policy, ZoneId zoneIdOfSolution)
      throws IOException, ConflictingPolicyException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean updatePolicy(Policy policy, ZoneId zoneIdOfSolution)
      throws IOException, ResourceUpdateException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean updatePolicyAndId(Policy policyWithNewId, ZoneId zoneIdOfSolution,
      PolicyId oldPolicyId) throws IOException, ResourceUpdateException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isWhitelistModeEnabled() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isInFailureMode() {
    // TODO Auto-generated method stub
    return false;
  }

}
