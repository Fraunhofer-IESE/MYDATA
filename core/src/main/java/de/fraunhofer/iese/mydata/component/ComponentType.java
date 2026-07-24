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

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.component.interfaces.IBasicManagementService;
import de.fraunhofer.iese.mydata.component.interfaces.IManagementService;
import de.fraunhofer.iese.mydata.component.interfaces.IMyDataComponent;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyDecisionPoint;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyEnforcementPoint;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyExecutionPoint;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyInformationPoint;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * The different types of MYDATA components.
 *
 * @author Fraunhofer IESE
 */
public enum ComponentType {

  /**
   * Policy Decision Point.
   */
  PDP(IPolicyDecisionPoint.class, "urn:component:%s:pdp:%s", null),

  /**
   * Policy Enforcement Point.
   */
  PEP(IPolicyEnforcementPoint.class, "urn:component:%s:pep:%s", "urn:action:%s:%s"),

  /**
   * Policy Information Point.
   */
  PIP(IPolicyInformationPoint.class, "urn:component:%s:pip:%s", "urn:info:%s:%s"),

  /**
   * Policy Management Point Client.
   */
  PMP(IBasicManagementService.class, "urn:component:%s:pmp:%s", null),

  /**
   * Policy Management Point Server.
   */
  MS(IManagementService.class, "urn:component:%s:ms:%s", null),

  /**
   * Policy Execution Point.
   */
  PXP(IPolicyExecutionPoint.class, "urn:component:%s:pxp:%s", "urn:action:%s:%s");

  private String identifierForInterfaceDescriptionFormat;

  /**
   * The class that corresponds to the type.
   */
  private Class<? extends IMyDataComponent> interfaceClass;

  private String componentIdFormat;

  /**
   * Instantiates a new component type.
   *
   * @param type the type
   */
  ComponentType(Class<? extends IMyDataComponent> type) {
    this.interfaceClass = type;
  }

  /**
   * Instantiates a new component type.
   *
   * @param type the type
   */
  ComponentType(Class<? extends IMyDataComponent> type, String componentIdFormat,
      String identifierForInterfaceDescriptionFormat) {
    this.interfaceClass = type;
    this.componentIdFormat = componentIdFormat;
    this.identifierForInterfaceDescriptionFormat = identifierForInterfaceDescriptionFormat;
  }

  public static ComponentType fromComponentId(ComponentId id) {
    switch (id.getUrn().split(":")[3]) {
      case "pdp":
        return PDP;
      case "pep":
        return PEP;
      case "pip":
        return PIP;
      case "pxp":
        return PXP;
      case "ms":
        return MS;
      case "pmp":
        return PMP;
      default:
        return null;
    }
  }

  /**
   * Gets interface class for the component type.
   *
   * @return the MYDATA component interface
   */
  public Class<? extends IMyDataComponent> getInterface() {
    return this.interfaceClass;
  }

  private String getComponentIdFormat() {
    return this.componentIdFormat;
  }

  public ComponentId getComponentId(SolutionId solutionId, String componentName)
      throws InvalidEntityException {
    MyDataEntity.validateAndNullCheck(solutionId);
    if (StringUtils.isBlank(componentName)) {
      throw new InvalidEntityException("componentName must not be blank");
    }
    final ComponentId componentId = new ComponentId(
        String.format(this.getComponentIdFormat(), solutionId.getIdentifier(), componentName));
    MyDataEntity.validateAndNullCheck(componentId);
    return componentId;
  }

  public String getIdentifierForInterfaceDescription(SolutionId solutionId, String methodName)
      throws InvalidEntityException {
    if (this.identifierForInterfaceDescriptionFormat == null) {
      throw new InvalidEntityException(
          "There is no Identifier for Interface Description for ComponentType " + this.name());
    }
    MyDataEntity.validateAndNullCheck(solutionId);
    return String.format(this.getIdentifierForInterfaceDescriptionFormat(),
        solutionId.getIdentifier(), Validate.notBlank(methodName));
  }

  private String getIdentifierForInterfaceDescriptionFormat() {
    return this.identifierForInterfaceDescriptionFormat;
  }

}
