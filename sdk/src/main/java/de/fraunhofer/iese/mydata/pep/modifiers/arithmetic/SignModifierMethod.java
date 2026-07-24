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

public class SignModifierMethod extends PrimitiveModifierMethod {

  @Override
  public String getDisplayName() {
    return "sign";
  }

  @Override
  public Object doModification(Object currentObject, ParameterList modifierMethodParameterList) {
    final Object modeObj = modifierMethodParameterList.getParameterValueForName("mode");
    String mode = (modeObj == null) ? "none" : (String) modeObj;
    
    Object result = null;
    if (currentObject instanceof Integer)
      result = sign((Integer)currentObject, mode).intValue();
    if (currentObject instanceof Long)
      result = sign((Long)currentObject, mode).longValue();
    if (currentObject instanceof Float)
      result = sign((Float)currentObject, mode).floatValue();
    if (currentObject instanceof Double)
      result = sign((Double)currentObject, mode).doubleValue();
    return result;
  }

  @ActionDescription(description = "Takes the absolute value, negative value or toggles the current sign of the target", pepSupportedType = Number.class)
  public Number sign(Number currentNumber, 
      @ActionParameterDescription(name = "mode", description = "operation mode: 'absolute', 'negative' or 'toggle'; 'none' is the default", mandatory = true) String mode) {
    if ("absolute".equals(mode)) {
      return Math.abs(currentNumber.doubleValue());
    }
    
    if ("negative".equals(mode)) {
      return -Math.abs(currentNumber.doubleValue());
    }
    
    if ("toggle".equals(mode)) {
      return -currentNumber.doubleValue();
    }
    
    return currentNumber;
  }
}
