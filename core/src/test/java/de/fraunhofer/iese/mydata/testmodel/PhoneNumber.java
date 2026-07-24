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

/**
 * The Class PhoneNumber.
 */
public class PhoneNumber {

  /**
   * The type.
   */
  private PhoneType type;

  /**
   * The number.
   */
  private String number;

  /**
   * Instantiates a new phone number.
   *
   * @param type   the type
   * @param number the number
   */
  public PhoneNumber(PhoneType type, String number) {
    super();
    this.type = type;
    this.number = number;
  }

  /**
   * Gets the type.
   *
   * @return the type
   */
  public PhoneType getType() {
    return this.type;
  }

  /**
   * Sets the type.
   *
   * @param type the new type
   */
  public void setType(PhoneType type) {
    this.type = type;
  }

  /**
   * Gets the number.
   *
   * @return the number
   */
  public String getNumber() {
    return this.number;
  }

  /**
   * Sets the number.
   *
   * @param number the new number
   */
  public void setNumber(String number) {
    this.number = number;
  }

}
