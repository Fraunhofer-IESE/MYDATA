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

package de.fraunhofer.iese.mydata.policy.exception;

import java.util.Arrays;

/**
 * Indicates a policy is conflicted with another policy.
 */
public class ConflictingPolicyException extends Exception {

  /**
   * The Constant serialVersionUID.
   */
  private static final long serialVersionUID = 4357288472498187758L;

  /**
   * Instantiates a new conflicting policy exception.
   *
   * @param base                {@link String} of the policy which is in conflict with
   *                              conclictingPolicies.
   * @param conclictingPolicies the array of policyId of other conclictingPolicies.
   */
  public ConflictingPolicyException(String base, String... conclictingPolicies) {
    super(base + " conflicts with " + Arrays.toString(conclictingPolicies));
  }
}
