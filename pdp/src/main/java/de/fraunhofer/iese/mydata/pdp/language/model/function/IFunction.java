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

import de.fraunhofer.iese.mydata.pdp.language.model.PolicyConstant;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyEvent;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyParameter;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.BooleanFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.PipOperator;
import de.fraunhofer.iese.mydata.pdp.language.parser.PolicyVisitor;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;

import jakarta.xml.bind.annotation.XmlSeeAlso;

/**
 * Any kind of element in the policy condition has to implement the IOperator Interface.
 */
@XmlSeeAlso({
    PolicyConstant.class, PolicyParameter.class, PolicyEvent.class, BooleanFunction.class,
    PipOperator.class, Function.class
})
public interface IFunction {

  /**
   * Gets the return type of the element.
   *
   * @return                          the type
   * @throws IllegalArgumentException the illegal argument exception
   */
  Class<?> getType();

  /**
   * Evaluates the operator, returning a {@link DataObject} of the specified type.
   *
   * @param  evt                            the event to evaluate
   * @return                                the data object containing the result of the evaluation
   * @throws EvaluationUndecidableException if the given data is insufficient for evaluation
   */
  DataObject<?> evaluate(Event evt) throws EvaluationUndecidableException;

  /**
   * Visitor that walks over policy structure.
   * 
   * @param policyVisitor
   */
  void accept(PolicyVisitor policyVisitor);

  /**
   * @return the name of the element, needed for event occurrences
   */
  String getName();

}
