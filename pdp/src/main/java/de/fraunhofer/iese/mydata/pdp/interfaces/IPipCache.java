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

package de.fraunhofer.iese.mydata.pdp.interfaces;

import de.fraunhofer.iese.mydata.component.information.method.MethodInterfaceDescription;
import de.fraunhofer.iese.mydata.policy.PipRequest;
import de.fraunhofer.iese.mydata.policy.exception.InformationUndeterminableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import java.io.IOException;

/**
 * Interface that defines PIP cache functionality.
 */
public interface IPipCache {
  /**
   * Evaluates a PIP and returns the evaluation result as a {@link DataObject}.
   * If the result has already been cached, the cached value will be returned.
   * <p>
   * A value is retrieved from cache if the pipQuery is identical and the ttl is
   * not reached.
   * </p>
   *
   * @param ttl time to live for the PIP result
   * @param pipQuery The {link InterfaceDescription} for the needed PIP.
   * @param pipRequest The {link PipRequest} object that contains method,
   *          parameters and default answer.
   * @param solutionId Scope of the evaluation.
   * @return NULL in case of an error (e.g. connection error, evaluation error)
   *         or the evaluation result in case of success.
   * @throws IOException In case of an I/O exception.
   * @throws InformationUndeterminableException In case an evaluation is not
   *           possible.
   */
  DataObject<?> getEvalResultFromCache(final long ttl, final MethodInterfaceDescription pipQuery, final PipRequest pipRequest, SolutionId solutionId)
      throws IOException, InformationUndeterminableException;

  /**
   * Clears the cache.
   */
  void clearCache();
}
