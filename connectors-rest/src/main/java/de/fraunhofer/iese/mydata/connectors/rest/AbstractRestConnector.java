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

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.connector.Authentication;
import de.fraunhofer.iese.mydata.component.connector.OAuthCredentials;
import de.fraunhofer.iese.mydata.component.health.HealthStatus;
import de.fraunhofer.iese.mydata.component.interfaces.IMyDataComponent;
import de.fraunhofer.iese.mydata.exception.ConflictingResourceException;
import de.fraunhofer.iese.mydata.exception.ForbiddenException;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.MessagingException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.exception.ResourceUpdateException;

import com.google.gson.Gson;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.routing.SystemDefaultRoutePlanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

/**
 * Contains common methods and default implementations for all component specific REST connector
 * classes.
 * <p>
 * Also includes standard implementation for {@link IMyDataComponent} interfaces
 * </p>
 */
public abstract class AbstractRestConnector implements IMyDataComponent {

  /**
   * The Constant ERROR_EXCEPTION.
   */
  protected static final String ERROR_EXCEPTION = "Exception while communication with remote";

  /**
   * The Constant ERROR_UNEXPECTED.
   */
  protected static final String ERROR_UNEXPECTED = "Unexpected response: Status ";

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(AbstractRestConnector.class);

  /**
   * The gson. Do not set transient
   */
  protected Gson gson;

  /**
   * The scheme.
   */
  protected String scheme;

  /**
   * HTTP Client Interface.
   */
  protected RestTemplate httpClient;

  /**
   * The base url.
   */
  private String baseUrl;

  /**
   * The host.
   */
  private String host;

  /**
   * The port.
   */
  private int port;

  /**
   * The name.
   */
  private String name;

  /**
   * Credentials used for login with oauth.
   */
  protected OAuthCredentials credentials;

  /**
   * Default constructor using URI.
   *
   * @param uri the URL of the remote component
   */
  public AbstractRestConnector(URI uri) {
    this(uri, null);
  }

  /**
   * Constructor used with credentials for Oauth
   *
   * @param uri         URI to connect to.
   * @param credentials Credentials to login.
   */
  public AbstractRestConnector(URI uri, Authentication credentials) {
    if (credentials != null && !(credentials instanceof OAuthCredentials)) {
      throw new IllegalArgumentException("credentials must be of type OauthCredentials");
    }
    this.credentials = (OAuthCredentials) credentials;
    if (uri == null) {
      LOG.warn("URL must not be null");
      throw new IllegalArgumentException("URL must not be null");
    }
    this.initialize(uri.getScheme(), uri.getHost(), uri.getPort(), uri.getPath());
    final String s = uri.toASCIIString();
    this.setBaseUrl(s);
  }

  /**
   * Default constructor using explicit parts.
   *
   * @param scheme the scheme (http/https)
   * @param host   the remote host name
   * @param port   the remote port
   * @param name   the path
   */
  public AbstractRestConnector(String scheme, String host, int port, String name) {
    this.initialize(scheme, host, port, name);
    this.setBaseUrl(this.scheme + "://" + this.host + ":" + this.port + "/" + this.name);
  }

  /**
   * Initializes the Connector.
   *
   * @param scheme Scheme of the Connector.
   * @param host   Host to connect.
   * @param port   Port of the system.
   * @param name   Application sub part.
   */
  protected void initialize(String scheme, String host, int port, String name) {
    if (!"http".equals(scheme) && !"https".equals(scheme)) {
      LOG.warn("Scheme {} not supported.", scheme);
      throw new IllegalArgumentException("Scheme " + scheme + " not supported");
    }

    if (host == null) {
      LOG.warn("Host must not be null");
      throw new IllegalArgumentException("Host must not be null");
    }

    this.scheme = scheme;
    this.host = host;
    this.port = (port > 0) ? port : 80;
    this.gson = MyDataEntity.getGson();
    if (name != null && name.length() > 0) {
      if (name.startsWith("/")) {
        this.name = name.substring(1);
      } else {
        this.name = name;
      }
    } else {
      this.name = "";
    }
    HttpClientBuilder httpClientBuilder;

    httpClientBuilder = this.createHttpClientBuilder();

    final StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter(
        StandardCharsets.UTF_8);

    final GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter(
        this.gson);

    final HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(
        httpClientBuilder.build());
    final RestTemplate restTemplate = new RestTemplate();
    restTemplate.setRequestFactory(factory);

    if (this.credentials != null) {
      throw new IllegalArgumentException("Credentials are not supported");
    }

    this.httpClient = restTemplate;
    this.httpClient
        .setMessageConverters(Arrays.asList(stringHttpMessageConverter, gsonHttpMessageConverter));

  }

  /**
   * Creates a {@link HttpClientBuilder} in a way, that a http-proxy can be used. The code is
   * adapted from a former version of this class, and isolated in a method, for not creating an
   * http-client-instance for every request, like before.
   *
   * @return the http client builder uses for http requests
   */
  protected HttpClientBuilder createHttpClientBuilder() {
    // Create a PoolingHttpClientConnectionManager with custom settings
    final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
    connectionManager.setDefaultMaxPerRoute(1000); // Set the maximum number of connections per route
    connectionManager.setMaxTotal(2000); // Set the maximum total connections

    final SystemDefaultRoutePlanner routePlanner = new SystemDefaultRoutePlanner(
        new MyProxySelector());

    return HttpClients.custom().setRoutePlanner(routePlanner)
        .setConnectionManager(connectionManager);
  }

  protected static String addQueryParameters(String url, Map<String, Object> queryParams) {
    final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
    for (final Map.Entry<String, Object> entry : queryParams.entrySet()) {
      builder.queryParam(entry.getKey(), entry.getValue());
    }
    return builder.toUriString();
  }

  /**
   * org.apache.http.HttpHost#getPort() Method which executes the /reset web service as GET request.
   *
   * @return             TRUE in case of a successful reset, FALSE otherwise
   * @throws IOException In case of an error.
   */
  @Override
  public boolean reset() throws IOException {
    try {
      return this.httpClient.getForObject(this.getBaseUrl() + "reset", boolean.class);
    } catch (final Exception e) {
      LOG.warn("Cannot reset Component", e);
      return false;
    }
  }

  /**
   * Method to request the componentId of a component (executes /componentId as GET request).
   *
   * @return             Id of the component as {@link ComponentId} object.
   * @throws IOException In case of an error.
   */
  @Override
  public ComponentId getId() throws IOException {
    try {
      return this.httpClient.getForObject(this.getBaseUrl() + "component-id", ComponentId.class);
    } catch (final Exception e) {
      throw new IOException(e);
    }
  }

  /**
   * Customizes the toString() method in a way so it returns "classname: baseUrl".
   *
   * @return the string
   */
  @Override
  public String toString() {
    return this.getClass().getSimpleName() + ": " + this.baseUrl;
  }

  /**
   * Gets the base url.
   *
   * @return the base url
   */
  public String getBaseUrl() {
    return this.baseUrl;
  }

  /**
   * Sets the base url.
   *
   * @param baseUrl the new base url
   */
  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
  }

  @Override
  public HealthStatus getHealth() throws IOException {
    try {
      return this.httpClient.getForObject(this.getBaseUrl() + "health", HealthStatus.class);
    } catch (final Exception e) {
      throw new IOException(e);
    }
  }

  protected void handleAndRethrowNoSuchEntityException(RestClientException httpException)
      throws NoSuchEntityException {
    if (!(httpException instanceof HttpStatusCodeException)) {
      return;
    }
    if (((HttpStatusCodeException) httpException).getStatusCode() == HttpStatus.NOT_FOUND) {
      throw new NoSuchEntityException(
          ((HttpStatusCodeException) httpException).getResponseBodyAsString());
    }
  }

  protected void handleAndRethrowInvalidEntityException(RestClientException httpException)
      throws InvalidEntityException {
    if (!(httpException instanceof HttpStatusCodeException)) {
      return;
    }
    if (((HttpStatusCodeException) httpException).getStatusCode() == HttpStatus.BAD_REQUEST) {
      throw new InvalidEntityException(
          ((HttpStatusCodeException) httpException).getResponseBodyAsString());
    }
  }

  protected void handleAndRethrowResourceUpdateException(RestClientException httpException)
      throws ResourceUpdateException {
    if (!(httpException instanceof HttpStatusCodeException)) {
      return;
    }
    if (((HttpStatusCodeException) httpException)
        .getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
      throw new ResourceUpdateException(
          ((HttpStatusCodeException) httpException).getResponseBodyAsString());
    }
  }

  protected void handleAndRethrowConflictingResourceException(RestClientException httpException)
      throws ConflictingResourceException {
    if (!(httpException instanceof HttpStatusCodeException)) {
      return;
    }
    if (((HttpStatusCodeException) httpException).getStatusCode() == HttpStatus.CONFLICT) {
      throw new ConflictingResourceException(
          ((HttpStatusCodeException) httpException).getResponseBodyAsString());
    }
  }

  protected void handleAndRethrowForbiddenException(RestClientException httpException)
      throws ForbiddenException {
    if (!(httpException instanceof HttpStatusCodeException)) {
      return;
    }
    if (((HttpStatusCodeException) httpException).getStatusCode() == HttpStatus.FORBIDDEN) {
      throw new ForbiddenException(
          ((HttpStatusCodeException) httpException).getResponseBodyAsString());
    }
  }

  protected void handleAndRethrowMessagingException(RestClientException httpException)
      throws MessagingException {
    if (!(httpException instanceof HttpStatusCodeException)) {
      return;
    }
    if (((HttpStatusCodeException) httpException)
        .getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
      throw new MessagingException(
          ((HttpStatusCodeException) httpException).getResponseBodyAsString());
    }
  }

}
