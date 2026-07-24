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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SolutionTest {

  @Test
  void newSolution() {
    final String solutionUrn = "urn:solution:test-solution";
    final SolutionId solutionId = new SolutionId(solutionUrn);
    final Solution solution = new Solution(solutionId);

    assertFalse(solution.isSolutionLocked());
    assertEquals(solutionId, solution.getSolutionId());

    // users should not be null, but empty
    assertNotNull(solution.getUsers());
    assertTrue(solution.getUsers().isEmpty());

    // policies should not be null, but empty
    assertNotNull(solution.getPolicies());
    assertTrue(solution.getPolicies().isEmpty());

    // timers should not be null, but empty
    assertNotNull(solution.getTimers());
    assertTrue(solution.getTimers().isEmpty());

    // library clients should not be null, but empty
    assertNotNull(solution.getLibraryClients());
    assertTrue(solution.getLibraryClients().isEmpty());

    // peps should not be null, but empty
    assertNotNull(solution.getPeps());
    assertTrue(solution.getPeps().isEmpty());

    // pips should not be null, but empty
    assertNotNull(solution.getPips());
    assertTrue(solution.getPips().isEmpty());

    // pxps should not be null, but empty
    assertNotNull(solution.getPxps());
    assertTrue(solution.getPxps().isEmpty());
  }

  @Test
  void newSolution_whenSolutionIdIsNull_throwException() {
    assertThrows(IllegalArgumentException.class, () -> {
      new Solution(null);
    });
  }

  @Test
  void setSolutionId() {
    final String solutionUrn = "urn:solution:test-solution";
    final SolutionId solutionId = new SolutionId(solutionUrn);
    final Solution solution = new Solution(solutionId);

    final String solutionUrn2 = "urn:solution:test-solution2";
    final SolutionId newSolutionId = new SolutionId(solutionUrn2);

    solution.setSolutionId(newSolutionId);

    assertEquals(newSolutionId, solution.getSolutionId());
  }

  @Test
  void setSolutionIdAsUrnString() {
    final String solutionUrn = "urn:solution:test-solution";
    final SolutionId solutionId = new SolutionId(solutionUrn);
    final Solution solution = new Solution(solutionId);

    final String solutionUrn2 = "urn:solution:test-solution2";

    solution.setSolutionId(solutionUrn2);

    assertEquals(solutionUrn2, solution.getSolutionId().getUrn());
  }

  @Test
  void whenSetUsersIsNull_throwException() {
    final String solutionUrn = "urn:solution:test-solution";
    final SolutionId solutionId = new SolutionId(solutionUrn);
    final Solution solution = new Solution(solutionId);
    assertThrows(IllegalArgumentException.class, () ->

      solution.setUsers(null));
  }

  @Test
  void equalAndHashCode() {
    final String solutionUrn1 = "urn:solution:test-solution1";
    final SolutionId solutionId1 = new SolutionId(solutionUrn1);
    final Solution solution1 = new Solution(solutionId1);

    final String solutionUrn2 = "urn:solution:test-solution1";
    final SolutionId solutionId2 = new SolutionId(solutionUrn2);
    final Solution solution2 = new Solution(solutionId2);

    assertEquals(solution1, solution2);
    assertEquals(solution1.hashCode(), solution2.hashCode());
  }
}
