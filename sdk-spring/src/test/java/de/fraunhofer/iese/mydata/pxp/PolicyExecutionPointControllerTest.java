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

package de.fraunhofer.iese.mydata.pxp;

import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.health.HealthStatus;
import de.fraunhofer.iese.mydata.component.health.Status;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyExecutionPoint;
import de.fraunhofer.iese.mydata.internal.IMyDataEnvironmentFullFace;
import de.fraunhofer.iese.mydata.policy.decision.ExecuteAction;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;
import de.fraunhofer.iese.mydata.registry.RestExposeHelper;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Optional;

/**
 * Test for Controller.
 */
@RunWith(MockitoJUnitRunner.class)
public class PolicyExecutionPointControllerTest {

  @InjectMocks
  private PolicyExecutionPointController testCandidate;

  @Mock
  private RestExposeHelper restExposeHelper;

  @Mock
  private IMyDataEnvironmentFullFace myDataEnvironment;

  @Mock
  private IPolicyExecutionPoint pxp;

  /**
   * Happy path.
   *
   * @throws IOException
   */
  @Test
  public void happyPath() throws IOException {
    final ParameterList parameters = new ParameterList();
    Mockito.when(this.restExposeHelper.getPxpComponentIdForPath(ArgumentMatchers.any()))
        .thenReturn(Optional.of(new ComponentId("urn:component:mydata:pxp:bla")));
    Mockito.when(this.myDataEnvironment.getManagedPxp(ArgumentMatchers.any()))
        .thenReturn(Optional.of(this.pxp));
    Mockito.when(this.pxp.execute(ArgumentMatchers.any())).thenReturn(true);
    Assert.assertEquals(true,
        this.testCandidate
            .execute(new ExecuteAction(new ActionId("urn:action:a:b"), parameters).toJson(true), "")
            .getBody());
    Mockito.verify(this.pxp).execute(ArgumentMatchers.any());
  }

  /**
   * When no exception is raised then status S hould be OK.
   *
   * @throws IOException
   */
  @Test
  public void whenNoExceptionIsRaised_ThenStatusSHouldBeOK() throws IOException {
    final ParameterList parameters = new ParameterList();
    Mockito.when(this.restExposeHelper.getPxpComponentIdForPath(ArgumentMatchers.any()))
        .thenReturn(Optional.of(new ComponentId("urn:component:mydata:pxp:bla")));
    Mockito.when(this.myDataEnvironment.getManagedPxp(ArgumentMatchers.any()))
        .thenReturn(Optional.of(this.pxp));
    Mockito.when(this.pxp.execute(ArgumentMatchers.any())).thenReturn(true);
    Assert.assertEquals(HttpStatus.OK,
        this.testCandidate
            .execute(new ExecuteAction(new ActionId("urn:action:a:b"), parameters).toJson(true), "")
            .getStatusCode());
  }

  /**
   * When information undeterminable exception is raised then status S hould be unprocessable.
   */
  @Test
  public void whenInformationUndeterminableExceptionIsRaised_ThenStatusSHouldBeUnprocessable() {
    final ParameterList parameters = new ParameterList();
    Mockito.when(this.restExposeHelper.getPxpComponentIdForPath(ArgumentMatchers.any()))
        .thenReturn(Optional.of(new ComponentId("urn:component:mydata:pxp:bla")));
    Mockito.when(this.myDataEnvironment.getManagedPxp(ArgumentMatchers.any()))
        .thenReturn(Optional.empty());
    Assert.assertEquals(HttpStatus.UNPROCESSABLE_CONTENT,
        this.testCandidate
            .execute(new ExecuteAction(new ActionId("urn:action:a:b"), parameters).toJson(true), "")
            .getStatusCode());
  }

  /**
   * When other exception is raised then status S hould be unprocessable.
   *
   * @throws IOException
   */
  @Test
  public void whenOtherExceptionIsRaised_ThenStatusSHouldBeUnprocessable() throws IOException {
    final ParameterList parameters = new ParameterList();
    Mockito.when(this.restExposeHelper.getPxpComponentIdForPath(ArgumentMatchers.any()))
        .thenReturn(Optional.of(new ComponentId("urn:component:mydata:pxp:bla")));
    Mockito.when(this.myDataEnvironment.getManagedPxp(ArgumentMatchers.any()))
        .thenReturn(Optional.of(this.pxp));
    Mockito.when(this.pxp.execute(ArgumentMatchers.any()))
        .thenThrow(new IllegalArgumentException());
    Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
        this.testCandidate
            .execute(new ExecuteAction(new ActionId("urn:action:a:b"), parameters).toJson(true), "")
            .getStatusCode());
  }

  /**
   * Alive chec returns component componentId.
   */
  @Test
  public void aliveChecReturnsComponentId() {
    final ComponentId componentId = new ComponentId("urn:component:test:pxp:some");
    Mockito.when(this.restExposeHelper.getPxpComponentIdForPath("some"))
        .thenReturn(Optional.of(componentId));
    final ResponseEntity<ComponentId> answer = this.testCandidate.aliveCheck("some");
    Assert.assertEquals(HttpStatus.OK, answer.getStatusCode());
    Assert.assertEquals(componentId, answer.getBody());
  }

  /**
   * Name not found alive chec returns null.
   */
  @Test
  public void nameNotFound_aliveChecReturnsNull() {
    Mockito.when(this.restExposeHelper.getPxpComponentIdForPath("some"))
        .thenReturn(Optional.empty());
    final ResponseEntity<ComponentId> answer = this.testCandidate.aliveCheck("some");
    Assert.assertEquals(HttpStatus.NOT_FOUND, answer.getStatusCode());
    Assert.assertNull(answer.getBody());
  }

  @Test
  public void nameNotFound_health404() {
    Mockito.when(this.restExposeHelper.getPxpComponentIdForPath("some"))
        .thenReturn(Optional.empty());
    Assert.assertEquals(HttpStatus.NOT_FOUND, this.testCandidate.getHealth("some").getStatusCode());
  }

  @Test
  public void serviceUp_healthOk() throws IOException {
    final ComponentId componentId = Mockito.mock(ComponentId.class);
    Mockito.when(this.restExposeHelper.getPxpComponentIdForPath("some"))
        .thenReturn(Optional.of(componentId));
    Mockito.when(this.myDataEnvironment.getManagedPxp(ArgumentMatchers.any()))
        .thenReturn(Optional.of(this.pxp));
    Mockito.when(this.pxp.getHealth()).thenReturn(HealthStatus.of(Status.UP));
    final ResponseEntity<HealthStatus> controllerReturn = this.testCandidate.getHealth("some");
    Assert.assertEquals(HttpStatus.OK, controllerReturn.getStatusCode());
    final HealthStatus body = controllerReturn.getBody();
    Assert.assertNotNull(body);
    Assert.assertEquals(Status.UP, body.getStatus());
  }

  @Test
  public void serviceDown_healthOk() throws IOException {
    final ComponentId componentId = Mockito.mock(ComponentId.class);
    Mockito.when(this.restExposeHelper.getPxpComponentIdForPath("some"))
        .thenReturn(Optional.of(componentId));
    Mockito.when(this.myDataEnvironment.getManagedPxp(ArgumentMatchers.any()))
        .thenReturn(Optional.of(this.pxp));
    Mockito.when(this.pxp.getHealth()).thenReturn(HealthStatus.of(Status.DOWN));
    final ResponseEntity<HealthStatus> controllerReturn = this.testCandidate.getHealth("some");
    Assert.assertEquals(HttpStatus.OK, controllerReturn.getStatusCode());
    final HealthStatus body = controllerReturn.getBody();
    Assert.assertNotNull(body);
    Assert.assertEquals(Status.DOWN, body.getStatus());
  }

  @Test
  public void getHealthThrows_healthOk() throws IOException {
    final ComponentId componentId = Mockito.mock(ComponentId.class);
    Mockito.when(this.restExposeHelper.getPxpComponentIdForPath("some"))
        .thenReturn(Optional.of(componentId));
    Mockito.when(this.myDataEnvironment.getManagedPxp(ArgumentMatchers.any()))
        .thenReturn(Optional.of(this.pxp));
    Mockito.when(this.pxp.getHealth()).thenThrow(IOException.class);
    final ResponseEntity<HealthStatus> controllerReturn = this.testCandidate.getHealth("some");
    Assert.assertEquals(HttpStatus.OK, controllerReturn.getStatusCode());
    final HealthStatus body = controllerReturn.getBody();
    Assert.assertNotNull(body);
    Assert.assertEquals(Status.UNKNOWN, body.getStatus());
  }

  @Test
  public void serviceNotFound_health404() {
    final ComponentId componentId = Mockito.mock(ComponentId.class);
    Mockito.when(this.restExposeHelper.getPxpComponentIdForPath("some"))
        .thenReturn(Optional.of(componentId));
    Mockito.when(this.myDataEnvironment.getManagedPxp(ArgumentMatchers.any()))
        .thenReturn(Optional.empty());
    Assert.assertEquals(HttpStatus.NOT_FOUND, this.testCandidate.getHealth("some").getStatusCode());
  }

}
