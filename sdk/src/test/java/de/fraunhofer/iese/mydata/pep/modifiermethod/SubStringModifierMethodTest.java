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

import de.fraunhofer.iese.mydata.User;
import de.fraunhofer.iese.mydata.User.CreditCardInfo;
import de.fraunhofer.iese.mydata.pep.common.DecisionEnforcer;
import de.fraunhofer.iese.mydata.pep.enforce.JsonPathDecisionEnforcer;
import de.fraunhofer.iese.mydata.pep.modifiers.string.SubStringModifierMethod;
import de.fraunhofer.iese.mydata.policy.decision.AuthorizationDecision;
import de.fraunhofer.iese.mydata.policy.decision.Modifier;
import de.fraunhofer.iese.mydata.policy.decision.ModifierEngine;
import de.fraunhofer.iese.mydata.policy.exception.InhibitException;
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class SubStringModifierMethodTest {
  /**
   * startIndex 2 endIndex 6 expects "2345"
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier01a() throws InhibitException {

    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("startIndex", 2));
    parameterList.add(new Parameter<>("endIndex", 6));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("2345", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * startIndex 2, endIndex 6, fillString "*",expects "*2345*"
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier01b() throws InhibitException {

    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("startIndex", 2));
    parameterList.add(new Parameter<>("endIndex", 6));
    parameterList.add(new Parameter<>("fillString", "*"));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("*2345*", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * startIndex 2 endIndex 6, fillString "*x", autofill true, expects "*x2345*x*x"
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier01c() throws InhibitException {

    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("startIndex", 2));
    parameterList.add(new Parameter<>("endIndex", 6));
    parameterList.add(new Parameter<>("fillString", "*x"));
    parameterList.add(new Parameter<>("autoFill", true));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("*x2345*x*x", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * startIndex 0 endIndex 3 expects "012"
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier02() throws InhibitException {
    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("startIndex", 0));
    parameterList.add(new Parameter<>("endIndex", 3));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("012", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * startIndex 0 endIndex 3, fillString "*-" expects "012*-"
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier02b() throws InhibitException {
    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("startIndex", 0));
    parameterList.add(new Parameter<>("endIndex", 3));
    parameterList.add(new Parameter<>("fillString", "*-"));
    parameterList.add(new Parameter<>("autoFill", false));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("012*-", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * startIndex 0, endIndex 3, fillString expects "012*******"
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier02c() throws InhibitException {
    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("startIndex", 0));
    parameterList.add(new Parameter<>("endIndex", 3));
    parameterList.add(new Parameter<>("fillString", "*"));
    parameterList.add(new Parameter<>("autoFill", true));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("012*******", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * startIndex -2 endIndex 0 expects "89"
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier03a() throws InhibitException {

    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("startIndex", -2));
    parameterList.add(new Parameter<>("endIndex", 0));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("89", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * startIndex -2 endIndex 0 expects "89"
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier03b() throws InhibitException {

    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("startIndex", -2));
    parameterList.add(new Parameter<>("endIndex", 0));
    parameterList.add(new Parameter<>("fillString", "x"));
    parameterList.add(new Parameter<>("autoFill", false));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("x89", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * startIndex -2 endIndex 0 expects "89"
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier03c() throws InhibitException {

    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("startIndex", -2));
    parameterList.add(new Parameter<>("endIndex", 0));
    parameterList.add(new Parameter<>("fillString", "xyz"));
    parameterList.add(new Parameter<>("autoFill", true));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("xyzxyzxy89", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * startIndex -3 endIndex -2 expects "7"
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier04() throws InhibitException {
    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("startIndex", -3));
    parameterList.add(new Parameter<>("endIndex", -2));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("7", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * startIndex -3 endIndex -2, fillString "xyz" expects "xyz7xyz"
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier04b() throws InhibitException {
    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("startIndex", -3));
    parameterList.add(new Parameter<>("endIndex", -2));
    parameterList.add(new Parameter<>("fillString", "xyz"));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("xyz7xyz", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * startIndex -3 endIndex -2, fillString "*", autoFill true expects "*******7**"
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier04c() throws InhibitException {
    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("startIndex", -3));
    parameterList.add(new Parameter<>("endIndex", -2));
    parameterList.add(new Parameter<>("fillString", "*"));
    parameterList.add(new Parameter<>("autoFill", true));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("*******7**", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * startIndex -2 endIndex 2 expects "0189"
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier05a() throws InhibitException {
    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("startIndex", -2));
    parameterList.add(new Parameter<>("endIndex", 2));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("0189", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * startIndex -2 endIndex 2, fillString "-**-" expects "01-**-89"
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier05b() throws InhibitException {
    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("startIndex", -2));
    parameterList.add(new Parameter<>("endIndex", 2));
    parameterList.add(new Parameter<>("fillString", "-**-"));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("01-**-89", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * startIndex -2 endIndex 2, fillString "X", autoFill true, expects "01XXXXXX89"
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier05c() throws InhibitException {
    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("startIndex", -2));
    parameterList.add(new Parameter<>("endIndex", 2));
    parameterList.add(new Parameter<>("fillString", "X"));
    parameterList.add(new Parameter<>("autoFill", true));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("01XXXXXX89", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * startIndex 2 endIndex -4 expects "2345"
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier06() throws InhibitException {
    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("startIndex", 2));
    parameterList.add(new Parameter<>("endIndex", -4));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("2345", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * startIndex 2 endIndex -4 expects "2345"
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier06b() throws InhibitException {
    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("startIndex", 2));
    parameterList.add(new Parameter<>("endIndex", -4));
    parameterList.add(new Parameter<>("fillString", "*"));
    parameterList.add(new Parameter<>("autoFill", false));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("*2345*", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * startIndex 2 endIndex -4, fill expects "xx2345xxxx"
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier06c() throws InhibitException {
    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("startIndex", 2));
    parameterList.add(new Parameter<>("endIndex", -4));
    parameterList.add(new Parameter<>("fillString", "x"));
    parameterList.add(new Parameter<>("autoFill", true));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("xx2345xxxx", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * startIndex 0 endIndex -5 expects "01234"
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier07() throws InhibitException {
    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("startIndex", 0));
    parameterList.add(new Parameter<>("endIndex", -5));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("01234", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * startIndex 0 endIndex -5 expects "01234"
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier07b() throws InhibitException {
    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("startIndex", 0));
    parameterList.add(new Parameter<>("endIndex", -5));
    parameterList.add(new Parameter<>("fillString", "#+#"));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("01234#+#", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * startIndex 0 endIndex -5 expects "01234"
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier07c() throws InhibitException {
    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("startIndex", 0));
    parameterList.add(new Parameter<>("endIndex", -5));
    parameterList.add(new Parameter<>("fillString", "#+#"));
    parameterList.add(new Parameter<>("autoFill", true));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("01234#+##+", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * startIndex 6 endIndex 2 expects ""
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier08() throws InhibitException {
    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("startIndex", 6));
    parameterList.add(new Parameter<>("endIndex", 2));
    parameterList.add(new Parameter<>("fillString", "xY"));
    parameterList.add(new Parameter<>("autoFill", true));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * startIndex 6 endIndex 2 expects ""
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier08b() throws InhibitException {
    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("startIndex", 6));
    parameterList.add(new Parameter<>("endIndex", 2));
    parameterList.add(new Parameter<>("fillString", "xY"));
    parameterList.add(new Parameter<>("autoFill", false));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * startIndex -3 endIndex -4 expects ""
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier09() throws InhibitException {
    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("startIndex", -3));
    parameterList.add(new Parameter<>("endIndex", -4));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * startIndex -3 endIndex -4 expects ""
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier09b() throws InhibitException {
    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("startIndex", -3));
    parameterList.add(new Parameter<>("endIndex", -4));
    parameterList.add(new Parameter<>("fillString", "*"));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * startIndex -3 endIndex -4 expects ""
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier09c() throws InhibitException {
    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("startIndex", -3));
    parameterList.add(new Parameter<>("endIndex", -4));
    parameterList.add(new Parameter<>("fillString", "*"));
    parameterList.add(new Parameter<>("autoFill", true));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * startIndex 0 endIndex 0 expects "0123456789"
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier10() throws InhibitException {
    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("startIndex", 0));
    parameterList.add(new Parameter<>("endIndex", 0));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("0123456789", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * startIndex 0 endIndex 0 expects "0123456789"
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier10b() throws InhibitException {
    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("startIndex", 0));
    parameterList.add(new Parameter<>("endIndex", 0));
    parameterList.add(new Parameter<>("fillString", "*"));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("0123456789", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * startIndex 0 endIndex 0 expects "0123456789"
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier10c() throws InhibitException {
    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("startIndex", 0));
    parameterList.add(new Parameter<>("endIndex", 0));
    parameterList.add(new Parameter<>("fillString", "*"));
    parameterList.add(new Parameter<>("autoFill", true));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("0123456789", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * no startIndex ,no endIndex expects "0123456789"
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier11() throws InhibitException {
    final ParameterList parameterList = new ParameterList();

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("0123456789", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * no startIndex ,no endIndex expects "0123456789"
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier11b() throws InhibitException {
    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("fillString", "*"));
    parameterList.add(new Parameter<>("autoFill", false));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("0123456789", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * no startIndex ,no endIndex expects "0123456789"
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier11c() throws InhibitException {
    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("fillString", "*"));
    parameterList.add(new Parameter<>("autoFill", true));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("0123456789", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * startIndex -3, no endIndex expects "789"
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier12() throws InhibitException {
    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("startIndex", -3));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("789", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * startIndex -3, no endIndex expects "789"
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier12b() throws InhibitException {
    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("startIndex", -3));
    parameterList.add(new Parameter<>("fillString", "*"));
    parameterList.add(new Parameter<>("autoFill", false));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("*789", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * startIndex -3, no endIndex expects "789"
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier12c() throws InhibitException {
    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("startIndex", -3));
    parameterList.add(new Parameter<>("fillString", "*"));
    parameterList.add(new Parameter<>("autoFill", true));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("*******789", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * no startIndex , endIndex 4 expects "0123"
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier13() throws InhibitException {
    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("endIndex", 4));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("0123", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * no startIndex , endIndex 4 expects "0123"
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier13b() throws InhibitException {
    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("endIndex", 4));
    parameterList.add(new Parameter<>("fillString", "-"));
    parameterList.add(new Parameter<>("autoFill", false));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("0123-", modUser.getAccountDetails().get("Visa").getPin());
  }

  /**
   * no startIndex , endIndex 4 expects "0123"
   *
   * @throws InhibitException
   */
  @Test
  void testSubstringModifier13c() throws InhibitException {
    final ParameterList parameterList = new ParameterList();
    parameterList.add(new Parameter<>("endIndex", 4));
    parameterList.add(new Parameter<>("fillString", "-"));
    parameterList.add(new Parameter<>("autoFill", true));

    final AuthorizationDecision decision = this.createSubstringAuthorizationDecision("$..pin",
        parameterList);
    final ParameterList result = this.enforce(decision, this.getUser());
    final User modUser = (User) result.getParameterValueForName("user");

    assertEquals("0123------", modUser.getAccountDetails().get("Visa").getPin());
  }

  private User getUser() {
    final User user = new User();
    user.setName("Jack Bauer");

    user.setPhoneNo(new Long[] {
        2L, 2L, 34L, 343L, 3L
    });
    user.getTags().add("bla");
    user.getAccountDetails().put("Visa", new CreditCardInfo("0123456789", "sparkasse"));
    user.getAccountDetails().put("Master", new CreditCardInfo("1234567890", "dkb b"));
    return user;
  }

  private DecisionEnforcer getEnforcer() {
    final DecisionEnforcer decisionEnforcer = new JsonPathDecisionEnforcer();
    decisionEnforcer.addModificationMethod(new SubStringModifierMethod());
    return decisionEnforcer;
  }

  private AuthorizationDecision createSubstringAuthorizationDecision(String expression,
      ParameterList parameterList) {

    final ModifierEngine engine = new ModifierEngine("substring", parameterList);
    final List<ModifierEngine> modifierEngines = new ArrayList<>();
    modifierEngines.add(engine);
    final Modifier modifier = new Modifier("user", modifierEngines);
    modifier.setExpression(expression);
    final AuthorizationDecision decision = new AuthorizationDecision();
    decision.addModifier(modifier);
    return decision;
  }

  private ParameterList enforce(AuthorizationDecision decision, Object user)
      throws InhibitException {
    final DecisionEnforcer enforcer = this.getEnforcer();
    return enforcer.enforce(decision, new ParameterList(new Parameter<>("user", user)));
  }

}
