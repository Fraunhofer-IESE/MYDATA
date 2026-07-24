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

package de.fraunhofer.iese.mydata.pmp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.connector.ConnectorFactory;
import de.fraunhofer.iese.mydata.component.health.HealthStatus;
import de.fraunhofer.iese.mydata.component.health.Status;
import de.fraunhofer.iese.mydata.component.information.PdpComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PepComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PipComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PxpComponentInformation;
import de.fraunhofer.iese.mydata.component.information.method.MethodInterfaceDescription;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyDecisionPoint;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyExecutionPoint;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyInformationPoint;
import de.fraunhofer.iese.mydata.exception.ConflictingResourceException;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.exception.ResourceUpdateException;
import de.fraunhofer.iese.mydata.policy.Policy;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.policy.exception.ConflictingPolicyException;
import de.fraunhofer.iese.mydata.solution.SolutionId;
import de.fraunhofer.iese.mydata.timer.Timer;
import de.fraunhofer.iese.mydata.timer.TimerId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import java.io.IOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

class PolicyManagementPointTest {

  private static final ComponentId PDP_COMPONENT_ID = new ComponentId(
      "urn:component:test:pdp:demo");

  private static final ComponentId PEP_COMPONENT_ID = new ComponentId(
      "urn:component:test:pep:demo");

  private static final ComponentId PIP_COMPONENT_ID = new ComponentId(
      "urn:component:test:pip:demo");

  private static final ComponentId PXP_COMPONENT_ID = new ComponentId(
      "urn:component:test:pxp:demo");

  private static final SolutionId SOLUTION_ID = new SolutionId("urn:solution:test");

  private final String policyContent = """
      <policy id='urn:policy:test:test-policy' xmlns='http://www.mydata-control.de/4.0/mydataLanguage' xmlns:tns='http://www.mydata-control.de/4.0/mydataLanguage' xmlns:parameter='http://www.mydata-control.de/4.0/parameter' xmlns:pip='http://www.mydata-control.de/4.0/pip' xmlns:function='http://www.mydata-control.de/4.0/function' xmlns:event='http://www.mydata-control.de/4.0/event' xmlns:constant='http://www.mydata-control.de/4.0/constant' xmlns:variable='http://www.mydata-control.de/4.0/variable' xmlns:variableDeclaration='http://www.mydata-control.de/4.0/variableDeclaration' xmlns:valueChanged='http://www.mydata-control.de/4.0/valueChanged' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>
        <mechanism event='urn:action:test:test-event'>
          <if>
            <constant:true/>
            <then>
              <allow/>
            </then>
          </if>
        </mechanism>
      </policy>
      """;

  private final String policyContent2 = """
      <policy id='urn:policy:test:test-policy2' xmlns='http://www.mydata-control.de/4.0/mydataLanguage' xmlns:tns='http://www.mydata-control.de/4.0/mydataLanguage' xmlns:parameter='http://www.mydata-control.de/4.0/parameter' xmlns:pip='http://www.mydata-control.de/4.0/pip' xmlns:function='http://www.mydata-control.de/4.0/function' xmlns:event='http://www.mydata-control.de/4.0/event' xmlns:constant='http://www.mydata-control.de/4.0/constant' xmlns:variable='http://www.mydata-control.de/4.0/variable' xmlns:variableDeclaration='http://www.mydata-control.de/4.0/variableDeclaration' xmlns:valueChanged='http://www.mydata-control.de/4.0/valueChanged' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>
        <mechanism event='urn:action:test:test-event'>
          <if>
            <constant:true/>
            <then>
              <allow/>
            </then>
          </if>
        </mechanism>
      </policy>
      """;

  final String timerContent = """
      <timer  xmlns='http://www.mydata-control.de/4.0/mydataLanguageTimer'
       xmlns:tns='http://www.mydata-control.de/4.0/mydataLanguageTimer'
       xmlns:parameter='http://www.mydata-control.de/4.0/parameter'
       xmlns:pip='http://www.mydata-control.de/4.0/pip'
       xmlns:event='http://www.mydata-control.de/4.0/event'
       xmlns:constant='http://www.mydata-control.de/4.0/constant'
       xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'
       cron="0/55 * * * * ?" id="urn:timer:test:test-timer" description="Its my description.">

          <event action="urn:action:test:writeData">
              <parameter:string name="blubb" value="xc"/>
              <parameter:number name="blubb" value="1"/>
              <parameter:boolean name="blubb" value="true"/>
              <parameter:object name="blubb" value="xc"/>
          </event>

          <event action="urn:action:test:writeData">
              <parameter:string name="blubb" value="xc"/>
              <parameter:number name="blubb" value="1"/>
              <parameter:boolean name="blubb" value="false"/>
              <parameter:object name="blubb" value="xc"/>
          </event>

      </timer>
      """;

  private PolicyManagementPoint pmp;

  private IPolicyDecisionPoint pdp;

  private Scheduler scheduler;

  private ConnectorFactory connectorFactory;

  @BeforeEach
  void setup() throws Exception {
    final PdpComponentInformation pdpComponentInformation = Mockito
        .mock(PdpComponentInformation.class);
    Mockito.when(pdpComponentInformation.getComponentId()).thenReturn(PDP_COMPONENT_ID);
    this.pdp = Mockito.mock(IPolicyDecisionPoint.class);
    Mockito.when(this.pdp.getId()).thenReturn(PDP_COMPONENT_ID);
    this.scheduler = Mockito.mock(Scheduler.class);
    this.connectorFactory = Mockito.mock(ConnectorFactory.class);
    this.pmp = new PolicyManagementPoint(new ComponentId("urn:component:test:pmp:pmp"),
        pdpComponentInformation, this.pdp, ZoneId.of("Europe/Berlin"), this.scheduler,
        this.connectorFactory);
  }

  @Test
  void newPmpWithInvalidEntityException() throws IOException {

    final PdpComponentInformation pdpComponentInformation = Mockito
        .mock(PdpComponentInformation.class);
    Mockito.when(pdpComponentInformation.getComponentId()).thenReturn(PDP_COMPONENT_ID);
    this.pdp = Mockito.mock(IPolicyDecisionPoint.class);
    Mockito.when(this.pdp.getId()).thenReturn(new ComponentId("urn:component:test:pdp:pdp2"));
    this.scheduler = Mockito.mock(Scheduler.class);
    this.connectorFactory = Mockito.mock(ConnectorFactory.class);

    assertThrows(InvalidEntityException.class, () -> {
      this.pmp = new PolicyManagementPoint(new ComponentId("urn:component:test:pmp:pmp"),
          pdpComponentInformation, this.pdp, ZoneId.of("Europe/Berlin"), this.scheduler,
          this.connectorFactory);
    });
  }

  @Test // just for Code Coverage -> no Exception
  void newPmpInternalIOException() throws InvalidEntityException, IOException {

    final PdpComponentInformation pdpComponentInformation = Mockito
        .mock(PdpComponentInformation.class);
    Mockito.when(pdpComponentInformation.getComponentId()).thenReturn(PDP_COMPONENT_ID);
    this.pdp = Mockito.mock(IPolicyDecisionPoint.class);
    Mockito.when(this.pdp.getId()).thenThrow(IOException.class);
    this.scheduler = Mockito.mock(Scheduler.class);
    this.connectorFactory = Mockito.mock(ConnectorFactory.class);

    this.pmp = new PolicyManagementPoint(new ComponentId("urn:component:test:pmp:pmp"),
        pdpComponentInformation, this.pdp, ZoneId.of("Europe/Berlin"), this.scheduler,
        this.connectorFactory);
  }

  @Test
  void newPmpInternalRuntimeExceptionCausedByScheduler()
      throws InvalidEntityException, IOException, SchedulerException {

    final PdpComponentInformation pdpComponentInformation = Mockito
        .mock(PdpComponentInformation.class);
    Mockito.when(pdpComponentInformation.getComponentId()).thenReturn(PDP_COMPONENT_ID);
    this.pdp = Mockito.mock(IPolicyDecisionPoint.class);
    Mockito.when(this.pdp.getId()).thenReturn(PDP_COMPONENT_ID);
    this.scheduler = Mockito.mock(Scheduler.class);
    this.connectorFactory = Mockito.mock(ConnectorFactory.class);

    Mockito.when(this.scheduler.isStarted()).thenReturn(false);
    doThrow(SchedulerException.class).when(this.scheduler).start();

    assertThrows(RuntimeException.class,
        () -> new PolicyManagementPoint(new ComponentId("urn:component:test:pmp:pmp"),
            pdpComponentInformation, this.pdp, ZoneId.of("Europe/Berlin"), this.scheduler,
            this.connectorFactory));
  }

  @Test
  void testDeployTimer_revokeTimer_schedulerIsCalled() throws Exception {
    final TimerId timerId = new TimerId("urn:timer:test:test-timer");
    final Timer t = new Timer(this.timerContent);
    final TimerId resultId = this.pmp.addTimer(t);
    assertEquals(timerId.getUrn(), resultId.getUrn());
    this.pmp.deployTimer(timerId);
    Mockito.verify(this.scheduler).scheduleJob(ArgumentMatchers.any(), ArgumentMatchers.any());
    boolean isDeployed = this.pmp.isTimerDeployed(timerId);
    assertTrue(isDeployed);
    this.pmp.revokeTimer(timerId);
    Mockito.verify(this.scheduler).deleteJob(ArgumentMatchers.any());
    isDeployed = this.pmp.isTimerDeployed(timerId);
    assertFalse(isDeployed);
    this.pmp.deleteTimer(timerId);
  }

  @Test
  void deployTimerFailTest()
      throws ResourceUpdateException, InvalidEntityException, NoSuchEntityException {
    // create and add Timer
    final TimerId timerId = new TimerId("urn:timer:test:test-timer");
    // mock pdp deploy
    assertThrows(NoSuchEntityException.class, () -> this.pmp.deployTimer(timerId));
  }

  @Test
  void deploymenStatusTimerFailTest() throws NoSuchEntityException, InvalidEntityException {
    // create and add Timer
    final TimerId timerId = new TimerId("urn:timer:test:test-timer");

    final boolean result = this.pmp.isTimerDeployed(timerId);
    assertFalse(result);
  }

  @Test
  void revokeTimerFailTest()
      throws NoSuchEntityException, ResourceUpdateException, InvalidEntityException {
    // create and add Timer
    final TimerId timerId = new TimerId("urn:timer:test:test-timer");
    assertThrows(NoSuchEntityException.class, () -> this.pmp.revokeTimer(timerId));
  }

  @Test
  void deleteTimerFailTest()
      throws NoSuchEntityException, ResourceUpdateException, InvalidEntityException {
    // create and add Timer
    final TimerId timerId = new TimerId("urn:timer:test:test-timer");
    assertThrows(NoSuchEntityException.class, () -> this.pmp.deleteTimer(timerId));
  }

  @Test
  void deployDeleteWithoutRevokeAndStatusTimerTest() throws InvalidEntityException,
      ConflictingResourceException, NoSuchEntityException, ResourceUpdateException {
    // create and add Timer
    boolean catchedResourceUpdateException = false;
    final Timer timer = new Timer(this.timerContent);
    final TimerId timerId = this.pmp.addTimer(timer);
    this.pmp.deployTimer(timerId);
    boolean result = this.pmp.isTimerDeployed(timerId);
    assertEquals(true, result);
    try {
      this.pmp.deleteTimer(timerId);
    } catch (final ResourceUpdateException e) {
      catchedResourceUpdateException = true;
    }
    assertTrue(catchedResourceUpdateException);
    this.pmp.revokeTimer(timerId);
    result = this.pmp.isTimerDeployed(timerId);
    assertEquals(false, result);
    this.pmp.deleteTimer(timerId);
  }

  @Test
  void deployRevokeDeleteAndStatusTimerTest()
      throws ResourceUpdateException, ConflictingResourceException, InvalidEntityException,
      NoSuchEntityException, SchedulerException {

    final TimerId timerId = new TimerId("urn:timer:test:test-timer");
    // create and add Timer
    final Timer timer = new Timer(this.timerContent);
    final TimerId resultTimerId = this.pmp.addTimer(timer);
    assertEquals(timerId.getUrn(), resultTimerId.getUrn());
    this.pmp.deployTimer(timerId);
    Mockito.verify(this.scheduler).scheduleJob(ArgumentMatchers.any(), ArgumentMatchers.any());
    boolean result = this.pmp.isTimerDeployed(timerId);
    assertEquals(true, result);
    this.pmp.revokeTimer(timerId);
    Mockito.verify(this.scheduler).deleteJob(ArgumentMatchers.any());
    result = this.pmp.isTimerDeployed(timerId);
    assertEquals(false, result);
    this.pmp.deleteTimer(timerId);
  }

  @Test
  void deployAlreadyDeployedTimerTest()
      throws ResourceUpdateException, ConflictingResourceException, InvalidEntityException,
      NoSuchEntityException, SchedulerException {

    final TimerId timerId = new TimerId("urn:timer:test:test-timer");
    // create and add Timer
    final Timer timer = new Timer(this.timerContent);
    final TimerId resultTimerId = this.pmp.addTimer(timer);
    assertEquals(timerId.getUrn(), resultTimerId.getUrn());
    this.pmp.deployTimer(timerId);
    this.pmp.deployTimer(timerId);
    Mockito.verify(this.scheduler).scheduleJob(ArgumentMatchers.any(), ArgumentMatchers.any());
    final boolean timerIsDeployed = this.pmp.isTimerDeployed(timerId);
    assertTrue(timerIsDeployed);
  }

  @Test
  void addAndUpdateTimerTest() throws ResourceUpdateException, ConflictingResourceException,
      InvalidEntityException, NoSuchEntityException {
    final Timer timer = new Timer(this.timerContent);
    timer.setDeployed(true);

    final TimerId timerId = this.pmp.addTimer(timer);

    timer.setDeployed(false);
    timer.setModificationTime(0L);
    this.pmp.updateTimer(timer);
    final Timer resultTimer = this.pmp.getTimer(timerId);

    assertEquals(false, resultTimer.isDeployed());
    assertNotEquals(0L, resultTimer.getModificationTime());
  }

  @Test
  void resetTest() throws IOException, ResourceUpdateException, InvalidEntityException,
      ConflictingResourceException, NoSuchEntityException, ConflictingPolicyException {
    // create and add Policy
    final Policy policy = new Policy(this.policyContent);
    final PolicyId policyId = this.pmp.addPolicy(policy);
    final Policy policy2 = new Policy(this.policyContent2);
    final PolicyId policyId2 = this.pmp.addPolicy(policy2);

    final Timer timer = new Timer(this.timerContent);
    timer.setDeployed(true);
    final TimerId timerId = this.pmp.addTimer(timer);

    // mock pdp deploy
    when(this.pdp.deploy(policy, ZoneId.of("Europe/Berlin"))).thenReturn(true);

    this.pmp.deployPolicy(policyId);
    assertTrue(this.pmp.reset());

    // try to get the Policies and Timers should fail
    try {
      this.pmp.getPolicy(policyId);
    } catch (final NoSuchEntityException e) {
      assertTrue(true, "Exception expected,  because there is no policy anymore");
    }

    try {
      this.pmp.getPolicy(policyId2);
    } catch (final NoSuchEntityException e) {
      assertTrue(true, "Exception expected,  because there is no policy anymore");
    }

    try {
      this.pmp.getTimer(timerId);
    } catch (final NoSuchEntityException e) {
      assertTrue(true, "Exception expected,  because there is no timer anymore");
      return;
    }

    fail();
  }

  @Test
  void resetTestFailResourceUpdateExceptionOnRevokePolicy()
      throws IOException, ResourceUpdateException, InvalidEntityException,
      ConflictingResourceException, NoSuchEntityException, ConflictingPolicyException {
    // create and add Policy
    final Policy policy = new Policy(this.policyContent);
    final PolicyId policyId = this.pmp.addPolicy(policy);
    // mock pdp deploy
    when(this.pdp.deploy(policy, ZoneId.of("Europe/Berlin"))).thenReturn(true);
    this.pmp.deployPolicy(policyId);
    when(this.pdp.revokePolicy(policyId)).thenThrow(ResourceUpdateException.class);

    assertFalse(this.pmp.reset());
  }

  @Test
  void resetTestFailIoExceptionOnRevokePolicy()
      throws IOException, ResourceUpdateException, InvalidEntityException,
      ConflictingResourceException, NoSuchEntityException, ConflictingPolicyException {
    // create and add Policy
    final Policy policy = new Policy(this.policyContent);
    final PolicyId policyId = this.pmp.addPolicy(policy);
    // mock pdp deploy
    when(this.pdp.deploy(policy, ZoneId.of("Europe/Berlin"))).thenReturn(true);
    this.pmp.deployPolicy(policyId);
    when(this.pdp.revokePolicy(policyId)).thenThrow(IOException.class);

    assertFalse(this.pmp.reset());
  }

  @Test
  void resetTestFailResourceUpdateExceptionOnRevokeTimer() throws ResourceUpdateException,
      ConflictingResourceException, InvalidEntityException, NoSuchEntityException {
    final PolicyManagementPoint spyPmp = Mockito.spy(this.pmp);

    // create and add Policy
    final Timer timer = new Timer(this.timerContent);
    final TimerId timerId = spyPmp.addTimer(timer);
    spyPmp.deployTimer(timerId);
    doThrow(ResourceUpdateException.class).when(spyPmp).revokeTimer(timerId);

    assertFalse(spyPmp.reset());
  }

  @Test
  void resetTestFailInvalidEntityOnRevokeTimer() throws ResourceUpdateException,
      ConflictingResourceException, InvalidEntityException, NoSuchEntityException {
    final PolicyManagementPoint spyPmp = Mockito.spy(this.pmp);

    // create and add Policy
    final Timer timer = new Timer(this.timerContent);
    final TimerId timerId = spyPmp.addTimer(timer);
    spyPmp.deployTimer(timerId);
    doThrow(InvalidEntityException.class).when(spyPmp).revokeTimer(timerId);

    assertFalse(spyPmp.reset());
  }

  @Test
  void resetTestwithNoSuchEntityExceptionOnRevokeTimer() throws ResourceUpdateException,
      ConflictingResourceException, InvalidEntityException, NoSuchEntityException {
    final PolicyManagementPoint spyPmp = Mockito.spy(this.pmp);

    // create and add Policy
    final Timer timer = new Timer(this.timerContent);
    final TimerId timerId = this.pmp.addTimer(timer);
    spyPmp.deployTimer(timerId);
    doThrow(NoSuchEntityException.class).when(spyPmp).revokeTimer(timerId);
    final boolean result = spyPmp.reset();
    assertTrue(result);
  }

  @Test
  void addPolicyNullTest() throws IOException, ResourceUpdateException, InvalidEntityException,
      ConflictingResourceException, NoSuchEntityException {
    // create and add Policy
    final Policy policy = null;

    assertThrows(InvalidEntityException.class, () -> {
      this.pmp.addPolicy(policy);
    });
  }

  @Test
  void addPolicy0LTest() throws IOException, ResourceUpdateException, InvalidEntityException,
      ConflictingResourceException, NoSuchEntityException {
    // create and add Policy
    final Policy policy = new Policy(this.policyContent);
    policy.setModificationTime(0L);
    assertEquals(0L, policy.getModificationTime());
    this.pmp.addPolicy(policy);
    assertNotEquals(0L, policy.getModificationTime());
  }

  @Test
  void deployRevokeDeleteAndStatusPolicyTest()
      throws IOException, ResourceUpdateException, InvalidEntityException,
      ConflictingResourceException, NoSuchEntityException, ConflictingPolicyException {

    final PolicyId policyId = new PolicyId("urn:policy:test:test-policy");
    // create and add Policy
    final Policy policy = new Policy(this.policyContent);
    final PolicyId resultPolicyId = this.pmp.addPolicy(policy);
    assertEquals(policyId.getUrn(), resultPolicyId.getUrn());
    // mock pdp deploy
    when(this.pdp.deploy(policy, ZoneId.of("Europe/Berlin"))).thenReturn(true);
    this.pmp.deployPolicy(policyId);
    boolean result = this.pmp.isPolicyDeployed(policyId);
    assertEquals(true, result);
    // mock pdp revoke
    when(this.pdp.revokePolicy(policyId)).thenReturn(true);
    this.pmp.revokePolicy(policyId);
    result = this.pmp.isPolicyDeployed(policyId);
    assertEquals(false, result);
    this.pmp.deletePolicy(policyId);
  }

  @Test
  void deployAlreadyDeployedPolicyTest()
      throws IOException, ResourceUpdateException, InvalidEntityException,
      ConflictingResourceException, NoSuchEntityException, ConflictingPolicyException {

    final PolicyId policyId = new PolicyId("urn:policy:test:test-policy");
    // create and add Policy
    final Policy policy = new Policy(this.policyContent);
    final PolicyId resultPolicyId = this.pmp.addPolicy(policy);
    assertEquals(policyId.getUrn(), resultPolicyId.getUrn());
    // mock pdp deploy
    when(this.pdp.deploy(policy, ZoneId.of("Europe/Berlin"))).thenReturn(true);
    this.pmp.deployPolicy(policyId);
    this.pmp.deployPolicy(policyId);
    final boolean policyIsDeployed = this.pmp.isPolicyDeployed(policyId);
    assertTrue(policyIsDeployed);
  }

  @Test
  void deployPolicyFailTest()
      throws IOException, ResourceUpdateException, InvalidEntityException, NoSuchEntityException {
    // create and add Policy
    final PolicyId policyId = new PolicyId("urn:policy:test:test-policy");
    // mock pdp deploy
    assertThrows(NoSuchEntityException.class, () -> {
      this.pmp.deployPolicy(policyId);
    });
  }

  @Test
  void deploymenStatusPolicyFailTest() throws InvalidEntityException {
    // create and add Policy
    final PolicyId policyId = new PolicyId("urn:policy:test:test-policy");

    final boolean result = this.pmp.isPolicyDeployed(policyId);
    assertFalse(result);
  }

  @Test
  void revokePolicyFailTest()
      throws NoSuchEntityException, IOException, ResourceUpdateException, InvalidEntityException {
    // create and add Policy
    final PolicyId policyId = new PolicyId("urn:policy:test:test-policy");
    // mock pdp revoke
    when(this.pdp.revokePolicy(policyId)).thenReturn(true);

    assertThrows(NoSuchEntityException.class, () -> this.pmp.revokePolicy(policyId));
  }

  @Test
  void deletePolicyFailTest()
      throws NoSuchEntityException, ResourceUpdateException, InvalidEntityException {
    // create and add Policy
    final PolicyId policyId = new PolicyId("urn:policy:test:test-policy");

    assertThrows(NoSuchEntityException.class, () -> this.pmp.deletePolicy(policyId));
  }

  @Test
  void deployDeleteWithoutRevokeAndStatusPolicyTest()
      throws IOException, InvalidEntityException, ConflictingResourceException,
      NoSuchEntityException, ConflictingPolicyException, ResourceUpdateException {
    // create and add Policy
    boolean catchedResourceUpdateException = false;
    final Policy policy = new Policy(this.policyContent);
    final PolicyId policyId = this.pmp.addPolicy(policy);
    // mock pdp deploy
    when(this.pdp.deploy(policy, ZoneId.of("Europe/Berlin"))).thenReturn(true);
    this.pmp.deployPolicy(policyId);
    boolean result = this.pmp.isPolicyDeployed(policyId);
    assertEquals(true, result);
    try {
      this.pmp.deletePolicy(policyId);
    } catch (final ResourceUpdateException e) {
      catchedResourceUpdateException = true;
    }
    assertTrue(catchedResourceUpdateException);
    // mock pdp revoke
    when(this.pdp.revokePolicy(policyId)).thenReturn(true);
    this.pmp.revokePolicy(policyId);
    result = this.pmp.isPolicyDeployed(policyId);
    assertEquals(false, result);
    this.pmp.deletePolicy(policyId);
  }

  @Test
  void getDeployedPoliciesTest()
      throws IOException, ResourceUpdateException, InvalidEntityException,
      ConflictingResourceException, NoSuchEntityException, ConflictingPolicyException {
    // create and add Policy
    final Policy policy = new Policy(this.policyContent);
    final PolicyId policyId = this.pmp.addPolicy(policy);
    // mock pdp deploy
    when(this.pdp.deploy(policy, ZoneId.of("Europe/Berlin"))).thenReturn(true);
    this.pmp.deployPolicy(policyId);
    Set<Policy> deployedPolicies = this.pmp.getDeployedPolicies();
    assertEquals(1, deployedPolicies.size());
    assertTrue(deployedPolicies.contains(policy));
    // mock pdp revoke
    when(this.pdp.revokePolicy(policyId)).thenReturn(true);
    this.pmp.revokePolicy(policyId);
    deployedPolicies = this.pmp.getDeployedPolicies();
    assertEquals(0, deployedPolicies.size());
    assertFalse(deployedPolicies.contains(policy));
    this.pmp.deletePolicy(policyId);
  }

  @Test
  void getDeployedTimersTest() throws ResourceUpdateException, InvalidEntityException,
      ConflictingResourceException, NoSuchEntityException, SchedulerException {
    final TimerId timerId = new TimerId("urn:timer:test:test-timer");
    // create and add Timer
    final Timer timer = new Timer(this.timerContent);
    final TimerId resultTimerId = this.pmp.addTimer(timer);
    assertEquals(timerId.getUrn(), resultTimerId.getUrn());
    this.pmp.deployTimer(timerId);
    Mockito.verify(this.scheduler).scheduleJob(ArgumentMatchers.any(), ArgumentMatchers.any());
    Set<Timer> deployedTimers = this.pmp.getDeployedTimers(new SolutionId("urn:solution:test"));
    assertEquals(1, deployedTimers.size());
    assertTrue(deployedTimers.contains(timer));
    this.pmp.revokeTimer(timerId);
    Mockito.verify(this.scheduler).deleteJob(ArgumentMatchers.any());
    deployedTimers = this.pmp.getDeployedTimers(new SolutionId("urn:solution:test"));
    assertEquals(0, deployedTimers.size());
    assertFalse(deployedTimers.contains(timer));
    this.pmp.deleteTimer(timerId);
  }

  @Test
  void listDeployedPoliciesforSolutionTest()
      throws IOException, ResourceUpdateException, InvalidEntityException,
      ConflictingResourceException, NoSuchEntityException, ConflictingPolicyException {
    // create and add Policy
    final Policy policy = new Policy(this.policyContent);
    final PolicyId policyId = this.pmp.addPolicy(policy);
    final SolutionId solutionId = SOLUTION_ID;
    // mock pdp deploy
    when(this.pdp.deploy(policy, ZoneId.of("Europe/Berlin"))).thenReturn(true);
    this.pmp.deployPolicy(policyId);
    Set<PolicyId> deployedPolicies = this.pmp.listDeployedPolicies(solutionId);
    assertEquals(1, deployedPolicies.size());
    assertTrue(deployedPolicies.contains(policyId));
    // mock pdp revoke
    when(this.pdp.revokePolicy(policyId)).thenReturn(true);
    this.pmp.revokePolicy(policyId);
    deployedPolicies = this.pmp.listDeployedPolicies(solutionId);
    assertEquals(0, deployedPolicies.size());
    assertFalse(deployedPolicies.contains(policyId));
  }

  @Test
  void listRevokedPoliciesforSolutionTest()
      throws IOException, ResourceUpdateException, InvalidEntityException,
      ConflictingResourceException, NoSuchEntityException, ConflictingPolicyException {
    // create and add Policy
    final Policy policy = new Policy(this.policyContent);
    final PolicyId policyId = this.pmp.addPolicy(policy);
    final Policy policy2 = new Policy(this.policyContent2);
    this.pmp.addPolicy(policy2);
    final SolutionId solutionId = SOLUTION_ID;
    // mock pdp deploy
    when(this.pdp.deploy(policy, ZoneId.of("Europe/Berlin"))).thenReturn(true);
    this.pmp.deployPolicy(policyId);
    Set<PolicyId> revokedPolicies = this.pmp.listRevokedPolicies(solutionId);
    assertEquals(1, revokedPolicies.size());
    assertFalse(revokedPolicies.contains(policyId));
    // mock pdp revoke
    when(this.pdp.revokePolicy(policyId)).thenReturn(true);
    this.pmp.revokePolicy(policyId);
    revokedPolicies = this.pmp.listRevokedPolicies(solutionId);
    assertEquals(2, revokedPolicies.size());
    assertTrue(revokedPolicies.contains(policyId));
  }

  @Test
  void listPoliciesforSolutionTest()
      throws IOException, ResourceUpdateException, InvalidEntityException,
      ConflictingResourceException, NoSuchEntityException, ConflictingPolicyException {
    // create and add Policy
    final Policy policy = new Policy(this.policyContent);
    final PolicyId policyId = this.pmp.addPolicy(policy);
    final SolutionId solutionId = SOLUTION_ID;
    // mock pdp deploy
    when(this.pdp.deploy(policy, ZoneId.of("Europe/Berlin"))).thenReturn(true);
    this.pmp.deployPolicy(policyId);
    Set<PolicyId> policies = this.pmp.listPolicies(solutionId);
    assertEquals(1, policies.size());
    assertTrue(policies.contains(policyId));
    // mock pdp revoke
    when(this.pdp.revokePolicy(policyId)).thenReturn(true);
    this.pmp.revokePolicy(policyId);
    policies = this.pmp.listPolicies(solutionId);
    assertEquals(1, policies.size());
    assertTrue(policies.contains(policyId));
  }

  @Test
  void listPoliciesforSolutionTestFailsOnSolutionCompareWithINvalidEntityException()
      throws IOException, ResourceUpdateException, InvalidEntityException,
      ConflictingResourceException, NoSuchEntityException, ConflictingPolicyException {
    // create and add Policy
    final Policy policy = Mockito.spy(new Policy(this.policyContent));
    final PolicyId policyId = this.pmp.addPolicy(policy);
    final SolutionId solutionId = SOLUTION_ID;
    // mock pdp deploy
    when(this.pdp.deploy(policy, ZoneId.of("Europe/Berlin"))).thenReturn(true);
    this.pmp.deployPolicy(policyId);

    when(policy.getPolicyId()).thenReturn(new PolicyId("urn:test"));

    final Set<PolicyId> policies = this.pmp.listPolicies(solutionId);
    assertEquals(0, policies.size());
    assertFalse(policies.contains(policyId));
  }

  @Test
  void listDeployedPoliciesForSolutionDoesNotExistTest()
      throws IOException, ResourceUpdateException, InvalidEntityException,
      ConflictingResourceException, NoSuchEntityException, ConflictingPolicyException {
    // create and add Policy
    final Policy policy = new Policy(this.policyContent);
    final PolicyId policyId = this.pmp.addPolicy(policy);
    final SolutionId solutionId = new SolutionId("urn:solution:notest");
    // mock pdp deploy
    when(this.pdp.deploy(policy, ZoneId.of("Europe/Berlin"))).thenReturn(true);
    this.pmp.deployPolicy(policyId);
    Set<PolicyId> deployedPolicies = this.pmp.listDeployedPolicies(solutionId);
    assertEquals(0, deployedPolicies.size());
    assertFalse(deployedPolicies.contains(policyId));
    // mock pdp revoke
    when(this.pdp.revokePolicy(policyId)).thenReturn(true);
    this.pmp.revokePolicy(policyId);
    deployedPolicies = this.pmp.listDeployedPolicies(solutionId);
    assertEquals(0, deployedPolicies.size());
    assertFalse(deployedPolicies.contains(policyId));
    this.pmp.deletePolicy(policyId);
  }

  @Test
  void listRevokePoliciesForSolutionDoesNotExistTest()
      throws IOException, ResourceUpdateException, InvalidEntityException,
      ConflictingResourceException, NoSuchEntityException, ConflictingPolicyException {
    // create and add Policy
    final Policy policy = new Policy(this.policyContent);
    final PolicyId policyId = this.pmp.addPolicy(policy);
    final SolutionId solutionId = new SolutionId("urn:solution:notest");
    // mock pdp deploy
    when(this.pdp.deploy(policy, ZoneId.of("Europe/Berlin"))).thenReturn(true);
    this.pmp.deployPolicy(policyId);
    final Set<PolicyId> revokedPolicies = this.pmp.listRevokedPolicies(solutionId);
    assertEquals(0, revokedPolicies.size());
    assertFalse(revokedPolicies.contains(policyId));
  }

  @Test
  void listDeployedPoliciesForInvalidSolutionTest()
      throws IOException, ResourceUpdateException, InvalidEntityException,
      ConflictingResourceException, NoSuchEntityException, ConflictingPolicyException {
    // create and add Policy
    final Policy policy = new Policy(this.policyContent);
    final PolicyId policyId = this.pmp.addPolicy(policy);
    final SolutionId solutionId = new SolutionId("urn:notest");

    // mock pdp deploy
    when(this.pdp.deploy(policy, ZoneId.of("Europe/Berlin"))).thenReturn(true);

    this.pmp.deployPolicy(policyId);

    assertThrows(InvalidEntityException.class, () -> {
      this.pmp.listDeployedPolicies(solutionId);
    });
  }

  @Test
  void listRevokedPoliciesForInvalidSolutionTest() throws IOException, ResourceUpdateException,
      InvalidEntityException, ConflictingResourceException, NoSuchEntityException {
    // create and add Policy
    final Policy policy = new Policy(this.policyContent);
    this.pmp.addPolicy(policy);
    final SolutionId solutionId = new SolutionId("urn:notest");

    assertThrows(InvalidEntityException.class, () -> {
      this.pmp.listRevokedPolicies(solutionId);
    });
  }

  @Test
  void getWrongPolicyId() throws InvalidEntityException, NoSuchEntityException {
    assertThrows(NoSuchEntityException.class, () -> {
      this.pmp.getPolicy(new PolicyId("urn:policy:test:null"));
    });
  }

  @Test
  void getWrongTimerId() throws InvalidEntityException, NoSuchEntityException {
    assertThrows(NoSuchEntityException.class, () -> {
      this.pmp.getTimer(new TimerId("urn:timer:test:null"));
    });
  }

  @Test
  void addDeployedPolicyAndGetTest()
      throws IOException, ResourceUpdateException, InvalidEntityException,
      ConflictingResourceException, NoSuchEntityException, ConflictingPolicyException {
    final Policy policy = new Policy(this.policyContent);
    policy.setDeployed(true);
    // mock pdp deploy
    when(this.pdp.deploy(policy, ZoneId.of("Europe/Berlin"))).thenReturn(true);

    final PolicyId policyId = this.pmp.addPolicy(policy);

    final Policy resultPolicy = this.pmp.getPolicy(policyId);

    assertEquals(policy, resultPolicy);
    assertTrue(resultPolicy.isDeployed());
  }

  @Test
  void addAndUpdatePolicyTest() throws IOException, ResourceUpdateException, InvalidEntityException,
      ConflictingResourceException, NoSuchEntityException, ConflictingPolicyException {
    final Policy policy = new Policy(this.policyContent);
    policy.setDeployed(true);
    // mock pdp deploy
    when(this.pdp.deploy(policy, ZoneId.of("Europe/Berlin"))).thenReturn(true);

    final PolicyId policyId = this.pmp.addPolicy(policy);

    final Policy policy2 = new Policy(this.policyContent);
    policy2.setDeployed(false);
    policy2.setModificationTime(0L);
    // mock revoke at pdp
    when(this.pdp.revokePolicy(policyId)).thenReturn(true);

    this.pmp.updatePolicy(policy2);
    final Policy resultPolicy = this.pmp.getPolicy(policyId);

    assertFalse(resultPolicy.isDeployed());
    assertNotEquals(0L, resultPolicy.getModificationTime());
  }

  @Test
  void addAndUpdateRevokedPolicyTest()
      throws IOException, ResourceUpdateException, InvalidEntityException,
      ConflictingResourceException, NoSuchEntityException, ConflictingPolicyException {
    final Policy policy = new Policy(this.policyContent);
    policy.setDeployed(false);

    final PolicyId policyId = this.pmp.addPolicy(policy);

    final Policy policy2 = new Policy(this.policyContent);
    policy2.setDeployed(true);
    policy2.setModificationTime(0L);
    // mock pdp deploy
    when(this.pdp.deploy(policy2, ZoneId.of("Europe/Berlin"))).thenReturn(true);

    this.pmp.updatePolicy(policy2);
    final Policy resultPolicy = this.pmp.getPolicy(policyId);

    assertTrue(resultPolicy.isDeployed());
    assertNotEquals(0L, resultPolicy.getModificationTime());
  }

  @Test
  void addAndUpdateRevokedPolicyTestFailOnPdpDeployfalse()
      throws IOException, ResourceUpdateException, InvalidEntityException,
      ConflictingResourceException, NoSuchEntityException, ConflictingPolicyException {
    final Policy policy = new Policy(this.policyContent);
    policy.setDeployed(false);

    this.pmp.addPolicy(policy);

    final Policy policy2 = new Policy(this.policyContent);
    policy2.setDeployed(true);
    policy2.setModificationTime(0L);
    // mock pdp deploy
    when(this.pdp.deploy(policy2, ZoneId.of("Europe/Berlin"))).thenReturn(false);

    assertThrows(ResourceUpdateException.class, () -> {
      this.pmp.updatePolicy(policy2);
    });
  }

  @Test
  void addAndUpdateRevokedPolicyTestFailOnPdpDeployConflictingPolicyException()
      throws IOException, ResourceUpdateException, InvalidEntityException,
      ConflictingResourceException, NoSuchEntityException, ConflictingPolicyException {
    final Policy policy = new Policy(this.policyContent);
    policy.setDeployed(false);

    this.pmp.addPolicy(policy);

    final Policy policy2 = new Policy(this.policyContent);
    policy2.setDeployed(true);
    policy2.setModificationTime(0L);
    // mock pdp deploy
    when(this.pdp.deploy(policy2, ZoneId.of("Europe/Berlin")))
        .thenThrow(ConflictingPolicyException.class);

    assertThrows(ResourceUpdateException.class, () -> {
      this.pmp.updatePolicy(policy2);
    });
  }

  @Test
  void addAndUpdateDeployedPolicyTestFailsOnPdpUpdate()
      throws IOException, ResourceUpdateException, InvalidEntityException,
      ConflictingResourceException, NoSuchEntityException, ConflictingPolicyException {
    final Policy policy = new Policy(this.policyContent);
    policy.setDeployed(true);
    // mock pdp deploy
    when(this.pdp.deploy(policy, ZoneId.of("Europe/Berlin"))).thenReturn(true);
    when(this.pdp.updatePolicy(policy, ZoneId.of("Europe/Berlin"))).thenReturn(false);

    this.pmp.addPolicy(policy);
    assertThrows(ResourceUpdateException.class, () -> {
      this.pmp.updatePolicy(policy);
    });
  }

  @Test
  void addAndUpdateNotDelpoyedPolicyTestFailsOnPdpUpdate()
      throws IOException, ResourceUpdateException, InvalidEntityException,
      ConflictingResourceException, NoSuchEntityException, ConflictingPolicyException {
    final Policy policy = new Policy(this.policyContent);
    policy.setDeployed(true);
    // mock pdp deploy
    when(this.pdp.deploy(policy, ZoneId.of("Europe/Berlin"))).thenReturn(true);
    when(this.pdp.revokePolicy(policy.getPolicyId())).thenReturn(false);

    this.pmp.addPolicy(policy);

    final Policy policy2 = new Policy(this.policyContent);
    policy2.setDeployed(false);
    assertThrows(ResourceUpdateException.class, () -> {
      this.pmp.updatePolicy(policy2);
    });
  }

  @Test
  void updatePolicyNotExist()
      throws IOException, ResourceUpdateException, InvalidEntityException, NoSuchEntityException {
    final Policy policy = new Policy(this.policyContent);
    assertThrows(NoSuchEntityException.class, () -> {
      this.pmp.updatePolicy(policy);
    });
  }

  @Test
  void addPolicyAlreadyExists() throws IOException, ResourceUpdateException, InvalidEntityException,
      ConflictingResourceException, NoSuchEntityException {
    final Policy policy = new Policy(this.policyContent);
    this.pmp.addPolicy(policy);

    assertThrows(ConflictingResourceException.class, () -> {
      this.pmp.addPolicy(policy);
    });
  }

  @Test
  void addTimer0LTest() throws ResourceUpdateException, ConflictingResourceException,
      InvalidEntityException, NoSuchEntityException {
    // create and add Timer
    final Timer timer = new Timer(this.timerContent);
    timer.setModificationTime(0L);
    assertEquals(0L, timer.getModificationTime());
    this.pmp.addTimer(timer);
    assertNotEquals(0L, timer.getModificationTime());
  }

  @Test
  void addTimerNullTest() throws ResourceUpdateException, ConflictingResourceException,
      InvalidEntityException, NoSuchEntityException {
    // create and add Policy
    final Timer timer = null;

    assertThrows(InvalidEntityException.class, () -> {
      this.pmp.addTimer(timer);
    });
  }

  @Test
  void updateTimerNullTest() throws ResourceUpdateException, ConflictingResourceException,
      InvalidEntityException, NoSuchEntityException {

    assertThrows(InvalidEntityException.class, () -> {
      this.pmp.updateTimer(null);
    });
  }

  @Test
  void listTimersForSolutionTest() throws ResourceUpdateException, ConflictingResourceException,
      InvalidEntityException, NoSuchEntityException {
    // create and add Timer
    final Timer timer = new Timer(this.timerContent);
    final TimerId timerId = this.pmp.addTimer(timer);
    final SolutionId solutionId = SOLUTION_ID;
    this.pmp.deployTimer(timerId);
    Set<TimerId> timers = this.pmp.listTimers(solutionId);
    assertEquals(1, timers.size());
    assertTrue(timers.contains(timerId));
    this.pmp.revokeTimer(timerId);
    timers = this.pmp.listTimers(solutionId);
    assertEquals(1, timers.size());
    assertTrue(timers.contains(timerId));
  }

  @Test
  void listDeployedTimersforSolutionTest()
      throws ResourceUpdateException, ConflictingResourceException, InvalidEntityException,
      NoSuchEntityException, SchedulerException {
    // create and add Timer
    final Timer timer = new Timer(this.timerContent);
    final TimerId timerId = this.pmp.addTimer(timer);
    final SolutionId solutionId = SOLUTION_ID;
    this.pmp.deployTimer(timerId);
    Mockito.verify(this.scheduler).scheduleJob(ArgumentMatchers.any(), ArgumentMatchers.any());
    Set<TimerId> deployedTimers = this.pmp.listDeployedTimers(solutionId);
    assertEquals(1, deployedTimers.size());
    assertTrue(deployedTimers.contains(timerId));
    this.pmp.revokeTimer(timerId);
    Mockito.verify(this.scheduler).deleteJob(ArgumentMatchers.any());
    deployedTimers = this.pmp.listDeployedTimers(solutionId);
    assertEquals(0, deployedTimers.size());
    assertFalse(deployedTimers.contains(timerId));
    this.pmp.deleteTimer(timerId);
  }

  @Test
  void listRevokedTimersforSolutionTest() throws ResourceUpdateException,
      ConflictingResourceException, InvalidEntityException, NoSuchEntityException {
    // create and add Timer
    final Timer timer = new Timer(this.timerContent);
    final TimerId timerId = this.pmp.addTimer(timer);
    final SolutionId solutionId = SOLUTION_ID;
    final Set<TimerId> revokedTimers = this.pmp.listRevokedTimers(solutionId);
    assertEquals(1, revokedTimers.size());
    assertTrue(revokedTimers.contains(timerId));
  }

  @Test
  void listDeployedTimersForSolutionDoesNotExistTest()
      throws ResourceUpdateException, ConflictingResourceException, InvalidEntityException,
      NoSuchEntityException, SchedulerException {
    // create and add Timer
    final Timer timer = new Timer(this.timerContent);
    final TimerId timerId = this.pmp.addTimer(timer);
    final SolutionId solutionId = new SolutionId("urn:solution:notest");
    this.pmp.deployTimer(timerId);
    Mockito.verify(this.scheduler).scheduleJob(ArgumentMatchers.any(), ArgumentMatchers.any());
    Set<TimerId> deployedTimers = this.pmp.listDeployedTimers(solutionId);
    assertEquals(0, deployedTimers.size());
    assertFalse(deployedTimers.contains(timerId));
    this.pmp.revokeTimer(timerId);
    Mockito.verify(this.scheduler).deleteJob(ArgumentMatchers.any());
    deployedTimers = this.pmp.listDeployedTimers(solutionId);
    assertEquals(0, deployedTimers.size());
    assertFalse(deployedTimers.contains(timerId));
    this.pmp.deleteTimer(timerId);
  }

  @Test
  void listRevokedTimersForSolutionDoesNotExistTest() throws ResourceUpdateException,
      ConflictingResourceException, InvalidEntityException, NoSuchEntityException {
    // create and add Timer
    final Timer timer = new Timer(this.timerContent);
    final TimerId timerId = this.pmp.addTimer(timer);
    final SolutionId solutionId = new SolutionId("urn:solution:notest");

    final Set<TimerId> revokedTimers = this.pmp.listRevokedTimers(solutionId);
    assertEquals(0, revokedTimers.size());
    assertFalse(revokedTimers.contains(timerId));
  }

  @Test
  void listDeployedTimersForInvalidSolutionTest()
      throws ResourceUpdateException, ConflictingResourceException, InvalidEntityException,
      NoSuchEntityException, SchedulerException {
    // create and add Timer
    final Timer timer = new Timer(this.timerContent);
    final TimerId timerId = this.pmp.addTimer(timer);
    final SolutionId solutionId = new SolutionId("urn:notest");
    this.pmp.deployTimer(timerId);
    Mockito.verify(this.scheduler).scheduleJob(ArgumentMatchers.any(), ArgumentMatchers.any());
    assertThrows(InvalidEntityException.class, () -> {
      this.pmp.listDeployedTimers(solutionId);
    });
  }

  @Test
  void listRevokedTimersForInvalidSolutionTest() throws ResourceUpdateException,
      ConflictingResourceException, InvalidEntityException, NoSuchEntityException {
    // create and add Timer
    final Timer timer = new Timer(this.timerContent);
    this.pmp.addTimer(timer);
    final SolutionId solutionId = new SolutionId("urn:notest");
    assertThrows(InvalidEntityException.class, () -> {
      this.pmp.listRevokedTimers(solutionId);
    });
  }

  @Test
  void add_get_lookup_delete_PepTest() throws ConflictingResourceException, InvalidEntityException,
      NoSuchEntityException, ResourceUpdateException, IOException {
    final PepComponentInformation pepComponent = new PepComponentInformation(PEP_COMPONENT_ID);
    final ComponentId resultID = this.pmp.addPep(pepComponent);

    assertEquals(PEP_COMPONENT_ID, resultID);

    final PepComponentInformation reslutPepComponent = this.pmp.getPep(PEP_COMPONENT_ID);

    assertEquals(reslutPepComponent, pepComponent);
    assertTrue(this.pmp.lookupPep(SOLUTION_ID).contains(pepComponent));

    this.pmp.deletePep(PEP_COMPONENT_ID);
    assertFalse(this.pmp.lookupPep(SOLUTION_ID).contains(pepComponent));

  }

  @Test
  void addPepTwiceTest() throws ConflictingResourceException, InvalidEntityException {
    final PepComponentInformation pepComponent = new PepComponentInformation(PEP_COMPONENT_ID);
    this.pmp.addPep(pepComponent);
    assertThrows(ConflictingResourceException.class, () -> {
      this.pmp.addPep(pepComponent);
    });
  }

  @Test
  void add_get_update_state_delete_PipTest() throws ConflictingResourceException,
      InvalidEntityException, IOException, NoSuchEntityException, ResourceUpdateException {
    final PipComponentInformation pipComponent = new PipComponentInformation(PIP_COMPONENT_ID);
    final List<MethodInterfaceDescription> methodInterfaceDescriptions = new ArrayList<>();
    final MethodInterfaceDescription methodInterfaceDescription = new MethodInterfaceDescription(
        "urn:info:test:methodname", String.class, "test description");
    methodInterfaceDescriptions.add(methodInterfaceDescription);
    pipComponent.setMethodInterfaceDescriptions(methodInterfaceDescriptions);

    // add
    final ComponentId resultID = this.pmp.addPip(pipComponent);
    assertEquals(PIP_COMPONENT_ID, resultID);

    // get
    final PipComponentInformation reslutPipComponent = this.pmp.getPip(PIP_COMPONENT_ID);

    assertEquals(reslutPipComponent, pipComponent);
    assertTrue(this.pmp.lookupPip(SOLUTION_ID, methodInterfaceDescription).contains(pipComponent));

    // change for update
    final MethodInterfaceDescription methodInterfaceDescription2 = new MethodInterfaceDescription(
        "urn:info:test:methodname2", String.class, "test update description");
    methodInterfaceDescriptions.add(methodInterfaceDescription2);
    pipComponent.setMethodInterfaceDescriptions(methodInterfaceDescriptions);

    // update
    this.pmp.updatePip(pipComponent);

    final PipComponentInformation resultPxpComponent2 = this.pmp.getPip(PIP_COMPONENT_ID);

    assertEquals(resultPxpComponent2, pipComponent);
    assertTrue(this.pmp.lookupPip(SOLUTION_ID, methodInterfaceDescription2).contains(pipComponent));

    // mocks for state
    final IPolicyInformationPoint pipMock = Mockito.mock(IPolicyInformationPoint.class);
    Mockito.when(this.connectorFactory.getPip(any(PipComponentInformation.class), eq(null)))
        .thenReturn(pipMock);
    Mockito.when(pipMock.getHealth()).thenReturn(HealthStatus.of(Status.UP));

    // state
    assertEquals(HealthStatus.of(Status.UP), this.pmp.getPipState(PIP_COMPONENT_ID));

    final Map<ComponentId, HealthStatus> allComponentStates = this.pmp
        .getAllComponentStates(SOLUTION_ID);
    assertTrue(allComponentStates.containsKey(PIP_COMPONENT_ID));
    assertEquals(HealthStatus.of(Status.UP), allComponentStates.get(PIP_COMPONENT_ID));

    // delete
    this.pmp.deletePip(PIP_COMPONENT_ID);
    assertFalse(this.pmp.lookupPip(SOLUTION_ID, methodInterfaceDescription).contains(pipComponent));
  }

  @Test
  void addPipTwiceTest() throws ConflictingResourceException, InvalidEntityException, IOException {
    final PipComponentInformation pipComponent = new PipComponentInformation(PIP_COMPONENT_ID);
    final List<MethodInterfaceDescription> methodInterfaceDescriptions = new ArrayList<>();
    final MethodInterfaceDescription methodInterfaceDescription = new MethodInterfaceDescription(
        "urn:info:test:methodname", String.class, "test description");
    methodInterfaceDescriptions.add(methodInterfaceDescription);
    pipComponent.setMethodInterfaceDescriptions(methodInterfaceDescriptions);

    // add
    this.pmp.addPip(pipComponent);

    assertThrows(ConflictingResourceException.class, () -> {
      this.pmp.addPip(pipComponent);
    });
  }

  @Test
  void add_get_update_state_delete_PxpTest() throws ConflictingResourceException,
      InvalidEntityException, IOException, NoSuchEntityException, ResourceUpdateException {
    final PxpComponentInformation pxpComponent = new PxpComponentInformation(PXP_COMPONENT_ID);
    final List<MethodInterfaceDescription> methodInterfaceDescriptions = new ArrayList<>();
    final MethodInterfaceDescription methodInterfaceDescription = new MethodInterfaceDescription(
        "urn:action:test:methodname", String.class, "test description");
    methodInterfaceDescriptions.add(methodInterfaceDescription);
    pxpComponent.setMethodInterfaceDescriptions(methodInterfaceDescriptions);

    // add
    final ComponentId resultID = this.pmp.addPxp(pxpComponent);
    assertEquals(PXP_COMPONENT_ID, resultID);

    // get
    final PxpComponentInformation resultPxpComponent = this.pmp.getPxp(PXP_COMPONENT_ID);

    assertEquals(resultPxpComponent, pxpComponent);
    assertTrue(this.pmp.lookupPxp(SOLUTION_ID, methodInterfaceDescription).contains(pxpComponent));

    // change for update
    final MethodInterfaceDescription methodInterfaceDescription2 = new MethodInterfaceDescription(
        "urn:action:test:methodname2", String.class, "test update description");
    methodInterfaceDescriptions.add(methodInterfaceDescription2);
    pxpComponent.setMethodInterfaceDescriptions(methodInterfaceDescriptions);

    // update
    this.pmp.updatePxp(pxpComponent);

    final PxpComponentInformation resultPxpComponent2 = this.pmp.getPxp(PXP_COMPONENT_ID);

    assertEquals(resultPxpComponent2, pxpComponent);
    assertTrue(this.pmp.lookupPxp(SOLUTION_ID, methodInterfaceDescription2).contains(pxpComponent));

    // mocks for status
    final IPolicyExecutionPoint pxpMock = Mockito.mock(IPolicyExecutionPoint.class);
    Mockito.when(this.connectorFactory.getPxp(any(PxpComponentInformation.class), eq(null)))
        .thenReturn(pxpMock);
    Mockito.when(pxpMock.getHealth()).thenReturn(HealthStatus.of(Status.UP));

    // status
    assertEquals(HealthStatus.of(Status.UP), this.pmp.getPxpState(PXP_COMPONENT_ID));

    final Map<ComponentId, HealthStatus> allComponentStates = this.pmp
        .getAllComponentStates(SOLUTION_ID);
    assertTrue(allComponentStates.containsKey(PXP_COMPONENT_ID));
    assertEquals(HealthStatus.of(Status.UP), allComponentStates.get(PXP_COMPONENT_ID));

    // delete
    this.pmp.deletePxp(PXP_COMPONENT_ID);
    assertFalse(this.pmp.lookupPxp(SOLUTION_ID, methodInterfaceDescription).contains(pxpComponent));
  }

  @Test
  void addPxpTwiceTest() throws IOException, ConflictingResourceException, InvalidEntityException {
    final PxpComponentInformation pxpComponent = new PxpComponentInformation(PXP_COMPONENT_ID);
    final List<MethodInterfaceDescription> methodInterfaceDescriptions = new ArrayList<>();
    final MethodInterfaceDescription methodInterfaceDescription = new MethodInterfaceDescription(
        "urn:action:test:methodname", String.class, "test description");
    methodInterfaceDescriptions.add(methodInterfaceDescription);
    pxpComponent.setMethodInterfaceDescriptions(methodInterfaceDescriptions);

    // add
    this.pmp.addPxp(pxpComponent);

    assertThrows(ConflictingResourceException.class, () -> {
      this.pmp.addPxp(pxpComponent);
    });
  }

  @Test
  void getPdpTest() throws NoSuchEntityException {
    final PdpComponentInformation pdpComponentInformation = this.pmp.getPdp();
    assertEquals(PDP_COMPONENT_ID, pdpComponentInformation.getComponentId());
  }

  @Test
  void newPmpNullPdpComponentIdTest() throws InvalidEntityException {
    assertThrows(InvalidEntityException.class, () -> {
      this.pmp = new PolicyManagementPoint(new ComponentId("urn:component:test:pmp:pmp"), null,
          null, ZoneId.of("Europe/Berlin"), this.scheduler, this.connectorFactory);
    });
  }

  @Test
  void newPmpNullTest() throws InvalidEntityException {
    final PdpComponentInformation pdpComponentInformation = Mockito
        .mock(PdpComponentInformation.class);
    Mockito.when(pdpComponentInformation.getComponentId()).thenReturn(PDP_COMPONENT_ID);

    assertThrows(InvalidEntityException.class, () -> {
      this.pmp = new PolicyManagementPoint(new ComponentId("urn:component:test:pmp:pmp"),
          pdpComponentInformation, null, null, this.scheduler, this.connectorFactory);
    });
  }

  @Test
  void HealthStatusTest() {
    assertEquals(HealthStatus.of(Status.UP), this.pmp.getHealth());
  }

}
