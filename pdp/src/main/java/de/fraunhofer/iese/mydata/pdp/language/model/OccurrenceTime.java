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

package de.fraunhofer.iese.mydata.pdp.language.model;

import de.fraunhofer.iese.mydata.pdp.language.model.function.IFunction;
import de.fraunhofer.iese.mydata.pdp.language.parser.PolicyVisitor;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Handle the time of an event occurrence
 *
 * @param <T>
 */
@Getter
@Setter
public class OccurrenceTime<T> extends DataObject<T> implements IFunction {

  /**
   *
   */
  private static final long serialVersionUID = 3253035044006382575L;

  private EventOccurrence eventOccurrence;

  /**
   * The event name of the eventOccurrence
   */
  private String time;

  /**
   * The sub operators.
   */
  private List<IFunction> subOperators;

  /**
   * @return the eo
   */
  public EventOccurrence getEventOccurrence() {
    return this.eventOccurrence;
  }

  /**
   * @param eo the eo to set
   */
  public void setEventOccurrence(EventOccurrence eo) {
    this.eventOccurrence = eo;
  }

  /**
   * @return the list of suboperators based on the list of XMLelements declared
   */
  @XmlElements({
      @XmlElement(name = "eventOccurrence", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = EventOccurrence.class),
  })
  public List<IFunction> getSubOperators() {
    return this.subOperators;
  }

  /**
   * @param subOperators
   */
  public void setSubOperators(List<IFunction> subOperators) {
    this.subOperators = subOperators;
  }

  @Override
  public DataObject<?> evaluate(Event evt) throws EvaluationUndecidableException {
    if (this.subOperators != null) {
      for (final IFunction op : this.subOperators) {
        final DataObject<?> evalResult = op.evaluate(evt);
        if (evalResult instanceof EventOccurrence) {
          this.eventOccurrence = (EventOccurrence) evalResult;
        }
      }
    }
    return this;
  }

  @Override
  public void accept(PolicyVisitor policyVisitor) {

  }

  @Override
  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }

}
