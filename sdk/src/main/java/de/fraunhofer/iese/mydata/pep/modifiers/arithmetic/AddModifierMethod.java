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

/**
 * Addition Modifiers
 */
public class AddModifierMethod extends PrimitiveModifierMethod {

  /**
   * Display Name of Modifiers.
   *
   * @return Name
   */
  @Override
  public String getDisplayName() {
    return "add";
  }

  /**
   * Doing modification.
   *
   * @param currentObject Object to be modified.
   * @param modifierMethodParameterList list of parameters
   * @return Modified Object
   */
  @Override
  public Object doModification(Object currentObject, ParameterList modifierMethodParameterList) {
    final Object operandObj = modifierMethodParameterList.getParameterValueForName("operand");
    
    Object result = null;
    if (currentObject instanceof Integer)
      result = add(Integer.valueOf(String.valueOf(currentObject)), Double.valueOf(String.valueOf(operandObj))).intValue();
    if (currentObject instanceof Long)
      result = add(Long.valueOf(String.valueOf(currentObject)), Double.valueOf(String.valueOf(operandObj))).longValue();
    if (currentObject instanceof Float)
      result = add(Float.valueOf(String.valueOf(currentObject)), Double.valueOf(String.valueOf(operandObj))).floatValue();
    if (currentObject instanceof Double)
      result = add(Double.valueOf(String.valueOf(currentObject)), Double.valueOf(String.valueOf(operandObj))).doubleValue();
    return result;
  }

  @ActionDescription(description = "Adds two numbers", pepSupportedType = Number.class)
  public Number add(Number currentNumber,
      @ActionParameterDescription(name = "operand", description = "Number to be added", mandatory = true) Number operand) {
    return operand.doubleValue() + currentNumber.doubleValue();
  }
}
