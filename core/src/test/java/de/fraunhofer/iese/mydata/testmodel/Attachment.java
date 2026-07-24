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
 * The Class Attachment.
 */
public class Attachment {

  /**
   * The description.
   */
  String description;

  /**
   * The author.
   */
  Person author;

  /**
   * The mediatype.
   */
  String mediatype;

  /**
   * The content.
   */
  String content;

  /**
   * The creation date.
   */
  Date creationDate;

  /**
   * Instantiates a new attachment.
   *
   * @param description the description
   * @param author      the author
   * @param mediatype   the mediatype
   * @param content     the content
   */
  public Attachment(String description, Person author, String mediatype, String content) {
    super();
    this.description = description;
    this.author = author;
    this.mediatype = mediatype;
    this.content = content;
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
   * Gets the author.
   *
   * @return the author
   */
  public Person getAuthor() {
    return this.author;
  }

  /**
   * Sets the author.
   *
   * @param author the new author
   */
  public void setAuthor(Person author) {
    this.author = author;
  }

  /**
   * Gets the mediatype.
   *
   * @return the mediatype
   */
  public String getMediatype() {
    return this.mediatype;
  }

  /**
   * Sets the mediatype.
   *
   * @param mediatype the new mediatype
   */
  public void setMediatype(String mediatype) {
    this.mediatype = mediatype;
  }

  /**
   * Gets the content.
   *
   * @return the content
   */
  public String getContent() {
    return this.content;
  }

  /**
   * Sets the content.
   *
   * @param content the new content
   */
  public void setContent(String content) {
    this.content = content;
  }

  /**
   * Gets the creation date.
   *
   * @return the creation date
   */
  public Date getCreationDate() {
    return this.creationDate;
  }

  /**
   * Sets the creation date.
   *
   * @param creationDate the new creation date
   */
  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

}
