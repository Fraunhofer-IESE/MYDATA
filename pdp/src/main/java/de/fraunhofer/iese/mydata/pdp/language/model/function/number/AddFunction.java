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

package de.fraunhofer.iese.mydata.pdp.language.model.function.number;

import de.fraunhofer.iese.mydata.pdp.language.model.function.Function;
import de.fraunhofer.iese.mydata.pdp.language.model.function.IFunction;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;

import java.math.BigDecimal;
import java.math.RoundingMode;


@SuppressWarnings("javadoc")
public class AddFunction extends Function {

  private static final BigDecimal LONG_MAX_VAL = new BigDecimal(Long.MAX_VALUE);

  private static final BigDecimal LONG_MIN_VAL = new BigDecimal(Long.MIN_VALUE);

  @Override
  public DataObject<Number> evaluate(Event evt) throws EvaluationUndecidableException {
    try {

      if (this.getParameters() == null || this.getParameters().isEmpty()) {
        throw new EvaluationUndecidableException("Cannot multiply less than one arguments");
      }

      BigDecimal result = BigDecimal.ZERO;

      for (final IFunction op : this.getParameters()) {
        final DataObject<?> evalResult = op.evaluate(evt);

        if (String.class.isAssignableFrom(evalResult.getValue().getClass())) {
          result = result.add(new BigDecimal((String)evalResult.getValue()));

        } else if (Integer.class.isAssignableFrom(evalResult.getValue().getClass())) {
          result = result.add(BigDecimal.valueOf((Integer)evalResult.getValue()));

        } else if (Long.class.isAssignableFrom(evalResult.getValue().getClass())) {
          result = result.add(BigDecimal.valueOf((Long)evalResult.getValue()));

        } else if (Float.class.isAssignableFrom(evalResult.getValue().getClass())) {
          result = result.add(BigDecimal.valueOf((Float)evalResult.getValue()));

        } else if (Double.class.isAssignableFrom(evalResult.getValue().getClass())) {
          result = result.add(BigDecimal.valueOf((Double)evalResult.getValue()));

        } else {
          throw new IllegalArgumentException("Only numbers and Strings are supported");
        }

        if (result.equals(BigDecimal.ZERO)) {
          return new DataObject<>(0);
        }
      }

      if ((result.compareTo(result.setScale(0, RoundingMode.DOWN))) == 0 && result.longValue() <= Integer.MAX_VALUE && result.longValue() >= Integer.MIN_VALUE) {
        return new DataObject<>(result.intValue());
      }

      if ((result.compareTo(result.setScale(0, RoundingMode.DOWN))) == 0 && result.compareTo(LONG_MAX_VAL) <= 0 && result.compareTo(LONG_MIN_VAL) > 0) {
        return new DataObject<>(result.longValue());
      }

      return new DataObject<>(result.doubleValue());

    } catch (final Exception e) {
      throw new EvaluationUndecidableException("Error during addition", e);
    }
  }

}
