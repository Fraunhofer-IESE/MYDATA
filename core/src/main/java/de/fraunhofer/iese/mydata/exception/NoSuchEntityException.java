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

/**
 * Thrown when something is not found
 */
public class NoSuchEntityException extends Exception {

  private static final long serialVersionUID = -2259742065563233870L;

  private static final int STATUS = 404;

  public NoSuchEntityException(String string, Exception e) {
    super(string, e);
  }

  public NoSuchEntityException(String string) {
    super(string);
  }

  public NoSuchEntityException() {
    super();
  }

  /**
   * @return the status
   */
  public int getStatus() {
    return STATUS;
  }
}
