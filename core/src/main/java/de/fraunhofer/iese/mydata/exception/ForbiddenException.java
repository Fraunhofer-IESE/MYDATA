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
 * Thrown when something is forbidden, but has nothing to do with authorization. Unlike an access
 * denied exception, it's independent of the authorization, so even if the authorization is correct,
 * it may still be forbidden.
 */
public class ForbiddenException extends Exception {

  private static final long serialVersionUID = 8710260320451384477L;

  private static final int STATUS = 403;

  public ForbiddenException(String string, Exception e) {
    super(string, e);
  }

  public ForbiddenException(String string) {
    super(string);
  }

  public int getStatus() {
    return STATUS;
  }
}
