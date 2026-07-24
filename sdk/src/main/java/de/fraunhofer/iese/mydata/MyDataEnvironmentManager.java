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

import de.fraunhofer.iese.mydata.component.connector.ConnectorFactory;
import de.fraunhofer.iese.mydata.component.connector.OAuthCredentials;
import de.fraunhofer.iese.mydata.exception.InitializationException;
import de.fraunhofer.iese.mydata.internal.IMyDataEnvironmentFullFace;
import de.fraunhofer.iese.mydata.internal.IMyDataEnvironmentInitializer;
import de.fraunhofer.iese.mydata.internal.IMyDataEnvironmentTechnicalAccess;
import de.fraunhofer.iese.mydata.internal.TechnicalAccessGranter;
import de.fraunhofer.iese.mydata.policy.event.history.IEventRepository;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.jspecify.annotations.Nullable;

/**
 * {@link MyDataEnvironmentManager} handles the {@link IMyDataEnvironment}, whether it is local or
 * cloud.
 * <p>
 * Simple usage example (local-mode, manual init/config):
 *
 * <pre>
 *   <code>
 *     MyDataEnvironmentManager.constructDefaultEnvironment().initializeLocal(new SolutionId("urn:solution:mysolution"), "Europe/Berlin", 4, true, null);
 *     IMyDataEnvironment myDataEnvironment = MyDataEnvironmentManager.getDefaultEnvironment();
 *     myDataEnvironment.registerLocalPxp("mailpxp", new MailPxp(...));
 *     PolicyId policyId = myDataEnvironment.getPmp().addPolicy(new Policy(...));
 *     myDataEnvironment.getPmp().deployPolicy(policyId);
 *     Event event = new EventBuilder("mysolution", "read").withParameter("text", "Hello World", String.class).getEvent();
 *     try{
 *       myDataEnvironment.getPep().enforce(event); // use of a provided default PEP
 *       System.out.println(event.getValueForName("text")); // access to the enforced event/data
 *     } catch (InhibitException e){ // handling of InhibitException
 *       System.out.println("Inhibited");
 *     } catch (EvaluationUndecidableException e) { // handling of EvaluationUndecidableException
 *       System.out.println("EvaluationUndecidable");
 *     } catch (IOException e){ // handling of IOException
 *       System.out.println("IOException");
 *     }
 *     </code>
 * </pre>
 */
public final class MyDataEnvironmentManager {

  /**
   * The id of the default {@link IMyDataEnvironment} instance to which a reference is kept by the
   * static context of {@link MyDataEnvironmentManager}.
   */
  public static final String DEFAULT_ENVIRONMENT_ID = "mydata-default-environment";

  private static final Logger LOG = LoggerFactory.getLogger(MyDataEnvironmentManager.class);

  private static final Map<String, IMyDataEnvironment> environments = new ConcurrentHashMap<>();

  private static final ReentrantLock initLock = new ReentrantLock();

  private static final AtomicBoolean enableOverwritingOfExistingMyDataEnvironments = new AtomicBoolean(
      false);

  private MyDataEnvironmentManager() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Enable overwriting of existing MyDataEnvironments for testing purpose
   */
  public static void enableOverwritingOfExistingMyDataEnvironments() {
    LOG.warn("Going to enableOverwritingOfExistingMyDataEnvironments");
    MyDataEnvironmentManager.enableOverwritingOfExistingMyDataEnvironments.set(true);
  }

  /**
   * Disable overwriting of existing MyDataEnvironments for testing purpose
   */
  public static void disableOverwritingOfExistingMyDataEnvironments() {
    LOG.warn("Going to disableOverwritingOfExistingMyDataEnvironments");
    MyDataEnvironmentManager.enableOverwritingOfExistingMyDataEnvironments.set(false);
  }

  /**
   * Static access to the default {@link IMyDataEnvironment}
   *
   * @return                       the default {@link IMyDataEnvironment}
   * @throws IllegalStateException when the default {@link IMyDataEnvironment} has not been
   *                                 initialized yet.
   */
  public static IMyDataEnvironment getDefaultEnvironment() {
    final Optional<IMyDataEnvironment> defaultEnvironmentOptional = getEnvironment(
        DEFAULT_ENVIRONMENT_ID);
    if (!defaultEnvironmentOptional.isPresent()) {
      throw new IllegalStateException(
          "Default IMyDataEnvironment unavailable. Maybe it is not yet initialized.");
    } else {
      return defaultEnvironmentOptional.get();
    }
  }

  /**
   * Access to a {@link IMyDataEnvironment} instance via environmentId
   *
   * @param  environmentId the id of the {@link IMyDataEnvironment} instance to access
   * @return               an Optional that contains the {@link IMyDataEnvironment} if available
   */
  public static Optional<IMyDataEnvironment> getEnvironment(String environmentId) {
    if (environmentId == null) {
      throw new IllegalArgumentException("environmentId must not be null.");
    }
    final Optional<IMyDataEnvironment> environmentOptional = Optional
        .ofNullable(environments.get(environmentId));
    if (!environmentOptional.isPresent()) {
      LOG.warn(
          "Tried to retrieve non-existing IMyDataEnvironment with id {}. Returning empty Optional.",
          environmentId);
    }
    return environmentOptional;
  }

  /**
   * Method to create {@link IMyDataEnvironment} instances
   *
   * <pre>
   *   <code>IMyDataEnvironment myDataEnvironment = MyDataEnvironmentManager.constructEnvironment("my-id").initializeLocal(...);</code>
   * </pre>
   * <p>
   * Multiple Environments are not supported at the moment, so there is only a "Default Environment"
   *
   * @param  environmentId the environmentId of the {@link IMyDataEnvironment} to create
   * @return               a {@link Builder} that can be initialized
   * @see                  MyDataEnvironmentManager#constructDefaultEnvironment()
   */
  private static IMyDataEnvironmentInitializer constructEnvironment(String environmentId) {
    return new Builder(environmentId, new ConnectorFactory());
  }

  /**
   * Method to create the default {@link IMyDataEnvironment} instance
   *
   * <pre>
   *   <code>MyDataEnvironment myDataEnvironment = MyDataEnvironmentManager.constructDefaultEnvironment().initializeLocal(...);</code>
   * </pre>
   * <p>
   * Multiple Environments are not supported at the moment, so there is only the "Default
   * Environment"
   *
   * @return a {@link IMyDataEnvironmentInitializer} that can be initialized
   */
  public static IMyDataEnvironmentInitializer constructDefaultEnvironment() {
    return constructEnvironment(DEFAULT_ENVIRONMENT_ID);
  }

  private static void destroyIfExists(String environmentId) {
    MyDataEnvironmentManager.initLock.lock();
    try {
      final IMyDataEnvironment oldEnvironment = MyDataEnvironmentManager.environments
          .remove(environmentId);
      if (oldEnvironment != null) {
        final IMyDataEnvironmentTechnicalAccess myDataEnvironmentTechnicalAccess = TechnicalAccessGranter
            .getTechnicalAccess(oldEnvironment);
        myDataEnvironmentTechnicalAccess.destroy();
      }
    } finally {
      MyDataEnvironmentManager.initLock.unlock();
    }
  }

  /**
   * A builder for managed {@link IMyDataEnvironment} instances
   *
   * @see MyDataEnvironmentManager#constructDefaultEnvironment()
   */
  static final class Builder implements IMyDataEnvironmentInitializer {
    private final String environmentId;

    private final ConnectorFactory connectorFactory;

    private Builder(String environmentId, ConnectorFactory connectorFactory) {
      this.environmentId = environmentId;
      this.connectorFactory = connectorFactory;
    }

    @Override
    public IMyDataEnvironment initializeCloud(SolutionId solutionId, URI cloudPmpUrl,
        OAuthCredentials authentication) throws InitializationException {
      return this.initialize((myDataEnvironment) -> myDataEnvironment.initializeCloud(solutionId,
          cloudPmpUrl, authentication));
    }

    @Override
    public IMyDataEnvironment initializeLocalWithCloudSync(SolutionId solutionId, URI cloudPmpUrl,
        OAuthCredentials authentication, String timezone, boolean cacheEnabled,
        String policyCachePath, String timerCachePath, String maxPolicyAndTimerAge,
        String syncSchedule, boolean masterClient, int numPdpThreads, boolean whitelistModeEnabled,
        @Nullable IEventRepository eventRepository) throws InitializationException {
      return this.initialize((myDataEnvironment) -> myDataEnvironment.initializeLocalWithCloudSync(
          solutionId, cloudPmpUrl, authentication, timezone, cacheEnabled, policyCachePath,
          timerCachePath, maxPolicyAndTimerAge, syncSchedule, masterClient, numPdpThreads,
          whitelistModeEnabled, eventRepository));
    }

    @Override
    public IMyDataEnvironment initializeLocalWithFileSync(SolutionId solutionId, String timezone,
        String fileSyncPath, int numPdpThreads, boolean whitelistModeEnabled,
        @Nullable IEventRepository eventRepository) throws InitializationException {
      return this.initialize(
          (myDataEnvironment) -> myDataEnvironment.initializeLocalWithFileSync(solutionId, timezone,
              fileSyncPath, numPdpThreads, whitelistModeEnabled, eventRepository));
    }

    @Override
    public IMyDataEnvironment initializeLocal(SolutionId solutionId, String timezone,
        int numPdpThreads, boolean whitelistModeEnabled, @Nullable IEventRepository eventRepository)
        throws InitializationException {
      return this.initialize((myDataEnvironment) -> myDataEnvironment.initializeLocal(solutionId,
          timezone, numPdpThreads, whitelistModeEnabled, eventRepository));
    }

    private IMyDataEnvironment initialize(Consumer<IMyDataEnvironmentInitializer> initConsumer)
        throws InitializationException {
      MyDataEnvironmentManager.initLock.lock();
      try {
        if (MyDataEnvironmentManager.environments.containsKey(this.environmentId)) {
          if (MyDataEnvironmentManager.enableOverwritingOfExistingMyDataEnvironments.get()) {
            LOG.warn(
                "--- !!! --- A previously registered IMyDataEnvironment will be replaced. This should not happen in productive environments but may be okay for unit tests. EnvironmentId is: {}",
                this.environmentId);
            MyDataEnvironmentManager.destroyIfExists(this.environmentId);
            assert !MyDataEnvironmentManager.environments
                .containsKey(this.environmentId) : "old environment should be destroyed";
          } else {
            throw new InitializationException("IMyDataEnvironment with id "
                + this.environmentId
                + " already exists. When you are writing tests, you can enable this via MyDataEnvironmentManager::enableOverwritingOfExistingMyDataEnvironments");
          }
        }
        assert !MyDataEnvironmentManager.environments
            .containsKey(this.environmentId) : "there is no environment with this id";
        final IMyDataEnvironmentFullFace myDataEnvironment = new MyDataEnvironmentFullFace(
            this.environmentId, this.connectorFactory);
        initConsumer.accept(myDataEnvironment);
        this.publishToEnvironmentRegistry(myDataEnvironment);
        assert MyDataEnvironmentManager.environments
            .containsKey(this.environmentId) : "environment should be registered now";
        return myDataEnvironment;
      } catch (final InitializationException e) {
        // TODO maybe we want to destroy the partially initialized MyDataEnvironment instance?
        throw e;
      } catch (final Exception e) {
        // TODO maybe we want to destroy the partially initialized MyDataEnvironment instance?
        throw new InitializationException(e.getMessage(), e);
      } finally {
        MyDataEnvironmentManager.initLock.unlock();
      }
    }

    private void publishToEnvironmentRegistry(final IMyDataEnvironment myDataEnvironment) {
      MyDataEnvironmentManager.environments.put(this.environmentId, myDataEnvironment);
      LOG.info("IMyDataEnvironment with id {} registered.", this.environmentId);
    }

    private interface Consumer<T> {
      void accept(T t) throws InitializationException;
    }
  }

}
