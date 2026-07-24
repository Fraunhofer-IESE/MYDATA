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

package de.fraunhofer.iese.mydata.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import de.fraunhofer.iese.mydata.component.ComponentType;
import de.fraunhofer.iese.mydata.component.information.method.InputParameterDescription;
import de.fraunhofer.iese.mydata.component.information.method.MethodInterfaceDescription;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.registry.pipservice.ServiceWithMultipleServiceMethodsDocumented;
import de.fraunhofer.iese.mydata.registry.pipservice.ServiceWithOneServiceMethod;
import de.fraunhofer.iese.mydata.registry.pipservice.ServiceWithOneServiceMethodDocumented;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;

/**
 * Tests for {@link InterfaceDescriptionDiscovery}.
 */
class InterfaceDescriptionDiscoveryTest {

  /**
   * The test candidate.
   */
  private final InterfaceDescriptionDiscovery testCandidate = new InterfaceDescriptionDiscovery();

  /**
   * The solution id.
   */
  private final SolutionId solutionId = new SolutionId("urn:solution:iot3-ppe");

  /**
   * Test one no argument method.
   * 
   * @throws InvalidEntityException
   */
  @Test
  void testOneNoArgumentMethod() throws InvalidEntityException {
    final Map<MethodInterfaceDescription, Method> returnValue = this.testCandidate
        .discover(ServiceWithOneServiceMethod.class, ComponentType.PIP, this.solutionId);
    assertFalse(returnValue.isEmpty());
    final MethodInterfaceDescription firstElement = returnValue.keySet().iterator().next();
    assertEquals("urn:info:iot3-ppe:doSome", firstElement.getMethodName());
    assertTrue(firstElement.getParameters().isEmpty());
  }

  /**
   * Test one no argument method documented.
   * 
   * @throws InvalidEntityException
   */
  @Test
  void testOneNoArgumentMethodDocumented() throws InvalidEntityException {
    final Map<MethodInterfaceDescription, Method> returnValue = this.testCandidate
        .discover(ServiceWithOneServiceMethodDocumented.class, ComponentType.PIP, this.solutionId);
    assertFalse(returnValue.isEmpty());
    final MethodInterfaceDescription firstElement = returnValue.keySet().iterator().next();
    assertEquals("some method", firstElement.getDescription());
  }

  /**
   * Argument with name should have its name.
   *
   * @throws ClassNotFoundException
   * @throws InvalidEntityException
   */
  @Test
  void argumentWithName_ShouldHaveItsName() throws ClassNotFoundException, InvalidEntityException {
    final Map<MethodInterfaceDescription, Method> returnValue = this.testCandidate.discover(
        ServiceWithMultipleServiceMethodsDocumented.class, ComponentType.PIP, this.solutionId);
    for (final MethodInterfaceDescription interfaceDescription : returnValue.keySet()) {
      if ("urn:info:iot3-ppe:withParamDocumentedName"
          .equals(interfaceDescription.getMethodName())) {
        assertEquals("documentedName",
            interfaceDescription.getParameters().iterator().next().getName());
        //        assertEquals(String.class, interfaceDescription.getParameters().iterator().next().getTypeDescription().getTypeClass()); //TODO we do  not have the typedescription anymore
        return;
      }
    }
    fail();
  }

  /**
   * Multiple arguments with description should have their names. w
   * 
   * @throws InvalidEntityException
   */
  @Test
  void multipleArgumentsWithDescription_ShouldHaveTheirNames() throws InvalidEntityException {
    final Map<MethodInterfaceDescription, Method> returnValue = this.testCandidate.discover(
        ServiceWithMultipleServiceMethodsDocumented.class, ComponentType.PIP, this.solutionId);
    for (final MethodInterfaceDescription interfaceDescription : returnValue.keySet()) {
      if ("urn:info:iot3-ppe:withParamMultipleParameters"
          .equals(interfaceDescription.getMethodName())) {
        final Iterator<InputParameterDescription> parameters = interfaceDescription.getParameters()
            .iterator();
        parameters.next();
        // want to access the second from the list
        assertEquals("some description", parameters.next().getDescription());
        return;
      }
    }
    fail();
  }
}
