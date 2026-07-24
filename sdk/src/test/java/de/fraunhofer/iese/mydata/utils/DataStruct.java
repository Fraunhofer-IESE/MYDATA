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

package de.fraunhofer.iese.mydata.utils;

public class DataStruct extends Object {
  public String string = "a_string";
  public String[] stringList = new String[]{"a_string", "other_string", "almost_not_a_string"};
  
  public Integer natural = 5;
  public Double real = 5.5;
  public Integer[] naturalList = new Integer[]{-5, 0, 5};
  public Double[] realList = new Double[]{-5.5, 0.0, 5.5};
  public Number[] mixedList = new Number[]{-5.5, -5, -0.0, 0, 5, 5.5};
  
  public DataStruct other;
  
  public void numberFix() {
      mixedList[0] = mixedList[0].doubleValue();
      mixedList[1] = mixedList[1].intValue();
      mixedList[2] = mixedList[2].doubleValue();
      mixedList[3] = mixedList[3].intValue();
      mixedList[4] = mixedList[4].intValue();
      mixedList[5] = mixedList[5].doubleValue();
  }
  
}
