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

package de.fraunhofer.iese.mydata.pep.modifiermethod;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.fraunhofer.iese.mydata.DataFactory;
import de.fraunhofer.iese.mydata.pep.common.DecisionEnforcer;
import de.fraunhofer.iese.mydata.pep.enforce.JsonPathDecisionEnforcer;
import de.fraunhofer.iese.mydata.pep.modifiers.basic.ReplaceModifierMethod;
import de.fraunhofer.iese.mydata.pep.testdata.model.Company;
import de.fraunhofer.iese.mydata.pep.testdata.model.Information;
import de.fraunhofer.iese.mydata.policy.decision.AuthorizationDecision;
import de.fraunhofer.iese.mydata.policy.decision.Modifier;
import de.fraunhofer.iese.mydata.policy.decision.ModifierEngine;
import de.fraunhofer.iese.mydata.policy.exception.InhibitException;
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class ReplaceModifierMethodTest {

  Gson gson;

  ReplaceModifierMethod replaceModifierMethod;

  private DocumentContext companyWithProjectDocumentContext;

  {
    // JsonPath jsonPath = JsonPath.parse()
    this.gson = new GsonBuilder().setPrettyPrinting().create();
    Configuration.setDefaults(new Configuration.Defaults() {

      private final JsonProvider jsonProvider = new GsonJsonProvider();

      private final MappingProvider mappingProvider = new GsonMappingProvider();

      @Override
      public JsonProvider jsonProvider() {
        return this.jsonProvider;
      }

      @Override
      public Set<Option> options() {
        return EnumSet.noneOf(Option.class);
      }

      @Override
      public MappingProvider mappingProvider() {
        return this.mappingProvider;
      }
    });
  }

  @BeforeEach
  public void initialize() {
    this.companyWithProjectDocumentContext = JsonPath
        .parse(this.gson.toJson(DataFactory.getCompanyWithProjects()));
    this.replaceModifierMethod = new ReplaceModifierMethod();
  }

  //  @Ignore(value = "Replace modifier is a JsonPrimitive modifier. It does not support JsonObject.")
  //  @Test
  //  public void setEmployees() {
  //    final ParameterList parameters = new ParameterList();
  //    parameters.add(new Parameter<>("replaceWith",
  //        "[{\"userId\":\"ttzz456$\",\"role\":\"CSM\",\"firstName\":\"sainik\",\"lastName\":\"kumar\"}]"));
  //    this.replaceModifierMethod.doModification(this.companyWithProjectDocumentContext, "$.employees",
  //        parameters);
  //    final Type companyType = new TypeToken<Company>() {
  //    }.getType();
  //    final Company company = this.gson.fromJson(this.companyWithProjectDocumentContext.jsonString(),
  //        companyType);
  //    Assert.assertEquals(1, company.getEmployees().size());
  //  }

  @Test
  void setName() {
    final ParameterList parameters = new ParameterList();
    parameters.add(new Parameter<>("replaceWith", "Fraunhofer IESE."));
    this.replaceModifierMethod.doModification(this.companyWithProjectDocumentContext, "$.name",
        parameters);
    final Type companyType = new TypeToken<Company>() {
    }.getType();
    final Company company = this.gson.fromJson(this.companyWithProjectDocumentContext.jsonString(),
        companyType);
    assertEquals("Fraunhofer IESE.", company.getName());
  }

  /**
   * replace an byte array
   *
   * @throws InhibitException
   */
  @Test
  void testReplaceByteArray() throws InhibitException {

    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("replaceWith", "[1,2,3,4]"));

    final AuthorizationDecision decision = this.createReplaceAuthorizationDecision("$.byteArray",
        parameterList);
    assertThrows(InhibitException.class, () -> {
      final ParameterList result = this.enforce(decision, this.getInformation());
      final Information modInformation = (Information) result
          .getParameterValueForName("information");
      assertTrue(modInformation.getByteArray().length == 4);
    });

  }

  private AuthorizationDecision createReplaceAuthorizationDecision(String expression,
      ParameterList parameterList) {

    final ModifierEngine engine = new ModifierEngine("replace", parameterList);
    final List<ModifierEngine> modifierEngines = new ArrayList<>();
    modifierEngines.add(engine);
    final Modifier modifier = new Modifier("information", modifierEngines);
    modifier.setExpression(expression);
    final AuthorizationDecision decision = new AuthorizationDecision();
    decision.addModifier(modifier);
    return decision;
  }

  private Information getInformation() {
    final Information information = new Information();
    final byte[] byteArray = {
        0, 1, 2
    };
    information.setByteArray(byteArray);
    return information;
  }

  private ParameterList enforce(AuthorizationDecision decision, Object information)
      throws InhibitException {
    final DecisionEnforcer enforcer = this.getEnforcer();
    return enforcer.enforce(decision,
        new ParameterList(new Parameter<>("information", information)));
  }

  private DecisionEnforcer getEnforcer() {
    final DecisionEnforcer decisionEnforcer = new JsonPathDecisionEnforcer();
    decisionEnforcer.addModificationMethod(new ReplaceModifierMethod());
    return decisionEnforcer;
  }

}
