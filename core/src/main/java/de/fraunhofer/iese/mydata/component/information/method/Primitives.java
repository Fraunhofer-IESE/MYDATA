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

import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper class for primitive data types.
 */
public class Primitives {
  /**
   * Map for fetching wrapper-classes by their primitives.
   */
  private static final Map<Class<?>, Class<?>> wrappersByPrimitives = new HashMap<>();

  static {
    wrappersByPrimitives.put(Byte.TYPE, Byte.class);
    wrappersByPrimitives.put(Boolean.TYPE, Boolean.class);
    wrappersByPrimitives.put(Character.TYPE, Character.class);
    wrappersByPrimitives.put(Short.TYPE, Short.class);
    wrappersByPrimitives.put(Integer.TYPE, Integer.class);
    wrappersByPrimitives.put(Long.TYPE, Long.class);
    wrappersByPrimitives.put(Float.TYPE, Float.class);
    wrappersByPrimitives.put(Double.TYPE, Double.class);
  }


  /**
   * The constructor not to create instance.
   */
  private Primitives() {
  }

  /**
   * Replaces class by it's wrapper, when it's primitive. Otherwise the class
   * itself is returned.
   *
   * @param c class
   * @return Class
   */
  public static Class<?> replaceByWrapper(Class<?> c) {
    if (null == c || !c.isPrimitive()) {
      return c;
    }
    return wrappersByPrimitives.get(c);
  }


}
