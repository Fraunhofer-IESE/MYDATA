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

package de.fraunhofer.iese.mydata.timer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import org.junit.jupiter.api.Test;

class TimerIdTest {

  @Test
  void constructor2() throws Exception {
    final String urn = "urn:timer:dum:my";
    final TimerId t = new TimerId(urn);
    MyDataEntity.validateAndNullCheck(t);
    assertEquals("dum", SolutionId.fromTimerId(t).getUrn().split("urn:solution:")[1]);
    assertEquals(urn, t.toString());
  }

  @Test
  void constructor3() throws Exception {
    final String urn = "urn:timer:dum:my";
    final TimerId t = new TimerId(urn);
    MyDataEntity.validateAndNullCheck(t);
    assertEquals("urn:solution:dum", SolutionId.fromTimerId(t).toString());
    assertEquals("urn:timer:dum:my", t.toString());
  }

  @Test
  void constructor4() {
    final TimerId id = new TimerId("urn:policy:cd:34");
    assertThrows(InvalidEntityException.class, () ->
      MyDataEntity.validateAndNullCheck(id));
  }

  @Test
  void constructor5() {
    final TimerId id = new TimerId("hello world");
    assertThrows(InvalidEntityException.class, () ->
      MyDataEntity.validateAndNullCheck(id));
  }

  @Test
  void whenEqualThenEqualsAndHashCodeOk() {
    final String urn1 = "urn:timer:test:test-timer";
    final TimerId timerId1 = new TimerId(urn1);

    final String urn2 = "urn:timer:test:test-timer";
    final TimerId timerId2 = new TimerId(urn2);

    assertEquals(timerId1, timerId2);
    assertEquals(timerId1.hashCode(), timerId2.hashCode());
  }

  @Test
  void wheNotEqualThenEqualsAndHashCodeFail() {
    final String urn1 = "urn:timer:test:test-timer1";
    final TimerId timerId1 = new TimerId(urn1);

    final String urn2 = "urn:timer:test:test-timer2";
    final TimerId timerId2 = new TimerId(urn2);

    assertNotEquals(timerId1, timerId2);
    assertNotEquals(timerId1.hashCode(), timerId2.hashCode());
  }

}
