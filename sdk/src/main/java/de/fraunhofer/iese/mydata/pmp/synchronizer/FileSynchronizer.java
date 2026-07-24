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

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.component.information.PepComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PipComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PxpComponentInformation;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyDecisionPoint;
import de.fraunhofer.iese.mydata.exception.ConflictingResourceException;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.exception.ResourceUpdateException;
import de.fraunhofer.iese.mydata.policy.IPolicyService;
import de.fraunhofer.iese.mydata.policy.Policy;
import de.fraunhofer.iese.mydata.policy.PolicyDeployableGroup;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.timer.ITimerService;
import de.fraunhofer.iese.mydata.timer.Timer;
import de.fraunhofer.iese.mydata.timer.TimerDeployableGroup;
import de.fraunhofer.iese.mydata.timer.TimerId;

import com.google.common.annotations.VisibleForTesting;
import jakarta.validation.groups.Default;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileSynchronizer implements ISyncService {
  public static final String FILE_SYNC_THREAD_NAME = "MYDATA-File-Sync-Thread";

  private static final String POLICY_FILE_EXTENSION_WITH_PRECEDING_DOT = ".mdpx";

  private static final String TIMER_FILE_EXTENSION_WITH_PRECEDING_DOT = ".mdtx";

  private static final Logger logger = LoggerFactory.getLogger(FileSynchronizer.class);

  private final PathMatcher policyMatcher = FileSystems.getDefault()
      .getPathMatcher("glob:*{" + POLICY_FILE_EXTENSION_WITH_PRECEDING_DOT + "}");

  private final PathMatcher timerMatcher = FileSystems.getDefault()
      .getPathMatcher("glob:*{" + TIMER_FILE_EXTENSION_WITH_PRECEDING_DOT + "}");

  private final Map<URI, PolicyId> filePolicyMapping = new HashMap<>();

  private final Map<URI, TimerId> fileTimerMapping = new HashMap<>();

  private final Path deployedPoliciesAndTimersPath;

  private final Runnable task;

  private final IPolicyService policyService;

  private final ITimerService timerService;

  private final IPolicyDecisionPoint pdp;

  private final AtomicReference<Thread> threadReference = new AtomicReference<>(null);

  public FileSynchronizer(String path, ITimerService localTimerService,
      IPolicyService localPolicyService, IPolicyDecisionPoint localPdp) {
    this.policyService = localPolicyService;
    this.timerService = localTimerService;
    this.pdp = localPdp;
    this.deployedPoliciesAndTimersPath = Paths.get(path);
    this.task = () -> {
      try {

        try {
          logger.info("Starting initial Sync.");
          this.initialSync();
        } catch (final Exception e) {
          logger.warn("Initial Sync from {} failed.", this.deployedPoliciesAndTimersPath, e);
          throw e;
        }
        logger.info("Initial Sync from {} succeeded.", this.deployedPoliciesAndTimersPath);
        this.pdp.setFailureMode(false);
        if (logger.isDebugEnabled()) {
          logger.debug("Currently registered files to policy:\n{}",
              this.mapToStringForPolicy(this.filePolicyMapping));
          logger.debug("Currently registered files to timer:\n{}",
              this.mapToStringForTimer(this.fileTimerMapping));
        }

        try (final WatchService watcher = FileSystems.getDefault().newWatchService()) {
          this.deployedPoliciesAndTimersPath.register(watcher, ENTRY_CREATE, ENTRY_DELETE,
              ENTRY_MODIFY);
          while (!Thread.currentThread().isInterrupted()) {
            logger.debug("Waiting for changes");
            final WatchKey key = watcher.take();
            Thread.sleep(50); // wait for non-atomic file modifications to finish?

            this.processChanges(key);
            // TODO handle exceptions

            logger.debug("All changes processed.");
            if (logger.isDebugEnabled()) {
              logger.debug("Currently registered files to policy:\n{}",
                  this.mapToStringForPolicy(this.filePolicyMapping));
              logger.debug("Currently registered files to timer:\n{}",
                  this.mapToStringForTimer(this.fileTimerMapping));
            }
            if (!key.reset()) { // prepare for new notifications
              // key is no longer valid
              logger.debug("WatchKey no longer valid, going to stop the Synchronzier");
              break;
            }
          }
        } catch (final InterruptedException e) {
          logger.debug("File listener was interrupted, that is the signal to shutdown", e);
          Thread.currentThread().interrupt(); // re-interrupt (best practice)
        }
      } catch (final Exception e) {
        logger.warn("Unhandled exception in FileSynchronizer Task.", e);
      } finally {
        logger
            .warn("FileSynchronizer Task is ending, failureMode of PDP is going to be activated.");
        try {
          this.pdp.setFailureMode(true);
        } catch (final IOException e) {
          logger.warn("Cannot set PDP to failureMode", e);
        }
        this.threadReference.set(null);
      }
    };
  }

  private String mapToStringForPolicy(final Map<URI, PolicyId> map) {
    return map.keySet().stream().map(key -> key + " <--> " + map.get(key).getUrn())
        .collect(Collectors.joining("\n"));
  }

  private String mapToStringForTimer(final Map<URI, TimerId> map) {
    return map.keySet().stream().map(key -> key + " <--> " + map.get(key).getUrn())
        .collect(Collectors.joining("\n"));
  }

  private void initialSync() throws IOException, ConflictingResourceException,
      InvalidEntityException, ResourceUpdateException, NoSuchEntityException {
    try (final Stream<Path> paths = Files.walk(this.deployedPoliciesAndTimersPath, 1)) {
      final List<Path> filePathList = paths.collect(Collectors.toList());
      final List<Path> policyFilePathList = filePathList
          .stream().filter(Files::isRegularFile).filter(filePath -> filePath.getFileName()
              .toString().endsWith(POLICY_FILE_EXTENSION_WITH_PRECEDING_DOT))
          .collect(Collectors.toList());
      for (final Path filePath : policyFilePathList) {
        this.handleExistingPolicyOnInit(filePath);
      }
      final List<Path> timerFilePathList = filePathList
          .stream().filter(Files::isRegularFile).filter(filePath -> filePath.getFileName()
              .toString().endsWith(TIMER_FILE_EXTENSION_WITH_PRECEDING_DOT))
          .collect(Collectors.toList());
      for (final Path filePath : timerFilePathList) {
        this.handleExistingTimerOnInit(filePath);
      }
    }
  }

  @Override
  public void start() {
    final Thread t = new Thread(this.task, FILE_SYNC_THREAD_NAME);
    if (this.threadReference.compareAndSet(null, t)) {
      logger.info("Starting FileSynchronizer");
      t.start();
    } else {
      logger.info("FileSynchronizer already running");
    }
  }

  @Override
  public void stop() {
    final Thread t = this.threadReference.getAndSet(null);
    if (null != t) {
      logger.info("Stopping FileSynchronizer");
      t.interrupt();
      try {
        t.join();
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    } else {
      logger.info("FileSynchronizer not running");
    }
  }

  @VisibleForTesting
  void processChanges(WatchKey key) throws InvalidEntityException, ResourceUpdateException,
      ConflictingResourceException, IOException, NoSuchEntityException {
    // TODO handle renaming?
    for (final WatchEvent<?> event : key.pollEvents()) {
      final WatchEvent.Kind<?> kind = event.kind();

      @SuppressWarnings("unchecked")
      final WatchEvent<Path> ev = (WatchEvent<Path>) event;
      final Path fileName = ev.context();

      if (this.policyMatcher.matches(fileName)) {
        logger.debug("Event {} about File {} is relevant as it contains a policy", kind, fileName);
        if (kind == ENTRY_MODIFY
            && this.filePolicyMapping.containsKey(this.fileNameToUri(fileName))) {
          this.handleModifyPolicy(fileName);
        } else if (kind == ENTRY_DELETE
            && this.filePolicyMapping.containsKey(this.fileNameToUri(fileName))) {
          this.handleDeletePolicy(fileName);
        } else if (kind == ENTRY_CREATE
            && !this.filePolicyMapping.containsKey(this.fileNameToUri(fileName))) {
          this.handleCreatePolicy(fileName);
        } else {
          throw new ResourceUpdateException("invalid file change for file " + fileName);
        }
      } else if (this.timerMatcher.matches(fileName)) {
        logger.debug("Event {} about File {} is relevant as it contains a timer", kind, fileName);
        if (kind == ENTRY_MODIFY
            && this.fileTimerMapping.containsKey(this.fileNameToUri(fileName))) {
          this.handleModifyTimer(fileName);
        } else if (kind == ENTRY_DELETE
            && this.fileTimerMapping.containsKey(this.fileNameToUri(fileName))) {
          this.handleDeleteTimer(fileName);
        } else if (kind == ENTRY_CREATE
            && !this.fileTimerMapping.containsKey(this.fileNameToUri(fileName))) {
          this.handleCreateTimer(fileName);
        } else {
          throw new ResourceUpdateException("invalid file change for file " + fileName);
        }
      } else {
        logger.debug("Event {} about File {} not relevant", kind, fileName);
      }
    }
  }

  private URI fileNameToUri(Path fileName) {
    return Paths.get(this.deployedPoliciesAndTimersPath.toString(), fileName.toString()).toUri();
  }

  private void handleDeleteTimer(Path fileName) throws IOException, ResourceUpdateException,
      InvalidEntityException, NoSuchEntityException, ConflictingResourceException {
    final URI uri = this.fileNameToUri(fileName);
    final TimerId id = this.fileTimerMapping.get(uri);
    MyDataEntity.validateAndNullCheck(id);
    this.timerService.revokeTimer(id);
    this.timerService.deleteTimer(id);
    this.filePolicyMapping.remove(uri);
    logger.info("Revoked and deleted timer {}", id);
  }

  private void handleModifyTimer(Path fileName) throws IOException, InvalidEntityException,
      NoSuchEntityException, ResourceUpdateException, ConflictingResourceException {
    final Timer timer = this.readAndValidateTimerFromFilePath(
        Paths.get(this.deployedPoliciesAndTimersPath.toString(), fileName.toString()));
    final URI uri = this.fileNameToUri(fileName);
    final TimerId timerIdOfExistingTimer = this.fileTimerMapping.get(uri);
    if (timerIdOfExistingTimer != null && timerIdOfExistingTimer.equals(timer.getTimerId())) {
      // unchanged timerId
      timer.setDeployed(true);
      this.timerService.updateTimer(timer);
      logger.info("Updated timer {}", timer.getTimerId());
    } else {
      // changed timerId
      this.timerService.addTimer(timer);
      this.timerService.deployTimer(timer.getTimerId());
      logger.info("Added and deployed timer {}", timer.getTimerId());
      this.timerService.revokeTimer(timerIdOfExistingTimer);
      this.timerService.deleteTimer(timerIdOfExistingTimer);
      logger.info("Revoked and deleted timer {}", timerIdOfExistingTimer);
    }
    this.fileTimerMapping.put(uri, timer.getTimerId());
  }

  private void handleCreateTimer(Path fileName) throws IOException, InvalidEntityException,
      ResourceUpdateException, ConflictingResourceException, NoSuchEntityException {
    final Timer timer = this.readAndValidateTimerFromFilePath(
        Paths.get(this.deployedPoliciesAndTimersPath.toString(), fileName.toString()));
    final URI uri = this.fileNameToUri(fileName);
    this.addNewTimerFromFile(timer, uri);
  }

  private void handleExistingTimerOnInit(Path filePath) throws IOException, InvalidEntityException,
      ResourceUpdateException, ConflictingResourceException, NoSuchEntityException {
    final Timer timer = this.readAndValidateTimerFromFilePath(filePath);
    this.addNewTimerFromFile(timer, this.fileNameToUri(filePath.getFileName()));
  }

  private Timer readAndValidateTimerFromFilePath(Path filePath)
      throws IOException, InvalidEntityException {
    final String fileContent = this.readFile(filePath);
    final Timer timer = new Timer(fileContent);
    MyDataEntity.validateAndNullCheck(timer, TimerDeployableGroup.class, Default.class);
    return timer;
  }

  private void addNewTimerFromFile(Timer timer, URI fileNameUri)
      throws IOException, InvalidEntityException, ResourceUpdateException,
      ConflictingResourceException, NoSuchEntityException {
    this.timerService.addTimer(timer);
    this.timerService.deployTimer(timer.getTimerId());
    this.fileTimerMapping.put(Objects.requireNonNull(fileNameUri), timer.getTimerId());
    logger.info("Added and deployed timer {}", timer.getTimerId());
  }

  private void handleDeletePolicy(Path fileName) throws IOException, ResourceUpdateException,
      InvalidEntityException, NoSuchEntityException, ConflictingResourceException {
    final URI uri = this.fileNameToUri(fileName);
    final PolicyId id = this.filePolicyMapping.get(uri);
    MyDataEntity.validateAndNullCheck(id);
    this.policyService.revokePolicy(id);
    this.policyService.deletePolicy(id);
    this.filePolicyMapping.remove(uri);
    logger.info("Revoked and deleted policy {}", id);
  }

  private void handleModifyPolicy(Path fileName) throws IOException, InvalidEntityException,
      NoSuchEntityException, ResourceUpdateException, ConflictingResourceException {
    final Policy policy = this.readAndValidatePolicyFromFilePath(
        Paths.get(this.deployedPoliciesAndTimersPath.toString(), fileName.toString()));
    final URI uri = this.fileNameToUri(fileName);
    final PolicyId policyIdOfExistingPolicy = this.filePolicyMapping.get(uri);
    if (policyIdOfExistingPolicy != null && policyIdOfExistingPolicy.equals(policy.getPolicyId())) {
      // unchanged policyId
      policy.setDeployed(true);
      this.policyService.updatePolicy(policy);
      logger.info("Updated policy {}", policy.getPolicyId());
    } else {
      // changed policyId
      this.policyService.addPolicy(policy);
      this.policyService.deployPolicy(policy.getPolicyId());
      logger.info("Added and deployed policy {}", policy.getPolicyId());
      this.policyService.revokePolicy(policyIdOfExistingPolicy);
      this.policyService.deletePolicy(policyIdOfExistingPolicy);
      logger.info("Revoked and deleted policy {}", policyIdOfExistingPolicy);
    }
    this.filePolicyMapping.put(uri, policy.getPolicyId());
  }

  private void handleCreatePolicy(Path fileName) throws IOException, InvalidEntityException,
      ResourceUpdateException, ConflictingResourceException, NoSuchEntityException {
    final Policy policy = this.readAndValidatePolicyFromFilePath(
        Paths.get(this.deployedPoliciesAndTimersPath.toString(), fileName.toString()));
    final URI uri = this.fileNameToUri(fileName);
    this.addNewPolicyFromFile(policy, uri);
  }

  private void handleExistingPolicyOnInit(Path filePath) throws IOException, InvalidEntityException,
      ResourceUpdateException, ConflictingResourceException, NoSuchEntityException {
    final Policy policy = this.readAndValidatePolicyFromFilePath(filePath);
    this.addNewPolicyFromFile(policy, this.fileNameToUri(filePath.getFileName()));
  }

  private void addNewPolicyFromFile(Policy policy, URI fileNameUri)
      throws IOException, InvalidEntityException, ResourceUpdateException,
      ConflictingResourceException, NoSuchEntityException {
    this.policyService.addPolicy(policy);
    this.policyService.deployPolicy(policy.getPolicyId());
    this.filePolicyMapping.put(Objects.requireNonNull(fileNameUri), policy.getPolicyId());
    logger.info("Added and deployed policy {}", policy.getPolicyId());
  }

  private Policy readAndValidatePolicyFromFilePath(Path filePath)
      throws IOException, InvalidEntityException {
    final String fileContent = this.readFile(filePath);
    final Policy policy = new Policy(fileContent);
    MyDataEntity.validateAndNullCheck(policy, PolicyDeployableGroup.class, Default.class);
    return policy;
  }

  private String readFile(Path filePath) throws IOException {
    return new String(Files.readAllBytes(filePath));
  }

  @Override
  public void pushPep(PepComponentInformation pepComponentInformation) {
    // intended blank, no up-sync for components
  }

  @Override
  public void pushPip(PipComponentInformation pipComponentInformation) {
    // intended blank, no up-sync for components
  }

  @Override
  public void pushPxp(PxpComponentInformation pxpComponentInformation) {
    // intended blank, no up-sync for components
  }

}
