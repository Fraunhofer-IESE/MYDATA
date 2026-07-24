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

import de.fraunhofer.iese.mydata.component.ComponentType;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyEnforcementPoint;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;
import de.fraunhofer.iese.mydata.reactive.common.EventParameter;
import de.fraunhofer.iese.mydata.reactive.common.EventSpecification;
import de.fraunhofer.iese.mydata.reactive.common.IncorrectPepDescriptionError;
import de.fraunhofer.iese.mydata.reactive.common.RxPep;
import de.fraunhofer.iese.mydata.reactive.common.RxPepState;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Base Implementation of a RxPep.
 *
 * @param <T>
 */
public abstract class AbstractRxPep<T> implements RxPep<T> {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractRxPep.class);

  /**
   * The pep state.
   */
  protected final AtomicReference<RxPepState> pepState = new AtomicReference<>(
      RxPepState.REGISTRATION_NOT_STARTED);

  /**
   * The policy enforcement point.
   */
  protected final IPolicyEnforcementPoint policyEnforcementPoint;

  /**
   * The documentation API.
   */
  protected final Class<T> pepInterfaceDescription;

  /**
   * Instantiates a new Rx Pep.
   *
   * @param pepInterfaceDescription the documentation API
   * @param policyEnforcementPoint  the policy enforcement point
   */
  public AbstractRxPep(IPolicyEnforcementPoint policyEnforcementPoint,
      Class<T> pepInterfaceDescription) {
    this.policyEnforcementPoint = policyEnforcementPoint;
    this.pepInterfaceDescription = pepInterfaceDescription;
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.reactive.common.RxPep#createInstanceAPI()
   */
  @Override
  @SuppressWarnings("unchecked")
  public T createInstanceAPI() {
    if (this.pepState.get() == RxPepState.REGISTRATION_DONE_SUCCESSFULLY) {
      return (T) Proxy.newProxyInstance(this.pepInterfaceDescription.getClassLoader(),
          new Class<?>[] {
              this.pepInterfaceDescription
          }, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

              // pass to object class if method belongs to Object class
              if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
              }
              return AbstractRxPep.this.enforceDecision(method, args);

            }
          });
    }
    throw new IllegalStateException(
        "RxPep is not registered yet, please perform successful registration before using documentation API");
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.reactive.common.RxPep#doRegisterAtPMP()
   */
  @Override
  public Observable<Boolean> doRegisterAtPMP() {
    return Observable.fromCallable(() -> {
      switch (this.pepState.get()) {
        case REGISTRATION_DONE_SUCCESSFULLY:
          return false;
        case REGISTRATION_FAILED:
          return false;
        case REGISTRATION_NOT_STARTED:
          this.pepState.set(RxPepState.REGISTRATION_UNDER_PROCESS);
          return this.policyEnforcementPoint.initialize();
        case REGISTRATION_UNDER_PROCESS:
          throw new IllegalStateException(
              "Pep under registration, don't try to register when it's already under the process");
        default:
          throw new IllegalStateException("WTF state");
      }
    }).doOnError((e) -> {
      LOG.error("Registration of Pep at PMP failed!", e);
      this.pepState.set(RxPepState.REGISTRATION_FAILED);
    }).doOnNext(aBoolean -> {
      if (this.pepState.get() != RxPepState.REGISTRATION_DONE_SUCCESSFULLY) {
        if (aBoolean) {
          this.pepState.set(RxPepState.REGISTRATION_DONE_SUCCESSFULLY);
          this.postSuccessfulRegistration();
        } else {
          this.pepState.set(RxPepState.REGISTRATION_FAILED);
        }
      }
    }).subscribeOn(Schedulers.trampoline());
  }

  protected abstract Object enforceDecision(Method method, Object[] args);

  /**
   * Returns the parameter of type PepParamKey.
   *
   * @param  annotations the annotations
   * @return             the parameter annotation
   */
  EventParameter getParameterAnnotation(Annotation[] annotations) {
    for (final Annotation annotation : annotations) {
      if (annotation instanceof EventParameter) {
        return (EventParameter) annotation;
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.reactive.common.RxPep#getPolicyEnforcementPoint( )
   */
  @Override
  public IPolicyEnforcementPoint getPolicyEnforcementPoint() {
    if (this.getState() != RxPepState.REGISTRATION_DONE_SUCCESSFULLY) {
      throw new IllegalStateException("Enforcement point is not yet registered at PMP");
    }
    return this.policyEnforcementPoint;
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.reactive.common.RxPep#getState()
   */
  @Override
  public RxPepState getState() {
    return this.pepState.get();
  }

  protected abstract void postSuccessfulRegistration();

  /**
   * Gets the action id from the given method and creates a new event with specific UUID and
   * parameters set.
   *
   * @param  method                       the method
   * @param  methodArguments              the method arguments
   * @return                              the event
   * @throws IncorrectPepDescriptionError the incorrect Pep description error
   */
  protected Event readAndCreateEvent(Method method, Object[] methodArguments)
      throws IncorrectPepDescriptionError {
    final ActionId actionId = this.readEventDescription(method);
    final Event event = new Event(actionId);
    final ParameterList params = this.readParameterList(method.getParameterTypes(),
        method.getParameterAnnotations(), methodArguments);
    event.setParameters(params);
    return event;
  }

  /**
   * Gets the platform, context and action values from the event specification of the given method
   * and creates the action id and returns it.
   *
   * @param  method the method
   * @return        the action id
   */
  protected ActionId readEventDescription(Method method) {
    final Annotation[] methodAnnotation = method
        .getDeclaredAnnotationsByType(EventSpecification.class);
    if (methodAnnotation.length > 0) {
      try {
        final SolutionId solutionId = SolutionId
            .fromComponentId(this.policyEnforcementPoint.getId());
        final EventSpecification ed = (EventSpecification) methodAnnotation[0];
        final String eventName = StringUtils.isNotBlank(ed.action()) ? ed.action()
            : method.getName();
        final String actionIdAsString = ComponentType.PEP
            .getIdentifierForInterfaceDescription(solutionId, eventName);
        return new ActionId(actionIdAsString);
      } catch (InvalidEntityException | IOException e) {
        LOG.error(e.getMessage(), e);
      }
    }
    LOG.error("API declaration error: ActionId can't be null for event.");
    throw new IncorrectPepDescriptionError(
        "ActionId can't be null for event. API declaration error");
  }

  /**
   * Returns the annotations of type PepParamKey with their corresponding method argument.
   *
   * @param  parametersType        parameter type
   * @param  parametersAnnotations the parameters annotations
   * @param  methodArguments       the method arguments
   * @return                       the parameter list
   */
  protected ParameterList readParameterList(Class<?>[] parametersType,
      Annotation[][] parametersAnnotations, Object[] methodArguments) {
    final ParameterList parameters = new ParameterList();
    for (int i = 0; i < parametersAnnotations.length; i++) {
      final EventParameter pepParamKey = this.getParameterAnnotation(parametersAnnotations[i]);
      if (pepParamKey != null) {
        parameters.addParameter(pepParamKey.name(), methodArguments[i], parametersType[i]);
      }
    }
    return parameters;
  }

}
