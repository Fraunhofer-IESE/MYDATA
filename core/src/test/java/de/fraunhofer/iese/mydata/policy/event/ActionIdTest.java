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

package de.fraunhofer.iese.mydata.policy.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;

import org.junit.jupiter.api.Test;

class ActionIdTest {

  @Test
  void whenActionIdUrnIsValidThenOk() throws Exception {
    final String urn = "urn:action:test-solution:test1234";
    final ActionId actionId = new ActionId(urn);

    MyDataEntity.validate(actionId);
  }

  @Test
  void whenActionIdUrnIsNotValidThenThrowException() {
    final String urn = "urn:pip:1234";
    final ActionId actionId = new ActionId(urn);
    assertThrows(InvalidEntityException.class, () ->

      MyDataEntity.validate(actionId));
  }

  @Test
  void whenEqualThenEqualsAndHashCodeOk() {
    final String urn1 = "urn:action:test-solution:test1234";
    final ActionId actionId1 = new ActionId(urn1);
    final String urn2 = "urn:action:test-solution:test1234";
    final ActionId actionId2 = new ActionId(urn2);

    assertEquals(actionId1, actionId2);
    assertEquals(actionId1.hashCode(), actionId2.hashCode());
  }

  @Test
  void whenNotEqualThenEqualsAndHashCodeFail() {
    final String urn1 = "urn:action:test-solution:test1234";
    final ActionId actionId1 = new ActionId(urn1);
    final String urn2 = "urn:action:test-solution:test4567";
    final ActionId actionId2 = new ActionId(urn2);

    assertNotEquals(actionId1, actionId2);
    assertNotEquals(actionId1.hashCode(), actionId2.hashCode());
  }

  @Test
  void whenUrnIsSetToNullThenThrowException() {
    final String urn = "urn:action:test-solution:test1234";
    final ActionId actionId = new ActionId(urn);
    assertThrows(IllegalArgumentException.class, () ->

      actionId.setUrn(null));
  }
}
