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

import org.junit.jupiter.api.Test;

class ExecuteActionTest {

  @Test
  void whenSameIdThenEqualsAndHashCodeOk() {
    final ExecuteAction executeAction1 = new ExecuteAction("urn:action:solution:testaction");
    final ExecuteAction executeAction2 = new ExecuteAction("urn:action:solution:testaction");

    assertEquals(executeAction1, executeAction2);
    assertEquals(executeAction1.hashCode(), executeAction2.hashCode());
  }

  @Test
  void whenSameIdButDifferentParameterValuesThenEqualsAndHashCodeFail() {
    final ExecuteAction executeAction1 = new ExecuteAction("urn:action:solution:testaction");
    final ExecuteAction executeAction2 = new ExecuteAction("urn:action:solution:testaction");

    executeAction1.addParameter("testparam", "abc");
    executeAction2.addParameter("testparam", "xyz");

    assertNotEquals(executeAction1, executeAction2);
    assertNotEquals(executeAction1.hashCode(), executeAction2.hashCode());
  }
}
