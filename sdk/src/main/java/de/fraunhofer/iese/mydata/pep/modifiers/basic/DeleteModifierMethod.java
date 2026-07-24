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

import de.fraunhofer.iese.mydata.pep.common.ModifierMethod;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;
import de.fraunhofer.iese.mydata.registry.ActionDescription;

import com.jayway.jsonpath.DocumentContext;

import java.lang.reflect.Method;

/****
 * This a default Modifiers method with Pep sdk which deletes attribute where
 * event parameter is either a Json Object or string (primitive type)
 */
public class DeleteModifierMethod implements ModifierMethod {

  @Override
  public DocumentContext doModification(DocumentContext documentContext, String expression, ParameterList modifierMethodParameterList) {

    if ("$".equals(expression)) {
      return null;
    }

    return this.delete(documentContext, expression);
  }

  @ActionDescription(description = "Deletes keys that are evaluated by expression", pepSupportedType = Object.class)
  public DocumentContext delete(DocumentContext documentContext, String expression) {
    return documentContext.delete(expression);
  }

  @Override
  public String getDisplayName() {
    return "delete";
  }

  @Override
  public Object doModification(Object currentObject, ParameterList modifierMethodParameterList) {
    return null;
  }

  @Override
  public boolean nameIsValid(){
    Method[] methods = this.getClass().getMethods();
    for (Method m:methods)
      if (m.getName().equalsIgnoreCase(getDisplayName()))
        return true;
    return false;
  }
}
