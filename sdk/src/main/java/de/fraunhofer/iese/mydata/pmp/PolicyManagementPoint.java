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

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.ComponentType;
import de.fraunhofer.iese.mydata.component.connector.ConnectorFactory;
import de.fraunhofer.iese.mydata.component.health.HealthStatus;
import de.fraunhofer.iese.mydata.component.health.Status;
import de.fraunhofer.iese.mydata.component.information.PdpComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PepComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PipComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PxpComponentInformation;
import de.fraunhofer.iese.mydata.component.information.method.MethodInterfaceDescription;
import de.fraunhofer.iese.mydata.component.interfaces.IBasicManagementService;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyDecisionPoint;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyExecutionPoint;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyInformationPoint;
import de.fraunhofer.iese.mydata.exception.ConflictingResourceException;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.exception.ResourceUpdateException;
import de.fraunhofer.iese.mydata.policy.Policy;
import de.fraunhofer.iese.mydata.policy.PolicyDeployableGroup;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.policy.exception.ConflictingPolicyException;
import de.fraunhofer.iese.mydata.solution.SolutionId;
import de.fraunhofer.iese.mydata.timer.Timer;
import de.fraunhofer.iese.mydata.timer.TimerDeployableGroup;
import de.fraunhofer.iese.mydata.timer.TimerId;
import de.fraunhofer.iese.mydata.util.ClockProvider;

import jakarta.validation.groups.Default;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

/**
 * The core implementation for a policy management point.
 *
 * @author Fraunhofer IESE
 */
public class PolicyManagementPoint implements IBasicManagementService {

  private static final Logger LOG = LoggerFactory.getLogger(PolicyManagementPoint.class);

  private final Map<TimerId, JobKey> scheduledTimers = new HashMap<>();

  private final ComponentId componentId;

  private final Scheduler timerScheduler;

  private final Map<PolicyId, Policy> policies = new HashMap<>();

  private final Map<TimerId, Timer> timers = new HashMap<>();

  private final Map<ComponentId, PepComponentInformation> peps = new HashMap<>();

  private final Map<ComponentId, PxpComponentInformation> pxps = new HashMap<>();

  private final Map<ComponentId, PipComponentInformation> pips = new HashMap<>();

  private final ZoneId zoneIdOfSolution;

  private final PdpComponentInformation pdpComponentInformation;

  private final IPolicyDecisionPoint pdp;

  private final ConnectorFactory connectorFactory;

  public PolicyManagementPoint(ComponentId componentId,
      PdpComponentInformation pdpComponentInformation, IPolicyDecisionPoint policyDecisionPoint,
      ZoneId timeZone, Scheduler timerScheduler, ConnectorFactory connectorFactory)
      throws InvalidEntityException {
    MyDataEntity.validateAndNullCheck(componentId);
    this.componentId = componentId;
    if (null == pdpComponentInformation || null == policyDecisionPoint) {
      throw new InvalidEntityException("Pdp componentId or policyDecisionPoint is null");
    }
    try {
      if (!pdpComponentInformation.getComponentId().equals(policyDecisionPoint.getId())) {
        throw new InvalidEntityException("Pdp componentId does not match");
      }
    } catch (final IOException e) {
      // TODO should not happen locally as local PMP uses local PDP
    }
    this.pdpComponentInformation = pdpComponentInformation;
    this.pdp = policyDecisionPoint;
    this.zoneIdOfSolution = Objects.requireNonNull(timeZone);
    this.timerScheduler = Objects.requireNonNull(timerScheduler);
    try {
      if (!timerScheduler.isStarted()) {
        timerScheduler.start();
      }
    } catch (final SchedulerException e) {
      LOG.warn("Unable to start timer scheduler", e);
      throw new RuntimeException("Unable to start timer scheduler"); // TODO
    }
    this.connectorFactory = Objects.requireNonNull(connectorFactory);
    LOG.info("Local PMP initialized");
  }

  @Override
  public HealthStatus getHealth() {
    return HealthStatus.of(Status.UP);
  }

  @Override
  public ComponentId getId() {
    return this.componentId;
  }

  @Override
  public boolean reset() {

    for (final Policy p : this.policies.values()) {
      if (!p.isDeployed()) {
        continue;
      }
      try {
        this.pdp.revokePolicy(p.getPolicyId());
      } catch (IOException | ResourceUpdateException e) {
        return false;
      }
    }

    for (final TimerId timerId : this.scheduledTimers.keySet()) {
      try {
        this.revokeTimer(timerId);
      } catch (InvalidEntityException | ResourceUpdateException e) {
        return false;
      } catch (final NoSuchEntityException e1) {
        // do nothing on purpose
      }
    }

    this.timers.clear();
    this.policies.clear();
    this.peps.clear();
    this.pxps.clear();
    this.pips.clear();
    return true;
  }

  @Override
  public PolicyId addPolicy(Policy policy) throws IOException, ResourceUpdateException,
      InvalidEntityException, ConflictingResourceException, NoSuchEntityException {
    if (null == policy) {
      throw new InvalidEntityException("Policy must not be null");
    }
    if (0L == policy.getModificationTime()) {
      policy.setModificationTime(ClockProvider.getClock().getCurrentEpochTime());
    }
    MyDataEntity.validateAndNullCheck(policy);
    if (this.policies.containsKey(policy.getPolicyId())) {
      throw new ConflictingResourceException(
          "Policy with id " + policy.getPolicyId() + " already exists.");
    }
    final boolean shouldBeDeployed = policy.isDeployed();
    policy.setDeployed(false);
    if (shouldBeDeployed) {
      MyDataEntity.validateAndNullCheck(policy, PolicyDeployableGroup.class, Default.class);
      // prevent to save invalid policy that should be deployed
      this.policies.put(policy.getPolicyId(), policy);
      this.deployPolicy(policy.getPolicyId());
    } else {
      this.policies.put(policy.getPolicyId(), policy);
    }
    return policy.getPolicyId();
  }

  @Override
  public boolean policyExists(PolicyId policyId) throws InvalidEntityException {
    MyDataEntity.validateAndNullCheck(policyId);
    return this.policies.containsKey(policyId);
  }

  @Override
  public Policy getPolicy(PolicyId policyId) throws NoSuchEntityException, InvalidEntityException {
    if (!this.policyExists(policyId)) {
      throw new NoSuchEntityException();
    }
    return this.policies.get(policyId);
  }

  @Override
  public Set<Policy> getPolicies(SolutionId solutionId) throws InvalidEntityException {
    MyDataEntity.validateAndNullCheck(solutionId);
    return this.policies.values().stream().filter(policy -> {
      try {
        return solutionId.equals(SolutionId.fromPolicyId(policy.getPolicyId()));
      } catch (final InvalidEntityException e) {
        return false;
      }
    }).collect(Collectors.toSet());
  }

  @Override
  public Set<Policy> getRevokedPolicies(SolutionId solutionId) throws InvalidEntityException {
    MyDataEntity.validateAndNullCheck(solutionId);
    return this.policies.values().stream().filter(policy -> {
      try {
        return !policy.isDeployed()
            && solutionId.equals(SolutionId.fromPolicyId(policy.getPolicyId()));
      } catch (final InvalidEntityException e) {
        return false;
      }
    }).collect(Collectors.toSet());

  }

  @Override
  public Set<PolicyId> listRevokedPolicies(SolutionId solutionId) throws InvalidEntityException {
    return this.getRevokedPolicies(solutionId).stream().map(Policy::getPolicyId)
        .collect(Collectors.toSet());

  }

  @Override
  public Set<PolicyId> listPolicies(SolutionId solutionId) throws InvalidEntityException {
    return this.getPolicies(solutionId).stream().map(Policy::getPolicyId)
        .collect(Collectors.toSet());
  }

  @Override
  public Set<Policy> getDeployedPolicies() {
    return this.policies.values().stream().filter(Policy::isDeployed).collect(Collectors.toSet());
  }

  @Override
  public Set<Policy> getDeployedPolicies(SolutionId solutionId) throws InvalidEntityException {
    MyDataEntity.validateAndNullCheck(solutionId);
    return this.policies.values().stream().filter(policy -> {
      try {
        return policy.isDeployed()
            && solutionId.equals(SolutionId.fromPolicyId(policy.getPolicyId()));
      } catch (final InvalidEntityException e) {
        return false;
      }
    }).collect(Collectors.toSet());
  }

  @Override
  public Set<PolicyId> listDeployedPolicies(SolutionId solutionId) throws InvalidEntityException {
    return this.getDeployedPolicies(solutionId).stream().map(Policy::getPolicyId)
        .collect(Collectors.toSet());
  }

  @Override
  public boolean isPolicyDeployed(PolicyId policyId) throws InvalidEntityException {
    MyDataEntity.validateAndNullCheck(policyId);
    return this.policies.containsKey(policyId) && this.policies.get(policyId).isDeployed();
  }

  @Override
  public PolicyId updatePolicy(Policy policy)
      throws IOException, ResourceUpdateException, NoSuchEntityException, InvalidEntityException {
    if (null == policy) {
      throw new InvalidEntityException("Policy must not be null");
    }
    if (0L == policy.getModificationTime()) {
      policy.setModificationTime(ClockProvider.getClock().getCurrentEpochTime());
    }
    MyDataEntity.validateAndNullCheck(policy);

    if (!this.policies.containsKey(policy.getPolicyId())) {
      throw new NoSuchEntityException();
    }

    if (this.policies.get(policy.getPolicyId()).isDeployed() && policy.isDeployed()) {
      MyDataEntity.validateAndNullCheck(policy, PolicyDeployableGroup.class, Default.class);
      if (!this.pdp.updatePolicy(policy, this.zoneIdOfSolution)) {
        throw new ResourceUpdateException("Unable to update policy in PDP");
      }

    } else if (this.policies.get(policy.getPolicyId()).isDeployed() && !policy.isDeployed()) {
      if (!this.pdp.revokePolicy(policy.getPolicyId())) {
        throw new ResourceUpdateException("Unable to revoke policy in PDP");
      }

    } else if (!this.policies.get(policy.getPolicyId()).isDeployed() && policy.isDeployed()) {
      MyDataEntity.validateAndNullCheck(policy, PolicyDeployableGroup.class, Default.class);
      try {
        if (!this.pdp.deploy(policy, this.zoneIdOfSolution)) {
          throw new ResourceUpdateException("Unable to deploy policy in PDP");
        }
      } catch (final ConflictingPolicyException e) {
        throw new ResourceUpdateException("Unable to deploy policy in PDP", e);
      }
    }

    this.policies.put(policy.getPolicyId(), policy);

    return policy.getPolicyId();
  }

  @Override
  public void deployPolicy(PolicyId policyId)
      throws IOException, NoSuchEntityException, ResourceUpdateException, InvalidEntityException {
    MyDataEntity.validateAndNullCheck(policyId);

    if (!this.policies.containsKey(policyId)) {
      throw new NoSuchEntityException();
    }

    final Policy p = this.policies.get(policyId);

    if (!p.isDeployed()) {
      MyDataEntity.validateAndNullCheck(p, PolicyDeployableGroup.class, Default.class);
      try {
        if (!this.pdp.deploy(p, this.zoneIdOfSolution)) {
          throw new ResourceUpdateException("Unable to deploy policy in PDP");
        } else {
          p.setDeployed(true);
        }
      } catch (final ConflictingPolicyException e) {
        throw new ResourceUpdateException("Error while deploying policy", e);
      }
    }

  }

  @Override
  public synchronized void revokePolicy(PolicyId policyId)
      throws IOException, NoSuchEntityException, ResourceUpdateException, InvalidEntityException {
    MyDataEntity.validateAndNullCheck(policyId);

    if (!this.policies.containsKey(policyId)) {
      throw new NoSuchEntityException();
    }

    if (!this.policies.get(policyId).isDeployed()) {
      return;
    }

    if (!this.pdp.revokePolicy(policyId)) {
      throw new ResourceUpdateException("Error while revoking policy");
    }

    this.policies.get(policyId).setDeployed(false);
  }

  @Override
  public void deletePolicy(PolicyId policyId)
      throws ResourceUpdateException, NoSuchEntityException, InvalidEntityException {

    MyDataEntity.validateAndNullCheck(policyId);

    if (!this.policies.containsKey(policyId)) {
      throw new NoSuchEntityException();
    }

    if (this.policies.get(policyId).isDeployed()) {
      throw new ResourceUpdateException(
          "Cannot delete a policy that is still deployed. Revoke it first.");
    }

    this.policies.remove(policyId);
  }

  private void validateTimer(Timer timer, boolean forDeploy) throws InvalidEntityException {
    if (forDeploy) {
      MyDataEntity.validateAndNullCheck(timer, TimerDeployableGroup.class, Default.class);
      try {
        // TODO move to the Entity Validation mechanism?
        new CronExpression(timer.getCronValue());
      } catch (final ParseException e) {
        throw new InvalidEntityException(
            "Cron expression " + timer.getCronValue() + " is not valid.", e);
      }
    } else {
      MyDataEntity.validateAndNullCheck(timer);
    }
  }

  @Override
  public TimerId addTimer(Timer timer) throws ResourceUpdateException, ConflictingResourceException,
      InvalidEntityException, NoSuchEntityException {
    if (null == timer) {
      throw new InvalidEntityException("Timer must not be null");
    }
    if (0L == timer.getModificationTime()) {
      timer.setModificationTime(ClockProvider.getClock().getCurrentEpochTime());
    }
    this.validateTimer(timer, false);
    if (this.timers.containsKey(timer.getTimerId())) {
      throw new ConflictingResourceException(
          "Timer with id " + timer.getTimerId() + " already exists.");
    }
    final boolean shouldBeDeployed = timer.isDeployed();
    timer.setDeployed(false);
    if (shouldBeDeployed) {
      this.validateTimer(timer, true);
      // prevent to save invalid timer that should be deployed
      this.timers.put(timer.getTimerId(), timer);
      this.deployTimer(timer.getTimerId());
    } else {
      this.timers.put(timer.getTimerId(), timer);
    }
    return timer.getTimerId();
  }

  @Override
  public Timer getTimer(TimerId timerId) throws NoSuchEntityException, InvalidEntityException {
    if (!this.timerExists(timerId)) {
      throw new NoSuchEntityException();
    }
    return this.timers.get(timerId);
  }

  @Override
  public Set<Timer> getTimers(SolutionId solutionId) throws InvalidEntityException {
    MyDataEntity.validateAndNullCheck(solutionId);
    return this.timers.values().stream().filter(timer -> {
      try {
        return solutionId.equals(SolutionId.fromTimerId(timer.getTimerId()));
      } catch (final InvalidEntityException e) {
        return false;
      }
    }).collect(Collectors.toSet());

  }

  @Override
  public Set<TimerId> listTimers(SolutionId solutionId) throws InvalidEntityException {
    return this.getTimers(solutionId).stream().map(Timer::getTimerId).collect(Collectors.toSet());
  }

  @Override
  public Set<Timer> getDeployedTimers(SolutionId solutionId) throws InvalidEntityException {
    MyDataEntity.validateAndNullCheck(solutionId);
    return this.timers.values().stream().filter(timer -> {
      try {
        return timer.isDeployed() && solutionId.equals(SolutionId.fromTimerId(timer.getTimerId()));
      } catch (final InvalidEntityException e) {
        return false;
      }
    }).collect(Collectors.toSet());
  }

  @Override
  public Set<TimerId> listDeployedTimers(SolutionId solutionId) throws InvalidEntityException {
    return this.getDeployedTimers(solutionId).stream().map(Timer::getTimerId)
        .collect(Collectors.toSet());
  }

  @Override
  public boolean isTimerDeployed(TimerId timerId)
      throws NoSuchEntityException, InvalidEntityException {
    MyDataEntity.validateAndNullCheck(timerId);
    return this.timers.containsKey(timerId) && this.timers.get(timerId).isDeployed();
  }

  @Override
  public void deployTimer(TimerId timerId)
      throws NoSuchEntityException, ResourceUpdateException, InvalidEntityException {
    MyDataEntity.validateAndNullCheck(timerId);

    final Timer timer = this.timers.get(timerId);
    if (timer == null) {
      throw new NoSuchEntityException("Timer " + timerId + " not found");
    }
    this.validateTimer(timer, true);
    if (this.scheduledTimers.containsKey(timer.getTimerId())) {
      LOG.info("Someone tried to deploy an already deployed timer: {}", timerId);
      timer.setDeployed(true);
      return;
    }

    LOG.info("Scheduling new timer task: {}", timerId);
    try {

      final Trigger trigger = TriggerBuilder.newTrigger()
          .withSchedule(CronScheduleBuilder.cronSchedule(timer.getCronValue())
              .inTimeZone(TimeZone.getTimeZone(this.zoneIdOfSolution)))
          .build();

      final JobDetail job = JobBuilder.newJob(TimerJob.class).build();
      final JobDataMap jobDataMap = job.getJobDataMap();
      jobDataMap.put("timer", timer);
      jobDataMap.put("pdp", this.pdp);

      try {
        this.timerScheduler.scheduleJob(job, trigger);
        this.scheduledTimers.put(timerId, job.getKey());
      } catch (final SchedulerException e) {
        throw new ResourceUpdateException("Unable to deploy timer", e);
      }

      LOG.info("Scheduling new timer task completed: {}", timerId);
      timer.setDeployed(true);
    } catch (final IllegalArgumentException e) {
      throw new ResourceUpdateException(e.getMessage());
    }

  }

  @Override
  public void revokeTimer(TimerId timerId)
      throws NoSuchEntityException, ResourceUpdateException, InvalidEntityException {
    MyDataEntity.validateAndNullCheck(timerId);

    if (!this.timers.containsKey(timerId)) {
      throw new NoSuchEntityException();
    }

    if (!this.timers.get(timerId).isDeployed()) {
      return;
    }

    if (this.scheduledTimers.containsKey(timerId)) {
      LOG.info("Removing scheduled task from futureMap: {}", timerId);
      try {
        this.timerScheduler.deleteJob(this.scheduledTimers.get(timerId));
        this.scheduledTimers.remove(timerId);
        final Timer timer = this.timers.get(timerId);
        timer.setDeployed(false);
      } catch (final SchedulerException e) {
        throw new ResourceUpdateException("Unable to revoke timer", e);
      }
      LOG.info("Removing scheduled task from futureMap successful: {}", timerId);
    } else {
      LOG.info("Timer not found in futureMap: {}", timerId);
    }

  }

  @Override
  public void deleteTimer(TimerId timerId)
      throws ResourceUpdateException, NoSuchEntityException, InvalidEntityException {
    MyDataEntity.validateAndNullCheck(timerId);

    if (!this.timers.containsKey(timerId)) {
      throw new NoSuchEntityException();
    }

    if (this.timers.get(timerId).isDeployed()) {
      throw new ResourceUpdateException(
          "Cannot delete a timer that is still deployed. Revoke it first.");
    }

    this.timers.remove(timerId);
  }

  @Override
  public boolean timerExists(TimerId timerId) throws InvalidEntityException {
    MyDataEntity.validateAndNullCheck(timerId);
    return this.timers.containsKey(timerId);
  }

  @Override
  public ComponentId addPdp(PdpComponentInformation component)
      throws ConflictingResourceException, InvalidEntityException {
    MyDataEntity.validateAndNullCheck(component);
    throw new ConflictingResourceException("This component already exists."); // TODO:unsupportedOperationException
  }

  @Override
  public ComponentId addPep(PepComponentInformation component)
      throws ConflictingResourceException, InvalidEntityException {
    MyDataEntity.validateAndNullCheck(component);

    if (this.peps.containsKey(component.getComponentId())) {
      throw new ConflictingResourceException(
          "This component already exists. Delete it first, or use update");
    }
    this.peps.put(component.getComponentId(), component);
    return component.getComponentId();
  }

  @Override
  public ComponentId addPip(PipComponentInformation component)
      throws IOException, ConflictingResourceException, InvalidEntityException {
    MyDataEntity.validateAndNullCheck(component);

    if (this.pips.containsKey(component.getComponentId())) {
      throw new ConflictingResourceException(
          "This component already exists. Delete it first, or use update");
    }
    this.pips.put(component.getComponentId(), component);
    this.pdp.clearAllCaches(SolutionId.fromComponentId(component.getComponentId()));
    return component.getComponentId();
  }

  @Override
  public ComponentId addPxp(PxpComponentInformation component)
      throws IOException, ConflictingResourceException, InvalidEntityException {
    MyDataEntity.validateAndNullCheck(component);

    if (this.pxps.containsKey(component.getComponentId())) {
      throw new ConflictingResourceException(
          "This component already exists. Delete it first, or use update");
    }
    this.pxps.put(component.getComponentId(), component);
    this.pdp.clearAllCaches(SolutionId.fromComponentId(component.getComponentId()));
    return component.getComponentId();
  }

  @Override
  public boolean pdpExists(ComponentId componentId) throws InvalidEntityException {
    MyDataEntity.validateAndNullCheck(componentId);
    final ComponentType componentType = ComponentType.fromComponentId(componentId);
    if (componentType == null || !componentType.equals(ComponentType.PDP)) {
      return false;
    }
    return this.pdpComponentInformation != null
        && componentId.equals(this.pdpComponentInformation.getComponentId());
  }

  @Override
  public boolean pepExists(ComponentId componentId) throws InvalidEntityException {
    MyDataEntity.validateAndNullCheck(componentId);
    final ComponentType componentType = ComponentType.fromComponentId(componentId);
    if (componentType == null || !componentType.equals(ComponentType.PEP)) {
      return false;
    }
    return this.peps.containsKey(componentId);
  }

  @Override
  public boolean pipExists(ComponentId componentId) throws InvalidEntityException {
    MyDataEntity.validateAndNullCheck(componentId);
    final ComponentType componentType = ComponentType.fromComponentId(componentId);
    if (componentType == null || !componentType.equals(ComponentType.PIP)) {
      return false;
    }
    return this.pips.containsKey(componentId);
  }

  @Override
  public boolean pxpExists(ComponentId componentId) throws InvalidEntityException {
    MyDataEntity.validateAndNullCheck(componentId);
    final ComponentType componentType = ComponentType.fromComponentId(componentId);
    if (componentType == null || !componentType.equals(ComponentType.PXP)) {
      return false;
    }
    return this.pxps.containsKey(componentId);
  }

  @Override
  public PdpComponentInformation getPdp() throws NoSuchEntityException {
    if (this.pdpComponentInformation == null) {
      throw new NoSuchEntityException();
    }
    return this.pdpComponentInformation;
  }

  @Override
  public PepComponentInformation getPep(ComponentId id)
      throws NoSuchEntityException, InvalidEntityException {
    MyDataEntity.validateAndNullCheck(id);

    if (!this.pepExists(id)) {
      throw new NoSuchEntityException();
    }
    return this.peps.get(id);
  }

  @Override
  public PipComponentInformation getPip(ComponentId id)
      throws NoSuchEntityException, InvalidEntityException {
    MyDataEntity.validateAndNullCheck(id);

    if (!this.pipExists(id)) {
      throw new NoSuchEntityException();
    }
    return this.pips.get(id);
  }

  @Override
  public PxpComponentInformation getPxp(ComponentId id)
      throws NoSuchEntityException, InvalidEntityException {
    MyDataEntity.validateAndNullCheck(id);

    if (!this.pxpExists(id)) {
      throw new NoSuchEntityException();
    }
    return this.pxps.get(id);
  }

  @Override
  public HealthStatus getPipState(ComponentId id)
      throws NoSuchEntityException, InvalidEntityException, IOException {
    if (this.pipExists(id)) {
      final IPolicyInformationPoint pip = this.connectorFactory.getPip(this.getPip(id), null);
      if (null != pip) {
        return pip.getHealth();
      } else {
        return HealthStatus.of(Status.DOWN);
      }
    } else {
      throw new NoSuchEntityException("PIP with id " + id.getUrn() + " does not exist");
    }
  }

  @Override
  public HealthStatus getPxpState(ComponentId id)
      throws NoSuchEntityException, InvalidEntityException, IOException {
    if (this.pxpExists(id)) {
      final IPolicyExecutionPoint pxp = this.connectorFactory.getPxp(this.getPxp(id), null);
      if (null != pxp) {
        return pxp.getHealth();
      } else {
        return HealthStatus.of(Status.DOWN);
      }
    } else {
      throw new NoSuchEntityException("PXP with id " + id.getUrn() + " does not exist");
    }
  }

  @Override
  public Map<ComponentId, HealthStatus> getAllComponentStates(SolutionId solutionId) {
    // TODO: unsupportedOperation?

    final Map<ComponentId, HealthStatus> states = new HashMap<>();

    for (final PipComponentInformation c : this.pips.values()) {
      try {
        states.put(c.getComponentId(), this.getPipState(c.getComponentId()));
      } catch (NoSuchEntityException | InvalidEntityException | IOException e) {
        states.put(c.getComponentId(), HealthStatus.of(Status.DOWN));
      }
    }

    for (final PxpComponentInformation c : this.pxps.values()) {
      try {
        states.put(c.getComponentId(), this.getPxpState(c.getComponentId()));
      } catch (NoSuchEntityException | InvalidEntityException | IOException e) {
        states.put(c.getComponentId(), HealthStatus.of(Status.DOWN));
      }
    }

    states.put(this.componentId, this.getHealth());
    try {
      states.put(this.pdpComponentInformation.getComponentId(), this.pdp.getHealth());
    } catch (final IOException ioException) {
      states.put(this.pdpComponentInformation.getComponentId(), HealthStatus.of(Status.DOWN));
    }

    return states;
  }

  @Override
  public Set<PepComponentInformation> lookupPep(SolutionId solutionId)
      throws InvalidEntityException {
    MyDataEntity.validateAndNullCheck(solutionId);
    // TODO: just for solution
    return new HashSet<>(this.peps.values());
  }

  @Override
  public Set<PipComponentInformation> lookupPip(SolutionId solutionId,
      MethodInterfaceDescription query) throws InvalidEntityException {
    MyDataEntity.validateAndNullCheck(solutionId);
    MyDataEntity.validateAndNullCheck(query);

    final Set<PipComponentInformation> toReturn = new HashSet<>();

    for (final PipComponentInformation c : this.pips.values()) {
      for (final MethodInterfaceDescription id : c.getMethodInterfaceDescriptions()) {
        if (id.getMethodName().equals(query.getMethodName())) {
          toReturn.add(c);
          break;
        }
      }
    }

    return toReturn;
  }

  @Override
  public Set<PxpComponentInformation> lookupPxp(SolutionId solutionId,
      MethodInterfaceDescription query) throws InvalidEntityException {
    MyDataEntity.validateAndNullCheck(solutionId);
    MyDataEntity.validateAndNullCheck(query);

    final Set<PxpComponentInformation> toReturn = new HashSet<>();

    for (final PxpComponentInformation c : this.pxps.values()) {
      for (final MethodInterfaceDescription id : c.getMethodInterfaceDescriptions()) {
        if (id.getMethodName().equals(query.getMethodName())) {
          toReturn.add(c);
          break;
        }
      }
    }

    return toReturn;
  }

  @Override
  public ComponentId updatePdp(PdpComponentInformation component)
      throws NoSuchEntityException, ResourceUpdateException, InvalidEntityException {
    MyDataEntity.validateAndNullCheck(component);
    throw new ResourceUpdateException("Component cannot be updated");
  }

  @Override
  public ComponentId updatePep(PepComponentInformation component)
      throws NoSuchEntityException, ResourceUpdateException, InvalidEntityException {
    MyDataEntity.validateAndNullCheck(component);

    if (!this.peps.containsKey(component.getComponentId())) {
      throw new ResourceUpdateException("No component to update");
    }

    this.peps.put(component.getComponentId(), component);

    return component.getComponentId();
  }

  @Override
  public ComponentId updatePip(PipComponentInformation component)
      throws NoSuchEntityException, ResourceUpdateException, InvalidEntityException, IOException {
    MyDataEntity.validateAndNullCheck(component);

    if (!this.pips.containsKey(component.getComponentId())) {
      throw new ResourceUpdateException("No component to update");
    }

    this.pips.put(component.getComponentId(), component);
    this.pdp.clearAllCaches(SolutionId.fromComponentId(component.getComponentId()));
    return component.getComponentId();
  }

  @Override
  public ComponentId updatePxp(PxpComponentInformation component)
      throws NoSuchEntityException, ResourceUpdateException, InvalidEntityException, IOException {
    MyDataEntity.validateAndNullCheck(component);

    if (!this.pxps.containsKey(component.getComponentId())) {
      throw new ResourceUpdateException("No component to update");
    }

    this.pxps.put(component.getComponentId(), component);
    this.pdp.clearAllCaches(SolutionId.fromComponentId(component.getComponentId()));
    return component.getComponentId();
  }

  @Override
  public void deletePep(ComponentId componentId)
      throws NoSuchEntityException, ResourceUpdateException, InvalidEntityException, IOException {
    MyDataEntity.validateAndNullCheck(componentId);

    final ComponentType componentType = ComponentType.fromComponentId(componentId);
    if (componentType == null) {
      throw new ResourceUpdateException(
          "Cannot determine the ComponentType form componentId " + componentId.getUrn());
    }
    if (!componentType.equals(ComponentType.PEP)) {
      throw new ResourceUpdateException("Wrong ComponentType");
    }

    this.peps.remove(componentId);
  }

  @Override
  public void deletePip(ComponentId componentId)
      throws NoSuchEntityException, ResourceUpdateException, InvalidEntityException, IOException {
    MyDataEntity.validateAndNullCheck(componentId);

    final ComponentType componentType = ComponentType.fromComponentId(componentId);
    if (componentType == null) {
      throw new ResourceUpdateException(
          "Cannot determine the ComponentType form componentId " + componentId.getUrn());
    }
    if (!componentType.equals(ComponentType.PIP)) {
      throw new ResourceUpdateException("Wrong ComponentType");
    }

    this.pips.remove(componentId);
  }

  @Override
  public void deletePxp(ComponentId componentId)
      throws NoSuchEntityException, ResourceUpdateException, InvalidEntityException, IOException {
    MyDataEntity.validateAndNullCheck(componentId);

    final ComponentType componentType = ComponentType.fromComponentId(componentId);
    if (componentType == null) {
      throw new ResourceUpdateException(
          "Cannot determine the ComponentType form componentId " + componentId.getUrn());
    }
    if (!componentType.equals(ComponentType.PXP)) {
      throw new ResourceUpdateException("Wrong ComponentType");
    }

    this.pxps.remove(componentId);
  }

  @Override
  public Set<Timer> getRevokedTimers(SolutionId solutionId) throws InvalidEntityException {
    MyDataEntity.validateAndNullCheck(solutionId);
    return this.getTimers(solutionId).stream().filter(timer -> !timer.isDeployed())
        .collect(Collectors.toSet());
  }

  @Override
  public Set<TimerId> listRevokedTimers(SolutionId solutionId) throws InvalidEntityException {
    return this.getRevokedTimers(solutionId).stream().map(Timer::getTimerId)
        .collect(Collectors.toSet());
  }

  @Override
  public TimerId updateTimer(Timer timer) throws ResourceUpdateException, NoSuchEntityException,
      InvalidEntityException, ConflictingResourceException {
    if (null == timer) {
      throw new InvalidEntityException("Timer must not be null");
    }
    if (0L == timer.getModificationTime()) {
      timer.setModificationTime(ClockProvider.getClock().getCurrentEpochTime());
    }
    this.validateTimer(timer, false);

    if (!this.timers.containsKey(timer.getTimerId())) {
      throw new NoSuchEntityException();
    }

    if (timer.isDeployed()) {
      // prevent changes when the new timer should be deployed but is invalid
      // for deployment
      this.validateTimer(timer, true);
    }

    // revoke the timer first it it is deployed
    if (this.timers.get(timer.getTimerId()).isDeployed()) {
      this.revokeTimer(timer.getTimerId());
    }

    this.deleteTimer(timer.getTimerId());

    return this.addTimer(timer);
  }

  public static class TimerJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

      final JobDataMap dataMap = context.getJobDetail().getJobDataMap();
      final Timer timer = (Timer) dataMap.get("timer");
      final IPolicyDecisionPoint pdp = (IPolicyDecisionPoint) dataMap.get("pdp");

      try {
        pdp.evaluate(timer.getEvents());
      } catch (IllegalArgumentException | IOException e) {
        throw new JobExecutionException(e);
      }

    }
  }

}
