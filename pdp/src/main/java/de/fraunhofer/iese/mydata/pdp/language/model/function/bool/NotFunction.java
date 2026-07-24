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
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;

/**
 * The Class NotOperator.
 */
public class NotFunction extends Function {

  /**
   * The default constructor.
   */
  public NotFunction() {
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.pdp.java.language.model.operator.Operator#
   * evaluate(de.fraunhofer.iese.mydata.api.policy.Event)
   */
  @SuppressWarnings("unchecked")
  @Override
  public DataObject<Boolean> evaluate(Event evt) throws EvaluationUndecidableException {
    if (this.getParameters() == null || this.getParameters().isEmpty() || this.getParameters().size() > 1) {
      throw new EvaluationUndecidableException("Not must have exactly one child");
    }
    final DataObject<?> evalResult = this.getParameters().get(0).evaluate(evt);

    if (evalResult.getType() != boolean.class && evalResult.getType() != Boolean.class) {
      throw new IllegalArgumentException();
    }

    return new DataObject<>(!((DataObject<Boolean>)evalResult).getValue());
  }
}
