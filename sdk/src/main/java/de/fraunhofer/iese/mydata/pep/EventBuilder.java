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

import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;

import java.lang.reflect.Type;
import java.time.Instant;

public class EventBuilder {

  private final ActionId actionId;

  private final ParameterList parameterList = new ParameterList();

  private Instant timeStamp = null;

  /**
   * Instantiates a new event builder for given action id
   *
   * @param actionId the action id of the event to be created
   */
  public EventBuilder(ActionId actionId) {
    this.actionId = actionId;
  }

  public EventBuilder withParameter(String key, Object value, Type valueType) {
    final Parameter<?> param = new Parameter<>(key, value, valueType);
    this.parameterList.add(param);
    return this;
  }

  /**
   * Generates an event
   *
   * @return {@link Event}
   */
  public Event getEvent() {
    if (this.timeStamp == null) {
      this.timeStamp = Instant.now();
    }
    return new Event(this.actionId, this.timeStamp, this.parameterList);
  }
}
