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

package de.fraunhofer.iese.mydata.policy.parameter;

import java.util.ArrayList;

/**
 * Types that are currently supported by MYDATA.
 *
 * @author Fraunhofer IESE
 */
public enum DataType {

  /**
   * The string.
   */
  STRING(String.class, 0),
  /**
   * The bool.
   */
  BOOL(Boolean.class, 8),
  /**
   * The int.
   */
  INT(Integer.class, 6),
  /**
   * The long.
   */
  LONG(Long.class, 7),
  /**
   * The float.
   */
  FLOAT(Float.class, 10),
  /**
   * The double.
   */
  DOUBLE(Double.class, 11),
  /**
   * The datausage.
   */
  DATAUSAGE(String.class, 1),
  /**
   * The xpath.
   */
  XPATH(String.class, 2),
  /**
   * The regex.
   */
  REGEX(String.class, 3),
  /**
   * The context.
   */
  CONTEXT(String.class, 4),
  /**
   * The binary.
   */
  BINARY(byte[].class, 5),
  /**
   * The string array.
   */
  STRING_ARRAY(String[].class, 9),

  /**
   * The array.list
   */
  ARRAYLIST(ArrayList.class, 12);

  /**
   * The clazz.
   */
  private Class<?> clazz;

  /**
   * The type component_id.
   */
  private int typeId;

  /**
   * Instantiates a new data type.
   *
   * @param clazz the clazz
   * @param typeId the type component_id
   */
  DataType(Class<?> clazz, int typeId) {
    this.clazz = clazz;
    this.typeId = typeId;
  }

  /**
   * Gets the by class.
   *
   * @param clazz the clazz
   * @return the by class
   */
  public static DataType getByClass(Class<?> clazz) {
    for (final DataType type : DataType.values()) {
      if (type.getDataTypeClass() == clazz) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unsupported Type");
  }

  /**
   * Gets the by component_id.
   *
   * @param id the component_id
   * @return the by component_id
   */
  public static DataType getById(int id) {
    for (final DataType type : DataType.values()) {
      if (type.getDataTypeId() == id) {
        return type;
      }
    }
    return null;
  }

  /**
   * Gets the data type class.
   *
   * @return the data type class
   */
  public Class<?> getDataTypeClass() {
    return this.clazz;
  }

  /**
   * Gets the data type component_id.
   *
   * @return the data type component_id
   */
  public int getDataTypeId() {
    return this.typeId;
  }

  /**
   * Matches.
   *
   * @param clazz the clazz
   * @param id the component_id
   * @return true, if successful
   */
  public boolean matches(Class<?> clazz, int id) {
    for (final DataType type : DataType.values()) {
      if (type.getDataTypeId() == id) {
        return clazz == type.getDataTypeClass();
      }
    }
    return false;
  }
}
