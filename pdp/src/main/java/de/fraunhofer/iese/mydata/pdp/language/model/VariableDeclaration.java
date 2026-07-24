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
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.EqualsFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.ExistsFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.OrFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.PipOperator;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.PipOperatorBoolean;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.PipOperatorList;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.PipOperatorNumber;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.PipOperatorString;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.RegexFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.XorFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.number.CountFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.string.ConcatFunction;
import de.fraunhofer.iese.mydata.pdp.language.parser.PolicyVisitor;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement()
public class VariableDeclaration {

  private String name;

  private IFunction function;

  private final ThreadLocal<DataObject<?>> cache = new ThreadLocal<>();

  @XmlAttribute
  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @XmlElements({
      @XmlElement(name = "string", namespace = "http://www.mydata-control.de/4.0/pip", type = PipOperatorString.class),
      @XmlElement(name = "number", namespace = "http://www.mydata-control.de/4.0/pip", type = PipOperatorNumber.class),
      @XmlElement(name = "object", namespace = "http://www.mydata-control.de/4.0/pip", type = PipOperator.class),
      @XmlElement(name = "boolean", namespace = "http://www.mydata-control.de/4.0/pip", type = PipOperatorBoolean.class),
      @XmlElement(name = "list", namespace = "http://www.mydata-control.de/4.0/pip", type = PipOperatorList.class),
      @XmlElement(name = "string", namespace = "http://www.mydata-control.de/4.0/constant", type = StringPolicyConstant.class),
      @XmlElement(name = "number", namespace = "http://www.mydata-control.de/4.0/constant", type = NumberPolicyConstant.class),
      @XmlElement(name = "object", namespace = "http://www.mydata-control.de/4.0/constant", type = ObjectPolicyConstant.class),
      @XmlElement(name = "boolean", namespace = "http://www.mydata-control.de/4.0/constant", type = BooleanPolicyConstant.class),
      @XmlElement(name = "list", namespace = "http://www.mydata-control.de/4.0/constant", type = ListPolicyConstant.class),
      @XmlElement(name = "true", namespace = "http://www.mydata-control.de/4.0/constant", type = TruePolicyConstant.class),
      @XmlElement(name = "false", namespace = "http://www.mydata-control.de/4.0/constant", type = FalsePolicyConstant.class),
      @XmlElement(type = PolicyParameter.class), @XmlElement(type = PolicyEvent.class),
      @XmlElement(name = "list", namespace = "http://www.mydata-control.de/4.0/event", type = ListEvent.class),
      @XmlElement(name = "string", namespace = "http://www.mydata-control.de/4.0/event", type = StringEvent.class),
      @XmlElement(name = "boolean", namespace = "http://www.mydata-control.de/4.0/event", type = BooleanEvent.class),
      @XmlElement(name = "number", namespace = "http://www.mydata-control.de/4.0/event", type = NumberEvent.class),
      // @XmlElement(name = "list", namespace =
      // "http://www.mydata-control.de/4.0/event", type =
      // PolicyEvent.class),
      @XmlElement(name = "object", namespace = "http://www.mydata-control.de/4.0/event", type = ObjectEvent.class),

      @XmlElement(type = BooleanFunction.class),

      @XmlElement(name = "exists", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = ExistsFunction.class),
      @XmlElement(name = "equals", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = EqualsFunction.class),
      @XmlElement(type = PipOperator.class),
      @XmlElement(name = "or", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = OrFunction.class),
      @XmlElement(name = "xor", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = XorFunction.class),
      @XmlElement(name = "and", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = AndFunction.class),
      @XmlElement(type = Function.class),
      @XmlElement(name = "count", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = CountFunction.class),
      @XmlElement(name = "concat", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = ConcatFunction.class),
      @XmlElement(name = "regex", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", type = RegexFunction.class)
  })
  public IFunction getFunction() {
    return this.function;
  }

  public void setFunction(IFunction function) {
    this.function = function;
  }

  public void accept(PolicyVisitor visitor) {
    visitor.visit(this);
  }

  public DataObject<?> evaluate(Event evt) throws EvaluationUndecidableException {

    if (this.cache.get() == null) {
      this.cache.set(this.function.evaluate(evt));
    }
    return this.cache.get();
  }

  public void reset() {
    this.cache.remove();
  }
}
