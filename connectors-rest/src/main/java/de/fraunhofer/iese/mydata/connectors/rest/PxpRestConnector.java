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
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyExecutionPoint;
import de.fraunhofer.iese.mydata.policy.decision.ExecuteAction;

import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;

import java.io.IOException;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.Optional;

/**
 * Rest based connector interface for the PXP service.
 */
@Connector(protocol = {
    "http", "https"
}, type = ComponentType.PXP)
public class PxpRestConnector extends AbstractRestConnector implements IPolicyExecutionPoint {

  /**
   * The Constant serialVersionUID.
   */
  private static final String EXECUTE = "execute";

  /**
   * Constructor of {@link #PxpRestConnector} which uses an uri string to identify the component.
   *
   * @param  uri                      Used to identify the component.
   * @throws RemoteException          in case of a communication error.
   * @throws IllegalArgumentException in case of a wrong or unknown uri.
   */
  public PxpRestConnector(String uri) throws RemoteException {
    this(URI.create(uri));
  }

  /**
   * Constructor of {@link #PxpRestConnector} which uses an uri object to identify the component.
   *
   * @param  uri             Used to identify the component.
   * @throws RemoteException in case of a communication error.
   */
  public PxpRestConnector(URI uri) throws RemoteException {
    super(uri);
  }

  /**
   * Constructor of {@link #PxpRestConnector} which uses (instead of an uri) a scheme, host, name
   * string as well as port to identify the component.
   *
   * @param scheme used scheme of the component (e.g. http). See also {@link URI#getScheme()}
   * @param host   hostname of the component. See also {@link URI#getHost()}
   * @param port   the port
   * @param name   An identifying name of the component.
   */
  public PxpRestConnector(String scheme, String host, int port, String name) {
    super(scheme, host, port, name);
  }

  /**
   * Constructor of {@link #PxpRestConnector} which uses an uri string to identify the component.
   *
   * @param  uri                      Used to identify the component.
   * @param  credentials              {@link Authentication} with client_id and secret
   * @throws RemoteException          in case of a communication error.
   * @throws IllegalArgumentException in case of a wrong or unknown uri.
   */
  public PxpRestConnector(final String uri, final Authentication credentials)
      throws RemoteException {
    this(URI.create(uri), credentials);
  }

  /**
   * Constructor of {@link #PxpRestConnector} which uses an uri object to identify the component.
   *
   * @param  uri             Used to identify the component.
   * @param  credentials     {@link Authentication} with client_id and secret
   * @throws RemoteException in case of a communication error.
   */
  public PxpRestConnector(URI uri, final Authentication credentials) throws RemoteException {
    super(uri, credentials);
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.api.component.interfaces.IPolicyExecutionPoint#
   * execute(de.fraunhofer.iese.mydata.api.policy.ExecuteAction,
   * de.fraunhofer.iese.mydata.api.policy.Event)
   */
  @Override
  public boolean execute(ExecuteAction action) throws IOException {
    try {
      final HttpEntity<ExecuteAction> requestEntity = new HttpEntity<>(action);
      return Optional.ofNullable(
          this.httpClient.postForObject(this.getBaseUrl() + EXECUTE, requestEntity, boolean.class))
          .orElse(false);
    } catch (final Exception e) {
      LoggerFactory.getLogger(this.getClass()).info("Calling PXP :" + this.getBaseUrl(), e);
      return false;
    }
  }

}
