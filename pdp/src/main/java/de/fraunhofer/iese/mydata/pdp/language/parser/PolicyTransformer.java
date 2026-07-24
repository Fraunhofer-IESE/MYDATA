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

package de.fraunhofer.iese.mydata.pdp.language.parser;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.policy.PolicyDeployableGroup;

import jakarta.validation.groups.Default;

public class PolicyTransformer {

  public de.fraunhofer.iese.mydata.pdp.language.model.Policy fromCoreToPdp(
      de.fraunhofer.iese.mydata.policy.Policy corePolicy)
      throws InvalidEntityException, TransformationException {
    MyDataEntity.validateAndNullCheck(corePolicy, PolicyDeployableGroup.class, Default.class);
    final IPolicyParser policyParser = new PolicyXmlParser();
    try {
      return policyParser.parsePolicyText(corePolicy.getContent());
    } catch (final IllegalArgumentException e) {
      throw new TransformationException(e.getMessage(), e);
    }
  }

  public static class TransformationException extends Exception {

    private static final long serialVersionUID = 7537426175790090537L;

    public TransformationException(String message) {
      super(message);
    }

    public TransformationException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
