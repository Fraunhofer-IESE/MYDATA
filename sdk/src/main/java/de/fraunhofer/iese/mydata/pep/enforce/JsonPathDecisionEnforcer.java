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

package de.fraunhofer.iese.mydata.pep.enforce;

import de.fraunhofer.iese.mydata.pep.common.DecisionEnforcer;
import de.fraunhofer.iese.mydata.pep.common.ModifierMethod;
import de.fraunhofer.iese.mydata.pep.common.ModifierNotFoundException;
import de.fraunhofer.iese.mydata.policy.decision.AuthorizationDecision;
import de.fraunhofer.iese.mydata.policy.decision.Modifier;
import de.fraunhofer.iese.mydata.policy.decision.ModifierEngine;
import de.fraunhofer.iese.mydata.policy.exception.InhibitException;
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Decision Enforcer that uses {@link JsonPath} to provide expressions to
 */
public class JsonPathDecisionEnforcer implements DecisionEnforcer {

  private static final Logger LOG = LoggerFactory.getLogger(JsonPathDecisionEnforcer.class);

  private static final Configuration configuration = new Configuration.ConfigurationBuilder()
      .jsonProvider(new GsonJsonProvider()).mappingProvider(new GsonMappingProvider()).build();

  private final Map<String, ModifierMethod> modifiers = new TreeMap<>();

  private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  @Override
  public boolean addModificationMethod(ModifierMethod method) {
    if (method.nameIsValid()) {
      this.modifiers.put(method.getDisplayName(), method);
      LOG.info("Modifiers is added to the list of modifiers: {}", method.getDisplayName());
      return true;
    } else {
      LOG.warn(
          "Modifiers was not added to the list of modifiers because the display name does not match the method name: {}",
          method.getDisplayName());
      return false;
    }
  }

  private void doModificationForParameter(ParameterList parameterList, Modifier eventParameter)
      throws InhibitException {

    final Parameter<?> parameterForName = parameterList
        .getParameterForName(eventParameter.getName());
    if (parameterForName != null) {
      LOG.debug("Starting with event modification.");
      this.doModificationForParameter(parameterList, eventParameter, parameterForName);
    }
  }

  private void doModificationForParameter(ParameterList parameterList, Modifier eventParameter,
      Parameter<?> parameterForName) throws InhibitException {
    Object parameterValue = parameterForName.getValue();
    final Type parameterType = parameterForName.getType();
    final String parameterName = parameterForName.getName();
    DocumentContext parameterValueDocumentContext = JsonPath.parse(this.gson.toJson(parameterValue),
        configuration);
    final List<ModifierEngine> modifierEngines = eventParameter.getEngine();
    Object modifiedObject = null;
    for (final ModifierEngine modifierEngine : modifierEngines) {
      final ModifierMethod modifierMethod = this.modifiers.get(modifierEngine.getMethod());
      modifierEngine.addParameter("parameterName", parameterName);
      if (modifierMethod == null) {
        LOG.error("Not found modifier: {}", modifierEngine.getMethod());
        throw new ModifierNotFoundException(
            "ModificationActor is not registered against Modifiers Method named  : "
                + modifierEngine.getMethod());
      }
      if (eventParameter.getExpression() == null) {
        if (parameterValue.getClass().isPrimitive()
            || org.apache.commons.lang3.ClassUtils
                .wrapperToPrimitive(parameterValue.getClass()) != null
            || parameterValue.getClass() == String.class) {
          parameterValue = modifierMethod.doModification(parameterValue,
              modifierEngine.getParameters());
          modifiedObject = parameterValue;
        }
      } else {
        try {
          parameterValueDocumentContext = modifierMethod.doModification(
              parameterValueDocumentContext, eventParameter.getExpression(),
              modifierEngine.getParameters());
          if (parameterValueDocumentContext != null) {
            String jsonString;
            try {
              jsonString = parameterValueDocumentContext.jsonString();
            } catch (final Exception e2) {
              LOG.info(
                  "Problem getting DocumentContext.jsonString(), use toString() for JSON Object",
                  e2);
              jsonString = parameterValueDocumentContext.json().toString();
            }

            try {
              modifiedObject = this.gson.fromJson(jsonString, parameterType);
            } catch (final Exception e) {
              LOG.error("gson.fromJson(jsonString,parameterType) failed", e);
              throw new InhibitException("Not able todo modification due to ", e);
            }
          } else {
            modifiedObject = null;
          }
        } catch (final PathNotFoundException pnfe) {
          LOG.warn("Nothing to do, {} does not exist in JSON", eventParameter.getExpression());
          return;
        } catch (final Exception e) {
          LOG.error("event is inhibited. Error on modification of the modifier: {}",
              modifierEngine.getMethod());
          throw new InhibitException("Not able to do modification due to ", e);
        }
      }

    }
    parameterList.removeParameter(eventParameter.getName());
    if (modifiedObject != null) {
      parameterList.addParameter(eventParameter.getName(), modifiedObject, parameterType);
    }
  }

  @Override
  public ParameterList enforce(AuthorizationDecision authorizationDecision,
      ParameterList parameterList) throws InhibitException {
    final List<Modifier> theModifiers = authorizationDecision.getModifiers();

    if (theModifiers == null || theModifiers.isEmpty()) {
      LOG.debug("Event does not need to be modified");
      return parameterList;
    }

    LOG.debug("Event needs modification");

    for (final Modifier eventParameter : theModifiers) {
      this.doModificationForParameter(parameterList, eventParameter);
    }

    return parameterList;
  }

  @Override
  public boolean removeModificationMethod(String name) {
    if (!this.modifiers.isEmpty() && this.modifiers.containsKey(name)) {
      this.modifiers.remove(name);
      return true;
    } else {
      return false;
    }
  }

}
