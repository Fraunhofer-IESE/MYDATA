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
import java.util.Date;
import java.util.List;

/**
 * The Class Task.
 */
public class Task {

  /**
   * The task id.
   */
  private String taskId;

  /**
   * The name.
   */
  private String name;

  /**
   * The due date.
   */
  private Date dueDate;

  /**
   * The description.
   */
  private String description;

  /**
   * The foreman.
   */
  private Person foreman;

  /**
   * The lat.
   */
  private float lat;

  /**
   * The lon.
   */
  private float lon;

  /**
   * The checklist.
   */
  private Checklist checklist;

  /**
   * The attachments.
   */
  private List<Attachment> attachments;

  /**
   * Instantiates a new task.
   *
   * @param taskId      the task id
   * @param name        the name
   * @param dueDate     the due date
   * @param description the description
   * @param foreman     the foreman
   * @param lat         the lat
   * @param lon         the lon
   */
  public Task(String taskId, String name, Date dueDate, String description, Person foreman,
      float lat, float lon) {
    super();
    this.taskId = taskId;
    this.name = name;
    this.dueDate = dueDate;
    this.description = description;
    this.foreman = foreman;
    this.lat = lat;
    this.lon = lon;
  }

  /**
   * Adds the attachment.
   *
   * @param a the a
   */
  public void addAttachment(Attachment a) {
    if (this.attachments == null) {
      this.attachments = new ArrayList<>();
    }
    this.attachments.add(a);
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
   * Gets the foreman.
   *
   * @return the foreman
   */
  public Person getForeman() {
    return this.foreman;
  }

  /**
   * Sets the foreman.
   *
   * @param foreman the new foreman
   */
  public void setForeman(Person foreman) {
    this.foreman = foreman;
  }

  /**
   * Gets the lat.
   *
   * @return the lat
   */
  public float getLat() {
    return this.lat;
  }

  /**
   * Sets the lat.
   *
   * @param lat the new lat
   */
  public void setLat(float lat) {
    this.lat = lat;
  }

  /**
   * Gets the lon.
   *
   * @return the lon
   */
  public float getLon() {
    return this.lon;
  }

  /**
   * Sets the lang.
   *
   * @param lang the new lang
   */
  public void setLang(float lang) {
    this.lon = lang;
  }

  /**
   * Gets the checklist.
   *
   * @return the checklist
   */
  public Checklist getChecklist() {
    return this.checklist;
  }

  /**
   * Sets the checklist.
   *
   * @param checklist the new checklist
   */
  public void setChecklist(Checklist checklist) {
    this.checklist = checklist;
  }

  /**
   * Gets the attachments.
   *
   * @return the attachments
   */
  public List<Attachment> getAttachments() {
    return this.attachments;
  }

  /**
   * Sets the attachments.
   *
   * @param attachments the new attachments
   */
  public void setAttachments(List<Attachment> attachments) {
    this.attachments = attachments;
  }

  /**
   * Gets the task id.
   *
   * @return the taskId
   */
  public String getTaskId() {
    return this.taskId;
  }

  /**
   * Sets the task id.
   *
   * @param taskId the taskId to set
   */
  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

}
