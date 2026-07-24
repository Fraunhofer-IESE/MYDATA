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

package de.fraunhofer.iese.mydata.component;

import de.fraunhofer.iese.mydata.component.health.HealthStatus;
import de.fraunhofer.iese.mydata.component.information.PdpComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PepComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PipComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PxpComponentInformation;
import de.fraunhofer.iese.mydata.component.information.method.MethodInterfaceDescription;
import de.fraunhofer.iese.mydata.exception.ConflictingResourceException;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.exception.ResourceUpdateException;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * The Interface IComponentService.
 */
public interface IComponentService {

  /**
   * Adds a pdp.
   *
   * @param  component                    the component
   * @return                              true if the component was successfully added, false
   *                                      otherwise
   * @throws IOException                  communication failure
   * @throws ConflictingResourceException if there is already a pdp component
   * @throws ResourceUpdateException      if the pdp component could not be registered
   * @throws InvalidEntityException       if the pdp component is not valid
   * @throws NoSuchEntityException
   */
  ComponentId addPdp(PdpComponentInformation component)
      throws IOException, ConflictingResourceException, ResourceUpdateException,
      InvalidEntityException, NoSuchEntityException;

  /**
   * Adds a pep.
   *
   * @param  component                    the component
   * @return                              true if the component was successfully added, false
   *                                      otherwise
   * @throws IOException                  communication failure
   * @throws ConflictingResourceException if there is already a pep component with the id given
   * @throws ResourceUpdateException      the resource update exception
   * @throws InvalidEntityException       if the pep component is not valid
   * @throws NoSuchEntityException
   */
  ComponentId addPep(PepComponentInformation component)
      throws IOException, ConflictingResourceException, ResourceUpdateException,
      InvalidEntityException, NoSuchEntityException;

  /**
   * Adds a pip.
   *
   * @param  component                    the component
   * @return                              true if the component was successfully added, false
   *                                      otherwise
   * @throws IOException                  communication failure
   * @throws ConflictingResourceException if there is already a pip component with the id given
   * @throws ResourceUpdateException      the resource update exception
   * @throws InvalidEntityException       if the pip component is not valid
   * @throws NoSuchEntityException
   */
  ComponentId addPip(PipComponentInformation component)
      throws IOException, ConflictingResourceException, ResourceUpdateException,
      InvalidEntityException, NoSuchEntityException;

  /**
   * Adds a pxp.
   *
   * @param  component                    the component
   * @return                              true if the component was successfully added, false
   *                                      otherwise
   * @throws IOException                  communication failure
   * @throws ConflictingResourceException if there is already a pxp component with the id given
   * @throws ResourceUpdateException      the resource update exception
   * @throws InvalidEntityException       if the pxp component is not valid
   * @throws NoSuchEntityException
   */
  ComponentId addPxp(PxpComponentInformation component)
      throws IOException, ConflictingResourceException, ResourceUpdateException,
      InvalidEntityException, NoSuchEntityException;

  /**
   * pdp component exists
   *
   * @param  componentId
   * @return                        true if the pdp exists, false otherwise
   * @throws IOException
   * @throws InvalidEntityException if the component id is not valid
   */
  boolean pdpExists(ComponentId componentId) throws IOException, InvalidEntityException;

  /**
   * Pep component exists
   *
   * @param  componentId
   * @return                        true if the pep exists, false otherwise
   * @throws IOException
   * @throws InvalidEntityException if the component id is not valid
   */
  boolean pepExists(ComponentId componentId) throws IOException, InvalidEntityException;

  /**
   * Pip component exists
   *
   * @param  componentId
   * @return                        true if the pip exists, false otherwise
   * @throws IOException
   * @throws InvalidEntityException if the component id is not valid
   */
  boolean pipExists(ComponentId componentId) throws IOException, InvalidEntityException;

  /**
   * Pxp component exists
   *
   * @param  componentId
   * @return                        true if the pxp exists, false otherwise
   * @throws IOException
   * @throws InvalidEntityException if the component id is not valid
   */
  boolean pxpExists(ComponentId componentId) throws IOException, InvalidEntityException;

  /**
   * Returns the registered PDP.
   *
   * @return                       the PDP
   * @throws IOException           communication failure
   * @throws NoSuchEntityException the no such element exception
   */
  PdpComponentInformation getPdp() throws IOException, NoSuchEntityException;

  /**
   * Lookup PEP by {@link ComponentId}.
   *
   * @param  id                     the id
   * @return                        the component
   * @throws IOException            communication failure
   * @throws NoSuchEntityException  if no pep component with the id given exists
   * @throws InvalidEntityException if the component id is not valid
   */
  PepComponentInformation getPep(ComponentId id)
      throws IOException, InvalidEntityException, NoSuchEntityException;

  /**
   * Lookup PIP by {@link ComponentId}.
   *
   * @param  id                     the id
   * @return                        the component
   * @throws IOException            communication failure
   * @throws NoSuchEntityException  if no pip component with the id given exists
   * @throws InvalidEntityException if the component id is not valid
   */
  PipComponentInformation getPip(ComponentId id)
      throws IOException, NoSuchEntityException, InvalidEntityException;

  /**
   * Lookup PXP by {@link ComponentId}.
   *
   * @param  id                     the id
   * @return                        the component
   * @throws IOException            communication failure
   * @throws NoSuchEntityException  if no pxp component with the id given exists
   * @throws InvalidEntityException if the component id is not valid
   */
  PxpComponentInformation getPxp(ComponentId id)
      throws IOException, NoSuchEntityException, InvalidEntityException;

  /**
   * Get the pip component state
   *
   * @param componentId
   * @return HealthStatus of the PIP
   * @throws IOException            Signals that an I/O exception has occurred.
   * @throws NoSuchEntityException  if there is no pip component with the id given
   * @throws InvalidEntityException if the component id is not valid
   */
  HealthStatus getPipState(ComponentId componentId)
      throws IOException, NoSuchEntityException, InvalidEntityException;

  /**
   * Get the pxp component state
   *
   * @param componentId
   * @return HealthStatus of the PXP
   * @throws IOException            Signals that an I/O exception has occurred.
   * @throws NoSuchEntityException  if there is no pxp component with the id given
   * @throws InvalidEntityException if the component id is not valid
   */
  HealthStatus getPxpState(ComponentId componentId)
      throws IOException, NoSuchEntityException, InvalidEntityException;

  /**
   * Gets the all component states.
   *
   * @param solutionId the solution id
   * @return the all component states
   * @throws IOException            Signals that an I/O exception has occurred.
   * @throws InvalidEntityException the invalid entity exception
   * @throws NoSuchEntityException
   */
  Map<ComponentId, HealthStatus> getAllComponentStates(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException;

  /**
   * Lookup all PEPs of a certain solution.
   *
   * @param  solutionId             the solutionId
   * @return                        the list of Peps
   * @throws IOException            communication failure
   * @throws InvalidEntityException the invalid entity exception
   * @throws NoSuchEntityException
   */
  Set<PepComponentInformation> lookupPep(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException;

  /**
   * Lookup all PIPs of a certain solution.
   *
   * @param  solutionId             the solutionId
   * @param  query                  the required / available {@link MethodInterfaceDescription}s to
   *                                  match against; null to return all
   * @return                        the list of Pips
   * @throws IOException            communication failure
   * @throws InvalidEntityException the invalid entity exception
   * @throws NoSuchEntityException
   */
  Set<PipComponentInformation> lookupPip(SolutionId solutionId, MethodInterfaceDescription query)
      throws IOException, InvalidEntityException, NoSuchEntityException;

  /**
   * Lookup all PXPs of a certain solution.
   *
   * @param  solutionId             the solutionId
   * @param  query                  the required / available {@link MethodInterfaceDescription}s to
   *                                  match against; null to return all
   * @return                        the list of Pxps
   * @throws IOException            communication failure
   * @throws InvalidEntityException the invalid entity exception
   * @throws NoSuchEntityException
   */
  Set<PxpComponentInformation> lookupPxp(SolutionId solutionId, MethodInterfaceDescription query)
      throws IOException, InvalidEntityException, NoSuchEntityException;

  /**
   * Adds a pdp.
   *
   * @param  component               the component
   * @return                         true if the component was successfully added, false otherwise
   * @throws IOException             communication failure
   * @throws NoSuchEntityException   the no such element exception
   * @throws ResourceUpdateException the resource update exception
   * @throws InvalidEntityException  the invalid entity exception
   */
  ComponentId updatePdp(PdpComponentInformation component)
      throws IOException, NoSuchEntityException, ResourceUpdateException, InvalidEntityException;

  /**
   * Adds a pep.
   *
   * @param  component               the component
   * @return                         true if the component was successfully added, false otherwise
   * @throws IOException             communication failure
   * @throws NoSuchEntityException   the no such element exception
   * @throws ResourceUpdateException the resource update exception
   * @throws InvalidEntityException  the invalid entity exception
   */
  ComponentId updatePep(PepComponentInformation component)
      throws IOException, NoSuchEntityException, ResourceUpdateException, InvalidEntityException;

  /**
   * Adds a pip.
   *
   * @param  component               the component
   * @return                         true if the component was successfully added, false otherwise
   * @throws IOException             communication failure
   * @throws NoSuchEntityException   the no such element exception
   * @throws ResourceUpdateException the resource update exception
   * @throws InvalidEntityException  the invalid entity exception
   */
  ComponentId updatePip(PipComponentInformation component)
      throws IOException, NoSuchEntityException, ResourceUpdateException, InvalidEntityException;

  /**
   * Adds a pxp.
   *
   * @param  component               the component
   * @return                         true if the component was successfully added, false otherwise
   * @throws IOException             communication failure
   * @throws NoSuchEntityException   the no such element exception
   * @throws ResourceUpdateException the resource update exception
   * @throws InvalidEntityException  the invalid entity exception
   */
  ComponentId updatePxp(PxpComponentInformation component)
      throws IOException, NoSuchEntityException, ResourceUpdateException, InvalidEntityException;

  /**
   * Delete a pep component from the database
   *
   * @param  componentId
   * @throws IOException
   * @throws NoSuchEntityException
   * @throws ResourceUpdateException
   * @throws InvalidEntityException  if the component id is not valid
   */
  void deletePep(ComponentId componentId)
      throws IOException, NoSuchEntityException, ResourceUpdateException, InvalidEntityException;

  /**
   * Delete a pip component from the database
   *
   * @param  componentId
   * @throws IOException
   * @throws NoSuchEntityException
   * @throws ResourceUpdateException
   * @throws InvalidEntityException
   */
  void deletePip(ComponentId componentId)
      throws IOException, NoSuchEntityException, ResourceUpdateException, InvalidEntityException;

  /**
   * Delete a pxp component from the database
   *
   * @param  componentId
   * @throws IOException
   * @throws NoSuchEntityException
   * @throws ResourceUpdateException
   * @throws InvalidEntityException
   */
  void deletePxp(ComponentId componentId)
      throws IOException, NoSuchEntityException, ResourceUpdateException, InvalidEntityException;

}
