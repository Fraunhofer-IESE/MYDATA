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

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;

import java.util.List;

@XmlRootElement(name = "when", namespace = "http://www.mydata-control.de/4.0/mydataLanguage")
@Getter
public class When<T> extends DataObject<T> implements IFunction {

  /**
   *
   */
  private static final long serialVersionUID = 8968895952696864849L;

  /**
   * The event name of the eventOccurrence
   */
  private String fixedTime;

  private Start<?> start;

  private End<?> end;

  /**
   * The sub operators.
   */
  private List<IFunction> subOperators;

  /**
   * @return the start
   */
  public Start<?> getStart() {
    return this.start;
  }

  /**
   * @param start the start to set
   */
  public void setStart(Start<?> start) {
    this.start = start;
  }

  /**
   * @return the end
   */
  public End<?> getEnd() {
    return this.end;
  }

  /**
   * @param end the end to set
   */
  public void setEnd(End<?> end) {
    this.end = end;
  }

  @XmlAttribute
  public String getFixedTime() {
    if (this.fixedTime == null || this.fixedTime.isEmpty()) {
      return null;
    }
    return this.fixedTime;
  }

  public void setFixedTime(String fixedTime) {
    this.fixedTime = fixedTime;
  }

  @Override
  public Class<?> getType() {
    return String.class;
  }

  @XmlElements({
      @XmlElement(name = "start", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = Start.class),
      @XmlElement(name = "end", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = End.class),
  })
  public List<IFunction> getSubOperators() {
    return this.subOperators;
  }

  public void setSubOperators(List<IFunction> subOperators) {
    this.subOperators = subOperators;
  }

  @Override
  public DataObject<?> evaluate(Event evt) throws EvaluationUndecidableException {
    if (this.subOperators != null) {
      for (final IFunction op : this.subOperators) {
        final DataObject<?> evalResult = op.evaluate(evt);
        if (evalResult instanceof Start) {
          this.start = (Start<?>) evalResult;
        } else if (evalResult instanceof End) {
          this.end = (End<?>) evalResult;
        }
      }
    }
    return this;
  }

  @Override
  public void accept(PolicyVisitor policyVisitor) {
    // TODO Auto-generated method stub

  }

  @Override
  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }

}
