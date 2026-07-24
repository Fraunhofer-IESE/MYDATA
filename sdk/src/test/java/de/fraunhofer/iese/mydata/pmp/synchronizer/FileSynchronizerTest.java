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

package de.fraunhofer.iese.mydata.pmp.synchronizer;

import de.fraunhofer.iese.mydata.component.information.PepComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PipComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PxpComponentInformation;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyDecisionPoint;
import de.fraunhofer.iese.mydata.exception.ResourceUpdateException;
import de.fraunhofer.iese.mydata.policy.IPolicyService;
import de.fraunhofer.iese.mydata.policy.Policy;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.timer.ITimerService;
import de.fraunhofer.iese.mydata.timer.Timer;
import de.fraunhofer.iese.mydata.timer.TimerId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.Collections;

public class FileSynchronizerTest {

  private IPolicyService policyService;

  private ITimerService timerService;

  private IPolicyDecisionPoint pdp;

  private FileSynchronizer testSubject;

  @BeforeEach
  void init() {
    this.policyService = Mockito.mock(IPolicyService.class);
    this.timerService = Mockito.mock(ITimerService.class);
    this.pdp = Mockito.mock(IPolicyDecisionPoint.class);
    this.testSubject = new FileSynchronizer("target/test-classes/policies", this.timerService,
        this.policyService, this.pdp);
  }

  @Test
  void pushPep_doesNothing() {
    this.testSubject.pushPep(Mockito.mock(PepComponentInformation.class));
    Mockito.verifyNoMoreInteractions(this.policyService);
    Mockito.verifyNoMoreInteractions(this.timerService);
    Mockito.verifyNoMoreInteractions(this.pdp);
  }

  @Test
  void pushPip_doesNothing() {
    this.testSubject.pushPip(Mockito.mock(PipComponentInformation.class));
    Mockito.verifyNoMoreInteractions(this.policyService);
    Mockito.verifyNoMoreInteractions(this.timerService);
    Mockito.verifyNoMoreInteractions(this.pdp);
  }

  @Test
  void pushPxp_doesNothing() {
    this.testSubject.pushPxp(Mockito.mock(PxpComponentInformation.class));
    Mockito.verifyNoMoreInteractions(this.policyService);
    Mockito.verifyNoMoreInteractions(this.timerService);
    Mockito.verifyNoMoreInteractions(this.pdp);
  }

  @Test
  void processCreateEventForPolicy() throws Exception {
    final WatchKey watchKey = Mockito.mock(WatchKey.class);
    // noinspection unchecked
    final WatchEvent<Path> watchEvent = Mockito.mock(WatchEvent.class);
    Mockito.when(watchKey.pollEvents()).thenReturn(Collections.singletonList(watchEvent));
    Mockito.when(watchEvent.kind()).thenReturn(StandardWatchEventKinds.ENTRY_CREATE);
    Mockito.when(watchEvent.context()).thenReturn(Paths.get("urn_policy_test.mdpx"));

    this.testSubject.processChanges(watchKey);

    Mockito.verify(this.policyService).addPolicy(ArgumentMatchers.any(Policy.class));
    Mockito.verify(this.policyService).deployPolicy(ArgumentMatchers.any(PolicyId.class));
  }

  @Test
  void processCreateEventForTimer() throws Exception {
    final WatchKey watchKey = Mockito.mock(WatchKey.class);
    // noinspection unchecked
    final WatchEvent<Path> watchEvent = Mockito.mock(WatchEvent.class);
    Mockito.when(watchKey.pollEvents()).thenReturn(Collections.singletonList(watchEvent));
    Mockito.when(watchEvent.kind()).thenReturn(StandardWatchEventKinds.ENTRY_CREATE);
    Mockito.when(watchEvent.context()).thenReturn(Paths.get("urn_timer_test.mdtx"));

    this.testSubject.processChanges(watchKey);

    Mockito.verify(this.timerService).addTimer(ArgumentMatchers.any(Timer.class));
    Mockito.verify(this.timerService).deployTimer(ArgumentMatchers.any(TimerId.class));
  }

  @Test
  void givenWeCreatedAPolicy_processDeleteEvent() throws Exception {
    {
      final WatchKey watchKey = Mockito.mock(WatchKey.class);
      // noinspection unchecked
      final WatchEvent<Path> watchEvent = Mockito.mock(WatchEvent.class);
      Mockito.when(watchKey.pollEvents()).thenReturn(Collections.singletonList(watchEvent));
      Mockito.when(watchEvent.kind()).thenReturn(StandardWatchEventKinds.ENTRY_CREATE);
      Mockito.when(watchEvent.context()).thenReturn(Paths.get("urn_policy_test.mdpx"));

      this.testSubject.processChanges(watchKey);

      Mockito.verify(this.policyService).addPolicy(ArgumentMatchers.any(Policy.class));
      Mockito.verify(this.policyService).deployPolicy(ArgumentMatchers.any(PolicyId.class));
    }

    {
      final WatchKey watchKey = Mockito.mock(WatchKey.class);
      // noinspection unchecked
      final WatchEvent<Path> watchEvent = Mockito.mock(WatchEvent.class);
      Mockito.when(watchKey.pollEvents()).thenReturn(Collections.singletonList(watchEvent));
      Mockito.when(watchEvent.kind()).thenReturn(StandardWatchEventKinds.ENTRY_DELETE);
      Mockito.when(watchEvent.context()).thenReturn(Paths.get("urn_policy_test.mdpx"));

      this.testSubject.processChanges(watchKey);

      Mockito.verify(this.policyService).revokePolicy(ArgumentMatchers.any(PolicyId.class));
      Mockito.verify(this.policyService).deletePolicy(ArgumentMatchers.any(PolicyId.class));
    }
  }

  @Test
  void givenWeCreatedATimer_processDeleteEvent() throws Exception {
    {
      final WatchKey watchKey = Mockito.mock(WatchKey.class);
      // noinspection unchecked
      final WatchEvent<Path> watchEvent = Mockito.mock(WatchEvent.class);
      Mockito.when(watchKey.pollEvents()).thenReturn(Collections.singletonList(watchEvent));
      Mockito.when(watchEvent.kind()).thenReturn(StandardWatchEventKinds.ENTRY_CREATE);
      Mockito.when(watchEvent.context()).thenReturn(Paths.get("urn_timer_test.mdtx"));

      this.testSubject.processChanges(watchKey);

      Mockito.verify(this.timerService).addTimer(ArgumentMatchers.any(Timer.class));
      Mockito.verify(this.timerService).deployTimer(ArgumentMatchers.any(TimerId.class));
    }

    {
      final WatchKey watchKey = Mockito.mock(WatchKey.class);
      // noinspection unchecked
      final WatchEvent<Path> watchEvent = Mockito.mock(WatchEvent.class);
      Mockito.when(watchKey.pollEvents()).thenReturn(Collections.singletonList(watchEvent));
      Mockito.when(watchEvent.kind()).thenReturn(StandardWatchEventKinds.ENTRY_DELETE);
      Mockito.when(watchEvent.context()).thenReturn(Paths.get("urn_timer_test.mdtx"));

      this.testSubject.processChanges(watchKey);

      Mockito.verify(this.timerService).revokeTimer(ArgumentMatchers.any(TimerId.class));
      Mockito.verify(this.timerService).deleteTimer(ArgumentMatchers.any(TimerId.class));
    }
  }

  @Test
  void givenWeCreatedAPolicy_processModifyEvent() throws Exception {
    {
      final WatchKey watchKey = Mockito.mock(WatchKey.class);
      // noinspection unchecked
      final WatchEvent<Path> watchEvent = Mockito.mock(WatchEvent.class);
      Mockito.when(watchKey.pollEvents()).thenReturn(Collections.singletonList(watchEvent));
      Mockito.when(watchEvent.kind()).thenReturn(StandardWatchEventKinds.ENTRY_CREATE);
      Mockito.when(watchEvent.context()).thenReturn(Paths.get("urn_policy_test.mdpx"));

      this.testSubject.processChanges(watchKey);

      Mockito.verify(this.policyService).addPolicy(ArgumentMatchers.any(Policy.class));
      Mockito.verify(this.policyService).deployPolicy(ArgumentMatchers.any(PolicyId.class));
    }

    {
      final WatchKey watchKey = Mockito.mock(WatchKey.class);
      // noinspection unchecked
      final WatchEvent<Path> watchEvent = Mockito.mock(WatchEvent.class);
      Mockito.when(watchKey.pollEvents()).thenReturn(Collections.singletonList(watchEvent));
      Mockito.when(watchEvent.kind()).thenReturn(StandardWatchEventKinds.ENTRY_MODIFY);
      Mockito.when(watchEvent.context()).thenReturn(Paths.get("urn_policy_test.mdpx"));

      this.testSubject.processChanges(watchKey);

      Mockito.verify(this.policyService).updatePolicy(ArgumentMatchers.any(Policy.class));
    }
  }

  @Test
  void givenWeCreatedATimer_processModifyEvent() throws Exception {
    {
      final WatchKey watchKey = Mockito.mock(WatchKey.class);
      // noinspection unchecked
      final WatchEvent<Path> watchEvent = Mockito.mock(WatchEvent.class);
      Mockito.when(watchKey.pollEvents()).thenReturn(Collections.singletonList(watchEvent));
      Mockito.when(watchEvent.kind()).thenReturn(StandardWatchEventKinds.ENTRY_CREATE);
      Mockito.when(watchEvent.context()).thenReturn(Paths.get("urn_timer_test.mdtx"));

      this.testSubject.processChanges(watchKey);

      Mockito.verify(this.timerService).addTimer(ArgumentMatchers.any(Timer.class));
      Mockito.verify(this.timerService).deployTimer(ArgumentMatchers.any(TimerId.class));
    }

    {
      final WatchKey watchKey = Mockito.mock(WatchKey.class);
      // noinspection unchecked
      final WatchEvent<Path> watchEvent = Mockito.mock(WatchEvent.class);
      Mockito.when(watchKey.pollEvents()).thenReturn(Collections.singletonList(watchEvent));
      Mockito.when(watchEvent.kind()).thenReturn(StandardWatchEventKinds.ENTRY_MODIFY);
      Mockito.when(watchEvent.context()).thenReturn(Paths.get("urn_timer_test.mdtx"));

      this.testSubject.processChanges(watchKey);

      Mockito.verify(this.timerService).updateTimer(ArgumentMatchers.any(Timer.class));
    }
  }

  @Test
  void testInitialSyncOnStart() throws Exception {
    this.testSubject.start();
    Thread.sleep(100); // give some little time to reach the wait condition
    this.testSubject.stop(); // stop waits for file-sync-thread to end
    Mockito.verify(this.policyService).addPolicy(ArgumentMatchers.any(Policy.class));
    Mockito.verify(this.policyService).deployPolicy(ArgumentMatchers.any(PolicyId.class));
    Mockito.verify(this.timerService).addTimer(ArgumentMatchers.any(Timer.class));
    Mockito.verify(this.timerService).deployTimer(ArgumentMatchers.any(TimerId.class));
    Mockito.verify(this.pdp).setFailureMode(false); // initial sync worked
    Mockito.verify(this.pdp).setFailureMode(true); // stop results in failureMode
  }

  @Test
  void whenSyncFails_PdpResultsInFailureMode() throws Exception {
    Mockito.doThrow(ResourceUpdateException.class).when(this.policyService)
        .deployPolicy(ArgumentMatchers.any(PolicyId.class));
    this.testSubject.start();
    this.testSubject.stop();
    Mockito.verify(this.policyService).addPolicy(ArgumentMatchers.any(Policy.class));
    Mockito.verify(this.policyService).deployPolicy(ArgumentMatchers.any(PolicyId.class));
    // no one disables the failure mode, just one confirmation
    Mockito.verify(this.pdp).setFailureMode(true);
    Mockito.verifyNoMoreInteractions(this.pdp);
  }

  @Test
  void onlyXmlFilesWillBeProcessed() throws Exception {
    final WatchKey watchKey = Mockito.mock(WatchKey.class);
    // noinspection unchecked
    final WatchEvent<Path> watchEvent = Mockito.mock(WatchEvent.class);
    Mockito.when(watchKey.pollEvents()).thenReturn(Collections.singletonList(watchEvent));
    Mockito.when(watchEvent.kind()).thenReturn(StandardWatchEventKinds.ENTRY_CREATE);
    Mockito.when(watchEvent.context()).thenReturn(Paths.get("bla.txt"));

    this.testSubject.processChanges(watchKey);
    Mockito.verifyNoMoreInteractions(this.policyService);
    Mockito.verifyNoMoreInteractions(this.pdp);
  }

}
