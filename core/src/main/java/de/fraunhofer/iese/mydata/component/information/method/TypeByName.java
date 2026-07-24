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

package de.fraunhofer.iese.mydata.component.information.method;

import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Like {@link Class#forName(String)}, but primitive are supported, too.
 */
@UtilityClass
public class TypeByName {

  /** The logger. */
  private static final Logger LOG = LoggerFactory.getLogger(TypeByName.class);

  /**
   * Primitives can not be instantiated by Class.forName.
   */
  private static Map<String, Class<?>> primitivesByName = new HashMap<>();

  static {
    primitivesByName.put("byte", Byte.TYPE);
    primitivesByName.put("boolean", Boolean.TYPE);
    primitivesByName.put("char", Character.TYPE);
    primitivesByName.put("short", Short.TYPE);
    primitivesByName.put("int", Integer.TYPE);
    primitivesByName.put("long", Long.TYPE);
    primitivesByName.put("double", Double.TYPE);
    primitivesByName.put("float", Float.TYPE);
  }

  /**
   * Get the named type.
   *
   * @param  name                   the name
   * @return                        the return type of the method
   * @throws ClassNotFoundException
   */
  public static Class<?> getClassForName(String name) throws ClassNotFoundException {
    try {
      return Class.forName(name);
    } catch (final ClassNotFoundException e) {
      LOG.trace("Class not known, getting primitive type", e);
      final Class<?> result = primitivesByName.get(name);
      if (result == null) {
        throw new ClassNotFoundException();
      }
      return result;
    } catch (final NullPointerException e) {
      LOG.trace("Primitive type not known, getting primitive type", e);
      throw new ClassNotFoundException();
    }
  }

}
