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

package de.fraunhofer.iese.mydata.pep.common;

import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;

import com.jayway.jsonpath.DocumentContext;

/***
 * Base Interface for all Modifiers.
 * A Modifiers should have a name, that can be used in policies
 * {@link #getDisplayName()}. The Method
 * {@link #doModification(DocumentContext, String, ParameterList)} is used for
 * modification of complex objects that are serialized to a JSON Structure.
 * {@link #doModification(Object, ParameterList)} should be overriden if the
 * Modifiers also supports modification of primitive values including wrapper
 * Types as well as String and Date.
 */
public interface ModifierMethod {

  /***
   * Modification for Complex Objects that are serialized using
   * {@link com.jayway.jsonpath.JsonPath}.
   *
   * @param documentContext Serialized Object Structure with
   *          {@link com.jayway.jsonpath.JsonPath}
   * @param expression {@link com.jayway.jsonpath.JsonPath} Expression to adress
   *          a specific element/attribute in the json.
   * @param modifierMethodParameterList Values used for modification.
   * @return {@link DocumentContext}
   */
  DocumentContext doModification(DocumentContext documentContext, String expression, ParameterList modifierMethodParameterList);

  /***
   * This method to be overridden when modifier wants to handle primitive type
   * like Integer(int), Float(float), Boolean(boolean), Long(long),
   * Short(short), Byte(byte) and Double(double) and String
   *
   * @param currentObject The primitive value for modification.
   * @param modifierMethodParameterList List of parameters that should be used
   *          for modification
   * @return This default implementation is returning the object without
   *         modification.
   */
  Object doModification(Object currentObject, ParameterList modifierMethodParameterList);

  /***
   * @return Display name
   */
  String getDisplayName();

  boolean nameIsValid();

}
