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

package de.fraunhofer.iese.mydata.pdp;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.connector.ConnectorFactory;
import de.fraunhofer.iese.mydata.component.health.HealthStatus;
import de.fraunhofer.iese.mydata.component.health.Status;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyDecisionPoint;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.ResourceUpdateException;
import de.fraunhofer.iese.mydata.pdp.interfaces.IPolicyStore;
import de.fraunhofer.iese.mydata.pdp.language.model.Policy;
import de.fraunhofer.iese.mydata.pdp.language.parser.PolicyTransformer;
import de.fraunhofer.iese.mydata.pdp.utils.ConnectorCache;
import de.fraunhofer.iese.mydata.pdp.utils.PipOperatorCache;
import de.fraunhofer.iese.mydata.pdp.utils.PolicyStoreMapMatching;
import de.fraunhofer.iese.mydata.policy.PolicyDeployableGroup;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.policy.decision.AuthorizationDecision;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.event.history.IEventRepository;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;

import java.io.IOException;
import java.net.URI;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;
import jakarta.validation.groups.Default;

/**
 * The Class PolicyDecisionPoint.
 */
public class PolicyDecisionPoint implements IPolicyDecisionPoint {
  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PolicyDecisionPoint.class);

  /**
   * The Constant NUM_OPTIONAL_EXECUTEACTIONS_EXECUTOR_THREADS.
   */
  private static final int NUM_OPTIONAL_EXECUTEACTIONS_EXECUTOR_THREADS = 4;

  /**
   * The pdp instance.
   */
  private static PolicyDecisionPoint pdpInstance; // TODO Holder Pattern would
  // be nice
  // private final AtomicBoolean initialized = new AtomicBoolean(false); //TODO
  // nutzen?

  /**
   * The Constant EXECUTE_ACTIONS_THREADPOOL.
   */
  private final ExecutorService EXECUTE_ACTIONS_THREADPOOL = Executors
      .newFixedThreadPool(NUM_OPTIONAL_EXECUTEACTIONS_EXECUTOR_THREADS);

  /**
   * The deployed policies.
   */
  private final IPolicyStore policyStore;

  /**
   * Indicates that the PDP is in a failure mode
   */
  private final AtomicBoolean failureModeFlag;

  private final ConnectorCache connectorCache;

  private final PipOperatorCache pipOperatorCache;

  /**
   * The component componentId.
   */
  private ComponentId componentId;

  /**
   * The pmp uri.
   */
  private URI pmpURI;

  /**
   * The threadPool.
   */
  private ExecutorService threadpool;

  /**
   * The num threads.
   */
  private int numThreads;

  /**
   * Event Repository
   */
  @Nullable
  private IEventRepository eventRepository;

  /**
   * Whitelist mode
   */
  private boolean whitelistModeEnabled;

  /**
   * Instantiates a new policy decision point.
   */
  private PolicyDecisionPoint(ConnectorCache connectorCache, PipOperatorCache pipOperatorCache) {
    this.policyStore = new PolicyStoreMapMatching(10);
    this.failureModeFlag = new AtomicBoolean(false);
    this.connectorCache = connectorCache;
    this.pipOperatorCache = pipOperatorCache;
  }

  /**
   * Gets the single instance of PolicyDecisionPoint.
   *
   * @return single instance of PolicyDecisionPoint
   */
  public static synchronized PolicyDecisionPoint getInstance() {
    if (pdpInstance == null) {
      final ConnectorCache connectorCache = new ConnectorCache(new ConnectorFactory());
      pdpInstance = new PolicyDecisionPoint(connectorCache, new PipOperatorCache(connectorCache));
    }

    return PolicyDecisionPoint.pdpInstance;
  }

  public Optional<IEventRepository> getEventRepository() {
    return Optional.ofNullable(this.eventRepository);
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.api.component.interfaces.IPolicyDecisionPoint# clearAllCaches()
   */
  @Override
  public boolean clearAllCaches(@Nullable
  SolutionId solutionId) {
    LOG.debug("clearAllCaches({})", solutionId);
    // TODO clear only the things that relate to the specified solutionId (IND2UCE-3248)
    this.connectorCache.clearPipCache();
    this.connectorCache.clearPxpCache();
    this.connectorCache.clearPMPCache();
    this.pipOperatorCache.clearCache();

    return true;
  }

  private Callable<AuthorizationDecision> createDecisionMakerTask(final Event event) {
    if (event == null) {
      LOG.error("Received a null event object.");
      throw new IllegalArgumentException("Received a null event object.");
    }

    if (this.eventRepository != null) {
      this.eventRepository.notify(event);
    }

    return new DecisionMaker(event, this.policyStore, this.EXECUTE_ACTIONS_THREADPOOL);
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.api.component.interfaces.IPolicyDecisionPoint#
   * decisionRequest(de.fraunhofer.iese.mydata.api.policy.Event)
   */
  @Override
  public AuthorizationDecision decisionRequest(Event event)
      throws IOException, EvaluationUndecidableException {

    if (this.failureModeFlag.get()) {
      LOG.debug("PDP is in failure mode - inhibiting event.");
      return AuthorizationDecision.DECISION_INHIBIT;
    }

    final Callable<AuthorizationDecision> task = this.createDecisionMakerTask(event);

    final Future<AuthorizationDecision> authorizationDecision = this.threadpool.submit(task);
    try {
      return authorizationDecision.get();
    } catch (CancellationException | ExecutionException e) {
      throw new EvaluationUndecidableException("Error during event evaluation!", e);
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new EvaluationUndecidableException("Error during event evaluation!", e);
    }
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.api.component.interfaces.IPolicyDecisionPoint#
   * deploy(de.fraunhofer.iese.mydata.api.policy.identifier.String, java.lang.String)
   */
  @Override
  public boolean deploy(de.fraunhofer.iese.mydata.policy.Policy p, ZoneId zoneId)
      throws IOException {
    if (!this.validatePolicyAndZone(zoneId, p)) {
      return false;
    }

    if (this.policyStore.checkIfPolicyIsAlreadyDeployed(p.getPolicyId())) {
      LOG.debug("Policy has already been deployed. Revoking it first.");
      try {
        this.revokePolicy(p.getPolicyId());
      } catch (final ResourceUpdateException e) {
        LOG.warn("Old policy could not be revoked.", e);
        return false;
      }
    }
    return this.deployPolicy(p, zoneId);
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  private boolean validatePolicyAndZone(ZoneId zoneId,
      de.fraunhofer.iese.mydata.policy.Policy policy) {
    try {
      MyDataEntity.validateAndNullCheck(policy, PolicyDeployableGroup.class, Default.class);
    } catch (final InvalidEntityException e) {
      LOG.warn("Invalid policy");
      return false;
    }

    if (zoneId == null) {
      LOG.warn("Invalid policy, the zoneId is missing");
      return false;
    }
    return true;
  }

  private boolean deployPolicy(de.fraunhofer.iese.mydata.policy.Policy p, ZoneId zoneId) {
    try {
      LOG.debug("Start transforming policy: [{}]", p.getPolicyId());
      final Policy policy = new PolicyTransformer().fromCoreToPdp(p);
      LOG.debug("Transformed policy successfully: [{}]", p.getPolicyId());
      this.policyStore.storePolicy(policy, zoneId);
      if (this.eventRepository != null) {
        this.eventRepository.policyDeployed(p);
      }
      LOG.info("Successfully deployed policy with id [{}]", p.getPolicyId());
      return true;
    } catch (final Exception e) {
      LOG.error("Error while deploying policy: [{}]: {}", p.getPolicyId(), e.getMessage(), e);
      return false; // TODO: ConflictingTimerException...
    }
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#finalize()
   */
  @Override
  protected void finalize() throws Throwable {
    try {
      if (this.threadpool != null) {
        LOG.debug("Shutting down thread pool in finalize!");
        this.threadpool.shutdownNow();
      }
    } finally {
      super.finalize();
    }
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.api.component.interfaces.IComponent#getId()
   */
  @Override
  public ComponentId getId() throws IOException {
    return this.componentId;
  }

  /**
   * Gets the pmp uri.
   *
   * @return the pmp uri
   */
  public URI getPmpURI() {
    return this.pmpURI;
  }

  /**
   * Initialize.
   *
   * @param  componentId            the componentId
   * @param  pmpUrl                 the pmp url
   * @param  numberOfThreads        the number of threads
   * @param  whitelistModeEnabled   activate whitelisting
   * @param  eventRepository        the eventRepository
   * @return                        true, if successful
   * @throws InvalidEntityException
   */
  public boolean initialize(ComponentId componentId, URI pmpUrl, int numberOfThreads,
      boolean whitelistModeEnabled, @Nullable IEventRepository eventRepository)
      throws InvalidEntityException {
    // TODO tests call this multiple times, restrict to only once later?
    // if (initialized.compareAndSet(false, true)) {
    MyDataEntity.validateAndNullCheck(componentId);
    if (pmpUrl == null) {
      LOG.error("ComponentId and PMP URL required");
      return false;
    }

    LOG.debug("Entering initialize({}, {}, {}, {}, {})", componentId, pmpUrl, numberOfThreads,
        whitelistModeEnabled, eventRepository == null ? "null" : "<EventRepository>");

    try {
      this.componentId = componentId;
      this.pmpURI = pmpUrl;
      this.numThreads = numberOfThreads;
      this.whitelistModeEnabled = whitelistModeEnabled;
      if (eventRepository != null) {
        this.eventRepository = eventRepository;
        LOG.info("EventRepository set");
      } else {
        LOG.info("EventRepository NOT set");
      }
      if (this.threadpool != null) {
        this.threadpool.shutdownNow();
        this.threadpool = null;
      }
      this.threadpool = Executors.newFixedThreadPool(this.numThreads);
    } catch (final NumberFormatException e) {
      LOG.error("Error while parsing initialization arguments. Returning false...");
      return false;
    }

    LOG.info("PDP has been initialized and is ready...");

    return true;
    // } else {
    // throw new IllegalStateException("Already initialized");
    // }
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.api.component.interfaces.IPolicyDecisionPoint#
   * listDeployedPolicies()
   */
  @Override
  public Set<String> listDeployedPolicies() throws IOException {
    return this.policyStore.getDeployedPolicyIds();
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.api.component.interfaces.IComponent#reset()
   */
  @Override
  public boolean reset() throws IOException {
    // Revoking all policies
    LOG.info("Performing reset. Shutting down thread pool (hard)...");
    if (this.threadpool != null) {
      final List<Runnable> notExecutedThreads = this.threadpool.shutdownNow();
      LOG.info("Number of lost decision requests: {}", notExecutedThreads.size());
      LOG.info("All policies are going to be revoked.");
      this.policyStore.clear();
      if (this.eventRepository != null) {
        // TODO maybe this is not enough, we should have a look at the whole
        // reset thing ...
        this.eventRepository.reset();
      }
      LOG.info("Clearing all caches...");
      this.clearAllCaches(null);
      LOG.info("Starting new thread pool");
      this.threadpool = Executors.newFixedThreadPool(this.numThreads);
      LOG.debug("Reset finished.");
    } else {
      LOG.info("Nothing to reset. Threadpool is null");
    }
    this.failureModeFlag.set(false);
    return true;
  }

  @Override
  public HealthStatus getHealth() throws IOException {
    return HealthStatus.of(Status.UP);
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.api.component.interfaces.IPolicyDecisionPoint#
   * revokePolicy(de.fraunhofer.iese.mydata.api.policy.identifier.String)
   */
  @Override
  public boolean revokePolicy(PolicyId policyId) throws IOException, ResourceUpdateException {
    if (policyId == null) {
      LOG.warn("Trying to revoke a policy with policyId == null.");
      throw new IllegalArgumentException("Trying to revoke a policy with policyId == null.");
    }

    if (this.policyStore.checkIfPolicyIsAlreadyDeployed(policyId)) {
      LOG.debug("Found policy [{}] as deployed policy. Revoking...", policyId);
      this.policyStore.removePolicy(policyId);
      LOG.info("Policy revocation successful [{}].", policyId);
      if (this.eventRepository != null) {
        final de.fraunhofer.iese.mydata.policy.Policy p = new de.fraunhofer.iese.mydata.policy.Policy();
        p.setPolicyId(policyId);
        this.eventRepository.policyRevoked(p);
      }
      return true;
    } else {
      LOG.warn("Policy [{}] is not deployed and can't be revoked.", policyId);
      return false;
    }
  }

  @Override
  public boolean addToBlacklist(Set<SolutionId> ids) throws IOException {
    // TODO
    return false;
  }

  @Override
  public boolean removeFromBlacklist(Set<SolutionId> ids) throws IOException {
    // TODO
    return false;
  }

  @Override
  public List<AuthorizationDecision> decisionRequests(List<Event> events) throws IOException {
    final List<AuthorizationDecision> decisions = new ArrayList<>(events.size());
    for (final Event event : events) {
      if (this.failureModeFlag.get()) {
        decisions.add(AuthorizationDecision.DECISION_INHIBIT);
        continue;
      }

      final Callable<AuthorizationDecision> task = this.createDecisionMakerTask(event);

      final Future<AuthorizationDecision> authorizationDecision = this.threadpool.submit(task);
      try {
        decisions.add(authorizationDecision.get());
      } catch (CancellationException | ExecutionException e) {
        decisions.add(AuthorizationDecision.DECISION_INHIBIT);
        // throw new EvaluationUndecidableException("Error during event
        // evaluation!", e);
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
        decisions.add(AuthorizationDecision.DECISION_INHIBIT);
      }
    }
    return decisions;
  }

  @Override
  public boolean evaluate(Event event) throws IOException {
    try {
      this.decisionRequest(event);
    } catch (final EvaluationUndecidableException e) {
      LOG.debug("Evaluation undecidable.", e);
      return false;
    }
    return true;
  }

  @Override
  public boolean evaluate(Set<Event> events) throws IOException {
    this.decisionRequests(new ArrayList<>(events));
    return true;
  }

  @Override
  public boolean updatePolicy(de.fraunhofer.iese.mydata.policy.Policy p, ZoneId zoneId)
      throws IOException, ResourceUpdateException {
    if (!this.validatePolicyAndZone(zoneId, p)) {
      return false;
    }

    if (this.policyStore.checkIfPolicyIsAlreadyDeployed(p.getPolicyId())) {
      LOG.debug("Policy [{}] has already been deployed. Revoking it first.", p.getPolicyId());
      this.policyStore.removePolicy(p.getPolicyId());
    }

    if (this.eventRepository != null) {
      this.eventRepository.policyUpdate(p);
    }

    try {
      return this.deployPolicy(p, zoneId);
    } catch (final DOMException e) {
      LOG.warn("Policy [{}] could not be deployed because of {}", p.getPolicyId(), e.getMessage(),
          e);
      // TODO rollback change to policystore
    }
    return false;
  }

  @Override
  public boolean updatePolicyAndId(de.fraunhofer.iese.mydata.policy.Policy policyWithNewId,
      ZoneId zoneId, PolicyId oldPolicyId) throws IOException, ResourceUpdateException {
    if (!this.validatePolicyAndZone(zoneId, policyWithNewId)) {
      return false;
    }
    final PolicyId newPolicyId = policyWithNewId.getPolicyId();

    try {
      MyDataEntity.validateAndNullCheck(oldPolicyId);
    } catch (final InvalidEntityException e) {
      LOG.warn("Invalid PolicyId in PolicyDecisionPoint::updatePolicyId", e);
      return false;
    }

    if (newPolicyId.equals(oldPolicyId)) {
      return this.updatePolicy(policyWithNewId, zoneId);
    }

    if (this.policyStore.checkIfPolicyIsAlreadyDeployed(newPolicyId)) {
      LOG.warn("There is already a deployed policy with the ID {}", newPolicyId.getUrn());
      return false;
    }

    if (this.policyStore.checkIfPolicyIsAlreadyDeployed(oldPolicyId)) {
      LOG.debug("Replace old policy from policystore");
      this.policyStore.changePolicyId(oldPolicyId, newPolicyId);
    }
    if (this.eventRepository != null) {
      this.eventRepository.policyUpdate(policyWithNewId, oldPolicyId);
    }

    // id changed

    // update event map policyid<->eventsToBeStored
    // old events will be deleted has the policy gets updated (changing the id
    // has strong consequences)

    // apply changes to content:
    return this.updatePolicy(policyWithNewId, zoneId);
  }

  @Override
  public boolean isInFailureMode() {
    return this.failureModeFlag.get();
  }

  @Override
  public void setFailureMode(boolean active) throws IOException {
    if (active) {
      // going to fail
      LOG.warn("PDP failureMode set to: {}", true);
      // reset();
      this.failureModeFlag.set(true);
    } else {
      final boolean wasActive = this.failureModeFlag.getAndSet(false);
      if (wasActive) {
        // going to restore
        LOG.info("PDP failureMode set to: {}", false);
      }
    }
  }

  /**
   * @return whitelist mode enabled
   */
  @Override
  public boolean isWhitelistModeEnabled() {
    return this.whitelistModeEnabled;
  }

  /**
   * @return the global connectorCache
   */
  public ConnectorCache getConnectorCache() {
    return this.connectorCache;
  }

  /**
   * @return the pipOperator cache
   */
  public PipOperatorCache getPipOperatorCache() {
    return this.pipOperatorCache;
  }

}
