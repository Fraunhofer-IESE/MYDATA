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

package de.fraunhofer.iese.mydata.reactive;

import de.fraunhofer.iese.mydata.IMyDataEnvironment;
import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.ComponentType;
import de.fraunhofer.iese.mydata.component.information.method.InputParameterDescription;
import de.fraunhofer.iese.mydata.component.information.method.MethodInterfaceDescription;
import de.fraunhofer.iese.mydata.component.information.method.PepInterfaceDescription;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.pep.PolicyEnforcementPoint;
import de.fraunhofer.iese.mydata.pep.common.DecisionEnforcer;
import de.fraunhofer.iese.mydata.pep.common.ModifierMethod;
import de.fraunhofer.iese.mydata.pep.enforce.JsonPathDecisionEnforcer;
import de.fraunhofer.iese.mydata.policy.decision.AuthorizationDecision;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.reactive.common.EventParameter;
import de.fraunhofer.iese.mydata.reactive.common.EventSpecification;
import de.fraunhofer.iese.mydata.reactive.common.IncorrectPepDescriptionError;
import de.fraunhofer.iese.mydata.reactive.common.Modifiers;
import de.fraunhofer.iese.mydata.reactive.common.PepServiceDescription;
import de.fraunhofer.iese.mydata.reactive.common.PepType;
import de.fraunhofer.iese.mydata.reactive.common.RxPep;
import de.fraunhofer.iese.mydata.registry.ActionDescription;
import de.fraunhofer.iese.mydata.registry.ActionParameterDescription;
import de.fraunhofer.iese.mydata.solution.SolutionId;
import de.fraunhofer.iese.mydata.util.ModifierMethodDiscoveryUtil;

import io.reactivex.rxjava3.core.Observable;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Entry Point to define reactive Policy Enforcement Points. This Factory has the capability to
 * create Reactive Pep.
 */
public class RxPepFactory {

  private static final Logger LOG = LoggerFactory.getLogger(RxPepFactory.class);

  private RxPepFactory() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Creates a new ReactivePep object, takes the componentId from the @{@link PepServiceDescription}
   * annotation.
   *
   * @param  <T>                 the generic type
   * @param  myDataEnvironment   the IMyDataEnvironment the PEP belongs to
   * @param  decisionEnforcer    the decision enforcer
   * @param  pepDocumentationApi the pep documentation api
   * @return                     the rx Pep of type T
   */
  public static <T> RxPep<T> createRxPep(IMyDataEnvironment myDataEnvironment,
      DecisionEnforcer decisionEnforcer, final Class<T> pepDocumentationApi) {
    return createRxPep(myDataEnvironment, decisionEnforcer,
        getComponentId(myDataEnvironment.getSolutionId(), pepDocumentationApi),
        pepDocumentationApi);
  }

  /**
   * Creates a new ReactivePep object, takes the componentId from parameter
   *
   * @param  <T>                 the generic type
   * @param  myDataEnvironment   the IMyDataEnvironment the PEP belongs to
   * @param  decisionEnforcer    the decision enforcer
   * @param  componentId         the componentId of the PEP
   * @param  pepDocumentationApi the pep documentation api
   * @return                     the rx Pep of type T
   */
  // TODO checked vs unchecked exception?
  public static <T> RxPep<T> createRxPep(IMyDataEnvironment myDataEnvironment,
      DecisionEnforcer decisionEnforcer, final ComponentId componentId,
      final Class<T> pepDocumentationApi) {
    RxPepFactory.validateDocumentationApi(pepDocumentationApi);
    RxPepFactory.validateMethodReturnType(pepDocumentationApi);
    final Collection<ModifierMethod> allModifierMethods = getAllModifierActor(pepDocumentationApi);
    try {
      allModifierMethods.forEach(decisionEnforcer::addModificationMethod);
    } catch (final Exception e) {
      LOG.error("Error while adding modifiers", e);
      throw new IncorrectPepDescriptionError("Unknown Pep creation error happened!", e);
    }
    try {
      final SolutionId solutionId = SolutionId.fromComponentId(componentId);
      final PolicyEnforcementPoint policyEnforcementPoint = new PolicyEnforcementPoint(
          myDataEnvironment, decisionEnforcer, componentId,
          discoverPepDocumentationApi(pepDocumentationApi, solutionId),
          discoverModifierInterfaceDescription(pepDocumentationApi), false);
      return new ReactivePep<>(pepDocumentationApi, policyEnforcementPoint);
    } catch (final IOException io) {
      LOG.error("IOException must not happen!", io);
    } catch (final InvalidEntityException e) {
      throw new IncorrectPepDescriptionError("Pep not created because of an invalid entity", e);
    } catch (final NoSuchEntityException e) {
      LOG.error("Not found", e);
    }
    throw new IncorrectPepDescriptionError("Unknown Pep creation error happened!");
  }

  /**
   * Creates a new ReactivePep object, takes the componentId from the @{@link PepServiceDescription}
   * annotation.
   *
   * @param  <T>                 the generic type
   * @param  myDataEnvironment   the IMyDataEnvironment the PEP belongs to
   * @param  pepDocumentationApi the pep documentation api
   * @return                     the rx Pep of type T
   */
  public static <T> RxPep<T> createRxPep(IMyDataEnvironment myDataEnvironment,
      final Class<T> pepDocumentationApi) {
    return createRxPep(myDataEnvironment,
        getComponentId(myDataEnvironment.getSolutionId(), pepDocumentationApi),
        pepDocumentationApi);
  }

  /**
   * Creates a new ReactivePep object, takes the componentId from parameter
   *
   * @param  <T>                 the generic type
   * @param  myDataEnvironment   the IMyDataEnvironment the PEP belongs to
   * @param  componentId         the componentId of the PEP
   * @param  pepDocumentationApi the pep documentation api
   * @return                     the rx Pep of type T
   */
  public static <T> RxPep<T> createRxPep(IMyDataEnvironment myDataEnvironment,
      final ComponentId componentId, final Class<T> pepDocumentationApi) {
    final JsonPathDecisionEnforcer jsonPathDecisionEnforcer = new JsonPathDecisionEnforcer();
    return createRxPep(myDataEnvironment, jsonPathDecisionEnforcer, componentId,
        pepDocumentationApi);
  }

  /**
   * Creates an input parameter description.
   *
   * @param  parameterType the parameter type
   * @param  annotations   the annotations
   * @return               the input parameter description
   */
  private static InputParameterDescription createInputParameterDescription(Class<?> parameterType,
      Annotation[] annotations) {
    final ActionParameterDescription annotation = getParameterDescriptionAnnotation(annotations);

    if (annotation != null) {
      final String parameterName = annotation.name();
      final String description = annotation.description();
      return new InputParameterDescription(parameterName, description, annotation.mandatory(),
          parameterType);
    }
    // TODO don't check validity of the parameter annotation. there is no
    // constraints regarding it.
    return null;
  }

  /**
   * Discover modifier interface description.
   *
   * @param  <T>    the generic type
   * @param  tClass the t class
   * @return        the list
   */
  private static <T> List<MethodInterfaceDescription> discoverModifierInterfaceDescription(
      final Class<T> tClass) {
    final List<MethodInterfaceDescription> modifierInterfaceDescriptions = new ArrayList<>();
    final Annotation[] requiredModifier = tClass.getAnnotationsByType(Modifiers.class);
    if (requiredModifier.length > 0) {
      // get all modifier required for the event
      final Set<Class<? extends ModifierMethod>> modifierClasses = getModifiersFromAnnotation(
          requiredModifier[0]);
      // final Class<? extends ModifierMethod>[] modifierClasses =
      // ((Modifiers)requiredModifier[0]).className();
      for (final Class<? extends ModifierMethod> modifierClass : modifierClasses) {
        final Method[] modifierMethods = modifierClass.getDeclaredMethods();
        for (final Method modifierMethod : modifierMethods) {
          // if method is annotated with ActionDescription then read action
          // name and description
          if (modifierMethod.isAnnotationPresent(ActionDescription.class)) {
            // get the ActionDescription of the method
            final ActionDescription actionDescription = modifierMethod
                .getAnnotation(ActionDescription.class);
            // take the method name is name is not specified with
            // ActionDescription
            final String modifierName = readModifierName(modifierMethod, actionDescription);
            // read modifier successfully so there is no problem
            final List<InputParameterDescription> modifierInputParameter = readModifierParameter(
                modifierMethod);
            // get the return type of the method
            final Class<?> returnType = actionDescription.pepSupportedType() == Void.class
                ? modifierMethod.getReturnType()
                : actionDescription.pepSupportedType();
            // read the description
            final String modifierDescription = actionDescription.description();
            // add the PepInterfaceDescription to interfaceDescriptions
            final MethodInterfaceDescription newDescription = new MethodInterfaceDescription(
                modifierName, returnType, modifierDescription, modifierInputParameter);
            modifierInterfaceDescriptions.add(newDescription);
          }
        }
      }
    }
    return modifierInterfaceDescriptions;
  }

  /**
   * Discovers Pep interface description.
   *
   * @param  <T>                 the generic type
   * @param  pepDocumentationApi the pep documentation API
   * @return                     the list of interface description
   */
  private static <T> List<PepInterfaceDescription> discoverPepDocumentationApi(
      final Class<T> pepDocumentationApi, final SolutionId solutionId)
      throws InvalidEntityException {
    // get all the method of the API documentation interface
    final Method[] methods = pepDocumentationApi.getDeclaredMethods();
    // list of Pep interface description
    final List<PepInterfaceDescription> interfaceDescriptions = new ArrayList<>();
    // iterate over all methods
    for (final Method method : methods) {
      // if method is
      final Annotation[] eventDescriptionAnnotations = method
          .getAnnotationsByType(EventSpecification.class);
      if (eventDescriptionAnnotations.length == 0) {
        continue;
      }
      final EventSpecification eventDescriptionForMethod = (EventSpecification) eventDescriptionAnnotations[0];
      // create the action id for the event
      final String eventName = StringUtils.isNotBlank(eventDescriptionForMethod.action())
          ? eventDescriptionForMethod.action()
          : method.getName();
      final String actionIdAsString = ComponentType.PEP
          .getIdentifierForInterfaceDescription(solutionId, eventName);
      final ActionId actionId = new ActionId(actionIdAsString);
      // get all the modifier required for event
      final PepInterfaceDescription pepInterfaceDescription = new PepInterfaceDescription(actionId,
          true, eventDescriptionForMethod.description(), readEventParameterDetails(method));
      checkDuplicateEventRegistration(interfaceDescriptions, pepInterfaceDescription);
      interfaceDescriptions.add(pepInterfaceDescription);
    }
    return interfaceDescriptions;
  }

  private static void checkDuplicateEventRegistration(
      List<PepInterfaceDescription> interfaceDescriptions,
      PepInterfaceDescription pepInterfaceDescription) {

    final List<PepInterfaceDescription> duplicateInterfaceDescriptions = interfaceDescriptions
        .stream().filter(existingDescription -> existingDescription.getEvent()
            .equals(pepInterfaceDescription.getEvent()))
        .toList();
    if (!duplicateInterfaceDescriptions.isEmpty()) {
      throw new IncorrectPepDescriptionError(
          "The event " + pepInterfaceDescription.getEvent() + " for more then one enforcements.");
    }
  }

  /**
   * Filters annotations of type given annotation class.
   *
   * @param annotations the annotations
   * @return the annotation of type EventParameter or it returns NULL if
   * there is no such.
   */
  private static EventParameter findEventParameterAnnotation(Annotation[] annotations) {
    for (final Annotation annotation : annotations) {
      if (annotation instanceof EventParameter eventParameterAnnotation) {
        return eventParameterAnnotation;
      }
    }
    return null;
  }

  /**
   * This method determines whether the documentation API is Valid or not
   *
   * @param  pepDocumentationApi interface of documentation API
   * @param  <T>                 the generic type
   * @return                     PepType
   */
  public static <T> PepType findAPIDocumentationType(final Class<T> pepDocumentationApi) {
    if (Boolean.TRUE.equals(isValidDocumentation(pepDocumentationApi).getKey())) {
      return PepType.REACTIVE;
    }
    return PepType.INVALID;
  }

  /**
   * Gets all modifier actors.
   *
   * @param  pepDocumentationApi the Pep documentation API
   * @param  <T>                 the generic type
   * @return                     a collection of modifier actors
   */
  private static <T> Collection<ModifierMethod> getAllModifierActor(
      final Class<T> pepDocumentationApi) {
    // TODO test it
    final Map<String, ModifierMethod> toReturn = readModifierNewInstanceFromApiDocumentation(
        pepDocumentationApi);
    final Annotation[] requiredModifier = pepDocumentationApi.getAnnotationsByType(Modifiers.class);
    if (requiredModifier.length > 0) {

      final Set<Class<? extends ModifierMethod>> modifierActors = getModifiersFromAnnotation(
          requiredModifier[0]);

      // final Class<? extends ModifierMethod>[] modifierActors =
      // ((Modifiers)requiredModifier[0]).className();
      for (final Class<? extends ModifierMethod> modifierActor : modifierActors) {
        if (!toReturn.containsKey(modifierActor.getCanonicalName())) {
          try {
            toReturn.put(modifierActor.getCanonicalName(),
                modifierActor.getDeclaredConstructor().newInstance());
          } catch (InstantiationException | IllegalAccessException | NoSuchMethodException
              | InvocationTargetException e) {
            throw new IncorrectPepDescriptionError(modifierActor.getCanonicalName()
                + "does not have default contractor nor supplied with static method in Pep API documentation interface",
                e);
          }
        }
      }
    }
    return toReturn.values();
  }

  // TODO method is called multiple times for one PEP: DecisionEnforcer-modifier
  // add + PEP's modifierInterfaceDescription, let's reuse the results and
  // improve performance
  private static Set<Class<? extends ModifierMethod>> getModifiersFromAnnotation(
      Annotation annotation) {
    final Set<Class<? extends ModifierMethod>> modifierActors = new HashSet<>();

    if (((Modifiers) annotation).classNames().length > 0) {
      modifierActors.addAll(Arrays.asList(((Modifiers) annotation).classNames()));
    }
    if (((Modifiers) annotation).packageNames().length > 0) {
      final Set<String> packageNames = new HashSet<>(Arrays.asList(((Modifiers) annotation).packageNames()));
      modifierActors.addAll(ModifierMethodDiscoveryUtil.findInPackages(packageNames));
    }
    // When there is no parameter set, add all modifiers of all packages
    if (((Modifiers) annotation).classNames().length == 0
        && ((Modifiers) annotation).packageNames().length == 0) {
      modifierActors.addAll(ModifierMethodDiscoveryUtil.findAll());
    }

    return modifierActors;
  }


  /**
   * Gets the component ID from the PepServiceDescription annotation included in the given Pep
   * documentation API.
   *
   * @param  <T>                          the generic type
   * @param  solutionId                   the solutionId of the solution the PEP will belong to
   * @param  pepDocumentationApi          the Pep documentation API
   * @return                              the component ID
   * @throws IncorrectPepDescriptionError if there is no valid @PepServiceDescription annotation or
   *                                        the passed solutionId is invalid
   */
  public static <T> ComponentId getComponentId(SolutionId solutionId,
      final Class<T> pepDocumentationApi) throws IncorrectPepDescriptionError {
    final Annotation[] classAnnotations = pepDocumentationApi
        .getAnnotationsByType(PepServiceDescription.class);
    if (classAnnotations.length == 0) {
      throw new IncorrectPepDescriptionError("@PepServiceDescription annotation missing");
    }
    final String componentName = ((PepServiceDescription) classAnnotations[0]).componentName();
    try {
      return ComponentType.PEP.getComponentId(solutionId, componentName);
    } catch (final Exception e) {
      throw new IncorrectPepDescriptionError(e.getMessage(), e);
    }

  }

  /**
   * Gets the parameter description annotation.
   *
   * @param  annotations the annotations
   * @return             the parameter description annotation
   */
  private static ActionParameterDescription getParameterDescriptionAnnotation(
      Annotation[] annotations) {
    for (final Annotation annotation : annotations) {
      if (annotation instanceof ActionParameterDescription) {
        return (ActionParameterDescription) annotation;
      }
    }
    return null;
  }

  /***
   * Checks whether documentation API is valid.
   *
   * @param  <T>                 the generic type
   * @param  pepDocumentationApi documentation API interface
   * @return                     if correct or incorrect and reason as exception
   */
  private static <T> Pair<Boolean, RuntimeException> isValidDocumentation(
      final Class<T> pepDocumentationApi) {
    final Method[] pepDocumentationApiMethods = pepDocumentationApi.getDeclaredMethods();
    for (final Method pepDocumentationApiMethod : pepDocumentationApiMethods) {
      if (java.lang.reflect.Modifier.isStatic(pepDocumentationApiMethod.getModifiers())) {
        continue;
      }
      final Type returnType = pepDocumentationApiMethod.getGenericReturnType();
      if (pepDocumentationApiMethod.getReturnType() == Observable.class
          && returnType instanceof ParameterizedType
          && pepDocumentationApiMethod.isAnnotationPresent(EventSpecification.class)) {
        final Type[] observableType = ((ParameterizedType) returnType).getActualTypeArguments();
        if (observableType[0] == Event.class) {
          //noinspection UnnecessaryContinue
          continue;
        } else if (observableType[0] instanceof ParameterizedType
            && ((ParameterizedType) observableType[0]).getRawType() == Pair.class
            && ((ParameterizedType) observableType[0]).getActualTypeArguments().length == 2
            && ((ParameterizedType) observableType[0]).getActualTypeArguments()[0] == Event.class
            && ((ParameterizedType) observableType[0])
                .getActualTypeArguments()[1] == AuthorizationDecision.class) {
          //noinspection UnnecessaryContinue
          continue;
        } else {
          return new ImmutablePair<>(false, new IncorrectPepDescriptionError(
              "Observable only can have Event or Pair<Event,AuthorizationDecision>"));
        }
      } else {
        return new ImmutablePair<>(false, new IncorrectPepDescriptionError(
            "Pep can't have void/anything else method, All methods should return ModifierMethod or Observable"));
      }
    }
    return new ImmutablePair<>(true, null);
  }

  /**
   * Filters parameters of type PepParamKey from the given method and creates the input parameter
   * descriptions.
   *
   * @param  method the modifier
   * @return        the list of input parameter descriptions
   */
  private static List<InputParameterDescription> readEventParameterDetails(Method method) {
    final List<InputParameterDescription> inputParameterDescriptions = new ArrayList<>();
    final Parameter[] parameters = method.getParameters();
    final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
    for (int i = 0; i < parameterAnnotations.length; i++) {
      final EventParameter pepParamKey = findEventParameterAnnotation(parameterAnnotations[i]);
      if (pepParamKey != null) {
        inputParameterDescriptions
            .add(new InputParameterDescription(pepParamKey.name(), pepParamKey.description(), true,
                parameters[i].getParameterizedType(), parameters[i].getType()));
      }
    }
    return inputParameterDescriptions;
  }

  /**
   * Gets modifier actors with their corresponding names from documentation API.
   *
   * @param  <T>                 the generic type
   * @param  pepDocumentationApi the Pep documentation API
   * @return                     a map of modifier actors and their corresponding names
   */
  private static <T> Map<String, ModifierMethod> readModifierNewInstanceFromApiDocumentation(
      final Class<T> pepDocumentationApi) {
    final Method[] allMethod = pepDocumentationApi.getDeclaredMethods();
    final Map<String, ModifierMethod> toReturn = new HashMap<>();
    for (final Method method : allMethod) {
      if (java.lang.reflect.Modifier.isStatic(method.getModifiers())
          && ModifierMethod.class.isAssignableFrom(method.getReturnType())) {
        try {
          final ModifierMethod modifierActor = (ModifierMethod) method.invoke(null);
          toReturn.put(method.getReturnType().getCanonicalName(), modifierActor);
        } catch (IllegalAccessException | InvocationTargetException e) {
          throw new IncorrectPepDescriptionError(
              "method to supply ModifierActor should not have any arguments.", e);
        }
      }
    }
    return toReturn;
  }

  /**
   * Reads method parameters.
   *
   * @param  method the modifier actor
   * @return        the list of input parameter description
   */
  private static List<InputParameterDescription> readModifierParameter(Method method) {
    final Class<?>[] parameters = method.getParameterTypes();
    final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
    final List<InputParameterDescription> toReturn = new ArrayList<>();
    for (int i = 0; i < parameters.length; i++) {
      final InputParameterDescription inputParameterDescription = createInputParameterDescription(
          parameters[i], parameterAnnotations[i]);
      if (inputParameterDescription != null) {
        toReturn.add(inputParameterDescription);
      }
    }
    return toReturn;
  }

  /**
   * Returns method name.
   *
   * @param  method            the modifier actor
   * @param  actionDescription the action description
   * @return                   method name
   */
  private static String readModifierName(Method method, ActionDescription actionDescription) {
    return StringUtils.isNotBlank(actionDescription.methodName()) ? actionDescription.methodName()
        : method.getName();
  }

  /**
   * Validates that the Pep documentation API is an interface and does not extend other interfaces.
   *
   * @param <T>                 the generic type
   * @param pepDocumentationApi the Pep documentation API
   */
  private static <T> void validateDocumentationApi(final Class<T> pepDocumentationApi) {
    if (!pepDocumentationApi.isInterface()) {
      throw new IllegalArgumentException("Reactive Pep API documentation must be interfaces.");
    }
    if (pepDocumentationApi.getInterfaces().length > 0) {
      throw new IllegalArgumentException(
          "Reactive Pep API documentation interfaces must not extend other interfaces.");
    }
  }

  /**
   * Validates method return type for reactive Pep.
   * </p>
   * Enforcement method must have observable event or observable pair of event and authorization
   * decision to get the response from PDP to use ReactivePep
   *
   * @param <T>                 the generic type
   * @param pepDocumentationApi the pep documentation api
   */
  private static <T> void validateMethodReturnType(final Class<T> pepDocumentationApi) {
    final Pair<Boolean, RuntimeException> booleanRuntimeExceptionPair = isValidDocumentation(
        pepDocumentationApi);
    if (Boolean.FALSE.equals(booleanRuntimeExceptionPair.getKey())) {
      throw booleanRuntimeExceptionPair.getValue();
    }
  }

}
