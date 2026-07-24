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
import de.fraunhofer.iese.mydata.pdp.language.model.function.EnumTimeExpression;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;
import de.fraunhofer.iese.mydata.policy.time.TimeUtil;

import jakarta.xml.bind.annotation.XmlAttribute;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@SuppressWarnings("javadoc")
public class DateFunction extends BooleanFunction {

  private String value;

  private String is;

  boolean valid = false;

  public DateFunction() {
    super();
  }

  public DateFunction(String is, String value) {
    super();
    this.is = is;
    this.value = value;
  }

  @Override
  public DataObject<Boolean> evaluate(Event evt) throws EvaluationUndecidableException {

    try {
      // we set the time (hour, minute, second) to zero as it does not play any
      // role in the comparison.
      final ZoneId zId = evt.getZoneId();
      final Instant instant = Instant.ofEpochMilli(evt.getMillisecondSinceEpoch());
      final ZonedDateTime zdt = instant.atZone(zId);

      final ZonedDateTime occurredAt = ZonedDateTime.of(zdt.getYear(), zdt.getMonthValue(),
          zdt.getDayOfMonth(), 0, 0, 0, 0, zId);

      final String givenDateString = this.value + " " + "00:00:00";
      final ZonedDateTime givenDate = TimeUtil.getZonedDateTime(givenDateString, zId);

      if (this.is.equalsIgnoreCase(EnumTimeExpression.BEFORE.value())) {
        this.valid = occurredAt.isBefore(givenDate);
      } else if (this.is.equalsIgnoreCase(EnumTimeExpression.EXACTLY.value())) {
        this.valid = occurredAt.isEqual(givenDate);
      } else if (this.is.equalsIgnoreCase(EnumTimeExpression.AFTER.value())) {
        this.valid = occurredAt.isAfter(givenDate);
      }

      if (this.valid) {
        return PolicyConstant.TRUE;
      } else {
        return PolicyConstant.FALSE;
      }
    } catch (final Exception e) {
      throw new EvaluationUndecidableException("Error during date function evaluation", e);
    }

  }

  @XmlAttribute(name = "is")
  public String getIs() {
    return this.is;
  }

  public void setIs(String str) {
    this.is = str;
  }

  @XmlAttribute(name = "value")
  public String getValue() {
    return this.value;
  }

  public void setValue(String value) {
    this.value = value;
  }

}
