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

package de.fraunhofer.iese.mydata.pip;

import static org.springframework.http.HttpStatus.OK;

import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.health.HealthStatus;
import de.fraunhofer.iese.mydata.component.health.Status;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyInformationPoint;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.internal.IMyDataEnvironmentFullFace;
import de.fraunhofer.iese.mydata.policy.PipRequest;
import de.fraunhofer.iese.mydata.policy.event.InfoId;
import de.fraunhofer.iese.mydata.policy.exception.InformationUndeterminableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;
import de.fraunhofer.iese.mydata.registry.RestExposeHelper;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
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
public class PolicyInformationPointControllerTest {

  /**
   * The Constant DUMMY.
   */
  public static final String DUMMY = "dummy";

  /**
   * The Constant DEFAULT_VALUE.
   */
  public static final String DEFAULT_VALUE = "DEFAULT_VALUE";

  @Mock
  IPolicyInformationPoint pip;

  @InjectMocks
  private PolicyInformationPointController testCandidate;

  @Mock
  private RestExposeHelper restExposeHelper;

  @Mock
  private IMyDataEnvironmentFullFace myDataEnvironment;

  @Mock
  private HttpServletResponse response;

  @Mock
  private ServletOutputStream outputStream;

  /**
   * Happy path.
   *
   * @throws InformationUndeterminableException
   * @throws IOException
   * @throws InvalidEntityException
   */
  @Test
  public void happyPath() throws InformationUndeterminableException, IOException {
    final ParameterList parameters = new ParameterList();
    Mockito.when(this.restExposeHelper.getPipComponentIdForPath(ArgumentMatchers.any()))
        .thenReturn(Optional.of(new ComponentId("urn:component:mydata:pip:bla")));
    Mockito.when(this.myDataEnvironment.getManagedPip(ArgumentMatchers.any()))
        .thenReturn(Optional.of(this.pip));
    Mockito.when(this.pip.evaluate(ArgumentMatchers.any())).thenReturn(new DataObject(DUMMY));
    Mockito.when(this.response.getOutputStream()).thenReturn(this.outputStream);
    Assert.assertNull(this.testCandidate.execute(
        new PipRequest(new InfoId("urn:info:a:b"), parameters).toJson(true), "", this.response));
    Mockito.verify(this.outputStream).write(new DataObject<>(DUMMY).toJson(false).getBytes());
  }

  /**
   * When no exception is raised then status S hould be OK.
   *
   * @throws InformationUndeterminableException the information undeterminable exception
   * @throws IOException
   * @throws InvalidEntityException
   */
  @Test
  public void whenNoExceptionIsRaised_ThenStatusSHouldBeOK()
      throws InformationUndeterminableException, IOException {
    final ParameterList parameters = new ParameterList();
    Mockito.when(this.restExposeHelper.getPipComponentIdForPath(ArgumentMatchers.any()))
        .thenReturn(Optional.of(new ComponentId("urn:component:mydata:pip:bla")));
    Mockito.when(this.myDataEnvironment.getManagedPip(ArgumentMatchers.any()))
        .thenReturn(Optional.of(this.pip));
    Mockito.when(this.pip.evaluate(ArgumentMatchers.any())).thenReturn(new DataObject(DUMMY));
    Mockito.when(this.response.getOutputStream()).thenReturn(this.outputStream);
    this.testCandidate.execute(new PipRequest(new InfoId("urn:info:a:b"), parameters).toJson(true),
        "", this.response);
    Mockito.verify(this.response).setStatus(OK.value());
  }

  /**
   * When information undeterminable exception is raised then status S hould be unprocessable.
   *
   * @throws InformationUndeterminableException the information undeterminable exception
   * @throws IOException
   * @throws InvalidEntityException
   */
  @Test
  public void whenInformationUndeterminableExceptionIsRaised_ThenStatusSHouldBeUnprocessable()
      throws InformationUndeterminableException, IOException {
    final ParameterList parameters = new ParameterList();
    Mockito.when(this.restExposeHelper.getPipComponentIdForPath(ArgumentMatchers.any()))
        .thenReturn(Optional.of(new ComponentId("urn:component:mydata:pip:bla")));
    Mockito.when(this.myDataEnvironment.getManagedPip(ArgumentMatchers.any()))
        .thenReturn(Optional.of(this.pip));
    Mockito.when(this.pip.evaluate(ArgumentMatchers.any()))
        .thenThrow(new InformationUndeterminableException());
    Assert.assertEquals(HttpStatus.UNPROCESSABLE_CONTENT,
        ((ResponseEntity) this.testCandidate.execute(
            new PipRequest(new InfoId("urn:info:a:b"), parameters).toJson(true), "", this.response))
                .getStatusCode());
  }

  /**
   * When other exception is raised then status S hould be unprocessable.
   *
   * @throws InformationUndeterminableException the information undeterminable exception
   * @throws IOException
   * @throws InvalidEntityException
   */
  @Test
  public void whenOtherExceptionIsRaised_ThenStatusSHouldBeUnprocessable()
      throws InformationUndeterminableException, IOException {
    final ParameterList parameters = new ParameterList();
    Mockito.when(this.restExposeHelper.getPipComponentIdForPath(ArgumentMatchers.any()))
        .thenReturn(Optional.of(new ComponentId("urn:component:mydata:pip:bla")));
    Mockito.when(this.myDataEnvironment.getManagedPip(ArgumentMatchers.any()))
        .thenReturn(Optional.of(this.pip));
    Mockito.when(this.pip.evaluate(ArgumentMatchers.any()))
        .thenThrow(new IllegalArgumentException());
    Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
        ((ResponseEntity) this.testCandidate.execute(
            new PipRequest(new InfoId("urn:info:a:b"), parameters).toJson(true), "", this.response))
                .getStatusCode());
  }

  /**
   * When information undeterminable exception is raised then message should be in body.
   *
   * @throws InformationUndeterminableException the information undeterminable exception
   * @throws IOException
   * @throws InvalidEntityException
   */
  @Test
  public void whenInformationUndeterminableExceptionIsRaised_ThenMessageShouldBeInBody()
      throws InformationUndeterminableException, IOException {
    final ParameterList parameters = new ParameterList();
    final String message = "SOME ERROR";
    Mockito.when(this.restExposeHelper.getPipComponentIdForPath(ArgumentMatchers.any()))
        .thenReturn(Optional.of(new ComponentId("urn:component:mydata:pip:bla")));
    Mockito.when(this.myDataEnvironment.getManagedPip(ArgumentMatchers.any()))
        .thenReturn(Optional.of(this.pip));
    Mockito.when(this.pip.evaluate(ArgumentMatchers.any()))
        .thenThrow(new InformationUndeterminableException(message));
    Assert.assertEquals(message,
        ((ResponseEntity) this.testCandidate.execute(
            new PipRequest(new InfoId("urn:info:a:b"), parameters).toJson(true), "", this.response))
                .getBody());
  }

  /**
   * Alive check returns component component_id.
   */
  @Test
  public void aliveChecReturnsComponentId() {
    final ComponentId componentId = new ComponentId("urn:component:test:pip:some");
    Mockito.when(this.restExposeHelper.getPipComponentIdForPath("some"))
        .thenReturn(Optional.of(componentId));
    final ResponseEntity<ComponentId> answer = this.testCandidate.aliveCheck("some");
    Assert.assertEquals(HttpStatus.OK, answer.getStatusCode());
    Assert.assertEquals(componentId, answer.getBody());
  }

  /**
   * Name not found alive check returns null.
   */
  @Test
  public void nameNotFound_aliveChecReturnsNull() {
    Mockito.when(this.restExposeHelper.getPipComponentIdForPath("some"))
        .thenReturn(Optional.empty());
    final ResponseEntity<ComponentId> answer = this.testCandidate.aliveCheck("some");
    Assert.assertEquals(HttpStatus.NOT_FOUND, answer.getStatusCode());
    Assert.assertNull(answer.getBody());
  }

  @Test
  public void whenNotFound_ThenStatusShouldBeUnprocessable() {
    final ParameterList parameters = new ParameterList();
    Mockito.when(this.restExposeHelper.getPipComponentIdForPath(ArgumentMatchers.any()))
        .thenReturn(Optional.of(new ComponentId("urn:component:mydata:pip:bla")));
    Mockito.when(this.myDataEnvironment.getManagedPip(ArgumentMatchers.any()))
        .thenReturn(Optional.empty());
    Assert.assertEquals(HttpStatus.UNPROCESSABLE_CONTENT,
        ((ResponseEntity) this.testCandidate.execute(
            new PipRequest(new InfoId("urn:info:a:b"), parameters).toJson(true), "", this.response))
                .getStatusCode());
  }

  @Test
  public void nameNotFound_health404() {
    Mockito.when(this.restExposeHelper.getPipComponentIdForPath("some"))
        .thenReturn(Optional.empty());
    Assert.assertEquals(HttpStatus.NOT_FOUND, this.testCandidate.getHealth("some").getStatusCode());
  }

  @Test
  public void serviceUp_healthOk() throws IOException {
    final ComponentId componentId = Mockito.mock(ComponentId.class);
    Mockito.when(this.restExposeHelper.getPipComponentIdForPath("some"))
        .thenReturn(Optional.of(componentId));
    Mockito.when(this.myDataEnvironment.getManagedPip(ArgumentMatchers.any()))
        .thenReturn(Optional.of(this.pip));
    Mockito.when(this.pip.getHealth()).thenReturn(HealthStatus.of(Status.UP));
    final ResponseEntity<HealthStatus> controllerReturn = this.testCandidate.getHealth("some");
    Assert.assertEquals(HttpStatus.OK, controllerReturn.getStatusCode());
    final HealthStatus body = controllerReturn.getBody();
    Assert.assertNotNull(body);
    Assert.assertEquals(Status.UP, body.getStatus());
  }

  @Test
  public void getHealthThrows_healthOk() throws IOException {
    final ComponentId componentId = Mockito.mock(ComponentId.class);
    Mockito.when(this.restExposeHelper.getPipComponentIdForPath("some"))
        .thenReturn(Optional.of(componentId));
    Mockito.when(this.myDataEnvironment.getManagedPip(ArgumentMatchers.any()))
        .thenReturn(Optional.of(this.pip));
    Mockito.when(this.pip.getHealth()).thenThrow(IOException.class);
    final ResponseEntity<HealthStatus> controllerReturn = this.testCandidate.getHealth("some");
    Assert.assertEquals(HttpStatus.OK, controllerReturn.getStatusCode());
    final HealthStatus body = controllerReturn.getBody();
    Assert.assertNotNull(body);
    Assert.assertEquals(Status.UNKNOWN, body.getStatus());
  }

  @Test
  public void serviceDown_healthOk() throws IOException {
    final ComponentId componentId = Mockito.mock(ComponentId.class);
    Mockito.when(this.restExposeHelper.getPipComponentIdForPath("some"))
        .thenReturn(Optional.of(componentId));
    Mockito.when(this.myDataEnvironment.getManagedPip(ArgumentMatchers.any()))
        .thenReturn(Optional.of(this.pip));
    Mockito.when(this.pip.getHealth()).thenReturn(HealthStatus.of(Status.DOWN));
    final ResponseEntity<HealthStatus> controllerReturn = this.testCandidate.getHealth("some");
    Assert.assertEquals(HttpStatus.OK, controllerReturn.getStatusCode());
    final HealthStatus body = controllerReturn.getBody();
    Assert.assertNotNull(body);
    Assert.assertEquals(Status.DOWN, body.getStatus());
  }

  @Test
  public void serviceNotFound_health404() {
    final ComponentId componentId = Mockito.mock(ComponentId.class);
    Mockito.when(this.restExposeHelper.getPipComponentIdForPath("some"))
        .thenReturn(Optional.of(componentId));
    Mockito.when(this.myDataEnvironment.getManagedPip(ArgumentMatchers.any()))
        .thenReturn(Optional.empty());
    Assert.assertEquals(HttpStatus.NOT_FOUND, this.testCandidate.getHealth("some").getStatusCode());
  }

}
