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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyProxySelector extends ProxySelector {
  private static final Logger logger = LoggerFactory.getLogger(MyProxySelector.class);

  private final ProxySelector defaultproxySelector = ProxySelector.getDefault();

  private final List<Proxy> noProxyProxies = new ArrayList<>();

  private final List<Proxy> httpProxyProxies = new ArrayList<>();

  private final List<Proxy> httpsProxyProxies = new ArrayList<>();

  private final List<String> noProxy = new ArrayList<>();

  public MyProxySelector() {

    this.noProxyProxies.add(Proxy.NO_PROXY);

    final String httpProxyEnv = System.getenv("http_proxy");

    if (httpProxyEnv != null) {
      try {
        final URL proxyUrl = new URI(httpProxyEnv).toURL();
        this.httpProxyProxies.add(new Proxy(Proxy.Type.HTTP,
            new InetSocketAddress(proxyUrl.getHost(), proxyUrl.getPort())));
        logger.debug("using http_proxy {}:{}", proxyUrl.getHost(), proxyUrl.getPort());
      } catch (final MalformedURLException | URISyntaxException e) {
        logger.debug("Error while parsing http_proxy. Using no proxy!", e);
      }
    }

    final String httpsProxyEnv = System.getenv("https_proxy");
    if (httpsProxyEnv != null) {
      try {
        final URL proxyUrl = new URI(httpsProxyEnv).toURL();
        this.httpsProxyProxies.add(new Proxy(Proxy.Type.HTTP,
            new InetSocketAddress(proxyUrl.getHost(), proxyUrl.getPort())));
        logger.debug("using https_proxy {}:{}", proxyUrl.getHost(), proxyUrl.getPort());
      } catch (final MalformedURLException | URISyntaxException e) {
        logger.debug("Error while parsing https_proxy. Using no proxy!", e);
      }
    }

    final String noProxyEnv = System.getenv("no_proxy");
    if (noProxyEnv != null) {
      final List<String> noProxyStrings = Arrays.asList(noProxyEnv.split(","));
      this.noProxy.addAll(noProxyStrings);
      logger.debug("using no_proxy {}", noProxyStrings);
    }

  }

  @Override
  public List<Proxy> select(URI uri) {
    if (this.noProxy.stream()
        .anyMatch(s -> uri == null || uri.getHost().toLowerCase().endsWith(s))) {
      return this.noProxyProxies;
    }

    if (uri.getScheme().equalsIgnoreCase("https")) {
      return this.httpsProxyProxies;
    }

    if (uri.getScheme().equalsIgnoreCase("http")) {
      return this.httpProxyProxies;
    }

    if (this.defaultproxySelector != null) {
      return this.defaultproxySelector.select(uri);
    }

    return this.noProxyProxies;
  }

  @Override
  public void connectFailed(URI arg0, SocketAddress arg1, IOException arg2) {
    logger.debug("Connect to {} ({}) failed", arg0, arg1, arg2);
  }
}
