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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/****
 * This a default Modifiers method with Pep sdk which shortens the string to it first letter, set it
 * to upper of lower case (default: upper) and append a character (default: point) where event
 * parameter is either a Json Object or string (primitive type)
 */
public class ShortenInitialModifierMethod extends PrimitiveModifierMethod {

  private static final Logger LOG = LoggerFactory.getLogger(ShortenInitialModifierMethod.class);

  @Override
  public String getDisplayName() {
    return "shorten";
  }

  @Override
  public Object doModification(Object currentObject, ParameterList modifierMethodParameterList) {
    final Object suffix = modifierMethodParameterList.getParameterValueForName("suffixParam");
    final String suffixParam = suffix != null ? suffix.toString() : ".";
    if (suffix == null || "".equals(suffix.toString())) {
      LOG.warn(
          "The suffix parameter 'suffixParam' could not be extracted: '.' will be used per default");
    }
    final Object caseTo = modifierMethodParameterList.getParameterValueForName("caseToParam");
    final String caseToParam = caseTo != null ? caseTo.toString() : "upper";
    if (!caseToParam.equalsIgnoreCase("upper") && !caseToParam.equalsIgnoreCase("lower")) {
      LOG.warn(
          "The case parameter 'caseToParam' was null or not among upper or lower: the 'upper' case will be used per default");
    }
    return this.shorten(currentObject.toString(), suffixParam, caseToParam);
  }

  @ActionDescription(description = "Shorten a string to the first letter, change the case (per default to uppercase) and append a character (per default a point)", pepSupportedType = String.class)
  public String shorten(String name,
      @ActionParameterDescription(name = "suffixParam", description = "suffix to be set", mandatory = true) String suffixParam,
      @ActionParameterDescription(name = "caseToParam", description = "case change", mandatory = true) String caseToParam) {

    if (name == null) {
      return null;
    }
    final String trimmed = name.trim();
    if (!StringUtils.isEmpty(trimmed)) {
      if (caseToParam.equalsIgnoreCase("upper")) {
        return trimmed.substring(0, 1).toUpperCase(Locale.ROOT) + suffixParam;
      } else {
        return trimmed.substring(0, 1).toLowerCase(Locale.ROOT) + suffixParam;
      }
    }
    return name;

  }

}
