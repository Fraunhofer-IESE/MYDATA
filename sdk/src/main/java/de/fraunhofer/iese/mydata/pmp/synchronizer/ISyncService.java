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

package de.fraunhofer.iese.mydata.pmp.synchronizer;

import de.fraunhofer.iese.mydata.component.information.PepComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PipComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PxpComponentInformation;

/**
 * This interface encapsulates the synchronization of policies, timers and componentInformation.
 * The Service needs to be configured before calling any of the methods declared in this interface.
 * The local PDP will start in failureMode when there is a ISyncService. It is therefore the obligation of
 * any ISyncService implementation to deactivate the failureMode when the initial sync completed successfully.
 */
public interface ISyncService {

  /**
   * Start synchronization
   *
   * @throws SynchronizerException in case of an error while starting the synchronization
   */
  void start() throws SynchronizerException;

  /**
   * Stop synchronization
   *
   * @throws SynchronizerException in case of an error while stopping the synchronization
   */
  void stop() throws SynchronizerException;

  /**
   * Push the registration of a PEP component
   *
   * @param pepComponentInformation the pepComponentInformation
   * @throws SynchronizerException in case of an error while pushing the information
   */
  void pushPep(PepComponentInformation pepComponentInformation) throws SynchronizerException;

  /**
   * Push the registration of a PIP component
   *
   * @param pipComponentInformation the pipComponentInformation
   * @throws SynchronizerException in case of an error while pushing the information
   */
  void pushPip(PipComponentInformation pipComponentInformation) throws SynchronizerException;

  /**
   * Push the registration of a PXP component
   *
   * @param pxpComponentInformation the pxpComponentInformation
   * @throws SynchronizerException in case of an error while pushing the information
   */
  void pushPxp(PxpComponentInformation pxpComponentInformation) throws SynchronizerException;

}
