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

package de.fraunhofer.iese.mydata.component.connector;

import de.fraunhofer.iese.mydata.component.ComponentType;
import de.fraunhofer.iese.mydata.component.information.BasicManagementServiceComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PdpComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PipComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PxpComponentInformation;
import de.fraunhofer.iese.mydata.component.interfaces.IBasicManagementService;
import de.fraunhofer.iese.mydata.component.interfaces.IManagementService;
import de.fraunhofer.iese.mydata.component.interfaces.IMyDataComponent;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyDecisionPoint;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyExecutionPoint;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyInformationPoint;

import org.jspecify.annotations.Nullable;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Helper class for establishing connections to the MYDATA components by using a URI. It uses
 * reflection to determine the type of the connector needed for a certain protocol.
 */
//TODO check thread safety
public class ConnectorFactory {

  /**
   * The logger instance.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ConnectorFactory.class);

  private static final String MYDATA_PACKAGE_NAME = "de.fraunhofer.iese.mydata";

  /**
   * Priority in increasing order. binder: Least priority as it will not work on non Android
   * devices, which have an own ConnectorFactory. amqp: AMQP is good but quite special. That's why
   * it is not preferred in comparison with https. http: HTTP/REST interfaces are not so good wrt.
   * performance. https: HTTPS/REST interfaces are not so good wrt. performance. tcp: Only available
   * for PDP rmi: Least error prone and good wrt. performance. java: Invocation in the same process
   */
  private static final List<String> connectorPriorities = Arrays.asList("binder", "amqp", "http",
      "https", "tcp", "rmi", "java");

  private final Set<String> packageNamesWhereWeWillLookForConnectorClasses;

  /**
   * Used as cache to hold all connector classes (annotated with Connector) which are available in
   * the jar.
   */
  private Set<Class<?>> connectorClasses;

  /**
   * Constructor without any additional packageNames to look for Connector classes.
   *
   * @see ConnectorFactory#ConnectorFactory(Set)
   */
  public ConnectorFactory() {
    this(Collections.emptySet());
  }

  /**
   * Constructor with the ability to specify additional packageNames where we should look for
   * Connector classes.
   *
   * @param additionalPackageNames set of package names
   */
  // TODO make use of this ctor
  public ConnectorFactory(Set<String> additionalPackageNames) {
    this.packageNamesWhereWeWillLookForConnectorClasses = new HashSet<>();
    this.packageNamesWhereWeWillLookForConnectorClasses.add(MYDATA_PACKAGE_NAME);
    this.packageNamesWhereWeWillLookForConnectorClasses.addAll(additionalPackageNames);
  }

  /**
   * Contains.
   *
   * @param  array the array
   * @param  query the query
   * @return       true, if successful
   */
  private static boolean contains(String[] array, String query) {
    for (final String element : array) {
      if (element.equals(query)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Uses reflection to lookup a connector class and instantiates it for a certain communication
   * protocol.
   *
   * @param  <T>                      the generic type
   * @param  urls                     the urls
   * @param  type                     The type of the component requested.
   * @param  preferredConnectorType   The preferred connector type (optional). If null, if the
   *                                    preferredConnectorType string is not found or if there is
   *                                    only one url, the connector will be selected according to
   *                                    the priorities list. This parameter is not case sensitive.
   * @param  protocolToAuthentication the protocol to authentication
   * @return                          A connector suitable for the specified protocol and component
   *                                  type, null in case of an error
   */
  private <T extends IMyDataComponent> T getConnector(List<URI> urls, final ComponentType type,
      final String preferredConnectorType, Map<String, Authentication> protocolToAuthentication) {
    LOG.debug("Entering getConnector");
    LOG.trace("Entering getConnector(urls={}, type={})", urls, type);

    if (urls == null || urls.isEmpty()) {
      return null;
    }

    if (urls.size() == 1) {
      return this.getConnector(urls.iterator().next(), type, null);
    }

    URI connectorURI = null;
    if (preferredConnectorType != null) {
      if (preferredConnectorType.equalsIgnoreCase("REST")) {
        connectorURI = this.getConnectorURI(urls, "https");
        if (connectorURI == null) {
          connectorURI = this.getConnectorURI(urls, "http");
        }
      } else {
        connectorURI = this.getConnectorURI(urls, preferredConnectorType);
      }
    }

    if (connectorURI == null) {
      final ArrayList<URI> sortedUrls = new ArrayList<>(urls);
      // Sort urls with respect to the defined priority
      sortedUrls.sort(new Comparator<URI>() {
        @Override
        public int compare(URI o1, URI o2) {
          final Integer prio1 = this.getPriority(o1.getScheme());
          final Integer prio2 = this.getPriority(o2.getScheme());
          return -prio1.compareTo(prio2);
        }

        private int getPriority(String protocol) {
          return connectorPriorities.indexOf(protocol);
        }
      });

      connectorURI = sortedUrls.get(0);
    }
    Authentication authenticationForConnector = null;
    if (protocolToAuthentication != null
        && protocolToAuthentication.containsKey(connectorURI.getScheme())) {
      authenticationForConnector = protocolToAuthentication.get(connectorURI.getScheme());

    }
    final IMyDataComponent componentConnector = this.getConnector(connectorURI, type,
        authenticationForConnector);
    LOG.trace("Leaving getConnector(): {}", componentConnector);
    // noinspection unchecked
    return (T) componentConnector;
  }

  /**
   * Uses reflection to lookup a connector class and instantiates it for a certain communication
   * protocol.
   *
   * @param  <T>         the generic type
   * @param  urls        the urls
   * @param  type        The type of the component requested.
   * @param  credentials the credentials
   * @return             A connector suitable for the specified protocol and component type, null in
   *                     case of an error
   */
  private <T extends IMyDataComponent> T getConnector(List<URI> urls, final ComponentType type,
      Authentication credentials) {
    LOG.info("Entering getConnector");
    LOG.trace("Entering getConnector(urls={}, type={})", urls, type);
    final String preferredConnectorType = "REST";
    if (urls == null || urls.isEmpty()) {
      return null;
    }

    if (urls.size() == 1) {
      return this.getConnector(urls.iterator().next(), type, null);
    }

    URI connectorURI = null;
    if (preferredConnectorType.equalsIgnoreCase("REST")) {
      connectorURI = this.getConnectorURI(urls, "https");
      if (connectorURI == null) {
        connectorURI = this.getConnectorURI(urls, "http");
      }
    } else {
      connectorURI = this.getConnectorURI(urls, preferredConnectorType);
    }

    if (connectorURI == null) {
      final ArrayList<URI> sortedUrls = new ArrayList<>(urls);
      // Sort urls with respect to the defined priority
      sortedUrls.sort(new Comparator<URI>() {
        @Override
        public int compare(URI o1, URI o2) {
          final Integer prio1 = this.getPriority(o1.getScheme());
          final Integer prio2 = this.getPriority(o2.getScheme());
          return -prio1.compareTo(prio2);
        }

        private int getPriority(String protocol) {
          return connectorPriorities.indexOf(protocol);
        }
      });

      connectorURI = sortedUrls.get(0);
    }

    final IMyDataComponent componentConnector = this.getConnector(connectorURI, type, credentials);
    LOG.trace("Leaving getConnector(): {}", componentConnector);
    // noinspection unchecked
    return (T) componentConnector;
  }

  /**
   * Gets the connector URI.
   *
   * @param  urls                   the urls
   * @param  preferredConnectorType the preferred connector type
   * @return                        the connector URI
   */
  private URI getConnectorURI(List<URI> urls, final String preferredConnectorType) {
    URI connectorURI = null;
    for (final URI uri : urls) {
      if (uri.getScheme().equalsIgnoreCase(preferredConnectorType)) {
        connectorURI = uri;
        break;
      }
    }
    return connectorURI;
  }

  /**
   * Uses reflection to lookup a connector class and instantiates it for a certain communication
   * protocol.
   *
   * @param  <T>            the generic type
   * @param  url            The URL used to connect to the component, like
   *                          rmi://localhost:1111/pmpRmi
   * @param  type           The type of the component requested.
   * @param  authentication the authentication
   * @return                A connector suitable for the specified protocol and component type, null
   *                        in case of an error
   */
  private <T extends IMyDataComponent> T getConnector(URI url, ComponentType type,
      @Nullable Authentication authentication) {
    LOG.trace("Entering getConnector(url={}, type={})", url, type);

    this.loadClasses();
    final String protocol = url.getScheme();
    for (final Class<?> connector : this.connectorClasses) {
      final Connector annotation = connector.getAnnotation(Connector.class);
      final boolean annotationOk = annotation.type() == type;
      final boolean protocolOk = contains(annotation.protocol(), protocol);

      if (annotationOk && protocolOk) {
        try {

          final Constructor<?> constructor = this.getConstructor(connector, authentication);
          final T result = this.createInstance(url, authentication, constructor);
          LOG.trace("Leaving getConnector(): {}", result);
          return result;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
            | InvocationTargetException | NoSuchMethodException | SecurityException e) {
          LOG.warn("Could not instantiate connector", e);
        }
      }
    }

    LOG.info("Cannot find connector for (url={}, type={})", url, type);
    return null;
  }

  /**
   * Creates a new Connector object.
   *
   * @param  <T>                       the generic type
   * @param  url                       the url
   * @param  credentials               the credentials
   * @param  constructor               the constructor
   * @return                           the t
   * @throws InstantiationException    the instantiation exception
   * @throws IllegalAccessException    the illegal access exception
   * @throws InvocationTargetException the invocation target exception
   */
  @SuppressWarnings("unchecked")
  private <T extends IMyDataComponent> T createInstance(URI url, Authentication credentials,
      Constructor<?> constructor)
      throws InstantiationException, IllegalAccessException, InvocationTargetException {
    return (T) (credentials != null ? constructor.newInstance(url, credentials)
        : constructor.newInstance(url));
  }

  /**
   * Gets the constructor.
   *
   * @param  connector             the connector
   * @param  credentials           the credentials
   * @return                       the constructor
   * @throws NoSuchMethodException the no such method exception
   */
  private Constructor<?> getConstructor(Class<?> connector, Authentication credentials)
      throws NoSuchMethodException {
    return credentials != null ? connector.getConstructor(URI.class, Authentication.class)
        : connector.getConstructor(URI.class);
  }

  /**
   * Tries to establish a connection to a PDP by using the passed url variable.
   *
   * @param  component   The Component containing the URL used to connect to the component, like
   *                       rmi://localhost:1111/pdpRmi
   * @param  credentials the protocol to authentication
   * @return             A connector suitable for the specified protocol, null in case of an error
   */
  public IPolicyDecisionPoint getPdp(PdpComponentInformation component,
      Authentication credentials) {
    return this.getConnector(component.getUrls(), ComponentType.PDP, credentials);
  }

  /**
   * Tries to establish a connection to a PDP by using the passed url variable.
   *
   * @param  component                The Component containing the URL used to connect to the
   *                                    component, like rmi://localhost:1111/pdpRmi
   * @param  preferredConnectorType   The preferred connector type (optional). If null, if the
   *                                    preferredConnectorType string is not found or if there is
   *                                    only one url, the connector will be selected according to
   *                                    the priorities list. This parameter is not case sensitive.
   * @param  protocolToAuthentication the protocol to authentication
   * @return                          A connector suitable for the specified protocol, null in case
   *                                  of an error
   */
  public IPolicyDecisionPoint getPdp(PdpComponentInformation component,
      final String preferredConnectorType, Map<String, Authentication> protocolToAuthentication) {
    return this.getConnector(component.getUrls(), ComponentType.PDP, preferredConnectorType,
        protocolToAuthentication);
  }

  /**
   * Tries to establish a connection to a PDP by using the passed url variable.
   *
   * @param  component                The Component containing the URL used to connect to the
   *                                    component, like rmi://localhost:1111/pdpRmi
   * @param  preferredConnectorType   The preferred connector type (optional). If null, if the
   *                                    preferredConnectorType string is not found or if there is
   *                                    only one url, the connector will be selected according to
   *                                    the priorities list. This parameter is not case sensitive.
   * @param  protocolToAuthentication the protocol to authentication
   * @param  activeprofiles           the activeprofiles
   * @return                          A connector suitable for the specified protocol, null in case
   *                                  of an error
   */
  public IPolicyDecisionPoint getPdp(PdpComponentInformation component,
      final String preferredConnectorType, Map<String, Authentication> protocolToAuthentication,
      String[] activeprofiles) {
    return this.getConnector(component.getUrls(), ComponentType.PDP, preferredConnectorType,
        protocolToAuthentication);
  }

  /**
   * Tries to establish a connection to a PDP by using the passed url variable.
   *
   * @param  url The URL used to connect to the component, like rmi://localhost:1111/pdpRmi
   * @return     A connector suitable for the specified protocol, null in case of an error
   */
  public IPolicyDecisionPoint getPdp(URI url) {
    return this.getConnector(url, ComponentType.PDP, null);
  }

  /**
   * Tries to establish a connection to a PDP by using the passed url variable.
   *
   * @param  url         The URL used to connect to the component, like rmi://localhost:1111/pdpRmi
   * @param  credentials API Key (Oauth)
   * @return             A connector suitable for the specified protocol, null in case of an error
   */
  public IPolicyDecisionPoint getPdp(URI url, Authentication credentials) {
    return this.getConnector(url, ComponentType.PDP, credentials);
  }

  /**
   * Tries to establish a connection to a PIP by using the passed url variable.
   *
   * @param  component              The Component containing the URL used to connect to the
   *                                  component, like rmi://localhost:1111/pipRmi
   * @param  preferredConnectorType The preferred connector type (optional). If null, if the
   *                                  preferredConnectorType string is not found or if there is only
   *                                  one url, the connector will be selected according to the
   *                                  priorities list. This parameter is not case sensitive.
   * @return                        A connector suitable for the specified protocol, null in case of
   *                                an error
   */
  public IPolicyInformationPoint getPip(PipComponentInformation component,
      final String preferredConnectorType) {
    return this.getConnector(component.getUrls(), ComponentType.PIP, preferredConnectorType, null);
  }

  /**
   * Tries to establish a connection to a PIP by using the passed url variable.
   *
   * @param  url The URL used to connect to the component, like rmi://localhost:1111/pipRmi
   * @return     A connector suitable for the specified protocol, null in case of an error
   */
  public IPolicyInformationPoint getPip(URI url) {
    return this.getConnector(url, ComponentType.PIP, null);
  }

  /**
   * Tries to establish a connection to a PMP client by using the passed url variable.
   *
   * @param  component              The Component containing the URL used to connect to the
   *                                  component, like rmi://localhost:1111/pmpRmi
   * @param  preferredConnectorType The preferred connector type (optional). If null, if the
   *                                  preferredConnectorType string is not found or if there is only
   *                                  one url, the connector will be selected according to the
   *                                  priorities list. This parameter is not case sensitive.
   * @return                        A connector suitable for the specified protocol, null in case of
   *                                an error
   */
  public IBasicManagementService getBasicManagementService(
      BasicManagementServiceComponentInformation component, final String preferredConnectorType) {
    return this.getConnector(component.getUrls(), ComponentType.PMP, preferredConnectorType, null);
  }

  /**
   * Tries to establish a connection to a PMP client by using the passed url variable.
   *
   * @param  url              The URL used to connect to the component, like
   *                            rmi://localhost:1111/pmpRmi
   * @param  oAuthCredentials Credentials to login.
   * @return                  A connector suitable for the specified protocol, null in case of an
   *                          error
   */
  public IBasicManagementService getPmpClient(URI url, final OAuthCredentials oAuthCredentials) {
    return this.getConnector(url, ComponentType.PMP, oAuthCredentials);
  }

  /**
   * Tries to establish a connection to a PMP client by using the passed url variable.
   *
   * @param  url The URL used to connect to the component, like rmi://localhost:1111/pmpRmi
   * @return     A connector suitable for the specified protocol, null in case of an error
   */
  public IBasicManagementService getPmpClient(URI url) {
    return this.getConnector(url, ComponentType.PMP, null);
  }

  /**
   * Gets the management service.
   *
   * @param  url the url
   * @return     the management service
   */
  public IManagementService getManagementService(URI url) {
    return this.getConnector(url, ComponentType.MS, null);
  }

  /**
   * Tries to establish a connection to a PMP server by using the passed url variable.
   *
   * @param  url              the url
   * @param  oAuthCredentials the o auth credentials
   * @return                  A connector suitable for the specified protocol, null in case of an
   *                          error
   */
  public IManagementService getManagementService(URI url, final OAuthCredentials oAuthCredentials) {
    return this.getConnector(url, ComponentType.MS, oAuthCredentials);
  }

  /**
   * Tries to establish a connection to a PMP server by using the passed url variable.
   *
   * @param  url The URL used to connect to the component, like rmi://localhost:1111/pmpRmi
   * @return     A connector suitable for the specified protocol, null in case of an error
   */
  public IManagementService getPmpServer(URI url) {
    return this.getConnector(url, ComponentType.MS, null);
  }

  /**
   * Tries to establish a connection to a PXP by using the passed url variable.
   *
   * @param  component              The Component containing the URL used to connect to the
   *                                  component, like rmi://localhost:1111/pxpRmi
   * @param  preferredConnectorType The preferred connector type (optional). If null, if the
   *                                  preferredConnectorType string is not found or if there is only
   *                                  one url, the connector will be selected according to the
   *                                  priorities list. This parameter is not case sensitive.
   * @return                        A connector suitable for the specified protocol, null in case of
   *                                an error
   */
  public IPolicyExecutionPoint getPxp(PxpComponentInformation component,
      final String preferredConnectorType) {
    return this.getConnector(component.getUrls(), ComponentType.PXP, preferredConnectorType, null);
  }

  /**
   * Tries to establish a connection to a PXP by using the passed url variable.
   *
   * @param  url The URL used to connect to the component, like rmi://localhost:1111/pxpRmi
   * @return     A connector suitable for the specified protocol, null in case of an error
   */
  public IPolicyExecutionPoint getPxp(URI url) {
    return this.getConnector(url, ComponentType.PXP, null);
  }

  /**
   * Load classes.
   */
  private void loadClasses() {
    if (this.connectorClasses == null || this.connectorClasses.isEmpty()) {
      synchronized (this) {
        if (this.connectorClasses == null || this.connectorClasses.isEmpty()) {
          LOG.info("Going to lookup Connector classes in the following packages: {}",
              this.packageNamesWhereWeWillLookForConnectorClasses);
          final ConfigurationBuilder config = new ConfigurationBuilder()
              .forPackages(
                  this.packageNamesWhereWeWillLookForConnectorClasses.toArray(new String[0]))
              .setScanners(Scanners.TypesAnnotated, Scanners.SubTypes);
          final Reflections reflections = new Reflections(config);
          this.connectorClasses = reflections.getTypesAnnotatedWith(Connector.class, true);
          LOG.info("Discovered connectorClasses: {}", this.connectorClasses);
        }
      }
    }
  }

}
