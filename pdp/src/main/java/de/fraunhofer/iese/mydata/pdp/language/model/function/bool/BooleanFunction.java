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

import de.fraunhofer.iese.mydata.pdp.language.model.function.Function;
import de.fraunhofer.iese.mydata.pdp.language.model.function.IFunction;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSeeAlso;

/**
 * The abstract Class BooleanFunction is the base class for all functions that return a boolean
 * value.
 */
@XmlRootElement(namespace = "http://www.mydata-control.de/4.0/mydataLanguage")
@XmlSeeAlso({
    ContainsFunction.class, RegexFunction.class, EqualsFunction.class, ExistsFunction.class,
    ArithmeticFunction.class
})
public abstract class BooleanFunction extends Function implements IFunction {

  /**
   * Instantiates a new boolean function.
   */
  public BooleanFunction() {
    this.returnType = Boolean.class;
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.pdp.java.language.model.IOperator#evaluate(de.
   * fraunhofer.iese.mydata.internal.policy.Event)
   */
  @Override
  public abstract DataObject<Boolean> evaluate(Event evt) throws EvaluationUndecidableException;

}
