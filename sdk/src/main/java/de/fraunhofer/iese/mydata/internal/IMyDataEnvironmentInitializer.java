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

package de.fraunhofer.iese.mydata.internal;

import de.fraunhofer.iese.mydata.IMyDataEnvironment;
import de.fraunhofer.iese.mydata.component.connector.OAuthCredentials;
import de.fraunhofer.iese.mydata.exception.InitializationException;
import de.fraunhofer.iese.mydata.policy.event.history.IEventRepository;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import java.net.URI;

import org.jspecify.annotations.Nullable;

/**
 * This interface covers the initialization of a MyDataEnvironment
 */
public interface IMyDataEnvironmentInitializer {

  /**
   * Initialize with cloud mode (cloud Pmp, cloud Pdp)
   *
   * @param solutionId the solutionId of the solution this IMyDataEnvironment
   *          will be associated with
   * @param cloudPmpUrl URL to the cloud Pmp
   * @param authentication authentication
   * @return initialized {@link IMyDataEnvironment} instance
   * @throws InitializationException when initialization fails
   */
  IMyDataEnvironment initializeCloud(SolutionId solutionId, URI cloudPmpUrl, OAuthCredentials authentication) throws InitializationException;

  /**
   * Initialize local with cloud sync
   *
   * @param solutionId the solutionId of the solution this IMyDataEnvironment
   *          will be associated with
   * @param cloudPmpUrl URL to the cloud Pmp
   * @param authentication authentication
   * @param timezone Timezone
   * @param cacheEnabled whether cache should be enabled
   * @param policyCachePath the path for the policies cache
   * @param timerCachePath the path for the timers cache
   * @param maxPolicyAge maxPolicyAge
   * @param syncSchedule syncSchedule
   * @param masterClient whether this instance is a master client
   * @param numPdpThreads numPdpThreads
   * @param whitelistModeEnabled whether Pdp whitelist mode should be enabled
   * @param eventRepository An {@link IEventRepository} instance if this feature
   *          should be enabled, else {@code null}
   * @return initialized {@link IMyDataEnvironment} instance
   * @throws InitializationException when initialization fails
   */
  IMyDataEnvironment initializeLocalWithCloudSync(SolutionId solutionId, URI cloudPmpUrl, OAuthCredentials authentication, String timezone, boolean cacheEnabled, String policyCachePath,
      String timerCachePath, String maxPolicyAge, String syncSchedule, boolean masterClient, int numPdpThreads, boolean whitelistModeEnabled, @Nullable IEventRepository eventRepository)
      throws InitializationException;

  /**
   * Initialize local with file sync
   *
   * @param solutionId the solutionId of the solution this IMyDataEnvironment
   *          will be associated with
   * @param timezone Timezone
   * @param fileSyncPath path for filesync
   * @param numPdpThreads numPdpThreads
   * @param whitelistModeEnabled whether Pdp whitelist mode should be enabled
   * @param eventRepository An {@link IEventRepository} instance if this feature
   *          should be enabled, else {@code null}
   * @return initialized {@link IMyDataEnvironment} instance
   * @throws InitializationException when initialization fails
   */
  IMyDataEnvironment initializeLocalWithFileSync(SolutionId solutionId, String timezone, String fileSyncPath, int numPdpThreads, boolean whitelistModeEnabled, @Nullable IEventRepository eventRepository)
      throws InitializationException;

  /**
   * Initialize local
   *
   * @param solutionId the solutionId of the solution this IMyDataEnvironment
   *          will be associated with
   * @param timezone Timezone
   * @param numPdpThreads numPdpThreads
   * @param whitelistModeEnabled whether Pdp whitelist mode should be enabled
   * @param eventRepository An {@link IEventRepository} instance if this feature
   *          should be enabled, else {@code null}
   * @return initialized {@link IMyDataEnvironment} instance
   * @throws InitializationException when initialization fails
   */
  IMyDataEnvironment initializeLocal(SolutionId solutionId, String timezone, int numPdpThreads, boolean whitelistModeEnabled, @Nullable IEventRepository eventRepository) throws InitializationException;
}
