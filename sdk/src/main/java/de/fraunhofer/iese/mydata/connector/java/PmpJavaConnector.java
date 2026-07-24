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
import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.ComponentType;
import de.fraunhofer.iese.mydata.component.connector.Authentication;
import de.fraunhofer.iese.mydata.component.connector.Connector;
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
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Connector(protocol = {
    "java"
}, type = ComponentType.PMP)
public class PmpJavaConnector implements IBasicManagementService {

  private final IBasicManagementService basicManagementService;

  public PmpJavaConnector(URI uri) {
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
    this.basicManagementService = myDataEnvironment.getPmp();
  }

  public PmpJavaConnector(URI uri, Authentication auth) {
    this(uri);
  }

  @Override
  public HealthStatus getHealth() throws IOException {
    return this.basicManagementService.getHealth();
  }

  @Override
  public ComponentId getId() throws IOException {
    return this.basicManagementService.getId();
  }

  @Override
  public boolean reset() throws IOException, NoSuchEntityException {
    return this.basicManagementService.reset();
  }

  @Override
  public PolicyId addPolicy(Policy policy) throws IOException, ConflictingResourceException,
      ResourceUpdateException, InvalidEntityException, NoSuchEntityException {
    return this.basicManagementService.addPolicy(policy);
  }

  @Override
  public boolean policyExists(PolicyId policyId) throws IOException, InvalidEntityException {
    return this.basicManagementService.policyExists(policyId);
  }

  @Override
  public Policy getPolicy(PolicyId policyId)
      throws IOException, NoSuchEntityException, InvalidEntityException {
    return this.basicManagementService.getPolicy(policyId);
  }

  @Override
  public Set<Policy> getPolicies(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException {
    return this.basicManagementService.getPolicies(solutionId);
  }

  @Override
  public Set<PolicyId> listPolicies(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException {
    return this.basicManagementService.listPolicies(solutionId);
  }

  @Override
  public Set<Policy> getDeployedPolicies() throws IOException {
    return this.basicManagementService.getDeployedPolicies();
  }

  @Override
  public Set<Policy> getDeployedPolicies(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException {
    return this.basicManagementService.getDeployedPolicies(solutionId);
  }

  @Override
  public Set<PolicyId> listDeployedPolicies(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException {
    return this.basicManagementService.listDeployedPolicies(solutionId);
  }

  @Override
  public boolean isPolicyDeployed(PolicyId policyId)
      throws IOException, NoSuchEntityException, InvalidEntityException {
    return this.basicManagementService.isPolicyDeployed(policyId);
  }

  @Override
  public PolicyId updatePolicy(Policy policy)
      throws IOException, ResourceUpdateException, NoSuchEntityException, InvalidEntityException {
    return this.basicManagementService.updatePolicy(policy);
  }

  @Override
  public void deployPolicy(PolicyId policyId)
      throws IOException, NoSuchEntityException, ResourceUpdateException, InvalidEntityException {
    this.basicManagementService.deployPolicy(policyId);
  }

  @Override
  public void revokePolicy(PolicyId policyId)
      throws IOException, NoSuchEntityException, ResourceUpdateException, InvalidEntityException {
    this.basicManagementService.revokePolicy(policyId);
  }

  @Override
  public void deletePolicy(PolicyId policyId) throws IOException, ResourceUpdateException,
      NoSuchEntityException, InvalidEntityException, ConflictingResourceException {
    this.basicManagementService.deletePolicy(policyId);
  }

  @Override
  public TimerId addTimer(Timer timer) throws IOException, ConflictingResourceException,
      ResourceUpdateException, InvalidEntityException, NoSuchEntityException {
    return this.basicManagementService.addTimer(timer);
  }

  @Override
  public Timer getTimer(TimerId timerId)
      throws IOException, NoSuchEntityException, InvalidEntityException {
    return this.basicManagementService.getTimer(timerId);
  }

  @Override
  public Set<Timer> getTimers(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException {
    return this.basicManagementService.getTimers(solutionId);
  }

  @Override
  public Set<TimerId> listTimers(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException {
    return this.basicManagementService.listTimers(solutionId);
  }

  @Override
  public Set<Timer> getDeployedTimers(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException {
    return this.basicManagementService.getDeployedTimers(solutionId);
  }

  @Override
  public Set<TimerId> listDeployedTimers(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException {
    return this.basicManagementService.listDeployedTimers(solutionId);
  }

  @Override
  public boolean isTimerDeployed(TimerId timerId)
      throws IOException, NoSuchEntityException, InvalidEntityException {
    return this.basicManagementService.isTimerDeployed(timerId);
  }

  @Override
  public void deployTimer(TimerId timerId)
      throws IOException, NoSuchEntityException, ResourceUpdateException, InvalidEntityException {
    this.basicManagementService.deployTimer(timerId);
  }

  @Override
  public void revokeTimer(TimerId timerId)
      throws IOException, NoSuchEntityException, ResourceUpdateException, InvalidEntityException {
    this.basicManagementService.revokeTimer(timerId);
  }

  @Override
  public void deleteTimer(TimerId timerId) throws IOException, ResourceUpdateException,
      NoSuchEntityException, InvalidEntityException, ConflictingResourceException {
    this.basicManagementService.deleteTimer(timerId);
  }

  @Override
  public boolean timerExists(TimerId timerId) throws IOException, InvalidEntityException {
    return this.basicManagementService.timerExists(timerId);
  }

  @Override
  public ComponentId addPdp(PdpComponentInformation component)
      throws IOException, ConflictingResourceException, ResourceUpdateException,
      InvalidEntityException, NoSuchEntityException {
    return this.basicManagementService.addPdp(component);
  }

  @Override
  public ComponentId addPep(PepComponentInformation component)
      throws IOException, ConflictingResourceException, ResourceUpdateException,
      InvalidEntityException, NoSuchEntityException {
    return this.basicManagementService.addPep(component);
  }

  @Override
  public ComponentId addPip(PipComponentInformation component)
      throws IOException, ConflictingResourceException, ResourceUpdateException,
      InvalidEntityException, NoSuchEntityException {
    return this.basicManagementService.addPip(component);
  }

  @Override
  public ComponentId addPxp(PxpComponentInformation component)
      throws IOException, ConflictingResourceException, ResourceUpdateException,
      InvalidEntityException, NoSuchEntityException {
    return this.basicManagementService.addPxp(component);
  }

  @Override
  public boolean pdpExists(ComponentId componentId) throws IOException, InvalidEntityException {
    return this.basicManagementService.pdpExists(componentId);
  }

  @Override
  public boolean pepExists(ComponentId componentId) throws IOException, InvalidEntityException {
    return this.basicManagementService.pepExists(componentId);
  }

  @Override
  public boolean pipExists(ComponentId componentId) throws IOException, InvalidEntityException {
    return this.basicManagementService.pipExists(componentId);
  }

  @Override
  public boolean pxpExists(ComponentId componentId) throws IOException, InvalidEntityException {
    return this.basicManagementService.pxpExists(componentId);
  }

  @Override
  public PdpComponentInformation getPdp() throws IOException, NoSuchEntityException {
    return this.basicManagementService.getPdp();
  }

  @Override
  public PepComponentInformation getPep(ComponentId id)
      throws IOException, NoSuchEntityException, InvalidEntityException {
    return this.basicManagementService.getPep(id);
  }

  @Override
  public PipComponentInformation getPip(ComponentId id)
      throws IOException, NoSuchEntityException, InvalidEntityException {
    return this.basicManagementService.getPip(id);
  }

  @Override
  public PxpComponentInformation getPxp(ComponentId id)
      throws IOException, NoSuchEntityException, InvalidEntityException {
    return this.basicManagementService.getPxp(id);
  }

  @Override
  public HealthStatus getPipState(ComponentId componentId)
      throws IOException, NoSuchEntityException, InvalidEntityException {
    return this.basicManagementService.getPipState(componentId);
  }

  @Override
  public HealthStatus getPxpState(ComponentId componentId)
      throws IOException, NoSuchEntityException, InvalidEntityException {
    return this.basicManagementService.getPxpState(componentId);
  }

  @Override
  public Map<ComponentId, HealthStatus> getAllComponentStates(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException {
    return this.basicManagementService.getAllComponentStates(solutionId);
  }

  @Override
  public Set<PepComponentInformation> lookupPep(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException {
    return this.basicManagementService.lookupPep(solutionId);
  }

  @Override
  public Set<PipComponentInformation> lookupPip(SolutionId solutionId,
      MethodInterfaceDescription query)
      throws IOException, InvalidEntityException, NoSuchEntityException {
    return this.basicManagementService.lookupPip(solutionId, query);
  }

  @Override
  public Set<PxpComponentInformation> lookupPxp(SolutionId solutionId,
      MethodInterfaceDescription query)
      throws IOException, InvalidEntityException, NoSuchEntityException {
    return this.basicManagementService.lookupPxp(solutionId, query);
  }

  @Override
  public ComponentId updatePdp(PdpComponentInformation component)
      throws IOException, NoSuchEntityException, ResourceUpdateException, InvalidEntityException {
    return this.basicManagementService.updatePdp(component);
  }

  @Override
  public ComponentId updatePep(PepComponentInformation component)
      throws IOException, NoSuchEntityException, ResourceUpdateException, InvalidEntityException {
    return this.basicManagementService.updatePep(component);
  }

  @Override
  public ComponentId updatePip(PipComponentInformation component)
      throws IOException, NoSuchEntityException, ResourceUpdateException, InvalidEntityException {
    return this.basicManagementService.updatePip(component);
  }

  @Override
  public ComponentId updatePxp(PxpComponentInformation component)
      throws IOException, NoSuchEntityException, ResourceUpdateException, InvalidEntityException {
    return this.basicManagementService.updatePxp(component);
  }

  @Override
  public void deletePep(ComponentId componentId)
      throws IOException, NoSuchEntityException, ResourceUpdateException, InvalidEntityException {
    this.basicManagementService.deletePep(componentId);
  }

  @Override
  public void deletePip(ComponentId componentId)
      throws IOException, NoSuchEntityException, ResourceUpdateException, InvalidEntityException {
    this.basicManagementService.deletePip(componentId);
  }

  @Override
  public void deletePxp(ComponentId componentId)
      throws IOException, NoSuchEntityException, ResourceUpdateException, InvalidEntityException {
    this.basicManagementService.deletePxp(componentId);
  }

  @Override
  public Set<Policy> getRevokedPolicies(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException {
    return this.basicManagementService.getRevokedPolicies(solutionId);
  }

  @Override
  public Set<PolicyId> listRevokedPolicies(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException {
    return this.basicManagementService.listRevokedPolicies(solutionId);
  }

  @Override
  public Set<Timer> getRevokedTimers(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException {
    return this.basicManagementService.getRevokedTimers(solutionId);
  }

  @Override
  public Set<TimerId> listRevokedTimers(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException {
    return this.basicManagementService.listRevokedTimers(solutionId);
  }

  @Override
  public TimerId updateTimer(Timer timer) throws IOException, ResourceUpdateException,
      NoSuchEntityException, InvalidEntityException, ConflictingResourceException {
    return this.basicManagementService.updateTimer(timer);
  }

}
