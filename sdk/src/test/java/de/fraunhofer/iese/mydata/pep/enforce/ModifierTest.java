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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.fraunhofer.iese.mydata.internal.IMyDataEnvironmentFullFace;
import de.fraunhofer.iese.mydata.pep.DefaultPolicyEnforcementPoint;
import de.fraunhofer.iese.mydata.pep.modifiers.string.AppendModifierMethod;
import de.fraunhofer.iese.mydata.pep.testdata.model.Person;
import de.fraunhofer.iese.mydata.pep.testdata.model.Project;
import de.fraunhofer.iese.mydata.pep.testdata.model.Role;
import de.fraunhofer.iese.mydata.policy.decision.AuthorizationDecision;
import de.fraunhofer.iese.mydata.policy.decision.DecisionId;
import de.fraunhofer.iese.mydata.policy.decision.Modifier;
import de.fraunhofer.iese.mydata.policy.decision.ModifierEngine;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.InhibitException;
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;

public class ModifierTest {

  @Test
  public void test() throws InhibitException {
    final IMyDataEnvironmentFullFace mockedMyDataEnvironment = Mockito
        .mock(IMyDataEnvironmentFullFace.class);

    assertTrue(true);
    final JsonPathDecisionEnforcer enforcer = new JsonPathDecisionEnforcer();
    enforcer.addModificationMethod(new AppendModifierMethod());

    final DefaultPolicyEnforcementPoint pep = new DefaultPolicyEnforcementPoint(
        mockedMyDataEnvironment, enforcer);

    final Event e = new Event(new ActionId("urn:action:cs4:show-projects"));

    final Person leader = new Person("2", "Hans", "wurst", Role.CSM);
    final Project p = new Project("test", "test", leader, 100000.0f);
    e.addParameter("project", p);

    final ArrayList<ModifierEngine> ml = new ArrayList<>();
    ml.add(new ModifierEngine("append", new ParameterList(new Parameter<>("suffix", "test"))));
    final Modifier m = new Modifier("project", ml);
    m.setExpression("$.projectLeader.firstName");
    final AuthorizationDecision d = new AuthorizationDecision(new DecisionId("urn:decision:1"),
        true, null, m);
    pep.enforceDecision(e, d);
    // System.err.println(e.toJson(true));

    // System.err.println(d.toJson(true));

    final Parameter res = e.getParameterForName("project");
    assertNotNull(res);
    assertTrue(res.getValue() instanceof Project);
    assertEquals("Hanstest", ((Project) res.getValue()).getProjectLeader().getFirstName());

  }

}
