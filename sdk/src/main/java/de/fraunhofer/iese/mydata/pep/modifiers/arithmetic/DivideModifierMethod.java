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

package de.fraunhofer.iese.mydata.pep.modifiers.arithmetic;

import de.fraunhofer.iese.mydata.pep.common.PrimitiveModifierMethod;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;
import de.fraunhofer.iese.mydata.registry.ActionDescription;
import de.fraunhofer.iese.mydata.registry.ActionParameterDescription;

public class DivideModifierMethod extends PrimitiveModifierMethod {

  @Override
  public String getDisplayName() {
    return "divide";
  }

  @Override
  public Object doModification(Object currentObject, ParameterList modifierMethodParameterList) {
    final Object numeratorObj = modifierMethodParameterList.getParameterValueForName("numerator");
    final Object denominatorObj = modifierMethodParameterList.getParameterValueForName("denominator");
    Number numerator = (numeratorObj == null) ? null : Double.valueOf(String.valueOf(numeratorObj));
    Number denominator = (denominatorObj == null) ? null : Double.valueOf(String.valueOf(denominatorObj));

    Object result = null;
    if (currentObject instanceof Integer)
      result = divide(Integer.valueOf(String.valueOf(currentObject)), numerator, denominator).intValue();
    if (currentObject instanceof Long)
      result = divide(Long.valueOf(String.valueOf(currentObject)), numerator, denominator).longValue();
    if (currentObject instanceof Float)
      result = divide(Float.valueOf(String.valueOf(currentObject)), numerator, denominator).floatValue();
    if (currentObject instanceof Double)
      result = divide(Double.valueOf(String.valueOf(currentObject)), numerator, denominator).doubleValue();
    return result;
  }

  @ActionDescription(description = "Divides the target by the denominator or divides the numerator by the target. If both are present, replaces the target by the result."
      + "If none are present, returns the unmodified target.", pepSupportedType = Number.class)
  public Number divide(Number currentNumber,
      @ActionParameterDescription(name = "numerator", description = "Number to be divided", mandatory = false) Number numerator,
      @ActionParameterDescription(name = "denominator", description = "Number to divide by", mandatory = false) Number denominator) {
    if (numerator == null && denominator != null)
      return div(currentNumber.doubleValue(), denominator.doubleValue());
    if (numerator != null && denominator == null)
      return div(numerator.doubleValue(), currentNumber.doubleValue());
    if (numerator != null && denominator != null)
      return div(numerator.doubleValue(), denominator.doubleValue());
    return currentNumber;
  }

  private double div (double a, double b) {
    if (b == 0) {
      throw new IllegalArgumentException("Argument denominator is 0");
    }
    return a/b;
  }
}
