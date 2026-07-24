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

package de.fraunhofer.iese.mydata.registry;

import de.fraunhofer.iese.mydata.IMyDataEnvironment;
import de.fraunhofer.iese.mydata.OperationalMode;
import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.ComponentType;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Collections;

/**
 * ApplicationListener that retrieves beans with the annotation
 * {@link ComponentServicePostProcessor#annotationClassOfBean} e.g. @PipService from the created
 * applicationContext to register them as components at the PMP.
 */
public class ComponentServicePostProcessor implements ApplicationListener<ContextRefreshedEvent> {
  private static final Logger LOG = LoggerFactory.getLogger(ComponentServicePostProcessor.class);

  private final ComponentType type;

  private final Class<? extends Annotation> annotationClassOfBean;

  private final IMyDataEnvironment myDataEnvironment;

  private final String pathPrefix;

  private final String serverUrl;

  private final String servletContextPath;

  @Nullable
  private final RestExposeHelper restExposeHelper;

  /**
   * @param type                  Type of the components to create.
   * @param annotationClassOfBean Annotation that each service bean has.
   * @param myDataEnvironment     The {@link IMyDataEnvironment} the components should be registered
   *                                to
   * @param serverUrl
   * @param servletContextPath
   * @param pathPrefix
   * @param restExposeHelper      the {@link RestExposeHelper} bean
   */
  public ComponentServicePostProcessor(ComponentType type,
      Class<? extends Annotation> annotationClassOfBean, IMyDataEnvironment myDataEnvironment,
      String serverUrl, String servletContextPath, String pathPrefix,
      @Nullable RestExposeHelper restExposeHelper) {
    this.type = type;
    this.annotationClassOfBean = annotationClassOfBean;
    this.myDataEnvironment = myDataEnvironment;
    this.serverUrl = serverUrl;
    this.servletContextPath = servletContextPath;
    this.pathPrefix = pathPrefix;
    this.restExposeHelper = restExposeHelper;
  }

  /**
   * Creates the CallbackURL for this component.<br/>
   * Composed with scheme://server:port/servletContextPath/<b>pathPrefix</b>/componentType/path.
   */
  private String createFullUrl(String path) {
    final StringBuilder toReturn = new StringBuilder();
    toReturn.append(this.serverUrl);
    this.addServletContextPath(toReturn);
    this.addPathPrefix(toReturn);
    this.addComponentTypeSpecificPath(toReturn);
    this.addServicePath(toReturn, path);
    return toReturn.toString();
  }

  private void addServicePath(StringBuilder toReturn, String path) {
    if (!(toReturn.toString().endsWith("/") || path != null && path.startsWith("/"))) {
      toReturn.append("/");
    }
    if (path != null) {
      toReturn.append(path);
    }
  }

  private void addServletContextPath(StringBuilder toReturn) {
    if (this.servletContextPath != null && !this.servletContextPath.isEmpty()
        && !(this.serverUrl.endsWith("/") || this.servletContextPath.startsWith("/"))) {
      toReturn.append("/");
    }
    if (this.servletContextPath != null) {
      toReturn.append(this.servletContextPath);
    }
  }

  private void addPathPrefix(StringBuilder toReturn) {
    if (this.pathPrefix != null && !this.pathPrefix.isEmpty()
        && !(this.serverUrl.endsWith("/") || this.pathPrefix.startsWith("/"))) {
      toReturn.append("/");
    }
    if (this.pathPrefix != null) {
      toReturn.append(this.pathPrefix);
    }
  }

  private void addComponentTypeSpecificPath(StringBuilder toReturn) {
    if (!toReturn.toString().endsWith("/")) {
      toReturn.append("/");
    }

    if (this.type == ComponentType.PIP) {
      toReturn.append("pip/");
    } else if (this.type == ComponentType.PXP) {
      toReturn.append("pxp/");
    }
  }

  /**
   * Listening to the event that is generated after context is refreshed (after creation).
   *
   * @param event Contains the applicationContext that should be discovered.
   * @see         ApplicationListener#onApplicationEvent(ApplicationEvent)
   */
  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {

    final String[] beanNamesForAnnotation = event.getApplicationContext()
        .getBeanNamesForAnnotation(this.annotationClassOfBean);
    this.discoverBeans(event, beanNamesForAnnotation);
  }

  private void discoverBeans(ContextRefreshedEvent event, String[] beanNamesForAnnotation) {
    for (final String name : beanNamesForAnnotation) {
      this.discoverBean(event, name);
    }
  }

  private void discoverBean(ContextRefreshedEvent event, String name) {
    try {
      final Object o = event.getApplicationContext().getBean(name);
      final Class<?> aClass = o.getClass();
      final Annotation a = aClass.getAnnotation(this.annotationClassOfBean);
      final String componentName = (String) a.getClass().getMethod("componentName").invoke(a);
      final ComponentId componentId = this.type
          .getComponentId(this.myDataEnvironment.getSolutionId(), componentName);
      LOG.info("Registering {}: {}", this.type, componentId.getUrn());
      if (OperationalMode.CLOUD == this.myDataEnvironment.getOperationalMode()) {
        if (this.restExposeHelper == null || this.serverUrl == null) {
          throw new IllegalStateException(
              "Cloud-mode configured but there are some properties or dependencies missing. Check the configuration.");
        }

        String path = (String) a.getClass().getMethod("path").invoke(a);
        if (StringUtils.isBlank(path)) {
          path = componentId.getUrn(); // default to componentId
        }

        final URI fullUrl = URI.create(this.createFullUrl(path));
        if (ComponentType.PIP == this.type) {
          this.restExposeHelper.registerPipComponentIdToPath(path, componentId);
          this.myDataEnvironment.registerManagedPip(componentId, o,
              Collections.singletonList(fullUrl));
        } else if (ComponentType.PXP == this.type) {
          this.restExposeHelper.registerPxpComponentIdToPath(path, componentId);
          this.myDataEnvironment.registerManagedPxp(componentId, o,
              Collections.singletonList(fullUrl));
        }
      } else {
        if (ComponentType.PIP == this.type) {
          this.myDataEnvironment.registerLocalPip(componentId, o);
        } else if (ComponentType.PXP == this.type) {
          this.myDataEnvironment.registerLocalPxp(componentId, o);
        }
      }
      LOG.info("Registered {}: {}", this.type, componentId.getUrn());
    } catch (final Exception e) {
      LOG.warn("Cannot create and register Component for Bean {}", name, e);
    }
  }

}
