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

package de.fraunhofer.iese.mydata.component.interfaces;

import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.health.HealthStatus;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;

import java.io.IOException;

/**
 * Shared interface that offers basic functionality for all MYDATA components.
 */
public interface IMyDataComponent {

  /**
   * Checks whether a component is up and running.
   *
   * @return             a {@link de.fraunhofer.iese.mydata.component.health.HealthStatus} instance
   *                     describing the current health status of the component
   * @throws IOException if getHealth failed due to connection problem
   */
  HealthStatus getHealth() throws IOException;

  /**
   * Gets the unique ID of the component.
   *
   * @return             the unique ID of the component.
   * @throws IOException if getIdentifier fails due to connection problem
   */
  ComponentId getId() throws IOException;

  /**
   * Resets the component to the initial state.
   *
   * @return                       true, if the component was successfully reset, false otherwise
   * @throws IOException           if reset failed due to connection problem
   * @throws NoSuchEntityException
   */
  boolean reset() throws IOException, NoSuchEntityException;

}
