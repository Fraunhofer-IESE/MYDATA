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

import com.google.common.annotations.VisibleForTesting;

import java.time.Instant;

public class ClockProvider {

  private static Clock clock = buildDefaultClock();

  private ClockProvider() {
    // private constructor as suggested for utility classes
  }

  public static Clock getClock() {
    return ClockProvider.clock;
  }

  @VisibleForTesting
  public static void setClock(Clock clock) {
    ClockProvider.clock = clock;
  }

  @VisibleForTesting
  public static void resetClock() {
    ClockProvider.clock = buildDefaultClock();
  }

  private static Clock buildDefaultClock() {
    return new Clock(Instant::now);
  }
}
