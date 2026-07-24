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

import de.fraunhofer.iese.mydata.DataFactory;
import de.fraunhofer.iese.mydata.pep.modifiers.basic.HashModifierMethod;
import de.fraunhofer.iese.mydata.pep.testdata.model.Company;
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
import java.util.EnumSet;
import java.util.Set;

/**
 * Tests for the hash modifier
 */
class HashModifierMethodTest {

  Gson gson;

  HashModifierMethod hashModifierMethod;

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
  void initialize() {
    this.companyWithProjectDocumentContext = JsonPath
        .parse(this.gson.toJson(DataFactory.getCompanyWithProjects()));
    this.hashModifierMethod = new HashModifierMethod();
  }

  @Test
  void whenNoAlgorithmThenSha256() {
    final ParameterList parameters = new ParameterList();
    this.hashModifierMethod.doModification(this.companyWithProjectDocumentContext, "$.name",
        parameters);
    final Type companyType = new TypeToken<Company>() {
    }.getType();
    final Company company = this.gson.fromJson(this.companyWithProjectDocumentContext.jsonString(),
        companyType);
    assertEquals("4e65aa2ea1468bec48e812f3f76d8e1a30f7c762f8aa5b64ae0660e6f5103f0a",
        company.getName());
  }

  @Test
  void whenMD5thenMD5() {
    final ParameterList parameters = new ParameterList();
    parameters.add(new Parameter<>("algorithm", "md5"));
    this.hashModifierMethod.doModification(this.companyWithProjectDocumentContext, "$.name",
        parameters);
    final Type companyType = new TypeToken<Company>() {
    }.getType();
    final Company company = this.gson.fromJson(this.companyWithProjectDocumentContext.jsonString(),
        companyType);
    assertEquals("e7b1d5e1f09e015f693ad4e143e15d73", company.getName());
  }

  @Test
  void whenSHA1thenSHA1() {
    final ParameterList parameters = new ParameterList();
    parameters.add(new Parameter<>("algorithm", "sha1"));
    this.hashModifierMethod.doModification(this.companyWithProjectDocumentContext, "$.name",
        parameters);
    final Type companyType = new TypeToken<Company>() {
    }.getType();
    final Company company = this.gson.fromJson(this.companyWithProjectDocumentContext.jsonString(),
        companyType);
    assertEquals("c30715485cd0084f4f00b65dc9dfefbebb7d15fb", company.getName());
  }

  @Test
  void whenNullObject() {
    final ParameterList parameters = new ParameterList();
    final Object o = this.hashModifierMethod.doModification(null, parameters);
    assertEquals(null, o);
  }

  @Test
  void whenNumberThenHashed() {
    final ParameterList parameters = new ParameterList();
    final Object o = this.hashModifierMethod.doModification(123, parameters);
    assertEquals("a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3", o);
  }

}
