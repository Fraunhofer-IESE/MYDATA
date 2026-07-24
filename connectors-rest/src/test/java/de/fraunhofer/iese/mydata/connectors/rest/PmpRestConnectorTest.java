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

import static de.fraunhofer.iese.mydata.connectors.rest.PmpRestConnector.COMPONENT_PREFIX;
import static de.fraunhofer.iese.mydata.connectors.rest.PmpRestConnector.POLICY_IDS_PREFIX;
import static de.fraunhofer.iese.mydata.connectors.rest.PmpRestConnector.POLICY_PREFIX;
import static de.fraunhofer.iese.mydata.connectors.rest.PmpRestConnector.TIMER_IDS_PREFIX;
import static de.fraunhofer.iese.mydata.connectors.rest.PmpRestConnector.TIMER_PREFIX;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.ComponentType;
import de.fraunhofer.iese.mydata.component.health.HealthStatus;
import de.fraunhofer.iese.mydata.component.health.Status;
import de.fraunhofer.iese.mydata.component.information.PdpComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PepComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PipComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PxpComponentInformation;
import de.fraunhofer.iese.mydata.component.information.method.MethodInterfaceDescription;
import de.fraunhofer.iese.mydata.exception.ConflictingResourceException;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.exception.ResourceUpdateException;
import de.fraunhofer.iese.mydata.policy.Policy;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.policy.exception.ConflictingPolicyException;
import de.fraunhofer.iese.mydata.solution.SolutionId;
import de.fraunhofer.iese.mydata.timer.Timer;
import de.fraunhofer.iese.mydata.timer.TimerId;

import com.google.common.collect.Sets;
import org.apache.hc.client5.http.ClientProtocolException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The Class PmpRestConnectorTest.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class PmpRestConnectorTest extends CommonTestSetup {

  @Mock
  protected RestTemplate mockRestTemplate;

  /**
   * Test String.
   */
  private static final PolicyId POLICY_ID = new PolicyId("urn:policy:demo:demo");

  private static final TimerId TIMER_ID = new TimerId("urn:timer:test:timer");

  private static final String URI_VARIABLES_SOLUTION_ID = "solution-id";

  private static final String URI_VARIABLES_DEPLOYED = "deployed";

  private static String URI_VARIABLES_QUERY = "query";

  /**
   * The connector.
   */
  private PmpRestConnector connector;

  /**
   * The deployed policies list.
   */
  private final Set<PolicyId> deployedPoliciesList = Sets.newHashSet(POLICY_ID);

  private final PolicyId[] POLICY_IDS = new PolicyId[]{
      POLICY_ID
  };

  private static final String INTENDED_TEST_EXCEPTION_MSG = "Intended Test Exception";

  private static final RestClientException REST_CLIENT_EXCEPTION = new RestClientException(
      "Intended Test Exception");

  private static final HttpServerErrorException HTTP_SERVER_ERROR_EXCEPTION_NOT_FOUND = new HttpServerErrorException(
      HttpStatus.NOT_FOUND, INTENDED_TEST_EXCEPTION_MSG);

  private static final HttpServerErrorException HTTP_SERVER_ERROR_EXCEPTION_BAD_REQUEST = new HttpServerErrorException(
      HttpStatus.BAD_REQUEST, INTENDED_TEST_EXCEPTION_MSG);

  private static final HttpServerErrorException HTTP_SERVER_ERROR_EXCEPTION_INTERNAL_SERVER_ERROR = new HttpServerErrorException(
      HttpStatus.INTERNAL_SERVER_ERROR, INTENDED_TEST_EXCEPTION_MSG);

  /**
   * Test Policy
   */
  private Policy POLICY;

  private Policy[] POLICIES;

  /**
   * The policy list.
   */
  private Set<Policy> policyList;// = Sets.newHashSet(POLICY);

  private Timer timer;

  private Timer[] timers;

  private Set<Timer> timerList;

  private static final ComponentId PDP_COMPONENT_ID = new ComponentId(
      "urn:component:test:pdp:demo");

  private static final ComponentId PEP_COMPONENT_ID = new ComponentId(
      "urn:component:test:pep:demo");

  private static final ComponentId PIP_COMPONENT_ID = new ComponentId(
      "urn:component:test:pip:demo");

  private static final ComponentId PXP_COMPONENT_ID = new ComponentId(
      "urn:component:test:pxp:demo");

  /**
   * PDPCompoenent
   */
  private PdpComponentInformation PDP_COMPONENT;

  private PepComponentInformation PEP_COMPONENT;

  private PipComponentInformation PIP_COMPONENT;

  private PxpComponentInformation PXP_COMPONENT;

  private Set<PepComponentInformation> PEP_COMPONENT_LIST;

  private PepComponentInformation[] PEP_COMPONENTS;

  private Set<PxpComponentInformation> PXP_COMPONENT_LIST;

  private Set<PipComponentInformation> PIP_COMPONENT_LIST;

  private PdpComponentInformation[] PDP_COMPONENTS;

  private PxpComponentInformation[] PXP_COMPONENTS;

  private PipComponentInformation[] PIP_COMPONENTS;

  /**
   * The json PEP_COMPONENT list response.
   */
  private String jsonComponentListResponse;

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.connectors.rest.CommonTestSetup#setUp()
   */
  @Override
  @BeforeEach
  public void setUp() throws UnsupportedOperationException, IOException, InvalidEntityException {
    super.setUp();

    this.POLICY = new Policy(
        "<policy xmlns=\"http://www.mydata-control.de/4.0/mydataLanguage\" id='urn:policy:test:test'></policy>");
    this.POLICIES = new Policy[]{
        this.POLICY
    };

    this.PDP_COMPONENT = new PdpComponentInformation(PDP_COMPONENT_ID, new ArrayList<URI>());

    this.PEP_COMPONENT = new PepComponentInformation(PEP_COMPONENT_ID);

    this.PIP_COMPONENT = new PipComponentInformation(PIP_COMPONENT_ID);

    this.PXP_COMPONENT = new PxpComponentInformation(PXP_COMPONENT_ID);

    this.PEP_COMPONENT_LIST = Sets.newHashSet(this.PEP_COMPONENT);

    this.PEP_COMPONENTS = this.PEP_COMPONENT_LIST.toArray(new PepComponentInformation[1]);

    this.PXP_COMPONENT_LIST = Sets.newHashSet(this.PXP_COMPONENT);

    this.PIP_COMPONENT_LIST = Sets.newHashSet(this.PIP_COMPONENT);

    this.PDP_COMPONENTS = new PdpComponentInformation[]{
        this.PDP_COMPONENT
    };

    this.PXP_COMPONENTS = new PxpComponentInformation[]{
        this.PXP_COMPONENT
    };

    this.PIP_COMPONENTS = new PipComponentInformation[]{
        this.PIP_COMPONENT
    };

    this.jsonComponentListResponse = MyDataEntity.getGson().toJson(this.PXP_COMPONENT_LIST);

    this.POLICY.setPolicyId(POLICY_ID);

    this.policyList = Sets.newHashSet(this.POLICY);

    this.timer = new Timer(
        "<timer xmlns=\"http://www.mydata-control.de/4.0/mydataLanguage\" id='urn:timer:test:timer'  cron=\"0/55 * * * * *\"></timer>");
    this.timers = new Timer[]{
        this.timer
    };
    this.timerList = Sets.newHashSet(this.timer);

    this.connector = new PmpRestConnector(Constants.BASE_URL);
    setInternalState(this.connector, "httpClient", this.mockRestTemplate);
  }

  @Test
  public void whenDeployPolicy_thenOk()
      throws IOException, URISyntaxException, ConflictingPolicyException, InvalidEntityException,
      ResourceUpdateException, NoSuchEntityException {

    this.connector.deployPolicy(POLICY_ID);

    Mockito.verify(this.mockRestTemplate).patchForObject(
        Constants.BASE_URL + POLICY_PREFIX + "/" + POLICY_ID.getUrn() + "/deployed", Boolean.TRUE,
        Void.class);
  }

  @Test
  public void whenDeployPolicyThrowsRestClientException_thenThrowIOException()
      throws UnsupportedOperationException, InvalidEntityException,
      ResourceUpdateException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      Mockito.when(this.mockRestTemplate.patchForObject(
          Constants.BASE_URL + POLICY_PREFIX + "/" + POLICY_ID.toString() + "/deployed", Boolean.TRUE,
          Void.class)).thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.deployPolicy(POLICY_ID);
      Assertions.fail();
    });
  }

  @Test
  public void whenDeployTimer_thenOk()
      throws IOException, InvalidEntityException, ResourceUpdateException, NoSuchEntityException {
    final TimerId timerId = new TimerId("urn:timer:cs3:mock");

    this.connector.deployTimer(timerId);

    Mockito.verify(this.mockRestTemplate).patchForObject(
        Constants.BASE_URL + TIMER_PREFIX + "/" + timerId + "/deployed", Boolean.TRUE, Void.class);
  }

  @Test
  public void whenGetDeployedPolicies_thenReturnPolicies()
      throws IOException, InvalidEntityException, NoSuchEntityException {
    final URI url = URI.create(Constants.BASE_URL
        + POLICY_PREFIX
        + "?"
        + URI_VARIABLES_SOLUTION_ID
        + "=urn:solution:demo&"
        + URI_VARIABLES_DEPLOYED
        + "=true");
    when(this.mockRestTemplate.getForObject(url, Policy[].class)).thenReturn(this.POLICIES);

    final Set<Policy> deployedPolicies = this.connector
        .getDeployedPolicies(new SolutionId("urn:solution:" + "demo"));
    Mockito.verify(this.mockRestTemplate).getForObject(url, Policy[].class);
    Assertions.assertEquals(this.policyList.iterator().next(), deployedPolicies.iterator().next());
  }

  @Test
  public void whenGetDeployedPoliciesThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final Map<String, Object> uriVariables = new HashMap<String, Object>();
      uriVariables.put(URI_VARIABLES_SOLUTION_ID, new SolutionId("urn:solution:" + "demo"));
      uriVariables.put(URI_VARIABLES_DEPLOYED, true);

      when(this.mockRestTemplate.getForObject(ArgumentMatchers.any(URI.class),
          ArgumentMatchers.eq(Policy[].class))).thenThrow(REST_CLIENT_EXCEPTION);
      this.connector.getDeployedPolicies(new SolutionId("urn:solution:" + "demo"));
      fail();
    });
  }

  @Test
  public void whenGetPolicies_thenReturnPolicies()
      throws IOException, InvalidEntityException, NoSuchEntityException {
    when(this.mockRestTemplate.getForObject(URI.create(Constants.BASE_URL
        + POLICY_PREFIX
        + "?"
        + URI_VARIABLES_SOLUTION_ID
        + "=urn:solution:demo"), Policy[].class)).thenReturn(this.POLICIES);

    final Set<Policy> deployedPolicies = this.connector
        .getPolicies(new SolutionId("urn:solution:" + "demo"));
    Assertions.assertEquals(this.policyList.iterator().next(), deployedPolicies.iterator().next());
    Mockito.verify(this.mockRestTemplate)
        .getForObject(URI.create(Constants.BASE_URL
            + POLICY_PREFIX
            + "?"
            + URI_VARIABLES_SOLUTION_ID
            + "=urn:solution:demo"), Policy[].class);
  }

  @Test
  public void whenGetPoliciesThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final Map<String, Object> uriVariables = new HashMap<String, Object>();
      uriVariables.put(URI_VARIABLES_SOLUTION_ID, new SolutionId("urn:solution:" + "demo"));

      when(this.mockRestTemplate.getForObject(ArgumentMatchers.any(URI.class),
          ArgumentMatchers.eq(Policy[].class))).thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.getPolicies(new SolutionId("urn:solution:" + "demo"));
    });
  }

  @Test
  public void whenListPolicies_thenReturnPolicyIds()
      throws ClientProtocolException, IOException, InvalidEntityException, NoSuchEntityException {
    final SolutionId solutionId = new SolutionId("urn:solution:" + "test");

    when(this.mockRestTemplate.getForObject(
        URI.create(Constants.BASE_URL + POLICY_IDS_PREFIX + "?solution-id=urn:solution:test"),
        PolicyId[].class)).thenReturn(this.POLICY_IDS);

    final Set<PolicyId> policyIds = this.connector.listPolicies(solutionId);
    Assertions.assertEquals(this.deployedPoliciesList.iterator().next(), policyIds.iterator().next());
  }

  @Test
  public void whenListPoliciesThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {

      when(this.mockRestTemplate.getForObject(
          URI.create(Constants.BASE_URL + POLICY_IDS_PREFIX + "?solution-id=urn:solution:demo"),
          PolicyId[].class)).thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.listPolicies(new SolutionId("urn:solution:" + "demo"));
    });
  }

  @Test
  public void whenGetPolicy_thenReturnPolicy() throws UnsupportedOperationException, IOException,
      InvalidEntityException, NoSuchEntityException {
    when(this.mockRestTemplate.getForObject(Constants.BASE_URL + POLICY_PREFIX + "/" + POLICY_ID,
        Policy.class)).thenReturn(this.POLICY);

    final Policy result = this.connector.getPolicy(POLICY_ID);
    Assertions.assertEquals(result, this.POLICY);

    Mockito.verify(this.mockRestTemplate)
        .getForObject(Constants.BASE_URL + POLICY_PREFIX + "/" + POLICY_ID, Policy.class);
  }

  @Test
  public void whenGetPolicyThrowsRestClientException_thenThrowIOException()
      throws UnsupportedOperationException, InvalidEntityException,
      NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      when(this.mockRestTemplate.getForObject(Constants.BASE_URL + POLICY_PREFIX + "/" + POLICY_ID,
          Policy.class)).thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.getPolicy(POLICY_ID);
    });
  }

  @Test
  public void whenListDeployedPoliciesThrowsHttpServerErrorExceptionNotFound_thenThrowNoSuchEntityException()
      throws ClientProtocolException, IOException, InvalidEntityException {
    assertThrows(NoSuchEntityException.class, () -> {
      final SolutionId solutionId = new SolutionId("urn:solution:" + "demo");

      when(this.mockRestTemplate.getForObject(ArgumentMatchers.any(URI.class),
          ArgumentMatchers.eq(PolicyId[].class))).thenThrow(HTTP_SERVER_ERROR_EXCEPTION_NOT_FOUND);
      this.connector.listDeployedPolicies(solutionId);
    });
  }

  @Test
  public void whenListDeployedPolicies_thenReturnPolicies()
      throws ClientProtocolException, IOException, InvalidEntityException, NoSuchEntityException {
    final SolutionId solutionId = new SolutionId("urn:solution:" + "demo");
    when(this.mockRestTemplate.getForObject(URI.create(
            Constants.BASE_URL + POLICY_IDS_PREFIX + "?solution-id=urn:solution:demo&deployed=true"),
        PolicyId[].class)).thenReturn(this.POLICY_IDS);
    final Set<PolicyId> deployedPolicyIds = this.connector.listDeployedPolicies(solutionId);
    Assertions.assertEquals(this.deployedPoliciesList.iterator().next(),
        deployedPolicyIds.iterator().next());
  }

  @Test
  public void whenListDeployedPoliciesThrosRestClientException_thenThrowIOException()
      throws InvalidEntityException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final SolutionId solutionId = new SolutionId("urn:solution:" + "demo");

      when(this.mockRestTemplate.getForObject(URI.create(
              Constants.BASE_URL + POLICY_IDS_PREFIX + "?solution-id=urn:solution:demo&deployed=true"),
          PolicyId[].class)).thenThrow(REST_CLIENT_EXCEPTION);
      this.connector.listDeployedPolicies(solutionId);
    });
  }

  @Test
  public void whenLookupPdp_thenReturnPdpComponentInformation()
      throws IOException, NoSuchEntityException {
    when(this.mockRestTemplate.getForObject(Constants.BASE_URL
        + COMPONENT_PREFIX
        + "/"
        + ComponentType.PDP.name().toString().toLowerCase(), PdpComponentInformation.class))
        .thenReturn(this.PDP_COMPONENT);
    final PdpComponentInformation comp = this.connector.getPdp();
    Assertions.assertEquals(this.PDP_COMPONENT, comp);

  }

  @Test
  public void whenLookupPdpThrowsRestClientException_thenThrowIOException()
      throws NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      when(this.mockRestTemplate.getForObject(
          Constants.BASE_URL + COMPONENT_PREFIX + "/" + ComponentType.PDP.name().toLowerCase(),
          PdpComponentInformation.class)).thenThrow(REST_CLIENT_EXCEPTION);
      this.connector.getPdp();
    });
  }

  @Test
  public void whenLookupPep_thenOk()
      throws IOException, InvalidEntityException, NoSuchEntityException {
    final SolutionId solutionId = new SolutionId("urn:solution:" + "test");

    // when(mockRestTemplate.getForObject(this.createLookupUri(ComponentType.PEP),
    // PdpComponentInformation[].class)).thenReturn(PEP_COMPONENTS);
    when(this.mockRestTemplate.getForObject(
        URI.create(Constants.BASE_URL + COMPONENT_PREFIX + "/pep?solution-id=urn:solution:test"),
        PepComponentInformation[].class)).thenReturn(this.PEP_COMPONENTS);
    final Set<PepComponentInformation> comp = this.connector.lookupPep(solutionId);
    Assertions.assertEquals(this.PEP_COMPONENT_LIST, comp);
  }

  @Test
  public void whenLookupPepThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final Map<String, Object> uriVariables = new HashMap<String, Object>();
      uriVariables.put(URI_VARIABLES_SOLUTION_ID, null);

      when(this.mockRestTemplate.getForObject(
          URI.create(Constants.BASE_URL + COMPONENT_PREFIX + "/pep"),
          PepComponentInformation[].class)).thenThrow(REST_CLIENT_EXCEPTION);
      this.connector.lookupPep(null);
    });
  }

  @Test
  public void whenLookupPip_thenOk()
      throws IOException, InvalidEntityException, NoSuchEntityException {
    final SolutionId solutionId = new SolutionId("urn:solution:" + "test");

    final Map<String, Object> uriVariables = new HashMap<String, Object>();
    uriVariables.put(URI_VARIABLES_SOLUTION_ID, solutionId);

    when(this.mockRestTemplate.getForObject(
        URI.create(Constants.BASE_URL + COMPONENT_PREFIX + "/pip?solution-id=urn:solution:test"),
        PipComponentInformation[].class)).thenReturn(this.PIP_COMPONENTS);
    final Set<PipComponentInformation> comp = this.connector.lookupPip(solutionId, null);
    Assertions.assertEquals(this.PIP_COMPONENT_LIST, comp);
    Mockito.verify(this.mockRestTemplate).getForObject(
        URI.create(Constants.BASE_URL + COMPONENT_PREFIX + "/pip?solution-id=urn:solution:test"),
        PipComponentInformation[].class);
  }

  @Test
  public void whenLookupPipWithMethodInterfaceDescriptions_thenOk()
      throws IOException, InvalidEntityException, NoSuchEntityException {
    final SolutionId solutionId = new SolutionId("urn:solution:test");
    final MethodInterfaceDescription query = new MethodInterfaceDescription("foo", String.class,
        "somefoo");
    final UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
        .fromUriString(Constants.BASE_URL + COMPONENT_PREFIX + "/pip");
    uriComponentsBuilder.queryParam(URI_VARIABLES_SOLUTION_ID, solutionId);
    uriComponentsBuilder.queryParam(URI_VARIABLES_QUERY, query.toJson(false));
    final URI url = URI.create(uriComponentsBuilder.toUriString());

    when(this.mockRestTemplate.getForObject(url, PipComponentInformation[].class))
        .thenReturn(this.PIP_COMPONENTS);
    final Set<PipComponentInformation> comp = this.connector.lookupPip(solutionId, query);
    Mockito.verify(this.mockRestTemplate).getForObject(url, PipComponentInformation[].class);
    Assertions.assertEquals(this.PIP_COMPONENT_LIST, comp);
  }

  @Test
  public void whenLookupPipThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {

      when(this.mockRestTemplate.getForObject(ArgumentMatchers.any(URI.class),
          ArgumentMatchers.eq(PipComponentInformation[].class))).thenThrow(REST_CLIENT_EXCEPTION);
      this.connector.lookupPip(null, null);
      Assertions.fail();
    });
  }

  @Test
  public void whenLookupPxp_thenOk()
      throws ClientProtocolException, IOException, InvalidEntityException, NoSuchEntityException {

    when(this.mockRestTemplate.getForObject(
        URI.create(Constants.BASE_URL + COMPONENT_PREFIX + "/pxp"),
        PxpComponentInformation[].class)).thenReturn(this.PXP_COMPONENTS);
    final Set<PxpComponentInformation> comp = this.connector.lookupPxp(null, null);
    Assertions.assertEquals(this.PXP_COMPONENT_LIST, comp);
  }

  @Test
  public void whenLookupPxpWithQuery_thenOk()
      throws IOException, URISyntaxException, InvalidEntityException, NoSuchEntityException {
    final MethodInterfaceDescription query = new MethodInterfaceDescription("foo", String.class,
        "somefoo");

    final UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
        .fromUriString(Constants.BASE_URL + COMPONENT_PREFIX + "/pxp");
    uriComponentsBuilder.queryParam(URI_VARIABLES_QUERY, query.toJson(false));
    when(this.mockRestTemplate.getForObject(URI.create(uriComponentsBuilder.toUriString()),
        PxpComponentInformation[].class)).thenReturn(this.PXP_COMPONENTS);
    final Set<PxpComponentInformation> comp = this.connector.lookupPxp(null, query);
    Assertions.assertEquals(this.PXP_COMPONENT_LIST, comp);
  }

  @Test
  public void whenLookupPxpWithQueryThrowsRestClientException_thenThrowIOException()
      throws URISyntaxException, InvalidEntityException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final MethodInterfaceDescription query = new MethodInterfaceDescription("foo", String.class,
          "somefoo");

      when(this.mockRestTemplate.getForObject(ArgumentMatchers.any(URI.class),
          ArgumentMatchers.eq(PxpComponentInformation[].class))).thenThrow(REST_CLIENT_EXCEPTION);
      this.connector.lookupPxp(null, query);
      Mockito.verify(this.mockRestTemplate)
          .getForObject(URI.create(Constants.BASE_URL
              + COMPONENT_PREFIX
              + "pxp?"
              + URI_VARIABLES_QUERY
              + "="
              + query.toJson(false)), PxpComponentInformation[].class);
    });
  }

  @Test
  public void whenAddPdp_thenReturnPdpComponentId()
      throws IOException, ConflictingResourceException, InvalidEntityException,
      ResourceUpdateException, NoSuchEntityException {
    final String url = Constants.BASE_URL + COMPONENT_PREFIX + "/pdp";
    Mockito.when(this.mockRestTemplate.postForObject(url, this.PDP_COMPONENT, ComponentId.class))
        .thenReturn(PDP_COMPONENT_ID);

    final ComponentId result = this.connector.addPdp(this.PDP_COMPONENT);
    Mockito.verify(this.mockRestTemplate).postForObject(url, this.PDP_COMPONENT, ComponentId.class);
    Assertions.assertEquals(PDP_COMPONENT_ID, result);
  }

  @Test
  public void whenAddPdpThrowsRestClientException_thenThrowIOException()
      throws ConflictingResourceException,
      InvalidEntityException, ResourceUpdateException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final String url = Constants.BASE_URL + COMPONENT_PREFIX + "/pdp";
      Mockito.when(this.mockRestTemplate.postForObject(url, this.PDP_COMPONENT, ComponentId.class))
          .thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.addPdp(this.PDP_COMPONENT);
      Assertions.fail();
    });
  }

  @Test
  public void whenRestTemplateReturnsStatusCodeNot200_ThenReturnFalse()
      throws UnsupportedOperationException, IOException, ResourceUpdateException,
      InvalidEntityException, NoSuchEntityException {
    final String url = Constants.BASE_URL + POLICY_PREFIX + "/" + POLICY_ID + "/deployed";
//    when(this.mockRestTemplate.patchForObject(
//        url,
//        Boolean.FALSE, HttpStatus.class)).thenReturn(HttpStatus.GONE);

    this.connector.revokePolicy(POLICY_ID);

    Mockito.verify(this.mockRestTemplate).patchForObject(
        url,
        Boolean.FALSE, Void.class);
  }

  @Test
  public void addPep() throws IOException, ConflictingResourceException, InvalidEntityException,
      ResourceUpdateException, NoSuchEntityException {
    when(this.mockRestTemplate.postForObject(Constants.BASE_URL + COMPONENT_PREFIX + "/pep",
        this.PEP_COMPONENT, ComponentId.class)).thenReturn(this.PEP_COMPONENT.getComponentId());

    final ComponentId result = this.connector.addPep(this.PEP_COMPONENT);
    Assertions.assertEquals(this.PEP_COMPONENT.getComponentId(), result);
  }

  @Test
  public void addPepThrowsRestClientException() throws ConflictingResourceException,
      InvalidEntityException, ResourceUpdateException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      when(this.mockRestTemplate.postForObject(Constants.BASE_URL + COMPONENT_PREFIX + "/pep",
          this.PEP_COMPONENT, ComponentId.class)).thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.addPep(this.PEP_COMPONENT);
      Assertions.fail();
    });
  }

  @Test
  public void addPepThrowsHttpServerExceptionNotFound()
      throws IOException, ConflictingResourceException, InvalidEntityException,
      ResourceUpdateException {
    assertThrows(NoSuchEntityException.class, () -> {
      when(this.mockRestTemplate.postForObject(Constants.BASE_URL + COMPONENT_PREFIX + "/pep",
          this.PEP_COMPONENT, ComponentId.class)).thenThrow(HTTP_SERVER_ERROR_EXCEPTION_NOT_FOUND);

      this.connector.addPep(this.PEP_COMPONENT);
      Assertions.fail();
    });
  }

  @Test
  public void addPip() throws IOException, ConflictingResourceException, InvalidEntityException,
      ResourceUpdateException, NoSuchEntityException {
    when(this.mockRestTemplate.postForObject(Constants.BASE_URL + COMPONENT_PREFIX + "/pip",
        this.PIP_COMPONENT, ComponentId.class)).thenReturn(this.PIP_COMPONENT.getComponentId());

    final ComponentId result = this.connector.addPip(this.PIP_COMPONENT);
    Assertions.assertEquals(this.PIP_COMPONENT.getComponentId(), result);
  }

  @Test
  public void addPipThrowsRestClientException() throws ConflictingResourceException,
      InvalidEntityException, ResourceUpdateException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      when(this.mockRestTemplate.postForObject(Constants.BASE_URL + COMPONENT_PREFIX + "/pip",
          this.PIP_COMPONENT, ComponentId.class)).thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.addPip(this.PIP_COMPONENT);
      Assertions.fail();
    });
  }

  @Test
  public void addPxp() throws IOException, ConflictingResourceException, InvalidEntityException,
      ResourceUpdateException, NoSuchEntityException {
    when(this.mockRestTemplate.postForObject(Constants.BASE_URL + COMPONENT_PREFIX + "/pxp",
        this.PXP_COMPONENT, ComponentId.class)).thenReturn(this.PXP_COMPONENT.getComponentId());

    final ComponentId result = this.connector.addPxp(this.PXP_COMPONENT);
    Assertions.assertEquals(this.PXP_COMPONENT.getComponentId(), result);
  }

  @Test
  public void addPxpThrowsRestClientException() throws ConflictingResourceException,
      InvalidEntityException, ResourceUpdateException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      when(this.mockRestTemplate.postForObject(Constants.BASE_URL + COMPONENT_PREFIX + "/pxp",
          this.PXP_COMPONENT, ComponentId.class)).thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.addPxp(this.PXP_COMPONENT);
      Assertions.fail();
    });
  }

  @Test
  public void addPolicy() throws IOException, ConflictingResourceException, InvalidEntityException,
      ResourceUpdateException, NoSuchEntityException {
    final String url = Constants.BASE_URL + POLICY_PREFIX;
    Mockito.when(this.mockRestTemplate.postForObject(url, this.POLICY, PolicyId.class))
        .thenReturn(POLICY_ID);
    final PolicyId result = this.connector.addPolicy(this.POLICY);
    Assertions.assertEquals(POLICY_ID, result);
  }

  @Test
  public void addPolicyThrowsRestClientException() throws ConflictingResourceException,
      InvalidEntityException, ResourceUpdateException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final String url = Constants.BASE_URL + POLICY_PREFIX;
      Mockito.when(this.mockRestTemplate.postForObject(url, this.POLICY, PolicyId.class))
          .thenThrow(REST_CLIENT_EXCEPTION);
      this.connector.addPolicy(this.POLICY);
      Assertions.fail();
    });
  }

  @Test
  public void addTimer() throws IOException, ConflictingResourceException, InvalidEntityException,
      ResourceUpdateException, NoSuchEntityException {
    final String timerXml = "<timer></timer>";
    final TimerId tId = new TimerId("test");
    final Timer timer = new Timer(timerXml);

    when(this.mockRestTemplate.postForObject(Constants.BASE_URL + TIMER_PREFIX, timer,
        TimerId.class)).thenReturn(tId);

    final TimerId result = this.connector.addTimer(timer);
    Assertions.assertEquals(tId, result);

    Mockito.verify(this.mockRestTemplate).postForObject(Constants.BASE_URL + TIMER_PREFIX, timer,
        TimerId.class);
  }

  @Test
  public void addTimerThrowsRestClientException() throws ConflictingResourceException,
      InvalidEntityException, ResourceUpdateException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final String timerXml = "<timer></timer>";
      final Timer timer = new Timer(timerXml);

      when(this.mockRestTemplate.postForObject(Constants.BASE_URL + TIMER_PREFIX, timer,
          TimerId.class)).thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.addTimer(timer);
      Assertions.fail();
    });
  }

  @Test
  public void addTimerThrowNotFound() throws IOException, ConflictingResourceException,
      InvalidEntityException, ResourceUpdateException {
    assertThrows(NoSuchEntityException.class, () -> {
      final String timerXml = "<timer></timer>";
      final Timer timer = new Timer(timerXml);

      when(this.mockRestTemplate.postForObject(Constants.BASE_URL + TIMER_PREFIX, timer,
          TimerId.class)).thenThrow(HTTP_SERVER_ERROR_EXCEPTION_NOT_FOUND);

      this.connector.addTimer(timer);
      Assertions.fail();
    });
  }

  @Test
  public void addTimerThrowBadRequest() throws IOException, ConflictingResourceException, ResourceUpdateException, NoSuchEntityException {
    assertThrows(InvalidEntityException.class, () -> {
      final String timerXml = "<timer></timer>";
      final Timer timer = new Timer(timerXml);

      when(this.mockRestTemplate.postForObject(Constants.BASE_URL + TIMER_PREFIX, timer,
          TimerId.class)).thenThrow(HTTP_SERVER_ERROR_EXCEPTION_BAD_REQUEST);

      this.connector.addTimer(timer);
      Assertions.fail();
    });
  }

  @Test
  public void addTimerThrowInternalServerError() throws IOException, ConflictingResourceException,
      InvalidEntityException, NoSuchEntityException {
    assertThrows(ResourceUpdateException.class, () -> {
      final String timerXml = "<timer></timer>";
      final Timer timer = new Timer(timerXml);

      when(this.mockRestTemplate.postForObject(Constants.BASE_URL + TIMER_PREFIX, timer,
          TimerId.class)).thenThrow(HTTP_SERVER_ERROR_EXCEPTION_INTERNAL_SERVER_ERROR);

      this.connector.addTimer(timer);
      Assertions.fail();
    });
  }

  @Test
  public void whenDeletePep_thenOk()
      throws IOException, InvalidEntityException, ResourceUpdateException, NoSuchEntityException {
    this.connector.deletePep(PEP_COMPONENT_ID);
    Mockito.verify(this.mockRestTemplate)
        .delete(Constants.BASE_URL + COMPONENT_PREFIX + "/pep/" + PEP_COMPONENT_ID.getUrn());
  }

  @Test
  public void whenDeletePepThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException, ResourceUpdateException,
      NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      doThrow(REST_CLIENT_EXCEPTION).when(this.mockRestTemplate)
          .delete(Constants.BASE_URL + COMPONENT_PREFIX + "/pep/" + PEP_COMPONENT_ID);

      this.connector.deletePep(PEP_COMPONENT_ID);
      Assertions.fail();
    });
  }

  @Test
  public void whenDeletePip_thenOk()
      throws IOException, InvalidEntityException, ResourceUpdateException, NoSuchEntityException {
    this.connector.deletePip(PIP_COMPONENT_ID);
    Mockito.verify(this.mockRestTemplate)
        .delete(Constants.BASE_URL + COMPONENT_PREFIX + "/pip/" + PIP_COMPONENT_ID.getUrn());
  }

  @Test
  public void whenDeletePipThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException, ResourceUpdateException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      doThrow(REST_CLIENT_EXCEPTION).when(this.mockRestTemplate)
          .delete(Constants.BASE_URL + COMPONENT_PREFIX + "/pip/" + PIP_COMPONENT_ID);

      this.connector.deletePip(PIP_COMPONENT_ID);
      Assertions.fail();
    });
  }

  @Test
  public void whenDeletePxp_thenOk()
      throws IOException, InvalidEntityException, ResourceUpdateException, NoSuchEntityException {
    this.connector.deletePxp(PXP_COMPONENT_ID);
    Mockito.verify(this.mockRestTemplate)
        .delete(Constants.BASE_URL + COMPONENT_PREFIX + "/pxp/" + PXP_COMPONENT_ID.getUrn());
  }

  @Test
  public void whenDeletePxpThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException, ResourceUpdateException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      doThrow(REST_CLIENT_EXCEPTION).when(this.mockRestTemplate)
          .delete(Constants.BASE_URL + COMPONENT_PREFIX + "/pxp/" + PXP_COMPONENT_ID);

      this.connector.deletePxp(PXP_COMPONENT_ID);
      Assertions.fail();
    });
  }

  @Test
  public void whenDeletePolicy_thenOk() throws IOException, InvalidEntityException,
      ResourceUpdateException, ConflictingResourceException, NoSuchEntityException {
    this.connector.deletePolicy(this.POLICY.getPolicyId());
    Mockito.verify(this.mockRestTemplate)
        .delete(Constants.BASE_URL + POLICY_PREFIX + "/" + this.POLICY.getPolicyId().getUrn());
  }

  @Test
  public void whenDeletePolicyThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException, ResourceUpdateException,
      ConflictingResourceException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      Mockito.doThrow(REST_CLIENT_EXCEPTION).when(this.mockRestTemplate)
          .delete(Constants.BASE_URL + POLICY_PREFIX + "/" + this.POLICY.getPolicyId().getUrn());
      this.connector.deletePolicy(this.POLICY.getPolicyId());
      Assertions.fail();
    });
  }

  @Test
  public void whenDeleteTimer_thenOk() throws IOException, InvalidEntityException,
      ResourceUpdateException, ConflictingResourceException, NoSuchEntityException {
    this.connector.deleteTimer(this.timer.getTimerId());
    Mockito.verify(this.mockRestTemplate)
        .delete(Constants.BASE_URL + TIMER_PREFIX + "/" + this.timer.getTimerId().getUrn());
  }

  @Test
  public void whenDeleteTimerThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException, ResourceUpdateException,
      ConflictingResourceException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      Mockito.doThrow(REST_CLIENT_EXCEPTION).when(this.mockRestTemplate)
          .delete(Constants.BASE_URL + TIMER_PREFIX + "/" + this.timer.getTimerId().getUrn());
      this.connector.deleteTimer(this.timer.getTimerId());
      Assertions.fail();
    });
  }

  @Test
  public void whenDeployTimerThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException, ResourceUpdateException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final TimerId timerId = new TimerId("urn:timer:cs3:mock");

      when(this.mockRestTemplate.patchForObject(
          Constants.BASE_URL + TIMER_PREFIX + "/" + timerId + "/deployed", Boolean.TRUE, Void.class))
          .thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.deployTimer(timerId);
      Assertions.fail();
    });
  }

  @Test
  public void whenGetPep_thenReturnPepComponentInformation()
      throws IOException, InvalidEntityException, NoSuchEntityException {
    when(this.mockRestTemplate.getForObject(
        Constants.BASE_URL + COMPONENT_PREFIX + "/pep/" + PEP_COMPONENT_ID,
        PepComponentInformation.class)).thenReturn(this.PEP_COMPONENT);

    final PepComponentInformation comp = this.connector.getPep(PEP_COMPONENT_ID);
    Assertions.assertEquals(this.PEP_COMPONENT, comp);
  }

  @Test
  public void whenGetPepThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      when(this.mockRestTemplate.getForObject(
          Constants.BASE_URL + COMPONENT_PREFIX + "/pep/" + PEP_COMPONENT_ID,
          PepComponentInformation.class)).thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.getPep(PEP_COMPONENT_ID);
      Assertions.fail();
    });
  }

  @Test
  public void whenGetPip_thenReturnPipComponentInformation()
      throws IOException, InvalidEntityException, NoSuchEntityException {
    when(this.mockRestTemplate.getForObject(
        Constants.BASE_URL + COMPONENT_PREFIX + "/pip/" + PIP_COMPONENT_ID,
        PipComponentInformation.class)).thenReturn(this.PIP_COMPONENT);

    final PipComponentInformation comp = this.connector.getPip(PIP_COMPONENT_ID);
    Assertions.assertEquals(this.PIP_COMPONENT, comp);
  }

  @Test
  public void whenGetPipThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      when(this.mockRestTemplate.getForObject(
          Constants.BASE_URL + COMPONENT_PREFIX + "/pip/" + PIP_COMPONENT_ID,
          PipComponentInformation.class)).thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.getPip(PIP_COMPONENT_ID);
      Assertions.fail();
    });
  }

  @Test
  public void whenGetPxp_thenReturnPxpComponentInformation()
      throws IOException, InvalidEntityException, NoSuchEntityException {
    when(this.mockRestTemplate.getForObject(
        Constants.BASE_URL + COMPONENT_PREFIX + "/pxp/" + PXP_COMPONENT_ID,
        PxpComponentInformation.class)).thenReturn(this.PXP_COMPONENT);

    final PxpComponentInformation comp = this.connector.getPxp(PXP_COMPONENT_ID);
    Assertions.assertEquals(this.PXP_COMPONENT, comp);
  }

  @Test
  public void whenGetPxpThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      when(this.mockRestTemplate.getForObject(
          Constants.BASE_URL + COMPONENT_PREFIX + "/pxp/" + PXP_COMPONENT_ID,
          PxpComponentInformation.class)).thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.getPxp(PXP_COMPONENT_ID);
      Assertions.fail();
    });
  }

  @Test
  public void whenGetAllComponentStates_thenReturnHealthStatuses()
      throws IOException, InvalidEntityException, NoSuchEntityException {
    final SolutionId solutionId = new SolutionId("urn:solution:test-solution");
    final ParameterizedTypeReference<Map<ComponentId, HealthStatus>> responseType = new ParameterizedTypeReference<Map<ComponentId, HealthStatus>>() {
    };
    final URI uri = URI.create(
        Constants.BASE_URL + COMPONENT_PREFIX + "/status?solution-id=" + solutionId.getUrn());
    final Map<ComponentId, HealthStatus> states = Collections.emptyMap();

    when(this.mockRestTemplate.exchange(uri, HttpMethod.GET, null, responseType))
        .thenReturn(new ResponseEntity<>(states, HttpStatus.OK));

    final Map<ComponentId, HealthStatus> result = this.connector.getAllComponentStates(solutionId);

    Assertions.assertEquals(states, result);
  }

  @Test
  public void whenGetAllComponentStatesThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final SolutionId solutionId = new SolutionId("urn:solution:test-solution");
      final ParameterizedTypeReference<Map<ComponentId, HealthStatus>> responseType = new ParameterizedTypeReference<Map<ComponentId, HealthStatus>>() {
      };
      final URI uri = URI.create(
          Constants.BASE_URL + COMPONENT_PREFIX + "/status?solution-id=" + solutionId.getUrn());

      when(this.mockRestTemplate.exchange(uri, HttpMethod.GET, null, responseType))
          .thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.getAllComponentStates(solutionId);
      Assertions.fail();
    });
  }

  @Test
  public void whenGetDeployedPoliciesWithoutSolutionId_thenReturnPolicies() throws IOException {
    final URI url = URI
        .create(Constants.BASE_URL + POLICY_PREFIX + "?" + URI_VARIABLES_DEPLOYED + "=true");
    when(this.mockRestTemplate.getForObject(url, Policy[].class)).thenReturn(this.POLICIES);

    final Set<Policy> deployedPolicies = this.connector.getDeployedPolicies();
    Assertions.assertEquals(this.policyList.iterator().next(), deployedPolicies.iterator().next());
    Mockito.verify(this.mockRestTemplate).getForObject(url, Policy[].class);
  }

  @Test
  public void whenGetDeployedPoliciesWithoutSolutionIdThrowsRestClientException_thenThrowIOException() {
    assertThrows(IOException.class, () -> {
      final URI url = URI
          .create(Constants.BASE_URL + POLICY_PREFIX + "?" + URI_VARIABLES_DEPLOYED + "=true");
      when(this.mockRestTemplate.getForObject(url, Policy[].class)).thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.getDeployedPolicies();
      Assertions.fail();
    });
  }

  @Test
  public void whenGetTimers_thenReturnTimers()
      throws IOException, InvalidEntityException, NoSuchEntityException {
    final URI uri = URI.create(
        Constants.BASE_URL + TIMER_PREFIX + "?" + URI_VARIABLES_SOLUTION_ID + "=urn:solution:demo");

    when(this.mockRestTemplate.getForObject(uri, Timer[].class)).thenReturn(this.timers);

    final Set<Timer> deployedTimers = this.connector.getTimers(new SolutionId("urn:solution:demo"));
    Assertions.assertEquals(deployedTimers.iterator().next(), this.timerList.iterator().next());
    Mockito.verify(this.mockRestTemplate).getForObject(uri, Timer[].class);
  }

  @Test
  public void whenGetTimersThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final URI uri = URI.create(
          Constants.BASE_URL + TIMER_PREFIX + "?" + URI_VARIABLES_SOLUTION_ID + "=urn:solution:demo");

      when(this.mockRestTemplate.getForObject(uri, Timer[].class)).thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.getTimers(new SolutionId("urn:solution:demo"));
      Assertions.fail();
    });
  }

  @Test
  public void whenGetDeployedTimers_thenReturnTimers()
      throws IOException, InvalidEntityException, NoSuchEntityException {
    final URI uri = URI.create(Constants.BASE_URL
        + TIMER_PREFIX
        + "?"
        + URI_VARIABLES_SOLUTION_ID
        + "=urn:solution:demo&"
        + URI_VARIABLES_DEPLOYED
        + "=true");

    when(this.mockRestTemplate.getForObject(uri, Timer[].class)).thenReturn(this.timers);

    final Set<Timer> deployedTimers = this.connector
        .getDeployedTimers(new SolutionId("urn:solution:demo"));
    Assertions.assertEquals(deployedTimers.iterator().next(), this.timerList.iterator().next());
    Mockito.verify(this.mockRestTemplate).getForObject(uri, Timer[].class);
  }

  @Test
  public void whenGetDeployedTimersThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final URI uri = URI.create(Constants.BASE_URL
          + TIMER_PREFIX
          + "?"
          + URI_VARIABLES_SOLUTION_ID
          + "=urn:solution:demo&"
          + URI_VARIABLES_DEPLOYED
          + "=true");

      when(this.mockRestTemplate.getForObject(uri, Timer[].class)).thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.getDeployedTimers(new SolutionId("urn:solution:demo"));
      Assertions.fail();
    });
  }

  @Test
  public void whenGetPipState_thenReturnHealthStatus()
      throws IOException, InvalidEntityException, NoSuchEntityException {
    final String url = Constants.BASE_URL
        + COMPONENT_PREFIX
        + "/pip/"
        + PIP_COMPONENT_ID
        + "/status";
    final HealthStatus healthStatus = HealthStatus.of(Status.UP);
    Mockito.when(this.mockRestTemplate.getForObject(url, HealthStatus.class))
        .thenReturn(healthStatus);
    final HealthStatus result = this.connector.getPipState(PIP_COMPONENT_ID);

    Assertions.assertEquals(healthStatus, result);
    Mockito.verify(this.mockRestTemplate).getForObject(url, HealthStatus.class);
  }

  @Test
  public void whenGetPipStateThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final String url = Constants.BASE_URL
          + COMPONENT_PREFIX
          + "/pip/"
          + PIP_COMPONENT_ID
          + "/status";
      Mockito.when(this.mockRestTemplate.getForObject(url, HealthStatus.class))
          .thenThrow(REST_CLIENT_EXCEPTION);
      this.connector.getPipState(PIP_COMPONENT_ID);
      Assertions.fail();
    });
  }

  @Test
  public void whenGetPxpState_thenReturnHealthStatus()
      throws IOException, InvalidEntityException, NoSuchEntityException {
    final String url = Constants.BASE_URL
        + COMPONENT_PREFIX
        + "/pxp/"
        + PXP_COMPONENT_ID
        + "/status";
    final HealthStatus healthStatus = HealthStatus.of(Status.UP);
    Mockito.when(this.mockRestTemplate.getForObject(url, HealthStatus.class))
        .thenReturn(healthStatus);
    final HealthStatus result = this.connector.getPxpState(PXP_COMPONENT_ID);

    Assertions.assertEquals(healthStatus, result);
    Mockito.verify(this.mockRestTemplate).getForObject(url, HealthStatus.class);
  }

  @Test
  public void whenGetPxpStateThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final String url = Constants.BASE_URL
          + COMPONENT_PREFIX
          + "/pxp/"
          + PXP_COMPONENT_ID
          + "/status";
      Mockito.when(this.mockRestTemplate.getForObject(url, HealthStatus.class))
          .thenThrow(REST_CLIENT_EXCEPTION);
      this.connector.getPxpState(PXP_COMPONENT_ID);
      Assertions.fail();
    });
  }

  @Test
  public void whenGetRevokedPolicies_thenReturnPolicies()
      throws IOException, InvalidEntityException, NoSuchEntityException {
    final URI url = URI.create(Constants.BASE_URL
        + POLICY_PREFIX
        + "?"
        + URI_VARIABLES_SOLUTION_ID
        + "=urn:solution:demo&"
        + URI_VARIABLES_DEPLOYED
        + "=false");

    Mockito.when(this.mockRestTemplate.getForObject(url, Policy[].class)).thenReturn(this.POLICIES);

    final Set<Policy> revokedPolicies = this.connector
        .getRevokedPolicies(new SolutionId("urn:solution:" + "demo"));
    Mockito.verify(this.mockRestTemplate).getForObject(url, Policy[].class);
    Assertions.assertEquals(Sets.newHashSet(this.POLICIES), revokedPolicies);
  }

  @Test
  public void whenGetRevokedPoliciesThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final URI url = URI.create(Constants.BASE_URL
          + POLICY_PREFIX
          + "?"
          + URI_VARIABLES_SOLUTION_ID
          + "=urn:solution:demo&"
          + URI_VARIABLES_DEPLOYED
          + "=false");

      Mockito.when(this.mockRestTemplate.getForObject(url, Policy[].class))
          .thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.getRevokedPolicies(new SolutionId("urn:solution:" + "demo"));
      Assertions.fail();
    });
  }

  @Test
  public void whenGetRevokedTimers_thenReturnTimers()
      throws IOException, InvalidEntityException, NoSuchEntityException {
    final URI uri = URI.create(Constants.BASE_URL
        + TIMER_PREFIX
        + "?"
        + URI_VARIABLES_SOLUTION_ID
        + "=urn:solution:demo&"
        + URI_VARIABLES_DEPLOYED
        + "=false");

    when(this.mockRestTemplate.getForObject(uri, Timer[].class)).thenReturn(this.timers);

    final Set<Timer> revokedTimers = this.connector
        .getRevokedTimers(new SolutionId("urn:solution:demo"));
    Assertions.assertEquals(revokedTimers.iterator().next(), this.timerList.iterator().next());
    Mockito.verify(this.mockRestTemplate).getForObject(uri, Timer[].class);
  }

  @Test
  public void whenGetRevokedTimersThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final URI uri = URI.create(Constants.BASE_URL
          + TIMER_PREFIX
          + "?"
          + URI_VARIABLES_SOLUTION_ID
          + "=urn:solution:demo&"
          + URI_VARIABLES_DEPLOYED
          + "=false");

      when(this.mockRestTemplate.getForObject(uri, Timer[].class)).thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.getRevokedTimers(new SolutionId("urn:solution:demo"));
      Assertions.fail();
    });
  }

  @Test
  public void whenGetTimer_thenReturnTimer()
      throws IOException, InvalidEntityException, NoSuchEntityException {
    when(this.mockRestTemplate.getForObject(
        Constants.BASE_URL + TIMER_PREFIX + "/" + this.timer.getTimerId(), Timer.class))
        .thenReturn(this.timer);

    final Timer result = this.connector.getTimer(this.timer.getTimerId());
    Assertions.assertEquals(result, this.timer);

    Mockito.verify(this.mockRestTemplate).getForObject(
        Constants.BASE_URL + TIMER_PREFIX + "/" + this.timer.getTimerId(), Timer.class);
  }

  @Test
  public void whenGetTimerThrowsHttpServerErrorExceptionBadRequest_thenThrowInvalidEntityException()
      throws IOException, NoSuchEntityException {
    assertThrows(InvalidEntityException.class, () -> {
      when(this.mockRestTemplate.getForObject(
          Constants.BASE_URL + TIMER_PREFIX + "/" + this.timer.getTimerId(), Timer.class))
          .thenThrow(HTTP_SERVER_ERROR_EXCEPTION_BAD_REQUEST);

      this.connector.getTimer(this.timer.getTimerId());
      Assertions.fail();
    });
  }

  @Test
  public void whenGetTimerThrowsHttpServerErrorExceptionNotFound_thenThrowNoSuchEntityEntityException()
      throws IOException, InvalidEntityException {
    assertThrows(NoSuchEntityException.class, () -> {
      when(this.mockRestTemplate.getForObject(
          Constants.BASE_URL + TIMER_PREFIX + "/" + this.timer.getTimerId(), Timer.class))
          .thenThrow(HTTP_SERVER_ERROR_EXCEPTION_NOT_FOUND);

      this.connector.getTimer(this.timer.getTimerId());
      Assertions.fail();
    });
  }

  @Test
  public void whenGetTimerThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      when(this.mockRestTemplate.getForObject(
          Constants.BASE_URL + TIMER_PREFIX + "/" + this.timer.getTimerId(), Timer.class))
          .thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.getTimer(this.timer.getTimerId());
      Assertions.fail();
    });
  }

  @Test
  public void whenIsPolicyDeployed_thenReturnTrue() throws UnsupportedOperationException,
      IOException, InvalidEntityException, NoSuchEntityException {
    when(this.mockRestTemplate.getForObject(
        Constants.BASE_URL + POLICY_PREFIX + "/" + POLICY_ID + "/deployed", Boolean.class))
        .thenReturn(true);
    final boolean result = this.connector.isPolicyDeployed(POLICY_ID);
    Assertions.assertEquals(Boolean.TRUE, result);
  }

  @Test
  public void whenIsPolicyDeployedThrowsRestClientException_thenThrowIOException()
      throws UnsupportedOperationException, InvalidEntityException,
      NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      when(this.mockRestTemplate.getForObject(
          Constants.BASE_URL + POLICY_PREFIX + "/" + POLICY_ID + "/deployed", Boolean.class))
          .thenThrow(REST_CLIENT_EXCEPTION);
      this.connector.isPolicyDeployed(POLICY_ID);
    });
  }

  @Test
  public void whenIsTimerDeployed_thenReturnTrue()
      throws IOException, InvalidEntityException, NoSuchEntityException {
    when(this.mockRestTemplate.getForObject(
        Constants.BASE_URL + TIMER_PREFIX + "/" + this.timer.getTimerId() + "/deployed",
        Boolean.class)).thenReturn(true);
    final boolean result = this.connector.isTimerDeployed(this.timer.getTimerId());
    Assertions.assertEquals(Boolean.TRUE, result);
  }

  @Test
  public void whenIsTimerDeployedThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      when(this.mockRestTemplate.getForObject(
          Constants.BASE_URL + TIMER_PREFIX + "/" + this.timer.getTimerId() + "/deployed",
          Boolean.class)).thenThrow(REST_CLIENT_EXCEPTION);
      this.connector.isTimerDeployed(this.timer.getTimerId());
      Assertions.fail();
    });
  }

  @Test
  public void whenListRevokedPolicies_thenReturnPolicyIds()
      throws ClientProtocolException, IOException, InvalidEntityException, NoSuchEntityException {
    final SolutionId solutionId = new SolutionId("urn:solution:" + "demo");
    when(this.mockRestTemplate.getForObject(URI.create(
            Constants.BASE_URL + POLICY_IDS_PREFIX + "?solution-id=urn:solution:demo&deployed=false"),
        PolicyId[].class)).thenReturn(this.POLICY_IDS);
    final Set<PolicyId> revokedPolicyIds = this.connector.listRevokedPolicies(solutionId);
    Assertions.assertEquals(this.deployedPoliciesList.iterator().next(),
        revokedPolicyIds.iterator().next());
  }

  @Test
  public void whenListTimers_thenReturnTimers()
      throws IOException, InvalidEntityException, NoSuchEntityException {
    final SolutionId solutionId = new SolutionId("urn:solution:demo");
    final TimerId[] timerIds = new TimerId[]{
        new TimerId("urn:timer:demo:test-timer1"), new TimerId("urn:timer:demo:test-timer2")
    };
    final URI url = URI
        .create(Constants.BASE_URL + TIMER_IDS_PREFIX + "?solution-id=" + solutionId.getUrn());
    when(this.mockRestTemplate.getForObject(url, TimerId[].class)).thenReturn(timerIds);
    final Set<TimerId> result = this.connector.listTimers(solutionId);
    Mockito.verify(this.mockRestTemplate).getForObject(url, TimerId[].class);
    Assertions.assertEquals(Sets.newHashSet(timerIds), result);
  }

  @Test
  public void whenListTimersThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final SolutionId solutionId = new SolutionId("urn:solution:demo");
      final URI url = URI
          .create(Constants.BASE_URL + TIMER_IDS_PREFIX + "?solution-id=" + solutionId.getUrn());
      when(this.mockRestTemplate.getForObject(url, TimerId[].class)).thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.listTimers(solutionId);
      Assertions.fail();
    });
  }

  @Test
  public void whenListDeployedTimers_thenReturnTimerIds()
      throws IOException, InvalidEntityException, NoSuchEntityException {
    final SolutionId solutionId = new SolutionId("urn:solution:demo");
    final TimerId[] timerIds = new TimerId[]{
        new TimerId("urn:timer:demo:test-timer1"), new TimerId("urn:timer:demo:test-timer2")
    };
    final URI url = URI.create(Constants.BASE_URL
        + TIMER_IDS_PREFIX
        + "?solution-id="
        + solutionId.getUrn()
        + "&deployed=true");
    when(this.mockRestTemplate.getForObject(url, TimerId[].class)).thenReturn(timerIds);
    final Set<TimerId> result = this.connector.listDeployedTimers(solutionId);
    Mockito.verify(this.mockRestTemplate).getForObject(url, TimerId[].class);
    Assertions.assertEquals(Sets.newHashSet(timerIds), result);
  }

  @Test
  public void whenListDeployedTimersThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final SolutionId solutionId = new SolutionId("urn:solution:demo");
      final URI url = URI.create(Constants.BASE_URL
          + TIMER_IDS_PREFIX
          + "?solution-id="
          + solutionId.getUrn()
          + "&deployed=true");
      when(this.mockRestTemplate.getForObject(url, TimerId[].class)).thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.listDeployedTimers(solutionId);
      Assertions.fail();
    });
  }

  @Test
  public void whenListRevokedTimers_thenReturnTimerIds()
      throws IOException, InvalidEntityException, NoSuchEntityException {
    final SolutionId solutionId = new SolutionId("urn:solution:demo");
    final TimerId[] timerIds = new TimerId[]{
        new TimerId("urn:timer:demo:test-timer1"), new TimerId("urn:timer:demo:test-timer2")
    };
    final URI url = URI.create(Constants.BASE_URL
        + TIMER_IDS_PREFIX
        + "?solution-id="
        + solutionId.getUrn()
        + "&deployed=false");
    when(this.mockRestTemplate.getForObject(url, TimerId[].class)).thenReturn(timerIds);
    final Set<TimerId> result = this.connector.listRevokedTimers(solutionId);
    Mockito.verify(this.mockRestTemplate).getForObject(url, TimerId[].class);
    Assertions.assertEquals(Sets.newHashSet(timerIds), result);
  }

  @Test
  public void whenListRevokedTimersThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final SolutionId solutionId = new SolutionId("urn:solution:demo");
      final URI url = URI.create(Constants.BASE_URL
          + TIMER_IDS_PREFIX
          + "?solution-id="
          + solutionId.getUrn()
          + "&deployed=false");
      when(this.mockRestTemplate.getForObject(url, TimerId[].class)).thenThrow(REST_CLIENT_EXCEPTION);
      this.connector.listRevokedTimers(solutionId);
      Assertions.fail();
    });
  }

  @Test
  public void whenExistsPdp_thenReturnTrue() throws IOException, InvalidEntityException {
    final String url = Constants.BASE_URL + COMPONENT_PREFIX + "/pdp/" + PDP_COMPONENT_ID;
    final boolean result = this.connector.pdpExists(PDP_COMPONENT_ID);
    Mockito.verify(this.mockRestTemplate).headForHeaders(url);
    Assertions.assertTrue(result);
  }

  @Test
  public void whenExistsPdpThrowsHttpServerErrorExceptionNotFound_thenReturnFalse()
      throws IOException, InvalidEntityException {
    final String url = Constants.BASE_URL + COMPONENT_PREFIX + "/pdp/" + PDP_COMPONENT_ID;
    Mockito.when(this.mockRestTemplate.headForHeaders(url))
        .thenThrow(HTTP_SERVER_ERROR_EXCEPTION_NOT_FOUND);
    final boolean result = this.connector.pdpExists(PDP_COMPONENT_ID);
    Mockito.verify(this.mockRestTemplate).headForHeaders(url);
    Assertions.assertFalse(result);
  }

  @Test
  public void whenExistsPdpThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException {
    assertThrows(IOException.class, () -> {
      final String url = Constants.BASE_URL + COMPONENT_PREFIX + "/pdp/" + PDP_COMPONENT_ID;
      Mockito.doThrow(REST_CLIENT_EXCEPTION).when(this.mockRestTemplate).headForHeaders(url);
      this.connector.pdpExists(PDP_COMPONENT_ID);
      Assertions.fail();
    });
  }

  @Test
  public void whenExistsPep_thenOk() throws IOException, InvalidEntityException {
    final String url = Constants.BASE_URL + COMPONENT_PREFIX + "/pep/" + PEP_COMPONENT_ID;
    final boolean result = this.connector.pepExists(PEP_COMPONENT_ID);
    Mockito.verify(this.mockRestTemplate).headForHeaders(url);
    Assertions.assertTrue(result);
  }

  @Test
  public void whenExistsPepThrowsHttpServerErrorExceptionNotFound_thenReturnFalse()
      throws IOException, InvalidEntityException {
    final String url = Constants.BASE_URL + COMPONENT_PREFIX + "/pep/" + PEP_COMPONENT_ID;
    Mockito.doThrow(HTTP_SERVER_ERROR_EXCEPTION_NOT_FOUND).when(this.mockRestTemplate)
        .headForHeaders(url);
    final boolean result = this.connector.pepExists(PEP_COMPONENT_ID);
    Mockito.verify(this.mockRestTemplate).headForHeaders(url);
    Assertions.assertFalse(result);
  }

  @Test
  public void whenExistsPepThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException {
    assertThrows(IOException.class, () -> {
      final String url = Constants.BASE_URL + COMPONENT_PREFIX + "/pep/" + PEP_COMPONENT_ID;
      Mockito.doThrow(REST_CLIENT_EXCEPTION).when(this.mockRestTemplate).headForHeaders(url);
      this.connector.pepExists(PEP_COMPONENT_ID);
      Assertions.fail();
    });
  }

  @Test
  public void whenExistsPip_thenReturnTrue() throws IOException, InvalidEntityException {
    final String url = Constants.BASE_URL + COMPONENT_PREFIX + "/pip/" + PIP_COMPONENT_ID;
    final boolean result = this.connector.pipExists(PIP_COMPONENT_ID);
    Mockito.verify(this.mockRestTemplate).headForHeaders(url);
    Assertions.assertTrue(result);
  }

  @Test
  public void whenExistsPipThroswHttpServerErrorExceptionNotFound_thenReturnFalse()
      throws IOException, InvalidEntityException {
    final String url = Constants.BASE_URL + COMPONENT_PREFIX + "/pip/" + PIP_COMPONENT_ID;
    Mockito.doThrow(HTTP_SERVER_ERROR_EXCEPTION_NOT_FOUND).when(this.mockRestTemplate)
        .headForHeaders(url);
    final boolean result = this.connector.pipExists(PIP_COMPONENT_ID);
    Mockito.verify(this.mockRestTemplate).headForHeaders(url);
    Assertions.assertFalse(result);
  }

  @Test
  public void whenExistsPipThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException {
    assertThrows(IOException.class, () -> {
      final String url = Constants.BASE_URL + COMPONENT_PREFIX + "/pip/" + PIP_COMPONENT_ID;
      Mockito.doThrow(REST_CLIENT_EXCEPTION).when(this.mockRestTemplate).headForHeaders(url);
      this.connector.pipExists(PIP_COMPONENT_ID);
      Assertions.fail();
    });
  }

  @Test
  public void whenExistsPxp_thenReturnTrue() throws IOException, InvalidEntityException {
    final String url = Constants.BASE_URL + COMPONENT_PREFIX + "/pxp/" + PXP_COMPONENT_ID;
    final boolean result = this.connector.pxpExists(PXP_COMPONENT_ID);
    Mockito.verify(this.mockRestTemplate).headForHeaders(url);
    Assertions.assertTrue(result);
  }

  @Test
  public void whenExistsPxpThrowsHttpServerErrorExceptionNotFound_thenReturnFalse()
      throws IOException, InvalidEntityException {
    final String url = Constants.BASE_URL + COMPONENT_PREFIX + "/pxp/" + PXP_COMPONENT_ID;
    Mockito.doThrow(HTTP_SERVER_ERROR_EXCEPTION_NOT_FOUND).when(this.mockRestTemplate)
        .headForHeaders(url);
    final boolean result = this.connector.pxpExists(PXP_COMPONENT_ID);
    Mockito.verify(this.mockRestTemplate).headForHeaders(url);
    Assertions.assertFalse(result);
  }

  @Test
  public void whenExistsPxpThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException {
    assertThrows(IOException.class, () -> {
      final String url = Constants.BASE_URL + COMPONENT_PREFIX + "/pxp/" + PXP_COMPONENT_ID;
      Mockito.doThrow(REST_CLIENT_EXCEPTION).when(this.mockRestTemplate).headForHeaders(url);
      this.connector.pxpExists(PXP_COMPONENT_ID);
      Assertions.fail();
    });
  }

  @Test
  public void whenExistsPolicy_thenReturnTrue() throws IOException, InvalidEntityException {
    final String url = Constants.BASE_URL + POLICY_PREFIX + "/" + POLICY_ID.getUrn();
    final boolean result = this.connector.policyExists(POLICY_ID);
    Mockito.verify(this.mockRestTemplate).headForHeaders(url);
    Assertions.assertTrue(result);
  }

  @Test
  public void whenExistsPolicyThrowsHttpServerErrorExceptionNotFound_thenReturnFalse()
      throws IOException, InvalidEntityException {
    final String url = Constants.BASE_URL + POLICY_PREFIX + "/" + POLICY_ID.getUrn();
    Mockito.when(this.mockRestTemplate.headForHeaders(url))
        .thenThrow(HTTP_SERVER_ERROR_EXCEPTION_NOT_FOUND);
    final boolean result = this.connector.policyExists(POLICY_ID);
    Mockito.verify(this.mockRestTemplate).headForHeaders(url);
    Assertions.assertFalse(result);
  }

  @Test
  public void whenExistsPolicyThrowsRestClientException_thenThrowIOException()
      throws RestClientException, InvalidEntityException {
    assertThrows(IOException.class, () -> {
      final String url = Constants.BASE_URL + POLICY_PREFIX + "/" + POLICY_ID.getUrn();
      Mockito.when(this.mockRestTemplate.headForHeaders(url)).thenThrow(REST_CLIENT_EXCEPTION);
      this.connector.policyExists(POLICY_ID);
      Assertions.fail();
    });
  }

  @Test
  public void whenRevokePolicy_thenOk() throws UnsupportedOperationException, IOException,
      ResourceUpdateException, InvalidEntityException, NoSuchEntityException {
    final String url = Constants.BASE_URL + POLICY_PREFIX + "/" + POLICY_ID.toString() + "/deployed";
//    when(this.mockRestTemplate.patchForObject(
//        url,
//        Boolean.FALSE, HttpStatus.class)).thenReturn(HttpStatus.OK);

    this.connector.revokePolicy(POLICY_ID);

    Mockito.verify(this.mockRestTemplate).patchForObject(
        url,
        Boolean.FALSE, Void.class);
  }

  @Test
  public void whenRevokePolicyThrowsRestClientException_thenThrowIOException()
      throws ResourceUpdateException, InvalidEntityException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      when(this.mockRestTemplate.patchForObject(
          Constants.BASE_URL + POLICY_PREFIX + "/" + POLICY_ID.toString() + "/deployed",
          Boolean.FALSE, Void.class)).thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.revokePolicy(POLICY_ID);
      Assertions.fail();
    });
  }

  @Test
  public void whenRevokeTimer_thenOk()
      throws InvalidEntityException, ResourceUpdateException, NoSuchEntityException, IOException {
    final String timerId = "urn:timer:cs3:mock";

    final String url = Constants.BASE_URL + TIMER_PREFIX + "/" + timerId + "/deployed";
//    when(this.mockRestTemplate.patchForObject(
//        url, Boolean.FALSE,
//        HttpStatus.class)).thenReturn(HttpStatus.OK);

    this.connector.revokeTimer(new TimerId(timerId));

    Mockito.verify(this.mockRestTemplate).patchForObject(
        url, Boolean.FALSE, Void.class);
  }

  @Test
  public void whenRevokeTimerThrowsHttpServerErrorExceptionBadRequestthenThrowInvalidEntityException()
      throws IOException, ResourceUpdateException, NoSuchEntityException {
    assertThrows(InvalidEntityException.class, () -> {
      final String timerId = "urn:timer-id:cs.23538%%%3:mock///"; // not valid

      when(this.mockRestTemplate.patchForObject(
          Constants.BASE_URL + TIMER_PREFIX + "/" + timerId + "/deployed", Boolean.FALSE, Void.class))
          .thenThrow(HTTP_SERVER_ERROR_EXCEPTION_BAD_REQUEST);

      this.connector.revokeTimer(new TimerId(timerId));
      Assertions.fail();
    });
  }

  @Test
  public void whenRevokeTimerThrowsHttpServerErrorExceptionNotFound_thenThrowNoSuchEntityException()
      throws IOException, InvalidEntityException, ResourceUpdateException {
    assertThrows(NoSuchEntityException.class, () -> {
      final String timerId = "urn:timer:cs3:mock";

      when(this.mockRestTemplate.patchForObject(
          Constants.BASE_URL + TIMER_PREFIX + "/" + timerId + "/deployed", Boolean.FALSE, Void.class))
          .thenThrow(HTTP_SERVER_ERROR_EXCEPTION_NOT_FOUND);

      this.connector.revokeTimer(new TimerId(timerId));
      Assertions.fail();
    });
  }

  @Test
  public void whenRevokeTimerThrowsHttpServerErrorExceptionInternalServerError_thenThrowResourceUpdateException()
      throws IOException, InvalidEntityException, NoSuchEntityException {
    assertThrows(ResourceUpdateException.class, () -> {
      final String timerId = "urn:timer:cs3:mock";

      when(this.mockRestTemplate.patchForObject(
          Constants.BASE_URL + TIMER_PREFIX + "/" + timerId + "/deployed", Boolean.FALSE, Void.class))
          .thenThrow(HTTP_SERVER_ERROR_EXCEPTION_INTERNAL_SERVER_ERROR);

      this.connector.revokeTimer(new TimerId(timerId));
      Assertions.fail();
    });
  }

  @Test
  public void whenRevokeTimerThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException, ResourceUpdateException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final String timerId = "urn:timer:cs3:mock";

      when(this.mockRestTemplate.patchForObject(
          Constants.BASE_URL + TIMER_PREFIX + "/" + timerId + "/deployed", Boolean.FALSE, Void.class))
          .thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.revokeTimer(new TimerId(timerId));
      Assertions.fail();
    });
  }

  @Test
  public void whenExistsTimer_thenReturnTrue() throws InvalidEntityException, IOException {
    final String url = Constants.BASE_URL + TIMER_PREFIX + "/" + TIMER_ID.getUrn();
    final boolean result = this.connector.timerExists(TIMER_ID);
    Mockito.verify(this.mockRestTemplate).headForHeaders(url);
    Assertions.assertTrue(result);
  }

  @Test
  public void whenExistsTimerThrowsHttpServerErrorExceptionNotFound_thenReturnFalse()
      throws InvalidEntityException, IOException {
    final String url = Constants.BASE_URL + TIMER_PREFIX + "/" + TIMER_ID.getUrn();
    Mockito.when(this.mockRestTemplate.headForHeaders(url))
        .thenThrow(HTTP_SERVER_ERROR_EXCEPTION_NOT_FOUND);
    final boolean result = this.connector.timerExists(TIMER_ID);
    Mockito.verify(this.mockRestTemplate).headForHeaders(url);
    Assertions.assertFalse(result);
  }

  @Test
  public void whenExistsTimerThrowsHttpServerErrorExceptionBadRequest_thenThrowInvalidEntityException()
      throws IOException {
    assertThrows(InvalidEntityException.class, () -> {
      final String url = Constants.BASE_URL + TIMER_PREFIX + "/" + TIMER_ID.getUrn();
      Mockito.when(this.mockRestTemplate.headForHeaders(url))
          .thenThrow(HTTP_SERVER_ERROR_EXCEPTION_BAD_REQUEST);
      this.connector.timerExists(TIMER_ID);
      Assertions.fail();
    });
  }

  @Test
  public void whenExistsTimerThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException {
    assertThrows(IOException.class, () -> {
      final String url = Constants.BASE_URL + TIMER_PREFIX + "/" + TIMER_ID.getUrn();
      Mockito.when(this.mockRestTemplate.headForHeaders(url)).thenThrow(REST_CLIENT_EXCEPTION);
      this.connector.timerExists(TIMER_ID);
      Assertions.fail();
    });
  }

  @Test
  public void whenUpdatePolicy_thenReturnPolicyId()
      throws IOException, InvalidEntityException, ResourceUpdateException, NoSuchEntityException {
    final String url = Constants.BASE_URL + POLICY_PREFIX + "/" + POLICY_ID.getUrn();
    final PolicyId result = this.connector.updatePolicy(this.POLICY);
    Mockito.verify(this.mockRestTemplate).put(url, this.POLICY);
    Assertions.assertEquals(POLICY_ID, result);
  }

  @Test
  public void whenUpdatePolicyThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException, ResourceUpdateException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final String url = Constants.BASE_URL + POLICY_PREFIX + "/" + POLICY_ID.getUrn();
      Mockito.doThrow(REST_CLIENT_EXCEPTION).when(this.mockRestTemplate).put(url, this.POLICY);
      this.connector.updatePolicy(this.POLICY);
      Assertions.fail();
    });
  }

  @Test
  public void whenUpdatePdp_thenReturnPdpComponentId()
      throws IOException, InvalidEntityException, ResourceUpdateException, NoSuchEntityException {
    final String url = Constants.BASE_URL + COMPONENT_PREFIX + "/pdp";
    final ComponentId result = this.connector.updatePdp(this.PDP_COMPONENT);
    Mockito.verify(this.mockRestTemplate).put(url, this.PDP_COMPONENT);
    Assertions.assertEquals(PDP_COMPONENT_ID, result);
  }

  @Test
  public void whenUpdatePdpThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException, ResourceUpdateException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final String url = Constants.BASE_URL + COMPONENT_PREFIX + "/pdp";
      Mockito.doThrow(REST_CLIENT_EXCEPTION).when(this.mockRestTemplate).put(url, this.PDP_COMPONENT);
      this.connector.updatePdp(this.PDP_COMPONENT);
      Assertions.fail();
    });
  }

  @Test
  public void whenUpdatePep_thenReturnPepComponentId()
      throws IOException, InvalidEntityException, ResourceUpdateException, NoSuchEntityException {
    final String url = Constants.BASE_URL + COMPONENT_PREFIX + "/pep/" + PEP_COMPONENT_ID;
    final ComponentId result = this.connector.updatePep(this.PEP_COMPONENT);
    Mockito.verify(this.mockRestTemplate).put(url, this.PEP_COMPONENT);
    Assertions.assertEquals(PEP_COMPONENT_ID, result);
  }

  @Test
  public void whenUpdatePepThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException, ResourceUpdateException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final String url = Constants.BASE_URL + COMPONENT_PREFIX + "/pep/" + PEP_COMPONENT_ID;
      Mockito.doThrow(REST_CLIENT_EXCEPTION).when(this.mockRestTemplate).put(url, this.PEP_COMPONENT);
      this.connector.updatePep(this.PEP_COMPONENT);
      Assertions.fail();
    });
  }

  @Test
  public void whenUpdatePip_thenReturnPipComponentId()
      throws IOException, ResourceUpdateException, InvalidEntityException, NoSuchEntityException {
    final String url = Constants.BASE_URL + COMPONENT_PREFIX + "/pip/" + PIP_COMPONENT_ID;
    final ComponentId result = this.connector.updatePip(this.PIP_COMPONENT);
    Mockito.verify(this.mockRestTemplate).put(url, this.PIP_COMPONENT);
    Assertions.assertEquals(PIP_COMPONENT_ID, result);
  }

  @Test
  public void whenUpdatePipThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException, ResourceUpdateException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final String url = Constants.BASE_URL + COMPONENT_PREFIX + "/pip/" + PIP_COMPONENT_ID;
      Mockito.doThrow(REST_CLIENT_EXCEPTION).when(this.mockRestTemplate).put(url, this.PIP_COMPONENT);
      this.connector.updatePip(this.PIP_COMPONENT);
      Assertions.fail();
    });
  }

  @Test
  public void whenUpdatePxp_thenReturnPxpComponentId()
      throws IOException, InvalidEntityException, ResourceUpdateException, NoSuchEntityException {
    final String url = Constants.BASE_URL + COMPONENT_PREFIX + "/pxp/" + PXP_COMPONENT_ID;
    final ComponentId result = this.connector.updatePxp(this.PXP_COMPONENT);
    Mockito.verify(this.mockRestTemplate).put(url, this.PXP_COMPONENT);
    Assertions.assertEquals(PXP_COMPONENT_ID, result);
  }

  @Test
  public void whenUpdatePxpThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException, ResourceUpdateException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final String url = Constants.BASE_URL + COMPONENT_PREFIX + "/pxp/" + PXP_COMPONENT_ID;
      Mockito.doThrow(REST_CLIENT_EXCEPTION).when(this.mockRestTemplate).put(url, this.PXP_COMPONENT);
      this.connector.updatePxp(this.PXP_COMPONENT);
      Assertions.fail();
    });
  }

  @Test
  public void whenUpdateTimer_thenReturnTimerId()
      throws IOException, InvalidEntityException, ResourceUpdateException, NoSuchEntityException {
    final String url = Constants.BASE_URL + TIMER_PREFIX + "/" + TIMER_ID;
    final TimerId result = this.connector.updateTimer(this.timer);
    Mockito.verify(this.mockRestTemplate).put(url, this.timer);
    Assertions.assertEquals(TIMER_ID, result);
  }

  @Test
  public void whenUpdateTimerThrowsRestClientException_thenThrowIOException()
      throws InvalidEntityException, ResourceUpdateException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final String url = Constants.BASE_URL + TIMER_PREFIX + "/" + TIMER_ID.getUrn();
      Mockito.doThrow(REST_CLIENT_EXCEPTION).when(this.mockRestTemplate).put(url, this.timer);
      this.connector.updateTimer(this.timer);
      Assertions.fail();
    });
  }
}
