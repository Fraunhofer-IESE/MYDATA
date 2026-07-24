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
import de.fraunhofer.iese.mydata.component.information.PdpComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PepComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PipComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PxpComponentInformation;
import de.fraunhofer.iese.mydata.component.information.method.MethodInterfaceDescription;
import de.fraunhofer.iese.mydata.component.interfaces.IBasicManagementService;
import de.fraunhofer.iese.mydata.exception.ConflictingResourceException;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.exception.ResourceUpdateException;
import de.fraunhofer.iese.mydata.policy.Policy;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.solution.SolutionId;
import de.fraunhofer.iese.mydata.timer.Timer;
import de.fraunhofer.iese.mydata.timer.TimerId;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Connector(protocol = {
    "http", "https"
}, type = ComponentType.PMP)
public class TestPmpHttpConnector implements IBasicManagementService {

  @Override
  public HealthStatus getHealth() throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PolicyId addPolicy(Policy policy)
      throws IOException, ConflictingResourceException, ResourceUpdateException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean policyExists(PolicyId policyId) throws IOException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Policy getPolicy(PolicyId policyId) throws IOException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<Policy> getPolicies(SolutionId solutionId) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<PolicyId> listPolicies(SolutionId solutionId) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<Policy> getDeployedPolicies() throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<Policy> getDeployedPolicies(SolutionId solutionId) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<PolicyId> listDeployedPolicies(SolutionId solutionId) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isPolicyDeployed(PolicyId policyId) throws IOException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public PolicyId updatePolicy(Policy policy)
      throws IOException, ResourceUpdateException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void deployPolicy(PolicyId policyId)
      throws IOException, NoSuchEntityException, ResourceUpdateException {
    // TODO Auto-generated method stub

  }

  @Override
  public void revokePolicy(PolicyId policyId)
      throws IOException, NoSuchEntityException, ResourceUpdateException {
    // TODO Auto-generated method stub

  }

  @Override
  public void deletePolicy(PolicyId policyId)
      throws IOException, ResourceUpdateException, NoSuchEntityException {
    // TODO Auto-generated method stub

  }

  @Override
  public TimerId addTimer(Timer timer)
      throws IOException, ConflictingResourceException, ResourceUpdateException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Timer getTimer(TimerId timerId) throws IOException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<Timer> getTimers(SolutionId solutionId) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<TimerId> listTimers(SolutionId solutionId) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<Timer> getDeployedTimers(SolutionId solutionId) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<TimerId> listDeployedTimers(SolutionId solutionId) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isTimerDeployed(TimerId timerId) throws IOException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void deployTimer(TimerId timerId)
      throws IOException, NoSuchEntityException, ResourceUpdateException {
    // TODO Auto-generated method stub

  }

  @Override
  public void revokeTimer(TimerId timerId)
      throws IOException, NoSuchEntityException, ResourceUpdateException {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteTimer(TimerId timerId)
      throws IOException, ResourceUpdateException, NoSuchEntityException {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean timerExists(TimerId timerId) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public ComponentId addPdp(PdpComponentInformation component)
      throws IOException, ConflictingResourceException, ResourceUpdateException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ComponentId addPep(PepComponentInformation component)
      throws IOException, ConflictingResourceException, ResourceUpdateException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ComponentId addPip(PipComponentInformation component)
      throws IOException, ConflictingResourceException, ResourceUpdateException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ComponentId addPxp(PxpComponentInformation component)
      throws IOException, ConflictingResourceException, ResourceUpdateException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean pdpExists(ComponentId policyId) throws IOException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean pepExists(ComponentId policyId) throws IOException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean pipExists(ComponentId policyId) throws IOException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean pxpExists(ComponentId policyId) throws IOException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public PdpComponentInformation getPdp() throws IOException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PepComponentInformation getPep(ComponentId id) throws IOException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PipComponentInformation getPip(ComponentId id) throws IOException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PxpComponentInformation getPxp(ComponentId id) throws IOException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public HealthStatus getPipState(ComponentId componentId)
      throws IOException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public HealthStatus getPxpState(ComponentId componentId)
      throws IOException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<ComponentId, HealthStatus> getAllComponentStates(SolutionId solutionId)
      throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<PepComponentInformation> lookupPep(SolutionId solutionId) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<PipComponentInformation> lookupPip(SolutionId solutionId,
      MethodInterfaceDescription query) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<PxpComponentInformation> lookupPxp(SolutionId solutionId,
      MethodInterfaceDescription query) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ComponentId updatePdp(PdpComponentInformation component)
      throws IOException, NoSuchEntityException, ResourceUpdateException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ComponentId updatePep(PepComponentInformation component)
      throws IOException, NoSuchEntityException, ResourceUpdateException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ComponentId updatePip(PipComponentInformation component)
      throws IOException, NoSuchEntityException, ResourceUpdateException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ComponentId updatePxp(PxpComponentInformation component)
      throws IOException, NoSuchEntityException, ResourceUpdateException {
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
  public Set<Policy> getRevokedPolicies(SolutionId solutionId) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<PolicyId> listRevokedPolicies(SolutionId solutionId) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<Timer> getRevokedTimers(SolutionId solutionId)
      throws IOException, InvalidEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<TimerId> listRevokedTimers(SolutionId solId)
      throws IOException, InvalidEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TimerId updateTimer(Timer timer)
      throws IOException, ResourceUpdateException, NoSuchEntityException, InvalidEntityException {
    return null;
  }

  @Override
  public void deletePep(ComponentId componentId)
      throws IOException, NoSuchEntityException, ResourceUpdateException, InvalidEntityException {
    // TODO Auto-generated method stub

  }

  @Override
  public void deletePip(ComponentId componentId)
      throws IOException, NoSuchEntityException, ResourceUpdateException, InvalidEntityException {
    // TODO Auto-generated method stub

  }

  @Override
  public void deletePxp(ComponentId componentId)
      throws IOException, NoSuchEntityException, ResourceUpdateException, InvalidEntityException {
    // TODO Auto-generated method stub

  }

}
