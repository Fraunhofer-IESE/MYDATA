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

package de.fraunhofer.iese.mydata.internal;

import de.fraunhofer.iese.mydata.IMyDataEnvironment;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyDecisionPoint;

import java.util.Optional;

/**
 * This interface covers the "Technical Access" to a MyDataEnvironment. Some
 * parts of the MyDataEnvironment are only for internal use to provide the
 * functionalities of the MYDATA Library. This interface (respective its
 * methods) should not be used by Library-Users.
 */
public interface IMyDataEnvironmentTechnicalAccess {
  /**
   * Access to the {@link IPolicyDecisionPoint} associated with this
   * {@link IMyDataEnvironment}
   *
   * @return the {@link IPolicyDecisionPoint} associated with this
   *         {@link IMyDataEnvironment} if available
   */
  Optional<IPolicyDecisionPoint> getPdp();

  /**
   * Triggers internal cleanup. The {@link IMyDataEnvironment} must not be used
   * after calling this method.
   */
  void destroy();
}
