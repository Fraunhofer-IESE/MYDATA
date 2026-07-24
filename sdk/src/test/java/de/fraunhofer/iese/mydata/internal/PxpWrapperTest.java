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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.information.method.MethodInterfaceDescription;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyExecutionPoint;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.policy.decision.ExecuteAction;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;
import de.fraunhofer.iese.mydata.registry.ActionDescription;
import de.fraunhofer.iese.mydata.registry.ActionParameterDescription;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class PxpWrapperTest {

  @Test
  void test_regularUsage() throws InvalidEntityException, IOException {
    final MyPxp myPxp = Mockito.mock(MyPxp.class);
    final ComponentId componentId = new ComponentId("urn:component:mydata:pxp:my-pxp");
    final PxpWrapper pxpWrapper = new PxpWrapper(componentId, myPxp);
    final List<MethodInterfaceDescription> methodInterfaceDescriptionSet = pxpWrapper
        .getMethodInterfaceDescriptions();
    assertEquals(1, methodInterfaceDescriptionSet.size());
    final MethodInterfaceDescription firstMethodInterfaceDescription = methodInterfaceDescriptionSet
        .iterator().next();
    assertEquals("urn:action:mydata:get", firstMethodInterfaceDescription.getMethodName());
    assertEquals(1, firstMethodInterfaceDescription.getParameters().size());
    assertEquals("param1",
        firstMethodInterfaceDescription.getParameters().iterator().next().getName());

    Mockito.when(myPxp.get("test")).thenReturn(true);

    final IPolicyExecutionPoint pxp = pxpWrapper;
    final ExecuteAction executeAction = new ExecuteAction(new ActionId("urn:action:mydata:get"),
        new ParameterList(new Parameter<>("param1", "test")));
    final boolean result = pxp.execute(executeAction);
    assertTrue(result);
    Mockito.verify(myPxp).get("test");
  }

  @Test
  void testAlreadyRightTyped() throws Exception {
    final IPolicyExecutionPoint myPxp = Mockito.mock(IPolicyExecutionPoint.class);
    final ComponentId componentId = new ComponentId("urn:component:mydata:pxp:my-pxp");
    final PxpWrapper pxpWrapper = new PxpWrapper(componentId, myPxp);
    final List<MethodInterfaceDescription> methodInterfaceDescriptionSet = pxpWrapper
        .getMethodInterfaceDescriptions();
    assertEquals(0, methodInterfaceDescriptionSet.size());

    final IPolicyExecutionPoint pxp = pxpWrapper;
    final ExecuteAction executeAction = new ExecuteAction(new ActionId("urn:action:mydata:get"),
        new ParameterList(new Parameter<>("param1", "test")));
    pxp.execute(executeAction);
    Mockito.verify(myPxp).execute(executeAction);
  }

  @Test
  void test_unknownFunction() throws IOException, InvalidEntityException {
    final MyPxp myPxp = Mockito.mock(MyPxp.class);
    final ComponentId componentId = new ComponentId("urn:component:mydata:pxp:my-pxp");
    final PxpWrapper pxpWrapper = new PxpWrapper(componentId, myPxp);
    final IPolicyExecutionPoint pxp = pxpWrapper;
    final ExecuteAction executeAction = new ExecuteAction(new ActionId("urn:action:mydata:bla"),
        new ParameterList(new Parameter<>("param1", "test")));
    final boolean result = pxp.execute(executeAction);
    assertFalse(result);
  }

  @Test
  void testReset() throws InvalidEntityException, IOException, NoSuchEntityException {
    final MyPxp myPxp = Mockito.mock(MyPxp.class);
    final ComponentId componentId = new ComponentId("urn:component:mydata:pxp:my-pxp");
    final PxpWrapper pxpWrapper = new PxpWrapper(componentId, myPxp);
    final IPolicyExecutionPoint pxp = pxpWrapper;
    Mockito.when(myPxp.reset()).thenReturn(true);
    assertTrue(pxp.reset());
    Mockito.verify(myPxp).reset();
  }

  public static class MyPxp {
    @ActionDescription
    public boolean get(@ActionParameterDescription(name = "param1") String name) {
      return true;
    }

    public boolean reset() {
      return true;
    }
  }

}
