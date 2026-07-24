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
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.AndFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.ContainsFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.ContinuousOccurrenceFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.DateFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.DayFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.EqualsFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.ExecuteFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.ExistsFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.GreaterEqualFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.GreaterFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.ImpliesFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.LessEqualFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.LessFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.NotFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.OrFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.PipOperatorBoolean;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.RegexFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.TimeFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.ValueChangedFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.XorFunction;
import de.fraunhofer.iese.mydata.pdp.language.parser.PolicyVisitor;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * The Class Condition.
 */
@XmlRootElement(namespace = "http://www.mydata-control.de/4.0/mydataLanguage")
public class PolicyIfBlock {

  /**
   * The operator.
   */
  private IFunction operator;

  /**
   * The decision to draw when the operator is fulfilled
   */
  private PolicyDecision decision;

  /**
   * The name.
   */
  private String name;

  /**
   * The default constructor.
   */
  public PolicyIfBlock() {
    super();
  }

  /**
   * Evaluate.
   *
   * @param  evt                            the evt
   * @return                                true, if successful
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   */
  public PolicyDecision evaluate(Event evt) throws EvaluationUndecidableException {
    if ((Boolean) this.operator.evaluate(evt).getValue()) {
      return this.decision;
    }
    return null;
  }

  /**
   * Gets the operator.
   *
   * @return the operator
   */
  @XmlElements({
      @XmlElement(name = "true", namespace = "http://www.mydata-control.de/4.0/constant", type = TruePolicyConstant.class),
      @XmlElement(name = "false", namespace = "http://www.mydata-control.de/4.0/constant", type = FalsePolicyConstant.class),
      @XmlElement(name = "boolean", namespace = "http://www.mydata-control.de/4.0/parameter", type = BooleanParameter.class),
      // @XmlElement(name = "boolean", namespace =
      // "http://www.mydata-control.de/4.0/event", type =
      // PolicyEvent.class),
      @XmlElement(name = "boolean", namespace = "http://www.mydata-control.de/4.0/event", type = BooleanEvent.class),
      @XmlElement(name = "boolean", namespace = "http://www.mydata-control.de/4.0/pip", type = PipOperatorBoolean.class),
      @XmlElement(name = "boolean", namespace = "http://www.mydata-control.de/4.0/variable", type = VariableReference.class),
      @XmlElement(name = "execute", type = ExecuteFunction.class, namespace = "http://www.mydata-control.de/4.0/mydataLanguage"),
      @XmlElement(name = "equals", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = EqualsFunction.class),
      @XmlElement(name = "implies", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = ImpliesFunction.class),
      @XmlElement(name = "not", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = NotFunction.class),
      @XmlElement(name = "less", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = LessFunction.class),
      @XmlElement(name = "lessEqual", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = LessEqualFunction.class),
      @XmlElement(name = "greater", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = GreaterFunction.class),
      @XmlElement(name = "greaterEqual", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = GreaterEqualFunction.class),
      @XmlElement(name = "eventHasParameter", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = ExistsFunction.class),
      @XmlElement(name = "regex", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = RegexFunction.class),
      @XmlElement(name = "contains", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = ContainsFunction.class),
      @XmlElement(name = "string", namespace = "http://www.mydata-control.de/4.0/valueChanged", type = ValueChangedFunction.class),
      @XmlElement(name = "number", namespace = "http://www.mydata-control.de/4.0/valueChanged", type = ValueChangedFunction.class),
      @XmlElement(name = "boolean", namespace = "http://www.mydata-control.de/4.0/valueChanged", type = ValueChangedFunction.class),
      @XmlElement(name = "list", namespace = "http://www.mydata-control.de/4.0/valueChanged", type = ValueChangedFunction.class),
      @XmlElement(name = "object", namespace = "http://www.mydata-control.de/4.0/valueChanged", type = ValueChangedFunction.class),
      @XmlElement(name = "valueUnchanged", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = ValueChangedFunction.class),
      @XmlElement(name = "date", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = DateFunction.class),
      @XmlElement(name = "time", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = TimeFunction.class),
      @XmlElement(name = "day", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = DayFunction.class),
      @XmlElement(name = "or", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = OrFunction.class),
      @XmlElement(name = "xor", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = XorFunction.class),
      @XmlElement(name = "and", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = AndFunction.class),
      @XmlElement(name = "continuousOccurrence", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = ContinuousOccurrenceFunction.class),
  })
  public IFunction getOperator() {
    return this.operator;
  }

  /**
   * Sets the operator.
   *
   * @param operator the operator to set
   */
  public void setOperator(IFunction operator) {
    this.operator = operator;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  /**
   * Sets the name.
   *
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  @XmlElement(name = "then", namespace = "http://www.mydata-control.de/4.0/mydataLanguage")
  public PolicyDecision getDecision() {
    return this.decision;
  }

  public void setDecision(PolicyDecision decision) {
    this.decision = decision;
  }

  public void accept(PolicyVisitor policyVisitor) {

    policyVisitor.visit(this);
  }

}
