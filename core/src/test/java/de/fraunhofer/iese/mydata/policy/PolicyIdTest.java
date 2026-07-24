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

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The Class TestPolicyId.
 */
class PolicyIdTest {

  /**
   * Test constructor valid uri.
   * 
   * @throws InvalidEntityException
   */
  @Test
  void constructorValidUri() throws Exception {
    final PolicyId p = new PolicyId("urn:policy:456:789");
    MyDataEntity.validateAndNullCheck(p);
  }

  /**
   * Test policy component_id.
   *
   * @throws IOException              Signals that an I/O exception has occurred.
   * @throws URISyntaxException       the URI syntax exception
   * @throws IllegalArgumentException
   * @throws InvalidEntityException
   */
  @Test
  void policyId()
      throws Exception {
    final String policy = this.readResourceFile("policyOk.xml");
    final Policy p = new Policy(policy);
    assertEquals("urn:policy:test:foo", p.getPolicyId().getUrn());
  }

  /**
   * Read resource file.
   *
   * @param  file               the file
   * @return                    the string
   * @throws IOException        Signals that an I/O exception has occurred.
   * @throws URISyntaxException the URI syntax exception
   */
  private String readResourceFile(String file) throws IOException, URISyntaxException {
    return new String(
        Files.readAllBytes(Paths.get(this.getClass().getClassLoader().getResource(file).toURI())));
  }
}
