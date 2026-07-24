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

package de.fraunhofer.iese.mydata.pep;

import de.fraunhofer.iese.mydata.IMyDataEnvironment;
import de.fraunhofer.iese.mydata.exception.InitializationException;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.reactive.ReactivePep;
import de.fraunhofer.iese.mydata.reactive.common.PepServiceDescription;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Objects;

/**
 * Factory for {@link ReactivePep}.
 * <p>
 * Creates a {@link ReactivePep} for an Interface that is annotated with
 * {@link PepServiceDescription}. The resulting instance (dynamic proxy) implements the Intefacee.
 * </p>
 */
@Component("pepFactory")
public class PepBeanFactory {

  private final IMyDataEnvironment myDataEnvironment;

  public PepBeanFactory(IMyDataEnvironment myDataEnvironment) {
    this.myDataEnvironment = myDataEnvironment;
  }

  /**
   * Create a {@link ReactivePep} that implements the given interfaceOfPep.
   * <p>
   * The interface must contain an annotation of type {@link PepServiceDescription}.
   * </p>
   *
   * @param  interfaceOfPep The Interface to generate a PEP for.
   * @param  <T>            Type of the Interface.
   * @return                Dynamic Prox {@link ReactivePep} that implements the given
   *                        interfaceOfPep and is able to enforce a decision at PDP by sending
   *                        events.
   */
  public <T> T createPep(Class<T> interfaceOfPep) {
    Objects.requireNonNull(interfaceOfPep);
    final String componentName;
    try {
      componentName = this.getComponentName(interfaceOfPep);
      return this.myDataEnvironment.constructAndRegisterCustomPep(componentName, interfaceOfPep);
    } catch (InvalidEntityException | InitializationException e) {
      throw new BeanCreationException(
          "Failed to construct PEP from interface " + interfaceOfPep.getCanonicalName(), e);
    }
  }

  private <T> String getComponentName(final Class<T> pepDocumentationApi)
      throws InvalidEntityException {
    final Annotation[] classAnnotations = pepDocumentationApi
        .getAnnotationsByType(PepServiceDescription.class);
    if (classAnnotations.length == 0) {
      throw new InvalidEntityException("@PepServiceDescription annotation missing");
    }
    return ((PepServiceDescription) classAnnotations[0]).componentName();
  }

}
