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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.fraunhofer.iese.mydata.User;
import de.fraunhofer.iese.mydata.User.CreditCardInfo;
import de.fraunhofer.iese.mydata.pep.common.DecisionEnforcer;
import de.fraunhofer.iese.mydata.pep.enforce.JsonPathDecisionEnforcer;
import de.fraunhofer.iese.mydata.pep.modifiers.basic.DeleteModifierMethod;
import de.fraunhofer.iese.mydata.pep.modifiers.basic.ReplaceModifierMethod;
import de.fraunhofer.iese.mydata.pep.modifiers.string.AnagramModifierMethod;
import de.fraunhofer.iese.mydata.pep.modifiers.string.AppendModifierMethod;
import de.fraunhofer.iese.mydata.pep.modifiers.string.PasswordModifierMethod;
import de.fraunhofer.iese.mydata.policy.decision.AuthorizationDecision;
import de.fraunhofer.iese.mydata.policy.decision.Modifier;
import de.fraunhofer.iese.mydata.policy.decision.ModifierEngine;
import de.fraunhofer.iese.mydata.policy.exception.InhibitException;
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class BaseTest {

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
    de.addModificationMethod(new ReplaceModifierMethod());
    de.addModificationMethod(new AppendModifierMethod());
    de.addModificationMethod(new PasswordModifierMethod());
    de.addModificationMethod(new AnagramModifierMethod());
    de.addModificationMethod(new DeleteModifierMethod());

    return de;
  }

  @Test
  void modifyTest1() throws InhibitException {
    final User user = this.getUser();
    final DecisionEnforcer enforcer = this.getEnforcer();
    final AuthorizationDecision decision = new AuthorizationDecision();

    final ModifierEngine engine = new ModifierEngine("append",
        new ParameterList(new Parameter<>("prefix", "{}")));
    final List<ModifierEngine> modifiers = new ArrayList<>();
    modifiers.add(engine);
    final Modifier m = new Modifier("user", modifiers);
    m.setExpression("$.name");
    decision.addModifier(m);

    final ParameterList result = enforcer.enforce(decision,
        new ParameterList(new Parameter<>("user", user)));

    final User u1 = (User) result.getParameterValueForName("user");
    assertEquals("{}Jack Bauer", u1.getName());
  }

  @Test
  void modifyTest2() throws InhibitException {
    final User user = this.getUser();
    final DecisionEnforcer enforcer = this.getEnforcer();
    final AuthorizationDecision decision = new AuthorizationDecision();

    final ModifierEngine engine = new ModifierEngine("append",
        new ParameterList(new Parameter<>("prefix", "test"), new Parameter<>("suffix", "test1")));
    final List<ModifierEngine> modifiers = new ArrayList<>();
    modifiers.add(engine);
    final Modifier m = new Modifier("user", modifiers);
    m.setExpression("$.name");
    decision.addModifier(m);

    final ParameterList result = enforcer.enforce(decision,
        new ParameterList(new Parameter<>("user", user)));

    final User u1 = (User) result.getParameterValueForName("user");
    assertEquals("testJack Bauertest1", u1.getName());
  }

  @Test
  void modifyTest3() throws InhibitException {
    final User user = this.getUser();
    final DecisionEnforcer enforcer = this.getEnforcer();
    final AuthorizationDecision decision = new AuthorizationDecision();

    final ModifierEngine engine = new ModifierEngine("append",
        new ParameterList(new Parameter<>("prefix", "test"), new Parameter<>("suffix", "\"{}")));
    final List<ModifierEngine> modifiers = new ArrayList<>();
    modifiers.add(engine);
    final Modifier m = new Modifier("user", modifiers);
    m.setExpression("$.name");
    decision.addModifier(m);

    final ParameterList result = enforcer.enforce(decision,
        new ParameterList(new Parameter<>("user", user)));

    final User u1 = (User) result.getParameterValueForName("user");
    assertEquals("testJack Bauer\"{}", u1.getName());
  }

  @Test
  void modifyTest4() throws InhibitException {
    final DecisionEnforcer enforcer = this.getEnforcer();
    final AuthorizationDecision decision = new AuthorizationDecision();

    final ModifierEngine engine = new ModifierEngine("append",
        new ParameterList(new Parameter<>("prefix", "test"), new Parameter<>("suffix", "{}")));
    final List<ModifierEngine> modifiers = new ArrayList<>();
    modifiers.add(engine);
    final Modifier m = new Modifier("plain", modifiers);
    decision.addModifier(m);

    final ParameterList result = enforcer.enforce(decision,
        new ParameterList(new Parameter<>("plain", "text")));

    final String u1 = (String) result.getParameterValueForName("plain");
    assertEquals("testtext{}", u1);
  }

  @Test
  public void modifyTest5() throws InhibitException {
    final User user = this.getUser();
    final DecisionEnforcer enforcer = this.getEnforcer();
    final AuthorizationDecision decision = new AuthorizationDecision();

    final ModifierEngine engine = new ModifierEngine("append",
        new ParameterList(new Parameter<>("suffix", 123)));
    final List<ModifierEngine> modifiers = new ArrayList<>();
    modifiers.add(engine);
    final Modifier m = new Modifier("user", modifiers);
    m.setExpression("$.phoneNo[0]");
    decision.addModifier(m);

    final ParameterList result = enforcer.enforce(decision,
        new ParameterList(new Parameter<>("user", user)));

    final User u1 = (User) result.getParameterValueForName("user");
    assertEquals(2123, (long) u1.getPhoneNo()[0]);

  }

  @Test
  void modifyTest6() throws InhibitException {
    final User user = this.getUser();
    final DecisionEnforcer enforcer = this.getEnforcer();
    final AuthorizationDecision decision = new AuthorizationDecision();

    final ModifierEngine engine = new ModifierEngine("append",
        new ParameterList(new Parameter<>("suffix", "test")));
    final List<ModifierEngine> modifiers = new ArrayList<>();
    modifiers.add(engine);
    final Modifier m = new Modifier("user", modifiers);
    m.setExpression("$.phoneNo");
    decision.addModifier(m);

    assertThrows(InhibitException.class, () -> {
      enforcer.enforce(decision, new ParameterList(new Parameter<>("user", user)));
    });
  }

  @Test
  void modifyTest7() throws InhibitException {
    final DecisionEnforcer enforcer = this.getEnforcer();
    final AuthorizationDecision decision = new AuthorizationDecision();

    final ModifierEngine engine = new ModifierEngine("append",
        new ParameterList(new Parameter<>("prefix", "{")));
    final List<ModifierEngine> modifiers = new ArrayList<>();
    modifiers.add(engine);
    final Modifier m = new Modifier("plain", modifiers);
    decision.addModifier(m);

    final ParameterList result = enforcer.enforce(decision,
        new ParameterList(new Parameter<>("plain", "text")));

    final String u1 = (String) result.getParameterValueForName("plain");
    // System.err.println(u1);
    assertEquals("{text", u1);

  }

  @Test
  void modifyTest8() throws InhibitException {
    final DecisionEnforcer enforcer = this.getEnforcer();
    final AuthorizationDecision decision = new AuthorizationDecision();

    final ModifierEngine engine = new ModifierEngine("append",
        new ParameterList(new Parameter<>("prefix", "'{}'")));
    final List<ModifierEngine> modifiers = new ArrayList<>();
    modifiers.add(engine);
    final Modifier m = new Modifier("plain", modifiers);
    decision.addModifier(m);

    final ParameterList result = enforcer.enforce(decision,
        new ParameterList(new Parameter<>("plain", "text")));

    final String u1 = (String) result.getParameterValueForName("plain");
    assertEquals("'{}'text", u1);
  }

  //  @Ignore(value = "Replace modifier is only implemented to accept String. It returns String.")
  //  @Test
  //  void modifyTest9() throws InhibitException {
  //    final User user = this.getUser();
  //    final DecisionEnforcer enforcer = this.getEnforcer();
  //    final AuthorizationDecision decision = new AuthorizationDecision();
  //
  //    final ModifierEngine engine = new ModifierEngine("replace",
  //        new ParameterList(new Parameter<>("replaceWith", "[]")));
  //    final List<ModifierEngine> modifiers = new ArrayList<>();
  //    modifiers.add(engine);
  //    final Modifier m = new Modifier("user", modifiers);
  //    m.setExpression("$.phoneNo");
  //    decision.addModifier(m);
  //
  //    final ParameterList result = enforcer.enforce(decision,
  //        new ParameterList(new Parameter<>("user", user)));
  //    final User u1 = (User) result.getParameterValueForName("user");
  //    assertEquals(0, u1.getPhoneNo().length);
  //  }

  @Test
  void modifyTest10() throws InhibitException {
    final User user = this.getUser();
    final DecisionEnforcer enforcer = this.getEnforcer();
    final AuthorizationDecision decision = new AuthorizationDecision();

    final ModifierEngine engine = new ModifierEngine("replace",
        new ParameterList(new Parameter<>("replaceWith", "0180")));
    final List<ModifierEngine> modifiers = new ArrayList<>();
    modifiers.add(engine);
    final Modifier m = new Modifier("user", modifiers);
    m.setExpression("$.phoneNo[0]");
    decision.addModifier(m);

    final ParameterList result = enforcer.enforce(decision,
        new ParameterList(new Parameter<>("user", user)));
    final User u1 = (User) result.getParameterValueForName("user");
    assertEquals(180, (long) u1.getPhoneNo()[0]);
  }

  @Test
  void modifyTest11() throws InhibitException {
    final User user = this.getUser();
    final DecisionEnforcer enforcer = this.getEnforcer();
    final AuthorizationDecision decision = new AuthorizationDecision();

    final ModifierEngine engine = new ModifierEngine("replace",
        new ParameterList(new Parameter<>("replaceWith", 0180.234f)));
    final List<ModifierEngine> modifiers = new ArrayList<>();
    modifiers.add(engine);
    final Modifier m = new Modifier("user", modifiers);
    m.setExpression("$.phoneNo[0]");
    decision.addModifier(m);

    assertThrows(InhibitException.class, () -> {
      final ParameterList result = enforcer.enforce(decision,
          new ParameterList(new Parameter<>("user", user)));
      final User u1 = (User) result.getParameterValueForName("user");
    });
  }

  @Test
  void modifyTest12() throws InhibitException {
    final User user = this.getUser();
    user.getAccountDetails().put("Visa", new CreditCardInfo("1574", "sparkasse"));
    user.getAccountDetails().put("Master", new CreditCardInfo("3244", "dkb"));
    final DecisionEnforcer enforcer = this.getEnforcer();
    final AuthorizationDecision decision = new AuthorizationDecision();

    final ModifierEngine engine = new ModifierEngine("replace",
        new ParameterList(new Parameter<>("replaceWith", "volksbank")));
    final List<ModifierEngine> modifiers = new ArrayList<>();
    modifiers.add(engine);
    final Modifier m = new Modifier("user", modifiers);
    m.setExpression("$..bankName");
    decision.addModifier(m);

    final ParameterList result = enforcer.enforce(decision,
        new ParameterList(new Parameter<>("user", user)));
    final User u1 = (User) result.getParameterValueForName("user");
    assertEquals(2, u1.getAccountDetails().size());
    assertEquals("volksbank", u1.getAccountDetails().get("Visa").getBankName());
    assertEquals("volksbank", u1.getAccountDetails().get("Master").getBankName());

  }

  @Test
  void modifyTest13() throws InhibitException {
    final User user = this.getUser();
    user.getAccountDetails().put("Visa", new CreditCardInfo("1574", "sparkasse"));
    user.getAccountDetails().put("Master", new CreditCardInfo("3244", "dkb b"));
    final DecisionEnforcer enforcer = this.getEnforcer();
    final AuthorizationDecision decision = new AuthorizationDecision();

    final ModifierEngine engine = new ModifierEngine("append",
        new ParameterList(new Parameter<>("suffix", "-b")));
    final List<ModifierEngine> modifiers = new ArrayList<>();
    modifiers.add(engine);
    final Modifier m = new Modifier("user", modifiers);
    m.setExpression("$..bankName");
    decision.addModifier(m);

    final ParameterList result = enforcer.enforce(decision,
        new ParameterList(new Parameter<>("user", user)));
    final User u1 = (User) result.getParameterValueForName("user");

    assertEquals(2, u1.getAccountDetails().size());
    assertEquals("sparkasse-b", u1.getAccountDetails().get("Visa").getBankName());
    assertEquals("dkb b-b", u1.getAccountDetails().get("Master").getBankName());
  }

  @Test
  void modifyTest14() throws InhibitException {
    final User user = this.getUser();
    final DecisionEnforcer enforcer = this.getEnforcer();
    final AuthorizationDecision decision = new AuthorizationDecision();

    final ModifierEngine engine = new ModifierEngine("password");
    final List<ModifierEngine> modifiers = new ArrayList<>();
    modifiers.add(engine);
    final Modifier m = new Modifier("user", modifiers);
    m.setExpression("$.name");
    decision.addModifier(m);

    final ParameterList result = enforcer.enforce(decision,
        new ParameterList(new Parameter<>("user", user)));

    final User u1 = (User) result.getParameterValueForName("user");
    assertEquals("********", u1.getName());
  }

  @Test
  void modifyTest15() throws InhibitException {
    final User user = this.getUser();
    user.getAccountDetails().put("Visa", new CreditCardInfo("1574", "sparkasse"));
    user.getAccountDetails().put("Master", new CreditCardInfo("3244", "dkb"));
    final DecisionEnforcer enforcer = this.getEnforcer();
    final AuthorizationDecision decision = new AuthorizationDecision();

    final ModifierEngine engine = new ModifierEngine("password");
    final List<ModifierEngine> modifiers = new ArrayList<>();
    modifiers.add(engine);
    final Modifier m = new Modifier("user", modifiers);
    m.setExpression("$..bankName");
    decision.addModifier(m);

    final ParameterList result = enforcer.enforce(decision,
        new ParameterList(new Parameter<>("user", user)));
    final User u1 = (User) result.getParameterValueForName("user");
    assertEquals(2, u1.getAccountDetails().size());
    assertEquals("********", u1.getAccountDetails().get("Visa").getBankName());
    assertEquals("********", u1.getAccountDetails().get("Master").getBankName());
  }

  @Test
  void modifyTest16() throws InhibitException {
    final DecisionEnforcer enforcer = this.getEnforcer();
    final AuthorizationDecision decision = new AuthorizationDecision();

    final ModifierEngine engine = new ModifierEngine("password");
    final List<ModifierEngine> modifiers = new ArrayList<>();
    modifiers.add(engine);
    final Modifier m = new Modifier("user", modifiers);
    decision.addModifier(m);

    final ParameterList result = enforcer.enforce(decision,
        new ParameterList(new Parameter<>("user", "user123")));
    final String u1 = (String) result.getParameterValueForName("user");
    assertEquals("********", u1);
  }

  @Test
  void modifyTest17() throws InhibitException {
    final User user = this.getUser();
    user.getAccountDetails().put("Visa", new CreditCardInfo("1574", "sparkasse"));
    user.getAccountDetails().put("Master", new CreditCardInfo("3244", "dkb"));
    final DecisionEnforcer enforcer = this.getEnforcer();
    final AuthorizationDecision decision = new AuthorizationDecision();

    final ModifierEngine engine = new ModifierEngine("delete");
    final List<ModifierEngine> modifiers = new ArrayList<>();
    modifiers.add(engine);
    final Modifier m = new Modifier("user", modifiers);
    m.setExpression("$..bankName");
    decision.addModifier(m);

    final ParameterList result = enforcer.enforce(decision,
        new ParameterList(new Parameter<>("user", user)));
    final User u1 = (User) result.getParameterValueForName("user");
    assertEquals(2, u1.getAccountDetails().size());
    assertNull(u1.getAccountDetails().get("Visa").getBankName());
    assertNull(u1.getAccountDetails().get("Master").getBankName());
  }

  @Test
  void modifyTest18() throws InhibitException {
    final DecisionEnforcer enforcer = this.getEnforcer();
    final AuthorizationDecision decision = new AuthorizationDecision();

    final ModifierEngine engine = new ModifierEngine("delete");
    final List<ModifierEngine> modifiers = new ArrayList<>();
    modifiers.add(engine);
    final Modifier m = new Modifier("user", modifiers);
    decision.addModifier(m);

    final ParameterList result = enforcer.enforce(decision,
        new ParameterList(new Parameter<>("user", "blabla")));
    final String u1 = (String) result.getParameterValueForName("user");
    assertNull(u1);
  }

  @Test
  void modifyTest19() throws InhibitException {
    final DecisionEnforcer enforcer = this.getEnforcer();
    final AuthorizationDecision decision = new AuthorizationDecision();

    final ModifierEngine engine = new ModifierEngine("anagram",
        new ParameterList(new Parameter<>("percentage", 100)));
    final List<ModifierEngine> modifiers = new ArrayList<>();
    modifiers.add(engine);
    final Modifier m = new Modifier("user", modifiers);
    decision.addModifier(m);

    final ParameterList result = enforcer.enforce(decision,
        new ParameterList(new Parameter<>("user", "bla")));

    final String res = (String) result.getParameterValueForName("user");
    assertTrue(this.isAnagram(res, "bla"));
  }

  @Test
  void modifyTest20() throws InhibitException {
    final User user = this.getUser();
    user.getAccountDetails().put("Visa", new CreditCardInfo("1574", "sparkasse"));
    user.getAccountDetails().put("Master", new CreditCardInfo("3244", "dkb"));
    final DecisionEnforcer enforcer = this.getEnforcer();
    final AuthorizationDecision decision = new AuthorizationDecision();

    final ModifierEngine engine = new ModifierEngine("anagram",
        new ParameterList(new Parameter<>("percentage", 100)));
    final List<ModifierEngine> modifiers = new ArrayList<>();
    modifiers.add(engine);
    final Modifier m = new Modifier("user", modifiers);
    m.setExpression("$..bankName");
    decision.addModifier(m);

    final ParameterList result = enforcer.enforce(decision,
        new ParameterList(new Parameter<>("user", user)));
    final User u1 = (User) result.getParameterValueForName("user");
    assertEquals(2, u1.getAccountDetails().size());

    assertTrue(this.isAnagram(u1.getAccountDetails().get("Visa").getBankName(), "sparkasse"));
  }

  @Test
  void modifyTest21() throws InhibitException {
    final User user = new User();
    final DecisionEnforcer enforcer = this.getEnforcer();
    final AuthorizationDecision decision = new AuthorizationDecision();

    final ModifierEngine engine = new ModifierEngine("anagram",
        new ParameterList(new Parameter<>("percentage", 100)));
    final List<ModifierEngine> modifiers = new ArrayList<>();
    modifiers.add(engine);
    final Modifier m = new Modifier("user", modifiers);
    m.setExpression("$.tags");
    decision.addModifier(m);

    final Object a = enforcer.enforce(decision, new ParameterList(new Parameter<>("user", user)));
  }

  @Test
  void modifyTest22() throws InhibitException {
    final DecisionEnforcer enforcer = this.getEnforcer();
    final AuthorizationDecision decision = new AuthorizationDecision();

    final ModifierEngine engine = new ModifierEngine("replace",
        new ParameterList(new Parameter<>("replaceWith", "{}")));
    final List<ModifierEngine> modifiers = new ArrayList<>();
    modifiers.add(engine);
    final Modifier m = new Modifier("plain", modifiers);
    decision.addModifier(m);

    final ParameterList result = enforcer.enforce(decision,
        new ParameterList(new Parameter<>("plain", "text")));

    final String u1 = (String) result.getParameterValueForName("plain");
    assertEquals("{}", u1);
  }

  @Test
  void modifyTest23() throws InhibitException {
    final User user = this.getUser();
    final DecisionEnforcer enforcer = this.getEnforcer();
    final AuthorizationDecision decision = new AuthorizationDecision();

    final ModifierEngine engine = new ModifierEngine("replace",
        new ParameterList(new Parameter<>("replaceWith", "{}")));
    final List<ModifierEngine> modifiers = new ArrayList<>();
    modifiers.add(engine);
    final Modifier m = new Modifier("user", modifiers);
    m.setExpression("$.name");
    decision.addModifier(m);

    final ParameterList result = enforcer.enforce(decision,
        new ParameterList(new Parameter<>("user", user)));
    final User u1 = (User) result.getParameterValueForName("user");
    assertEquals("{}", u1.getName());
  }

  //  @Ignore(value = "Replace modifier is a JsonPrimitive modifier. It does not support JsonObject.")
  //  @Test
  //  public void modifyTest24() throws InhibitException {
  //    final User user = this.getUser();
  //    user.getAccountDetails().put("Visa", new CreditCardInfo("1574", "sparkasse"));
  //    user.getAccountDetails().put("Master", new CreditCardInfo("3244", "dkb"));
  //    final DecisionEnforcer enforcer = this.getEnforcer();
  //    final AuthorizationDecision decision = new AuthorizationDecision();
  //
  //    final ModifierEngine engine = new ModifierEngine("replace",
  //        new ParameterList(new Parameter<>("replaceWith", "{}")));
  //    final List<ModifierEngine> modifiers = new ArrayList<>();
  //    modifiers.add(engine);
  //    final Modifier m = new Modifier("user", modifiers);
  //    m.setExpression("$.accountDetails");
  //    decision.addModifier(m);
  //
  //    final ParameterList result = enforcer.enforce(decision,
  //        new ParameterList(new Parameter<>("user", user)));
  //    final User u1 = (User) result.getParameterValueForName("user");
  //    assertTrue(u1.getAccountDetails().isEmpty());
  //  }

  @Test
  void modifyTest25() throws InhibitException {
    final User user = this.getUser();
    user.getAccountDetails().put("Visa", new CreditCardInfo("1574", "sparkasse"));
    user.getAccountDetails().put("Master", new CreditCardInfo("3244", "dkb"));
    final DecisionEnforcer enforcer = this.getEnforcer();
    final AuthorizationDecision decision = new AuthorizationDecision();

    final ModifierEngine engine = new ModifierEngine("replace",
        new ParameterList(new Parameter<>("replaceWith", 42)));
    final List<ModifierEngine> modifiers = new ArrayList<>();
    modifiers.add(engine);
    final Modifier m = new Modifier("user", modifiers);
    m.setExpression("$.name");
    decision.addModifier(m);

    final ParameterList result = enforcer.enforce(decision,
        new ParameterList(new Parameter<>("user", user)));
    final User u1 = (User) result.getParameterValueForName("user");
    assertEquals("42", u1.getName());
  }

  @Test
  void modifyNotExistingExpression() throws InhibitException {
    final User user = this.getUser();
    user.setName(null);
    user.getAccountDetails().put("Visa", new CreditCardInfo("1574", "sparkasse"));
    user.getAccountDetails().put("Master", new CreditCardInfo("3244", "dkb"));
    final DecisionEnforcer enforcer = this.getEnforcer();
    final AuthorizationDecision decision = new AuthorizationDecision();

    final ModifierEngine engine = new ModifierEngine("replace",
        new ParameterList(new Parameter<>("replaceWith", 3)));
    final List<ModifierEngine> modifiers = new ArrayList<>();
    modifiers.add(engine);
    final Modifier m = new Modifier("user", modifiers);
    m.setExpression("$.name");
    decision.addModifier(m);

    final ParameterList result = enforcer.enforce(decision,
        new ParameterList(new Parameter<>("user", user)));
    final User u1 = (User) result.getParameterValueForName("user");
    assertNull(u1.getName());
    assertEquals("1574", u1.getAccountDetails().get("Visa").getPin());
  }

  private boolean isAnagram(String firstWord, String secondWord) {
    if (firstWord.equals(secondWord)) {
      return false;
    }
    final char[] word1 = firstWord.replaceAll("[\\s]", "").toCharArray();
    final char[] word2 = secondWord.replaceAll("[\\s]", "").toCharArray();
    Arrays.sort(word1);
    Arrays.sort(word2);
    return Arrays.equals(word1, word2);
  }

}
