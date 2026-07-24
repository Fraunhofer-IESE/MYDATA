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

package de.fraunhofer.iese.mydata.affiliation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;

import org.junit.jupiter.api.Test;

class AffiliationIdTest {

  @Test
  void whenIdValidThenOk() throws Exception {
    final AffiliationId id = new AffiliationId("urn:affiliation:test");

    MyDataEntity.validate(id);
  }

  @Test
  void whenIdValid2ThenOk() throws Exception {
    final AffiliationId id = new AffiliationId("urn:affiliation:test2");

    MyDataEntity.validate(id);
  }

  @Test
  void whenIdValid3ThenOk() throws Exception {
    final AffiliationId id = new AffiliationId("test3");
    MyDataEntity.validate(id);
    assertEquals("urn:affiliation:test3", id.getUrn());
  }

  @Test
  void whenEmptyIdValidThenFail() {
    final AffiliationId id = new AffiliationId();
    assertThrows(InvalidEntityException.class, () ->

      MyDataEntity.validate(id));
  }

  @Test
  void whenInvalidIdThenFail() {
    final AffiliationId id = new AffiliationId("urn:affiliataion:test");
    assertThrows(InvalidEntityException.class, () ->

      MyDataEntity.validate(id));
  }

  @Test
  void whenInvalidId2ThenFail() {
    final AffiliationId id = new AffiliationId("urn:affiliation:test:1");
    assertThrows(InvalidEntityException.class, () ->

      MyDataEntity.validate(id));
  }

  @Test
  void whenInvalidId3ThenFail() {
    final AffiliationId id = new AffiliationId("blabla:");
    assertThrows(InvalidEntityException.class, () ->

      MyDataEntity.validate(id));
  }

  @Test
  void whenEqualThenEqualOk() throws Exception {
    final AffiliationId id1 = new AffiliationId("urn:affiliation:test");
    final AffiliationId id2 = new AffiliationId("urn:affiliation:test");

    MyDataEntity.validate(id1);
    MyDataEntity.validate(id2);
    assertEquals(id1, id2);
    assertEquals(id1.hashCode(), id2.hashCode());
  }

  @Test
  void whenNotEqualThenEqualFail() throws Exception {
    final AffiliationId id1 = new AffiliationId("urn:affiliation:test1");
    final AffiliationId id2 = new AffiliationId("urn:affiliation:test2");

    MyDataEntity.validate(id1);
    MyDataEntity.validate(id2);
    assertNotEquals(id1, id2);
    assertNotSame(id1.hashCode(), id2.hashCode());
  }
}
