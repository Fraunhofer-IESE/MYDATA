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

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.MapFunction;
import net.minidev.json.JSONArray;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Base class for modifiers that apply to single, primitive elements
 * Implements a generic Json-doModification that handles JsonPrimitives and recursively handles JsonArrays
 * Makes use of the Java-doModification, which must be implemented in specialist classes
 */
public abstract class PrimitiveModifierMethod implements ModifierMethod {
  
  @Override
  public DocumentContext doModification(DocumentContext documentContext, String expression, ParameterList modifierMethodParameterList) {
    final Gson gson = MyDataEntity.getGson();

    if (documentContext.read(expression) instanceof JsonObject)
      throw new IllegalArgumentException("While searching for JsonPrimitives, found a JsonObject!");

    // Handling a single primitive: call doModification on it; beware if expression is the root
    else if (documentContext.read(expression) instanceof JsonPrimitive) {
      JsonPrimitive expressionResult = documentContext.read(expression);
      Object modObject = doModificationToPrimitive(expressionResult, modifierMethodParameterList);
      if ("$".equals(expression)) {
        return JsonPath.parse(gson.toJson(modObject), documentContext.configuration());
      }
      return documentContext.set(expression, modObject);
    }

    // Handling an array: call doModification in all primitive leafs; beware of child arrays, recurse into them
    else if (documentContext.read(expression) instanceof JsonArray || documentContext.read(expression) instanceof JSONArray) {

      mapModification = (javaObject, configuration) -> {
        if (javaObject instanceof JsonObject) {
          throw new IllegalArgumentException("While searching for JsonPrimitives, found a JsonObject!");
        }
        if (javaObject instanceof JsonArray) {
          // If a JsonArray was found, this function has to return it as a JsonArray but modified.
          // Applying this function recursively to each element does the modifications,
          // but the new JsonArray cannot be built immediatly: its .add() function does not accept Objects,
          // even though all the Objects going around here are java primitive wrappers or JsonPrimitives, which it accepts
          ArrayList<Object> modArray = new ArrayList<>();
          for (Object element : (JsonArray) javaObject)
            modArray.add(this.mapModification.map(element, configuration));
          return gson.fromJson(gson.toJson(modArray), JsonArray.class);
        }
        // It seems that when using documentContext.map() on an expression that leads to a proper list, the javaObjects come as JsonPrimitives (unless they are JsonArrays)
        if (javaObject instanceof JsonPrimitive) {
          return this.doModificationToPrimitive((JsonPrimitive) javaObject, modifierMethodParameterList);
        }
        // On the other side, if the expression is a deep search "..", a list of the results is made and the javaObjects comes as a POJOs (again, unless they are JsonArrays)
        return this.doModification(javaObject, modifierMethodParameterList);
      };

      return documentContext.map(expression, mapModification);
    }

    // else if (expressionResult.isJsonNull()) or something else
    return documentContext;
  }

  private Object doModificationToPrimitive(JsonPrimitive jsonPrimitive, ParameterList modifierMethodParameterList) {
    Object javaObject;
    if (jsonPrimitive.isNumber()) {
      String numberString = jsonPrimitive.toString();
      if (numberString.contains("."))
        javaObject = Double.parseDouble(numberString);
      else
        javaObject = Long.parseLong(numberString);
    }
    else if (jsonPrimitive.isBoolean()) {
      javaObject = jsonPrimitive.getAsBoolean();
    }
    else if (jsonPrimitive.isString()) {
      javaObject = jsonPrimitive.getAsString();
    }
    else {
      throw new IllegalArgumentException("jsonPrimitive is none of the above. This should never happen!");
    }
    return this.doModification(javaObject, modifierMethodParameterList);
  }
  
  private MapFunction mapModification;

  @Override
  public boolean nameIsValid() {
    Method[] methods = this.getClass().getMethods();
    for (Method m:methods)
      if (m.getName().equalsIgnoreCase(getDisplayName()))
        return true;
    return false;
  }
}
