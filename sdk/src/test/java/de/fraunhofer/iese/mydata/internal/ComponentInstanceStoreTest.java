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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyExecutionPoint;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyInformationPoint;
import de.fraunhofer.iese.mydata.exception.ConflictingResourceException;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for {@link ComponentInstanceStore}
 */
@ExtendWith(MockitoExtension.class)
public class ComponentInstanceStoreTest {
  private static final Logger LOG = LoggerFactory.getLogger(ComponentInstanceStoreTest.class);

  private final IComponentInstanceStore testCandidate = new ComponentInstanceStore();
  //  @Parameterized.Parameter(0)
  //  public Class serviceClass;
  //  @Parameterized.Parameter(1)
  //  public String methodName;
  //  @Parameterized.Parameter(2)
  //  public Class[] parameterTypes;
  //  @Parameterized.Parameter(3)
  //  public String[] parameterNames;
  //  @Parameterized.Parameter(4)
  //  public boolean[] mandatoryFlags;
  //  @Parameterized.Parameter(5)
  //  public Object[] arguments;
  //  @Parameterized.Parameter(6)
  //  public Object result;
  //
  //  @Parameterized.Parameters(name = "{1}")
  //  public static Collection<Object[]> data() {
  //    return Arrays.asList(
  //        new Object[][]{
  //            {
  //                ServiceWithOneEmptyArgumentMethod.class,
  //                "urn:info:test:noArgumentMethod",
  //                new Class[0],
  //                new String[0],
  //                new boolean[0],
  //                new Object[0],
  //                "hello"
  //            },
  //            {
  //                ServiceWithOneArgumentMethod.class,
  //                "urn:info:test:argumentMethod",
  //                new Class[]{String.class},
  //                new String[]{"argument"},
  //                new boolean[1],
  //                new Object[]{"hello"},
  //                "hello"},
  //            {
  //                ServiceWithMultipleArgumentMethod.class,
  //                "urn:info:test:multiArgumentMethod",
  //                new Class[]{String.class, int.class},
  //                new String[]{"argument", "argument2"},
  //                new boolean[2],
  //                new Object[]{"hello", 2},
  //                "hellohello"
  //            },
  //            {
  //                ServiceWithMultipleArgumentMethod.class,
  //                "urn:info:test:multiArgumentMethod",
  //                new Class[]{String.class, int.class},
  //                new String[]{"argument", "argument2"},
  //                new boolean[2],
  //                new Object[]{"hello"},
  //                ""
  //            },
  //            {
  //                ServiceWithMandatoryArguments.class,
  //                "urn:info:test:nonOptional",
  //                new Class[]{String.class, int.class},
  //                new String[]{"argument", "argument2"},
  //                new boolean[]{true, true},
  //                new Object[]{"hello"},
  //                InformationUndeterminableException.class
  //            }
  //        });
  //  }
  //
  //  @Test
  //  public void testDispatching() throws InformationUndeterminableException, IllegalAccessException, InstantiationException, ConflictingResourceException, InvalidEntityException, IOException {
  //    final ParameterList params = new ParameterList();
  //    final MethodInterfaceDescription interfaceDescription = new MethodInterfaceDescription(this.methodName, String.class, "some");
  //    for (int i = 0; i < this.parameterTypes.length; i++) {
  //      final Object argument = this.getArgument(this.arguments, i);
  //      if (argument != null) {
  //        params.add(new Parameter<>(this.parameterNames[i], argument, this.parameterTypes[i]));
  //      }
  //      interfaceDescription.addParameter(new InputParameterDescription(this.parameterNames[i], "", this.mandatoryFlags[i], this.parameterTypes[i]));
  //    }
  //
  //    final ComponentId componentId = new ComponentId("urn:component:test:pip:test123");
  //    this.testCandidate.addPipInstance(componentId, new PipWrapper(componentId, this.serviceClass.newInstance()));
  //    try {
  //      Optional<IPolicyInformationPoint> pipOp = this.testCandidate.getPipInstanceByComponentId(componentId);
  //      Assert.assertTrue(pipOp.isPresent());
  //      Assert.assertEquals(this.result, pipOp.get().evaluate(new PipRequest(this.methodName, "DEFAULT", params)).getValue());
  //    } catch (InformationUndeterminableException e) {
  //      if (!InformationUndeterminableException.class.equals(this.result)) {
  //        throw e;
  //      }
  //    }
  //  }

  @Test
  void testPipCanBeAddedAndRetrieved() throws ConflictingResourceException, InvalidEntityException {
    final IPolicyInformationPoint pipMock = Mockito.mock(IPolicyInformationPoint.class);
    final ComponentId componentId = new ComponentId("urn:component:mydata:pip:my-pip");
    this.testCandidate.addPipInstance(componentId, pipMock);
    assertEquals(pipMock, this.testCandidate.getPipInstanceByComponentId(componentId).orElse(null));
  }

  @Test
  void testPipDuplicateResultsInException()
      throws ConflictingResourceException, InvalidEntityException {
    final IPolicyInformationPoint pipMock = Mockito.mock(IPolicyInformationPoint.class);
    final ComponentId componentId = new ComponentId("urn:component:mydata:pip:my-pip");
    this.testCandidate.addPipInstance(componentId, pipMock);
    assertThrows(ConflictingResourceException.class, () -> {
      this.testCandidate.addPipInstance(componentId, pipMock);
    });
  }

  @Test
  void testNonExistingPipResultsInEmptyOptional() {
    assertFalse(this.testCandidate
        .getPipInstanceByComponentId(new ComponentId("urn:component:mydata:pip:another"))
        .isPresent());
  }

  @Test
  void testPxpCanBeAddedAndRetrieved() throws ConflictingResourceException, InvalidEntityException {
    final IPolicyExecutionPoint pxpMock = Mockito.mock(IPolicyExecutionPoint.class);
    final ComponentId componentId = new ComponentId("urn:component:mydata:pxp:my-pxp");
    this.testCandidate.addPxpInstance(componentId, pxpMock);
    assertEquals(pxpMock, this.testCandidate.getPxpInstanceByComponentId(componentId).orElse(null));
  }

  @Test
  void testPxpDuplicateResultsInException()
      throws ConflictingResourceException, InvalidEntityException {
    final IPolicyExecutionPoint pxpMock = Mockito.mock(IPolicyExecutionPoint.class);
    final ComponentId componentId = new ComponentId("urn:component:mydata:pxp:my-pxp");
    this.testCandidate.addPxpInstance(componentId, pxpMock);
    assertThrows(ConflictingResourceException.class, () -> {
      this.testCandidate.addPxpInstance(componentId, pxpMock);
    });
  }

  @Test
  void testNonExistingPxpResultsInEmptyOptional() {
    assertFalse(this.testCandidate
        .getPxpInstanceByComponentId(new ComponentId("urn:component:mydata:pxp:another"))
        .isPresent());
  }

  @Test
  void testAddedPipCanBeRemoved() throws ConflictingResourceException, InvalidEntityException {
    final IPolicyInformationPoint pipMock = Mockito.mock(IPolicyInformationPoint.class);
    final ComponentId componentId = new ComponentId("urn:component:mydata:pip:my-pip");
    this.testCandidate.addPipInstance(componentId, pipMock);
    this.testCandidate.removePipInstance(componentId);
    assertNull(this.testCandidate.getPipInstanceByComponentId(componentId).orElse(null));
  }

  @Test
  void testAddedPxpCanBeRemoved() throws ConflictingResourceException, InvalidEntityException {
    final IPolicyExecutionPoint pxpMock = Mockito.mock(IPolicyExecutionPoint.class);
    final ComponentId componentId = new ComponentId("urn:component:mydata:pxp:my-pxp");
    this.testCandidate.addPxpInstance(componentId, pxpMock);
    this.testCandidate.removePxpInstance(componentId);
    assertNull(this.testCandidate.getPxpInstanceByComponentId(componentId).orElse(null));
  }

  @Test
  void testClear() throws ConflictingResourceException, InvalidEntityException {
    final IPolicyInformationPoint pipMock = Mockito.mock(IPolicyInformationPoint.class);
    final ComponentId componentId1 = new ComponentId("urn:component:mydata:pip:my-pip");
    this.testCandidate.addPipInstance(componentId1, pipMock);
    final IPolicyExecutionPoint pxpMock = Mockito.mock(IPolicyExecutionPoint.class);
    final ComponentId componentId2 = new ComponentId("urn:component:mydata:pxp:my-pxp");
    this.testCandidate.addPxpInstance(componentId2, pxpMock);
    this.testCandidate.clear();
    assertNull(this.testCandidate.getPipInstanceByComponentId(componentId1).orElse(null));
    assertNull(this.testCandidate.getPxpInstanceByComponentId(componentId2).orElse(null));
  }

  //  private Object getArgument(final Object[] arguments, final int i) {
  //    return arguments.length > i ? arguments[i] : null;
  //  }
  //
  //  public static class ServiceWithOneEmptyArgumentMethod {
  //
  //    @ActionDescription
  //    public String noArgumentMethod() {
  //      return "hello";
  //    }
  //  }
  //
  //  public static class ServiceWithOneArgumentMethod {
  //
  //    @ActionDescription
  //    public String argumentMethod(
  //        @ActionParameterDescription(name = "argument") String argument) {
  //      return argument;
  //    }
  //
  //  }
  //
  //  public static class ServiceWithMultipleArgumentMethod {
  //
  //    @ActionDescription
  //    public String multiArgumentMethod(
  //        @ActionParameterDescription(name = "argument") String argument,
  //        @ActionParameterDescription(name = "argument2") int argument2) {
  //      final StringBuilder toReturn = new StringBuilder();
  //      for (int i = 0; i < argument2; i++) {
  //        toReturn.append(argument);
  //      }
  //      return toReturn.toString();
  //    }
  //  }
  //
  //  public static class ServiceWithMandatoryArguments {
  //
  //    @ActionDescription
  //    public String nonOptional(
  //        @ActionParameterDescription(name = "argument", mandatory = true) String argument,
  //        @ActionParameterDescription(name = "argument2", mandatory = true) int argument2) {
  //      final StringBuilder toReturn = new StringBuilder();
  //      for (int i = 0; i < argument2; i++) {
  //        toReturn.append(argument);
  //      }
  //      return toReturn.toString();
  //    }
  //  }

}
