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

import static org.junit.jupiter.api.Assertions.assertTrue;

import de.fraunhofer.iese.mydata.DataFactory;
import de.fraunhofer.iese.mydata.pep.modifiers.string.AppendModifierMethod;
import de.fraunhofer.iese.mydata.pep.testdata.model.Person;
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class AppendModifierMethodTest {

  private final String suffix = "_suf";

  private final String prefix = "pre_";

  List<String> userNames;

  private DocumentContext documentContext;

  @BeforeEach
  void initialize() {
    this.documentContext = JsonPath.parse(new Gson().toJson(DataFactory.getUsers()));
    this.userNames = this.documentContext.read("$..firstName", List.class);
    final String a = "";
  }

  @Test
  void modifyString() {
    List<String> expectedResult = this.userNames.stream().collect(Collectors.toList());
    expectedResult = expectedResult.stream().map(user -> this.prefix + user + this.suffix)
        .collect(Collectors.toList());

    final ParameterList parameterList = new ParameterList(new Parameter("suffix", this.suffix),
        new Parameter("prefix", this.prefix));
    final AppendModifierMethod modifierMethod = new AppendModifierMethod();

    this.documentContext = modifierMethod.doModification(this.documentContext, "$..firstName",
        parameterList);

    final Type token = new TypeToken<HashMap<String, Person>>() {
    }.getType();

    final HashMap<String, Person> modifiedUserName = new Gson()
        .fromJson(this.documentContext.jsonString(), token);

    for (final String username : expectedResult) {
      boolean contained = false;
      for (final Person user : modifiedUserName.values()) {
        if (user.getFirstName().equals(username)) {
          contained = true;
          break;
        }
      }
      assertTrue(contained);
    }

  }

  @Test
  void modifyStringPrefix() {
    List<String> expectedResult = this.userNames.stream().collect(Collectors.toList());
    expectedResult = expectedResult.stream().map(user -> this.prefix + user)
        .collect(Collectors.toList());

    final ParameterList parameterList = new ParameterList(new Parameter("prefix", this.prefix));

    final AppendModifierMethod modifierMethod = new AppendModifierMethod();
    modifierMethod.doModification(this.documentContext, "$..firstName", parameterList);

    final List<String> modifiedUserName = this.documentContext.read("$..firstName", List.class);

    assertTrue(expectedResult.stream().collect(Collectors.toList()).containsAll(modifiedUserName));
  }

  @Test
  void modifyStringSuffix() {
    List<String> expectedResult = this.userNames.stream().collect(Collectors.toList());
    expectedResult = expectedResult.stream().map(user -> user + this.suffix)
        .collect(Collectors.toList());

    final ParameterList parameterList = new ParameterList(new Parameter("suffix", this.suffix));

    final AppendModifierMethod modifierMethod = new AppendModifierMethod();
    modifierMethod.doModification(this.documentContext, "$..firstName", parameterList);

    final List<String> modifiedUserName = this.documentContext.read("$..firstName", List.class);

    assertTrue(expectedResult.stream().collect(Collectors.toList()).containsAll(modifiedUserName));
  }
}
