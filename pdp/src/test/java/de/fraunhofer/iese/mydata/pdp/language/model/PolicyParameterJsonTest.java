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

package de.fraunhofer.iese.mydata.pdp.language.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.pdp.test.Address;
import de.fraunhofer.iese.mydata.pdp.test.User;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

/**
 * The Class ParameterTest.
 */
class PolicyParameterJsonTest {

  // @Test
  // // @Ignore
  // public void test() throws IOException, URISyntaxException {
  // final Event event = new Event(new
  // ActionId("urn:action:test:local:showEmployee"));
  // event.addParameter(this.getUserParameter());
  // event.addParameter("userName", "Manu");
  // System.out.println(event.toJson(true));
  // }

  @Test
  void whenFirstCityIsRequestedLautreIsReturned() throws Exception {

    final PolicyParameter<String> parameter = new PolicyParameter<>("user", String.class);
    parameter.setFunctionsAndParameters(Collections.singletonList(new PolicyEvent<>("user", String.class, "$.addresses[0].city")));

    final Event event = new Event(new ActionId("urn:action:local:showEmployee"));
    event.addParameter(this.getUserParameter());

    final Object resultValue = parameter.evaluate(event).getValue();
    assertEquals("Lautre", resultValue);
  }

  @Test
  void whenFirstAddressIsRequestedAddressIsReturned() throws Exception {

    final PolicyParameter<Address> parameter = new PolicyParameter<>("user", Address.class);
    parameter.setFunctionsAndParameters(Collections.singletonList(new PolicyEvent<>("user", Address.class, "$.addresses[0]")));

    final User user = MyDataEntity.getGson().fromJson(this.readResourceFile("testUser.json"), User.class);

    final Event event = new Event(new ActionId("urn:action:local:showEmployee"));
    event.addParameter(this.getUserParameter());

    final Object resultValue = parameter.evaluate(event).getValue();

    final JsonElement received = new Gson().toJsonTree(resultValue);
    final JsonElement expected = new Gson().toJsonTree(user.addresses.get(0));

    assertEquals(expected, received);
  }

  @Test
  void whenAddressesAreRequestedThenAllAddressesAreReturned() throws Exception {

    final PolicyParameter<List> parameter = new PolicyParameter<>("user", List.class);
    parameter.setFunctionsAndParameters(Collections.singletonList(new PolicyEvent<>("user", List.class, "$.addresses")));

    final User user = MyDataEntity.getGson().fromJson(this.readResourceFile("testUser.json"), User.class);

    final Event event = new Event(new ActionId("urn:action:local:showEmployee"));
    event.addParameter(this.getUserParameter());

    final Object resultValue = parameter.evaluate(event).getValue();

    final JsonElement received = new Gson().toJsonTree(resultValue);
    final JsonElement expected = new Gson().toJsonTree(new Address[] {
        user.addresses.get(0), user.addresses.get(1)
    });

    assertEquals(expected, received);
  }

  @Test
  void whenObjectIsQueriedForStringParameterThenException() throws Exception {
    final PolicyParameter<String> parameter = new PolicyParameter<>("user", String.class);
    parameter.setFunctionsAndParameters(Collections.singletonList(new PolicyEvent<>("user", String.class, "$.addresses[0]")));
    final User user = MyDataEntity.getGson().fromJson(this.readResourceFile("testUser.json"), User.class);
    final Event event = new Event(new ActionId("urn:action:local:showEmployee"));
    event.addParameter(this.getUserParameter());
    assertThrows(EvaluationUndecidableException.class, () ->

      parameter.evaluate(event).getValue());
  }

  @Test
  void whenObjectDoesNotExistThenException() throws Exception {
    final PolicyParameter<String> parameter = new PolicyParameter<>("user", String.class);
    parameter.setFunctionsAndParameters(Collections.singletonList(new PolicyEvent<>("user", Address.class, "$.addresses[44]")));
    final User user = MyDataEntity.getGson().fromJson(this.readResourceFile("testUser.json"), User.class);
    final Event event = new Event(new ActionId("urn:action:local:showEmployee"));
    event.addParameter(this.getUserParameter());
    assertThrows(EvaluationUndecidableException.class, () ->

      parameter.evaluate(event).getValue());
  }

  @Test
  void whenListIsQueriedForStringParameterThenException() throws Exception {
    final PolicyParameter<String> parameter = new PolicyParameter<>("user", String.class);
    parameter.setFunctionsAndParameters(Collections.singletonList(new PolicyEvent<>("user", String.class, "$.addresses")));
    final User user = MyDataEntity.getGson().fromJson(this.readResourceFile("testUser.json"), User.class);
    final Event event = new Event(new ActionId("urn:action:local:showEmployee"));
    event.addParameter(this.getUserParameter());
    assertThrows(EvaluationUndecidableException.class, () ->

      parameter.evaluate(event).getValue());
  }

  @Test
  void whenStringParameterAndIntQueryThenException() throws Exception {
    final PolicyParameter<String> parameter = new PolicyParameter<>("user", String.class);
    parameter.setFunctionsAndParameters(Collections.singletonList(new PolicyEvent<>("user", String.class, "$.age")));
    final User user = MyDataEntity.getGson().fromJson(this.readResourceFile("testUser.json"), User.class);
    final Event event = new Event(new ActionId("urn:action:local:showEmployee"));
    event.addParameter(this.getUserParameter());
    assertThrows(EvaluationUndecidableException.class, () ->

      parameter.evaluate(event).getValue());
  }

  @Test
  void whenIntParameterAndStringQueryThenException() throws Exception {
    final PolicyParameter<Integer> parameter = new PolicyParameter<>("user", Integer.class);
    parameter.setFunctionsAndParameters(Collections.singletonList(new PolicyEvent<>("user", Integer.class, "$.name.firstName")));
    final User user = MyDataEntity.getGson().fromJson(this.readResourceFile("testUser.json"), User.class);
    final Event event = new Event(new ActionId("urn:action:local:showEmployee"));
    event.addParameter(this.getUserParameter());
    assertThrows(EvaluationUndecidableException.class, () ->

      parameter.evaluate(event).getValue());
  }

  @Test
  void userInUserOut() throws Exception {
    final de.fraunhofer.iese.mydata.policy.parameter.Parameter<?> p = this.getUserParameter();

    // System.err.println(p.getTypeName());

    assertEquals("de.fraunhofer.iese.mydata.pdp.java.test.Project", p.getTypeName());
    assertEquals(String.class, p.getValue().getClass());
  }

  private de.fraunhofer.iese.mydata.policy.parameter.Parameter getUserParameter() throws IOException, URISyntaxException {
    return MyDataEntity.fromJson(this.readResourceFile("unknownParameter.json"), de.fraunhofer.iese.mydata.policy.parameter.Parameter.class);
  }

  /**
   * Read resource file.
   *
   * @param file the file
   * @return the string
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws URISyntaxException the URI syntax exception
   */
  private String readResourceFile(String file) throws IOException, URISyntaxException {
    return new String(Files.readAllBytes(Paths.get(this.getClass().getClassLoader().getResource(file).toURI())));
  }

}
