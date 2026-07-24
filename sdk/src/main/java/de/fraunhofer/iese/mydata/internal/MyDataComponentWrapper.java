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

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.ComponentType;
import de.fraunhofer.iese.mydata.component.health.HealthStatus;
import de.fraunhofer.iese.mydata.component.health.Status;
import de.fraunhofer.iese.mydata.component.information.method.InputParameterDescription;
import de.fraunhofer.iese.mydata.component.information.method.MethodInterfaceDescription;
import de.fraunhofer.iese.mydata.component.interfaces.IMyDataComponent;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.policy.exception.InformationUndeterminableException;
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;
import de.fraunhofer.iese.mydata.registry.InterfaceDescriptionDiscovery;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import com.google.common.primitives.Primitives;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Base class for wrapping components that do not have to implement the component's interface e.g.
 * {@link de.fraunhofer.iese.mydata.component.interfaces.IPolicyInformationPoint},
 * {@link de.fraunhofer.iese.mydata.component.interfaces.IPolicyExecutionPoint}
 *
 * @param <T>
 */
class MyDataComponentWrapper<T extends IMyDataComponent> implements IMyDataComponent {

  private static final InterfaceDescriptionDiscovery discovery = new InterfaceDescriptionDiscovery();

  private static final Logger LOG = LoggerFactory.getLogger(MyDataComponentWrapper.class);

  protected final T alreadyRightTypedReference;

  private final ComponentId componentId;

  private final Map<MethodInterfaceDescription, Method> interfaceToMethod;

  private final Object instanceToWrap;

  @SuppressWarnings("unchecked")
  public MyDataComponentWrapper(ComponentId componentId, Object instanceToWrap,
                                ComponentType componentType) throws InvalidEntityException {
    MyDataEntity.validateAndNullCheck(componentId);
    this.componentId = componentId;
    final Class<? extends IMyDataComponent> type = componentType.getInterface();
    this.instanceToWrap = Objects.requireNonNull(instanceToWrap);
    if (type.isAssignableFrom(instanceToWrap.getClass())) {
      alreadyRightTypedReference = (T) instanceToWrap;
    } else {
      alreadyRightTypedReference = null;
    }
    this.interfaceToMethod = MyDataComponentWrapper.discovery.discover(instanceToWrap.getClass(),
        componentType, SolutionId.fromComponentId(componentId));
  }

  /**
   * Retrieve the automatically discovered MethodInterfaceDescriptions
   *
   * @return The automatically discovered MethodInterfaceDescriptions
   */
  public List<MethodInterfaceDescription> getMethodInterfaceDescriptions() {
    return new ArrayList<>(interfaceToMethod.keySet());
  }

  private Optional<MethodInterfaceDescription> findInterfaceDescription(String actionName,
                                                                        ParameterList parameterList) {
    for (final MethodInterfaceDescription interfaceDescription : this.interfaceToMethod.keySet()) {
      if (this.matches(actionName, parameterList, interfaceDescription)) {
        return Optional.of(interfaceDescription);
      }
    }
    return Optional.empty();
  }

  private boolean matches(String actionName, ParameterList parameterList,
                          MethodInterfaceDescription interfaceDescription) {
    if (this.matchesName(actionName, interfaceDescription)) {
      if (this.matchesParameter(parameterList, interfaceDescription.getParameters())) {
        return true;
      } else {
        LOG.debug(
            "Service Method {} found but provided parameters do not match. Maybe there are mandatory parameters missing.",
            actionName);
        return false;
      }
    } else {
      return false;
    }
  }

  private boolean matchesName(String actionName, MethodInterfaceDescription interfaceDescription) {
    return actionName.equals(interfaceDescription.getMethodName());
  }

  private boolean matchesParameter(ParameterList parametersOfRequest,
                                   List<InputParameterDescription> parametersDescription) {
    for (final InputParameterDescription parameterDescription : parametersDescription) {
      if (parameterDescription.isMandatory()
          && parametersOfRequest.getParameterForName(parameterDescription.getName()) == null) {
        return false;
      }
    }
    for (final Parameter<?> parameter : parametersOfRequest) {
      if (!this.listContainsParameterWithName(parameter.getName(), parametersDescription)) {
        return false;
      }
    }
    return true;
  }

  private boolean listContainsParameterWithName(String name,
                                                List<InputParameterDescription> parametersDescription) {
    for (final InputParameterDescription inputParameterDescription : parametersDescription) {
      if (name.equals(inputParameterDescription.getName())) {
        return true;
      }
    }
    return false;
  }

  protected Object callServiceMethod(final String methodName, final ParameterList parameters)
      throws InformationUndeterminableException {
    final Optional<MethodInterfaceDescription> interfaceDescriptionOptional = this
        .findInterfaceDescription(methodName, parameters);
    if (!interfaceDescriptionOptional.isPresent()) {
      throw new InformationUndeterminableException(
          String.format("No registered ServiceMethod found, methodName=%s, parameters=%s",
              methodName, parameters));
    }
    final MethodInterfaceDescription interfaceDescription = interfaceDescriptionOptional.get();
    final Method method = this.interfaceToMethod.get(interfaceDescription);
    if (method == null) {
      // happens when the service is removed in the short window between lookup
      // and retrieval
      throw new InformationUndeterminableException("Concurrent Deregistration");
    }
    final Object[] parameterArray = this.createParameterArray(interfaceDescription, parameters,
        method.getParameterTypes());

    try {
      return method.invoke(instanceToWrap, parameterArray);
    } catch (IllegalAccessException | InvocationTargetException e) {
      if (e.getCause() instanceof InformationUndeterminableException) {
        throw (InformationUndeterminableException) e.getCause();
      }
      throw new InformationUndeterminableException("Error calling method", e);
    }
  }

  private Object[] createParameterArray(MethodInterfaceDescription interfaceDescription,
                                        ParameterList parameterList, final Class<?>[] methodParameterTypes)
      throws InformationUndeterminableException {
    List<InputParameterDescription> parameters = interfaceDescription.getParameters();
    if (parameters == null) {
      parameters = Collections.emptyList();
    }
    final Object[] toReturn = new Object[parameters.size()];
    int i = 0;
    for (final InputParameterDescription parameterDescription : parameters) {
      final String name = parameterDescription.getName();
      final Parameter<?> parameterForName = parameterList.getParameterForName(name);
      final Class<?> methodParameterType = methodParameterTypes[i];
      if (parameterForName != null) {

        toReturn[i] = handleNumericParameter(parameterForName.getValue(), methodParameterType);
      } else {
        if (!parameterDescription.isMandatory()) {
          toReturn[i] = this.getDefaultValueForParameter(methodParameterTypes[i]);
        } else {
          throw new InformationUndeterminableException(
              "Mandatory Parameter " + name + " not provided in PipRequest");
        }
      }
      i++;
    }
    return toReturn;
  }

  private Object mapParameter(Number value, Class<?> typeOfParameter) {
    if (typeOfParameter.equals(Float.class)) {
      return value.floatValue();
    }
    if (typeOfParameter.equals(Double.class)) {
      return value.doubleValue();
    }
    if (typeOfParameter.equals(Short.class)) {
      return value.shortValue();
    }
    if (typeOfParameter.equals(Integer.class)) {
      return value.intValue();
    }
    if (typeOfParameter.equals(Long.class)) {
      return value.longValue();
    }
    return value;
  }

  /**
   * For non primitive parameterClass Method returns null. Otherwise it returns the default value:
   * for boolean false, int 0, ...
   *
   * @param parameterClass Type for the default value.
   * @return Default Value for the type.
   */
  private Object getDefaultValueForParameter(Class<?> parameterClass) {
    return Array.get(Array.newInstance(parameterClass, 1), 0);
  }

  private Object handleNumericParameter(Object value, Class<?> methodParameterType) {
    Class<?> typeOfParameter = methodParameterType;
    if (methodParameterType.isPrimitive()) {
      typeOfParameter = Primitives.wrap(methodParameterType);
    }
    if (Number.class.isAssignableFrom(typeOfParameter) && value instanceof Number
        && !value.getClass().isAssignableFrom(methodParameterType)) {
      return mapParameter((Number) value, typeOfParameter);
    } else {
      return value;
    }
  }

  @Override
  public HealthStatus getHealth() throws IOException {
    if (null != alreadyRightTypedReference) {
      return alreadyRightTypedReference.getHealth();
    } else {
      try {
        final Method getHealthMethod = instanceToWrap.getClass().getMethod("getHealth");
        return (HealthStatus) getHealthMethod.invoke(instanceToWrap);
      } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
        LOG.warn("Failed to invoke getHealth method for component of type {}",
            instanceToWrap.getClass().getCanonicalName(), e);
      }
      return HealthStatus.of(Status.UNKNOWN); // TODO check default
    }
  }

  @Override
  public ComponentId getId() throws IOException {
    if (null != alreadyRightTypedReference) {
      return alreadyRightTypedReference.getId();
    } else {
      try {
        final Method getIdMethod = instanceToWrap.getClass().getMethod("getId");
        return (ComponentId) getIdMethod.invoke(instanceToWrap);
      } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
        LOG.warn("Id method not accessible for component of type {}",
            instanceToWrap.getClass().getCanonicalName(), e);
      }
      return componentId;
    }
  }

  @Override
  public boolean reset() throws IOException, NoSuchEntityException {
    if (null != alreadyRightTypedReference) {
      return alreadyRightTypedReference.reset();
    } else {
      try {
        final Method resetMethod = instanceToWrap.getClass().getMethod("reset");
        return (boolean) resetMethod.invoke(instanceToWrap);
      } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
        LOG.warn("Reset method not accessible for component of type {}", instanceToWrap.getClass().getCanonicalName(), e);
      }
      return false;
    }
  }
}
