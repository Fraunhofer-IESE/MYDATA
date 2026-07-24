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

package de.fraunhofer.iese.mydata.reactive.test.happy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.information.PepComponentInformation;
import de.fraunhofer.iese.mydata.component.information.method.MethodInterfaceDescription;
import de.fraunhofer.iese.mydata.component.interfaces.IBasicManagementService;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyDecisionPoint;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyEnforcementPoint;
import de.fraunhofer.iese.mydata.internal.IMyDataEnvironmentFullFace;
import de.fraunhofer.iese.mydata.pep.MockedPdpPmp;
import de.fraunhofer.iese.mydata.pep.PolicyEnforcementPoint;
import de.fraunhofer.iese.mydata.reactive.RxPepFactory;
import de.fraunhofer.iese.mydata.reactive.common.RxPep;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

public class RxPepPackageModifiersTest {

  private RxPep<MyRxPepDocuAPipackagesModifiers> rxPep;

  private MyRxPepDocuAPipackagesModifiers rxMyPepInterface;

  @Test
  public void ModifierAnnotationPackage() throws Exception {
    final IMyDataEnvironmentFullFace mockedMyDataEnvironment = Mockito
        .mock(IMyDataEnvironmentFullFace.class);
    final IBasicManagementService ipmp = MockedPdpPmp.mockedPMP();
    final IPolicyDecisionPoint ipdp = MockedPdpPmp.mockedPDP();
    doReturn(ipmp).when(mockedMyDataEnvironment).getPmp();
    doReturn(Optional.ofNullable(ipdp)).when(mockedMyDataEnvironment).getPdp();
    doReturn(new SolutionId("urn:solution:solution1")).when(mockedMyDataEnvironment)
        .getSolutionId();
    when(mockedMyDataEnvironment.registerPep(any(PepComponentInformation.class)))
        .thenReturn(new ComponentId("urn:component:solution1:pep:123"));

    this.rxPep = RxPepFactory.createRxPep(mockedMyDataEnvironment,
        MyRxPepDocuAPipackagesModifiers.class);

    this.rxPep.doRegisterAtPMP().blockingSubscribe((b) -> {
      // on next observable so only one object
      assertEquals(true, b);
      this.rxMyPepInterface = this.rxPep.createInstanceAPI();
    });
    assertNotNull(this.rxMyPepInterface);

    final IPolicyEnforcementPoint pep = this.rxPep.getPolicyEnforcementPoint();
    final Class<?> secretClass = pep.getClass();
    assertSame(PolicyEnforcementPoint.class, secretClass);
    final Field[] fields = secretClass.getDeclaredFields();

    for (final Field field : fields) {
      if (field.getName().equalsIgnoreCase("methodInterfaceDescriptions")) {
        // access to private fields of Pep
        field.setAccessible(true);
        final List<MethodInterfaceDescription> modifierInterfaceDescriptions = (List<MethodInterfaceDescription>) field
            .get(pep);
        assertEquals(6, modifierInterfaceDescriptions.size());
        return;
      }
    }
    fail();
  }

}
