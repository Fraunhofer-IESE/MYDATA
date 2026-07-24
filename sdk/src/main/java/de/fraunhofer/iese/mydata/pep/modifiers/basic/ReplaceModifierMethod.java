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

package de.fraunhofer.iese.mydata.pep.modifiers.basic;

import de.fraunhofer.iese.mydata.pep.common.PrimitiveModifierMethod;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;
import de.fraunhofer.iese.mydata.registry.ActionDescription;
import de.fraunhofer.iese.mydata.registry.ActionParameterDescription;

/***
 * This a default Modifiers method with Pep sdk which replaces a string with another one where event
 * parameter is either a Json Object or string (primitive type)
 */
public class ReplaceModifierMethod extends PrimitiveModifierMethod {

  @Override
  public Object doModification(Object currentObject, ParameterList modifierMethodParameterList) {
    final Object setWith = modifierMethodParameterList.getParameterValueForName("replaceWith");
    final String setWithString = setWith != null ? setWith.toString() : null;
    return this.replace(currentObject.toString(), setWithString);
  }

  @ActionDescription(description = "Replaces the string", pepSupportedType = String.class)
  public Object replace(String currentObject,
      @ActionParameterDescription(name = "replaceWith", description = "Replace the string with entered value", mandatory = true) String setWith) {

    return setWith;
  }

  @Override
  public String getDisplayName() {
    return "replace";
  }

}
