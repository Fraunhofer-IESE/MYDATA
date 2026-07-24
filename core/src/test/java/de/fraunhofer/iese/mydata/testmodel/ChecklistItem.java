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

package de.fraunhofer.iese.mydata.testmodel;

import java.util.Date;

/**
 * The Class ChecklistItem.
 */
public class ChecklistItem {

  /**
   * The title.
   */
  String title;

  /**
   * The description.
   */
  String description;

  /**
   * The due date.
   */
  Date dueDate;

  /**
   * The done date.
   */
  Date doneDate;

  /**
   * The done.
   */
  boolean done = false;

  /**
   * Instantiates a new checklist item.
   *
   * @param title       the title
   * @param description the description
   * @param dueDate     the due date
   */
  public ChecklistItem(String title, String description, Date dueDate) {
    super();
    this.title = title;
    this.description = description;
    this.dueDate = dueDate;
  }

  /**
   * Gets the title.
   *
   * @return the title
   */
  public String getTitle() {
    return this.title;
  }

  /**
   * Sets the title.
   *
   * @param title the new title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Gets the description.
   *
   * @return the description
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * Sets the description.
   *
   * @param description the new description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Gets the due date.
   *
   * @return the due date
   */
  public Date getDueDate() {
    return this.dueDate;
  }

  /**
   * Sets the due date.
   *
   * @param dueDate the new due date
   */
  public void setDueDate(Date dueDate) {
    this.dueDate = dueDate;
  }

  /**
   * Checks if is done.
   *
   * @return true, if is done
   */
  public boolean isDone() {
    return this.done;
  }

  /**
   * Sets the done.
   *
   * @param done the new done
   */
  public void setDone(boolean done) {
    this.done = done;
  }

  /**
   * Gets the done date.
   *
   * @return the done date
   */
  public Date getDoneDate() {
    return this.doneDate;
  }

  /**
   * Sets the done date.
   *
   * @param doneDate the new done date
   */
  public void setDoneDate(Date doneDate) {
    this.doneDate = doneDate;
  }

}
