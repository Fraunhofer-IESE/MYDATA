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

package de.fraunhofer.iese.mydata.common;

import de.fraunhofer.iese.mydata.exception.InvalidEntityException;

import com.google.gson.Gson;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.util.Set;

/**
 * Common super class for all other MYDATA classes.
 *
 * @author Fraunhofer IESE
 */
public abstract class MyDataEntity {

  private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory()
      .getValidator();

  private static final Gson GSON_DEFAULT;

  private static final Gson GSON_PRETTY;

  static {
    GSON_DEFAULT = GsonUtil.createDefaultGson();
    GSON_PRETTY = GSON_DEFAULT.newBuilder().setPrettyPrinting().create();
  }

  /**
   * Deserializes an MyDataEntity from JSON.
   *
   * @param  <T>   the generic type (sub class of {@link MyDataEntity})
   * @param  json  the serialized object in JSON notation
   * @param  clazz the generic type (sub class of {@link MyDataEntity})
   * @return       the deserialized {@link MyDataEntity}
   */
  public static <T extends MyDataEntity> T fromJson(String json, Class<T> clazz) {
    return GSON_PRETTY.fromJson(json, clazz);
  }

  /**
   * Gets the gson.
   *
   * @return the gson
   */
  public static Gson getGson() {
    return MyDataEntity.getGson(false);
  }

  /**
   * @param  pretty
   * @return        the serialized entity
   */
  public static Gson getGson(boolean pretty) {
    if (pretty) {
      return MyDataEntity.GSON_PRETTY;
    } else {
      return MyDataEntity.GSON_DEFAULT;
    }
  }

  /**
   * @param  entity
   * @param  validationGroups
   * @throws InvalidEntityException
   */
  public static void validate(MyDataEntity entity, Class<?>... validationGroups)
      throws InvalidEntityException {
    if (entity == null) {
      return;
    }

    validateAndNullCheck(entity, validationGroups);
  }

  /**
   * @param  entity
   * @param  validationGroups
   * @throws InvalidEntityException
   */
  @SuppressWarnings({
      "rawtypes", "unchecked"
  })
  public static void validateAndNullCheck(MyDataEntity entity, Class<?>... validationGroups)
      throws InvalidEntityException {
    if (entity == null) {
      throw new InvalidEntityException("Entity must not be null");
    }

    final Set violations = VALIDATOR.validate(entity, validationGroups);

    if (!violations.isEmpty()) {
      throw new InvalidEntityException(entity, violations);
    }
  }

  /**
   * Serializes an MyDataEntity to JSON.
   *
   * @param  pretty if true, pretty printing is enabled
   * @return        the JSON serialized {@link MyDataEntity}
   */
  public String toJson(boolean pretty) {
    return MyDataEntity.getGson(pretty).toJson(this);
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return this.toJson(false);
  }
}
