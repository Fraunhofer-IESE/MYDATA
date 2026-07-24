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
import de.fraunhofer.iese.mydata.component.information.PdpComponentInformation;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyDecisionPoint;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.ArrayList;

/**
 * Unit test for simple App.
 */
class ConnectorsPdpTest {
  private static final String PDP_COMPONENT_ID = "urn:component:mydata:pdp:123";

  private final ConnectorFactory cf = new ConnectorFactory();

  @Test
  void httpPdpConnector() {
    final IPolicyDecisionPoint pdp = this.cf.getPdp(URI.create("https://test.de/pdp"));
    assertInstanceOf(TestPdpHttpConnector.class, pdp);
  }

  @Test
  void httpPdpConnectorViaGetComponent() throws Exception {
    final PdpComponentInformation comp = new PdpComponentInformation(
        new ComponentId(PDP_COMPONENT_ID), new ArrayList<URI>());
    comp.addUrl(URI.create("http://test.de/pdp"));
    comp.addUrl(URI.create("rmi://test.de/pdp"));

    final IPolicyDecisionPoint pdp = this.cf.getPdp(comp, "http", null, null);
    assertInstanceOf(TestPdpHttpConnector.class, pdp);
  }

  @Test
  void rmiPdpConnector() {
    final IPolicyDecisionPoint pdp = this.cf.getPdp(URI.create("rmi://test.de/pdp"));
    assertInstanceOf(TestPdpRmiConnector.class, pdp);
  }

  @Test
  void pdpComponentConnectorOne() throws Exception {
    final PdpComponentInformation comp = new PdpComponentInformation(
        new ComponentId(PDP_COMPONENT_ID), new ArrayList<URI>());
    comp.addUrl(URI.create("http://test.de/pdp"));

    final IPolicyDecisionPoint pdp = this.cf.getPdp(comp, null, null);
    assertInstanceOf(TestPdpHttpConnector.class, pdp);
  }

  @Test
  void pdpComponentConnectorMulti() throws Exception {
    final PdpComponentInformation comp = new PdpComponentInformation(
        new ComponentId(PDP_COMPONENT_ID), new ArrayList<URI>());
    comp.addUrl(URI.create("http://test.de/pdp"));
    comp.addUrl(URI.create("rmi://test.de/pdp"));

    final IPolicyDecisionPoint pdp = this.cf.getPdp(comp, null, null);
    assertInstanceOf(TestPdpRmiConnector.class, pdp);
  }

  @Test
  void pdpComponentConnectorMultiSelectPreferred() throws Exception {
    final PdpComponentInformation comp = new PdpComponentInformation(
        new ComponentId(PDP_COMPONENT_ID), new ArrayList<URI>());
    comp.addUrl(URI.create("http://test.de/pdp"));
    comp.addUrl(URI.create("rmi://test.de/pdp"));

    final IPolicyDecisionPoint pdp = this.cf.getPdp(comp, "http", null);
    assertInstanceOf(TestPdpHttpConnector.class, pdp);
  }

  @Test
  void pdpComponentConnectorUnknown() throws Exception {
    final PdpComponentInformation comp = new PdpComponentInformation(
        new ComponentId(PDP_COMPONENT_ID), new ArrayList<URI>());
    comp.addUrl(URI.create("tcp://test.de/pdp"));

    final IPolicyDecisionPoint pdp = this.cf.getPdp(comp, null, null);
    assertNull(pdp);
  }

}
