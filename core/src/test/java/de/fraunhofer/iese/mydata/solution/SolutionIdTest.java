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

package de.fraunhofer.iese.mydata.solution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;

import org.junit.jupiter.api.Test;

class SolutionIdTest {

  @Test
  void whenSolutionIdValidThenOk() throws Exception {
    final SolutionId solutionId = new SolutionId("urn:solution:test-solution");

    MyDataEntity.validate(solutionId);
  }

  @Test
  void whenSolutionIdIsOnlyIdentifierUrnWillBeCompleted() {
    final SolutionId solutionId = new SolutionId("test-solution");

    assertEquals("urn:solution:test-solution", solutionId.getUrn());
  }

  @Test
  void whenSolutionIdIsNull_throwsException() {
    assertThrows(InvalidEntityException.class, () ->
      MyDataEntity.validate(new SolutionId()));
  }

  @Test
  void whenSolutionIdIsInvalid_throwException() {
    final SolutionId solutionId = new SolutionId("urn:component:pip:test");
    assertThrows(InvalidEntityException.class, () ->
      MyDataEntity.validate(solutionId));
  }
}
