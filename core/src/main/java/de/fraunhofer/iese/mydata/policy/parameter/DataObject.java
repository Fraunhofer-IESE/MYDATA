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

import com.google.gson.internal.Primitives;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;

/**
 * A value of a certain {@link DataType} that is currently supported by MYDATA.
 *
 * @param  <T> the type of the value. Must be one of {@link DataType}.
 * @author     Fraunhofer IESE
 */
public class DataObject<T> extends MyDataEntity {

  /**
   * The Constant log.
   */
  private static final Logger log = LoggerFactory.getLogger("DataObject<T>");

  /**
   * Indicates a complex type.
   */
  boolean isComplex = false;

  /**
   * The value.
   */
  // do not set as transient!
  @NotNull
  @Valid
  private T value;

  /**
   * The {@link DataType} of the value.
   */
  @NotBlank
  private String type;

  /**
   * Used for JAXB
   */
  public DataObject() {
    // Used for JAXB
  }

  /**
   * Instantiates a new data object.
   *
   * @param type the type
   */
  public DataObject(Class<T> type) {
    this.type = type.getCanonicalName();
  }

  /**
   * Instantiates a new data object.
   *
   * @param value the value
   */
  public DataObject(T value) {
    if (value == null) {
      throw new IllegalArgumentException();
    }
    this.value = value;
    if (value.getClass() != null && value.getClass().getCanonicalName() != null) {
      this.type = value.getClass().getCanonicalName();
    }
  }

  /**
   * Instantiates a new data object.
   *
   * @param value the value
   * @param clazz the clazz
   */
  public DataObject(T value, Class<T> clazz) {
    this(value);
    if (value.getClass() != clazz && !this.primitiveTypesMatches(value, clazz)) {
      throw new IllegalArgumentException(
          "Incompatible data types: " + value.getClass() + ", " + clazz);
    }
    this.type = clazz.getCanonicalName();
  }

  /**
   * *.
   *
   * @param value the value
   * @param type  the type
   */
  public DataObject(T value, Type type) {
    this(value);
    if (StringUtils.isEmpty(this.type) && value.getClass() != null) {
      this.type = value.getClass().getCanonicalName();
    }
    if (StringUtils.isEmpty(this.type) && (null != type)) {
      this.type = type.getTypeName();
    }
  }

  public static <T> DataObject<T> constructClone(DataObject<T> dataObject) {
    final DataObject<T> copy = new DataObject<>();
    copy.value = dataObject.value;
    copy.type = dataObject.type;
    copy.isComplex = dataObject.isComplex;
    return copy;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof DataObject)) {
      return false;
    }

    final DataObject<?> that = (DataObject<?>) o;

    return new EqualsBuilder().append(this.isComplex, that.isComplex).append(this.value, that.value)
        .append(this.type, that.type).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(this.value).append(this.type).toHashCode();
  }

  /**
   * Gets the {@link DataType} of the value.
   *
   * @return the {@link DataType} of the value
   */
  public Class<?> getType() {
    try {
      return Class.forName(this.type);
    } catch (final ClassNotFoundException e) {
      log.trace("Class not found for " + this.type, e);
      return null;
    }
  }

  /**
   * Sets the type.
   *
   * @param type the type to set
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Gets the type name.
   *
   * @return the type name
   */
  public String getTypeName() {
    return this.type;
  }

  /**
   * Gets the value.
   *
   * @return the value
   */
  public T getValue() {
    return this.value;
  }

  /**
   * Sets the value.
   *
   * @param value the new value
   */
  public void setValue(T value) {
    this.value = value;
    if (this.value != null) {
      this.type = value.getClass().getCanonicalName();
    }
  }

  /**
   * Checks if is complex.
   *
   * @return true, if is complex
   */
  public boolean isComplex() {
    return this.isComplex;
  }

  /**
   * Sets the complex.
   *
   * @param b the new complex
   */
  public void setComplex(boolean b) {
    this.isComplex = b;
  }

  /**
   * Primitive types matches.
   *
   * @param  value the value
   * @param  clazz the clazz
   * @return       true, if successful
   */
  private boolean primitiveTypesMatches(T value, Class<T> clazz) {
    return Primitives.isWrapperType(value.getClass())
        && Primitives.unwrap(value.getClass()) == clazz
        || Primitives.isWrapperType(clazz) && Primitives.unwrap(clazz) == value.getClass();
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.api.common.MyDataEntity#toString()
   */
  @Override
  public String toString() {
    return this.value == null ? null : this.value.toString();
  }

}
