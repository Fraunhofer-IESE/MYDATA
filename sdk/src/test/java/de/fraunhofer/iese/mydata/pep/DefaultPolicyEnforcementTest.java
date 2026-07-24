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

package de.fraunhofer.iese.mydata.pep;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyDecisionPoint;
import de.fraunhofer.iese.mydata.internal.IMyDataEnvironmentFullFace;
import de.fraunhofer.iese.mydata.pep.common.DecisionEnforcer;
import de.fraunhofer.iese.mydata.policy.decision.AuthorizationDecision;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.InhibitException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
class DefaultPolicyEnforcementTest {
  private DefaultPolicyEnforcementPoint testCandidate;

  @Mock
  private DecisionEnforcer enforcer;

  @Mock
  private IMyDataEnvironmentFullFace myDataEnvironment;

  @BeforeEach
  void setup() {
    this.testCandidate = new DefaultPolicyEnforcementPoint(this.myDataEnvironment, this.enforcer);
  }

  //TODO check what is the desired behaviour
  //  @Test
  //  void whenRemotePDPIsNull_ThenDecisionShouldBeNull()
  //      throws EvaluationUndecidableException, IOException {
  //    doReturn(Optional.empty()).when(this.myDataEnvironment).getPdp();
  //    assertThrows(EvaluationUndecidableException.class,
  //        () -> this.testCandidate.getDecision(Mockito.mock(Event.class)));
  //  }

  @Test
  void whenDecisionIsInhibit_ParametersShouldBeCleared() throws Exception {
    final IPolicyDecisionPoint iPolicyDecisionPoint = MockedPdpPmp
        .mockedPDP(AuthorizationDecision.getDecisionInhibit());
    doReturn(Optional.of(iPolicyDecisionPoint)).when(this.myDataEnvironment).getPdp();
    when(iPolicyDecisionPoint.getId()).thenReturn(new ComponentId("urn:component:mydata:pdp:pdp"));
    final Event event = Mockito.mock(Event.class);
    assertThrows(InhibitException.class, () -> this.testCandidate.enforce(event));
    verify(event).clearParameters();
  }
}
