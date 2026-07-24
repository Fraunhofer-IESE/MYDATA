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

package de.fraunhofer.iese.mydata.testmodel;

import java.util.Map;

/**
 * Created by chattapa on 12/19/16.
 */
public class ClassWithMapAsHas {

  private Map<String, Task> stringTaskMap;

  private ClassWithMap classWithMap;

  public ClassWithMap getClassWithMap() {
    return classWithMap;
  }

  public void setClassWithMap(ClassWithMap classWithMap) {
    this.classWithMap = classWithMap;
  }

  public Map<String, Task> getStringTaskMap() {
    return stringTaskMap;
  }

  public void setStringTaskMap(Map<String, Task> stringTaskMap) {
    this.stringTaskMap = stringTaskMap;
  }
}
