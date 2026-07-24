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

package de.fraunhofer.iese.mydata.internal;

import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyExecutionPoint;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyInformationPoint;
import de.fraunhofer.iese.mydata.exception.ConflictingResourceException;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;

import java.util.Optional;

/**
 * Encapsulates the management of PIP/PXP instances that not necessarily have to implement the
 * {@link de.fraunhofer.iese.mydata.component.interfaces.IPolicyInformationPoint} or
 * {@link de.fraunhofer.iese.mydata.component.interfaces.IPolicyExecutionPoint} interfaces.
 */
public interface IComponentInstanceStore {

  /**
   * Registers a PIP instance to the manager
   *
   * @param componentId the componentId, not null
   * @param instance    the instance, not null
   * @throws InvalidEntityException       when providing an invalid componentId
   * @throws ConflictingResourceException when there already is an instance with that componentId
   */
  void addPipInstance(ComponentId componentId, IPolicyInformationPoint instance) throws InvalidEntityException, ConflictingResourceException;

  /**
   * Registers a PXP instance to the manager
   *
   * @param componentId the componentId, not null
   * @param instance    the instance, not null
   * @throws InvalidEntityException       when providing an invalid componentId
   * @throws ConflictingResourceException when there already is an instance with that componentId
   */
  void addPxpInstance(ComponentId componentId, IPolicyExecutionPoint instance) throws InvalidEntityException, ConflictingResourceException;

  /**
   * Remove a specific instance from the manager
   *
   * @param componentId the componentId, not null
   */
  void removePipInstance(ComponentId componentId);

  /**
   * Remove a specific instance from the manager
   *
   * @param componentId the componentId, not null
   */
  void removePxpInstance(ComponentId componentId);

  /**
   * Retrieve previously added PIP instance
   *
   * @param componentId the componentId, not null
   * @return the previously added PIP instance
   */
  Optional<IPolicyInformationPoint> getPipInstanceByComponentId(ComponentId componentId);

  /**
   * Retrieve previously added PXP instance
   *
   * @param componentId the componentId, not null
   * @return the previously added PXP instance
   */
  Optional<IPolicyExecutionPoint> getPxpInstanceByComponentId(ComponentId componentId);

  /**
   * Clear all references to the service instances
   */
  void clear();
}
