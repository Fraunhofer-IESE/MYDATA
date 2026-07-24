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

package de.fraunhofer.iese.mydata.pdp.language.model.function;

import de.fraunhofer.iese.mydata.pdp.language.model.BooleanEvent;
import de.fraunhofer.iese.mydata.pdp.language.model.BooleanParameter;
import de.fraunhofer.iese.mydata.pdp.language.model.BooleanPolicyConstant;
import de.fraunhofer.iese.mydata.pdp.language.model.EventOccurrence;
import de.fraunhofer.iese.mydata.pdp.language.model.FalsePolicyConstant;
import de.fraunhofer.iese.mydata.pdp.language.model.ListEvent;
import de.fraunhofer.iese.mydata.pdp.language.model.ListPolicyConstant;
import de.fraunhofer.iese.mydata.pdp.language.model.NumberEvent;
import de.fraunhofer.iese.mydata.pdp.language.model.NumberPolicyConstant;
import de.fraunhofer.iese.mydata.pdp.language.model.ObjectEvent;
import de.fraunhofer.iese.mydata.pdp.language.model.ObjectPolicyConstant;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyEvent;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyParameter;
import de.fraunhofer.iese.mydata.pdp.language.model.Start;
import de.fraunhofer.iese.mydata.pdp.language.model.StringEvent;
import de.fraunhofer.iese.mydata.pdp.language.model.StringPolicyConstant;
import de.fraunhofer.iese.mydata.pdp.language.model.TruePolicyConstant;
import de.fraunhofer.iese.mydata.pdp.language.model.VariableReference;
import de.fraunhofer.iese.mydata.pdp.language.model.When;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.AndFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.BooleanFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.ContainsFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.ContinuousOccurrenceFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.DateFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.DayFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.EqualsFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.EventMatchOperator;
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

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * The Class Function.
 */
@XmlRootElement(namespace = "http://www.mydata-control.de/4.0/mydataLanguage")
@XmlSeeAlso({
    AndFunction.class, CountFunction.class, ImpliesFunction.class, EventMatchOperator.class,
    SizeFunction.class, OrFunction.class, ConcatFunction.class, BooleanFunction.class,
    NotFunction.class, XorFunction.class
})
public abstract class Function implements IFunction {

  /**
   * The name.
   */
  private String name;

  /**
   * The parameters.
   */
  protected List<IFunction> parameters;

  /**
   * The return type.
   */
  protected Class<?> returnType;

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(Function.class);

  /**
   * Instantiates a new function.
   */
  public Function() {
    this(0, null);
  }

  /**
   * Instantiates a new function.
   *
   * @param capacity   the capacity
   * @param returnType the return type
   */
  public Function(int capacity, Class<?> returnType) {
    this.setParameters(new ArrayList<>(capacity));
    this.returnType = returnType;
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.pdp.java.language.model.IOperator#getType()
   */
  @Override
  public Class<?> getType() {
    return this.returnType;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  @Override
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

  /**
   * Gets the parameters.
   *
   * @return the parameters
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
      // try to fix list contains
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
      @XmlElement(name = "continuousOccurrence", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = ContinuousOccurrenceFunction.class),
      @XmlElement(name = "concat", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = ConcatFunction.class),

      @XmlElement(name = "and", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = AndFunction.class),
      @XmlElement(name = "or", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = OrFunction.class),
      @XmlElement(name = "xor", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = XorFunction.class),
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
      @XmlElement(type = Function.class)
  })
  public List<IFunction> getParameters() {
    return this.parameters;
  }

  /**
   * Sets the parameters.
   *
   * @param parameters the parameters to set
   */
  public void setParameters(List<IFunction> parameters) {
    this.parameters = parameters;
  }

  /**
   * Adds the parameter.
   *
   * @param operator the operator
   */
  public void addParameter(IFunction operator) {
    if (this.parameters == null) {
      this.parameters = new ArrayList<>();
    }
    this.parameters.add(operator);
  }

  @Override
  public void accept(PolicyVisitor policyVisitor) {
    policyVisitor.visit(this);
  }

  protected String getProperty(String key) {
    final Properties prop = new Properties();
    InputStream input = null;

    final String filename = "application.properties";
    input = this.getClass().getClassLoader().getResourceAsStream(filename);
    if (input == null) {
      LOG.warn("Could not find application properties file {}", filename);
      return "UTC";
    }

    // load a properties file from class path, inside static method
    try {
      prop.load(input);
    } catch (final IOException e) {
      LOG.warn("Could not parse the properties file because of {}", e.getMessage(), e);
    }
    return prop.getProperty(key, "UTC");
  }
}
