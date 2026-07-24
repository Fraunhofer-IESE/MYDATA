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

class InfoIdTest {
  @Test
  void whenInfoIdUrnIsValidThenOk() throws Exception {
    final String urn = "urn:info:test-solution:test1234";
    final InfoId infoId = new InfoId(urn);

    MyDataEntity.validate(infoId);
  }

  @Test
  void whenInfoIdUrnIsNotValidThenThrowException() {
    final String urn = "urn:action:test-solution:test1234";
    final InfoId infoId = new InfoId(urn);
    assertThrows(InvalidEntityException.class, () ->

      MyDataEntity.validate(infoId));
  }

  @Test
  void whenEqualThenEqualsAndHashCodeOk() {
    final String urn1 = "urn:info:test-solution:test1234";
    final InfoId infoId1 = new InfoId(urn1);
    final String urn2 = "urn:info:test-solution:test1234";
    final InfoId infoId2 = new InfoId(urn2);

    assertEquals(infoId1, infoId2);
    assertEquals(infoId1.hashCode(), infoId2.hashCode());
  }

  @Test
  void whenNotEqualThenEqualsAndHashCodeFail() {
    final String urn1 = "urn:info:test-solution:test1234";
    final InfoId infoId1 = new InfoId(urn1);
    final String urn2 = "urn:info:test-solution:test4567";
    final InfoId infoId2 = new InfoId(urn2);

    assertNotEquals(infoId1, infoId2);
    assertNotEquals(infoId1.hashCode(), infoId2.hashCode());
  }
}
