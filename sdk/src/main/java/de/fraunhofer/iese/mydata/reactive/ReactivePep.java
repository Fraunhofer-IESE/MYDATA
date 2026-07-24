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

import de.fraunhofer.iese.mydata.component.interfaces.IPolicyEnforcementPoint;
import de.fraunhofer.iese.mydata.policy.decision.AuthorizationDecision;
import de.fraunhofer.iese.mydata.policy.event.Event;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.annotation.concurrent.ThreadSafe;

/**
 * The Class RxPep is one implementation for Reactive Pep.
 *
 * @param <T> the generic type
 */
@ThreadSafe
public class ReactivePep<T> extends AbstractRxPep<T> {

  /**
   * Instantiates a new rx Pep.
   *
   * @param descriptionInterface   the interface description
   * @param policyEnforcementPoint the policy enforcement point
   */
  public ReactivePep(Class<T> descriptionInterface,
      IPolicyEnforcementPoint policyEnforcementPoint) {
    super(policyEnforcementPoint, descriptionInterface);
  }

  @Override
  protected Object enforceDecision(Method method, Object[] args) {
    // take the return type of the method as it can be Observable<Event>
    // or Observable<AuthorizationDecision>
    final Type returnType = method.getGenericReturnType();
    // check if it has type Observable && ParameterizedType.class.i
    if (method.getReturnType() == Observable.class && returnType instanceof ParameterizedType) {
      final Type genericType = ((ParameterizedType) returnType).getActualTypeArguments()[0];
      if (genericType == Event.class) {
        return ReactivePep.this.enforceMethodForEvent(method, args);
      } else if (genericType instanceof ParameterizedType
          && ((ParameterizedType) genericType).getRawType() == Pair.class
          && ((ParameterizedType) genericType).getActualTypeArguments().length == 2
          && ((ParameterizedType) genericType).getActualTypeArguments()[0] == Event.class
          && ((ParameterizedType) genericType)
              .getActualTypeArguments()[1] == AuthorizationDecision.class) {
        return ReactivePep.this.getAuthorizationDecisionforEvent(method, args);
      }
    }
    throw new IllegalStateException("Unknown error happened due to illegal declaration in ");
  }

  /**
   * Enforce method for event.
   *
   * @param  method  the method
   * @param  objects the objects
   * @return         the observable
   */
  private Observable<Event> enforceMethodForEvent(final Method method, final Object[] objects) {

    return Observable.fromCallable(() -> {
      final Event event = ReactivePep.this.readAndCreateEvent(method, objects);
      ReactivePep.this.policyEnforcementPoint.enforce(event);
      return event;
    }).subscribeOn(Schedulers.io());
  }

  /**
   * Gets authorization decision for event.
   *
   * @param  method  the method
   * @param  objects the method arguments
   * @return         the events and authorization decisions
   */
  private Observable<ImmutablePair<Event, AuthorizationDecision>> getAuthorizationDecisionforEvent(
      Method method, Object[] objects) {
    return Observable.fromCallable(() -> {
      final Event event = this.readAndCreateEvent(method, objects);
      return new ImmutablePair<>(event, this.policyEnforcementPoint.getDecision(event));
    }).subscribeOn(Schedulers.io());
  }

  @Override
  protected void postSuccessfulRegistration() {
    // Intended blank
  }
}
