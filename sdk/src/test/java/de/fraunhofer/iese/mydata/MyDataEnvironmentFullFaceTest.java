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
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.connector.ConnectorFactory;
import de.fraunhofer.iese.mydata.component.information.PepComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PipComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PxpComponentInformation;
import de.fraunhofer.iese.mydata.component.interfaces.IBasicManagementService;
import de.fraunhofer.iese.mydata.component.interfaces.IManagementService;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyDecisionPoint;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyEnforcementPoint;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyExecutionPoint;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyInformationPoint;
import de.fraunhofer.iese.mydata.exception.ConflictingResourceException;
import de.fraunhofer.iese.mydata.exception.InitializationException;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.exception.ResourceUpdateException;
import de.fraunhofer.iese.mydata.internal.IComponentInstanceStore;
import de.fraunhofer.iese.mydata.internal.IMyDataEnvironmentFullFace;
import de.fraunhofer.iese.mydata.internal.TechnicalAccessGranter;
import de.fraunhofer.iese.mydata.pmp.synchronizer.ISyncService;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.reactive.common.EventParameter;
import de.fraunhofer.iese.mydata.reactive.common.EventSpecification;
import de.fraunhofer.iese.mydata.reactive.common.PepServiceDescription;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import io.reactivex.rxjava3.core.Observable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Optional;

/**
 * Here we only test the behaviour of an already initialized {@link MyDataEnvironmentFullFace}. The
 * initialization will be tested in {@link MyDataEnvironmentInitTest}
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class MyDataEnvironmentFullFaceTest { // TODO clean up and improve

  private static final Logger LOG = LoggerFactory.getLogger(MyDataEnvironmentFullFaceTest.class);

  @SuppressWarnings({
      "unused", "FieldCanBeLocal"
  })
  private String caseName;

  private String environmentId;

  private Boolean initializing;

  private Boolean active;

  private IComponentInstanceStore componentInstanceStore;

  private ISyncService syncService;

  private OperationalMode operationalMode;

  private ConnectorFactory connectorFactory;

  private SolutionId solutionId;

  private IBasicManagementService pmp;

  private IPolicyDecisionPoint pdp;

  private IPolicyEnforcementPoint pep;

  private ZoneId zoneId;

  private IMyDataEnvironmentFullFace myDataEnvironment;

  public MyDataEnvironmentFullFaceTest() {
    // default constructor for JUnit 5. We'll initialize fields in setUp().
  }

  @BeforeEach
  public void setUp() {
    // initialize default values similar to the original LOCAL case
    this.caseName = "Mode: LOCAL";
    this.environmentId = "test";
    this.initializing = true;
    this.active = true;
    this.componentInstanceStore = Mockito.mock(IComponentInstanceStore.class);
    this.syncService = null; // default: no sync service
    this.operationalMode = OperationalMode.LOCAL;
    this.connectorFactory = Mockito.mock(ConnectorFactory.class);
    this.solutionId = new SolutionId("urn:solution:test");
    this.pmp = Mockito.mock(IBasicManagementService.class);
    this.pdp = Mockito.mock(IPolicyDecisionPoint.class);
    this.pep = Mockito.mock(IPolicyEnforcementPoint.class);
    this.zoneId = ZoneId.of("Europe/Berlin");

    this.myDataEnvironment = new MyDataEnvironmentFullFace(this.environmentId, this.initializing,
        this.active, this.componentInstanceStore, this.syncService, this.operationalMode,
        this.connectorFactory, this.solutionId, this.pmp, this.pdp, this.pep, this.zoneId);
  }

  @Test
  void givenNotActive_whenGetPmp_thenThrowIllegalStateException() {
    this.myDataEnvironment = new MyDataEnvironmentFullFace(this.environmentId, false, false,
        this.componentInstanceStore, this.syncService, this.operationalMode, this.connectorFactory,
        this.solutionId, this.pmp, this.pdp, this.pep, this.zoneId);
    assertThrows(IllegalStateException.class, () -> {
      this.myDataEnvironment.getPmp();
    });
  }

  @Test
  void testEnvironmentIdIsRetrievableAndMatches() {
    assertEquals(this.environmentId, this.myDataEnvironment.getEnvironmentId());
  }

  @Test
  void givenServiceHasNoServiceMethods_whenRegisterManagedPip_NoRegistrationShouldHappenThrowsException()
      throws ResourceUpdateException, ConflictingResourceException, IOException,
      NoSuchEntityException {
    try {
      this.myDataEnvironment.registerManagedPip("bla", new DummyServiceWithNoMethod(),
          Collections.singletonList(URI.create("http://bla")));
    } catch (final InvalidEntityException e) {
      assertTrue(e.getMessage().contains("action method"));
      return;
    }
    fail();
  }

  @Test
  void givenWeAreNotInCloudMode_whenRegisterManagedPip_RegistrationShouldHappen()
      throws ConflictingResourceException, IOException, InvalidEntityException,
      ResourceUpdateException, NoSuchEntityException {
    if (this.operationalMode == OperationalMode.CLOUD) {
      LOG.info("skip test as condition does not match");
      return;
    }
    final DummyServiceWithOneMethod service = new DummyServiceWithOneMethod();
    final ComponentId componentId = new ComponentId("urn:component:test:pip:bla");

    when(this.pmp.addPip(any(PipComponentInformation.class))).thenReturn(componentId);
    this.myDataEnvironment.registerManagedPip("bla", service,
        Collections.singletonList(URI.create("http://bla")));
    verify(this.componentInstanceStore).addPipInstance(eq(componentId),
        any(IPolicyInformationPoint.class));
    verify(this.pmp).addPip(any(PipComponentInformation.class));
  }

  @Test
  void givenWeAreNotInCloudMode_whenRegisterManagedPxp_RegistrationShouldHappen()
      throws ConflictingResourceException, IOException, InvalidEntityException,
      ResourceUpdateException, NoSuchEntityException {
    if (this.operationalMode == OperationalMode.CLOUD) {
      LOG.info("skip test as condition does not match");
      return;
    }
    final DummyServiceWithOneMethod service = new DummyServiceWithOneMethod();
    final ComponentId componentId = new ComponentId("urn:component:test:pxp:bla");
    when(this.pmp.addPxp(any(PxpComponentInformation.class))).thenReturn(componentId);
    this.myDataEnvironment.registerManagedPxp("bla", service,
        Collections.singletonList(URI.create("http://bla")));
    verify(this.componentInstanceStore).addPxpInstance(eq(componentId),
        any(IPolicyExecutionPoint.class));
    verify(this.pmp).addPxp(any(PxpComponentInformation.class));
  }

  @Test
  void givenServiceHasNoServiceMethods_whenRegisterManagedPxp_NoRegistrationShouldHappenThrowsException()
      throws ResourceUpdateException, ConflictingResourceException, IOException,
      NoSuchEntityException {
    try {
      this.myDataEnvironment.registerManagedPxp("bla", new DummyServiceWithNoMethod(),
          Collections.singletonList(URI.create("http://bla")));
    } catch (final InvalidEntityException e) {
      assertTrue(e.getMessage().contains("action method"));
      return;
    }
    fail();
  }

  @Test
  void givenWeAreInCloudMode_whenRegisterLocalPip_ResourceUpdateExceptionShouldBeThrown()
      throws ConflictingResourceException, IOException, InvalidEntityException,
      NoSuchEntityException {
    if (this.operationalMode != OperationalMode.CLOUD) {
      LOG.info("skip test as condition does not match");
      return;
    }
    try {
      this.myDataEnvironment.registerLocalPip("bla", new DummyServiceWithOneMethod());
    } catch (final ResourceUpdateException e) {
      assertEquals("Local components are not allowed in CLOUD-mode", e.getMessage());
      return;
    }
    fail();
  }

  @Test
  void givenWeAreNotInCloudMode_whenRegisterLocalPip_RegistrationShouldHappen()
      throws ConflictingResourceException, IOException, InvalidEntityException,
      ResourceUpdateException, NoSuchEntityException {
    if (this.operationalMode == OperationalMode.CLOUD) {
      LOG.info("skip test as condition does not match");
      return;
    }
    final DummyServiceWithOneMethod service = new DummyServiceWithOneMethod();
    final ComponentId componentId = new ComponentId("urn:component:test:pip:bla");

    when(this.pmp.addPip(any(PipComponentInformation.class))).thenReturn(componentId);
    this.myDataEnvironment.registerLocalPip("bla", service);
    verify(this.componentInstanceStore).addPipInstance(eq(componentId),
        any(IPolicyInformationPoint.class));
    verify(this.pmp).addPip(any(PipComponentInformation.class));
  }

  @Test
  void givenWeAreNotInCloudMode_whenRegisterLocalPxp_RegistrationShouldHappen()
      throws ConflictingResourceException, IOException, InvalidEntityException,
      ResourceUpdateException, NoSuchEntityException {
    if (this.operationalMode == OperationalMode.CLOUD) {
      LOG.info("skip test as condition does not match");
      return;
    }
    final DummyServiceWithOneMethod service = new DummyServiceWithOneMethod();
    final ComponentId componentId = new ComponentId("urn:component:test:pxp:bla");
    when(this.pmp.addPxp(any(PxpComponentInformation.class))).thenReturn(componentId);
    this.myDataEnvironment.registerLocalPxp("bla", service);
    verify(this.componentInstanceStore).addPxpInstance(eq(componentId),
        any(IPolicyExecutionPoint.class));
    verify(this.pmp).addPxp(any(PxpComponentInformation.class));
  }

  @Test
  void givenWeAreNotInCloudMode_whenManagedPipRegistrationAtPmpFails_instanceShouldBeRemovedFromServiceInstanceManager()
      throws ConflictingResourceException, IOException, InvalidEntityException,
      ResourceUpdateException, NoSuchEntityException {
    if (this.operationalMode == OperationalMode.CLOUD) {
      LOG.info("skip test as condition does not match");
      return;
    }
    final DummyServiceWithOneMethod service = new DummyServiceWithOneMethod();
    final ComponentId componentId = new ComponentId("urn:component:test:pip:bla");
    when(this.pmp.addPip(any(PipComponentInformation.class)))
        .thenThrow(ResourceUpdateException.class);
    try {
      this.myDataEnvironment.registerLocalPip("bla", service);
    } catch (final ResourceUpdateException e) {
      verify(this.pmp).addPip(any(PipComponentInformation.class));
      verify(this.componentInstanceStore).addPipInstance(eq(componentId),
          any(IPolicyInformationPoint.class));
      verify(this.componentInstanceStore).removePipInstance(eq(componentId));
      return;
    }
    fail();
  }

  @Test
  void givenWeAreNotInCloudMode_whenManagedPxpRegistrationAtPmpFails_instanceShouldBeRemovedFromServiceInstanceManager()
      throws ConflictingResourceException, IOException, InvalidEntityException,
      ResourceUpdateException, NoSuchEntityException {
    if (this.operationalMode == OperationalMode.CLOUD) {
      LOG.info("skip test as condition does not match");
      return;
    }
    final DummyServiceWithOneMethod service = new DummyServiceWithOneMethod();
    final ComponentId componentId = new ComponentId("urn:component:test:pxp:bla");
    when(this.pmp.addPxp(any(PxpComponentInformation.class)))
        .thenThrow(ResourceUpdateException.class);
    try {
      this.myDataEnvironment.registerLocalPxp("bla", service);
    } catch (final ResourceUpdateException e) {
      verify(this.pmp).addPxp(any(PxpComponentInformation.class));
      verify(this.componentInstanceStore).addPxpInstance(eq(componentId),
          any(IPolicyExecutionPoint.class));
      verify(this.componentInstanceStore).removePxpInstance(eq(componentId));
      return;
    }
    fail();
  }

  @Test
  void givenWeAreInCloudMode_whenRegisterLocalPxp_ResourceUpdateExceptionShouldBeThrown()
      throws ConflictingResourceException, IOException, InvalidEntityException,
      NoSuchEntityException {
    if (this.operationalMode != OperationalMode.CLOUD) {
      LOG.info("skip test as condition does not match");
      return;
    }
    try {
      this.myDataEnvironment.registerLocalPxp("bla", new DummyServiceWithOneMethod());
    } catch (final ResourceUpdateException e) {
      assertEquals("Local components are not allowed in CLOUD-mode", e.getMessage());
      return;
    }
    fail();
  }

  @Test
  void testGetManagedPipQueriesUnderlyingServiceInstanceManager() {
    final ComponentId componentId = new ComponentId("urn:component:test:pip:bla");
    final IPolicyInformationPoint mockedPip = Mockito.mock(IPolicyInformationPoint.class);
    when(this.componentInstanceStore.getPipInstanceByComponentId(componentId))
        .thenReturn(Optional.of(mockedPip));
    final Optional<IPolicyInformationPoint> policyInformationPointOptional = this.myDataEnvironment
        .getManagedPip(componentId);
    assertTrue(policyInformationPointOptional.isPresent());
    assertEquals(mockedPip, policyInformationPointOptional.get());
    verify(this.componentInstanceStore).getPipInstanceByComponentId(componentId);
  }

  @Test
  void testGetManagedPxpQueriesUnderlyingServiceInstanceManager() {
    final ComponentId componentId = new ComponentId("urn:component:test:pxp:bla");
    final IPolicyExecutionPoint mockedPxp = Mockito.mock(IPolicyExecutionPoint.class);
    when(this.componentInstanceStore.getPxpInstanceByComponentId(componentId))
        .thenReturn(Optional.of(mockedPxp));
    final Optional<IPolicyExecutionPoint> policyExecutionPointOptional = this.myDataEnvironment
        .getManagedPxp(componentId);
    assertTrue(policyExecutionPointOptional.isPresent());
    assertEquals(mockedPxp, policyExecutionPointOptional.get());
    verify(this.componentInstanceStore).getPxpInstanceByComponentId(componentId);
  }

  @Test
  void testPmpReferenceIsRetrievable() {
    assertNotNull(this.myDataEnvironment.getPmp());
  }

  @Test
  void testPdpReferenceIsRetrievable() {
    if (null == this.pdp) {
      assertFalse(
          TechnicalAccessGranter.getTechnicalAccess(this.myDataEnvironment).getPdp().isPresent());
    } else {
      assertNotNull(TechnicalAccessGranter.getTechnicalAccess(this.myDataEnvironment).getPdp());
      assertTrue(
          TechnicalAccessGranter.getTechnicalAccess(this.myDataEnvironment).getPdp().isPresent());
      assertNotNull(
          TechnicalAccessGranter.getTechnicalAccess(this.myDataEnvironment).getPdp().get());
    }
  }

  @Test
  void testPepReferenceIsRetrievable() {
    assertNotNull(this.myDataEnvironment.getPep());
  }

  @Test
  void givenWeHaveAPdp_whenConstructAndRegisterCustomPepWithValidPepInterface_PepShouldBeConstructedRegisteredAndReturned()
      throws InitializationException, IOException, InvalidEntityException, ResourceUpdateException,
      ConflictingResourceException, NoSuchEntityException {
    if (null == this.pdp) {
      LOG.info("skip test as condition does not match");
      return;
    }
    final ComponentId componentId = new ComponentId("urn:component:test:pep:my-pep");
    when(this.pmp.addPep(any(PepComponentInformation.class))).thenReturn(componentId);
    final MyPep myPep = this.myDataEnvironment.constructAndRegisterCustomPep("my-pep", MyPep.class);
    assertNotNull(myPep); // constructed and returned
    verify(this.pmp).addPep(any(PepComponentInformation.class)); // registered at PMP
  }

  @Test
  void givenWeHaveNoPdp_whenConstructAndRegisterCustomPepWithValidPepInterface_PepShouldBeConstructedRegisteredAndReturned() {
    if (null == this.pdp) {
      final ComponentId componentId = new ComponentId("urn:component:test:pep:my-pep");
      try {
        this.myDataEnvironment.constructAndRegisterCustomPep("my-pep", MyPep.class);
      } catch (final InitializationException ignored) {
        // this is what we want
        return;
      }
      fail("this line should not be reached");
    } else {
      LOG.info("skip test as condition does not match");
    }
  }

  @Test
  void givenWeHaveAPdp_whenConstructAndRegisterCustomPepWithValidPepInterfaceWithAnnotation_PepShouldBeConstructedRegisteredAndReturned()
      throws Exception {
    if (null == this.pdp) {
      LOG.info("skip test as condition does not match");
      return;
    }
    final ComponentId componentId = new ComponentId("urn:component:test:pep:my-pep");
    when(this.pmp.addPep(any(PepComponentInformation.class))).thenReturn(componentId);
    final MyPepWithAnnotation myPep = this.myDataEnvironment.constructAndRegisterCustomPep("my-pep",
        MyPepWithAnnotation.class);
    assertNotNull(myPep);
    verify(this.pmp).addPep(any(PepComponentInformation.class)); // registered at PMP
  }

  @Test
  void givenWeHaveNoPdp_whenConstructAndRegisterCustomPepWithValidPepInterfaceWithAnnotation_PepShouldBeConstructedRegisteredAndReturned() {
    if (null == this.pdp) {
      final ComponentId componentId = new ComponentId("urn:component:test:pep:my-pep");
      try {
        this.myDataEnvironment.constructAndRegisterCustomPep("my-pep", MyPepWithAnnotation.class);
      } catch (final InitializationException ignored) {
        // this is what we want
        return;
      }
      fail("this line should not be reached");
    } else {
      LOG.info("skip test as condition does not match");
    }
  }

  @Test
  void whenCustomPepRegistrationFails_InitializationExceptionShoudBeThrown()
      throws ResourceUpdateException, ConflictingResourceException, InvalidEntityException,
      IOException, InitializationException, NoSuchEntityException {
    when(this.pmp.addPep(any(PepComponentInformation.class)))
        .thenThrow(InvalidEntityException.class);
    assertThrows(InitializationException.class, () -> {
      this.myDataEnvironment.constructAndRegisterCustomPep("my-pep", MyPep.class);
    });
  }

  @Test
  public void whenConstructAndRegisterCustomPepWithInvalidPepInterface_InitializationExceptionShoudBeThrown()
      throws InitializationException {
    assertThrows(InitializationException.class, () -> {
      this.myDataEnvironment.constructAndRegisterCustomPep("my-pep", InvalidPep.class);
    });
  }

  @Test
  public void testGetTimezone() throws Exception {
    if (this.operationalMode == OperationalMode.CLOUD) {
      // in cloud mode, request from the cloud pmp
      assertTrue(this.pmp instanceof IManagementService);
      final IManagementService ms = (IManagementService) this.pmp;
      final ZoneId expectedZoneId = ZoneId.of("Europe/Berlin");
      Mockito.when(ms.getZoneId(this.solutionId)).thenReturn(expectedZoneId);
      assertEquals(expectedZoneId, this.myDataEnvironment.getTimezone());
      Mockito.verify(ms).getZoneId(this.solutionId);
    } else {
      // in a local mode, take the property
      assertEquals(this.zoneId, this.myDataEnvironment.getTimezone());
      Mockito.verifyNoMoreInteractions(this.pmp);
    }
  }

  @Test
  void givenWeAreInAModeWithSyncServiceAvailable_whenRegisterPip_thenPushPipIsCalledOnSyncService()
      throws Exception {
    if (this.syncService != null) {
      this.myDataEnvironment.registerLocalPip("my-pip", new DummyServiceWithOneMethod());
      Mockito.verify(this.syncService).pushPip(any(PipComponentInformation.class));
    } else {
      LOG.info("skip test as condition does not match");
    }
  }

  @Test
  void givenWeAreInAModeWithSyncServiceAvailable_whenRegisterPxp_thenPushPxpIsCalledOnSyncService()
      throws Exception {
    if (this.syncService != null) {
      this.myDataEnvironment.registerLocalPxp("my-pxp", new DummyServiceWithOneMethod());
      Mockito.verify(this.syncService).pushPxp(any(PxpComponentInformation.class));
    } else {
      LOG.info("skip test as condition does not match");
    }
  }

  @Test
  void givenWeAreInAModeWithSyncServiceAvailable_whenRegisterPep_thenPushPepIsCalledOnSyncService()
      throws Exception {
    if (this.syncService != null) {
      final ComponentId componentId = new ComponentId("urn:component:test:pep:my-pep");
      Mockito.when(this.pmp.addPep(any(PepComponentInformation.class))).thenReturn(componentId);
      this.myDataEnvironment.constructAndRegisterCustomPep("my-pep", MyPep.class);
      Mockito.verify(this.syncService).pushPep(
          ArgumentMatchers.argThat(argument -> componentId.equals(argument.getComponentId())));
    } else {
      LOG.info("skip test as condition does not match");
    }
  }

  interface MyPep {
    @EventSpecification(action = "bla")
    Observable<Event> bla(@EventParameter(name = "text") String text);
  }

  @PepServiceDescription(componentName = "ganz-was-anderes")
  interface MyPepWithAnnotation {
    @EventSpecification(action = "bla")
    Observable<Event> bla(@EventParameter(name = "text") String text);
  }

  interface InvalidPep {
    Observable<Event> bla(@EventParameter(name = "text") String text);
  }
}
