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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.fraunhofer.iese.mydata.DataFactory;
import de.fraunhofer.iese.mydata.User;
import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.pep.EventBuilder;
import de.fraunhofer.iese.mydata.pep.MockedPdpPmp;
import de.fraunhofer.iese.mydata.pep.PolicyEnforcementPoint;
import de.fraunhofer.iese.mydata.pep.common.DecisionEnforcer;
import de.fraunhofer.iese.mydata.pep.modifiers.basic.DeleteModifierMethod;
import de.fraunhofer.iese.mydata.pep.modifiers.string.AppendModifierMethod;
import de.fraunhofer.iese.mydata.policy.decision.AuthorizationDecision;
import de.fraunhofer.iese.mydata.policy.decision.DecisionId;
import de.fraunhofer.iese.mydata.policy.decision.Modifier;
import de.fraunhofer.iese.mydata.policy.decision.ModifierEngine;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.parameter.ModifierList;
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;

import org.junit.jupiter.api.Test;

public class PrimitiveAppendAndDeletePepJunitTest extends AbstractModifierTest {

  @Override
  protected AuthorizationDecision getAuthorizationDecision() {
    final AuthorizationDecision pdpAuthorizationDecision = mock(AuthorizationDecision.class);
    final ModifierList modifierList = new ModifierList();
    final Modifier userNameModifier = new Modifier("string-1");
    final ModifierEngine appendModifierEngine = new ModifierEngine("append");
    appendModifierEngine.addParameter(new Parameter<>("prefix", "Mr. "));
    userNameModifier.addEngine(appendModifierEngine);

    final Modifier firstPhoneNoModifier = new Modifier("user");
    firstPhoneNoModifier.setExpression("$.phoneNo[0]");
    final ModifierEngine deleteModifierEngine = new ModifierEngine("delete");
    firstPhoneNoModifier.addEngine(deleteModifierEngine);

    modifierList.add(userNameModifier);
    modifierList.add(firstPhoneNoModifier);
    when(pdpAuthorizationDecision.getModifiers()).thenReturn(modifierList);
    when(pdpAuthorizationDecision.getId()).thenReturn(new DecisionId("urn:decision:remotePDP"));
    when(pdpAuthorizationDecision.isEventAllowed()).thenReturn(true);

    return pdpAuthorizationDecision;
  }

  @Test
  public void jsonPathBasicTest() throws Exception {
    final DecisionEnforcer decisionEnforcer = new JsonPathDecisionEnforcer();
    decisionEnforcer.addModificationMethod(new AppendModifierMethod());
    decisionEnforcer.addModificationMethod(new DeleteModifierMethod());
    final PolicyEnforcementPoint policyEnforcementPoint = new PolicyEnforcementPoint(
        this.mockedMyDataEnvironment, decisionEnforcer,
        new ComponentId("urn:component:ind2uce:pep:test"),
        MockedPdpPmp.getPepInterfaceDescription(), MockedPdpPmp.getMethodInterfaceDescription(),
        true);
    final User user = DataFactory.getUser();
    final Event event = new EventBuilder(new ActionId("urn:action:ind2uce:junit"))
        .withParameter("string-1", "Hello", String.class).withParameter("user", user, User.class)
        .getEvent();
    policyEnforcementPoint.enforce(event);
    final String modifiedString = (String) event.getParameterForName("string-1").getValue();
    final User modifiedUser = (User) event.getParameterForName("user").getValue();
    assertEquals("Mr. Hello", modifiedString);
    assertEquals(1, modifiedUser.getPhoneNo().length);
  }

}
