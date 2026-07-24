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

/**
 * Roles for the PMP.
 */
public enum MyDataRole {

  SOLUTION_DEVELOPER("Developer"),

  ADMINISTRATOR("Admin"),
  /**
   * Role that is able to manage all affiliations
   */
  SUPER_ADMIN("Super_Admin"),
  /**
   * This role is not stored with user. It' for components, only.
   */
  PDP("PDP"), PMP("PMP"), CLIENT("CLIENT"),

  MASTER_LIBRARY_CLIENT("MASTER_LIBRARY_CLIENT"), LIBRARY_CLIENT("LIBRARY_CLIENT"), TECH_CLIENT("TECH_CLIENT"),

  USER_CLIENT("USER_CLIENT");

  private final String text;

  private MyDataRole(final String text) {
    this.text = text;
  }

  /**
   * To spring role.
   *
   * @return the string
   */
  public String toSpringRole() {
    return "ROLE_" + this.name();
  }

  @Override
  public String toString() {
    return this.text;
  }
}
