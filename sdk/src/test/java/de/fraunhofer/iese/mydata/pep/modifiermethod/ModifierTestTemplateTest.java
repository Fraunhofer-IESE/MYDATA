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

import de.fraunhofer.iese.mydata.User;
import de.fraunhofer.iese.mydata.User.CreditCardInfo;
import de.fraunhofer.iese.mydata.pep.common.DecisionEnforcer;
import de.fraunhofer.iese.mydata.pep.enforce.JsonPathDecisionEnforcer;
import de.fraunhofer.iese.mydata.pep.modifiers.basic.ReplaceModifierMethod;
import de.fraunhofer.iese.mydata.pep.modifiers.string.AppendModifierMethod;
import de.fraunhofer.iese.mydata.policy.decision.AuthorizationDecision;
import de.fraunhofer.iese.mydata.policy.decision.Modifier;
import de.fraunhofer.iese.mydata.policy.decision.ModifierEngine;
import de.fraunhofer.iese.mydata.policy.exception.InhibitException;
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class ModifierTestTemplateTest {

  private User getUser() {
    final User u = new User();
    u.setName("Jack Bauer");

    u.setPhoneNo(new Long[] {
        2L, 2L, 34L, 343L, 3L
    });

    final List<String> tags = new ArrayList<>();
    tags.add("bla");
    u.setTags(tags);
    return u;
  }

  private DecisionEnforcer getEnforcer() {
    final DecisionEnforcer de = new JsonPathDecisionEnforcer();
    de.addModificationMethod(new AppendModifierMethod());
    de.addModificationMethod(new ReplaceModifierMethod());

    return de;
  }

  @Test
  void testPlain() throws InhibitException {
    final DecisionEnforcer enforcer = this.getEnforcer();
    final AuthorizationDecision decision = new AuthorizationDecision();

    final ModifierEngine engine = new ModifierEngine("append", new ParameterList(
        new Parameter<String>("prefix", "test"), new Parameter<String>("suffix", "{}")));
    final List<ModifierEngine> modifiers = new ArrayList<>();
    modifiers.add(engine);
    final Modifier m = new Modifier("plain", modifiers);
    decision.addModifier(m);

    final ParameterList result = enforcer.enforce(decision,
        new ParameterList(new Parameter<String>("plain", "text")));

    final String u1 = (String) result.getParameterValueForName("plain");
    assertEquals("testtext{}", u1);
  }

  @Test
  void testModifyName() throws InhibitException {
    final User user = this.getUser();
    final DecisionEnforcer enforcer = this.getEnforcer();
    final AuthorizationDecision decision = new AuthorizationDecision();

    final ModifierEngine engine = new ModifierEngine("append",
        new ParameterList(new Parameter<String>("prefix", "{}")));
    final List<ModifierEngine> modifiers = new ArrayList<>();
    modifiers.add(engine);
    final Modifier m = new Modifier("user", modifiers);
    m.setExpression("$.name");
    decision.addModifier(m);

    final ParameterList result = enforcer.enforce(decision,
        new ParameterList(new Parameter<User>("user", user)));

    final User u1 = (User) result.getParameterValueForName("user");
    assertEquals("{}Jack Bauer", u1.getName());
  }

  @Disabled("Replace modifier is a JsonPrimitive modifier. It does not support JsonObject.")
  @Test
  void testReplaceObject() throws InhibitException {
    final User user = this.getUser();
    user.getAccountDetails().put("Visa", new CreditCardInfo("1574", "sparkasse"));
    user.getAccountDetails().put("Master", new CreditCardInfo("3244", "dkb"));
    final DecisionEnforcer enforcer = this.getEnforcer();
    final AuthorizationDecision decision = new AuthorizationDecision();

    final ModifierEngine engine = new ModifierEngine("replace",
        new ParameterList(new Parameter<String>("replaceWith", "{}")));
    final List<ModifierEngine> modifiers = new ArrayList<>();
    modifiers.add(engine);
    final Modifier m = new Modifier("user", modifiers);
    m.setExpression("$.accountDetails");
    decision.addModifier(m);

    final ParameterList result = enforcer.enforce(decision,
        new ParameterList(new Parameter<User>("user", user)));
    final User u1 = (User) result.getParameterValueForName("user");
    assertTrue(u1.getAccountDetails().isEmpty());
  }

  @Test
  void testModifyFirstPhoneNumber() throws InhibitException {
    final User user = this.getUser();
    final DecisionEnforcer enforcer = this.getEnforcer();
    final AuthorizationDecision decision = new AuthorizationDecision();

    final ModifierEngine engine = new ModifierEngine("append",
        new ParameterList(new Parameter<Integer>("suffix", 123)));
    final List<ModifierEngine> modifiers = new ArrayList<>();
    modifiers.add(engine);
    final Modifier m = new Modifier("user", modifiers);
    m.setExpression("$.phoneNo[0]");
    decision.addModifier(m);

    final ParameterList result = enforcer.enforce(decision,
        new ParameterList(new Parameter<User>("user", user)));

    final User u1 = (User) result.getParameterValueForName("user");
    assertEquals(2123L, u1.getPhoneNo()[0]);
  }

  @Test
  void testModifyObject() throws InhibitException {
    final User user = this.getUser();
    final DecisionEnforcer enforcer = this.getEnforcer();
    final AuthorizationDecision decision = new AuthorizationDecision();

    final ModifierEngine engine = new ModifierEngine("append",
        new ParameterList(new Parameter<String>("suffix", "test")));
    final List<ModifierEngine> modifiers = new ArrayList<>();
    modifiers.add(engine);
    final Modifier m = new Modifier("user", modifiers);
    m.setExpression("$");
    decision.addModifier(m);

    assertThrows(InhibitException.class,
        () -> enforcer.enforce(decision, new ParameterList(new Parameter<User>("user", user))));
  }

  @Test
  void testModifyPhoneNumbers() throws InhibitException {
    final User user = this.getUser();
    final DecisionEnforcer enforcer = this.getEnforcer();
    final AuthorizationDecision decision = new AuthorizationDecision();

    final ModifierEngine engine = new ModifierEngine("append",
        new ParameterList(new Parameter<String>("suffix", "test")));
    final List<ModifierEngine> modifiers = new ArrayList<>();
    modifiers.add(engine);
    final Modifier m = new Modifier("user", modifiers);
    m.setExpression("$.phoneNo");
    decision.addModifier(m);

    assertThrows(InhibitException.class,
        () -> enforcer.enforce(decision, new ParameterList(new Parameter<User>("user", user))));
  }

  @Test
  void testModifyBankNames() throws InhibitException {
    final User user = this.getUser();
    user.getAccountDetails().put("Visa", new CreditCardInfo("1574", "sparkasse"));
    user.getAccountDetails().put("Master", new CreditCardInfo("3244", "dkb"));
    final DecisionEnforcer enforcer = this.getEnforcer();
    final AuthorizationDecision decision = new AuthorizationDecision();

    final ModifierEngine engine = new ModifierEngine("append",
        new ParameterList(new Parameter<String>("suffix", "-Superbank")));
    final List<ModifierEngine> modifiers = new ArrayList<>();
    modifiers.add(engine);
    final Modifier m = new Modifier("user", modifiers);
    m.setExpression("$..bankName");
    decision.addModifier(m);

    final ParameterList result = enforcer.enforce(decision,
        new ParameterList(new Parameter<User>("user", user)));
    final User u1 = (User) result.getParameterValueForName("user");
    assertEquals(2, u1.getAccountDetails().size());
    assertEquals("sparkasse-Superbank", u1.getAccountDetails().get("Visa").getBankName());
    assertEquals("dkb-Superbank", u1.getAccountDetails().get("Master").getBankName());

  }

}
