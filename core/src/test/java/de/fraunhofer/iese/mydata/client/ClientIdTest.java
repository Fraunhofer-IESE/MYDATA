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

package de.fraunhofer.iese.mydata.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;

import org.junit.jupiter.api.Test;

class ClientIdTest {

  @Test
  void whenIdValidThenOk() throws Exception {
    final ClientId id = new ClientId("urn:client:solution:test");

    MyDataEntity.validate(id);
  }

  @Test
  void whenIdValid2ThenOk() throws Exception {
    final ClientId id = new ClientId("urn:client:solution:test2");

    MyDataEntity.validate(id);
  }

  @Test
  void whenIdValid3ThenOk() throws Exception {
    final ClientId id = new ClientId("urn:client:solution:test_2");

    MyDataEntity.validate(id);
  }

  @Test
  void whenIdInvalidThenFail() {
    final ClientId id = new ClientId("urn:client:solution:test 2");
    assertThrows(InvalidEntityException.class, () ->

      MyDataEntity.validate(id));
  }

  @Test
  void whenEmptyIdValidThenFail() {
    final ClientId id = new ClientId();
    assertThrows(InvalidEntityException.class, () ->

      MyDataEntity.validate(id));
  }

  @Test
  void whenInvalidIdThenFail() {
    final ClientId id = new ClientId("urn:kleient:test");
    assertThrows(InvalidEntityException.class, () ->

      MyDataEntity.validate(id));
  }

  @Test
  void whenInvalidId2ThenFail() {
    final ClientId id = new ClientId("urn:client:solution:test:1");
    assertThrows(InvalidEntityException.class, () ->

      MyDataEntity.validate(id));
  }

  @Test
  void whenInvalidId3ThenFail() {
    final ClientId id = new ClientId("blabla");
    assertThrows(InvalidEntityException.class, () ->

      MyDataEntity.validate(id));
  }

  @Test
  void whenEqualThenEqualOk() throws Exception {
    final ClientId id1 = new ClientId("urn:client:solution:test");
    final ClientId id2 = new ClientId("urn:client:solution:test");

    MyDataEntity.validate(id1);
    MyDataEntity.validate(id2);
    assertEquals(id1, id2);
  }

  @Test
  void whenNotEqualThenEqualFail() throws Exception {
    final ClientId id1 = new ClientId("urn:client:solution:test1");
    final ClientId id2 = new ClientId("urn:client:solution:test2");

    MyDataEntity.validate(id1);
    MyDataEntity.validate(id2);
    assertNotEquals(id1, id2);
  }

}
