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

package de.fraunhofer.iese.mydata.component.interfaces;

import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.policy.decision.AuthorizationDecision;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.exception.InhibitException;

import java.io.IOException;

/**
 * Policy Enforcement Points intercept system events and communicate with the PDP to get a
 * corresponding decision.
 */
public interface IPolicyEnforcementPoint extends IMyDataComponent {

  /**
   * * Enforces PDP decision to the event if any PDP subscribes to ActionID of the event.
   *
   * @param  event                          to be sent to PDP and AuthorizationDecision to be
   *                                          enforced
   * @throws InhibitException               if event is not allowed by PDP
   * @throws EvaluationUndecidableException is thrown by PDP if it was not possible to evaluate
   * @throws IOException                    if there is a interruption in communication with PDP
   */
  void enforce(Event event) throws InhibitException, EvaluationUndecidableException, IOException;

  /***
   * It enforces the authorization decision to the event.
   *
   * @param  event            on what decision to be enforced
   * @param  decision         which is to be enforced
   * @throws InhibitException if event is not allowed by PDP
   */
  void enforceDecision(Event event, AuthorizationDecision decision) throws InhibitException;

  /**
   * * It sends the event to subscribed PDP and returns AuthorizationDecision of PDP.
   *
   * @param  event                          to be sent to subscribed PDP
   * @return                                AuthorizationDecision which is returned by subscribed
   *                                        PDP
   * @throws EvaluationUndecidableException is thrown by PDP if it was not possible to evaluate
   * @throws IOException                    if there is a interruption in communication with PDP
   */
  AuthorizationDecision getDecision(Event event) throws EvaluationUndecidableException, IOException;

  boolean initialize() throws IOException, NoSuchEntityException; // TODO method needed in interface?

}
