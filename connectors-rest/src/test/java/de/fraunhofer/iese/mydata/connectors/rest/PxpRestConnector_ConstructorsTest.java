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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.URI;

/**
 * The Class PxpRestConnector_ConstructorsTest.
 */
public class PxpRestConnector_ConstructorsTest {

  /**
   * Sets the up.
   */
  @BeforeEach
  public void setUp() {
  }

  /**
   * Test constructor and uris NULL.
   */
  @Test
  public void testConstructorAndUrisNULL() {
    try {
      new PxpRestConnector((String)null);
      Assertions.fail("No exception has been thrown. Error in PxpRestConnector(string)");
    } catch (final Exception e) {
      assertThat(e, instanceOf(NullPointerException.class));
      Assertions.assertTrue(true);
    }
  }

  /**
   * Test constructor and uris wrong format.
   */
  @Test
  public void testConstructorAndUrisWrongFormat() {
    try {
      new PxpRestConnector("demo:demo://demo:demo");
      Assertions.fail("No exception has been thrown. Error in PxpRestConnector(string)");
    } catch (final Exception e) {
      assertThat(e, instanceOf(IllegalArgumentException.class));
      Assertions.assertTrue(true);
    }
  }

  /**
   * Test constructor and uris as string.
   */
  @Test
  public void testConstructorAndUrisAsString() {
    try {
      final PxpRestConnector connector = new PxpRestConnector(Constants.BASE_URL);
      this.assertValues(connector);
    } catch (final Exception e) {
      Assertions.fail("Exception has been thrown. Error in PxpRestConnector(string): " + e.getMessage());
    }
  }

  /**
   * Test constructor and uris as uri.
   */
  @Test
  public void testConstructorAndUrisAsUri() {
    try {
      final URI uri = new URI(Constants.BASE_URL);
      final PxpRestConnector connector = new PxpRestConnector(uri);
      this.assertValues(connector);
    } catch (final Exception e) {
      Assertions.fail("Exception has been thrown. Error in PxpRestConnector(string): " + e.getMessage());
    }
  }

  /**
   * Test constructor and uris plain.
   */
  @Test
  public void testConstructorAndUrisPlain() {
    try {
      final PxpRestConnector connector = new PxpRestConnector(Constants.SCHEME, Constants.HOST, Constants.PORT, Constants.NAME);
      this.assertValues(connector);
    } catch (final Exception e) {
      Assertions.fail("Exception has been thrown. Error in PxpRestConnector(string): " + e.getMessage());
    }
  }

  /**
   * Assert values.
   *
   * @param connector the connector
   */
  private void assertValues(PxpRestConnector connector) {
    assertThat(connector.getBaseUrl(), equalTo(Constants.BASE_URL));
    assertThat((String)getInternalState(connector, "host"), equalTo(Constants.HOST));
    assertThat((String)getInternalState(connector, "name"), equalTo(Constants.NAME));
    assertThat((String)getInternalState(connector, "scheme"), equalTo(Constants.SCHEME));
    assertThat((Integer)getInternalState(connector, "port"), equalTo(Integer.valueOf(Constants.PORT)));
  }

  protected static Object getInternalState(Object o, String string) {
    try{
      if (o instanceof Class<?>) {
        Class<?> c = (Class<?>) o;
        Field f = getFieldFromHierarchy(c, string);
        f.setAccessible(true);
        return f.get(null);
      } else {
        Field f = getFieldFromHierarchy(o.getClass(), string);
        f.setAccessible(true);
        return f.get(o);
      }
    } catch (Exception e){
      throw new RuntimeException(e);
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
      throw new RuntimeException("You want me to get this field: '" + field + "' on this class: '" + clazz.getSimpleName() + "' but this field is not declared within the hierarchy of this class!");
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
