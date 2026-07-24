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

import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.ComponentType;
import de.fraunhofer.iese.mydata.component.connector.Authentication;
import de.fraunhofer.iese.mydata.component.connector.Connector;
import de.fraunhofer.iese.mydata.component.connector.OAuthCredentials;
import de.fraunhofer.iese.mydata.component.health.HealthStatus;
import de.fraunhofer.iese.mydata.component.information.PdpComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PepComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PipComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PxpComponentInformation;
import de.fraunhofer.iese.mydata.component.information.method.MethodInterfaceDescription;
import de.fraunhofer.iese.mydata.component.interfaces.IBasicManagementService;
import de.fraunhofer.iese.mydata.exception.ConflictingResourceException;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.exception.ResourceUpdateException;
import de.fraunhofer.iese.mydata.policy.Policy;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.solution.SolutionId;
import de.fraunhofer.iese.mydata.timer.Timer;
import de.fraunhofer.iese.mydata.timer.TimerId;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Rest based connector interface for the PMP service.
 */
@Connector(protocol = {
    "http", "https"
}, type = ComponentType.PMP)
@Slf4j
public class PmpRestConnector extends AbstractRestConnector implements IBasicManagementService {

  public static final String POLICY_PREFIX = "policies";

  public static final String POLICY_IDS_PREFIX = "policy-ids";

  public static final String TIMER_PREFIX = "timers";

  public static final String TIMER_IDS_PREFIX = "timer-ids";

  public static final String COMPONENT_PREFIX = "components";

  public static final String AFFILIATION_PREFIX = "affiliations";

  public static final String AFFILIATION_IDS_PREFIX = "affiliation-ids";

  public static final String SOLUTION_ACCESS_PREFIX = "solution-access";

  public static final String SOLUTION_PREFIX = "solutions";

  public static final String SOLUTION_IDS_PREFIX = "solution-ids";

  private static final String URI_VARIABLE_SOLUTION_ID = "solution-id";

  private static final String URI_VARIABLE_DEPLOYED = "deployed";

  private static final String URI_VARIABLE_QUERY = "query";

  private static final String REST_INTERFACE_DOES_NOT_SUPPORT_THIS_METHOD = "REST Interface does not support this method.";

  /**
   * Constructor of {@link #PmpRestConnector} which uses an uri string to identify the component.
   *
   * @param  uri                      Used to identify the component.
   * @throws RemoteException          in case of a communication error.
   * @throws IllegalArgumentException in case of a wrong or unknown uri.
   */
  public PmpRestConnector(String uri) throws RemoteException {
    this(URI.create(uri));
  }

  /**
   * Constructor of {@link #PmpRestConnector} which uses an uri string to identify the component.
   *
   * @param  uri                      Used to identify the component.
   * @param  credentials              {@link Authentication} with client_id and secret
   * @throws IllegalArgumentException in case of a wrong or unknown uri.
   */
  public PmpRestConnector(final String uri, final Authentication credentials) {
    this(URI.create(uri), credentials);
  }

  /**
   * Constructor of {@link #PmpRestConnector} which uses an uri string and oauth credentials to
   * identify the component.
   *
   * @param  uri                      Used to identify the component.
   * @param  credentials              {@link OAuthCredentials} with client_id and secret
   * @throws IllegalArgumentException in case of a wrong or unknown uri.
   */
  public PmpRestConnector(String uri, OAuthCredentials credentials) {
    this(URI.create(uri), credentials);
  }

  /**
   * Constructor of {@link #PmpRestConnector} which uses (instead of an uri) a scheme, host, name
   * string as well as port to identify the component.
   *
   * @param scheme used scheme of the component (e.g. http). See also {@link URI#getScheme()}
   * @param host   hostname of the component. See also {@link URI#getHost()}
   * @param port   port of the component. See also {@link URI#getPort()}
   * @param name   An identifying name of the component.
   */
  public PmpRestConnector(String scheme, String host, int port, String name) {
    super(scheme, host, port, name);
  }

  /**
   * Constructor of {@link #PmpRestConnector} which uses an uri object to identify the component.
   *
   * @param uri Used to identify the component.
   */
  public PmpRestConnector(URI uri) {
    super(uri);
  }

  /**
   * Constructor of {@link #PmpRestConnector} which uses an uri object to identify the component.
   *
   * @param uri         Used to identify the component.
   * @param credentials {@link Authentication} with client_id and secret
   */
  public PmpRestConnector(URI uri, final Authentication credentials) {
    super(uri, credentials);
  }

  /**
   * Constructor of {@link #PmpRestConnector} which uses an uri object and oauth credentials to
   * identify the component.
   *
   * @param uri         Used to identify the component.
   * @param credentials {@link OAuthCredentials} with client_id and secret
   */
  public PmpRestConnector(URI uri, OAuthCredentials credentials) {
    super(uri, credentials);
  }

  @Override
  public ComponentId addPdp(PdpComponentInformation component)
      throws IOException, ConflictingResourceException, InvalidEntityException,
      ResourceUpdateException, NoSuchEntityException { // checked
    try {
      return this.httpClient.postForObject(this.getBaseUrl() + COMPONENT_PREFIX + "/pdp", component,
          ComponentId.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowConflictingResourceException(httpException);
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowResourceUpdateException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public ComponentId addPep(PepComponentInformation component)
      throws IOException, ConflictingResourceException, InvalidEntityException,
      ResourceUpdateException, NoSuchEntityException { // checked
    try {
      return this.httpClient.postForObject(this.getBaseUrl() + COMPONENT_PREFIX + "/pep", component,
          ComponentId.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowConflictingResourceException(httpException);
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowResourceUpdateException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public ComponentId addPip(PipComponentInformation component)
      throws IOException, ConflictingResourceException, InvalidEntityException,
      ResourceUpdateException, NoSuchEntityException { // checked
    try {
      return this.httpClient.postForObject(this.getBaseUrl() + COMPONENT_PREFIX + "/pip", component,
          ComponentId.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowConflictingResourceException(httpException);
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowResourceUpdateException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public ComponentId addPxp(PxpComponentInformation component)
      throws IOException, ConflictingResourceException, InvalidEntityException,
      ResourceUpdateException, NoSuchEntityException { // checked
    try {
      return this.httpClient.postForObject(this.getBaseUrl() + COMPONENT_PREFIX + "/pxp", component,
          ComponentId.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowConflictingResourceException(httpException);
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowResourceUpdateException(httpException);
      throw new IOException(httpException);
    }
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.api.component.interfaces.IPolicyManagementPoint#
   * lookupPdp(boolean)
   */
  @Override
  public PdpComponentInformation getPdp() throws IOException, NoSuchEntityException { // checked
    try {
      return this.httpClient.getForObject(this.getBaseUrl() + COMPONENT_PREFIX + "/pdp",
          PdpComponentInformation.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public PepComponentInformation getPep(ComponentId componentId)
      throws IOException, InvalidEntityException, NoSuchEntityException { // checked
    try {
      return this.httpClient.getForObject(
          this.getBaseUrl() + COMPONENT_PREFIX + "/pep/" + componentId,
          PepComponentInformation.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public PipComponentInformation getPip(ComponentId componentId)
      throws IOException, InvalidEntityException, NoSuchEntityException {
    try {
      return this.httpClient.getForObject(
          this.getBaseUrl() + COMPONENT_PREFIX + "/pip/" + componentId,
          PipComponentInformation.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public PxpComponentInformation getPxp(ComponentId componentId)
      throws IOException, InvalidEntityException, NoSuchEntityException {
    try {
      return this.httpClient.getForObject(
          this.getBaseUrl() + COMPONENT_PREFIX + "/pxp/" + componentId,
          PxpComponentInformation.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public PolicyId addPolicy(Policy policy) throws IOException, ConflictingResourceException,
      InvalidEntityException, ResourceUpdateException, NoSuchEntityException { // checked
    try {
      return this.httpClient.postForObject(this.getBaseUrl() + POLICY_PREFIX, policy,
          PolicyId.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowConflictingResourceException(httpException);
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowResourceUpdateException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public TimerId addTimer(Timer timer) throws IOException, ConflictingResourceException,
      InvalidEntityException, ResourceUpdateException, NoSuchEntityException { // checked
    try {
      return this.httpClient.postForObject(this.getBaseUrl() + TIMER_PREFIX, timer, TimerId.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowConflictingResourceException(httpException);
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowResourceUpdateException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public void deletePep(ComponentId componentId)
      throws IOException, InvalidEntityException, ResourceUpdateException, NoSuchEntityException { // checked
    try {
      this.httpClient.delete(this.getBaseUrl() + COMPONENT_PREFIX + "/pep/" + componentId);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowResourceUpdateException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public void deletePip(ComponentId componentId)
      throws IOException, InvalidEntityException, ResourceUpdateException, NoSuchEntityException { // checked
    try {
      this.httpClient.delete(this.getBaseUrl() + COMPONENT_PREFIX + "/pip/" + componentId);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowResourceUpdateException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public void deletePxp(ComponentId componentId)
      throws IOException, InvalidEntityException, ResourceUpdateException, NoSuchEntityException { // checked
    try {
      this.httpClient.delete(this.getBaseUrl() + COMPONENT_PREFIX + "/pxp/" + componentId);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowResourceUpdateException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public void deletePolicy(PolicyId policyId) throws IOException, InvalidEntityException,
      ResourceUpdateException, ConflictingResourceException, NoSuchEntityException { // checked
    try {
      this.httpClient.delete(this.getBaseUrl() + POLICY_PREFIX + "/" + policyId.getUrn());
    } catch (final RestClientException httpException) {
      this.handleAndRethrowConflictingResourceException(httpException);
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowResourceUpdateException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public void deleteTimer(TimerId timerId) throws IOException, InvalidEntityException,
      ResourceUpdateException, ConflictingResourceException, NoSuchEntityException { // checked
    try {
      this.httpClient.delete(this.getBaseUrl() + TIMER_PREFIX + "/" + timerId.getUrn());
    } catch (final RestClientException httpException) {
      this.handleAndRethrowConflictingResourceException(httpException);
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowResourceUpdateException(httpException);
      throw new IOException(httpException);
    }
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.api.component.interfaces.IPolicyManagementPoint#
   * deployPolicy(de.fraunhofer.iese.mydata.api.policy.identifier. String)
   */
  @Override
  public void deployPolicy(PolicyId policyId)
      throws IOException, InvalidEntityException, ResourceUpdateException, NoSuchEntityException { // checked
    try {
      this.httpClient.patchForObject(
          this.getBaseUrl() + POLICY_PREFIX + "/" + policyId + "/deployed", Boolean.TRUE,
          Void.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowResourceUpdateException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public void deployTimer(TimerId timerId)
      throws IOException, InvalidEntityException, ResourceUpdateException, NoSuchEntityException { // checked
    try {
      this.httpClient.patchForObject(this.getBaseUrl() + TIMER_PREFIX + "/" + timerId + "/deployed",
          Boolean.TRUE, Void.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowResourceUpdateException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public Map<ComponentId, HealthStatus> getAllComponentStates(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException { // checked
    try {
      final Map<String, Object> queryParams = new HashMap<>();
      if (solutionId != null) {
        log.debug("Get all component states for solution {}", solutionId);
        queryParams.put(URI_VARIABLE_SOLUTION_ID, solutionId.getUrn());
      }
      final ParameterizedTypeReference<Map<ComponentId, HealthStatus>> responseType = new ParameterizedTypeReference<Map<ComponentId, HealthStatus>>() {
      };
      final URI uri = URI.create(
          addQueryParameters(this.getBaseUrl() + COMPONENT_PREFIX + "/status", queryParams));

      return this.httpClient.exchange(uri, HttpMethod.GET, null, responseType).getBody();
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public Set<Timer> getTimers(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException { // checked
    return this.getTimersInternal(solutionId, null);
  }

  @Override
  public Set<Timer> getDeployedTimers(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException {// checked
    return this.getTimersInternal(solutionId, true);
  }

  @Override
  public Set<Timer> getRevokedTimers(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException {// checked
    return this.getTimersInternal(solutionId, false);
  }

  private Set<Timer> getTimersInternal(SolutionId solutionId, Boolean deployed)
      throws InvalidEntityException, IOException, NoSuchEntityException {
    try {
      final UriComponentsBuilder builder = UriComponentsBuilder
          .fromUriString(this.getBaseUrl() + TIMER_PREFIX);
      if (solutionId != null) {
        builder.queryParam(URI_VARIABLE_SOLUTION_ID, solutionId.getUrn());
      }
      if (deployed != null) {
        builder.queryParam(URI_VARIABLE_DEPLOYED, deployed);
      }
      final Timer[] timers = this.httpClient.getForObject(URI.create(builder.toUriString()),
          Timer[].class);
      return timers != null ? Sets.newHashSet(timers) : new HashSet<>();
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public Set<Policy> getPolicies(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException { // checked
    return this.getPoliciesInternal(solutionId, null);
  }

  @Override
  public Set<Policy> getDeployedPolicies(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException { // checked
    return this.getPoliciesInternal(solutionId, true);
  }

  @Override
  public Set<Policy> getRevokedPolicies(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException {// checked
    return this.getPoliciesInternal(solutionId, false);
  }

  private Set<Policy> getPoliciesInternal(SolutionId solutionId, Boolean deployed)
      throws InvalidEntityException, IOException, NoSuchEntityException {
    try {
      final UriComponentsBuilder builder = UriComponentsBuilder
          .fromUriString(this.getBaseUrl() + POLICY_PREFIX);
      if (solutionId != null) {
        builder.queryParam(URI_VARIABLE_SOLUTION_ID, solutionId.getUrn());
      }
      if (deployed != null) {
        builder.queryParam(URI_VARIABLE_DEPLOYED, deployed);
      }

      final Policy[] policies = this.httpClient.getForObject(URI.create(builder.toUriString()),
          Policy[].class);
      return policies != null ? Sets.newHashSet(policies) : new HashSet<>();
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public Policy getPolicy(PolicyId policyId)
      throws IOException, InvalidEntityException, NoSuchEntityException { // checked
    try {
      return this.httpClient.getForObject(this.getBaseUrl() + POLICY_PREFIX + "/" + policyId,
          Policy.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public HealthStatus getPipState(ComponentId componentId)
      throws IOException, InvalidEntityException, NoSuchEntityException { // checked
    try {
      return this.httpClient.getForObject(
          this.getBaseUrl() + COMPONENT_PREFIX + "/pip/" + componentId + "/status",
          HealthStatus.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public HealthStatus getPxpState(ComponentId componentId)
      throws IOException, InvalidEntityException, NoSuchEntityException { // checked
    try {
      return this.httpClient.getForObject(
          this.getBaseUrl() + COMPONENT_PREFIX + "/pxp/" + componentId + "/status",
          HealthStatus.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public boolean isTimerDeployed(TimerId timerId)
      throws IOException, InvalidEntityException, NoSuchEntityException { // checked
    try {
      return this.httpClient.getForObject(
          this.getBaseUrl() + TIMER_PREFIX + "/" + timerId + "/deployed", Boolean.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public Timer getTimer(TimerId timerId)
      throws IOException, InvalidEntityException, NoSuchEntityException { // checked
    try {
      return this.httpClient.getForObject(this.getBaseUrl() + TIMER_PREFIX + "/" + timerId,
          Timer.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public boolean isPolicyDeployed(PolicyId policyId)
      throws IOException, InvalidEntityException, NoSuchEntityException { // checked
    try {
      return this.httpClient.getForObject(
          this.getBaseUrl() + POLICY_PREFIX + "/" + policyId + "/deployed", Boolean.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public Set<PolicyId> listDeployedPolicies(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException { // checked
    return this.getPolicyIdsInternal(solutionId, true);
  }

  @Override
  public Set<PolicyId> listRevokedPolicies(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException { // checked
    return this.getPolicyIdsInternal(solutionId, false);
  }

  @Override
  public Set<PolicyId> listPolicies(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException { // checked
    return this.getPolicyIdsInternal(solutionId, null);
  }

  private Set<PolicyId> getPolicyIdsInternal(SolutionId solutionId, Boolean deployed)
      throws InvalidEntityException, IOException, NoSuchEntityException {
    try {
      final UriComponentsBuilder builder = UriComponentsBuilder
          .fromUriString(this.getBaseUrl() + POLICY_IDS_PREFIX);
      if (solutionId != null) {
        builder.queryParam(URI_VARIABLE_SOLUTION_ID, solutionId.getUrn());
      }
      if (deployed != null) {
        builder.queryParam(URI_VARIABLE_DEPLOYED, deployed);
      }
      final PolicyId[] policies = this.httpClient.getForObject(URI.create(builder.toUriString()),
          PolicyId[].class);
      return policies != null ? Sets.newHashSet(policies) : new HashSet<>();
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public Set<PepComponentInformation> lookupPep(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException { // checked
    try {
      final UriComponentsBuilder builder = UriComponentsBuilder
          .fromUriString(this.getBaseUrl() + COMPONENT_PREFIX + "/pep");
      if (solutionId != null) {
        builder.queryParam(URI_VARIABLE_SOLUTION_ID, solutionId.getUrn());
      }
      final PepComponentInformation[] components = this.httpClient
          .getForObject(URI.create(builder.toUriString()), PepComponentInformation[].class);
      return components != null ? Sets.newHashSet(components) : new HashSet<>();
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public Set<PipComponentInformation> lookupPip(SolutionId solutionId,
      MethodInterfaceDescription query)
      throws IOException, InvalidEntityException, NoSuchEntityException {
    try {
      final UriComponentsBuilder builder = UriComponentsBuilder
          .fromUriString(this.getBaseUrl() + COMPONENT_PREFIX + "/pip");
      if (null != solutionId) {
        builder.queryParam(URI_VARIABLE_SOLUTION_ID, solutionId.getUrn());
      }
      if (null != query) {
        builder.queryParam(URI_VARIABLE_QUERY, query.toJson(false));
      }
      final PipComponentInformation[] components = this.httpClient
          .getForObject(URI.create(builder.toUriString()), PipComponentInformation[].class);
      return components != null ? Sets.newHashSet(components) : new HashSet<>();
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public Set<PxpComponentInformation> lookupPxp(SolutionId solutionId,
      MethodInterfaceDescription query)
      throws IOException, InvalidEntityException, NoSuchEntityException {
    try {
      final UriComponentsBuilder builder = UriComponentsBuilder
          .fromUriString(this.getBaseUrl() + COMPONENT_PREFIX + "/pxp");
      if (null != solutionId) {
        builder.queryParam(URI_VARIABLE_SOLUTION_ID, solutionId.getUrn());
      }
      if (null != query) {
        builder.queryParam(URI_VARIABLE_QUERY, query.toJson(false));
      }
      final PxpComponentInformation[] components = this.httpClient
          .getForObject(URI.create(builder.toUriString()), PxpComponentInformation[].class);
      return components != null ? Sets.newHashSet(components) : new HashSet<>();
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  /**
   * Method which executes the /reset web service as GET request. NOT supported via REST
   *
   * @return             TRUE in case of a successful reset, FALSE otherwise
   * @throws IOException In case of an error.
   */
  @Override
  public boolean reset() throws IOException { // TODO check
    throw new UnsupportedOperationException(REST_INTERFACE_DOES_NOT_SUPPORT_THIS_METHOD);
  }

  @Override
  public void revokePolicy(PolicyId policyId)
      throws IOException, ResourceUpdateException, InvalidEntityException, NoSuchEntityException {// checked
    try {
      this.httpClient.patchForObject(
          this.getBaseUrl() + POLICY_PREFIX + "/" + policyId + "/deployed", Boolean.FALSE,
          Void.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowResourceUpdateException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public void revokeTimer(TimerId timerId)
      throws IOException, InvalidEntityException, ResourceUpdateException, NoSuchEntityException {// checked
    try {
      this.httpClient.patchForObject(this.getBaseUrl() + TIMER_PREFIX + "/" + timerId + "/deployed",
          Boolean.FALSE, Void.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowResourceUpdateException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public PolicyId updatePolicy(Policy policy)
      throws IOException, InvalidEntityException, ResourceUpdateException, NoSuchEntityException { // checked
    try {
      this.httpClient.put(this.getBaseUrl() + POLICY_PREFIX + "/" + policy.getPolicyId(), policy);
      return policy.getPolicyId();
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowResourceUpdateException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public ComponentId updatePdp(PdpComponentInformation component)
      throws IOException, InvalidEntityException, ResourceUpdateException, NoSuchEntityException { // checked
    try {
      this.httpClient.put(this.getBaseUrl() + COMPONENT_PREFIX + "/pdp", component);
      return component.getComponentId();
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowResourceUpdateException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public ComponentId updatePep(PepComponentInformation component)
      throws IOException, InvalidEntityException, ResourceUpdateException, NoSuchEntityException { // checked
    try {
      this.httpClient.put(
          this.getBaseUrl() + COMPONENT_PREFIX + "/pep/" + component.getComponentId(), component);
      return component.getComponentId();
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowResourceUpdateException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public ComponentId updatePip(PipComponentInformation component)
      throws IOException, ResourceUpdateException, InvalidEntityException, NoSuchEntityException { // checked
    try {
      this.httpClient.put(
          this.getBaseUrl() + COMPONENT_PREFIX + "/pip/" + component.getComponentId(), component);
      return component.getComponentId();
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowResourceUpdateException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public ComponentId updatePxp(PxpComponentInformation component)
      throws IOException, InvalidEntityException, ResourceUpdateException, NoSuchEntityException {// checked
    try {
      this.httpClient.put(
          this.getBaseUrl() + COMPONENT_PREFIX + "/pxp/" + component.getComponentId(), component);
      return component.getComponentId();
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowResourceUpdateException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public Set<TimerId> listTimers(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException { // checked
    return this.getTimerIdsInternal(solutionId, null);
  }

  @Override
  public Set<TimerId> listDeployedTimers(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException {// checked
    return this.getTimerIdsInternal(solutionId, Boolean.TRUE);
  }

  @Override
  public Set<TimerId> listRevokedTimers(SolutionId solId)
      throws IOException, InvalidEntityException, NoSuchEntityException {
    return this.getTimerIdsInternal(solId, Boolean.FALSE);
  }

  private Set<TimerId> getTimerIdsInternal(SolutionId solutionId, Boolean deployed)
      throws InvalidEntityException, IOException, NoSuchEntityException {
    try {
      final Map<String, Object> queryParams = new HashMap<>();
      if (solutionId != null) {
        queryParams.put(URI_VARIABLE_SOLUTION_ID, solutionId.getUrn());
      }
      if (deployed != null) {
        queryParams.put(URI_VARIABLE_DEPLOYED, deployed);
      }
      final TimerId[] policies = this.httpClient.getForObject(
          URI.create(addQueryParameters(this.getBaseUrl() + TIMER_IDS_PREFIX, queryParams)),
          TimerId[].class);
      return policies != null ? Sets.newHashSet(policies) : new HashSet<>();
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public boolean policyExists(PolicyId policyId) throws IOException, InvalidEntityException { // checked
    try {
      this.httpClient.headForHeaders(this.getBaseUrl() + POLICY_PREFIX + "/" + policyId);
      return true;

    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      try {
        this.handleAndRethrowNoSuchEntityException(httpException);
      } catch (final NoSuchEntityException e) {
        return false;
      }
      throw new IOException(httpException);
    }
  }

  @Override
  public boolean timerExists(TimerId timerId) throws InvalidEntityException, IOException {// checked
    try {
      this.httpClient.headForHeaders(this.getBaseUrl() + TIMER_PREFIX + "/" + timerId);
      return true;
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      try {
        this.handleAndRethrowNoSuchEntityException(httpException);
      } catch (final NoSuchEntityException e) {
        return false;
      }
      throw new IOException(httpException);
    }
  }

  @Override
  public Set<Policy> getDeployedPolicies() throws IOException {
    try {
      return this.getPoliciesInternal(null, true);
    } catch (final InvalidEntityException | NoSuchEntityException e) {
      throw new IOException(e);
    }
  }

  @Override
  public boolean pdpExists(ComponentId componentId) throws IOException, InvalidEntityException { // checked
    try {
      this.httpClient.headForHeaders(this.getBaseUrl() + COMPONENT_PREFIX + "/pdp/" + componentId);
      return true;
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      try {
        this.handleAndRethrowNoSuchEntityException(httpException);
      } catch (final NoSuchEntityException e) {
        return false;
      }
      throw new IOException(httpException);
    }
  }

  @Override
  public boolean pepExists(ComponentId componentId) throws IOException, InvalidEntityException { // checked
    try {
      this.httpClient.headForHeaders(this.getBaseUrl() + COMPONENT_PREFIX + "/pep/" + componentId);
      return true;
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      try {
        this.handleAndRethrowNoSuchEntityException(httpException);
      } catch (final NoSuchEntityException e) {
        return false;
      }
      throw new IOException(httpException);
    }
  }

  @Override
  public boolean pipExists(ComponentId componentId) throws IOException, InvalidEntityException { // checked
    try {
      this.httpClient.headForHeaders(this.getBaseUrl() + COMPONENT_PREFIX + "/pip/" + componentId);
      return true;
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      try {
        this.handleAndRethrowNoSuchEntityException(httpException);
      } catch (final NoSuchEntityException e) {
        return false;
      }
      throw new IOException(httpException);
    }
  }

  @Override
  public boolean pxpExists(ComponentId componentId) throws IOException, InvalidEntityException { // checked
    try {
      this.httpClient.headForHeaders(this.getBaseUrl() + COMPONENT_PREFIX + "/pxp/" + componentId);
      return true;
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      try {
        this.handleAndRethrowNoSuchEntityException(httpException);
      } catch (final NoSuchEntityException e) {
        return false;
      }
      throw new IOException(httpException);
    }
  }

  @Override
  public TimerId updateTimer(Timer timer)
      throws IOException, ResourceUpdateException, NoSuchEntityException, InvalidEntityException {
    try {
      this.httpClient.put(this.getBaseUrl() + TIMER_PREFIX + "/" + timer.getTimerId(), timer);
      return timer.getTimerId();
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowResourceUpdateException(httpException);
      throw new IOException(httpException);
    }
  }
}
