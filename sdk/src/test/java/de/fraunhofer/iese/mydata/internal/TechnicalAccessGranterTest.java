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

package de.fraunhofer.iese.mydata.internal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.fraunhofer.iese.mydata.IMyDataEnvironment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TechnicalAccessGranterTest {

  @Test
  void testSupportedImplOkay() {
    final IMyDataEnvironmentFullFace myDataEnvironment = Mockito
        .mock(IMyDataEnvironmentFullFace.class);
    final IMyDataEnvironmentTechnicalAccess myDataEnvironmentTechnicalAccess = TechnicalAccessGranter
        .getTechnicalAccess(myDataEnvironment);
    assertNotNull(myDataEnvironmentTechnicalAccess);
  }

  @Test
  void testUnsupportedImplResultsInRuntimeException() {
    final IMyDataEnvironment myDataEnvironment = Mockito.mock(IMyDataEnvironment.class);
    assertThrows(RuntimeException.class, () -> {
      TechnicalAccessGranter.getTechnicalAccess(myDataEnvironment);
    });
  }

}
