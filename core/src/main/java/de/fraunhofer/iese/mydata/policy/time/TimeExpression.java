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

package de.fraunhofer.iese.mydata.policy.time;

import java.util.Map;

/**
 * Container for Time Expression. Assumption: only concrete, negative and positive values have to be
 * stored "*" will not change anything on the Date and will not be stored
 */

public class TimeExpression {

  /**
   * Map for relative expression, which changes a Unit of a Date, i.e. (TimeUtil.YEAR, "-1")
   */
  Map<String, Integer> relativeChangeMap;

  /**
   * Map for concrete expression of an Unit, i.e. (TimeUtil.MONTH, "1");
   */
  Map<String, Integer> concreteMap;

  public Map<String, Integer> getConcreteMap() {
    return this.concreteMap;
  }

  public void setConcreteMap(Map<String, Integer> concreteMap) {
    this.concreteMap = concreteMap;
  }

  public Map<String, Integer> getRelativeChangeMap() {
    return this.relativeChangeMap;
  }

  public void setRelativeChangeMap(Map<String, Integer> relativeChangeMap) {
    this.relativeChangeMap = relativeChangeMap;
  }

  public boolean hasConcreteValues() {
    return (null != this.concreteMap && !this.concreteMap.isEmpty());
  }

  public boolean hasReletiveChangeValues() {
    return (null != this.relativeChangeMap && !this.relativeChangeMap.isEmpty());
  }
}
