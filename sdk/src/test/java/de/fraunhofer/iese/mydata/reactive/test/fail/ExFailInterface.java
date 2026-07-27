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

package de.fraunhofer.iese.mydata.reactive.test.fail;

import de.fraunhofer.iese.mydata.User;
import de.fraunhofer.iese.mydata.pep.modifiers.string.AnagramModifierMethod;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.reactive.common.EventParameter;
import de.fraunhofer.iese.mydata.reactive.common.EventSpecification;
import de.fraunhofer.iese.mydata.reactive.common.Modifiers;
import de.fraunhofer.iese.mydata.reactive.common.PepServiceDescription;
import de.fraunhofer.iese.mydata.reactive.test.Address;

import io.reactivex.rxjava3.core.Observable;

@PepServiceDescription(componentName = "smartsite")
@Modifiers(classNames = {
    AnagramModifierMethod.class
})
public interface ExFailInterface {

  @EventSpecification(action = "show-user")
  Observable<Event> enforceForSmartsiteUserShow(@EventParameter(name = "user") User user,
      @EventParameter(name = "address") Address address);

  @EventSpecification(action = "show-project")
  Observable<Event> enforceForSmartsiteProjectShow(@EventParameter(name = "user") User user);

  @EventSpecification(action = "dont-show-project")
  void enforceForSmartsiteProjectDontShow(@EventParameter(name = "user") User user);

}
