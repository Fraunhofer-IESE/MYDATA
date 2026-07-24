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

package de.fraunhofer.iese.mydata.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import org.junit.jupiter.api.Test;

/**
 * The Class TestComponentId.
 */
class ComponentIdTest {

  /**
   * Test correct component type.
   *
   * @throws InvalidEntityException
   */
  @Test
  void correctComponentType() throws Exception {
    final ComponentId cid = new ComponentId("urn:component:mydata:pep:123");
    MyDataEntity.validate(cid);

    final SolutionId sid = new SolutionId("urn:solution:mydata");
    MyDataEntity.validate(sid);
    assertEquals(ComponentType.PEP, ComponentType.fromComponentId(cid));
    assertEquals(sid, SolutionId.fromComponentId(cid));
  }

  @Test
  void correctComponentId() throws Exception {
    final ComponentId cid = new ComponentId("urn:component:mydata:pep:123az");
    MyDataEntity.validate(cid);
  }

  @Test
  void incorrectComponentId() {
    final ComponentId cid = new ComponentId("urn:component:mydata:peP:123az");
    assertThrows(InvalidEntityException.class, () -> MyDataEntity.validate(cid));
  }

  @Test
  void incorrectComponentId2() {
    final ComponentId cid = new ComponentId("urn:component:mydata:psp:123az");
    assertThrows(InvalidEntityException.class, () -> MyDataEntity.validate(cid));
  }

  @Test
  void incorrectComponentId3() {
    final ComponentId cid = new ComponentId("urn:component:mydatA:pxp:123az");
    assertThrows(InvalidEntityException.class, () -> MyDataEntity.validate(cid));
  }

  @Test
  void inCorrectComponentId4() throws InvalidEntityException {
    final ComponentId cid = new ComponentId("urn:component:mydata:pep:123Az");
    assertThrows(InvalidEntityException.class, () -> MyDataEntity.validate(cid));
  }

  @Test
  void correctComponentId4() throws Exception {
    final ComponentId cid = new ComponentId("urn:component:mydata:pep:123az");
    MyDataEntity.validate(cid);
    assertEquals("mydata", SolutionId.fromComponentId(cid).getIdentifier());
    assertEquals(ComponentType.PEP, cid.getComponentType());
  }

  @Test
  void incorrectComponentRabbit() {
    final ComponentId cid = new ComponentId("urn:component:ind2uce:pdp:AMQP-online");
    assertThrows(InvalidEntityException.class, () -> MyDataEntity.validate(cid));
  }

  @Test
  void componentRabbit() throws Exception {
    final ComponentId cid = new ComponentId("urn:component:ind2uce:pdp:amqp-online");
    MyDataEntity.validate(cid);
    assertEquals("ind2uce", SolutionId.fromComponentId(cid).getIdentifier());
    assertEquals(ComponentType.PDP, cid.getComponentType());
  }

  @Test
  void whenSameUrnThenEqualsAndHashCodeOk() {
    final ComponentId componentId1 = new ComponentId("urn:component:mydata:pep:123az");
    final ComponentId componentId2 = new ComponentId("urn:component:mydata:pep:123az");

    assertEquals(componentId1, componentId2);
    assertEquals(componentId1.hashCode(), componentId2.hashCode());
  }

  @Test
  void whenDifferentUrnThenEqualsAndHashCodeFail() {
    final ComponentId componentId1 = new ComponentId("urn:component:mydata:pep:123az");
    final ComponentId componentId2 = new ComponentId("urn:component:mydata:pep:123xyz");

    assertNotEquals(componentId1, componentId2);
    assertNotSame(componentId1.hashCode(), componentId2.hashCode());
  }
}
