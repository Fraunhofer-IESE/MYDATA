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
 * Thrown if the update of a resource fails
 */
public class ResourceUpdateException extends Exception {

  private static final int STATUS = 500;

  private static final long serialVersionUID = -4421915359018591099L;

  public ResourceUpdateException(String string, Exception e) {
    super(string, e);
  }

  public ResourceUpdateException(String string) {
    super(string);
  }

  public int getStatus() {
    return STATUS;
  }
}
