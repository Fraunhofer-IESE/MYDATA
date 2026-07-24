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

package de.fraunhofer.iese.mydata.pdp.utils;

import de.fraunhofer.iese.mydata.component.information.method.MethodInterfaceDescription;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyInformationPoint;
import de.fraunhofer.iese.mydata.pdp.interfaces.IPipCache;
import de.fraunhofer.iese.mydata.policy.PipRequest;
import de.fraunhofer.iese.mydata.policy.exception.InformationUndeterminableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of a PIP operator cache as a singleton. The results are stored
 * within a {@link ConcurrentHashMap} with the PIP
 * {@link MethodInterfaceDescription} as key. According to the supplied TTL, a
 * separate thread takes care of cleaning up the cache. The TTL time (used for
 * clean-up interval) is always as high, as the lowest number passed to
 * {@link IPipCache#getEvalResultFromCache(long, MethodInterfaceDescription, PipRequest, SolutionId)}.
 * If the cache is empty, the clean-up thread is stopped.
 */
public class PipOperatorCache implements IPipCache {

  /**
   * The Constant CLEANUP_THREADPOOL_SIZE.
   */
  private static final int CLEANUP_THREADPOOL_SIZE = 1;

  /**
   * The Constant INITIAL_CAPACITY.
   */
  private static final int INITIAL_CAPACITY = 10;

  /**
   * The Constant INITIAL_SCHEDULING_DELAY_SECONDS.
   */
  private static final int INITIAL_SCHEDULING_DELAY_SECONDS = 0;

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PipOperatorCache.class);

  /**
   * The Constant pipCache.
   */
  private final ConcurrentHashMap<PipRequest, PipResponseDetail> pipCache;

  /**
   * The Constant scheduledExecutor.
   */
  private final ScheduledExecutorService scheduledExecutor;

  private final ConnectorCache connectorCache;

  /**
   * The current TTL.
   */
  private long currentTTL = Long.MAX_VALUE;

  /**
   * The scheduled cleanup task.
   */
  private ScheduledFuture<?> scheduledCleanupTask;

  /**
   * Method to clean-up the cache. Thread is canceled if the cache is empty.
   */
  private final Runnable cacheCleanup = new Runnable() {
    @Override
    public void run() {
      LOG.info("Starting PIP cache cleanup...");
      final Set<Entry<PipRequest, PipResponseDetail>> entries = pipCache.entrySet();
      for (final Entry<PipRequest, PipResponseDetail> entry : entries) {
        if (!entry.getValue().isResultUpToDate()) {
          pipCache.remove(entry.getKey());
        }
      }

      // If cache is empty, there is no need for further cleanup. Stop thread
      if (pipCache.size() == 0 && null != scheduledCleanupTask) {
        LOG.debug("No more clean up necessary, cache is empty. Stopping thread.");
        resetCleanupTask();
      }

      LOG.info("PIP cache cleanup finished...");
    }
  };

  /**
   * Method to update the scheduler interval to a new value.
   */
  private final Runnable updateSchedulerInterval = new Runnable() {
    @Override
    public void run() {
      LOG.info("Setting up a cleanup task...");
      final long ttlValueBackup = currentTTL;
      resetCleanupTask();
      currentTTL = ttlValueBackup;
      scheduledCleanupTask = scheduledExecutor.scheduleAtFixedRate(cacheCleanup, INITIAL_SCHEDULING_DELAY_SECONDS, currentTTL, TimeUnit.MILLISECONDS);
    }
  };

  /**
   * Instantiates a new PIP operator cache.
   * 
   * @param connectorCache
   */
  public PipOperatorCache(ConnectorCache connectorCache) {
    pipCache = new ConcurrentHashMap<>(INITIAL_CAPACITY);
    scheduledExecutor = Executors.newScheduledThreadPool(CLEANUP_THREADPOOL_SIZE);
    this.connectorCache = connectorCache;
  }

  /**
   * Cancels the cleanup task thread. Resets the currentTTL value.
   */
  private void resetCleanupTask() {
    // TODO check thread safety
    currentTTL = Long.MAX_VALUE;
    if (null != scheduledCleanupTask) {
      scheduledCleanupTask.cancel(false);
      scheduledCleanupTask = null;
    }
  }

  /*
   * (non-Javadoc)
   * @see IPipCache#clearCache()
   */
  @Override
  public void clearCache() {
    resetCleanupTask();
    pipCache.clear();
  }

  /*
   * (non-Javadoc)
   * @see IPipCache# getEvalResultFromCache(long,
   * de.fraunhofer.iese.mydata.api.component.description.
   * MethodInterfaceDescription,
   * de.fraunhofer.iese.mydata.api.policy.PipRequest)
   */
  @Override
  public DataObject<?> getEvalResultFromCache(final long ttl, final MethodInterfaceDescription pipQuery, final PipRequest pipRequest, SolutionId solutionId)
      throws IOException, InformationUndeterminableException {
    final PipResponseDetail cachedResponse = pipCache.get(pipRequest);
    if (null != cachedResponse) {
      LOG.debug("PIP cache hit. Checking ttl...");

      if (cachedResponse.isResultUpToDate()) {
        LOG.debug("Last PIP connection time is within the TTL. Returing cached answer: {}", cachedResponse.getResult());
        this.updateSchedulingInterval(ttl);
        return cachedResponse.getResult();
      } else {
        LOG.info("Cached content is too old. Updating cache.");
      }
    }
    LOG.debug("No cached PIP response available. Updating cache.");
    return this.updateCache(cachedResponse, pipQuery, pipRequest, ttl, solutionId);
  }

  /**
   * Triggers an evaluation of the PIP.
   *
   * @param pipQuery The {@link MethodInterfaceDescription} for the needed PIP.
   * @param pipRequest The {@link PipRequest} object that contains method,
   *          parameters and default answer.
   * @param solutionId
   * @return NULL in case of an error (e.g. connection error, evaluation error)
   *         or the evaluation result in case of success.
   * @throws IOException In case of an I/O exception.
   * @throws InformationUndeterminableException In case an evaluation is not
   *           possible.
   */
  protected DataObject<?> reevaluatePip(final MethodInterfaceDescription pipQuery, final PipRequest pipRequest, SolutionId solutionId) throws IOException, InformationUndeterminableException {
    final IPolicyInformationPoint pipConnection = connectorCache.getPipConnectionFromCache(pipQuery, solutionId);

    // add the scope to log messages mdc
    MDC.put("solution", solutionId.getUrn());

    if (null == pipConnection) {
      LOG.warn("Error during PIP lookup (null)");
      throw new IOException("Error during PIP lookup (null)");
    }

    try {
      LOG.trace("Triggering PIP evaluation...");
      final DataObject<?> evalResult = pipConnection.evaluate(pipRequest);
      LOG.info("Got PIP response: {}", evalResult);
      return evalResult;
    } catch (IOException | InformationUndeterminableException e) {
      LOG.warn("Error during PIP evaluation: ", e);
      throw e;
    } finally {
      // remove the solution from the mdc
      MDC.remove("solution");
    }

  }

  /**
   * Updates the internal cache if e.g. the stored response is outdated and the
   * PIP evaluation is successful. In this case, the clean-up thread will be
   * updated if neccessary.
   *
   * @param cachedResponse the currently stored response
   * @param pipQuery The {@link MethodInterfaceDescription} for the needed PIP.
   * @param pipRequest The {@link PipRequest} object that contains method,
   *          parameters and default answer.
   * @param ttl time to live for the PIP result. In case the ttl is zero, the
   *          PIP response will not be cached.
   * @param solutionId
   * @return NULL in case of an error (e.g. connection error, evaluation error)
   *         or the evaluation result in case of success.
   * @throws IOException In case of an I/O exception.
   * @throws InformationUndeterminableException In case an evaluation is not
   *           possible.
   */
  private DataObject<?> updateCache(final PipResponseDetail cachedResponse, final MethodInterfaceDescription pipQuery, final PipRequest pipRequest, final long ttl, SolutionId solutionId)
      throws IOException, InformationUndeterminableException {
    final DataObject<?> evalResult = this.reevaluatePip(pipQuery, pipRequest, solutionId);
    if (0 == ttl) { // Response should and will not be cached!
      return evalResult;
    } else if (null != evalResult) {
      pipCache.put(pipRequest, new PipResponseDetail(DataObject.constructClone(evalResult), ttl));
      this.updateSchedulingInterval(ttl);
      return evalResult;
    }
    // Cached value is outdated. Delete it!
    if (cachedResponse != null) {
      pipCache.remove(pipRequest);
    }
    return null;
  }

  /**
   * Helper to update the current TTL and thread scheduling to a new value.
   * Update is done, if {@link #currentTTL} < newTTL.
   *
   * @param newTTL The new TTL value.
   */
  private void updateSchedulingInterval(final long newTTL) {
    if (currentTTL <= newTTL) {
      return;
    }
    currentTTL = newTTL;
    scheduledExecutor.schedule(updateSchedulerInterval, 1, TimeUnit.SECONDS);
  }

  /**
   * Wrapper class to store a PIP response together with its TTL.
   */
  private final class PipResponseDetail {

    /**
     * The result.
     */
    private final DataObject<?> result;

    /**
     * The last update.
     */
    private final long lastUpdate;

    /**
     * The time to live.
     */
    private final long timeToLive;

    /**
     * Instantiates a new PIP response detail.
     *
     * @param res the res
     * @param ttl the ttl
     */
    public PipResponseDetail(final DataObject<?> res, final long ttl) {
      this.result = res;
      this.lastUpdate = System.currentTimeMillis();
      this.timeToLive = ttl;
    }

    /**
     * Gets the result.
     *
     * @return the result
     */
    public DataObject<?> getResult() {
      return DataObject.constructClone(this.result);
    }

    /**
     * Checks if is result up to date.
     *
     * @return true, if is result up to date
     */
    public boolean isResultUpToDate() {
      return (System.currentTimeMillis() - this.lastUpdate) < this.timeToLive;
    }
  }
}
