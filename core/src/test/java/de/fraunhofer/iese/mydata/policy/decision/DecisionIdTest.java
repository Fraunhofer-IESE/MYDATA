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

package de.fraunhofer.iese.mydata.policy.decision;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;

import org.junit.jupiter.api.Test;

class DecisionIdTest {

  @Test
  void whenDecisionIdIsValidThenOk() throws Exception {
    final String urn = "urn:decision:test1234";
    final DecisionId decisionId = new DecisionId(urn);

    MyDataEntity.validate(decisionId);
  }

  @Test
  void whenDecisionIdIsNotValidThenThrowException() {
    final DecisionId decisionId = new DecisionId("urn:cfl:fkjhf");
    assertThrows(InvalidEntityException.class, () ->
      MyDataEntity.validate(decisionId));
  }

  @Test
  void whenEqualThenEqualsAndHashCodeOk() {
    final String urn1 = "urn:decision:test1234";
    final DecisionId decisionId1 = new DecisionId(urn1);

    final String urn2 = "urn:decision:test1234";
    final DecisionId decisionId2 = new DecisionId(urn2);

    assertEquals(decisionId1, decisionId2);
    assertEquals(decisionId1.hashCode(), decisionId2.hashCode());
  }

  @Test
  void whenNotEqualsThenQualsAndHashCodeFail() {
    final String urn1 = "urn:decision:test1234";
    final DecisionId decisionId1 = new DecisionId(urn1);

    final String urn2 = "urn:decision:test4567";
    final DecisionId decisionId2 = new DecisionId(urn2);

    assertNotEquals(decisionId1, decisionId2);
    assertNotEquals(decisionId1.hashCode(), decisionId2.hashCode());
  }
}
