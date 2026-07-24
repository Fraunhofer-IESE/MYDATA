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

package de.fraunhofer.iese.mydata.pdp.language.policy;

import static org.junit.jupiter.api.Assertions.assertThrows;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.pdp.language.parser.PolicyTransformer;
import de.fraunhofer.iese.mydata.policy.Policy;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;


@SuppressWarnings("javadoc")
class PolicyTransformer_ValidationRelatedTest {

  /**
   * Read resource file.
   *
   * @param file the file
   * @return the string
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws URISyntaxException the URI syntax exception
   */
  private String readResourceFile(String file) throws IOException, URISyntaxException {
    return new String(Files.readAllBytes(Paths.get(this.getClass().getClassLoader().getResource(file).toURI())));
  }

  @Test
  void validPolicyWithPip() throws Exception {
    final Policy p = new Policy(readResourceFile("simple_policy_with_pip.xml"));
    MyDataEntity.validateAndNullCheck(p);
    final PolicyTransformer policyTransformer = new PolicyTransformer();
    final de.fraunhofer.iese.mydata.pdp.language.model.Policy policy = policyTransformer.fromCoreToPdp(p);

  }

  @Test
  void invalidPolicyWithPipSolutionMismatch() throws Exception {
    final Policy p = new Policy(readResourceFile("simple_policy_with_pip_solution_mismatch.xml"));
    final PolicyTransformer policyTransformer = new PolicyTransformer();
    assertThrows(InvalidEntityException.class, () -> {
      final de.fraunhofer.iese.mydata.pdp.language.model.Policy policy = policyTransformer.fromCoreToPdp(p);
    });
  }

  @Test
  void invalidPolicyWithPipMethodInvalid() throws Exception {
    final Policy p = new Policy(readResourceFile("simple_policy_with_pip_method_invalid.xml"));
    final PolicyTransformer policyTransformer = new PolicyTransformer();
    assertThrows(InvalidEntityException.class, () -> {
      final de.fraunhofer.iese.mydata.pdp.language.model.Policy policy = policyTransformer.fromCoreToPdp(p);
    });
  }

  @Test
  void invalidPolicyWithEventOccurenceEventFromOtherSolution() throws Exception {
    final Policy p = new Policy(readResourceFile("policy_with_eventoccurence_other_solution.xml"));
    final PolicyTransformer policyTransformer = new PolicyTransformer();
    assertThrows(InvalidEntityException.class, () -> {
      final de.fraunhofer.iese.mydata.pdp.language.model.Policy policy = policyTransformer.fromCoreToPdp(p);
    });
  }

  @Test
  void invalidPolicyWithEventOccurenceWhenEventFromOtherSolution() throws Exception {
    final Policy p = new Policy(readResourceFile("policy_with_eventoccurence_other_solution_when.xml"));
    final PolicyTransformer policyTransformer = new PolicyTransformer();
    assertThrows(InvalidEntityException.class, () -> {
      final de.fraunhofer.iese.mydata.pdp.language.model.Policy policy = policyTransformer.fromCoreToPdp(p);
    });
  }

  // TODO add test for pxp

}
