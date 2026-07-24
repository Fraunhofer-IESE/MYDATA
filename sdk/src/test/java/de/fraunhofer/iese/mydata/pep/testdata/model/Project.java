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

import java.util.ArrayList;
import java.util.List;

/**
 * The Class Project.
 */
public class Project {

  /**
   * The project id.
   */
  private String projectId;

  /**
   * The name.
   */
  private String name;

  /**
   * The project leader.
   */
  private Person projectLeader;

  /**
   * The budget.
   */
  private float budget;

  /**
   * The tasks.
   */
  private List<Task> tasks;

  /**
   * Instantiates a new project.
   *
   * @param projectId     the project id
   * @param name          the name
   * @param projectLeader the project leader
   * @param budget        the budget
   */
  public Project(String projectId, String name, Person projectLeader, float budget) {
    this.name = name;
    this.projectLeader = projectLeader;
    this.budget = budget;
    this.projectId = projectId;
  }

  /**
   * Adds the task.
   *
   * @param t the t
   */
  public void addTask(Task t) {
    if (this.tasks == null) {
      this.tasks = new ArrayList<>();
    }
    this.tasks.add(t);
  }

  /**
   * Gets the tasks.
   *
   * @return the tasks
   */
  public List<Task> getTasks() {
    return this.tasks;
  }

  /**
   * Sets the tasks.
   *
   * @param tasks the new tasks
   */
  public void setTasks(List<Task> tasks) {
    this.tasks = tasks;
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
   * Gets the project leader.
   *
   * @return the project leader
   */
  public Person getProjectLeader() {
    return this.projectLeader;
  }

  /**
   * Sets the project leader.
   *
   * @param projectLeader the new project leader
   */
  public void setProjectLeader(Person projectLeader) {
    if (projectLeader.getRole() != Role.CSM) {
      throw new IllegalArgumentException("Only a CSM can be project manager.");
    }
    this.projectLeader = projectLeader;
  }

  /**
   * Gets the budget.
   *
   * @return the budget
   */
  public float getBudget() {
    return this.budget;
  }

  /**
   * Sets the budget.
   *
   * @param budget the new budget
   */
  public void setBudget(float budget) {
    this.budget = budget;
  }

  /**
   * Gets the project id.
   *
   * @return the projectId
   */
  public String getProjectId() {
    return this.projectId;
  }

  /**
   * Sets the project id.
   *
   * @param projectId the projectId to set
   */
  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

}
