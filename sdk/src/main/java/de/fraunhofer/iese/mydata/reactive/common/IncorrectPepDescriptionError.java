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

package de.fraunhofer.iese.mydata.reactive.common;

/**
 * Incorrect Pep description error
 */
public class IncorrectPepDescriptionError extends RuntimeException {

  /**
   *
   */
  private static final long serialVersionUID = 2705967277310021246L;

  /**
   * Instantiates a new incorrect Pep description error.
   *
   * @param message the message
   */
  public IncorrectPepDescriptionError(String message) {
    super(message);
  }

  /**
   * For wrapping exceptions.
   *
   * @param message  Message of exception
   * @param causedBy caused by exception that should be wrapped.
   */
  public IncorrectPepDescriptionError(String message, Exception causedBy) {
    super(message, causedBy);
  }
}
