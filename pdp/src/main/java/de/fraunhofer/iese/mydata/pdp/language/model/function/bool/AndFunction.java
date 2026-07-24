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

import de.fraunhofer.iese.mydata.pdp.language.model.PolicyConstant;
import de.fraunhofer.iese.mydata.pdp.language.model.function.Function;
import de.fraunhofer.iese.mydata.pdp.language.model.function.IFunction;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;

import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * The Class AndOperator.
 */
@XmlRootElement(namespace = "http://www.mydata-control.de/4.0/mydataLanguage", name = "and")
public class AndFunction extends Function {

  /**
   * Instantiates a new and operator.
   */
  public AndFunction() {
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.pdp.java.language.model.operator.Operator#
   * evaluate(de.fraunhofer.iese.mydata.api.policy.Event)
   */
  @Override
  public DataObject<Boolean> evaluate(Event evt) throws EvaluationUndecidableException {
    if (this.getParameters() == null || this.getParameters().isEmpty()) {
      throw new EvaluationUndecidableException("Cannot compare less than two arguments");
    }
    for (final IFunction op : this.getParameters()) {
      final DataObject<?> evalResult = op.evaluate(evt);

      if (evalResult.getType() != boolean.class && evalResult.getType() != Boolean.class) {
        throw new IllegalArgumentException();
      }

      @SuppressWarnings("unchecked")
      final DataObject<Boolean> booleanResult = (DataObject<Boolean>) evalResult;

      if (Boolean.FALSE.equals(booleanResult.getValue())) {
        return PolicyConstant.FALSE;
      }
    }
    return PolicyConstant.TRUE;
  }
}
