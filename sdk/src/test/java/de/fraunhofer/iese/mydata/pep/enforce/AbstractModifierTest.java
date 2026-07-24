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

import static org.mockito.Mockito.doReturn;

import de.fraunhofer.iese.mydata.component.interfaces.IBasicManagementService;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyDecisionPoint;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.internal.IMyDataEnvironmentFullFace;
import de.fraunhofer.iese.mydata.pep.MockedPdpPmp;
import de.fraunhofer.iese.mydata.policy.decision.AuthorizationDecision;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

public abstract class AbstractModifierTest {

  protected IMyDataEnvironmentFullFace mockedMyDataEnvironment;

  @BeforeEach
  public void init() throws IOException, URISyntaxException, IllegalArgumentException,
      EvaluationUndecidableException, NoSuchEntityException {
    this.mockedMyDataEnvironment = Mockito.mock(IMyDataEnvironmentFullFace.class);
    final IBasicManagementService ipmp = MockedPdpPmp.mockedPMP();
    final IPolicyDecisionPoint ipdp = MockedPdpPmp.mockedPDP(this.getAuthorizationDecision());
    doReturn(ipmp).when(this.mockedMyDataEnvironment).getPmp();
    doReturn(Optional.ofNullable(ipdp)).when(this.mockedMyDataEnvironment).getPdp();
  }

  /**
   * Override if tests need some specific other than {@link MockedPdpPmp#getAuthorizationDecision()}
   */
  protected AuthorizationDecision getAuthorizationDecision() {
    return MockedPdpPmp.getAuthorizationDecision();
  }
}
