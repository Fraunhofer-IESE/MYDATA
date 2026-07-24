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

package de.fraunhofer.iese.mydata.pep.testdata.model;

import java.util.List;

/**
 * The Class Company.
 */
public class Company {

  /**
   * The name.
   */
  String name;

  /**
   * The employees.
   */
  List<Person> employees;

  /**
   * Instantiates a new company.
   *
   * @param name      the name
   * @param employees the employees
   */
  public Company(String name, List<Person> employees) {
    super();
    this.name = name;
    this.employees = employees;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  /**
   * Sets the name.
   *
   * @param name the new name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the employees.
   *
   * @return the employees
   */
  public List<Person> getEmployees() {
    return this.employees;
  }

  /**
   * Sets the employees.
   *
   * @param employees the new employees
   */
  public void setEmployees(List<Person> employees) {
    this.employees = employees;
  }

}
