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

import de.fraunhofer.iese.mydata.pep.common.PrimitiveModifierMethod;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;
import de.fraunhofer.iese.mydata.registry.ActionDescription;
import de.fraunhofer.iese.mydata.registry.ActionParameterDescription;

/****
 * This a default Modifiers method with Pep sdk which appends the prefix and suffix to string where
 * event parameter is either a Json Object or string (primitive type)
 */
public class AppendModifierMethod extends PrimitiveModifierMethod {

  @Override
  public String getDisplayName() {
    return "append";
  }

  @Override
  public Object doModification(Object currentObject, ParameterList modifierMethodParameterList) {
    final Object suffix = modifierMethodParameterList.getParameterValueForName("suffix");
    final String suffixParam = suffix != null ? suffix.toString() : null;
    final Object prefix = modifierMethodParameterList.getParameterValueForName("prefix");
    final String prefixParam = prefix != null ? prefix.toString() : null;
    return this.append(currentObject.toString(), prefixParam, suffixParam);
  }

  @ActionDescription(description = "Appends a prefix and/or suffix to an event parameter value", pepSupportedType = String.class)
  public String append(String input,
      @ActionParameterDescription(name = "prefix", description = "String to append at the head", mandatory = false) String prefix,
      @ActionParameterDescription(name = "suffix", description = "String to append at the end", mandatory = false) String suffix) {

    final StringBuilder builder = new StringBuilder();
    if (prefix != null) {
      builder.append(prefix);
    }
    builder.append(input);
    if (suffix != null) {
      builder.append(suffix);
    }
    return builder.toString();
  }

}
