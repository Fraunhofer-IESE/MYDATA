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

package de.fraunhofer.iese.mydata.pep;

import de.fraunhofer.iese.mydata.IMyDataEnvironment;
import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.health.HealthStatus;
import de.fraunhofer.iese.mydata.component.health.Status;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyDecisionPoint;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyEnforcementPoint;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.internal.TechnicalAccessGranter;
import de.fraunhofer.iese.mydata.pep.common.DecisionEnforcer;
import de.fraunhofer.iese.mydata.policy.decision.AuthorizationDecision;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.exception.InhibitException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

/**
 * This class should be used with Spring auto registration
 */
public class DefaultPolicyEnforcementPoint implements IPolicyEnforcementPoint {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultPolicyEnforcementPoint.class);

  protected final IMyDataEnvironment myDataEnvironment;

  private final DecisionEnforcer decisionEnforcer;

  private IPolicyDecisionPoint pdp;

  /**
   * Constructor
   *
   * @param myDataEnvironment the IMyDataEnvironment the PEP belongs to
   * @param decisionEnforcer  concrete implementation of decisionEnforcer
   */
  public DefaultPolicyEnforcementPoint(final IMyDataEnvironment myDataEnvironment,
      DecisionEnforcer decisionEnforcer) {
    this.myDataEnvironment = Objects.requireNonNull(myDataEnvironment);
    this.decisionEnforcer = Objects.requireNonNull(decisionEnforcer);
  }

  /**
   * Make sure the PDP is available
   *
   * @return true if the pdp is available.
   */
  private boolean assurePDP() {
    if (this.pdp == null) {
      return this.initPpdConnection();
    } else {
      return true;
    }
  }

  /**
   * Publish the event and enforce the {@link AuthorizationDecision} to {@link Event}
   *
   * @param  event                          The event to publish.
   * @throws EvaluationUndecidableException if PDP can't decide.
   * @throws InhibitException               if event is not allowed
   * @throws IOException                    if connection to PDP is not established
   */
  @Override
  public void enforce(Event event)
      throws EvaluationUndecidableException, InhibitException, IOException {
    LOG.info("Received event for enforcement: {}, {}", event.getActionId(), event.getTimestamp());
    if (this.assurePDP()) {
      LOG.debug("Requesting decision");
      final AuthorizationDecision authorizationDecision = this.pdp.decisionRequest(event);
      LOG.debug("Received decision: {}", authorizationDecision);
      this.enforceDecision(event, authorizationDecision);
    } else {
      throw new IOException("PDP not reachable.");
    }
  }

  /**
   * Impose the {@link AuthorizationDecision} authorizationDecision on {@link Event} event.
   *
   * @param  event                 on which authorizationDecision to be impose
   * @param  authorizationDecision the decision to enforce on event
   * @throws InhibitException      if event is not allowed
   */
  @Override
  public void enforceDecision(Event event, AuthorizationDecision authorizationDecision)
      throws InhibitException {
    InhibitException exception;
    if (authorizationDecision.isEventAllowed()) {
      LOG.debug("Event will be allowed");
      try {
        this.decisionEnforcer.enforce(authorizationDecision, event.getParameters());
        return; // prevent fallthrough to exception handling...
      } catch (final InhibitException e) {
        exception = e;
      } catch (final Exception e) {
        exception = new InhibitException(
            "Event cannot be allowed because of an internal Exception: " + e.getMessage(), e);
      }
    } else {
      exception = new InhibitException("Event is not allowed according to policy");
    }
    event.clearParameters();
    throw exception;
  }

  @Override
  public AuthorizationDecision getDecision(Event event)
      throws EvaluationUndecidableException, IOException {
    LOG.info("Event is going to PDP for Authorization Decision");
    if (this.assurePDP()) {
      return this.pdp.decisionRequest(event);
    } else {
      throw new IOException("PDP not reachable");
    }
  }

  /**
   * @return The id of the component.
   */
  @Override
  public ComponentId getId() {
    return null;
  }

  /**
   * Default initializer that establishes the connection to PDP.
   * 
   * @throws NoSuchEntityException
   */
  @Override
  public boolean initialize() throws IOException, NoSuchEntityException {
    return this.initPpdConnection();
  }

  /**
   * Initialize the PDP connection
   */
  private boolean initPpdConnection() {
    final Optional<IPolicyDecisionPoint> pdpOptional = TechnicalAccessGranter
        .getTechnicalAccess(this.myDataEnvironment).getPdp();
    if (pdpOptional.isPresent()) {
      this.pdp = pdpOptional.get();
      LOG.debug("Successfully connected to the PDP");
      return true;
    } else {
      this.pdp = null;
      LOG.warn(
          "Cannot establish connection to the PDP as the reference in the environment is null");
      return false;
    }
  }

  /**
   * Reset the PDP connection.
   *
   * @return                       true, if successful.
   * @throws NoSuchEntityException
   */
  @Override
  public boolean reset() throws IOException, NoSuchEntityException {
    return this.initialize();
  }

  @Override
  public HealthStatus getHealth() {
    return HealthStatus.of(Status.UP);
  }
}
