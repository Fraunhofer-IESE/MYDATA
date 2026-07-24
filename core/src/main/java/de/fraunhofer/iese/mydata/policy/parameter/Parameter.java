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

import de.fraunhofer.iese.mydata.common.MyDataEntity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.lang.reflect.Type;

/**
 * A parameter, basically a key-value pair.
 *
 * @param  <T> the type of the value
 * @author     Fraunhofer IESE
 */
public class Parameter<T> extends MyDataEntity {

  /**
   * The name of the parameter.
   */
  @NotBlank
  private final String name;

  /**
   * The value of the parameter.
   */
  @NotNull
  @Valid
  private DataObject<T> value;

  /**
   * Instantiates a new parameter.
   *
   * @param name  the name
   * @param value the value
   */
  public Parameter(String name, T value) {
    this.name = name;
    this.value = new DataObject<>(value);
  }

  /**
   * Instantiates a new parameter.
   *
   * @param name  the name
   * @param value the value
   * @param clazz the clazz
   */
  public Parameter(String name, T value, Class<T> clazz) {
    this.name = name;
    this.value = new DataObject<>(value, clazz);
  }

  /**
   * Instantiates a new parameter.
   *
   * @param name      the name
   * @param value     the value
   * @param valueType the value type
   */
  public Parameter(String name, T value, Type valueType) {
    this.name = name;
    this.value = new DataObject<>(value, valueType);
  }

  public String getName() {
    return this.name;
  }

  /**
   * Gets the type.
   *
   * @return the type
   */
  public Class<?> getType() {
    return this.value.getType();
  }

  /**
   * Gets the type name.
   *
   * @return the type name
   */
  public String getTypeName() {
    return this.value.getTypeName();
  }

  /**
   * Gets the value of the parameter.
   *
   * @return the value of the parameter
   */
  public T getValue() {
    return this.value.getValue();
  }

  /**
   * Sets the value of the parameter.
   *
   * @param value the new value of the parameter
   */
  public void setValue(T value) {
    this.value = new DataObject<>(value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof Parameter)) {
      return false;
    }

    final Parameter<?> parameter = (Parameter<?>) o;

    return new EqualsBuilder().append(this.name, parameter.name).append(this.value, parameter.value)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(this.name).append(this.value).toHashCode();
  }

  @Override
  public String toString() {
    return "  " + this.name + ": " + this.value;
  }

}
