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

package de.fraunhofer.iese.mydata.pdp.interfaces;

import de.fraunhofer.iese.mydata.component.information.method.MethodInterfaceDescription;
import de.fraunhofer.iese.mydata.component.interfaces.IBasicManagementService;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyExecutionPoint;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyInformationPoint;
import de.fraunhofer.iese.mydata.solution.SolutionId;

/**
 * This defines a general interface for a connector cache.
 */
public interface IConnectorCache {
  /**
   * Returns a PMP connector from the cache.
   *
   * @return NULL in case of an error, the connector otherwise.
   */
  IBasicManagementService getPmpConnectionFromCache();

  /**
   * Returns a PIP connector from the cache.
   *
   * @param pipQuery The interface description which identifies the cached PIP
   *          object.
   * @param solutionId : the solution component_id
   * @return NULL in case of an error, the connector otherwise.
   */
  IPolicyInformationPoint getPipConnectionFromCache(MethodInterfaceDescription pipQuery, SolutionId solutionId);

  /**
   * Returns a PXP connector from the cache.
   *
   * @param pxpQuery The interface description which identifies the cached PIP
   *          object.
   * @param solutionId : the solution component_id
   * @return NULL in case of an error, the connector otherwise.
   */
  IPolicyExecutionPoint getPxpConnectionFromCache(MethodInterfaceDescription pxpQuery, SolutionId solutionId);

  /**
   * Clears the PIP cache.
   */
  void clearPipCache();

  /**
   * Clears the PXP cache.
   */
  void clearPxpCache();

  /**
   * Clears the PMP cache.
   */
  void clearPMPCache();
}
