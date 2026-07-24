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

package de.fraunhofer.iese.mydata.component.connector;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.information.PipComponentInformation;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyInformationPoint;

import org.junit.jupiter.api.Test;

import java.net.URI;

/**
 * Unit test for simple App.
 */
class ConnectorsPipTest {
  private static final String PIP_COMPONENT_ID = "urn:component:mydata:pip:123";

  private final ConnectorFactory cf = new ConnectorFactory();

  @Test
  void httpPipConnector() {
    final IPolicyInformationPoint pip = this.cf.getPip(URI.create("https://test.de/pip"));
    assertInstanceOf(TestPipHttpConnector.class, pip);
  }

  @Test
  void httpPipConnectorViaGetComponent() throws Exception {
    final PipComponentInformation comp = new PipComponentInformation(
        new ComponentId(PIP_COMPONENT_ID));
    comp.addUrl(URI.create("http://test.de/pip"));
    comp.addUrl(URI.create("rmi://test.de/pip"));

    final IPolicyInformationPoint pip = this.cf.getPip(comp, "http");
    assertInstanceOf(TestPipHttpConnector.class, pip);
  }

  @Test
  void rmiPipConnector() {
    final IPolicyInformationPoint pip = this.cf.getPip(URI.create("rmi://test.de/pip"));
    assertInstanceOf(TestPipRMIConnector.class, pip);
  }

  @Test
  void pipComponentConnectorOne() throws Exception {
    final PipComponentInformation comp = new PipComponentInformation(
        new ComponentId(PIP_COMPONENT_ID));
    comp.addUrl(URI.create("http://test.de/pip"));

    final IPolicyInformationPoint pip = this.cf.getPip(comp, null);
    assertInstanceOf(TestPipHttpConnector.class, pip);
  }

  @Test
  void pipComponentConnectorMulti() throws Exception {
    final PipComponentInformation comp = new PipComponentInformation(
        new ComponentId(PIP_COMPONENT_ID));
    comp.addUrl(URI.create("http://test.de/pip"));
    comp.addUrl(URI.create("rmi://test.de/pip"));

    final IPolicyInformationPoint pip = this.cf.getPip(comp, null);
    assertInstanceOf(TestPipRMIConnector.class, pip);
  }

  @Test
  void whenConnectorCannotBeInstantiated_NullShouldBeReturned() {
    final IPolicyInformationPoint pip = this.cf.getPip(URI.create("test://test.de/pip"));
    assertNull(pip);
  }

}
