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

import de.fraunhofer.iese.mydata.client.ClientId;
import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.ComponentType;
import de.fraunhofer.iese.mydata.component.connector.ConnectorFactory;
import de.fraunhofer.iese.mydata.component.connector.OAuthCredentials;
import de.fraunhofer.iese.mydata.component.information.PdpComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PepComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PipComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PxpComponentInformation;
import de.fraunhofer.iese.mydata.component.information.method.MethodInterfaceDescription;
import de.fraunhofer.iese.mydata.component.interfaces.IBasicManagementService;
import de.fraunhofer.iese.mydata.component.interfaces.IManagementService;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyDecisionPoint;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyEnforcementPoint;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyExecutionPoint;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyInformationPoint;
import de.fraunhofer.iese.mydata.connector.java.PdpJavaConnector;
import de.fraunhofer.iese.mydata.exception.ConflictingResourceException;
import de.fraunhofer.iese.mydata.exception.InitializationException;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.exception.ResourceUpdateException;
import de.fraunhofer.iese.mydata.internal.ComponentInstanceStore;
import de.fraunhofer.iese.mydata.internal.IComponentInstanceStore;
import de.fraunhofer.iese.mydata.internal.IMyDataEnvironmentFullFace;
import de.fraunhofer.iese.mydata.internal.PipWrapper;
import de.fraunhofer.iese.mydata.internal.PxpWrapper;
import de.fraunhofer.iese.mydata.pdp.PolicyDecisionPoint;
import de.fraunhofer.iese.mydata.pep.DefaultPolicyEnforcementPoint;
import de.fraunhofer.iese.mydata.pep.common.ModifierMethod;
import de.fraunhofer.iese.mydata.pep.enforce.JsonPathDecisionEnforcer;
import de.fraunhofer.iese.mydata.pmp.PolicyManagementPoint;
import de.fraunhofer.iese.mydata.pmp.synchronizer.CloudSynchronizer;
import de.fraunhofer.iese.mydata.pmp.synchronizer.FileSynchronizer;
import de.fraunhofer.iese.mydata.pmp.synchronizer.ISyncService;
import de.fraunhofer.iese.mydata.pmp.synchronizer.SynchronizerException;
import de.fraunhofer.iese.mydata.policy.event.history.IEventRepository;
import de.fraunhofer.iese.mydata.reactive.RxPepFactory;
import de.fraunhofer.iese.mydata.reactive.common.PepType;
import de.fraunhofer.iese.mydata.reactive.common.RxPep;
import de.fraunhofer.iese.mydata.solution.SolutionId;
import de.fraunhofer.iese.mydata.util.DependencyCheck;
import de.fraunhofer.iese.mydata.util.ModifierMethodDiscoveryUtil;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.jspecify.annotations.Nullable;

final class MyDataEnvironmentFullFace implements IMyDataEnvironmentFullFace {
  private static final String FAILED_TO_PUBLISH_COMPONENT_INFORMATION_LOG_MESSAGE = "Failed to publish componentInformation of {} via SyncService";

  private static final String JAVA_URL_FORMAT_FOR_LOCAL_COMPONENTS = "java://%s/%s/local-component/%s";

  private static final Logger LOG = LoggerFactory.getLogger(MyDataEnvironmentFullFace.class);

  private final String environmentId;

  private final AtomicBoolean initializing;

  private final AtomicBoolean active;

  private final IComponentInstanceStore componentInstanceStore;

  private final AtomicReference<ISyncService> syncServiceReference;

  private final AtomicReference<OperationalMode> operationalModeReference;

  private final ConnectorFactory connectorFactory;

  private SolutionId solutionId;

  private IBasicManagementService pmp;

  private IPolicyDecisionPoint pdp;

  private IPolicyEnforcementPoint pep;

  /**
   * Holds the timezone configured for the instance. Null when in Cloud-Mode as the timezone needs
   * to be queried from the Cloud-PMP.
   */
  private ZoneId localTimezone;

  /**
   * This constructor is not public by design, use the {@link MyDataEnvironmentManager} to create
   * {@link IMyDataEnvironment} instances.
   *
   * @param environmentId    a unique identifier that is used to identify the IMyDataEnvironment
   *                           instance amongst others
   * @param connectorFactory a connectorFactory instance to enable communication
   */
  // TODO maybe we want to get rid of the initializer-Methods and switch to
  //  meaningful constructors that take care of it
  MyDataEnvironmentFullFace(String environmentId, ConnectorFactory connectorFactory) {
    this(Objects.requireNonNull(environmentId), false, false, new ComponentInstanceStore(), null,
        null, Objects.requireNonNull(connectorFactory), null, null, null, null, null);
  }

  /**
   * This constructor is for internal use only. It can also be used to unit test parts of the
   * MyDataEnvironmentFullFace implementation. When testing other parts of the software, mock the
   * IMyDataEnvironment/IMyDataEnvironmentFullFace interface!
   *
   * @param  environmentId          an identifier to identify the IMyDataEnvironment.
   * @param  initializing           flag that someone started the initialization.
   * @param  active                 flag that initialization is done and the IMyDataEnvironment is
   *                                  active (can be used)
   * @param  componentInstanceStore store for component instances
   * @param  syncService            the syncService associated to this IMyDataEnvironment
   * @param  operationalMode        the operationalMode of this IMyDataEnvironment
   * @param  connectorFactory       the connectorFactory instance to use internally
   * @param  solutionId             the solutionId this IMyDataEnvironment instance will be
   *                                  associated with
   * @param  pmp                    PMP reference
   * @param  pdp                    PDP reference
   * @param  pep                    reference to the default PEP
   * @param  localTimezone          the local timezone
   * @hidden
   */
  @VisibleForTesting
  MyDataEnvironmentFullFace(String environmentId, boolean initializing, boolean active,
      IComponentInstanceStore componentInstanceStore, ISyncService syncService,
      OperationalMode operationalMode, ConnectorFactory connectorFactory, SolutionId solutionId,
      IBasicManagementService pmp, IPolicyDecisionPoint pdp, IPolicyEnforcementPoint pep,
      ZoneId localTimezone) {
    this.environmentId = environmentId;
    this.initializing = new AtomicBoolean(initializing);
    this.active = new AtomicBoolean(active);
    this.componentInstanceStore = componentInstanceStore;
    this.syncServiceReference = new AtomicReference<>(syncService);
    this.operationalModeReference = new AtomicReference<>(operationalMode);
    this.connectorFactory = connectorFactory;
    this.solutionId = solutionId;
    this.pmp = pmp;
    this.pdp = pdp;
    this.pep = pep;
    this.localTimezone = localTimezone;
  }

  private static JsonPathDecisionEnforcer getDecisionEnforcer() {
    final JsonPathDecisionEnforcer enf = new JsonPathDecisionEnforcer();

    final Set<Class<? extends ModifierMethod>> modifierClasses = ModifierMethodDiscoveryUtil.findAll();

    for (final Class<? extends ModifierMethod> clazz : modifierClasses) {
      try {
        enf.addModificationMethod(clazz.getDeclaredConstructor().newInstance());
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
          | InvocationTargetException | NoSuchMethodException | SecurityException e) {
        // TODO how should we handle this? throw Exception or just write a log message?
        LOG.info("Exception in MyDataEnvironmentFullFace::getDecisionEnforcer: {}", e.getMessage());
      }
    }

    return enf;
  }

  private static InitializationException getAsInitializationException(Exception e) {
    if (e instanceof InitializationException) {
      return (InitializationException) e;
    } else {
      return new InitializationException(e.getMessage(), e);
    }
  }

  private URI constructLocalComponentJavaUrl(ComponentId componentId)
      throws InvalidEntityException {
    MyDataEntity.validateAndNullCheck(componentId);
    return URI.create(String.format(JAVA_URL_FORMAT_FOR_LOCAL_COMPONENTS,
        MyDataEnvironmentManager.class.getCanonicalName(), this.environmentId,
        componentId.getUrn()));
  }

  private void assureActive() {
    if (!this.active.get()) {
      throw new IllegalStateException("IMyDataEnvironment with id "
          + this.environmentId
          + " has not been initialized (you have to do this before using it) or has been replaced by another instance (so this instance is no longer valid).");
    }
  }

  @Override
  public String getEnvironmentId() {
    return this.environmentId;
  }

  @Override
  public SolutionId getSolutionId() {
    this.assureActive();
    return this.solutionId;
  }

  @Override
  public OperationalMode getOperationalMode() {
    return this.operationalModeReference.get();
  }

  @Override
  public ComponentId registerPep(PepComponentInformation pepComponentInformation)
      throws IOException, ResourceUpdateException, ConflictingResourceException,
      InvalidEntityException, NoSuchEntityException {
    this.assureActive();
    if (LOG.isInfoEnabled()) {
      LOG.info("Trying to register this PEP: {}", pepComponentInformation.toJson(true));
    }
    MyDataEntity.validateAndNullCheck(pepComponentInformation);
    // TODO check solution matches
    final ComponentId componentIdToReturn;
    if (this.pmp.pepExists(pepComponentInformation.getComponentId())) {
      // TODO only update when there are changes?
      componentIdToReturn = this.pmp.updatePep(pepComponentInformation);
    } else {
      componentIdToReturn = this.pmp.addPep(pepComponentInformation);
    }
    final ISyncService syncService = this.syncServiceReference.get();
    if (null != syncService) {
      try {
        syncService.pushPep(pepComponentInformation);
      } catch (final SynchronizerException e) {
        LOG.warn(FAILED_TO_PUBLISH_COMPONENT_INFORMATION_LOG_MESSAGE,
            pepComponentInformation.getComponentId(), e);
      }
    }
    return componentIdToReturn;
  }

  @Override
  public ComponentId registerUnmanagedPip(PipComponentInformation pipComponentInformation)
      throws IOException, ResourceUpdateException, ConflictingResourceException,
      InvalidEntityException, NoSuchEntityException {
    this.assureActive();
    if (LOG.isInfoEnabled()) {
      LOG.info("Trying to register this PIP: {}", pipComponentInformation.toJson(true));
    }
    MyDataEntity.validateAndNullCheck(pipComponentInformation);
    // TODO check solution matches
    final ComponentId componentIdToReturn;
    if (this.pmp.pipExists(pipComponentInformation.getComponentId())) {
      // TODO only update when there are changes?
      componentIdToReturn = this.pmp.updatePip(pipComponentInformation);
    } else {
      componentIdToReturn = this.pmp.addPip(pipComponentInformation);
    }
    final ISyncService syncService = this.syncServiceReference.get();
    if (null != syncService) {
      try {
        syncService.pushPip(pipComponentInformation);
      } catch (final SynchronizerException e) {
        LOG.warn(FAILED_TO_PUBLISH_COMPONENT_INFORMATION_LOG_MESSAGE,
            pipComponentInformation.getComponentId(), e);
      }
    }
    return componentIdToReturn;
  }

  @Override
  public ComponentId registerManagedPip(ComponentId componentId, Object instance,
      List<URI> listOfUrl) throws InvalidEntityException, ResourceUpdateException,
      ConflictingResourceException, IOException, NoSuchEntityException {
    this.assureActive();
    MyDataEntity.validateAndNullCheck(componentId);
    // TODO check solution matches
    Objects.requireNonNull(instance);
    Objects.requireNonNull(listOfUrl);
    final PipWrapper pipWrapper = new PipWrapper(componentId, instance);
    final List<MethodInterfaceDescription> methodInterfaceDescriptionSet = pipWrapper
        .getMethodInterfaceDescriptions();
    if (methodInterfaceDescriptionSet.isEmpty()) {
      throw new InvalidEntityException(
          "Pip with id " + componentId + " has no declared action methods.");
    }
    final PipComponentInformation pipComponentInformation = new PipComponentInformation(componentId,
        listOfUrl, methodInterfaceDescriptionSet);
    this.componentInstanceStore.addPipInstance(componentId, pipWrapper);
    try {
      return this.registerUnmanagedPip(pipComponentInformation);
    } catch (final Exception e) {
      this.componentInstanceStore.removePipInstance(componentId);
      throw e;
    }
  }

  @Override
  public ComponentId registerManagedPip(String componentName, Object instance, List<URI> listOfUrl)
      throws InvalidEntityException, ResourceUpdateException, ConflictingResourceException,
      IOException, NoSuchEntityException {
    this.assureActive();
    final ComponentId componentId = ComponentType.PIP.getComponentId(this.solutionId,
        componentName);
    return this.registerManagedPip(componentId, instance, listOfUrl);
  }

  @Override
  public ComponentId registerLocalPip(ComponentId componentId, Object instance)
      throws IOException, InvalidEntityException, ResourceUpdateException,
      ConflictingResourceException, NoSuchEntityException {
    this.assureActive();
    if (this.isLocalComponentAllowed()) {
      return this.registerManagedPip(componentId, instance,
          Collections.singletonList(this.constructLocalComponentJavaUrl(componentId)));
    } else {
      throw new ResourceUpdateException("Local components are not allowed in CLOUD-mode");
    }
  }

  @Override
  public ComponentId registerLocalPip(String componentName, Object instance)
      throws IOException, InvalidEntityException, ResourceUpdateException,
      ConflictingResourceException, NoSuchEntityException {
    this.assureActive();
    final ComponentId componentId = ComponentType.PIP.getComponentId(this.solutionId,
        componentName);
    return this.registerLocalPip(componentId, instance);
  }

  @Override
  public ComponentId registerUnmanagedPxp(PxpComponentInformation pxpComponentInformation)
      throws IOException, ResourceUpdateException, ConflictingResourceException,
      InvalidEntityException, NoSuchEntityException {
    this.assureActive();
    if (LOG.isInfoEnabled()) {
      LOG.info("Trying to register this PXP: {}", pxpComponentInformation.toJson(true));
    }
    MyDataEntity.validateAndNullCheck(pxpComponentInformation);
    // TODO check solution matches
    final ComponentId componentIdToReturn;
    if (this.pmp.pxpExists(pxpComponentInformation.getComponentId())) {
      // TODO only update when there are changes?
      componentIdToReturn = this.pmp.updatePxp(pxpComponentInformation);
    } else {
      componentIdToReturn = this.pmp.addPxp(pxpComponentInformation);
    }
    final ISyncService syncService = this.syncServiceReference.get();
    if (null != syncService) {
      try {
        syncService.pushPxp(pxpComponentInformation);
      } catch (final SynchronizerException e) {
        LOG.warn(FAILED_TO_PUBLISH_COMPONENT_INFORMATION_LOG_MESSAGE,
            pxpComponentInformation.getComponentId(), e);
      }
    }
    return componentIdToReturn;
  }

  @Override
  public ComponentId registerManagedPxp(ComponentId componentId, Object instance,
      List<URI> listOfUrl) throws InvalidEntityException, ResourceUpdateException,
      ConflictingResourceException, NoSuchEntityException, IOException {
    this.assureActive();
    MyDataEntity.validateAndNullCheck(componentId);
    // TODO check solution matches
    Objects.requireNonNull(instance);
    Objects.requireNonNull(listOfUrl);
    final PxpWrapper pxpWrapper = new PxpWrapper(componentId, instance);
    final List<MethodInterfaceDescription> methodInterfaceDescriptionSet = pxpWrapper
        .getMethodInterfaceDescriptions();
    if (methodInterfaceDescriptionSet.isEmpty()) {
      throw new InvalidEntityException(
          "Pxp with id " + componentId + " has no declared action methods.");
    }
    final PxpComponentInformation pxpComponentInformation = new PxpComponentInformation(componentId,
        listOfUrl, methodInterfaceDescriptionSet);
    this.componentInstanceStore.addPxpInstance(componentId, pxpWrapper);
    try {
      return this.registerUnmanagedPxp(pxpComponentInformation);
    } catch (final Exception e) {
      this.componentInstanceStore.removePxpInstance(componentId);
      throw e;
    }
  }

  @Override
  public ComponentId registerManagedPxp(String componentName, Object instance, List<URI> setOfUrl)
      throws InvalidEntityException, ResourceUpdateException, ConflictingResourceException,
      IOException, NoSuchEntityException {
    this.assureActive();
    final ComponentId componentId = ComponentType.PXP.getComponentId(this.solutionId,
        componentName);
    return this.registerManagedPxp(componentId, instance, setOfUrl);
  }

  private boolean isLocalComponentAllowed() {
    return OperationalMode.CLOUD != this.getOperationalMode();
  }

  @Override
  public ComponentId registerLocalPxp(ComponentId componentId, Object instance)
      throws IOException, InvalidEntityException, ResourceUpdateException,
      ConflictingResourceException, NoSuchEntityException {
    this.assureActive();
    if (this.isLocalComponentAllowed()) {
      return this.registerManagedPxp(componentId, instance,
          Collections.singletonList(this.constructLocalComponentJavaUrl(componentId)));
    } else {
      throw new ResourceUpdateException("Local components are not allowed in CLOUD-mode");
    }
  }

  @Override
  public ComponentId registerLocalPxp(String componentName, Object instance)
      throws IOException, InvalidEntityException, ResourceUpdateException,
      ConflictingResourceException, NoSuchEntityException {
    this.assureActive();
    final ComponentId componentId = ComponentType.PXP.getComponentId(this.solutionId,
        componentName);
    return this.registerLocalPxp(componentId, instance);
  }

  @Override
  public Optional<IPolicyInformationPoint> getManagedPip(final ComponentId componentId) {
    this.assureActive();
    return this.componentInstanceStore.getPipInstanceByComponentId(componentId);
  }

  @Override
  public Optional<IPolicyExecutionPoint> getManagedPxp(final ComponentId componentId) {
    this.assureActive();
    return this.componentInstanceStore.getPxpInstanceByComponentId(componentId);
  }

  @Override
  public <T> T constructAndRegisterCustomPep(ComponentId componentId, Class<T> interfaceOfPep)
      throws InitializationException {
    this.assureActive();
    try {
      MyDataEntity.validateAndNullCheck(componentId);
      // TODO check solution matches
      final PepType apiDocumentationType = RxPepFactory.findAPIDocumentationType(interfaceOfPep);
      if (PepType.REACTIVE == apiDocumentationType) {
        final RxPep<T> rxPep = RxPepFactory.createRxPep(this, componentId, interfaceOfPep);
        final boolean registered = rxPep.doRegisterAtPMP().blockingFirst();
        if (registered) {
          return rxPep.createInstanceAPI();
        } else {
          throw new InitializationException("registration failed");
        }
      } else {
        throw new InitializationException("Unsupported PepType: " + apiDocumentationType);
      }
    } catch (final InitializationException e) {
      throw e;
    } catch (final Exception e) {
      throw new InitializationException(e.getMessage(), e);
    }
  }

  @Override
  public <T> T constructAndRegisterCustomPep(String componentName, Class<T> interfaceOfPep)
      throws InitializationException {
    this.assureActive();
    try {
      final ComponentId componentId = ComponentType.PEP.getComponentId(this.solutionId,
          componentName);
      return this.constructAndRegisterCustomPep(componentId, interfaceOfPep);
    } catch (final InitializationException e) {
      throw e;
    } catch (final Exception e) {
      throw new InitializationException(e.getMessage(), e);
    }
  }

  @Override
  public ZoneId getTimezone() {
    this.assureActive();
    if (this.localTimezone == null && this.pmp instanceof IManagementService) {
      try {
        return ((IManagementService) this.pmp).getZoneId(this.solutionId);
      } catch (IOException | InvalidEntityException | NoSuchEntityException e) {
        LOG.error(e.getMessage(), e);
      }
      return null; // TODO error handling
    } else {
      return this.localTimezone;
    }
  }

  @Override
  public IMyDataEnvironment initializeCloud(SolutionId solutionId, URI cloudPmpUrl,
      OAuthCredentials authentication) throws InitializationException {
    try {
      if (this.initializing.compareAndSet(false, true)) {
        LOG.info("initializeCloud");
        this.operationalModeReference.set(OperationalMode.CLOUD);
        MyDataEntity.validateAndNullCheck(solutionId);
        this.solutionId = solutionId;
        MyDataEntity.validateAndNullCheck(authentication);
        this.assureSolutionIdAndClientIdMatch(solutionId, authentication.getClientId());
        this.pmp = this.connectorFactory == null ? null
            : this.connectorFactory.getManagementService(URI.create(cloudPmpUrl + "/ws"),
                authentication);
        if (this.pmp == null) {
          throw new IOException("Cannot establish connection to the PMP");
        }
        final PdpComponentInformation pdpComponentInformation = this
            .getPdpComponentInformationFromPmp();
        if (null != pdpComponentInformation) {
          MyDataEntity.validateAndNullCheck(pdpComponentInformation);
          this.pdp = this.connectorFactory.getPdp(pdpComponentInformation, authentication);
        } else {
          this.pdp = null;
        }
        if (this.pdp == null) {
          LOG.warn("There is no connection to the PDP.");
        }
        this.pep = new DefaultPolicyEnforcementPoint(this, getDecisionEnforcer());
        this.active.set(true);
        return this;
      } else {
        throw new IllegalStateException("Already initialized");
      }
    } catch (final Exception e) {
      throw MyDataEnvironmentFullFace.getAsInitializationException(e);
    }

  }

  @Nullable
  private PdpComponentInformation getPdpComponentInformationFromPmp() throws IOException {
    try {
      return this.pmp.getPdp();
    } catch (final NoSuchEntityException e) {
      LOG.info("PdpComponentInformation not available", e);
      return null;
    }
  }

  @Override
  public IMyDataEnvironment initializeLocalWithCloudSync(SolutionId solutionId, URI cloudPmpUrl,
      OAuthCredentials authentication, String timezone, boolean cacheEnabled,
      String policyCachePath, String timerCachePath, String maxPolicyAndTimerAge,
      String syncSchedule, boolean masterClient, int numPdpThreads, boolean whitelistModeEnabled,
      @Nullable IEventRepository eventRepository) throws InitializationException {
    try {
      if (this.initializing.compareAndSet(false, true)) {
        LOG.info("initializeLocalWithCloudSync");
        this.operationalModeReference.set(OperationalMode.LOCAL_WITH_CLOUD_SYNC);
        MyDataEntity.validateAndNullCheck(solutionId);
        this.solutionId = solutionId;
        this.localTimezone = ZoneId.of(timezone);

        MyDataEntity.validateAndNullCheck(authentication);
        this.assureSolutionIdAndClientIdMatch(solutionId, authentication.getClientId());
        // TODO MS vs Basic
        final IManagementService managementService = this.connectorFactory == null ? null
            : this.connectorFactory.getManagementService(URI.create(cloudPmpUrl + "/ws"),
                authentication);
        if (null == managementService) {
          throw new InitializationException("No connector for MS");
        }

        final ComponentId specialPmpComponentIdForLocalWithCloudSync = this.getLocalPmpComponentId(
            solutionId, ClientId.getClientIdentifier(authentication.getClientId()));

        this.initializeLocalPdp(solutionId, specialPmpComponentIdForLocalWithCloudSync,
            numPdpThreads, whitelistModeEnabled, eventRepository);

        // TODO maybe also apply to other op-modes
        this.pdp.setFailureMode(true); // initial in failure mode, restore
        // through successful sync

        final PdpComponentInformation pdpComponentInformation = this
            .getLocalPdpComponentInformation(solutionId);
        this.pmp = new PolicyManagementPoint(specialPmpComponentIdForLocalWithCloudSync,
            pdpComponentInformation, this.pdp, this.localTimezone, this.getFreshTimerScheduler(),
            this.connectorFactory);
        this.pep = new DefaultPolicyEnforcementPoint(this, getDecisionEnforcer());
        { // cloud-sync
          try {
            final ISyncService syncService = new CloudSynchronizer(Instant::now,
                authentication.getClientId(), this.pmp, this.pmp, this.pdp, managementService,
                cacheEnabled, policyCachePath, timerCachePath, maxPolicyAndTimerAge, syncSchedule,
                masterClient, this.getFreshSyncScheduler());
            this.syncServiceReference.set(syncService);
            syncService.start();
            LOG.info("Cloud sync initialized");
          } catch (final SchedulerException | ParseException | SynchronizerException e) {
            LOG.error("Unable to start sync process", e);
            throw new InitializationException(e.getMessage(), e);
          }
        }
        this.active.set(true);
        return this;
      } else {
        throw new IllegalStateException("Already initialized");
      }
    } catch (final Exception e) {
      throw MyDataEnvironmentFullFace.getAsInitializationException(e);
    }
  }

  @Override
  public IMyDataEnvironment initializeLocalWithFileSync(SolutionId solutionId, String timezone,
      String fileSyncPath, int numPdpThreads, boolean whitelistModeEnabled,
      @Nullable IEventRepository eventRepository) throws InitializationException {
    try {
      if (this.initializing.compareAndSet(false, true)) {
        LOG.info("initializeLocalWithFileSync");
        this.operationalModeReference.set(OperationalMode.LOCAL_WITH_FILE_SYNC);
        MyDataEntity.validateAndNullCheck(solutionId);
        this.solutionId = solutionId;
        this.localTimezone = ZoneId.of(timezone);
        final ComponentId pmpComponentId = this.getLocalPmpComponentId(solutionId);
        this.initializeLocalPdp(solutionId, pmpComponentId, numPdpThreads, whitelistModeEnabled,
            eventRepository);
        this.pdp.setFailureMode(true); // initial in failure mode, restore
        // through successful sync
        final PdpComponentInformation pdpComponentInformation = this
            .getLocalPdpComponentInformation(solutionId);
        this.pmp = new PolicyManagementPoint(pmpComponentId, pdpComponentInformation, this.pdp,
            this.localTimezone, this.getFreshTimerScheduler(), this.connectorFactory);
        this.pep = new DefaultPolicyEnforcementPoint(this, getDecisionEnforcer());
        { // file-sync
          if (StringUtils.isBlank(fileSyncPath)) {
            throw new InitializationException("fileSyncPath must not be blank for mode "
                + OperationalMode.LOCAL_WITH_FILE_SYNC.name());
          } else {
            try {
              final ISyncService syncService = new FileSynchronizer(fileSyncPath, this.pmp,
                  this.pmp, this.pdp);
              this.syncServiceReference.set(syncService);
              syncService.start();
              LOG.info("File sync initialized");
            } catch (final SynchronizerException e) {
              LOG.error("Unable to start sync process", e);
              throw new InitializationException(e.getMessage(), e);
            }
          }
        }
        this.active.set(true);
        return this;
      } else {
        throw new IllegalStateException("Already initialized");
      }
    } catch (final Exception e) {
      throw MyDataEnvironmentFullFace.getAsInitializationException(e);
    }

  }

  private ComponentId getLocalPmpComponentId(SolutionId solutionId) throws InvalidEntityException {
    return this.getLocalPmpComponentId(solutionId, "pmp");
  }

  private ComponentId getLocalPmpComponentId(SolutionId solutionId, String name)
      throws InvalidEntityException {
    return ComponentType.PMP.getComponentId(solutionId, name);
  }

  private ComponentId getLocalPdpComponentId(SolutionId solutionId) throws InvalidEntityException {
    return ComponentType.PDP.getComponentId(solutionId, "pdp");
  }

  @Override
  public IMyDataEnvironment initializeLocal(SolutionId solutionId, String timezone,
      int numPdpThreads, boolean whitelistModeEnabled, @Nullable IEventRepository eventRepository)
      throws InitializationException {
    try {
      if (this.initializing.compareAndSet(false, true)) {
        LOG.info("initializeLocal");
        this.operationalModeReference.set(OperationalMode.LOCAL);
        MyDataEntity.validateAndNullCheck(solutionId);
        this.solutionId = solutionId;
        this.localTimezone = ZoneId.of(timezone);
        final ComponentId pmpComponentId = this.getLocalPmpComponentId(solutionId);
        this.initializeLocalPdp(solutionId, pmpComponentId, numPdpThreads, whitelistModeEnabled,
            eventRepository);
        final PdpComponentInformation pdpComponentInformation = this
            .getLocalPdpComponentInformation(solutionId);
        this.pmp = new PolicyManagementPoint(pmpComponentId, pdpComponentInformation, this.pdp,
            this.localTimezone, this.getFreshTimerScheduler(), this.connectorFactory);
        this.pep = new DefaultPolicyEnforcementPoint(this, getDecisionEnforcer());
        this.active.set(true);
        return this;
      } else {
        throw new IllegalStateException("Already initialized");
      }
    } catch (final Exception e) {
      throw MyDataEnvironmentFullFace.getAsInitializationException(e);
    }
  }

  private void assureSolutionIdAndClientIdMatch(SolutionId solutionId, ClientId clientId)
      throws InvalidEntityException {
    MyDataEntity.validateAndNullCheck(solutionId);
    MyDataEntity.validateAndNullCheck(clientId);
    if (!SolutionId.fromClientId(clientId).equals(solutionId)) {
      throw new RuntimeException("Invalid configuration: SolutionId and ClientId do not match.");
    }
  }

  private void initializeLocalPdp(SolutionId solutionId, ComponentId pmpComponentId,
      int numPdpThreads, boolean whitelistModeEnabled, @Nullable IEventRepository eventRepository)
      throws InitializationException, InvalidEntityException {
    LOG.info("initializeLocalPdp({},{},{})", numPdpThreads, whitelistModeEnabled,
        eventRepository == null ? "null" : eventRepository.getClass().getCanonicalName());

    if (!DependencyCheck.pdpAvailable()) {
      throw new InitializationException(
          "PDP dependency is missing, unable to initialize local PDP");
    }

    final PolicyDecisionPoint pdp = PolicyDecisionPoint.getInstance();
    // TODO get rid of singleton and multiple initialization
    // TODO must the solution of pdp component id match the solution of the pmp
    // component id?
    final ComponentId pdpComponentId = this.getLocalPdpComponentId(solutionId);
    final boolean pdpInitOk = pdp.initialize(pdpComponentId,
        this.constructLocalComponentJavaUrl(pmpComponentId), numPdpThreads, whitelistModeEnabled,
        eventRepository);

    if (!pdpInitOk) {
      throw new InitializationException("Initialization of local PDP failed");
    }
    this.pdp = new PdpJavaConnector(pdp); // TODO wrapped because of event
    // should not be shared with PDP (see
    // IND2UCE-2931 and IND2UCE-2250)
  }

  private PdpComponentInformation getLocalPdpComponentInformation(SolutionId solutionId)
      throws InvalidEntityException {
    final ComponentId pdpComponentId = this.getLocalPdpComponentId(solutionId);
    final List<URI> ts = new ArrayList<>();
    ts.add(this.constructLocalComponentJavaUrl(pdpComponentId));
    return new PdpComponentInformation(pdpComponentId, ts);
  }

  private Scheduler getFreshTimerScheduler() throws SchedulerException {
    // TODO refactor and make it configurable
    final String schedulerName = this.getTimerSchedulerName();
    final Properties properties = new Properties();
    properties.put("org.quartz.scheduler.instanceName", schedulerName);
    properties.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
    properties.put("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
    properties.put("org.quartz.threadPool.threadsInheritContextClassLoaderOfInitializingThread",
        "true");
    properties.put("org.quartz.threadPool.threadCount", "1");
    return this.getFreshScheduler(schedulerName, properties);
  }

  private Scheduler getFreshSyncScheduler() throws SchedulerException {
    // TODO refactor and make it configurable
    final String schedulerName = this.getSyncSchedulerName();
    final Properties properties = new Properties();
    properties.put("org.quartz.scheduler.instanceName", schedulerName);
    properties.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
    properties.put("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
    properties.put("org.quartz.threadPool.threadsInheritContextClassLoaderOfInitializingThread",
        "true");
    properties.put("org.quartz.threadPool.threadCount", "1");
    return this.getFreshScheduler(schedulerName, properties);
  }

  private Scheduler getFreshScheduler(String schedulerName, Properties schedulerConfig)
      throws SchedulerException {
    LOG.debug("Going to provide scheduler {}", schedulerName);
    this.shutdownSchedulerIfExists(schedulerName);
    final StdSchedulerFactory schedulerFactory = new StdSchedulerFactory();
    schedulerFactory.initialize(schedulerConfig);
    return schedulerFactory.getScheduler();
  }

  private String getSyncSchedulerName() {
    return "mydata-sync-" + this.environmentId;
  }

  private String getTimerSchedulerName() {
    return "mydata-timer-" + this.environmentId;
  }

  private void shutdownSyncSchedulerIfExists() throws SchedulerException {
    this.shutdownSchedulerIfExists(this.getSyncSchedulerName());
  }

  private void shutdownTimerSchedulerIfExists() throws SchedulerException {
    this.shutdownSchedulerIfExists(this.getTimerSchedulerName());
  }

  private void shutdownSchedulerIfExists(String schedulerName) throws SchedulerException {
    final StdSchedulerFactory schedulerFactory = new StdSchedulerFactory();
    final Scheduler scheduler = schedulerFactory.getScheduler(schedulerName);
    if (null != scheduler) {
      LOG.debug("Going to shutdown scheduler {}", schedulerName);
      scheduler.shutdown();
    }
  }

  @Override
  public IBasicManagementService getPmp() {
    this.assureActive();
    return this.pmp;
  }

  @Override
  public IPolicyEnforcementPoint getPep() {
    this.assureActive();
    return this.pep;
  }

  @Override
  public Optional<IPolicyDecisionPoint> getPdp() {
    this.assureActive();
    return Optional.ofNullable(this.pdp);
  }

  @Override
  public void destroy() {
    LOG.warn("--- DESTROY CALLED on IMyDataEnvironment with id {} ---", this.environmentId);
    this.active.set(false);
    final ISyncService syncService = this.syncServiceReference.getAndSet(null);
    if (null != syncService) {
      try {
        syncService.stop();
      } catch (final SynchronizerException e) {
        LOG.error(e.getMessage(), e);
      }
    }
    try {
      this.shutdownSyncSchedulerIfExists();
    } catch (final SchedulerException e) {
      LOG.error(e.getMessage(), e);
    }
    if (this.operationalModeReference.get() != OperationalMode.CLOUD) {
      try {
        if (this.pmp != null) {
          this.pmp.reset(); // revoke timers
        }
      } catch (final IOException | NoSuchEntityException e) {
        LOG.error(e.getMessage(), e);
      }
      this.pmp = null;
      try {
        this.shutdownTimerSchedulerIfExists();
      } catch (final SchedulerException e) {
        LOG.error(e.getMessage(), e);
      }
      try {
        if (this.pdp != null) {
          this.pdp.reset(); // clear/renew threadPools
        }
      } catch (final IOException | NoSuchEntityException e) {
        LOG.error(e.getMessage(), e);
      }
      this.pdp = null;
      this.pep = null;
    } else {
      // TODO deregister components?
    }
    this.componentInstanceStore.clear();
    // TODO further cleanup? e.g. the old PDP and old PMP? There are
    // threads/timers/threadpools still running...
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("MyDataEnvironmentFullFace{");
    sb.append("environmentId='").append(this.environmentId).append('\'');
    sb.append(", active=").append(this.active.get());
    sb.append(", operationalModeReference=").append(this.operationalModeReference.get());
    sb.append(", solutionId=").append(this.solutionId);
    sb.append(", localTimezone=").append(this.localTimezone);
    sb.append('}');
    return sb.toString();
  }
}
