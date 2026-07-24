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

package de.fraunhofer.iese.mydata.pdp.language.model.function.bool;

import de.fraunhofer.iese.mydata.pdp.language.model.PolicyConstant;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;

import jakarta.xml.bind.annotation.XmlAttribute;
import org.apache.commons.lang3.StringUtils;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZonedDateTime;

@SuppressWarnings("javadoc")
public class DayFunction extends BooleanFunction {

  private String value;

  public DayFunction() {
    super();
  }

  public DayFunction(String value) {
    super();
    this.value = value;
  }

  @XmlAttribute(name = "value")
  public String getValue() {
    return this.value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public DataObject<Boolean> evaluate(Event evt) throws EvaluationUndecidableException {
    final Instant instant = Instant.ofEpochMilli(evt.getMillisecondSinceEpoch());
    final ZonedDateTime occurredAtZone = instant.atZone(evt.getZoneId());

    final DayOfWeek dayAtZone = occurredAtZone.getDayOfWeek();
    if (!StringUtils.isEmpty(this.value)
        && this.value.toUpperCase().contains(dayAtZone.name().toUpperCase())) {
      return PolicyConstant.TRUE;
    }

    return PolicyConstant.FALSE;
  }

}
