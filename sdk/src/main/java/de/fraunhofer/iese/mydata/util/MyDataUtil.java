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

package de.fraunhofer.iese.mydata.util;

import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.exception.InhibitException;

import io.reactivex.rxjava3.core.Observable;

import java.io.IOException;

/**
 * This class provides helpful tools.
 */
public class MyDataUtil {

  private MyDataUtil() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * This helper method supports you in retrieving the enforced Event from a reactive PEP with
   * checked exceptions.
   * 
   * <pre>
   *   <code>
   *     final Event enforcedEvent = MyDataUtil.checkedBlockingGet(myPep.enforceUser(u));
   *   </code>
   * </pre>
   *
   * @param  eventObservable                the {@code Observable<Event>} we are interested in
   * @return                                the enforced Event
   * @throws EvaluationUndecidableException if PDP can't decide.
   * @throws InhibitException               if event is not allowed
   * @throws IOException                    if connection to PDP is not established
   */
  public static Event checkedBlockingGet(final Observable<Event> eventObservable)
      throws InhibitException, EvaluationUndecidableException, IOException {
    try {
      return eventObservable.blockingFirst();
    } catch (final RuntimeException e) {
      final Throwable t = e.getCause();
      if (t instanceof InhibitException) {
        throw (InhibitException) t;
      } else if (t instanceof EvaluationUndecidableException) {
        throw (EvaluationUndecidableException) t;
      } else if (t instanceof IOException) {
        throw (IOException) t;
      } else {
        throw e;
      }
    }
  }
}
