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

import de.fraunhofer.iese.mydata.client.ClientId;
import de.fraunhofer.iese.mydata.component.connector.ConnectorFactory;
import de.fraunhofer.iese.mydata.component.connector.OAuthCredentials;
import de.fraunhofer.iese.mydata.component.information.PipComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PxpComponentInformation;
import de.fraunhofer.iese.mydata.component.information.method.MethodInterfaceDescription;
import de.fraunhofer.iese.mydata.component.interfaces.IBasicManagementService;
import de.fraunhofer.iese.mydata.component.interfaces.IMyDataComponent;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyExecutionPoint;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyInformationPoint;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.pdp.PolicyDecisionPoint;
import de.fraunhofer.iese.mydata.pdp.interfaces.IConnectorCache;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Implementation of a basic singleton connector cache. The connector objects are stored within a
 * map, identified by their {@link MethodInterfaceDescription}.
 */
public class ConnectorCache implements IConnectorCache {

  /**
   * The Constant pipCacheLock.
   */
  private final ReadWriteLock pipCacheLock;

  /**
   * The Constant pxpCacheLock.
   */
  private final ReadWriteLock pxpCacheLock;

  /**
   * The Constant pmpCacheLock.
   */
  private final ReadWriteLock pmpCacheLock;

  /**
   * The Constant INITIAL_CAPACITY.
   */
  private static final int INITIAL_CAPACITY = 5;

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ConnectorCache.class);

  /**
   * OAUTH credentials object
   */
  private OAuthCredentials oAuthCredentials;

  /**
   * The pip cache.
   */
  private final Map<MethodInterfaceDescription, IMyDataComponent> pipCache;

  /**
   * The pxp cache.
   */
  private final Map<MethodInterfaceDescription, IMyDataComponent> pxpCache;

  /**
   * The pmp cache.
   */
  private IBasicManagementService pmpCache;

  private final ConnectorFactory connectorFactory;

  /**
   * Instantiates a new connector cache.
   *
   * @param connectorFactory
   */
  public ConnectorCache(ConnectorFactory connectorFactory) {
    this.pipCacheLock = new ReentrantReadWriteLock(false);
    this.pxpCacheLock = new ReentrantReadWriteLock(false);
    this.pmpCacheLock = new ReentrantReadWriteLock(false);
    this.pipCache = new HashMap<>(ConnectorCache.INITIAL_CAPACITY);
    this.pxpCache = new HashMap<>(ConnectorCache.INITIAL_CAPACITY);
    this.pmpCache = null;
    this.connectorFactory = Objects.requireNonNull(connectorFactory);
  }

  /**
   * Sets the oauth values required for PMP connection
   *
   * @param clientId
   * @param clientSecret
   * @param tokenURL
   */
  public void setOAuthCredentials(String clientId, String clientSecret, String tokenURL) {
    LOG.trace("Set oAuthCredentials");
    if (clientId == null || clientSecret.trim().isEmpty() || tokenURL == null
        || tokenURL.trim().isEmpty()) {
      throw new IllegalArgumentException("Missing or malformed URL, clientId or client secret");
    }
    this.oAuthCredentials = new OAuthCredentials(new ClientId(clientId), clientSecret,
        URI.create(tokenURL));
  }

  /*
   * (non-Javadoc)
   * @see IConnectorCache# clearPipCache()
   */
  @Override
  public void clearPipCache() {
    this.pipCacheLock.writeLock().lock();
    try {
      this.pipCache.clear();
    } finally {
      this.pipCacheLock.writeLock().unlock();
    }
  }

  /*
   * (non-Javadoc)
   * @see IConnectorCache# clearPxpCache()
   */
  @Override
  public void clearPxpCache() {
    this.pxpCacheLock.writeLock().lock();
    try {
      this.pxpCache.clear();
    } finally {
      this.pxpCacheLock.writeLock().unlock();
    }
  }

  /*
   * (non-Javadoc)
   * @see IConnectorCache# clearPMPCache()
   */
  @Override
  public void clearPMPCache() {
    this.pmpCacheLock.writeLock().lock();
    try {
      this.pmpCache = null;
    } finally {
      this.pmpCacheLock.writeLock().unlock();
    }
  }

  /*
   * (non-Javadoc)
   * @see IConnectorCache# getPipConnectionFromCache(de.fraunhofer.iese.mydata.api.component.
   * description.MethodInterfaceDescription)
   */
  @Override
  public IPolicyInformationPoint getPipConnectionFromCache(MethodInterfaceDescription pipQuery,
      SolutionId solutionId) {
    try {
      return (IPolicyInformationPoint) this.getConnectionFromCache(pipQuery, this.pipCacheLock,
          this.pipCache, solutionId);
    } catch (final InvalidEntityException | NoSuchEntityException e) {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * @see IConnectorCache# getPxpConnectionFromCache(de.fraunhofer.iese.mydata.api.component.
   * description.MethodInterfaceDescription)
   */
  @Override
  public IPolicyExecutionPoint getPxpConnectionFromCache(MethodInterfaceDescription pxpQuery,
      SolutionId solutionId) {
    try {
      return (IPolicyExecutionPoint) this.getConnectionFromCache(pxpQuery, this.pxpCacheLock,
          this.pxpCache, solutionId);
    } catch (final InvalidEntityException | NoSuchEntityException e) {
      return null;
    }
  }

  /**
   * Generic method to fetch a connection from the cache. In case of a cache miss, the component is
   * looked up via the PMP.
   *
   * @param  query                  The components {@link MethodInterfaceDescription}
   * @param  cacheLock              A lock object to prevent concurrent modifications.
   * @param  cache                  The cache map.
   * @param  solutionId
   * @return                        NULL in case of an error (e.g. lookup failed), the component
   *                                otherwise.
   * @throws InvalidEntityException
   */
  private IMyDataComponent getConnectionFromCache(MethodInterfaceDescription query,
      ReadWriteLock cacheLock, Map<MethodInterfaceDescription, IMyDataComponent> cache,
      SolutionId solutionId) throws InvalidEntityException, NoSuchEntityException {
    if (null == query) {
      return null;
    }

    IMyDataComponent cachedConnection;
    cacheLock.readLock().lock();
    try {
      cachedConnection = cache.get(query);
    } finally {
      cacheLock.readLock().unlock();
    }

    if (null == cachedConnection) {
      cachedConnection = this.lookupComponent(query, cache, cacheLock, solutionId);

      if (null != cachedConnection) {
        cacheLock.writeLock().lock();
        try {
          cache.put(query, cachedConnection);
        } finally {
          cacheLock.writeLock().unlock();
        }
      }
    }

    return cachedConnection;
  }

  private IMyDataComponent lookupComponent(MethodInterfaceDescription query,
      Map<MethodInterfaceDescription, IMyDataComponent> cache, ReadWriteLock cacheLock,
      SolutionId solutionId) throws InvalidEntityException, NoSuchEntityException {
    final IMyDataComponent cacheResult = cache.get(query);
    if (null != cacheResult) {
      return cacheResult;
    }
    if (cache == this.pipCache) {
      return this.lookupPipComponent(query, cacheLock, solutionId);
    } else {
      return this.lookupPxpComponent(query, cacheLock, solutionId);
    }
  }

  /**
   * Method to lookup a component via the PMP.
   *
   * @param  query                  The components {@link MethodInterfaceDescription}
   * @param  cacheLock              A lock object to prevent concurrent modifications.
   * @param  solutionId
   * @return                        NULL in case of an error (e.g. lookup failed), the component
   *                                otherwise.
   * @throws InvalidEntityException
   */
  private IMyDataComponent lookupPipComponent(MethodInterfaceDescription query,
      ReadWriteLock cacheLock, SolutionId solutionId)
      throws InvalidEntityException, NoSuchEntityException {
    cacheLock.writeLock().lock();
    // cache == this.pipCache
    try {

      try {
        final IBasicManagementService pmpFromChache = this.getPmpConnectionFromCache();
        Set<PipComponentInformation> compList = null;
        if (pmpFromChache != null) {
          compList = pmpFromChache.lookupPip(solutionId, query);
        }

        if (compList == null || compList.isEmpty()) {
          LOG.warn("No component with given URN available! Returning null.");
          return null;
        }
        LOG.debug("Found [{}] components. Choosing first one.", compList.size());
        final PipComponentInformation component = compList.iterator().next();
        final List<URI> compUris = component.getUrls();

        if (null == compUris || compUris.isEmpty()) {
          LOG.warn("No component URIs available! Returning null.");
          return null;
        }

        LOG.debug("Found [{}] component URIs. Choosing first one: {}", compUris.size(),
            compUris.iterator().next().toASCIIString());
        final URI compUri = compUris.iterator().next();

        return this.connectorFactory.getPip(compUri);

      } catch (final IOException e) {
        LOG.error("Error during PIP lookup, returning default value", e);
        return null;
      }
    } finally {
      cacheLock.writeLock().unlock();
    }
  }

  /**
   * Method to lookup a component via the PMP.
   *
   * @param  query                  The components {@link MethodInterfaceDescription}
   * @param  cacheLock              A lock object to prevent concurrent modifications.
   * @param  solutionId
   * @return                        NULL in case of an error (e.g. lookup failed), the component
   *                                otherwise.
   * @throws InvalidEntityException
   */
  private IMyDataComponent lookupPxpComponent(MethodInterfaceDescription query,
      ReadWriteLock cacheLock, SolutionId solutionId)
      throws InvalidEntityException, NoSuchEntityException {
    cacheLock.writeLock().lock();

    try {

      try {
        final IBasicManagementService pmpFromChache = this.getPmpConnectionFromCache();
        Set<PxpComponentInformation> compList = null;
        if (pmpFromChache != null) {

          compList = pmpFromChache.lookupPxp(solutionId, query);
        }

        if (compList == null || compList.isEmpty()) {
          LOG.warn("No component with given URN available! Returning null.");
          return null;
        }
        LOG.debug("Found [{}] components. Choosing first one.", compList.size());
        final PxpComponentInformation component = compList.iterator().next();
        final List<URI> compUris = component.getUrls();

        if (null == compUris || compUris.isEmpty()) {
          LOG.warn("No component URIs available! Returning null.");
          return null;
        }

        LOG.debug("Found [{}] component URIs. Choosing first one: {}", compUris.size(),
            compUris.iterator().next().toASCIIString());
        final URI compUri = compUris.iterator().next();

        return this.connectorFactory.getPxp(compUri);
      } catch (final IOException e) {
        LOG.error("Error during PXP lookup, returning default value", e);
        return null;
      }
    } finally {
      cacheLock.writeLock().unlock();
    }
  }

  /*
   * (non-Javadoc)
   * @see IConnectorCache# getPMPConnectionFromCache()
   */
  @Override
  public IBasicManagementService getPmpConnectionFromCache() {
    IBasicManagementService cachedConnection;

    this.pmpCacheLock.readLock().lock();
    try {
      cachedConnection = this.pmpCache;
    } finally {
      this.pmpCacheLock.readLock().unlock();
    }

    if (null == cachedConnection) {
      cachedConnection = this.lookupPMP();

      if (null != cachedConnection) {
        this.pmpCacheLock.writeLock().lock();
        try {
          this.pmpCache = cachedConnection;
        } finally {
          this.pmpCacheLock.writeLock().unlock();
        }
      }
    }

    return cachedConnection;
  }

  /**
   * Method to lookup the PMP. It's URI will be taken from {@link PolicyDecisionPoint#getPmpURI()}.
   *
   * @return NULL in case of an error (e.g. lookup failed), the PMP component otherwise.
   */
  private IBasicManagementService lookupPMP() {
    LOG.trace("Lookup PMP: {}", PolicyDecisionPoint.getInstance().getPmpURI());
    if (this.oAuthCredentials == null) {
      return this.connectorFactory.getPmpClient(PolicyDecisionPoint.getInstance().getPmpURI());
    }
    return this.connectorFactory.getPmpClient(PolicyDecisionPoint.getInstance().getPmpURI(),
        this.oAuthCredentials);
  }
}
