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

package de.fraunhofer.iese.mydata.pep.common;

import de.fraunhofer.iese.mydata.pep.modifiers.basic.DeleteModifierMethod;
import de.fraunhofer.iese.mydata.policy.decision.AuthorizationDecision;
import de.fraunhofer.iese.mydata.policy.exception.InhibitException;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;

/**
 * It's any implementation is responsible for
 * enforcing @{@link AuthorizationDecision} to relevant parameter
 * of @{@link ParameterList} using given registered @{@link ModifierMethod}
 */
public interface DecisionEnforcer {

  /**
   * To add @{@link ModifierMethod} against
   * registered @{@link de.fraunhofer.iese.mydata.policy.decision.ModifierEngine}
   * i.e @{@link DeleteModifierMethod}
   *
   * @param modifierMethod Adds an actor.
   * @return true if adding was possible.
   */
  boolean addModificationMethod(ModifierMethod modifierMethod);

  /**
   * To enforce @{@link AuthorizationDecision} to @{@link ParameterList}
   *
   * @param authorizationDecision decision by PDP.
   * @param parameterList Parameter list send via PDP.
   * @throws InhibitException If the modification cannot be performed.
   * @return {@link ParameterList} of parameters to be used.
   */
  ParameterList enforce(AuthorizationDecision authorizationDecision, ParameterList parameterList) throws InhibitException;

  /**
   * To removed already added @{@link ModifierMethod}
   *
   * @param modificationMethodDisplayName Name fo the actor to remove.
   * @return true if removing was possible.
   */
  boolean removeModificationMethod(String modificationMethodDisplayName);

}
