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

import de.fraunhofer.iese.mydata.policy.PipRequest;
import de.fraunhofer.iese.mydata.policy.exception.InformationUndeterminableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;

import java.io.IOException;

/**
 * Policy Information Points provide additional information used by PDPs.
 */
public interface IPolicyInformationPoint extends IMyDataComponent {
  /**
   * Evaluates a certain method or condition.
   *
   * @param  request                            Object which contains detailed information (context
   *                                              name and its parameters) about the PIP request
   * @return                                    A DataObject containing the evaluation result and
   *                                            the evaluation result type.
   * @throws IOException                        if communication is failed
   * @throws InformationUndeterminableException if sent PIP request is semantically not processable
   *                                              by PIP
   */
  DataObject<?> evaluate(PipRequest request) throws IOException, InformationUndeterminableException;

}
