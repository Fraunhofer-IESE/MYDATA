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

package de.fraunhofer.iese.mydata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import de.fraunhofer.iese.mydata.client.ClientId;
import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.connector.Authentication;
import de.fraunhofer.iese.mydata.component.connector.ConnectorFactory;
import de.fraunhofer.iese.mydata.component.connector.OAuthCredentials;
import de.fraunhofer.iese.mydata.component.information.PdpComponentInformation;
import de.fraunhofer.iese.mydata.component.interfaces.IBasicManagementService;
import de.fraunhofer.iese.mydata.component.interfaces.IManagementService;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyDecisionPoint;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyEnforcementPoint;
import de.fraunhofer.iese.mydata.exception.InitializationException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.internal.IMyDataEnvironmentFullFace;
import de.fraunhofer.iese.mydata.internal.TechnicalAccessGranter;
import de.fraunhofer.iese.mydata.pmp.synchronizer.FileSynchronizer;
import de.fraunhofer.iese.mydata.policy.event.history.IEventRepository;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import java.io.IOException;
import java.net.URI;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class MyDataEnvironmentInitTest {

  private static final String TIMEZONE = "Europe/Berlin";

  private final SolutionId solutionId = new SolutionId("urn:solution:test");

  @Mock
  private IEventRepository eventRepository;

  @Mock
  private ConnectorFactory connectorFactory;

  private IMyDataEnvironmentFullFace myDataEnvironment;

  @BeforeEach
  public void init() {
    this.myDataEnvironment = new MyDataEnvironmentFullFace("test", this.connectorFactory);
  }

  @AfterEach
  public void clean() {
    TechnicalAccessGranter.getTechnicalAccess(this.myDataEnvironment).destroy();
  }

  @Test
  void whenInitializeLocal_FieldsShouldBeSetProperly()
      throws InitializationException, IOException, NoSuchEntityException {
    this.myDataEnvironment.initializeLocal(this.solutionId, TIMEZONE, 4, false,
        this.eventRepository);
    assertEquals(OperationalMode.LOCAL, this.myDataEnvironment.getOperationalMode());
    assertEquals(this.solutionId, this.myDataEnvironment.getSolutionId());
    final IBasicManagementService pmp = this.myDataEnvironment.getPmp();
    assertNotNull(pmp);
    assertEquals("urn:component:test:pmp:pmp", pmp.getId().getUrn());
    final IPolicyDecisionPoint pdp = TechnicalAccessGranter
        .getTechnicalAccess(this.myDataEnvironment).getPdp().orElse(null);
    assertNotNull(pdp);
    assertEquals("urn:component:test:pdp:pdp", pdp.getId().getUrn());
    assertFalse(pdp.isWhitelistModeEnabled());
    final IPolicyEnforcementPoint pep = this.myDataEnvironment.getPep();
    assertNotNull(pep);
    assertTrue(pep.initialize());
    assertEquals(ZoneId.of(TIMEZONE), this.myDataEnvironment.getTimezone());
  }

  @Test
  public void whenInitializeLocalWithFileSync_NoExceptionShouldOccur()
      throws InitializationException, IOException, NoSuchEntityException {
    this.myDataEnvironment.initializeLocalWithFileSync(this.solutionId, TIMEZONE,
        "target/test-classes/policies", 4, false, this.eventRepository);
    assertEquals(OperationalMode.LOCAL_WITH_FILE_SYNC, this.myDataEnvironment.getOperationalMode());
    assertEquals(this.solutionId, this.myDataEnvironment.getSolutionId());
    final IBasicManagementService pmp = this.myDataEnvironment.getPmp();
    assertNotNull(pmp);
    assertEquals("urn:component:test:pmp:pmp", pmp.getId().getUrn());
    final IPolicyDecisionPoint pdp = TechnicalAccessGranter
        .getTechnicalAccess(this.myDataEnvironment).getPdp().orElse(null);
    assertNotNull(pdp);
    assertEquals("urn:component:test:pdp:pdp", pdp.getId().getUrn());
    assertFalse(pdp.isWhitelistModeEnabled());
    // TODO add check that the PDP is initially in failureMode?
    final IPolicyEnforcementPoint pep = this.myDataEnvironment.getPep();
    assertNotNull(pep);
    assertTrue(pep.initialize());
    assertEquals(ZoneId.of(TIMEZONE), this.myDataEnvironment.getTimezone());
    // check file sync thread running
    final int size = Thread.activeCount();
    final Thread[] threads = new Thread[size * 2];
    Thread.enumerate(threads);
    assertTrue(Arrays.stream(threads)
        .filter(thread -> FileSynchronizer.FILE_SYNC_THREAD_NAME.equals(thread.getName()))
        .anyMatch(Thread::isAlive));
  }

  @Test
  public void whenInitializeLocalWithCloudSyncAndIsMasterClient_NoExceptionShouldOccur()
      throws Exception {
    final IManagementService msMock = Mockito.mock(IManagementService.class);
    Mockito.when(
        this.connectorFactory.getManagementService(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(msMock);
    this.myDataEnvironment.initializeLocalWithCloudSync(this.solutionId,
        URI.create("http://localhost:8080"),
        new OAuthCredentials(new ClientId("urn:client:test:test"), "secret",
            URI.create("http://localhost:8080/oauth/token")),
        TIMEZONE, false, null, null, "PT5M", "0/5 * * * * ?", true, 4, false, this.eventRepository);
    assertEquals(OperationalMode.LOCAL_WITH_CLOUD_SYNC,
        this.myDataEnvironment.getOperationalMode());
    assertEquals(this.solutionId, this.myDataEnvironment.getSolutionId());
    final IBasicManagementService pmp = this.myDataEnvironment.getPmp();
    assertNotNull(pmp);
    assertEquals("urn:component:test:pmp:test", pmp.getId().getUrn());
    final IPolicyDecisionPoint pdp = TechnicalAccessGranter
        .getTechnicalAccess(this.myDataEnvironment).getPdp().orElse(null);
    assertNotNull(pdp);
    assertEquals("urn:component:test:pdp:pdp", pdp.getId().getUrn());
    assertFalse(pdp.isWhitelistModeEnabled());
    // TODO add check that the PDP is initially in failureMode?
    final IPolicyEnforcementPoint pep = this.myDataEnvironment.getPep();
    assertNotNull(pep);
    assertTrue(pep.initialize());
    assertEquals(ZoneId.of(TIMEZONE), this.myDataEnvironment.getTimezone());
    // check cloud sync scheduler is running
    final Scheduler scheduler = new StdSchedulerFactory()
        .getScheduler("mydata-sync-" + this.myDataEnvironment.getEnvironmentId());
    assertNotNull(scheduler);
    assertTrue(scheduler.isStarted());
    assertFalse(scheduler.isShutdown());
    assertEquals(1, scheduler.getJobKeys(GroupMatcher.anyGroup()).size());
  }

  @Test
  public void whenInitializeLocalWithCloudSyncAndIsNotMasterClient_NoExceptionShouldOccur()
      throws Exception {
    final IManagementService msMock = Mockito.mock(IManagementService.class);
    Mockito.when(
        this.connectorFactory.getManagementService(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(msMock);
    this.myDataEnvironment.initializeLocalWithCloudSync(this.solutionId,
        URI.create("http://localhost:8080"),
        new OAuthCredentials(new ClientId("urn:client:test:test"), "secret",
            URI.create("http://localhost:8080/oauth/token")),
        TIMEZONE, false, null, null, "PT5M", "0/5 * * * * ?", false, 4, false,
        this.eventRepository);
    assertEquals(OperationalMode.LOCAL_WITH_CLOUD_SYNC,
        this.myDataEnvironment.getOperationalMode());
    assertEquals(this.solutionId, this.myDataEnvironment.getSolutionId());
    final IBasicManagementService pmp = this.myDataEnvironment.getPmp();
    assertNotNull(pmp);
    assertEquals("urn:component:test:pmp:test", pmp.getId().getUrn());
    final IPolicyDecisionPoint pdp = TechnicalAccessGranter
        .getTechnicalAccess(this.myDataEnvironment).getPdp().orElse(null);
    assertNotNull(pdp);
    assertEquals("urn:component:test:pdp:pdp", pdp.getId().getUrn());
    assertFalse(pdp.isWhitelistModeEnabled());
    // TODO add check that the PDP is initially in failureMode?
    final IPolicyEnforcementPoint pep = this.myDataEnvironment.getPep();
    assertNotNull(pep);
    assertTrue(pep.initialize());
    assertEquals(ZoneId.of(TIMEZONE), this.myDataEnvironment.getTimezone());
    // check cloud sync scheduler is running
    final Scheduler scheduler = new StdSchedulerFactory()
        .getScheduler("mydata-sync-" + this.myDataEnvironment.getEnvironmentId());
    assertNotNull(scheduler);
    assertTrue(scheduler.isStarted());
    assertFalse(scheduler.isShutdown());
    assertEquals(1, scheduler.getJobKeys(GroupMatcher.anyGroup()).size());
  }

  @Test
  public void whenInitializeLocalWithCloudSyncWithInvalidClientId_InitializationExceptionShouldBeThrown()
      throws Exception {
    final IManagementService msMock = Mockito.mock(IManagementService.class);
    assertThrows(InitializationException.class,
        () -> this.myDataEnvironment.initializeLocalWithCloudSync(this.solutionId,
            URI.create("http://localhost:8080"),
            new OAuthCredentials(new ClientId("urn:client:INVALID:test"), "secret",
                URI.create("http://localhost:8080/oauth/token")),
            TIMEZONE, false, null, null, "PT5M", "0/5 * * * * ?", false, 4, false,
            this.eventRepository));
  }

  @Test
  public void whenInitializeCloud_NoExceptionShouldOccur() throws Exception {
    final IManagementService msMock = Mockito.mock(IManagementService.class);
    final ComponentId pdpComponentId = new ComponentId("urn:component:mydata:pdp:pdp");
    final List<URI> ts = new ArrayList<>();
    ts.add(URI.create("http://test:8081"));
    Mockito.when(msMock.getPdp()).thenReturn(new PdpComponentInformation(pdpComponentId, ts));
    Mockito.when(msMock.getZoneId(ArgumentMatchers.any())).thenReturn(ZoneId.of(TIMEZONE));
    final IPolicyDecisionPoint pdpMock = Mockito.mock(IPolicyDecisionPoint.class);
    Mockito.when(
        this.connectorFactory.getManagementService(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(msMock);
    Mockito.when(this.connectorFactory.getPdp(ArgumentMatchers.any(PdpComponentInformation.class),
        ArgumentMatchers.any(Authentication.class))).thenReturn(pdpMock);
    this.myDataEnvironment.initializeCloud(this.solutionId, URI.create("http://localhost:8080"),
        new OAuthCredentials(new ClientId("urn:client:test:test"), "secret",
            URI.create("http://localhost:8080/oauth/token")));
    assertEquals(OperationalMode.CLOUD, this.myDataEnvironment.getOperationalMode());
    assertEquals(this.solutionId, this.myDataEnvironment.getSolutionId());
    final IBasicManagementService pmp = this.myDataEnvironment.getPmp();
    assertNotNull(pmp);
    final IPolicyDecisionPoint pdp = TechnicalAccessGranter
        .getTechnicalAccess(this.myDataEnvironment).getPdp().orElse(null);
    assertNotNull(pdp);
    final IPolicyEnforcementPoint pep = this.myDataEnvironment.getPep();
    assertNotNull(pep);
    assertTrue(pep.initialize());
    final SolutionId solutionId = new SolutionId("urn:solution:test");
    assertEquals(ZoneId.of(TIMEZONE), this.myDataEnvironment.getTimezone());
    verify(msMock).getZoneId(solutionId);
  }

  @Test
  public void whenInitializeCloudWithoutPdp_NoExceptionShouldOccur() throws Exception {
    final IManagementService msMock = Mockito.mock(IManagementService.class);
    Mockito.when(msMock.getPdp()).thenThrow(NoSuchEntityException.class);
    Mockito.when(msMock.getZoneId(ArgumentMatchers.any())).thenReturn(ZoneId.of(TIMEZONE));
    Mockito.when(
        this.connectorFactory.getManagementService(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(msMock);
    this.myDataEnvironment.initializeCloud(this.solutionId, URI.create("http://localhost:8080"),
        new OAuthCredentials(new ClientId("urn:client:test:test"), "secret",
            URI.create("http://localhost:8080/oauth/token")));
    assertEquals(OperationalMode.CLOUD, this.myDataEnvironment.getOperationalMode());
    assertEquals(this.solutionId, this.myDataEnvironment.getSolutionId());
    final IBasicManagementService pmp = this.myDataEnvironment.getPmp();
    assertNotNull(pmp);
    assertFalse(
        TechnicalAccessGranter.getTechnicalAccess(this.myDataEnvironment).getPdp().isPresent());
    final IPolicyEnforcementPoint pep = this.myDataEnvironment.getPep();
    assertNotNull(pep);
    assertFalse(pep.initialize());
    final SolutionId solutionId = new SolutionId("urn:solution:test");
    assertEquals(ZoneId.of(TIMEZONE), this.myDataEnvironment.getTimezone());
    verify(msMock).getZoneId(solutionId);
  }

}
