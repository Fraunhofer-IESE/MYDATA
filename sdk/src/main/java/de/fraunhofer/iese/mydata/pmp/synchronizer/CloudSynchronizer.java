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

package de.fraunhofer.iese.mydata.pmp.synchronizer;

import de.fraunhofer.iese.mydata.client.ClientId;
import de.fraunhofer.iese.mydata.client.SyncNotification;
import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.component.information.PepComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PipComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PxpComponentInformation;
import de.fraunhofer.iese.mydata.component.interfaces.IManagementService;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyDecisionPoint;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.pmp.cache.IPolicyCache;
import de.fraunhofer.iese.mydata.pmp.cache.ITimerCache;
import de.fraunhofer.iese.mydata.pmp.cache.PolicyCache;
import de.fraunhofer.iese.mydata.pmp.cache.TimerCache;
import de.fraunhofer.iese.mydata.policy.IPolicyService;
import de.fraunhofer.iese.mydata.policy.Policy;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.solution.SolutionId;
import de.fraunhofer.iese.mydata.timer.ITimerService;
import de.fraunhofer.iese.mydata.timer.Timer;
import de.fraunhofer.iese.mydata.timer.TimerId;

import com.google.gson.reflect.TypeToken;
import org.jspecify.annotations.Nullable;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CloudSynchronizer implements ISyncService {
  private static final Logger LOG = LoggerFactory.getLogger(CloudSynchronizer.class);

  private final IPolicyService policyService;

  private final ITimerService timerService;

  private final IManagementService managementService;

  private final Scheduler scheduler;

  private final AtomicReference<JobKey> jobKeyReference = new AtomicReference<>(null);

  private final CronExpression cronExpression;

  private final ClientId clientId;

  @Nullable
  private final IPolicyCache policyCache;

  @Nullable
  private final ITimerCache timerCache;

  private final IPolicyDecisionPoint pdp;

  private final boolean masterClient; // if true: push component registrations

  private final Supplier<Instant> instantSupplier;

  private final Semaphore semaphore;

  @Nullable
  private final Duration maxAge;

  private final AtomicReference<Long> lastSuccessfulSyncWithCloudTimeReference = new AtomicReference<>(
      null);

  /**
   * @param  instantSupplier        Supplier for current point in time (Instant)
   * @param  clientId               The clientId of the Library Client
   * @param  policyService          The local policy service
   * @param  timerService           The local timer service
   * @param  pdp                    The local PDP (just to manage its failureMode state)
   * @param  cloudManagementService The management service (cloud)
   * @param  cacheEnabled           Whether cache is enabled
   * @param  policyCachePath        File path, where to store the policy cache
   * @param  timerCachePath         File path, where to store the timer cache
   * @param  maxAge                 Optional duration string to determine how long synced
   *                                  information is valid
   * @param  cronSchedule           Cron schedule for synchronization
   * @param  masterClient           Whether the Library Client is a master client
   * @param  scheduler              The scheduler to schedule the synchronization tasks
   * @throws ParseException         In case of invalid cron expression by parameter cronSchedule
   */
  public CloudSynchronizer(Supplier<Instant> instantSupplier, ClientId clientId,
      IPolicyService policyService, ITimerService timerService, IPolicyDecisionPoint pdp,
      IManagementService cloudManagementService, boolean cacheEnabled, String policyCachePath,
      String timerCachePath, @Nullable String maxAge, String cronSchedule, boolean masterClient,
      Scheduler scheduler) throws ParseException {
    this.instantSupplier = Objects.requireNonNull(instantSupplier);
    this.policyService = Objects.requireNonNull(policyService);
    this.timerService = Objects.requireNonNull(timerService);
    this.pdp = Objects.requireNonNull(pdp);
    this.managementService = Objects.requireNonNull(cloudManagementService);
    this.clientId = Objects.requireNonNull(clientId);
    this.cronExpression = new CronExpression(cronSchedule);
    this.semaphore = new Semaphore(1, true);
    this.maxAge = maxAge == null ? null : Duration.parse(maxAge);

    if (cacheEnabled) {
      this.policyCache = new PolicyCache(this.instantSupplier, policyCachePath, this.maxAge);
      this.timerCache = new TimerCache(this.instantSupplier, timerCachePath, this.maxAge);
    } else {
      this.policyCache = null; // allows making the attribute final
      this.timerCache = null;
    }
    this.scheduler = Objects.requireNonNull(scheduler);
    this.masterClient = masterClient;
    LOG.info("CloudSynchronizer for client {} initialized, masterClient: {}", clientId,
        masterClient);
  }

  @Override
  public void start() throws SynchronizerException {
    // TODO add timezone information for cron schedule?
    final Trigger trigger = TriggerBuilder.newTrigger()
        .withSchedule(CronScheduleBuilder.cronSchedule(this.cronExpression)).build();
    final JobDetail job = JobBuilder.newJob(SyncJob.class).build();
    if (this.jobKeyReference.compareAndSet(null, job.getKey())) {
      LOG.info("Starting CloudSynchronizer");
      try {
        if (!this.scheduler.isStarted()) {
          this.scheduler.start();
        }
        final JobDataMap jobDataMap = job.getJobDataMap();
        jobDataMap.put("syncService", this);
        this.scheduler.scheduleJob(job, trigger);
        this.scheduler.triggerJob(job.getKey()); // immediate initial execution
        LOG.info("Started CloudSynchronizer, triggered immediate initial sync");
      } catch (final SchedulerException e) {
        this.jobKeyReference.set(null);
        throw new SynchronizerException(e.getMessage(), e);
      }
    }

  }

  @Override
  public void stop() throws SynchronizerException {
    try {
      final JobKey jobKeyToDelete = this.jobKeyReference.get();
      if (null != jobKeyToDelete) {
        LOG.info("Stopping CloudSynchronizer");
        this.scheduler.deleteJob(jobKeyToDelete);
        this.jobKeyReference.set(null);
        LOG.info("Stopped CloudSynchronizer");
      }
    } catch (final SchedulerException e) {
      throw new SynchronizerException(e.getMessage(), e);
    }
  }

  /*
  * Contract: this method does not throw any exceptions!
  * VisibleForTesting
  */
  void sync() {
    try {
      if (!this.semaphore.tryAcquire()) {
        LOG.info("Already syncing.");
      } else {
        // we acquired a permit
        final SyncNotification syncNotification = new SyncNotification(this.clientId);
        final Instant now = this.instantSupplier.get();
        final SyncSource policySyncSource = this.syncPolicies(syncNotification);
        final boolean policySyncFailed = syncNotification.isInFailureMode();

        SyncSource timerSyncSource = null;
        // no need to synchronise the timers if the policies could not be
        // synchronised
        if (!policySyncFailed) {
          timerSyncSource = this.syncTimers(syncNotification);
        }
        final boolean isSyncDataFromCloud = timerSyncSource == SyncSource.FROM_CLOUD
            && policySyncSource == SyncSource.FROM_CLOUD;

        if (syncNotification.isInFailureMode()) {
          this.setFailureMode(true);
          LOG.warn("Sync failed");
        } else {
          this.setFailureMode(false);
          if (isSyncDataFromCloud) {
            this.takeNoteOfSuccessfulSyncWithCloud(now);
            LOG.info("Successful Sync with Cloud at {}", now);
          } else {
            LOG.info("Successful Sync from Cache or by doing nothing at {}", now);
          }
        }
        this.notifySynchronisation(now, syncNotification);
      }
    } catch (final Exception e) {
      LOG.warn("Unhandled Exception in CloudSynchronizer::sync", e);
      assert false : "CloudSynchronizer::sync should not throw any exceptions";
    } finally {
      this.semaphore.release(); // make sure that we release the acquired permit
    }
  }

  private SyncSource syncPolicies(SyncNotification syncNotification) {
    LOG.info("Synchronizing policies with cloud PMP");
    Set<Policy> deployedCloudPoliciesSetOriginal;
    boolean isFromCache = false;
    try {
      deployedCloudPoliciesSetOriginal = this.managementService
          .getDeployedPolicies(SolutionId.fromClientId(this.clientId));
    } catch (InvalidEntityException | IOException | NoSuchEntityException e) {
      LOG.info("Failed to retrieve deployed policies from Cloud PMP", e);
      final Optional<Set<Policy>> validPoliciesFromCacheOptional;
      if (null != this.policyCache
          && (validPoliciesFromCacheOptional = this.policyCache.getPolicies()).isPresent()) {
        // cache available and valid
        LOG.debug("Will fallback to cache");
        deployedCloudPoliciesSetOriginal = validPoliciesFromCacheOptional.get();
        isFromCache = true;
      } else {
        LOG.debug("Cache unavailable or invalid");
        deployedCloudPoliciesSetOriginal = null;
      }
    }
    if (null == deployedCloudPoliciesSetOriginal) {
      // neither cloud pmp nor cache can provide data
      if (this.isOkayToDoNothing()) {
        LOG.info("No data available but it is okay to do nothing");
        return SyncSource.NO_DATA_NOOP_OKAY;
      }
      syncNotification.setInFailureMode(true);
      LOG.warn("Sync failed because of no valid data available");
      return SyncSource.NO_DATA_CURRENT_TOO_OLD;
      // TODO tell the Cloud-PMP or accept that communication is not
      // possible?
      // TODO retry vs wait for next invocation according to cron
    } else {
      // we want to prevent changes to the original Cloud-PMP response /
      // cache, so lets make a "deep clone"
      final Set<Policy> deployedCloudPoliciesSetClone = MyDataEntity.getGson().fromJson(
          MyDataEntity.getGson().toJson(deployedCloudPoliciesSetOriginal),
          new TypeToken<Set<Policy>>() {
          }.getType());
      final Map<PolicyId, Policy> deployedCloudPolicies = new HashMap<>();
      for (final Policy cloudPolicy : deployedCloudPoliciesSetClone) {
        deployedCloudPolicies.put(cloudPolicy.getPolicyId(), cloudPolicy);
      }
      // TODO maybe we want to validate the policies?
      this.syncPolicies(deployedCloudPolicies, syncNotification);

      if (null != this.policyCache && !isFromCache
          && !this.policyCache.updateCache(deployedCloudPoliciesSetOriginal)) {
        LOG.info("failed to update cache");
        this.policyCache.invalidate();
      }
    }
    return isFromCache ? SyncSource.FROM_CACHE : SyncSource.FROM_CLOUD;
  }

  private SyncSource syncTimers(SyncNotification syncNotification) {
    LOG.info("Synchronizing timers with cloud PMP");
    Set<Timer> deployedCloudTimersSetOriginal;
    boolean isFromCache = false;
    try {
      deployedCloudTimersSetOriginal = this.managementService
          .getDeployedTimers(SolutionId.fromClientId(this.clientId));
    } catch (InvalidEntityException | IOException | NoSuchEntityException e) {
      LOG.info("Failed to retrieve deployed timers from Cloud PMP", e);
      final Optional<Set<Timer>> validTimersFromCacheOptional;
      if (null != this.timerCache
          && (validTimersFromCacheOptional = this.timerCache.getTimers()).isPresent()) {
        // cache available and valid
        LOG.debug("Will fallback to cache");
        deployedCloudTimersSetOriginal = validTimersFromCacheOptional.get();
        isFromCache = true;
      } else {
        LOG.debug("Cache unavailable or invalid");
        deployedCloudTimersSetOriginal = null;
      }
    }
    if (null == deployedCloudTimersSetOriginal) {
      // neither cloud pmp nor cache can provide data
      if (this.isOkayToDoNothing()) {
        LOG.info("No data available but it is okay to do nothing");
        return SyncSource.NO_DATA_NOOP_OKAY;
      }
      syncNotification.setInFailureMode(true);
      LOG.warn("Sync failed because of no valid data available");
      return SyncSource.NO_DATA_CURRENT_TOO_OLD;
      // TODO tell the Cloud-PMP or accept that communication is not
      // possible?
      // TODO retry vs wait for next invocation according to cron
    } else {
      // we want to prevent changes to the original Cloud-PMP response /
      // cache, so lets make a "deep clone"
      final Set<Timer> deployedCloudTimersSetClone = MyDataEntity.getGson().fromJson(
          MyDataEntity.getGson().toJson(deployedCloudTimersSetOriginal),
          new TypeToken<Set<Timer>>() {
          }.getType());
      final Map<TimerId, Timer> deployedCloudTimers = new HashMap<>();
      for (final Timer cloudTimer : deployedCloudTimersSetClone) {
        deployedCloudTimers.put(cloudTimer.getTimerId(), cloudTimer);
      }
      // TODO maybe we want to validate the timers?
      this.syncTimers(deployedCloudTimers, syncNotification);

      if (null != this.timerCache && !isFromCache
          && !this.timerCache.updateCache(deployedCloudTimersSetOriginal)) {
        LOG.info("failed to update cache");
        this.timerCache.invalidate();
      }
    }
    return isFromCache ? SyncSource.FROM_CACHE : SyncSource.FROM_CLOUD;
  }

  private void notifySynchronisation(Instant now, SyncNotification syncNotification) {
    try {
      syncNotification.setSyncTime(now.toEpochMilli());
      final long nextSyncTime = this.cronExpression.getNextValidTimeAfter(Date.from(now)).getTime();
      syncNotification.setNextSyncTime(nextSyncTime);
      if (LOG.isInfoEnabled()) {
        LOG.info("Going to send SyncNotification: {}", syncNotification.toJson(false));
      }
      this.managementService.notifySync(syncNotification);
      LOG.info("SyncNotification sent.");
    } catch (final Exception e) {
      LOG.warn("Cannot send the syncNotification to CLOUD PMP", e);
    }
  }

  private void setFailureMode(boolean enable) {
    try {
      this.pdp.setFailureMode(enable);
    } catch (final IOException e) {
      LOG.warn("Error setting failure mode", e);
    }
  }

  private void syncPolicies(Map<PolicyId, Policy> deployedCloudPolicies,
      SyncNotification syncNotification) {
    try {
      final Set<Policy> localPoliciesSet = this.policyService
          .getPolicies(SolutionId.fromClientId(this.clientId));
      final Map<PolicyId, Policy> localPolicies = new HashMap<>();
      for (final Policy localPolicy : localPoliciesSet) {
        localPolicies.put(localPolicy.getPolicyId(), localPolicy);
      }
      final Set<Policy> locallyDeployedPoliciesSet = this.policyService
          .getDeployedPolicies(SolutionId.fromClientId(this.clientId));
      final Map<PolicyId, Policy> locallyDeployedPolicies = new HashMap<>();
      for (final Policy localPolicy : locallyDeployedPoliciesSet) {
        locallyDeployedPolicies.put(localPolicy.getPolicyId(), localPolicy);
      }

      // first, we add new policies
      // second, we update existing policies
      // third, we remove obsolete policies

      final Set<PolicyId> policiesToDeploy = deployedCloudPolicies.keySet().stream()
          .filter(policyId -> !locallyDeployedPolicies.containsKey(policyId))
          .collect(Collectors.toSet());
      final Set<PolicyId> policiesToUpdate = deployedCloudPolicies.keySet().stream()
          .filter(locallyDeployedPolicies::containsKey).collect(Collectors.toSet());
      final Set<PolicyId> policiesToDelete = localPolicies.keySet().stream()
          .filter(policyId -> !deployedCloudPolicies.containsKey(policyId))
          .collect(Collectors.toSet());

      for (final PolicyId policyId : policiesToDeploy) {
        final Policy policyToDeploy = deployedCloudPolicies.get(policyId);
        final Policy existingPolicy = localPolicies.get(policyId);
        if (existingPolicy != null) {
          // this policy is not deployed but exists on local PMP, delete it so
          // that we can replace it.
          try {
            this.policyService.deletePolicy(policyId);
          } catch (final NoSuchEntityException e) {
            // is okay
          }
        }
        policyToDeploy.setDeployed(false);
        this.policyService.addPolicy(policyToDeploy);
        this.policyService.deployPolicy(policyId);
        syncNotification.addDeployedPolicyVersion(policyId, policyToDeploy.getModificationTime());
      }

      for (final PolicyId policyId : policiesToUpdate) {
        final Policy policyToUpdate = deployedCloudPolicies.get(policyId);
        final Policy existingPolicy = localPolicies.get(policyId);
        if (existingPolicy.getModificationTime() != policyToUpdate.getModificationTime()) {
          // policy version does not match, update
          policyToUpdate.setDeployed(true);
          this.policyService.updatePolicy(policyToUpdate);
        } // else: policy is already up to date
        syncNotification.addDeployedPolicyVersion(policyId, policyToUpdate.getModificationTime());
      }

      for (final PolicyId policyId : policiesToDelete) {
        if (locallyDeployedPolicies.containsKey(policyId)) {
          this.policyService.revokePolicy(policyId);
        }
        try {
          this.policyService.deletePolicy(policyId);
        } catch (final NoSuchEntityException e) {
          // is okay
        }
      }
    } catch (final Exception e) {
      LOG.warn("Error while syncing policies with cloud. Vote for failure mode. Cause: {}",
          e.getMessage(), e);
      if (syncNotification.getDeployedPolicyVersions() != null) {
        syncNotification.getDeployedPolicyVersions().clear();
      }
      syncNotification.setInFailureMode(true);
    }
  }

  private void syncTimers(Map<TimerId, Timer> deployedCloudTimers,
      SyncNotification syncNotification) {
    try {
      final Set<Timer> localTimersSet = this.timerService
          .getTimers(SolutionId.fromClientId(this.clientId));
      final Map<TimerId, Timer> localTimers = new HashMap<>();
      for (final Timer localTimer : localTimersSet) {
        localTimers.put(localTimer.getTimerId(), localTimer);
      }
      final Set<Timer> locallyDeployedTimersSet = this.timerService
          .getDeployedTimers(SolutionId.fromClientId(this.clientId));
      final Map<TimerId, Timer> locallyDeployedTimers = new HashMap<>();
      for (final Timer localTimer : locallyDeployedTimersSet) {
        locallyDeployedTimers.put(localTimer.getTimerId(), localTimer);
      }

      // first, we add new timers
      // second, we update existing timers
      // third, we remove obsolete timers

      final Set<TimerId> timersToDeploy = deployedCloudTimers.keySet().stream()
          .filter(timerId -> !locallyDeployedTimers.containsKey(timerId))
          .collect(Collectors.toSet());
      final Set<TimerId> timersToUpdate = deployedCloudTimers.keySet().stream()
          .filter(locallyDeployedTimers::containsKey).collect(Collectors.toSet());
      final Set<TimerId> timersToDelete = localTimers.keySet().stream()
          .filter(timerId -> !deployedCloudTimers.containsKey(timerId)).collect(Collectors.toSet());

      for (final TimerId timerId : timersToDeploy) {
        final Timer timerToDeploy = deployedCloudTimers.get(timerId);
        final Timer existingTimer = localTimers.get(timerId);
        if (existingTimer != null) {
          // this timer is not deployed but exists on local PMP, delete it so
          // that we can replace it.
          try {
            this.timerService.deleteTimer(timerId);
          } catch (final NoSuchEntityException e) {
            // is okay
          }
        }
        timerToDeploy.setDeployed(false);
        this.timerService.addTimer(timerToDeploy);
        this.timerService.deployTimer(timerId);
        syncNotification.addDeployedTimerVersion(timerId, timerToDeploy.getModificationTime());
      }

      for (final TimerId timerId : timersToUpdate) {
        final Timer timerToUpdate = deployedCloudTimers.get(timerId);
        final Timer existingTimer = localTimers.get(timerId);
        if (existingTimer.getModificationTime() != timerToUpdate.getModificationTime()) {
          // timer version does not match, update
          timerToUpdate.setDeployed(true);
          this.timerService.updateTimer(timerToUpdate);
        } // else: timer is already up to date
        syncNotification.addDeployedTimerVersion(timerId, timerToUpdate.getModificationTime());
      }

      for (final TimerId timerId : timersToDelete) {
        if (locallyDeployedTimers.containsKey(timerId)) {
          this.timerService.revokeTimer(timerId);
        }
        try {
          this.timerService.deleteTimer(timerId);
        } catch (final NoSuchEntityException e) {
          // is okay
        }
      }
    } catch (final Exception e) {
      LOG.warn("Error while syncing timers with cloud. Vote for failure mode. Cause: {}",
          e.getMessage(), e);
      if (syncNotification.getDeployedTimerVersions() != null) {
        syncNotification.getDeployedTimerVersions().clear();
      }
      syncNotification.setInFailureMode(true);
    }
  }

  @Override
  public void pushPep(PepComponentInformation pepComponentInformation)
      throws SynchronizerException {
    if (this.masterClient) {
      try {
        MyDataEntity.validateAndNullCheck(pepComponentInformation);
        if (this.managementService.pepExists(pepComponentInformation.getComponentId())) {
          // TODO only update when there are changes?
          this.managementService.updatePep(pepComponentInformation);
        } else {
          this.managementService.addPep(pepComponentInformation);
        }
        LOG.info("Pushed registration of {}", pepComponentInformation.getComponentId());
      } catch (final Exception e) {
        throw new SynchronizerException(e.getMessage(), e);
      }
    } else {
      LOG.debug("I am no master, will not push the registration of component with id {}",
          pepComponentInformation == null ? null : pepComponentInformation.getComponentId());
    }
  }

  @Override
  public void pushPip(PipComponentInformation pipComponentInformation)
      throws SynchronizerException {
    if (this.masterClient) {
      try {
        MyDataEntity.validateAndNullCheck(pipComponentInformation);
        if (this.managementService.pipExists(pipComponentInformation.getComponentId())) {
          // TODO only update when there are changes?
          this.managementService.updatePip(pipComponentInformation);
        } else {
          this.managementService.addPip(pipComponentInformation);
        }
        LOG.info("Pushed registration of {}", pipComponentInformation.getComponentId());
      } catch (final Exception e) {
        throw new SynchronizerException(e.getMessage(), e);
      }
    } else {
      LOG.debug("I am no master, will not push the registration of component with id {}",
          pipComponentInformation == null ? null : pipComponentInformation.getComponentId());
    }
  }

  @Override
  public void pushPxp(PxpComponentInformation pxpComponentInformation)
      throws SynchronizerException {
    if (this.masterClient) {
      try {
        MyDataEntity.validateAndNullCheck(pxpComponentInformation);
        if (this.managementService.pxpExists(pxpComponentInformation.getComponentId())) {
          // TODO only update when there are changes?
          this.managementService.updatePxp(pxpComponentInformation);
        } else {
          this.managementService.addPxp(pxpComponentInformation);
        }
        LOG.info("Pushed registration of {}", pxpComponentInformation.getComponentId());
      } catch (final Exception e) {
        throw new SynchronizerException(e.getMessage(), e);
      }
    } else {
      LOG.debug("I am no master, will not push the registration of component with id {}",
          pxpComponentInformation == null ? null : pxpComponentInformation.getComponentId());
    }
  }

  private boolean isOkayToDoNothing() {
    // it is okay to do nothing when the last successful sync with cloud pmp was
    // within maxPolicyAge
    if (null == this.lastSuccessfulSyncWithCloudTimeReference.get()) {
      // no successful sync with cloud in past
      return false;
    } else {
      // successful sync with cloud in past
      if (null == this.maxAge) {
        // no time restrictions
        return true;
      } else {
        // there is a time restriction
        // we need to check whether last successful sync with cloud is too far
        // in the past...
        final Instant now = this.instantSupplier.get();
        final long diffInMillis = (now.toEpochMilli()
            - this.lastSuccessfulSyncWithCloudTimeReference.get());
        return diffInMillis <= this.maxAge.getSeconds() * 1000;
      }
    }
  }

  private void takeNoteOfSuccessfulSyncWithCloud(Instant instant) {
    this.lastSuccessfulSyncWithCloudTimeReference.set(instant.toEpochMilli());
  }

  private enum SyncSource {
    FROM_CLOUD, // successful sync
    FROM_CACHE, // sync from cache data
    NO_DATA_NOOP_OKAY, // no data available but it is okay to do nothing (maxAge)
    NO_DATA_CURRENT_TOO_OLD // no data available and last successful sync with cloud too long ago
  }

  @DisallowConcurrentExecution
  public static class SyncJob implements Job {
    @Override
    public void execute(JobExecutionContext context) {
      final JobDataMap dataMap = context.getJobDetail().getJobDataMap();
      final CloudSynchronizer syncService = (CloudSynchronizer) dataMap.get("syncService");
      syncService.sync();
    }
  }
}
