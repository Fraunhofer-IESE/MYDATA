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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

class PolicyTest {

  @Test
  void newPolicy() throws Exception {
    final Policy policy = new Policy(this.readResourceFile("policyOk.xml"));

    assertEquals("basic policy for the tests", policy.getDescription());
    assertTrue(policy.isLanguageValid());
    assertTrue(policy.isXmlValid());
  }

  @Test
  void whenEqualThenEqualsAndHashCodeOk() throws Exception {
    // read policy 1
    final Policy policy1 = new Policy(this.readResourceFile("policyOk.xml"));

    // read policy 2
    final Policy policy2 = new Policy(this.readResourceFile("policyOk.xml"));

    assertEquals(policy1, policy2);
    assertEquals(policy1.hashCode(), policy2.hashCode());
  }

  @Test
  void whenSamePolicyObjectThenEqualsAndHashCodeOk() throws Exception {
    // read policy 
    final Policy policy = new Policy(this.readResourceFile("policyOk.xml"));

    assertEquals(policy, policy);
    assertEquals(policy.hashCode(), policy.hashCode());
  }

  @Test
  void whenNotAPolicyObjectThenEqualsAndHashCodeFail()
      throws Exception {
    final Policy policy = new Policy(this.readResourceFile("policyOk.xml"));
    final PolicyId policyId = new PolicyId("urn:policy:test:foo");

    assertNotEquals(policy, policyId);
    assertNotSame(policy.hashCode(), policyId.hashCode());
  }

  @Test
  void whenPolicyIdsAreDifferentThenEqualsAndHashCodeFail()
      throws Exception {
    // read policy 1
    final Policy policy1 = new Policy(this.readResourceFile("policyOk.xml"));

    // read policy 2
    final Policy policy2 = new Policy(this.readResourceFile("simple_policy_with_pip.xml"));

    assertNotEquals(policy1, policy2);
    assertNotSame(policy1.hashCode(), policy2.hashCode());
  }

  @Test
  void translateToLatestLanguageVersion() throws Exception {
    // read a policy with language version 3.2
    final Policy policy = new Policy(this.readResourceFile("policyOk_3_2.xml"));

    // check if it is translated to 4.0
    final String policyContent = policy.getContent();
    assertTrue(policyContent.contains("http://www.mydata-control.de/4.0"));
  }

  private String readResourceFile(String file) throws IOException, URISyntaxException {
    return new String(
        Files.readAllBytes(Paths.get(this.getClass().getClassLoader().getResource(file).toURI())));
  }
}
