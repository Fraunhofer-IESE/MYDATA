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

import de.fraunhofer.iese.mydata.DataFactory;
import de.fraunhofer.iese.mydata.User;
import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.pep.EventBuilder;
import de.fraunhofer.iese.mydata.pep.MockedPdpPmp;
import de.fraunhofer.iese.mydata.pep.PolicyEnforcementPoint;
import de.fraunhofer.iese.mydata.pep.common.DecisionEnforcer;
import de.fraunhofer.iese.mydata.pep.modifiers.basic.DeleteModifierMethod;
import de.fraunhofer.iese.mydata.pep.modifiers.string.AppendModifierMethod;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;

import org.junit.jupiter.api.Test;

public class JsonPathPolicyEnforcementPointJunitTest extends AbstractModifierTest {

  @Test
  public void JsonPathBasicTest() throws Exception {
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
        .withParameter("user", user, User.class).getEvent();
    policyEnforcementPoint.enforce(event);
    final User modifiedUser = (User) event.getParameterForName("user").getValue();
    assertEquals("Mr. Denis Feth", modifiedUser.getName());
    assertEquals(1, modifiedUser.getPhoneNo().length);
  }

}
