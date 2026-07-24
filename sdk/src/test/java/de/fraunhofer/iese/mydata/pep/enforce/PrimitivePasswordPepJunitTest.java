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

import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.information.method.InputParameterDescription;
import de.fraunhofer.iese.mydata.component.information.method.PepInterfaceDescription;
import de.fraunhofer.iese.mydata.pep.EventBuilder;
import de.fraunhofer.iese.mydata.pep.MockedPdpPmp;
import de.fraunhofer.iese.mydata.pep.PolicyEnforcementPoint;
import de.fraunhofer.iese.mydata.pep.common.DecisionEnforcer;
import de.fraunhofer.iese.mydata.pep.modifiers.string.PasswordModifierMethod;
import de.fraunhofer.iese.mydata.policy.decision.AuthorizationDecision;
import de.fraunhofer.iese.mydata.policy.decision.DecisionId;
import de.fraunhofer.iese.mydata.policy.decision.Modifier;
import de.fraunhofer.iese.mydata.policy.decision.ModifierEngine;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.parameter.ModifierList;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class PrimitivePasswordPepJunitTest extends AbstractModifierTest {

  private static List<PepInterfaceDescription> getPepInterfaceDescription() {
    final List<PepInterfaceDescription> pepInterfaceDescriptions = new ArrayList<>();
    final InputParameterDescription messageInputParameterDescription = new InputParameterDescription(
        "password", "this is description of message", String.class);
    final List<InputParameterDescription> inputParameterDescriptions = new ArrayList<>();
    inputParameterDescriptions.add(messageInputParameterDescription);
    final PepInterfaceDescription pepInterfaceDescription = new PepInterfaceDescription(
        new ActionId("urn:solution:ind2uce:test"), true, "", inputParameterDescriptions);
    pepInterfaceDescriptions.add(pepInterfaceDescription);
    return pepInterfaceDescriptions;
  }

  @Override
  protected AuthorizationDecision getAuthorizationDecision() {
    final AuthorizationDecision pdpAuthorizationDecision = mock(AuthorizationDecision.class);
    final ModifierList modifierList = new ModifierList();
    final Modifier messageModifier = new Modifier("password");
    final ModifierEngine passwordModifierEngine = new ModifierEngine("password");
    messageModifier.addEngine(passwordModifierEngine);
    modifierList.add(messageModifier);
    when(pdpAuthorizationDecision.getModifiers()).thenReturn(modifierList);
    when(pdpAuthorizationDecision.getId()).thenReturn(new DecisionId("urn:decision:remotePDP"));
    when(pdpAuthorizationDecision.isEventAllowed()).thenReturn(true);

    return pdpAuthorizationDecision;
  }

  @Test
  public void primitiveTypeWithReplaceModifier() throws Exception {
    final DecisionEnforcer decisionEnforcer = new JsonPathDecisionEnforcer();
    decisionEnforcer.addModificationMethod(new PasswordModifierMethod());
    final PolicyEnforcementPoint policyEnforcementPoint = new PolicyEnforcementPoint(
        this.mockedMyDataEnvironment, decisionEnforcer,
        new ComponentId("urn:component:ind2uce:pep:test"), getPepInterfaceDescription(),
        MockedPdpPmp.getMethodInterfaceDescription(), true);
    final Event event = new EventBuilder(new ActionId("urn:action:ind2uce:junit"))
        .withParameter("password", "&Sainik234%", String.class).getEvent();
    policyEnforcementPoint.enforce(event);
    final String modifiedString = (String) event.getParameterForName("password").getValue();
    assertEquals("********", modifiedString);
  }

}
