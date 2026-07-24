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
import static org.junit.jupiter.api.Assertions.fail;

import de.fraunhofer.iese.mydata.DataFactory;
import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.pep.PolicyEnforcementPoint;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.InhibitException;
import de.fraunhofer.iese.mydata.reactive.ReactivePep;
import de.fraunhofer.iese.mydata.reactive.common.RxPep;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.io.IOException;

class RxPdpNotReadyTest {

  private RxPep<MyRxPepDocumentationAPI> rxPep;

  private MyRxPepDocumentationAPI api;

  private PolicyEnforcementPoint mockPep;

  // keep declaration for compatibility (not used now)
  // private IMyDataEnvironmentFullFace mockedMyDataEnvironment;

  @BeforeEach
  void setUp() throws Exception {
    this.mockPep = Mockito.mock(PolicyEnforcementPoint.class);

    Mockito.when(this.mockPep.getId())
        .thenReturn(new ComponentId("urn:component:test:pep:testpep"));

    // Instantiate ReactivePep directly with the mocked PolicyEnforcementPoint.
    this.rxPep = new ReactivePep<>(MyRxPepDocumentationAPI.class, this.mockPep);

    Mockito.when(this.mockPep.initialize()).thenReturn(true);
    this.rxPep.doRegisterAtPMP().blockingFirst();

    // Now creating API instance will succeed and the internal policy enforcement point is our mock
    this.api = this.rxPep.createInstanceAPI();
  }

  @AfterEach
  void tearDown() {
  }

  @Test
  void testOtherExceptionErrorCode() throws Exception {
    Mockito.doThrow(new IOException("Service unavailable. Demo.")).when(this.mockPep)
        .enforce(ArgumentMatchers.any(Event.class));

    this.api.enforceForCSProjectShow(DataFactory.getUser()).blockingSubscribe(
        t -> fail("onNext should not be executed!"), e -> assertEquals(e.getMessage(),
            (new IOException("Service unavailable. Demo.")).getMessage()));

    Mockito.verify(this.mockPep).enforce(ArgumentMatchers.any(Event.class));
  }

  @Test
  void testOtherExceptionType() throws Exception {

    Mockito.doThrow(new IOException("Service unavailable. Demo.")).when(this.mockPep)
        .enforce(ArgumentMatchers.any(Event.class));

    this.api.enforceForCSProjectShow(DataFactory.getUser()).blockingSubscribe(
        t -> fail("onNext should not be executed!"), e -> assertEquals(e.getMessage(),
            (new IOException("Service unavailable. Demo.")).getMessage()));

    Mockito.verify(this.mockPep).enforce(ArgumentMatchers.any(Event.class));
  }

  @Test
  void inhibitExceptionType() throws Exception {
    Mockito.doThrow(new InhibitException("Event is inhibited!")).when(this.mockPep)
        .enforce(ArgumentMatchers.any(Event.class));

    this.api.enforceForCSProjectShow(DataFactory.getUser()).blockingSubscribe(
        t -> fail("onNext should not be executed!"),
        e -> assertEquals("Event is inhibited!", e.getMessage()));

    Mockito.verify(this.mockPep).enforce(ArgumentMatchers.any(Event.class));

  }

}
