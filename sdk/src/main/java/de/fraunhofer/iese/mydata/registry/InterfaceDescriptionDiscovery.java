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

package de.fraunhofer.iese.mydata.registry;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.component.ComponentType;
import de.fraunhofer.iese.mydata.component.information.method.InputParameterDescription;
import de.fraunhofer.iese.mydata.component.information.method.MethodInterfaceDescription;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates a List of {@link MethodInterfaceDescription} by introspecting a class. Therefor the
 * discovery lists all methods that is annotated with an Annotation of Type
 * {@link ActionDescription} and creates an instance of {@link MethodInterfaceDescription} with the
 * information provided by the MethodSignature, the annotation and the
 * {@link ActionParameterDescription} annotations of each method parameter. For Example The Method
 *
 * <pre>
 *
 *
 * {@literal @}ActionDescription(description = "Retreives the authority (role) of an user")
 *  public String getAuthority(@ActionParameterDescription(name = "username", description = "The user the authority should be evaluated for.", mandatory = true) String username) {
 *  }
 * </pre>
 * <p>
 * Results in an InterfaceDescription with:
 * <ul>
 * <li>name = getAuthority</li>
 * <li>description = Retreives the authority (role) of an user</li>
 * <li>parameter =
 * <ul>
 * <li>InputParameterDescription: name = username, description = The user the authority should be
 * evaluated for, mandatory = true</li>
 * </ul>
 * </li>
 * </ul>
 */
public class InterfaceDescriptionDiscovery {

  /**
   * Compose interface description.
   *
   * @param  method            the method
   * @param  actionDescription the action description
   * @param  name              the name
   * @param  type              the componentType
   * @param  solutionId        the solution id
   * @return                   the interface description
   */
  private MethodInterfaceDescription composeInterfaceDescription(Method method,
      ActionDescription actionDescription, String name, ComponentType type, SolutionId solutionId)
      throws InvalidEntityException {
    return new MethodInterfaceDescription(
        type.getIdentifierForInterfaceDescription(solutionId, name), method.getReturnType(),
        actionDescription.description(), this.readParameter(method));
  }

  /**
   * Creates the input parameter description.
   *
   * @param  parameterType the parameter type
   * @param  annotations   the annotations
   * @return               the input parameter description
   */
  private InputParameterDescription createInputParameterDescription(Class<?> parameterType,
      Annotation[] annotations) throws InvalidEntityException {
    final ActionParameterDescription annotation = this
        .getParameterDescriptionAnnotation(annotations);

    if (null != annotation) {
      final String parameterName = annotation.name();
      final String description = annotation.description();
      parameterType = annotation.type().equals(Void.class) ? parameterType : annotation.type();
      return new InputParameterDescription(parameterName, description, annotation.mandatory(),
          parameterType);
    } else {
      throw new InvalidEntityException("Not all parameters are documented.");
    }
  }

  /**
   * Creates the interface description from service method.
   *
   * @param  method     the method
   * @param  type       the componentType
   * @param  solutionId the solutionId
   * @return            the map< interface description,? extends pair< method, object>>
   */
  private Map<MethodInterfaceDescription, Method> createInterfaceDescriptionFromServiceMethod(
      Method method, ComponentType type, SolutionId solutionId) throws InvalidEntityException {
    final ActionDescription actionDescription = method.getAnnotation(ActionDescription.class);
    final String name = this.readName(method, actionDescription);
    return Collections.singletonMap(
        this.composeInterfaceDescription(method, actionDescription, name, type, solutionId),
        method);
  }

  /**
   * Discover.
   *
   * @param  serviceClass           the class of the service
   * @param  type                   the type
   * @param  solutionId             the solution id
   * @return                        the map
   * @throws InvalidEntityException when solutionId is invalid or when the serviceClass is not a
   *                                  valid MyData Component (e.g. when not every parameter of an
   *                                  action is annotated)
   */
  public Map<MethodInterfaceDescription, Method> discover(Class<?> serviceClass, ComponentType type,
      SolutionId solutionId) throws InvalidEntityException {
    MyDataEntity.validateAndNullCheck(solutionId);
    final Map<MethodInterfaceDescription, Method> toReturn = new HashMap<>();
    for (final Method method : serviceClass.getMethods()) {
      if (this.isServiceMethod(method)) {
        toReturn.putAll(this.createInterfaceDescriptionFromServiceMethod(method, type, solutionId));
      }
    }
    return toReturn;
  }

  /**
   * Gets the parameter description annotation.
   *
   * @param  annotations the annotations
   * @return             the parameter description annotation
   */
  private ActionParameterDescription getParameterDescriptionAnnotation(Annotation[] annotations) {
    for (final Annotation annotation : annotations) {
      if (annotation instanceof ActionParameterDescription) {
        return (ActionParameterDescription) annotation;
      }
    }
    return null;
  }

  /**
   * Checks if is service method.
   *
   * @param  method the method
   * @return        true, if is service method
   */
  private boolean isServiceMethod(Method method) {
    return method.isAnnotationPresent(ActionDescription.class);
  }

  /**
   * Read name.
   *
   * @param  method            the method
   * @param  actionDescription the action description
   * @return                   the string
   */
  private String readName(Method method, ActionDescription actionDescription) {
    return StringUtils.isNotBlank(actionDescription.methodName()) ? actionDescription.methodName()
        : method.getName();
  }

  /**
   * Read parameter.
   *
   * @param  method the method
   * @return        the list
   */
  private List<InputParameterDescription> readParameter(Method method)
      throws InvalidEntityException {
    final Class<?>[] parameters = method.getParameterTypes();
    final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
    final List<InputParameterDescription> toReturn = new ArrayList<>();
    for (int i = 0; i < parameters.length; i++) {
      final InputParameterDescription inputParameterDescription = this
          .createInputParameterDescription(parameters[i], parameterAnnotations[i]);
      toReturn.add(inputParameterDescription);
    }
    return toReturn;
  }
}
