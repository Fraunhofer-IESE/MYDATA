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

package de.fraunhofer.iese.mydata.pdp;

import java.util.Arrays;

/**
 * The Class Evaluation.
 */
public class Evaluation {

  /**
   * The parameters.
   */
  public Object[] parameters;

  /**
   * The expected result.
   */
  public Object expectedResult;

  /**
   * The expected exception.
   */
  public Class<? extends Exception> expectedException;

  /**
   * Instantiates a new evaluation.
   *
   * @param expectedResult the expected result
   * @param objects the objects
   */
  public Evaluation(Object expectedResult, Object... objects) {
    this.parameters = objects;
    this.expectedResult = expectedResult;
  }

  /**
   * Instantiates a new evaluation.
   *
   * @param expectedException the expected exception
   * @param objects the objects
   */
  public Evaluation(Class<? extends Exception> expectedException, Object... objects) {
    this.parameters = objects;
    this.expectedException = expectedException;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    if (parameters != null) {
      for (Object o : parameters) {
        if (o != null && o.getClass().isArray()) {
          b.append(Arrays.toString((Object[])o));
        } else {
          b.append(o);
        }
        if (o != null) {
          b.append(" (");
          b.append(o.getClass().getSimpleName());
          b.append("), ");
        } else {
          b.append(", ");
        }
      }
    }

    b.append(" --> ");

    if (expectedException != null) {
      b.append(expectedException.getSimpleName());
    } else {
      b.append(expectedResult);
    }
    return b.toString();
  }
}
