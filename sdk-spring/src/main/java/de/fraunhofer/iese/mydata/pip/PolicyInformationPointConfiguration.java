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

package de.fraunhofer.iese.mydata.pip;

import de.fraunhofer.iese.mydata.IMyDataEnvironment;
import de.fraunhofer.iese.mydata.autoconfiguration.MyDataConfigurationProperties;
import de.fraunhofer.iese.mydata.component.ComponentType;
import de.fraunhofer.iese.mydata.registry.ComponentServicePostProcessor;
import de.fraunhofer.iese.mydata.registry.RestExposeHelper;

import jakarta.servlet.ServletContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import org.jspecify.annotations.Nullable;

/**
 * Configuration for automatic registration of PipService at PMP.
 */
@Configuration
@ComponentScan(basePackageClasses = PolicyInformationPointConfiguration.class)
public class PolicyInformationPointConfiguration {

  /**
   * * returns spring component of ComponentServicePostProcessor to be used to discover and register
   * {@link PipService}.
   * 
   * @param  myDataEnvironment
   * @param  properties
   * @param  servletContext
   * @param  restExposeHelper
   * @return                   ComponentServicePostProcessor for ComponentType.PIP
   */
  @Bean(name = "pipPostProcessor")
  ComponentServicePostProcessor componentServicePostProcessor(IMyDataEnvironment myDataEnvironment,
      MyDataConfigurationProperties properties, @Nullable ServletContext servletContext,
      @Nullable RestExposeHelper restExposeHelper) {
    return new ComponentServicePostProcessor(ComponentType.PIP, PipService.class, myDataEnvironment,
        properties.getExternalServerUrl(),
        servletContext == null ? null : servletContext.getContextPath(),
        properties.getComponent() == null ? null : properties.getComponent().getPath(),
        restExposeHelper);
  }
}
