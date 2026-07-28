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

package de.fraunhofer.iese.mydata;

import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.information.PepComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PipComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PxpComponentInformation;
import de.fraunhofer.iese.mydata.component.interfaces.IBasicManagementService;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyEnforcementPoint;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyExecutionPoint;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyInformationPoint;
import de.fraunhofer.iese.mydata.exception.ConflictingResourceException;
import de.fraunhofer.iese.mydata.exception.InitializationException;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.exception.ResourceUpdateException;
import de.fraunhofer.iese.mydata.pep.DefaultPolicyEnforcementPoint;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import java.io.IOException;
import java.net.URI;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

/**
 * This interface describes the public facade to the MyDataEnvironment,
 * regardless whether you use the Library in LOCAL or CLOUD mode. You can use
 * the {@link IMyDataEnvironment} more or less transparent, but you will have to
 * pay special attention when registering PIP/PXP components because they need
 * to be accessible to the PDP. Currently we distinguish three modes in which a
 * service component can be registered to the MyDataEnvironment:
 *
 * <ul>
 * <li><b>local:</b> component instance will be registered to the
 * MyDataEnvironment, will be accessible to local PDP and component information
 * will be published to PMP; no need to provide further information like URL.
 * see {@link #registerLocalPip(ComponentId, Object)}</li>
 * <li><b>managed:</b> component instance will be registered to the
 * MyDataEnvironment, can programmatically be retrieved via
 * {@link #getManagedPip(ComponentId)}/{@link #getManagedPxp(ComponentId)} and
 * the corresponding component information is published to the PMP. <b>You are
 * in charge to make the component accessible for the PDP under the specified
 * URL.</b> see {@link #registerManagedPip(ComponentId, Object, List)}</li>
 * <li><b>unmanaged:</b> component will not be managed by the MyDataEnvironment,
 * only the component information is published to the PMP. <b>You are in charge
 * to make the component accessible for the PDP.</b> see
 * {@link #registerUnmanagedPip(PipComponentInformation)}</li>
 * </ul>
 *
 * {@link IMyDataEnvironment} instances can be created and retrieved by using the
 * {@link MyDataEnvironmentManager}
 *
 * @see MyDataEnvironmentManager
 */
public interface IMyDataEnvironment {
  /**
   * @return the {@link IMyDataEnvironment}s environmentId
   */
  String getEnvironmentId();

  /**
   * An {@link IMyDataEnvironment} instance is always associated with exactly
   * one solution.
   *
   * @return the solutionId of the solution the {@link IMyDataEnvironment}
   * instance is associated with.
   */
  SolutionId getSolutionId();

  /**
   * Access to the instance's {@link OperationalMode}
   *
   * @return the instance's {@link OperationalMode}
   */
  OperationalMode getOperationalMode();

  /**
   * Registers a PepComponent to the IMyDataEnvironment and its encapsulated PMP
   *
   * @param pepComponentInformation the PipComponentInformation
   * @return the componentId
   * @throws IOException                  communication failure
   * @throws ConflictingResourceException the conflicting resource exception
   * @throws ResourceUpdateException      the resource update exception
   * @throws InvalidEntityException       the invalid entity exception
   * @throws NoSuchEntityException
   */
  ComponentId registerPep(PepComponentInformation pepComponentInformation) throws IOException, ResourceUpdateException, ConflictingResourceException, InvalidEntityException, NoSuchEntityException;

  /**
   * Registers a remote/external PipComponent that is not managed by the
   * {@link IMyDataEnvironment}.
   *
   * @param pipComponentInformation the PipComponentInformation
   * @return the componentId
   * @throws IOException                  communication failure
   * @throws ConflictingResourceException the conflicting resource exception
   * @throws ResourceUpdateException      the resource update exception
   * @throws InvalidEntityException       the invalid entity exception
   * @throws NoSuchEntityException
   */
  ComponentId registerUnmanagedPip(PipComponentInformation pipComponentInformation) throws IOException, ResourceUpdateException, ConflictingResourceException, InvalidEntityException, NoSuchEntityException;

  /**
   * Registers a PipComponent that is managed by the {@link IMyDataEnvironment}
   * but you are in charge to expose it accessible to the PDP via the specified
   * URL Managed Components can be retrieved from the
   * {@link IMyDataEnvironment}; see {@link #getManagedPip(ComponentId)}.
   *
   * @param componentId the componentId
   * @param instance    the instance
   * @param listOfUrl   the URL under which you will expose the PipComponent to be
   *                    used by the PDP.
   * @return the componentId
   * @throws IOException                  communication failure
   * @throws ConflictingResourceException the conflicting resource exception
   * @throws ResourceUpdateException      the resource update exception
   * @throws InvalidEntityException       the invalid entity exception
   * @throws NoSuchEntityException
   */
  ComponentId registerManagedPip(ComponentId componentId, Object instance, List<URI> listOfUrl) throws InvalidEntityException, ResourceUpdateException, ConflictingResourceException, IOException, NoSuchEntityException;

  /**
   * Registers a PipComponent that is managed by the {@link IMyDataEnvironment}
   * but you are in charge to expose it accessible to the PDP via the specified
   * URL Managed Components can be retrieved from the
   * {@link IMyDataEnvironment}; see {@link #getManagedPip(ComponentId)}.
   *
   * @param componentName the componentName
   * @param instance      the instance
   * @param listOfUrl     the URL under which you will expose the PipComponent to be
   *                      used by the PDP.
   * @return the componentId
   * @throws IOException                  communication failure
   * @throws ConflictingResourceException the conflicting resource exception
   * @throws ResourceUpdateException      the resource update exception
   * @throws InvalidEntityException       the invalid entity exception
   * @throws NoSuchEntityException
   */
  ComponentId registerManagedPip(String componentName, Object instance, List<URI> listOfUrl) throws InvalidEntityException, ResourceUpdateException, ConflictingResourceException, IOException, NoSuchEntityException;

  /**
   * Registers a local PipComponent that is managed by the
   * {@link IMyDataEnvironment} and is accessible to local PDP.
   *
   * @param componentId the componentId
   * @param instance    the object that should be used as a PipComponent, can
   *                    implement
   *                    {@link de.fraunhofer.iese.mydata.component.interfaces.IPolicyInformationPoint}
   *                    but does not have to.
   * @return the componentId
   * @throws IOException                  communication failure
   * @throws ConflictingResourceException the conflicting resource exception
   * @throws ResourceUpdateException      the resource update exception
   * @throws InvalidEntityException       the invalid entity exception
   * @throws NoSuchEntityException
   */
  ComponentId registerLocalPip(ComponentId componentId, Object instance) throws IOException, InvalidEntityException, ResourceUpdateException, ConflictingResourceException, NoSuchEntityException;

  /**
   * Registers a local PipComponent that is managed by the
   * {@link IMyDataEnvironment} and is accessible to local PDP.
   *
   * @param componentName the componentName
   * @param instance      the object that should be used as a PipComponent, can
   *                      implement
   *                      {@link de.fraunhofer.iese.mydata.component.interfaces.IPolicyInformationPoint}
   *                      but does not have to.
   * @return the componentId
   * @throws IOException                  communication failure
   * @throws ConflictingResourceException the conflicting resource exception
   * @throws ResourceUpdateException      the resource update exception
   * @throws InvalidEntityException       the invalid entity exception
   * @throws NoSuchEntityException
   */
  ComponentId registerLocalPip(String componentName, Object instance) throws IOException, InvalidEntityException, ResourceUpdateException, ConflictingResourceException, NoSuchEntityException;

  /**
   * Registers a remote/external PxpComponent that is not managed by the
   * {@link IMyDataEnvironment}.
   *
   * @param pxpComponentInformation the PxpComponentInformation
   * @return the componentId
   * @throws IOException                  communication failure
   * @throws ConflictingResourceException the conflicting resource exception
   * @throws ResourceUpdateException      the resource update exception
   * @throws InvalidEntityException       the invalid entity exception
   * @throws NoSuchEntityException
   */
  ComponentId registerUnmanagedPxp(PxpComponentInformation pxpComponentInformation) throws IOException, ResourceUpdateException, ConflictingResourceException, InvalidEntityException, NoSuchEntityException;

  /**
   * Registers a PxpComponent that is managed by the {@link IMyDataEnvironment}
   * but you are in charge to expose it accessible to the PDP via the specified
   * URL Managed Components can be retrieved from the
   * {@link IMyDataEnvironment}; see {@link #getManagedPxp(ComponentId)}.
   *
   * @param componentId the componentId
   * @param instance    the instance
   * @param listOfUrl   the URL under which you will expose the PxpComponent to be
   *                    used by the PDP.
   * @return the componentId
   * @throws IOException                  communication failure
   * @throws ConflictingResourceException the conflicting resource exception
   * @throws ResourceUpdateException      the resource update exception
   * @throws InvalidEntityException       the invalid entity exception
   * @throws NoSuchEntityException
   */
  ComponentId registerManagedPxp(ComponentId componentId, Object instance, List<URI> listOfUrl) throws InvalidEntityException, ResourceUpdateException, ConflictingResourceException, IOException, NoSuchEntityException;

  /**
   * Registers a PxpComponent that is managed by the {@link IMyDataEnvironment}
   * but you are in charge to expose it accessible to the PDP via the specified
   * URL Managed Components can be retrieved from the
   * {@link IMyDataEnvironment}; see {@link #getManagedPxp(ComponentId)}.
   *
   * @param componentName the componentName
   * @param instance      the instance
   * @param listOfUrl     the URL under which you will expose the PxpComponent to be
   *                      used by the PDP.
   * @return the componentId
   * @throws IOException                  communication failure
   * @throws ConflictingResourceException the conflicting resource exception
   * @throws ResourceUpdateException      the resource update exception
   * @throws InvalidEntityException       the invalid entity exception
   * @throws NoSuchEntityException
   */
  ComponentId registerManagedPxp(String componentName, Object instance, List<URI> listOfUrl) throws InvalidEntityException, ResourceUpdateException, ConflictingResourceException, IOException, NoSuchEntityException;

  /**
   * Registers a local PxpComponent that is managed by the
   * {@link IMyDataEnvironment} and is accessible to local PDP.
   *
   * @param componentId the componentId
   * @param instance    the object that should be used as a PxpComponent, can
   *                    implement
   *                    {@link de.fraunhofer.iese.mydata.component.interfaces.IPolicyExecutionPoint}
   *                    but does not have to.
   * @return the componentId
   * @throws IOException                  communication failure
   * @throws ConflictingResourceException the conflicting resource exception
   * @throws ResourceUpdateException      the resource update exception
   * @throws InvalidEntityException       the invalid entity exception
   * @throws NoSuchEntityException
   */
  ComponentId registerLocalPxp(ComponentId componentId, Object instance) throws IOException, InvalidEntityException, ResourceUpdateException, ConflictingResourceException, NoSuchEntityException;

  /**
   * Registers a local PxpComponent that is managed by the
   * {@link IMyDataEnvironment} and is accessible to local PDP.
   *
   * @param componentName the componentName
   * @param instance      the object that should be used as a PxpComponent, can
   *                      implement
   *                      {@link de.fraunhofer.iese.mydata.component.interfaces.IPolicyExecutionPoint}
   *                      but does not have to.
   * @return the componentId
   * @throws IOException                  communication failure
   * @throws ConflictingResourceException the conflicting resource exception
   * @throws ResourceUpdateException      the resource update exception
   * @throws InvalidEntityException       the invalid entity exception
   * @throws NoSuchEntityException
   */
  ComponentId registerLocalPxp(String componentName, Object instance) throws IOException, InvalidEntityException, ResourceUpdateException, ConflictingResourceException, NoSuchEntityException;

  /**
   * Retrieves a previously registered managed Pip instance
   *
   * @param componentId the componentId
   * @return the previously registered managed Pip instance
   */
  Optional<IPolicyInformationPoint> getManagedPip(ComponentId componentId);

  /**
   * Retrieves a previously registered managed Pxp instance
   *
   * @param componentId the componentId
   * @return the previously registered managed Pxp instance
   */
  Optional<IPolicyExecutionPoint> getManagedPxp(ComponentId componentId);

  /**
   * This method generates a custom PEP from the given interface and registers
   * it to the PMP. Make sure you call this method at most once per
   * componentId/interface.
   *
   * @param componentId    the componentId
   * @param interfaceOfPep The Interface to generate a PEP for.
   * @param <T>            Type of the Interface
   * @return the generated implementation of the custom PEP
   * @throws InitializationException in case of an error and when there is no PDP available
   */
  <T> T constructAndRegisterCustomPep(ComponentId componentId, Class<T> interfaceOfPep) throws InitializationException;

  /**
   * This method generates a custom PEP from the given interface and registers
   * it to the PMP. Make sure you call this method at most once per
   * componentId/interface.
   *
   * @param componentName  the componentName
   * @param interfaceOfPep The Interface to generate a PEP for.
   * @param <T>            Type of the Interface
   * @return the generated implementation of the custom PEP
   * @throws InitializationException in case of an error and when there is no PDP available
   */
  <T> T constructAndRegisterCustomPep(String componentName, Class<T> interfaceOfPep) throws InitializationException;

  /**
   * Get configured timezone for the associated solution
   *
   * @return the {@link ZoneId} that identifies the timezone
   */
  ZoneId getTimezone();

  /**
   * @return the {@link IBasicManagementService} for this environment
   */
  IBasicManagementService getPmp();

  /**
   * @return a {@link DefaultPolicyEnforcementPoint} that can be used for
   * enforcement
   */
  IPolicyEnforcementPoint getPep();
}
