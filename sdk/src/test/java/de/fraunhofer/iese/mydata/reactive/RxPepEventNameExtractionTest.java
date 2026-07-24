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

package de.fraunhofer.iese.mydata.reactive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.information.PepComponentInformation;
import de.fraunhofer.iese.mydata.component.interfaces.IBasicManagementService;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyDecisionPoint;
import de.fraunhofer.iese.mydata.internal.IMyDataEnvironmentFullFace;
import de.fraunhofer.iese.mydata.policy.decision.AuthorizationDecision;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.reactive.common.RxPep;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class RxPepEventNameExtractionTest {
  // classes under test: AbstractRxPep, ReactivePep, RxPepFactory

  private static RxPep<PepForEventNameTesting> rxPep;

  private static PepForEventNameTesting pepInstance;

  private static PepComponentInformation registeredComponentInformation;

  private static IPolicyDecisionPoint mockedPdp;

  @BeforeAll
  public static void initClass() throws Exception {
    final IMyDataEnvironmentFullFace mockedMyDataEnvironment = Mockito
        .mock(IMyDataEnvironmentFullFace.class);
    final IBasicManagementService ipmp = Mockito.mock(IBasicManagementService.class);
    mockedPdp = Mockito.mock(IPolicyDecisionPoint.class);
    final ComponentId componentId = new ComponentId("urn:component:test:pep:my-pep");
    doReturn(ipmp).when(mockedMyDataEnvironment).getPmp();
    doReturn(Optional.ofNullable(mockedPdp)).when(mockedMyDataEnvironment).getPdp();

    when(mockedMyDataEnvironment.registerPep(any(PepComponentInformation.class)))
        .thenAnswer(invocation -> {
          registeredComponentInformation = invocation.getArgument(0);
          return componentId;
        });
    // initialize rxPep

    rxPep = RxPepFactory.createRxPep(mockedMyDataEnvironment, componentId,
        PepForEventNameTesting.class);
    rxPep.doRegisterAtPMP().blockingSubscribe((b) -> {
      // on next observable so only one object
      assertTrue(b);
      pepInstance = rxPep.createInstanceAPI();
    });
  }

  @Test
  void checkCreatedPepUsesActionIdFromAnnotation() throws Exception {
    assertTrue(registeredComponentInformation.getInterfaceDescriptions().stream()
        .anyMatch(pepInterfaceDescription -> pepInterfaceDescription.getEvent().getUrn()
            .equals("urn:action:test:test")));
    final AtomicReference<Event> eventReference = new AtomicReference<>(null);
    when(mockedPdp.decisionRequest(any(Event.class))).thenAnswer(invocation -> {
      eventReference.set(invocation.getArgument(0));
      return AuthorizationDecision.getDecisionAllow();
    });
    pepInstance.getMyNameFromAnnotation().blockingFirst(); // ensure that call to pdp happened
    assertNotNull(eventReference.get());
    assertEquals("urn:action:test:test", eventReference.get().getActionId().getUrn());
  }

  @Test
  void checkCreatedPepFallbackToMethodName() throws Exception {
    assertTrue(registeredComponentInformation.getInterfaceDescriptions().stream()
        .anyMatch(pepInterfaceDescription -> pepInterfaceDescription.getEvent().getUrn()
            .equals("urn:action:test:getMyNameFromMethodName")));
    final AtomicReference<Event> eventReference = new AtomicReference<>(null);
    when(mockedPdp.decisionRequest(any(Event.class))).thenAnswer(invocation -> {
      eventReference.set(invocation.getArgument(0));
      return AuthorizationDecision.getDecisionAllow();
    });
    pepInstance.getMyNameFromMethodName().blockingFirst(); // ensure that call to pdp happened
    assertNotNull(eventReference.get());
    assertEquals("urn:action:test:getMyNameFromMethodName",
        eventReference.get().getActionId().getUrn());
  }

}
