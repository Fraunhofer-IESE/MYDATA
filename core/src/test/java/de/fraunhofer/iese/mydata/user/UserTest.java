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

package de.fraunhofer.iese.mydata.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.solution.Solution;
import de.fraunhofer.iese.mydata.solution.SolutionId;
import de.fraunhofer.iese.mydata.solution.Timezone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

class UserTest {

  private User user;

  private Solution solution, solution2;

  private static final Timezone TIMEZONE;

  static {
    TIMEZONE = new Timezone();
    TIMEZONE.setZoneid("Europe/Berlin");
  }

  @BeforeEach
  void prepare() {
    this.user = new User();
    this.user.setUsername("testuser");
    this.user.setFirstName("Hansi");
    this.user.setLastName("Wurst");
    this.user.setEmail("hansi.wurst@example.com");
    this.user.setPassword("3nCrYp73dP422w0rD");
    this.user.setRole(MyDataRole.SOLUTION_DEVELOPER);

    this.solution = new Solution(new SolutionId("urn:solution:" + "test-solution"));
    this.solution.setName("Solution");
    this.solution.setLockStatus(false);
    this.solution.setTimezone(TIMEZONE);

    this.solution2 = new Solution(new SolutionId("urn:solution:" + "test-solution-2"));
    this.solution2.setName("Solution2");
    this.solution2.setLockStatus(false);
    this.solution2.setTimezone(TIMEZONE);
  }

  @Test
  void newUser() {
    final User testuser = new User();
    testuser.setUsername("testuser");
    testuser.setFirstName("Hansi");
    testuser.setLastName("Wurst");
    testuser.setEmail("hansi.wurst@example.com");
    testuser.setPassword("3nCrYp73dP422w0rD");
    testuser.setRole(MyDataRole.SOLUTION_DEVELOPER);

    assertFalse(testuser.isAccountLocked());
    assertTrue(testuser.isAccountNonLocked());
    assertTrue(testuser.isAccountNonExpired());
  }

  @Test
  void newUser_whenIsEmpty_thenThrowException() {
    final User testuser = new User();
    assertThrows(InvalidEntityException.class, () ->
      MyDataEntity.validate(testuser));
  }

  @Test
  void addSolution() {
    this.user.addSolution(this.solution);

    assertTrue(this.user.getSolutions().contains(this.solution));
  }

  @Test
  void addSolution_whenSolutionIsNull_throwException() {
    assertThrows(IllegalArgumentException.class, () ->
      this.user.addSolution(null));
  }

  @Test
  void removeSolution() {
    // add solutions
    this.user.addSolution(this.solution);
    this.user.addSolution(this.solution2);

    // remove one
    this.user.removeSolution(this.solution);

    // check
    assertFalse(this.user.getSolutions().contains(this.solution));
    assertTrue(this.user.getSolutions().contains(this.solution2));
  }

  @Test
  void removeSolution_whenSolutionIsNull_throwException() {
    assertThrows(IllegalArgumentException.class, () ->
      this.user.removeSolution(null));
  }

  @Test
  void setSolutions() {
    final Set<Solution> solutions = new HashSet<>();
    solutions.add(this.solution);
    solutions.add(this.solution2);

    this.user.setSolutions(solutions);

    assertEquals(solutions, this.user.getSolutions());
    assertTrue(this.user.getSolutions().contains(this.solution));
    assertTrue(this.user.getSolutions().contains(this.solution2));
  }

  @Test
  void setSolution_whenSolutionSetIsNull_throwException() {
    assertThrows(IllegalArgumentException.class, () ->
      this.user.setSolutions(null));
  }

  @Test
  void whenAdministratorThenIsAdminIsTrue() {
    this.user.setRole(MyDataRole.ADMINISTRATOR);

    assertTrue(this.user.isAdmin());
  }

  @Test
  void whenSuperAdminThenIsSuperAdminIsTrue() {
    this.user.setRole(MyDataRole.SUPER_ADMIN);

    assertTrue(this.user.isSuperAdmin());
  }

  @Test
  void whenSolutionDeveloperThenIsDevIsTrue() {
    this.user.setRole(MyDataRole.SOLUTION_DEVELOPER);

    assertTrue(this.user.isDev());
  }

  @Test
  void whenEqualThenEqualsAndHashCodeOk() {
    final String userUuid = this.user.getUserUUID();

    final User user2 = new User();
    user2.setUserUUID(userUuid); // same user id
    user2.setUsername("testuser");
    user2.setFirstName("Hansi");
    user2.setLastName("Wurst");
    user2.setEmail("hansi.wurst@example.com");
    user2.setPassword("3nCrYp73dP422w0rD");
    user2.setRole(MyDataRole.SOLUTION_DEVELOPER);

    assertEquals(this.user, user2);
    assertEquals(this.user.hashCode(), user2.hashCode());
  }

  @Test
  void whenUserIdIsDifferent_thenEqualsAndHashCodeFail() {
    // create a new user so the user id is different
    final User user2 = new User();

    // set the values from user
    user2.setUsername("testuser");
    user2.setFirstName("Hansi");
    user2.setLastName("Wurst");
    user2.setEmail("hansi.wurst@example.com");
    user2.setPassword("3nCrYp73dP422w0rD");
    user2.setRole(MyDataRole.SOLUTION_DEVELOPER);

    assertNotEquals(this.user, user2);
    assertNotEquals(this.user.hashCode(), user2.hashCode());
  }
}
