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

package de.fraunhofer.iese.mydata.reactive.common;

import de.fraunhofer.iese.mydata.component.interfaces.IPolicyEnforcementPoint;

import io.reactivex.rxjava3.core.Observable;

/**
 * Reactive Pep interface
 *
 * @param <T> the generic type
 */

public interface RxPep<T> {

  /***
   * @return the generic type
   */
  T createInstanceAPI();

  /***
   * Registers the Pep at PMP.
   *
   * @return true if Pep is registered at PMP and returns false if Pep had been registered before or
   *         registration is failed.
   */
  Observable<Boolean> doRegisterAtPMP();

  /***
   * It returns a valid instance of {@link IPolicyEnforcementPoint}
   *
   * @return {@link IPolicyEnforcementPoint}
   */
  IPolicyEnforcementPoint getPolicyEnforcementPoint();

  /**
   * Returns one of the following RxPep states:
   * <ul>
   * <li>REGISTRATION_DONE_SUCCESSFULLY: Pep is already registered at PMP.</li>
   * <li>REGISTRATION_FAILED: Pep failed to register at PMP.</li>
   * <li>REGISTRATION_NOT_STARTED: Registration has not been started yet and Pep needs to be
   * initialized for the registration to be started.</li>
   * <li>REGISTRATION_UNDER_PROCESS: Registration is being processed.</li>
   * </ul>
   *
   * @return the current RxPep state
   */
  RxPepState getState();
}
