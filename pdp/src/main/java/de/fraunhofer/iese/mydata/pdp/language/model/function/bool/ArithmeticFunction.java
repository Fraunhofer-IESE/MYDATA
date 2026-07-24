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
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;

import jakarta.xml.bind.annotation.XmlSeeAlso;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The abstract Class ArithmeticFunction is the base class for implementing arithmetic operations.
 */
@XmlSeeAlso({
    GreaterEqualFunction.class, GreaterFunction.class, LessFunction.class, LessEqualFunction.class
})
public abstract class ArithmeticFunction extends BooleanFunction {

  /**
   * The logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ArithmeticFunction.class);

  /**
   * Instantiates a new arithmetic function.
   */
  public ArithmeticFunction() {
    super();
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.pdp.java.language.model.function.BooleanFunction
   * #evaluate(de.fraunhofer.iese.mydata.api.policy.Event)
   */
  @Override
  public final DataObject<Boolean> evaluate(Event e) throws EvaluationUndecidableException {
    LOG.debug("Entering evaluate(name={}, parameters={}, event={})", this.getName(),
        this.getParameters(), e);

    if (this.getParameters() == null || this.getParameters().size() < 2) {
      LOG.warn("Cannot compare less than two arguments");
      throw new EvaluationUndecidableException("Cannot compare less than two arguments");
    }

    double a = this.getNumber(this.getParameters().get(0).evaluate(e).getValue());

    for (int i = 1; i < this.getParameters().size(); i++) {
      final Object currentVal = this.getParameters().get(i).evaluate(e).getValue();
      final double b = this.getNumber(currentVal);
      if (!this.compare(a, b)) {
        LOG.debug("Leaving evaluate(): false.");
        return PolicyConstant.FALSE;
      }
      a = b;
    }
    LOG.debug("Leaving evaluate(): true");
    return PolicyConstant.TRUE;
  }

  /**
   * Returns the double value of the object, if it is a number.
   *
   * @param  o any object
   * @return   the double value of the object, if it is a number
   */
  private Double getNumber(Object o) {
    if (!(o instanceof Number)) {
      LOG.warn("Only numbers can be compared");
      throw new IllegalArgumentException("Only numbers can be compared");
    }
    return ((Number) o).doubleValue();
  }

  /**
   * Implements the arithmetic comparison.
   *
   * @param  leftOperand  the left operand of the operation
   * @param  rightOperand the right operand of the operation
   * @return              true, if the arithmetic comparison
   */
  protected abstract boolean compare(double leftOperand, double rightOperand);

}
