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

package de.fraunhofer.iese.mydata.connectors.rest;

import de.fraunhofer.iese.mydata.exception.InvalidEntityException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * This class is setting up the common class attributes and mock objects for all tests.
 */
public abstract class CommonTestSetup {

  @BeforeEach
  public void setUp() throws UnsupportedOperationException, IOException, InvalidEntityException {

  }

  /**
   * Reset.
   */
  @AfterEach
  public void reset() {

  }

  protected static Object getInternalState(Object o, String string) throws NoSuchFieldException,
      SecurityException, IllegalArgumentException, IllegalAccessException {
    if (o instanceof Class<?>) {
      final Class<?> c = (Class<?>) o;
      final Field f = getFieldFromHierarchy(c, string);
      f.setAccessible(true);
      return f.get(null);
    } else {
      final Field f = getFieldFromHierarchy(o.getClass(), string);
      f.setAccessible(true);
      return f.get(o);
    }
  }

  protected static void setInternalState(Object target, String field, Object value) {
    if (target instanceof Class<?>) {
      final Class<?> c = (Class<?>) target;
      try {
        final Field f = getFieldFromHierarchy(c, field);
        f.setAccessible(true);
        f.set(null, value);
      } catch (final Exception e) {
        throw new RuntimeException("Unable to set internal state on a private field.", e);
      }
    } else {
      final Class<?> c = target.getClass();
      try {
        final Field f = getFieldFromHierarchy(c, field);
        f.setAccessible(true);
        f.set(target, value);
      } catch (final Exception e) {
        throw new RuntimeException("Unable to set internal state on a private field.", e);
      }
    }
  }

  protected static Field getFieldFromHierarchy(Class<?> clazz, String field) {
    Field f = getField(clazz, field);
    while (f == null && clazz != Object.class) {
      clazz = clazz.getSuperclass();
      f = getField(clazz, field);
    }
    if (f == null) {
      throw new RuntimeException("You want me to get this field: '"
          + field
          + "' on this class: '"
          + clazz.getSimpleName()
          + "' but this field is not declared within the hierarchy of this class!");
    }
    return f;
  }

  protected static Field getField(Class<?> clazz, String field) {
    try {
      return clazz.getDeclaredField(field);
    } catch (final NoSuchFieldException e) {
      return null;
    }
  }
}
