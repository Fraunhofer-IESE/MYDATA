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

package de.fraunhofer.iese.mydata.pmp.cache;

import de.fraunhofer.iese.mydata.timer.Timer;

import java.util.Optional;
import java.util.Set;

public interface ITimerCache {

  /**
   * @return the cached timers if cache is valid or else empty Optional
   */
  Optional<Set<Timer>> getTimers();

  /**
   * @return whether the cache is valid
   */
  boolean isValid();

  /**
   * invalidates the cache
   */
  void invalidate();

  /**
   * refreshes validation time of the cache without changing its contents
   */
  void validate();

  /**
   * updates cache content and refreshes validation time
   *
   * @param timers
   * @return whether updating the cache succeeded
   */
  boolean updateCache(Set<Timer> timers);

}
