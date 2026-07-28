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

package de.fraunhofer.iese.mydata.reactive.test.fail;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import de.fraunhofer.iese.mydata.component.interfaces.IBasicManagementService;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyDecisionPoint;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.internal.IMyDataEnvironmentFullFace;
import de.fraunhofer.iese.mydata.pep.MockedPdpPmp;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.reactive.RxPepFactory;
import de.fraunhofer.iese.mydata.reactive.common.IncorrectPepDescriptionError;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

public class RxPepFailJunitTest {

  @Test
  void test1() throws IOException, URISyntaxException, EvaluationUndecidableException,
      NoSuchEntityException {
    final IMyDataEnvironmentFullFace mockedMyDataEnvironment = Mockito
        .mock(IMyDataEnvironmentFullFace.class);
    final IBasicManagementService ipmp = MockedPdpPmp.mockedPMP();
    final IPolicyDecisionPoint ipdp = MockedPdpPmp.mockedPDP();
    doReturn(ipmp).when(mockedMyDataEnvironment).getPmp();
    doReturn(Optional.ofNullable(ipdp)).when(mockedMyDataEnvironment).getPdp();
    doReturn(new SolutionId("urn:solution:test")).when(mockedMyDataEnvironment).getSolutionId();

    assertThrows(IncorrectPepDescriptionError.class, () -> {
      RxPepFactory.createRxPep(mockedMyDataEnvironment, ExFailInterface.class);
    });
  }

  @Test
  void checkDuplicateEventDefinition_LeadsToError() throws IOException, URISyntaxException,
      EvaluationUndecidableException, NoSuchEntityException {
    final IMyDataEnvironmentFullFace mockedMyDataEnvironment = Mockito
        .mock(IMyDataEnvironmentFullFace.class);
    final IBasicManagementService ipmp = MockedPdpPmp.mockedPMP();
    final IPolicyDecisionPoint ipdp = MockedPdpPmp.mockedPDP();
    doReturn(ipmp).when(mockedMyDataEnvironment).getPmp();
    doReturn(Optional.ofNullable(ipdp)).when(mockedMyDataEnvironment).getPdp();
    doReturn(new SolutionId("urn:solution:test")).when(mockedMyDataEnvironment).getSolutionId();

    assertThrows(IncorrectPepDescriptionError.class, () -> RxPepFactory
        .createRxPep(mockedMyDataEnvironment, DuplicateActionFailInterface.class));
  }
}
