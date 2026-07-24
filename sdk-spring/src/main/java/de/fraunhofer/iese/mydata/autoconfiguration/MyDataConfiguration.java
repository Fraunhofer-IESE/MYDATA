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

package de.fraunhofer.iese.mydata.autoconfiguration;

import de.fraunhofer.iese.mydata.IMyDataEnvironment;
import de.fraunhofer.iese.mydata.MyDataEnvironmentManager;
import de.fraunhofer.iese.mydata.OperationalMode;
import de.fraunhofer.iese.mydata.client.ClientId;
import de.fraunhofer.iese.mydata.component.connector.OAuthCredentials;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyEnforcementPoint;
import de.fraunhofer.iese.mydata.exception.InitializationException;
import de.fraunhofer.iese.mydata.internal.IMyDataEnvironmentInitializer;
import de.fraunhofer.iese.mydata.policy.event.history.IEventRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

import org.jspecify.annotations.Nullable;

@Configuration
public class MyDataConfiguration {
  private static final Logger LOG = LoggerFactory.getLogger(MyDataConfiguration.class);

  private final MyDataConfigurationProperties properties;

  @Nullable
  private final IEventRepository eventRepository;

  public MyDataConfiguration(MyDataConfigurationProperties properties,
      @Nullable IEventRepository eventRepository) {
    this.properties = properties;
    LOG.debug("MyData-Configuration: {}", properties);
    this.eventRepository = eventRepository;
  }

  @Bean(name = "myDataEnvironment")
  @ConditionalOnMissingBean
  IMyDataEnvironment getMyDataEnvironment() throws InitializationException {
    final IMyDataEnvironmentInitializer defaultBuilder = MyDataEnvironmentManager
        .constructDefaultEnvironment();
    final IMyDataEnvironment myDataEnvironment;
    // TODO check configuration completeness based on the selected op-mode...
    if (OperationalMode.LOCAL == this.properties.getOperationalMode()) {
      myDataEnvironment = defaultBuilder.initializeLocal(this.properties.getSolution(),
          this.properties.getTimezone(), this.properties.getPdp().getNumThreads(),
          this.properties.getPdp().isEnableWhitelistMode(), this.eventRepository);
    } else if (OperationalMode.LOCAL_WITH_FILE_SYNC == this.properties.getOperationalMode()) {
      myDataEnvironment = defaultBuilder.initializeLocalWithFileSync(this.properties.getSolution(),
          this.properties.getTimezone(), this.properties.getSync().getFileSync().getPath(),
          this.properties.getPdp().getNumThreads(),
          this.properties.getPdp().isEnableWhitelistMode(), this.eventRepository);
    } else if (OperationalMode.LOCAL_WITH_CLOUD_SYNC == this.properties.getOperationalMode()) {
      if (this.properties.getPmp().getCloud() == null) {
        throw new IllegalArgumentException("Invalid configuration, missing cloud config");
      }
      myDataEnvironment = defaultBuilder.initializeLocalWithCloudSync(this.properties.getSolution(),
          URI.create(this.properties.getPmp().getCloud().getUrl()),
          this.getAuthentication(this.properties.getPmp().getCloud().getClientId(),
              this.properties.getPmp().getCloud().getClientSecret(),
              this.properties.getPmp().getCloud().getUrl()),
          this.properties.getTimezone(),
          this.properties.getSync().getCloudSync().getCache().isEnabled(),
          this.properties.getSync().getCloudSync().getCache().getPolicyFilePath(),
          this.properties.getSync().getCloudSync().getCache().getTimerFilePath(),
          this.properties.getSync().getCloudSync().getMaxAge(),
          this.properties.getSync().getCloudSync().getSchedule(),
          this.properties.getSync().getCloudSync().isMasterClient(),
          this.properties.getPdp().getNumThreads(),
          this.properties.getPdp().isEnableWhitelistMode(), this.eventRepository);
    } else if (OperationalMode.CLOUD == this.properties.getOperationalMode()) {
      if (this.properties.getPmp().getCloud() == null) {
        throw new IllegalArgumentException("Invalid configuration, missing cloud config");
      }
      myDataEnvironment = defaultBuilder.initializeCloud(this.properties.getSolution(),
          URI.create(this.properties.getPmp().getCloud().getUrl()),
          this.getAuthentication(this.properties.getPmp().getCloud().getClientId(),
              this.properties.getPmp().getCloud().getClientSecret(),
              this.properties.getPmp().getCloud().getUrl()));
    } else {
      throw new IllegalArgumentException("Invalid configuration, invalid op-mode");
    }
    return myDataEnvironment;
  }

  @Bean(name = "pep")
  @ConditionalOnMissingBean
  @ConditionalOnSingleCandidate(IMyDataEnvironment.class)
  IPolicyEnforcementPoint getPep(IMyDataEnvironment myDataEnvironment) {
    return myDataEnvironment.getPep();
  }

  private OAuthCredentials getAuthentication(String cloudPmpClientId, String cloudPmpClientSecret,
      String cloudPmpUrl) {
    final ClientId clientId = new ClientId(cloudPmpClientId);
    if (cloudPmpClientSecret != null && !cloudPmpClientSecret.isEmpty() && cloudPmpUrl != null
        && !cloudPmpUrl.isEmpty()) {
      return new OAuthCredentials(clientId, cloudPmpClientSecret,
          URI.create(cloudPmpUrl + "/oauth/token"));
    }
    return null;
  }
}
