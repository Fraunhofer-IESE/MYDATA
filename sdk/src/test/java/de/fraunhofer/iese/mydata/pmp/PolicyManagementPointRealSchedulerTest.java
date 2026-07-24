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

package de.fraunhofer.iese.mydata.pmp;

import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.connector.ConnectorFactory;
import de.fraunhofer.iese.mydata.component.information.PdpComponentInformation;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyDecisionPoint;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.timer.Timer;
import de.fraunhofer.iese.mydata.timer.TimerId;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;

import java.time.ZoneId;
import java.util.Set;

public class PolicyManagementPointRealSchedulerTest {

  private PolicyManagementPoint pmp;

  private IPolicyDecisionPoint pdp;

  private Scheduler scheduler;

  private ConnectorFactory connectorFactory;

  @BeforeEach
  public void setup() throws Exception {
    final PdpComponentInformation pdpComponentInformation = Mockito.mock(PdpComponentInformation.class);
    Mockito.when(pdpComponentInformation.getComponentId())
        .thenReturn(new ComponentId("urn:component:test:pdp:pdp"));
    this.pdp = Mockito.mock(IPolicyDecisionPoint.class);
    Mockito.when(this.pdp.getId()).thenReturn(new ComponentId("urn:component:test:pdp:pdp"));
    final Scheduler s = StdSchedulerFactory.getDefaultScheduler();
    this.scheduler = Mockito.spy(s);
    this.connectorFactory = Mockito.mock(ConnectorFactory.class);
    this.pmp = new PolicyManagementPoint(new ComponentId("urn:component:test:pmp:pmp"),
        pdpComponentInformation, this.pdp, ZoneId.of("Europe/Berlin"), this.scheduler,
        this.connectorFactory);
  }

  @AfterEach
  public void shutdown() throws Exception {
    this.scheduler.shutdown();
  }

  // TODO can we improve test execution speed?
  @Test
  public void testDeployTimer_revokeTimer_schedulerIsCalled() throws Exception {
    final Timer t = new Timer("<timer  xmlns='http://www.mydata-control.de/4.0/mydataLanguageTimer'\n"
        + " xmlns:tns='http://www.mydata-control.de/4.0/mydataLanguageTimer'\n"
        + " xmlns:parameter='http://www.mydata-control.de/4.0/parameter'\n"
        + " xmlns:pip='http://www.mydata-control.de/4.0/pip'\n"
        + " xmlns:event='http://www.mydata-control.de/4.0/event' \n"
        + " xmlns:constant='http://www.mydata-control.de/4.0/constant' \n"
        + " xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'\n"
        + " cron=\"*/1 * * * * ?\" id=\"urn:timer:test:bla\" description=\"Its my description.\">\n"
        + "\n"
        + "    <event action=\"urn:action:test:writeData\">\n"
        + "        <parameter:string name=\"blubb\" value=\"xc\"/>\n"
        + "        <parameter:number name=\"blubb\" value=\"1\"/>\n"
        + "        <parameter:boolean name=\"blubb\" value=\"true\"/>\n"
        + "        <parameter:object name=\"blubb\" value=\"xc\"/>\n"
        + "    </event>\n"
        + "\n"
        + "    <event action=\"urn:action:test:writeData\">\n"
        + "        <parameter:string name=\"blubb\" value=\"xc\"/>\n"
        + "        <parameter:number name=\"blubb\" value=\"1\"/>\n"
        + "        <parameter:boolean name=\"blubb\" value=\"false\"/>\n"
        + "        <parameter:object name=\"blubb\" value=\"xc\"/>\n"
        + "    </event>\n"
        + "\n"
        + "</timer>");
    final Timer timerSpy = Mockito.spy(t);

    final TimerId id = this.pmp.addTimer(timerSpy);
    this.pmp.deployTimer(id);
    Mockito.verify(this.scheduler).scheduleJob(ArgumentMatchers.any(), ArgumentMatchers.any()); // job
    // is
    // scheduled
    Thread.sleep(200); // have some time to be executed, immediate fire?
    this.pmp.revokeTimer(id); // revoke timer
    Mockito.verify(this.scheduler).deleteJob(ArgumentMatchers.any()); // unscheduled
    Mockito.verify(this.pdp, Mockito.atLeastOnce()).evaluate(ArgumentMatchers.<Set<Event>> any()); // there
    // were
    // executions
    Thread.sleep(1200); // wait some time, maybe the job is triggered again
    Mockito.verify(this.pdp, Mockito.atMost(200)).getId(); // clean up for
    // verification in the
    // next line
    // make sure that it is not
    Mockito.verifyNoMoreInteractions(this.pdp);
  }
}
