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

package de.fraunhofer.iese.mydata.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.fraunhofer.iese.mydata.policy.event.history.ValueChangeEntity;

import org.junit.jupiter.api.Test;

class ValueChangeEntityTest {

  @Test
  void constructors() {
    final Policy policy = new Policy();
    final String blockHash = "blockHash";
    final String blockId = "blockId";
    final String value = "value";

    final ValueChangeEntity vce = new ValueChangeEntity(policy, blockHash, blockId, value);
    assertEquals(blockHash, vce.getBlockHash());
    assertEquals(blockId, vce.getBlockId());
    assertEquals(value, vce.getValue());
    assertEquals(policy, vce.getPolicy());

    final ValueChangeEntity vce2 = new ValueChangeEntity(new Policy(), "", "", "");
    vce2.setBlockHash(blockHash);
    vce2.setBlockId(blockId);
    vce2.setValue(value);

    assertEquals(vce.getBlockHash(), vce2.getBlockHash());
    assertEquals(vce.getBlockId(), vce2.getBlockId());
    assertEquals(vce.getValue(), vce2.getValue());
  }
}
