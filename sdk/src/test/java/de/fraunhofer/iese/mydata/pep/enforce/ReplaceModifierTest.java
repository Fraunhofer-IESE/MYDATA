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

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.fraunhofer.iese.mydata.pep.modifiers.basic.ReplaceModifierMethod;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests for replace modifier
 */
public class ReplaceModifierTest {

  private static final Configuration config = new Configuration.ConfigurationBuilder()
      .jsonProvider(new GsonJsonProvider()).mappingProvider(new GsonMappingProvider()).build();

  private final ReplaceModifierMethod testCandidate = new ReplaceModifierMethod();

  private final String json = "{\"userId\":\"foreman1\",\"role\":{\"name\":\"FOREMAN\"},\"firstName\":\"Florian\",\"lastName\":\"Finke\",\"phoneNumber\":[{\"type\":\"BUSINESS\",\"number\":\"+49 443 43123\"},{\"type\":\"PRIVATE\",\"number\":\"+49 443 1475523\"}],\"addresses\":[{\"type\":\"PRIVATE\",\"name\":\"foreman1_address1\",\"street\":\"Mainstr.\",\"number\":\"28\",\"zipCode\":\"67655\",\"city\":\"Kaiserslautern\",\"country\":\"Germany\"},{\"type\":\"BUSINESS\",\"name\":\"foreman1_address2\",\"street\":\"Hauptstr.\",\"number\":\"28\",\"zipCode\":\"67663\",\"city\":\"Kaiserslautern\",\"country\":\"Germany\"}]}";

  private final String expression = "$.phoneNumber[?(@.type == \"PRIVATE\")].number";

  @Test
  public void replace() throws IOException {
    final ParameterList parameters = new ParameterList();
    final String toReplaceWith = "+49 XXX";
    parameters.addParameter("replaceWith", toReplaceWith);
    final DocumentContext documentContext = this.testCandidate
        .doModification(JsonPath.parse(this.json, config), this.expression, parameters);
    final Object read = documentContext.read(this.expression);
    assertEquals("[\"" + toReplaceWith + "\"]", read.toString());

  }
}
