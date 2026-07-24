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

package de.fraunhofer.iese.mydata.reactive.test.happy;

import de.fraunhofer.iese.mydata.JavaNonParameterizedType;
import de.fraunhofer.iese.mydata.JavaTimeObjects;
import de.fraunhofer.iese.mydata.User;
import de.fraunhofer.iese.mydata.pep.modifiers.basic.ReplaceModifierMethod;
import de.fraunhofer.iese.mydata.pep.modifiers.string.AnagramModifierMethod;
import de.fraunhofer.iese.mydata.pep.modifiers.string.AppendModifierMethod;
import de.fraunhofer.iese.mydata.policy.decision.AuthorizationDecision;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.reactive.common.EventParameter;
import de.fraunhofer.iese.mydata.reactive.common.EventSpecification;
import de.fraunhofer.iese.mydata.reactive.common.Modifiers;
import de.fraunhofer.iese.mydata.reactive.common.PepServiceDescription;
import de.fraunhofer.iese.mydata.reactive.test.Address;

import io.reactivex.rxjava3.core.Observable;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

@PepServiceDescription(componentName = "cs4")
// only the three modifiers Append, Replace and Anagram must be added
@Modifiers(classNames = {
    AppendModifierMethod.class, ReplaceModifierMethod.class, AnagramModifierMethod.class
})
public interface MyRxPepDocumentationAPI {

  @EventSpecification(action = "show-user")
  Observable<Event> enforceForCSUserShow(@EventParameter(name = "user") List<User> user,
      @EventParameter(name = "address") Address address);

  @EventSpecification(action = "show-project")
  Observable<Event> enforceForCSProjectShow(@EventParameter(name = "user") User user);

  @EventSpecification(action = "dont-show-project")
  Observable<Pair<Event, AuthorizationDecision>> enforceForCSProjectDontShow(
      @EventParameter(name = "user") User user);

  @EventSpecification(action = "show-time-objects")
  Observable<Event> enforceTimeObjects(
      @EventParameter(name = "timeObject") JavaTimeObjects timeObject);

  @EventSpecification(action = "show-NonParameterized-objects2")
  Observable<Event> enforceNonParameterizedObject(
      @EventParameter(name = "nonParameterizedObject2") ArrayList<?> object);

  @EventSpecification(action = "show-NonParameterized-objects")
  Observable<Event> enforceNonParameterizedObjectInherit(
      @EventParameter(name = "nonParameterizedObject") JavaNonParameterizedType object);

  static AnagramModifierMethod getAnagramModifierActor() {
    return new AnagramModifierMethod();
  }

}
