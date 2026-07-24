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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.exception.InhibitException;

import io.reactivex.rxjava3.core.Observable;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class MyDataUtilTest {

  @Test
  void checkedBlockingGet_NoException()
      throws InhibitException, EvaluationUndecidableException, IOException {
    final Event event = new Event(new ActionId("urn:action:mydata:my-action"));
    final Observable<Event> observable = Observable.fromCallable(() -> event);
    final Event e = MyDataUtil.checkedBlockingGet(observable);
    assertNotNull(e);
    assertEquals(event, e);
  }

  @Test
  void checkedBlockingGet_Inhibit()
      throws InhibitException, EvaluationUndecidableException, IOException {
    final Observable<Event> observable = Observable.error(new InhibitException("nein"));
    assertThrows(InhibitException.class, () -> {
      MyDataUtil.checkedBlockingGet(observable);
    });
  }

  @Test
  void checkedBlockingGet_EvaluationUndecidable()
      throws InhibitException, EvaluationUndecidableException, IOException {
    final Observable<Event> observable = Observable
        .error(new EvaluationUndecidableException("nein"));
    assertThrows(EvaluationUndecidableException.class, () -> {
      MyDataUtil.checkedBlockingGet(observable);
    });
  }

  @Test
  void checkedBlockingGet_IO()
      throws InhibitException, EvaluationUndecidableException, IOException {
    final Observable<Event> observable = Observable.error(new IOException("nein"));
    assertThrows(IOException.class, () -> {
      MyDataUtil.checkedBlockingGet(observable);
    });
  }

  @Test
  void checkedBlockingGet_SomeRuntimeException()
      throws InhibitException, EvaluationUndecidableException, IOException {
    final Observable<Event> observable = Observable.error(new RuntimeException("nein"));
    assertThrows(RuntimeException.class, () -> {
      MyDataUtil.checkedBlockingGet(observable);
    });
  }
}
