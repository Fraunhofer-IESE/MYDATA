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

@SuppressWarnings("javadoc")
@XmlRootElement(name = "eventOccurrence", namespace = "http://www.mydata-control.de/4.0/mydataLanguage")
@Getter
public class EventOccurrence extends DataObject<Long> implements IFunction {

  /**
   *
   */
  private static final long serialVersionUID = 9000732024882901419L;

  /**
   * The event name of the eventOccurrence
   */
  private String event;

  /**
   * The mode of the eventOccurrence, only usefull for time relative to event occurrence
   */
  private String mode;

  //  /**
  //   * The function and parameters.
  //   */
  //  private List<PolicyParameter<?>> parameters;

  /**
   * The sub operators.
   */
  private List<IFunction> subOperators;

  @XmlAttribute
  public String getEvent() {
    return this.event;
  }

  public void setEvent(String event) {
    this.event = event;
  }

  @XmlAttribute
  public String getMode() {
    return this.mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  @XmlElements({
      @XmlElement(name = "string", namespace = "http://www.mydata-control.de/4.0/parameter", type = StringParameter.class),
      @XmlElement(name = "object", namespace = "http://www.mydata-control.de/4.0/parameter", type = ObjectParameter.class),
      @XmlElement(name = "list", namespace = "http://www.mydata-control.de/4.0/parameter", type = ListParameter.class),
      @XmlElement(name = "boolean", namespace = "http://www.mydata-control.de/4.0/parameter", type = BooleanParameter.class),
      @XmlElement(name = "number", namespace = "http://www.mydata-control.de/4.0/parameter", type = NumberParameter.class)
  })
  public List<IFunction> getSubOperators() {
    return this.subOperators;
  }

  public void setSubOperators(List<IFunction> subOperators) {
    this.subOperators = subOperators;
  }

  @Override
  public DataObject<?> evaluate(Event evt) throws EvaluationUndecidableException {
    return this;
  }

  @Override
  public void accept(PolicyVisitor policyVisitor) {
    policyVisitor.visit(this);
  }

  @Override
  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }

}
