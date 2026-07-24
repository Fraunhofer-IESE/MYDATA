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

public class RoundModifierMethod extends PrimitiveModifierMethod {

  @Override
  public String getDisplayName() {
    return "round";
  }

  @Override
  public Object doModification(Object currentObject, ParameterList modifierMethodParameterList) {
    final Object modeObj = modifierMethodParameterList.getParameterValueForName("mode");
    final Object intervalObj = modifierMethodParameterList.getParameterValueForName("interval");
    final Object offsetObj = modifierMethodParameterList.getParameterValueForName("offset");
    String mode = (modeObj == null) ? "round" : (String) modeObj;
    Double interval = (intervalObj == null) ? 1.0 : (Double) intervalObj; // Double.valueOf((String) intervalObj);
    Double offset = (offsetObj == null) ? 0.0 : (Double) offsetObj; // Double.valueOf((String) offsetObj);
    
    Object result = null;
    if (currentObject instanceof Integer)
      result = round((Integer)currentObject, mode, interval, offset).intValue();
    if (currentObject instanceof Long)
      result = round((Long)currentObject, mode, interval, offset).longValue();
    if (currentObject instanceof Float)
      result = round((Float)currentObject, mode, interval, offset).floatValue();
    if (currentObject instanceof Double)
      result = round((Double)currentObject, mode, interval, offset).doubleValue();
    return result;
  }

  // "interval" and "offset" parameters examples:
  // inputs = [0, 2000], interval = 500, offset = 250
  // results = 250, 750, 1250 or 1750
  // inputs = [0, 10], interval = 0.2, offset = 0.1
  // results = 0.1, 0.3, 0.5, 0.7, 0.9, 1.1, etc
  @ActionDescription(description = "Rounds, ceils or floors the target number to the closest point defined by interval and offset", pepSupportedType = Number.class)
  public Number round(Number currentNumber,
      @ActionParameterDescription(name = "mode", description = "Operation mode: 'round', 'ceil' or 'floor'; default is 'round'", mandatory = false) String mode,
      @ActionParameterDescription(name = "interval", description = "Interval of rounding; default is 1", mandatory = false) Double interval,
      @ActionParameterDescription(name = "offset", description = "Offset of rounding; default is 0", mandatory = false) Double offset) {
    if ("round".equals(mode))
      return Math.round( (currentNumber.doubleValue()-offset)/interval ) * interval + offset;
    if ("ceil".equals(mode))
      return Math.ceil( (currentNumber.doubleValue()-offset)/interval ) * interval + offset;
    if ("floor".equals(mode))
      return Math.floor( (currentNumber.doubleValue()-offset)/interval ) * interval + offset;
    // if no valid modes where selected, throw an error
    throw new IllegalArgumentException("Mode must be 'round', 'ceil' or 'floor'");
  }
}
