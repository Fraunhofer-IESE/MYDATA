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

package de.fraunhofer.iese.mydata.pep.common;

import com.google.gson.JsonPrimitive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonUtil {

  private static final Logger LOG = LoggerFactory.getLogger(CommonUtil.class);

  private CommonUtil() {

  }

  /**
   * Only String or JsonPrimitive are allowed, else throws
   * IllegalArgumentException("Only Strings allowed")
   *
   * @param input
   * @return
   */
  public static String getFromStringOrJsonPrimitive(Object input) {
    if (input instanceof String) {
      return (String)input;
    }
    if (input instanceof JsonPrimitive) {
      return ((JsonPrimitive)input).getAsString();
    }
    throw new IllegalArgumentException("Only Strings allowed");
  }

  /**
   * Only String or JsonPrimitive are allowed, else throws
   * IllegalArgumentException("Only Strings allowed")
   *
   * @param input
   * @return
   */
  public static Integer getIntegerFromObject(Object input) {
    if (null == input) {
      return 0;
    }
    if (input instanceof Integer) {
      return (Integer)input;
    }
    try {
      return (int)Math.round(Double.parseDouble((String)input));
    } catch (final Exception e) {
      LOG.info("Can not be cast into double", e);
    }
    return Integer.parseInt(input.toString());
  }

  public static boolean getbooleanFromObject(Object o) {
    try {
      return Boolean.parseBoolean(String.valueOf(o));
    } catch (final Exception e) {
      LOG.info("Can not parse Object to boolean", e);
      return false;
    }
  }

  public static String fixStringObject(Object o) {
    if (null == o) {
      return "";
    }
    return o.toString();
  }

}
