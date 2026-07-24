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
import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.information.PepComponentInformation;
import de.fraunhofer.iese.mydata.component.information.method.MethodInterfaceDescription;
import de.fraunhofer.iese.mydata.component.information.method.PepInterfaceDescription;
import de.fraunhofer.iese.mydata.exception.ConflictingResourceException;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.exception.ResourceUpdateException;
import de.fraunhofer.iese.mydata.pep.common.DecisionEnforcer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Should be instantiated to create Pep enable application
 */
public class PolicyEnforcementPoint extends DefaultPolicyEnforcementPoint {

  private static final Logger LOG = LoggerFactory.getLogger(PolicyEnforcementPoint.class);

  private final ComponentId componentId;

  private final List<PepInterfaceDescription> pepInterfaceDescriptions;

  private final List<MethodInterfaceDescription> methodInterfaceDescriptions;

  /**
   * Constructor. Initializes the Connection to PMP and registers the Pep.
   *
   * @param  myDataEnvironment           the IMyDataEnvironment the PEP belongs to
   * @param  decisionEnforcer            Enforcer to use.
   * @param  componentId                 Id of the Pep.
   * @param  pepInterfaceDescriptions    Modifiers this Pep exports.
   * @param  methodInterfaceDescriptions Modifiers Interface Description.
   * @param  initialize                  initialize?
   * @throws InvalidEntityException
   * @throws IOException
   * @throws NoSuchEntityException
   */
  public PolicyEnforcementPoint(IMyDataEnvironment myDataEnvironment,
      DecisionEnforcer decisionEnforcer, ComponentId componentId,
      List<PepInterfaceDescription> pepInterfaceDescriptions,
      List<MethodInterfaceDescription> methodInterfaceDescriptions, boolean initialize)
      throws InvalidEntityException, IOException, NoSuchEntityException {
    super(myDataEnvironment, decisionEnforcer);
    MyDataEntity.validateAndNullCheck(componentId);
    this.componentId = componentId;
    this.pepInterfaceDescriptions = pepInterfaceDescriptions;
    this.methodInterfaceDescriptions = methodInterfaceDescriptions;
    if (initialize) {
      this.initialize();
    }
  }

  /**
   * Returns Pep componet ID
   *
   * @return The id of the component.
   */
  @Override
  public ComponentId getId() {
    return this.componentId;
  }

  /**
   * initialization using registry builder
   *
   * @throws IOException           If connection to PDP could not be established.
   * @throws NoSuchEntityException
   */
  @Override
  public boolean initialize() throws IOException, NoSuchEntityException {
    if (!super.initialize()) {
      return false;
    }
    final PepComponentInformation pepComponentInformation = new PepComponentInformation(this.componentId);
    pepComponentInformation.setInterfaceDescriptions(this.pepInterfaceDescriptions);
    pepComponentInformation.setMethodInterfaceDescriptions(this.methodInterfaceDescriptions);
    ComponentId registrationComponent;
    try {
      registrationComponent = myDataEnvironment.registerPep(pepComponentInformation);
    } catch (IOException | ConflictingResourceException | ResourceUpdateException | InvalidEntityException e) {
      LOG.warn("an exception occurred while initializing Pep component", e);
      registrationComponent = null;
    }
    if (registrationComponent != null) {
      LOG.info("Pep initialization is done!");
    } else {
      LOG.warn("a problem occurred while initializing Pep component");
    }
    // TODO check exception handling
    return null != registrationComponent;
  }

}
