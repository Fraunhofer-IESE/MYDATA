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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.fraunhofer.iese.mydata.testmodel.User;

import org.junit.jupiter.api.Test;

class InputParameterDescriptionTest {

  @Test
  void constructortest() {
    final InputParameterDescription description = new InputParameterDescription("mytask",
        "this is my task", true, User.class);
    assertEquals("mytask", description.getName());
    assertEquals("this is my task", description.getDescription());
    assertNotNull(description.getTypeDescription());
    assertEquals(description.getTypeDescription().getTypeName(), User.class.getCanonicalName());
  }
}
