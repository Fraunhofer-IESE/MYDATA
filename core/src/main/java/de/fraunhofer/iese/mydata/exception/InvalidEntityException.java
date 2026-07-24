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

package de.fraunhofer.iese.mydata.exception;

import de.fraunhofer.iese.mydata.common.MyDataEntity;

import jakarta.validation.ConstraintViolation;

import java.util.Set;

/**
 * Thrown if an entity is not valid
 */
public class InvalidEntityException extends Exception {

  private static final long serialVersionUID = 6144566262845760930L;

  private String message;

  @SuppressWarnings("rawtypes")
  Set<ConstraintViolation> violations;

  private final int status = 400;

  /**
   * @param entity
   * @param message
   * @param violations
   * @param e
   */
  public InvalidEntityException(MyDataEntity entity, String message,
      @SuppressWarnings("rawtypes") Set<ConstraintViolation> violations, Throwable e) {
    super(e);
    this.setMessage(entity, message, violations);
    this.violations = violations;

  }

  /**
   * @param message
   */
  public InvalidEntityException(String message) {
    this.message = message;
  }

  /**
   * @param entity
   * @param violations
   */
  public InvalidEntityException(MyDataEntity entity,
      @SuppressWarnings("rawtypes") Set<ConstraintViolation> violations) {
    this.setMessage(entity, "The provided entity fails one or more integrity constraints:",
        violations);
    this.violations = violations;
  }

  /**
   * @param entity
   * @param violations
   * @param e
   */
  public InvalidEntityException(MyDataEntity entity,
      @SuppressWarnings("rawtypes") Set<ConstraintViolation> violations, Throwable e) {
    this(entity, "The provided entity fails one or more integrity constraints:", violations, e);
    this.violations = violations;
  }

  /**
   * @param string
   * @param e
   */
  public InvalidEntityException(String string, Exception e) {
    super(string, e);
    this.message = string;
  }

  @Override
  public String getMessage() {
    return this.message;
  }

  /**
   * @return the list of violations
   */
  @SuppressWarnings("rawtypes")
  public Set<ConstraintViolation> getConstraintViolations() {
    return this.violations;
  }

  private void setMessage(MyDataEntity entity, String message2,
      @SuppressWarnings("rawtypes") Set<ConstraintViolation> violations) {
    final StringBuilder b = new StringBuilder();

    b.append(message2);
    b.append(System.getProperty("line.separator"));

    if (entity != null) {
      b.append("Entity: ");
      b.append(entity.toString());
      b.append(" (");
      b.append(entity.getClass().getSimpleName());
      b.append("); ");
    }

    if (violations != null && !violations.isEmpty()) {
      b.append("Violations: ");

      for (final ConstraintViolation<?> cv : violations) {

        b.append("- ");
        b.append(cv.getRootBean().getClass().getSimpleName());
        b.append(".");
        b.append(cv.getPropertyPath());
        b.append(" (");
        b.append(cv.getInvalidValue());
        b.append(") ");
        b.append(cv.getMessage());
        b.append(System.getProperty("line.separator"));
      }
    }
    this.message = b.toString();
  }

  /**
   * @return the status
   */
  public int getStatus() {
    return this.status;
  }
}
