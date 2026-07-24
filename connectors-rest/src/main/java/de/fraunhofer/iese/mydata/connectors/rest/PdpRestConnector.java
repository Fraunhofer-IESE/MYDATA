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

package de.fraunhofer.iese.mydata.connectors.rest;

import de.fraunhofer.iese.mydata.component.ComponentType;
import de.fraunhofer.iese.mydata.component.connector.Authentication;
import de.fraunhofer.iese.mydata.component.connector.Connector;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyDecisionPoint;
import de.fraunhofer.iese.mydata.exception.ResourceUpdateException;
import de.fraunhofer.iese.mydata.policy.Policy;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.policy.decision.AuthorizationDecision;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.ConflictingPolicyException;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.net.URI;
import java.rmi.RemoteException;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import org.jspecify.annotations.Nullable;

/**
 * Rest based connector interface for the PDP service.
 */
@Connector(protocol = {
    "http", "https"
}, type = ComponentType.PDP)
@Slf4j
public class PdpRestConnector extends AbstractRestConnector implements IPolicyDecisionPoint {

  private static final String METHOD_NOT_SUPPORTED = "REST Interface does not support this method.";

  /**
   * Constructor of {@link #PdpRestConnector} which uses an uri string to identify the component.
   *
   * @param  uri                      Used to identify the component.
   * @throws RemoteException          in case of a communication error.
   * @throws IllegalArgumentException in case of a wrong or unknown uri.
   */
  public PdpRestConnector(String uri) throws RemoteException {
    this(URI.create(uri));
  }

  /**
   * Constructor of {@link #PdpRestConnector} which uses an uri string to identify the component.
   *
   * @param  uri                      Used to identify the component.
   * @param  credentials              {@link Authentication} with client_id and secret
   * @throws RemoteException          in case of a communication error.
   * @throws IllegalArgumentException in case of a wrong or unknown uri.
   */
  public PdpRestConnector(final String uri, final Authentication credentials)
      throws RemoteException {
    this(URI.create(uri), credentials);
  }

  /**
   * Constructor of {@link #PdpRestConnector} which uses (instead of an uri) a scheme, host, name
   * string as well as port to identify the component.
   *
   * @param scheme used scheme of the component (e.g. http). See also {@link URI#getScheme()}
   * @param host   hostname of the component. See also {@link URI#getHost()}
   * @param port   port of the component. See also {@link URI#getPort()}
   * @param name   An identifying name of the component.
   */
  public PdpRestConnector(String scheme, String host, int port, String name) {
    super(scheme, host, port, name);
  }

  /**
   * Constructor of {@link #PdpRestConnector} which uses an uri object to identify the component.
   *
   * @param  uri             Used to identify the component.
   * @throws RemoteException in case of a communication error.
   */
  public PdpRestConnector(URI uri) throws RemoteException {
    super(uri);
  }

  /**
   * Constructor of {@link #PdpRestConnector} which uses an uri object to identify the component.
   *
   * @param  uri             Used to identify the component.
   * @param  credentials     {@link Authentication} with client_id and secret
   * @throws RemoteException in case of a communication error.
   */
  public PdpRestConnector(URI uri, final Authentication credentials) throws RemoteException {
    super(uri, credentials);
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.api.component.interfaces.IPolicyDecisionPoint# clearAllCaches()
   */
  @Override
  public boolean clearAllCaches(@Nullable
  SolutionId solutionId) throws IOException {
    throw new UnsupportedOperationException(METHOD_NOT_SUPPORTED);
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.api.component.interfaces.IPolicyDecisionPoint#
   * decisionRequest(de.fraunhofer.iese.mydata.api.policy.Event)
   */
  @Override
  public AuthorizationDecision decisionRequest(Event event)
      throws EvaluationUndecidableException, IOException {
    try {
      return this.httpClient.postForObject(this.getBaseUrl() + "event", event,
          AuthorizationDecision.class);
    } catch (final RuntimeException e) {
      boolean mightContainList = false;
      for (final Parameter<?> parameter : event.getParameters()) {
        if (parameter.getType() != null && Set.class.isAssignableFrom(parameter.getType())) {
          final String error = "Did you send a list for the decision (ie. in the if block) ? Set are not supported yet, please use a wrapper.";
          log.warn("Exception during decisionRequest to {} with event {}\nHINT: {}",
              this.getBaseUrl(), event.getActionId(), error, e);
          mightContainList = true;
        }
      }
      if (!mightContainList) {
        log.warn("Exception during decisionRequest to {} with event {}", this.getBaseUrl(),
            event.getActionId(), e);
      }
      throw new IOException(e);
    }
  }

  @Override
  public boolean evaluate(Set<Event> events) throws IOException {
    try {
      return this.httpClient.postForObject(this.getBaseUrl() + "fireTimedEvents", events,
          Boolean.class);
    } catch (final RuntimeException e) {
      throw new IOException("Exception during timer decisionRequest to " + this.getBaseUrl()
          + " with event " + events, e);
    }
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.api.component.interfaces.IPolicyDecisionPoint#
   * listDeployedPolicies()
   */
  @Override
  public Set<String> listDeployedPolicies() throws IOException {
    throw new UnsupportedOperationException(METHOD_NOT_SUPPORTED);
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.api.component.interfaces.IPolicyDecisionPoint# reset()
   */
  @Override
  public boolean reset() throws IOException {
    throw new UnsupportedOperationException(METHOD_NOT_SUPPORTED);
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.api.component.interfaces.IPolicyDecisionPoint#
   * revokePolicy(de.fraunhofer.iese.mydata.api.policy.identifier.String)
   */
  @Override
  public boolean revokePolicy(PolicyId policyId) throws IOException {
    throw new UnsupportedOperationException(METHOD_NOT_SUPPORTED);
  }

  @Override
  public boolean addToBlacklist(Set<SolutionId> ids) throws IOException {
    throw new UnsupportedOperationException(METHOD_NOT_SUPPORTED);
  }

  @Override
  public boolean removeFromBlacklist(Set<SolutionId> ids) throws IOException {
    throw new UnsupportedOperationException(METHOD_NOT_SUPPORTED);
  }

  @Override
  public void setFailureMode(boolean active) {
    throw new UnsupportedOperationException(METHOD_NOT_SUPPORTED);
  }

  @Override
  public List<AuthorizationDecision> decisionRequests(List<Event> events) throws IOException {
    throw new UnsupportedOperationException(METHOD_NOT_SUPPORTED);
  }

  @Override
  public boolean evaluate(Event event) throws IOException {
    throw new UnsupportedOperationException(METHOD_NOT_SUPPORTED);
  }

  @Override
  public boolean deploy(Policy policy, ZoneId zoneIdOfSolution)
      throws IOException, ConflictingPolicyException {
    throw new UnsupportedOperationException(METHOD_NOT_SUPPORTED);
  }

  @Override
  public boolean updatePolicy(Policy policy, ZoneId zoneIdOfSolution)
      throws IOException, de.fraunhofer.iese.mydata.exception.ResourceUpdateException {
    throw new UnsupportedOperationException(METHOD_NOT_SUPPORTED);
  }

  @Override
  public boolean isWhitelistModeEnabled() {
    throw new UnsupportedOperationException(METHOD_NOT_SUPPORTED);
  }

  @Override
  public boolean updatePolicyAndId(Policy policyWithNewId, ZoneId zoneIdOfSolution,
      PolicyId oldPolicyId) throws IOException, ResourceUpdateException {
    throw new UnsupportedOperationException(METHOD_NOT_SUPPORTED);
  }

  @Override
  public boolean isInFailureMode() {
    throw new UnsupportedOperationException(METHOD_NOT_SUPPORTED);
  }

}
