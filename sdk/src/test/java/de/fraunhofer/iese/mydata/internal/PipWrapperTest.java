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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.health.HealthStatus;
import de.fraunhofer.iese.mydata.component.health.Status;
import de.fraunhofer.iese.mydata.component.information.method.MethodInterfaceDescription;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyInformationPoint;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.exception.ResourceUpdateException;
import de.fraunhofer.iese.mydata.policy.PipRequest;
import de.fraunhofer.iese.mydata.policy.event.InfoId;
import de.fraunhofer.iese.mydata.policy.exception.InformationUndeterminableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;
import de.fraunhofer.iese.mydata.registry.ActionDescription;
import de.fraunhofer.iese.mydata.registry.ActionParameterDescription;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class PipWrapperTest {

  @Test
  void test_regularUsage()
      throws InvalidEntityException, IOException, InformationUndeterminableException {
    final MyPip myPip = Mockito.mock(MyPip.class);
    final ComponentId componentId = new ComponentId("urn:component:mydata:pip:my-pip");
    final PipWrapper pipWrapper = new PipWrapper(componentId, myPip);
    final List<MethodInterfaceDescription> methodInterfaceDescriptionSet = pipWrapper
        .getMethodInterfaceDescriptions();
    assertEquals(1, methodInterfaceDescriptionSet.size());
    final MethodInterfaceDescription firstMethodInterfaceDescription = methodInterfaceDescriptionSet
        .iterator().next();
    assertEquals("urn:info:mydata:get", firstMethodInterfaceDescription.getMethodName());
    assertEquals(1, firstMethodInterfaceDescription.getParameters().size());
    assertEquals("param1",
        firstMethodInterfaceDescription.getParameters().iterator().next().getName());

    Mockito.when(myPip.get("test")).thenReturn("test");

    final IPolicyInformationPoint pip = pipWrapper;
    final PipRequest pipRequest1 = new PipRequest(new InfoId("urn:info:mydata:get"),
        new Parameter<>("param1", "test"));
    final DataObject<String> result = (DataObject<String>) pip.evaluate(pipRequest1);
    assertEquals("test", result.getValue());
    Mockito.verify(myPip).get("test");
  }

  @Test
  void testAlreadyRightTyped() throws Exception {
    final IPolicyInformationPoint myPip = Mockito.mock(IPolicyInformationPoint.class);
    final ComponentId componentId = new ComponentId("urn:component:mydata:pip:my-pip");
    final PipWrapper pipWrapper = new PipWrapper(componentId, myPip);
    final List<MethodInterfaceDescription> methodInterfaceDescriptionSet = pipWrapper
        .getMethodInterfaceDescriptions();
    assertEquals(0, methodInterfaceDescriptionSet.size());

    final IPolicyInformationPoint pip = pipWrapper;
    final PipRequest pipRequest1 = new PipRequest(new InfoId("urn:info:mydata:get"),
        new Parameter<>("param1", "test"));
    pip.evaluate(pipRequest1);
    Mockito.verify(myPip).evaluate(pipRequest1);
    pip.reset();
    Mockito.verify(myPip).reset();
    pip.getId();
    Mockito.verify(myPip).getId();
    pip.getHealth();
    Mockito.verify(myPip).getHealth();
  }

  @Test
  void test_unknownFunction()
      throws IOException, InvalidEntityException, InformationUndeterminableException {
    final MyPip myPip = Mockito.mock(MyPip.class);
    final ComponentId componentId = new ComponentId("urn:component:mydata:pip:my-pip");
    final PipWrapper pipWrapper = new PipWrapper(componentId, myPip);
    final IPolicyInformationPoint pip = pipWrapper;
    final PipRequest pipRequest1 = new PipRequest(new InfoId("urn:info:mydata:bla"),
        new Parameter<>("param1", "test"));
    assertThrows(InformationUndeterminableException.class, () -> {
      pip.evaluate(pipRequest1);
    });
  }

  @Test
  public void testDelegationOfComponentMethods()
      throws InvalidEntityException, IOException, ResourceUpdateException, NoSuchEntityException {
    final MyPip myPip = Mockito.mock(MyPip.class);
    final ComponentId componentId = new ComponentId("urn:component:mydata:pip:my-pip");
    final PipWrapper pipWrapper = new PipWrapper(componentId, myPip);
    final IPolicyInformationPoint pip = pipWrapper;
    Mockito.when(myPip.reset()).thenReturn(true);
    assertTrue(pip.reset());
    Mockito.verify(myPip).reset();
    Mockito.when(myPip.getHealth()).thenReturn(HealthStatus.of(Status.UP));
    assertEquals(HealthStatus.of(Status.UP), pip.getHealth());
    Mockito.verify(myPip).getHealth();
    Mockito.when(myPip.getId()).thenReturn(new ComponentId("urn:component:mydata:pip:my-pip"));
    assertEquals(new ComponentId("urn:component:mydata:pip:my-pip"), pip.getId());
    Mockito.verify(myPip).getId();
  }

  @Test
  void noGetHealthResultsInUnknownHealthStatus() throws InvalidEntityException, IOException {
    final PipWithNoHealth myPip = new PipWithNoHealth();
    final ComponentId componentId = new ComponentId("urn:component:mydata:pip:my-pip");
    final PipWrapper pipWrapper = new PipWrapper(componentId, myPip);
    final IPolicyInformationPoint pip = pipWrapper;
    assertEquals(HealthStatus.of(Status.UNKNOWN), pip.getHealth());
  }

  @Test
  void throwingGetHealthResultsInUnknownHealthStatus() throws InvalidEntityException, IOException {
    final MyPip myPip = Mockito.mock(MyPip.class);
    final ComponentId componentId = new ComponentId("urn:component:mydata:pip:my-pip");
    final PipWrapper pipWrapper = new PipWrapper(componentId, myPip);
    final IPolicyInformationPoint pip = pipWrapper;
    Mockito.when(myPip.getHealth()).thenThrow(new RuntimeException("intended for testing"));
    assertEquals(HealthStatus.of(Status.UNKNOWN), pip.getHealth());
    Mockito.verify(myPip).getHealth();
  }

  public static class MyPip {
    @ActionDescription
    public String get(@ActionParameterDescription(name = "param1") String name) {
      return name;
    }

    public boolean reset() {
      return true;
    }

    public HealthStatus getHealth() {
      return HealthStatus.of(Status.UP);
    }

    public ComponentId getId() {
      return new ComponentId("urn:component:mydata:pip:my-pip");
    }
  }

  public static class PipWithNoHealth {
    @ActionDescription
    public String get(@ActionParameterDescription(name = "param1") String name) {
      return name;
    }

    public boolean reset() {
      return true;
    }

    public ComponentId getId() {
      return new ComponentId("urn:component:mydata:pip:my-pip");
    }
  }

}
