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

package de.fraunhofer.iese.mydata.pdp.language.model.function;

import jakarta.xml.bind.annotation.XmlEnum;

/**
 * The Enum EMultiFunctionMode represents different evaluation modes for list operations.
 */
@XmlEnum

@SuppressWarnings("javadoc")
public enum EnumTimeExpression {

  /**
   * Before a specific time.
   */
  BEFORE("before"),
  /**
   * After a specific time.
   */
  AFTER("after"),

  /**
   * Exactly at a specific time.
   */
  EXACTLY("exactly");

  private final String value;

  EnumTimeExpression(String value) {
    this.value = value;
  }

  public String value() {
    return this.value;
  }
}
