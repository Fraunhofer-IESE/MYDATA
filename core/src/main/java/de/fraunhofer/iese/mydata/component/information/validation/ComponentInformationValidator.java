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

package de.fraunhofer.iese.mydata.component.information.validation;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.ComponentType;
import de.fraunhofer.iese.mydata.component.information.BasicManagementServiceComponentInformation;
import de.fraunhofer.iese.mydata.component.information.ManagementServiceComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PdpComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PepComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PipComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PxpComponentInformation;
import de.fraunhofer.iese.mydata.component.information.method.MethodInterfaceDescription;
import de.fraunhofer.iese.mydata.component.information.method.PepInterfaceDescription;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.InfoId;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentInformationValidator
    implements ConstraintValidator<MyDataComponentInformation, Object> {
  private static final Logger LOG = LoggerFactory.getLogger(ComponentInformationValidator.class);

  private ComponentType componentType;

  @Override
  public void initialize(MyDataComponentInformation constraintAnnotation) {
    this.componentType = constraintAnnotation.componentType();
  }

  @Override
  public boolean isValid(Object componentInformation, ConstraintValidatorContext context) {
    try {
      if (componentInformation == null) {
        return false;
      }
      final ComponentType valueComponentType = this.determineComponentType(componentInformation);
      if (null == valueComponentType) {
        return false;
      }
      if (valueComponentType != this.componentType) {
        return false;
      }
      switch (valueComponentType) {
        case PXP:
          this.validatePxp((PxpComponentInformation) componentInformation);
          break;
        case PIP:
          this.validatePip((PipComponentInformation) componentInformation);
          break;
        case PEP:
          this.validatePep((PepComponentInformation) componentInformation);
          break;
        case PDP:
          this.validatePdp((PdpComponentInformation) componentInformation);
          break;
        case PMP:
          this.validatePmp((BasicManagementServiceComponentInformation) componentInformation);
          break;
        case MS:
          this.validateMs((ManagementServiceComponentInformation) componentInformation);
          break;
        default:
          return false;
      }
      return true;
    } catch (final InvalidEntityException e) {
      LOG.info(e.getMessage(), e);
      return false;
    }
  }

  private void validateMs(ManagementServiceComponentInformation componentInformation)
      throws InvalidEntityException {
    final ComponentId componentId = componentInformation.getComponentId();
    MyDataEntity.validateAndNullCheck(componentId);
    if (componentId.getComponentType() != ComponentType.MS) {
      throw new InvalidEntityException(
          "ComponentId " + componentId.getUrn() + " does not fit to a MS");
    }
  }

  private void validatePmp(BasicManagementServiceComponentInformation componentInformation)
      throws InvalidEntityException {
    final ComponentId componentId = componentInformation.getComponentId();
    MyDataEntity.validateAndNullCheck(componentId);
    if (componentId.getComponentType() != ComponentType.PMP) {
      throw new InvalidEntityException(
          "ComponentId " + componentId.getUrn() + " does not fit to a PMP");
    }
  }

  private void validatePdp(PdpComponentInformation componentInformation)
      throws InvalidEntityException {
    final ComponentId componentId = componentInformation.getComponentId();
    MyDataEntity.validateAndNullCheck(componentId);
    if (componentId.getComponentType() != ComponentType.PDP) {
      throw new InvalidEntityException(
          "ComponentId " + componentId.getUrn() + " does not fit to a PDP");
    }
  }

  private void validatePep(PepComponentInformation componentInformation)
      throws InvalidEntityException {
    final ComponentId componentId = componentInformation.getComponentId();
    MyDataEntity.validateAndNullCheck(componentId);
    if (componentId.getComponentType() != ComponentType.PEP) {
      throw new InvalidEntityException(
          "ComponentId " + componentId.getUrn() + " does not fit to a PEP");
    }
    final SolutionId solutionId = SolutionId.fromComponentId(componentInformation.getComponentId());
    for (final PepInterfaceDescription pepInterfaceDescription : componentInformation
        .getInterfaceDescriptions()) {
      final ActionId actionId = pepInterfaceDescription.getEvent();
      MyDataEntity.validateAndNullCheck(actionId);
      final SolutionId isid = SolutionId.fromActionId(actionId);
      if (!isid.equals(solutionId)) {
        throw new InvalidEntityException(
            "The SolutionId from the MethodInterfaceDescription does not match the ComponentId");
      }
    }
  }

  private void validatePip(PipComponentInformation componentInformation)
      throws InvalidEntityException {
    final ComponentId componentId = componentInformation.getComponentId();
    MyDataEntity.validateAndNullCheck(componentId);
    if (componentId.getComponentType() != ComponentType.PIP) {
      throw new InvalidEntityException(
          "ComponentId " + componentId.getUrn() + " does not fit to a PIP");
    }
    final SolutionId solutionId = SolutionId.fromComponentId(componentInformation.getComponentId());
    for (final MethodInterfaceDescription methodInterfaceDescription : componentInformation
        .getMethodInterfaceDescriptions()) {
      final String methodName = methodInterfaceDescription.getMethodName();
      final InfoId iid = new InfoId(methodName);
      MyDataEntity.validateAndNullCheck(iid);
      final SolutionId isid = SolutionId.fromInfoId(iid);
      if (!isid.equals(solutionId)) {
        throw new InvalidEntityException(
            "The SolutionId from the MethodInterfaceDescription does not match the ComponentId");
      }
    }
  }

  private void validatePxp(PxpComponentInformation componentInformation)
      throws InvalidEntityException {
    final ComponentId componentId = componentInformation.getComponentId();
    MyDataEntity.validateAndNullCheck(componentId);
    if (componentId.getComponentType() != ComponentType.PXP) {
      throw new InvalidEntityException(
          "ComponentId " + componentId.getUrn() + " does not fit to a PXP");
    }
    final SolutionId solutionId = SolutionId.fromComponentId(componentInformation.getComponentId());
    for (final MethodInterfaceDescription methodInterfaceDescription : componentInformation
        .getMethodInterfaceDescriptions()) {
      final String methodName = methodInterfaceDescription.getMethodName();
      final ActionId aid = new ActionId(methodName);
      MyDataEntity.validateAndNullCheck(aid);
      final SolutionId isid = SolutionId.fromActionId(aid);
      if (!isid.equals(solutionId)) {
        throw new InvalidEntityException(
            "The SolutionId from the MethodInterfaceDescription does not match the ComponentId");
      }
    }
  }

  private ComponentType determineComponentType(Object o) {
    if (o == null) {
      return null;
    } else if (o instanceof PipComponentInformation) {
      return ComponentType.PIP;
    } else if (o instanceof PxpComponentInformation) {
      return ComponentType.PXP;
    } else if (o instanceof PepComponentInformation) {
      return ComponentType.PEP;
    } else if (o instanceof PdpComponentInformation) {
      return ComponentType.PDP;
    } else if (o instanceof ManagementServiceComponentInformation) {
      return ComponentType.MS;
    } else if (o instanceof BasicManagementServiceComponentInformation) {
      return ComponentType.PMP;
    } else {
      return null;
    }
  }
}
