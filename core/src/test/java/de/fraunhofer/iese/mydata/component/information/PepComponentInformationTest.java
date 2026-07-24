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

package de.fraunhofer.iese.mydata.component.information;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.information.method.PepInterfaceDescription;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.policy.event.ActionId;

import org.junit.jupiter.api.Test;

import java.util.Collections;

class PepComponentInformationTest {

  @Test
  void pepComponentWithValidMethodName() throws Exception {
    final PepComponentInformation componentInformation = new PepComponentInformation(
        new ComponentId("urn:component:test:pep:test"));
    componentInformation.setInterfaceDescriptions(Collections.singletonList(
        new PepInterfaceDescription(new ActionId("urn:action:test:test"), true, "desc")));
    MyDataEntity.validateAndNullCheck(componentInformation);
    // everything should be fine
  }

  @Test
  void pepComponentWithWrongComponentId() throws Exception {
    final PepComponentInformation componentInformation = new PepComponentInformation(
          new ComponentId("urn:component:test:pxp:test"));
    componentInformation.setInterfaceDescriptions(Collections.singletonList(
          new PepInterfaceDescription(new ActionId("urn:action:test:test"), true, "desc")));
    componentInformation.setInterfaceDescriptions(Collections.singletonList(
          new PepInterfaceDescription(new ActionId("urn:action:test:test"), true, "desc")));
    assertThrows(InvalidEntityException.class, () ->
      MyDataEntity.validateAndNullCheck(componentInformation));
  }

  @Test
  void pepComponentWithWrongSolutionMethod() throws Exception {
    final PepComponentInformation componentInformation = new PepComponentInformation(
          new ComponentId("urn:component:test:pep:test"));
    componentInformation
          .setInterfaceDescriptions(Collections.singletonList(new PepInterfaceDescription(
              new ActionId("urn:action:another-solution:test"), true, "desc")));
    assertThrows(InvalidEntityException.class, () ->
      MyDataEntity.validateAndNullCheck(componentInformation));
  }

  @Test
  void pepComponentWithInvalidMethodName() throws Exception {
    final PepComponentInformation componentInformation = new PepComponentInformation(
          new ComponentId("urn:component:test:pep:test"));
    componentInformation.setInterfaceDescriptions(Collections.singletonList(
          new PepInterfaceDescription(new ActionId("urn:mir-ist-das-egal"), true, "desc")));
    assertThrows(InvalidEntityException.class, () ->
      MyDataEntity.validateAndNullCheck(componentInformation));
  }

  @Test
  void whenSameComponentIdThenEqualsAndHashCodeOk() {
    final PepComponentInformation componentInformation1 = new PepComponentInformation(
        new ComponentId("urn:component:test:pep:test"));
    final PepComponentInformation componentInformation2 = new PepComponentInformation(
        new ComponentId("urn:component:test:pep:test"));

    assertEquals(componentInformation1, componentInformation2);
    assertEquals(componentInformation1.hashCode(), componentInformation2.hashCode());
  }

  @Test
  void whenDifferentComponentIdThenEqualsHashCodeFail() {
    final PepComponentInformation componentInformation1 = new PepComponentInformation(
        new ComponentId("urn:component:test:pep:test1"));
    final PepComponentInformation componentInformation2 = new PepComponentInformation(
        new ComponentId("urn:component:test:pep:test2"));

    assertNotEquals(componentInformation1, componentInformation2);
    assertNotSame(componentInformation1.hashCode(), componentInformation2.hashCode());
  }

}
