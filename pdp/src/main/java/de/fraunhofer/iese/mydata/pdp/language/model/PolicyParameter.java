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

import de.fraunhofer.iese.mydata.pdp.language.model.function.Function;
import de.fraunhofer.iese.mydata.pdp.language.model.function.IFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.AndFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.BooleanFunction;
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
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.PipOperator;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.PipOperatorBoolean;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.PipOperatorList;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.PipOperatorNumber;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.PipOperatorString;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.RegexFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.TimeFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.ValueChangedFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.XorFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.number.AddFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.number.CountFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.number.DivFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.number.MinusFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.number.MultFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.number.SizeFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.string.ConcatFunction;
import de.fraunhofer.iese.mydata.pdp.language.parser.PolicyVisitor;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class Parameter.
 *
 * @param <T> the generic type
 */
@XmlRootElement(namespace = "http://www.mydata-control.de/4.0/parameter")
@Getter
public class PolicyParameter<T> extends PolicyConstant<T> {

  /**
   * The logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PolicyParameter.class);

  /**
   * The Constant serialVersionUID.
   */
  private static final long serialVersionUID = 4955861931044219927L;

  /**
   * The name.
   */
  private String name;

  /**
   * The functions and parameters.
   */
  // why transient?
  @XmlTransient
  private List<IFunction> functionsAndParameters;

  private String jsonPathQuery;

  /**
   * Used for JAXB
   */
  public PolicyParameter() {
  }

  /**
   * Instantiates a new parameter.
   *
   * @param name  the name
   * @param value the value
   */
  public PolicyParameter(String name, T value) {
    super(value);
    this.setName(name);
    this.setFunctionsAndParameters(new ArrayList<IFunction>());
  }

  /**
   * Instantiates a new parameter.
   *
   * @param name  the name
   * @param clazz the clazz
   */
  public PolicyParameter(String name, Class<T> clazz) {
    super(clazz);
    this.setName(name);
    this.setFunctionsAndParameters(new ArrayList<IFunction>());
  }

  /**
   * Instantiates a new parameter.
   *
   * @param name  the name
   * @param value the value
   * @param clazz the clazz
   */
  public PolicyParameter(String name, T value, Class<T> clazz) {
    super(value, clazz);
    this.setName(name);
    this.setFunctionsAndParameters(new ArrayList<IFunction>());
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.pdp.java.language.model.Constant#evaluate(de.
   * fraunhofer.iese.mydata.internal.policy.Event)
   */
  @Override
  public DataObject<T> evaluate(Event evt) throws EvaluationUndecidableException {
    LOG.debug("Entering evaluate(name={}, event={})", this.getName(), evt);

    DataObject<T> result = null;

    if (this.getValue() != null) {
      LOG.debug("Leaving evaluate(): constant value {}", this);
      result = this;

    } else if (this.functionsAndParameters != null && !this.functionsAndParameters.isEmpty()) {
      result = this.evaluateChild(evt);

    }
    if (result == null) {
      result = new DataObject<>();
    }
    return result;
  }

  /**
   * Retrieves the parameter value from child elements.
   *
   * @param  evt                            the event
   * @return                                the value of the parameter with the given name
   * @throws EvaluationUndecidableException if the evaluation of the child element fails
   */
  @SuppressWarnings({
      "rawtypes", "unchecked"
  })
  private DataObject<T> evaluateChild(Event evt) throws EvaluationUndecidableException {
    LOG.debug("Parameter has no value, but child element");
    final DataObject res = this.functionsAndParameters.get(0).evaluate(evt);
    LOG.debug("Leaving evaluate(): {}", res);
    return res;
  }

  /**
   * Gets the functions and parameters.
   *
   * @return the functionsAndParameters
   */
  @XmlElements({
      @XmlElement(type = PipOperator.class),
      @XmlElement(name = "string", namespace = "http://www.mydata-control.de/4.0/constant", type = StringPolicyConstant.class),
      @XmlElement(name = "number", namespace = "http://www.mydata-control.de/4.0/constant", type = NumberPolicyConstant.class),
      @XmlElement(name = "object", namespace = "http://www.mydata-control.de/4.0/constant", type = ObjectPolicyConstant.class),
      @XmlElement(name = "boolean", namespace = "http://www.mydata-control.de/4.0/constant", type = BooleanPolicyConstant.class),
      @XmlElement(name = "list", namespace = "http://www.mydata-control.de/4.0/constant", type = ListPolicyConstant.class),
      @XmlElement(name = "true", namespace = "http://www.mydata-control.de/4.0/constant", type = TruePolicyConstant.class),
      @XmlElement(name = "false", namespace = "http://www.mydata-control.de/4.0/constant", type = FalsePolicyConstant.class),
      @XmlElement(type = PolicyParameter.class),
      @XmlElement(name = "string", namespace = "http://www.mydata-control.de/4.0/variable", type = VariableReference.class),
      @XmlElement(name = "boolean", namespace = "http://www.mydata-control.de/4.0/variable", type = VariableReference.class),
      @XmlElement(name = "number", namespace = "http://www.mydata-control.de/4.0/variable", type = VariableReference.class),
      @XmlElement(name = "object", namespace = "http://www.mydata-control.de/4.0/variable", type = VariableReference.class),
      @XmlElement(name = "list", namespace = "http://www.mydata-control.de/4.0/variable", type = VariableReference.class),
      @XmlElement(type = PolicyEvent.class),
      @XmlElement(name = "string", namespace = "http://www.mydata-control.de/4.0/event", type = StringEvent.class),

      @XmlElement(name = "list", namespace = "http://www.mydata-control.de/4.0/event", type = ListEvent.class),
      @XmlElement(name = "boolean", namespace = "http://www.mydata-control.de/4.0/event", type = BooleanEvent.class),
      @XmlElement(name = "number", namespace = "http://www.mydata-control.de/4.0/event", type = NumberEvent.class),
      // @XmlElement(name = "list", namespace =
      // "http://www.mydata-control.de/4.0/event", type =
      // PolicyEvent.class),
      @XmlElement(name = "object", namespace = "http://www.mydata-control.de/4.0/event", type = ObjectEvent.class),
      @XmlElement(type = PipOperator.class),
      @XmlElement(name = "string", namespace = "http://www.mydata-control.de/4.0/pip", type = PipOperatorString.class),
      @XmlElement(name = "number", namespace = "http://www.mydata-control.de/4.0/pip", type = PipOperatorNumber.class),
      @XmlElement(name = "object", namespace = "http://www.mydata-control.de/4.0/pip", type = PipOperator.class),
      @XmlElement(name = "boolean", namespace = "http://www.mydata-control.de/4.0/pip", type = PipOperatorBoolean.class),
      @XmlElement(type = BooleanFunction.class),
      @XmlElement(name = "list", namespace = "http://www.mydata-control.de/4.0/pip", type = PipOperatorList.class),
      @XmlElement(type = BooleanFunction.class),
      @XmlElement(name = "eventOccurrence", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = EventOccurrence.class),
      @XmlElement(name = "when", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = When.class),
      @XmlElement(name = "start", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = Start.class),

      @XmlElement(name = "equals", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = EqualsFunction.class),
      @XmlElement(name = "plus", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = AddFunction.class),
      @XmlElement(name = "divide", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = DivFunction.class),
      @XmlElement(name = "multiply", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = MultFunction.class),
      @XmlElement(name = "minus", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = MinusFunction.class),
      @XmlElement(name = "size", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = SizeFunction.class),
      @XmlElement(name = "count", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = CountFunction.class),
      @XmlElement(name = "concat", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = ConcatFunction.class),
      @XmlElement(name = "continuousOccurrence", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = ContinuousOccurrenceFunction.class),

      @XmlElement(name = "or", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = OrFunction.class),
      @XmlElement(name = "and", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = AndFunction.class),
      @XmlElement(name = "boolean", namespace = "http://www.mydata-control.de/4.0/parameter", type = BooleanParameter.class),
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
      @XmlElement(name = "xor", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = XorFunction.class),
      @XmlElement(name = "and", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = AndFunction.class),
      @XmlElement(type = Function.class)
  })
  public List<IFunction> getFunctionsAndParameters() {
    return this.functionsAndParameters;
  }

  /**
   * Sets the functions and parameters.
   *
   * @param functionsAndParameters the functionsAndParameters to set
   */
  public void setFunctionsAndParameters(List<IFunction> functionsAndParameters) {
    this.functionsAndParameters = functionsAndParameters;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  @Override
  @XmlAttribute
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

  /*
   * (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result
        + ((this.functionsAndParameters == null) ? 0 : this.functionsAndParameters.hashCode());
    result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    @SuppressWarnings("rawtypes")
    final PolicyParameter other = (PolicyParameter) obj;
    if (this.functionsAndParameters == null) {
      if (other.functionsAndParameters != null) {
        return false;
      }
    } else if (!this.functionsAndParameters.equals(other.functionsAndParameters)) {
      return false;
    }
    if (this.name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!this.name.equals(other.name)) {
      return false;
    }
    return true;
  }

  @Override
  public void accept(PolicyVisitor policyVisitor) {
    policyVisitor.visit(this);
  }

  /**
   * @return the jsonPathQuery Expression
   */
  @XmlAttribute()
  public String getJsonPathQuery() {
    return this.jsonPathQuery;
  }

  /**
   * @param expression the jsonPathQuery expression to set
   */
  public void setJsonPathQuery(String expression) {
    this.jsonPathQuery = expression;
  }

}
