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

package de.fraunhofer.iese.mydata.component.information;

import static org.junit.jupiter.api.Assertions.assertThrows;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;

import java.net.URI;

import org.junit.jupiter.api.Test;
import java.util.Collections;

class BasicManagementServiceComponentInformationTest {

  @Test
  void pmpComponentWithValidComponentId() throws Exception {
    final BasicManagementServiceComponentInformation componentInformation = new BasicManagementServiceComponentInformation(new ComponentId("urn:component:test:pmp:test"), Collections.singletonList(URI.create("http://localhost")));
    MyDataEntity.validateAndNullCheck(componentInformation);
  }

  @Test
  void pmpComponentWithWrongComponentId() throws Exception {
    final BasicManagementServiceComponentInformation componentInformation = new BasicManagementServiceComponentInformation(new ComponentId("urn:component:test:pdp:test"), Collections.singletonList(URI.create("http://localhost")));
    assertThrows(InvalidEntityException.class, () ->
      MyDataEntity.validateAndNullCheck(componentInformation));
  }

}
