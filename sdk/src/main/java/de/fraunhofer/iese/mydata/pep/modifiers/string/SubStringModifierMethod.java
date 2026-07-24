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

package de.fraunhofer.iese.mydata.pep.modifiers.string;

import de.fraunhofer.iese.mydata.pep.common.CommonUtil;
import de.fraunhofer.iese.mydata.pep.common.PrimitiveModifierMethod;
import de.fraunhofer.iese.mydata.pep.common.SubstringUtil;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;
import de.fraunhofer.iese.mydata.registry.ActionDescription;
import de.fraunhofer.iese.mydata.registry.ActionParameterDescription;

/****
 * This a default Modifiers method with Pep sdk which appends the prefix and suffix to string where
 * event parameter is either a Json Object or string (primitive type)
 */
public class SubStringModifierMethod extends PrimitiveModifierMethod {
  private static final String NAME = "substring";

  @Override
  public Object doModification(Object currentObject, ParameterList modifierMethodParameterList) {
    final Object start = modifierMethodParameterList.getParameterValueForName("startIndex");
    final String startParam = start != null ? start.toString() : null;
    final Object end = modifierMethodParameterList.getParameterValueForName("endIndex");
    final String endParam = end != null ? end.toString() : null;
    final Object fillString = modifierMethodParameterList.getParameterValueForName("fillString");
    final String fillStringParam = fillString != null ? fillString.toString() : "";
    final Object autoFill = modifierMethodParameterList.getParameterValueForName("autoFill");
    final String autoFillParam = autoFill != null ? autoFill.toString() : "";
    return this.substring(currentObject, CommonUtil.getIntegerFromObject(startParam),
        CommonUtil.getIntegerFromObject(endParam), fillStringParam,
        CommonUtil.getbooleanFromObject(autoFillParam));

  }

  @ActionDescription(description = "Appends a prefix and/or suffix to an event parameter value", pepSupportedType = String.class)
  public String substring(Object input,
      @ActionParameterDescription(name = "startIndex", description = "int to define the start parameter", mandatory = false) Integer start,
      @ActionParameterDescription(name = "endIndex", description = "int to define the end parameter", mandatory = false) Integer end,
      @ActionParameterDescription(name = "fillString", description = "String to fill cuted characters", mandatory = false) String fillString,
      @ActionParameterDescription(name = "autoFill", description = "auto fit length of fillString", mandatory = false) Boolean autoFill) {

    final String sInput = CommonUtil.getFromStringOrJsonPrimitive(input);

    return SubstringUtil.substring(sInput, start, end, fillString, autoFill);
  }

  @Override
  public String getDisplayName() {
    return NAME;
  }

}
