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

package de.fraunhofer.iese.mydata.eventhistory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.fraunhofer.iese.mydata.MyDataEnvironmentManager;
import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.ResourceUpdateException;
import de.fraunhofer.iese.mydata.pdp.PolicyDecisionPoint;
import de.fraunhofer.iese.mydata.policy.Policy;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.event.history.IEventRepository;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;
import de.fraunhofer.iese.mydata.policy.time.TimeUtil;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@EnableEventHistory
@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
@EnableAutoConfiguration
@ActiveProfiles("test")
public class EventOccurrenceVariableTest {

  public static ZonedDateTime zdt = TimeUtil.getZonedDateTime("06.04.2018 12:10:10",
      ZoneId.of("Europe/Berlin"));

  public static ParameterList pl = new ParameterList();

  public static String block_id = "block_id";

  public static PolicyId pId;

  private PolicyDecisionPoint pdpInstance;

  @Autowired
  private IEventRepository eventRepository;

  /**
   * The logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(EventOccurrenceVariableTest.class);

  @BeforeClass
  public static void testSetup() {
    MyDataEnvironmentManager.enableOverwritingOfExistingMyDataEnvironments();
  }

  @AfterClass
  public static void testTeardown() {
    MyDataEnvironmentManager.disableOverwritingOfExistingMyDataEnvironments();
  }

  @Before
  public void setup() throws IOException, InvalidEntityException {
    pId = new PolicyId("urn:policy:test:bla");

    this.pdpInstance = PolicyDecisionPoint.getInstance();
    this.pdpInstance.reset();
    PolicyDecisionPoint.getInstance().getConnectorCache().setOAuthCredentials("PDP", "secret123",
        "http://localhost:8081/oauth/token");

    this.pdpInstance.initialize(new ComponentId("urn:component:test:pdp:decisionservice"),
        URI.create("http://localhost:8080/ws/pmp"), 4, false, this.eventRepository);

  }

  @Test
  public void useEventWithVariable() throws IOException, URISyntaxException,
      EvaluationUndecidableException, ResourceUpdateException {

    this.pdpInstance.revokePolicy(pId);
    final Policy policy = new Policy(this.readResourceFile("policyEventVariable.xml"));
    assertEquals(pId, policy.getPolicyId());
    final boolean deployed = this.pdpInstance.deploy(policy, ZoneId.of("Europe/Berlin"));
    assertTrue(deployed);
    final Event e = new Event(new ActionId("urn:action:test:read"));
    e.addParameter(new Parameter<>("DataSetUuid", "ABC-123"));
    e.addParameter(new Parameter<>("ConnectionUuid", "CONN-123"));
    assertTrue(this.pdpInstance.decisionRequest(e).isEventAllowed());
    e.setTimestamp(Instant.now());
    assertTrue(this.pdpInstance.decisionRequest(e).isEventAllowed());
    e.setTimestamp(Instant.now());
    assertTrue(this.pdpInstance.decisionRequest(e).isEventAllowed());
    e.setTimestamp(Instant.now());
    assertTrue(this.pdpInstance.decisionRequest(e).isEventAllowed());
    e.setTimestamp(Instant.now());
    assertTrue(this.pdpInstance.decisionRequest(e).isEventAllowed());
    e.setTimestamp(Instant.now());
    assertFalse(this.pdpInstance.decisionRequest(e).isEventAllowed());

  }

  @After
  public void cleanup() throws IOException, ResourceUpdateException {
    this.pdpInstance.revokePolicy(pId);
  }

  /**
   * Read resource file.
   *
   * @param  file               the file
   * @return                    the string
   * @throws IOException        Signals that an I/O exception has occurred.
   * @throws URISyntaxException the URI syntax exception
   */
  private String readResourceFile(String file) throws IOException, URISyntaxException {
    return new String(
        Files.readAllBytes(Paths.get(this.getClass().getClassLoader().getResource(file).toURI())));
  }

}
