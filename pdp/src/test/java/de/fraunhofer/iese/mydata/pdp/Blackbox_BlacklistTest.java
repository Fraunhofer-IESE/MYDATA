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

package de.fraunhofer.iese.mydata.pdp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.connector.ConnectorFactory;
import de.fraunhofer.iese.mydata.component.connector.OAuthCredentials;
import de.fraunhofer.iese.mydata.component.information.PipComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PxpComponentInformation;
import de.fraunhofer.iese.mydata.component.information.method.MethodInterfaceDescription;
import de.fraunhofer.iese.mydata.component.interfaces.IBasicManagementService;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyExecutionPoint;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyInformationPoint;
import de.fraunhofer.iese.mydata.exception.ConflictingResourceException;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.exception.ResourceUpdateException;
import de.fraunhofer.iese.mydata.pdp.utils.ConnectorCache;
import de.fraunhofer.iese.mydata.policy.Policy;
import de.fraunhofer.iese.mydata.policy.decision.AuthorizationDecision;
import de.fraunhofer.iese.mydata.policy.decision.ModifierEngine;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.ConflictingPolicyException;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Class BlackboxTestWhitelist. Checks the whitelist mode
 */
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class Blackbox_BlacklistTest {

  /**
   * The pdp instance.
   */
  private PolicyDecisionPoint pdpInstance;

  /**
   * The pip.
   */
  private IPolicyInformationPoint pip;

  /**
   * The pxp.
   */
  private IPolicyExecutionPoint pxp;

  /**
   * Check if the blacklist mode is enabled
   */
  @Test
  void checkIfWhitelistModeIsEnabled_shouldReturnTrue() {
    assertFalse(this.pdpInstance.isWhitelistModeEnabled());
  }

  /**
   * No policy deployed should return ALLOW in blacklist mode
   *
   * @throws IOException
   * @throws EvaluationUndecidableException
   */
  @Test
  void decideNoPolicyDeployed_shouldReturnInhibit()
      throws Exception {
    final Event e = new Event(new ActionId("urn:action:ppe:cr-writeData"));
    e.addParameter(new Parameter<>("eventType", true));
    e.addParameter(new Parameter<>("superString", "laueft."));
    assertTrue(this.pdpInstance.decisionRequest(e).isEventAllowed());
  }

  /**
   * @throws IOException
   * @throws ConflictingPolicyException
   * @throws URISyntaxException
   * @throws EvaluationUndecidableException
   * @throws InvalidEntityException
   */
  @Test
  void decidePolicyDeployedNoMechanismMatch_shouldReturnInhibit()
      throws Exception {
    final boolean res = this.pdpInstance
        .deploy(new Policy(this.readResourceFile("policyShort.xml")), ZoneId.of("Europe/Berlin"));
    assertThat(res, is(true));

    // the action id is not registered
    final Event e = new Event(new ActionId("urn:action:test:cr-writeDataNotThere"));
    e.addParameter(new Parameter<>("eventType", true));
    e.addParameter(new Parameter<>("superString", "laueft."));
    assertTrue(this.pdpInstance.decisionRequest(e).isEventAllowed());
  }

  /**
   * @throws IOException
   * @throws EvaluationUndecidableException
   * @throws ConflictingPolicyException
   * @throws URISyntaxException
   * @throws InvalidEntityException
   */
  @Test
  void decidePolicyDeployedMechanismMatchNoDecision_shouldReturnInhibit()
      throws Exception {
    final boolean res = this.pdpInstance
        .deploy(new Policy(this.readResourceFile("policyShort.xml")), ZoneId.of("Europe/Berlin"));
    assertThat(res, is(true));

    // the action id is registered
    final Event e = new Event(new ActionId("urn:action:test:cr-writeData"));
    // but not the event parameter
    e.addParameter(new Parameter<>("eventType", true));
    e.addParameter(new Parameter<>("superString", "laueft."));
    assertTrue(this.pdpInstance.decisionRequest(e).isEventAllowed());
  }

  /**
   * Return allow
   *
   * @throws IOException
   * @throws ConflictingPolicyException
   * @throws URISyntaxException
   * @throws EvaluationUndecidableException
   * @throws InvalidEntityException
   */
  @Test
  void decidePolicyDeployedMechanismMatchDecisionAllow()
      throws Exception {
    // deploy policy
    final boolean res = this.pdpInstance.deploy(
        new Policy(this.readResourceFile("policyDecisionAllow.xml")), ZoneId.of("Europe/Berlin"));
    assertThat(res, is(true));

    // role admin should return allow
    final Event e1 = new Event(new ActionId("urn:action:test:showEmployee"));
    e1.addParameter(new Parameter<>("role", "admin"));
    assertTrue(this.pdpInstance.decisionRequest(e1).isEventAllowed());

    // role user should return allow
    final Event e2 = new Event(new ActionId("urn:action:test:showEmployee"));
    e2.addParameter(new Parameter<>("role", "user"));
    assertTrue(this.pdpInstance.decisionRequest(e2).isEventAllowed());

    // role user should return allow since there is no decision
    final Event e3 = new Event(new ActionId("urn:action:test:showEmployee"));
    e3.addParameter(new Parameter<>("role", "guest"));
    assertTrue(this.pdpInstance.decisionRequest(e3).isEventAllowed());
  }

  /**
   * Return inhibit
   *
   * @throws IOException
   * @throws ConflictingPolicyException
   * @throws URISyntaxException
   * @throws EvaluationUndecidableException
   * @throws InvalidEntityException
   */
  @Test
  void decidePolicyDeployedMechanismMatchDecisionInhibit()
      throws Exception {
    final boolean res = this.pdpInstance.deploy(
        new Policy(this.readResourceFile("policyDecisionInhibit.xml")), ZoneId.of("Europe/Berlin"));
    assertThat(res, is(true));

    // role guest should return inhibit
    final Event e1 = new Event(new ActionId("urn:action:test:showEmployee"));
    e1.addParameter(new Parameter<>("role", "guest"));
    assertFalse(this.pdpInstance.decisionRequest(e1).isEventAllowed());

    // role nobody should return inhibit
    final Event e2 = new Event(new ActionId("urn:action:test:showEmployee"));
    e2.addParameter(new Parameter<>("role", "nobody"));
    assertFalse(this.pdpInstance.decisionRequest(e2).isEventAllowed());

    // role asdf should return inhibit (no match)
    final Event e3 = new Event(new ActionId("urn:action:test:showEmployee"));
    e3.addParameter(new Parameter<>("role", "asdf"));
    assertTrue(this.pdpInstance.decisionRequest(e3).isEventAllowed());
  }

  /**
   * @throws IOException
   * @throws ConflictingPolicyException
   * @throws URISyntaxException
   * @throws EvaluationUndecidableException
   * @throws InvalidEntityException
   */
  @Test
  void decidePolicyDeployedMechanismMatchDecisionModify_shouldReturnModifiedEvent()
      throws Exception {
    final boolean res = this.pdpInstance
        .deploy(new Policy(this.readResourceFile("policyShort.xml")), ZoneId.of("Europe/Berlin"));
    assertThat(res, is(true));

    // the action id is registered
    final Event e = new Event(new ActionId("urn:action:test:cr-writeData"));
    // parameters are correct
    e.addParameter(new Parameter<>("eventType", true));
    e.addParameter(new Parameter<>("attribute/longitude", "5/2"));
    e.addParameter(new Parameter<>("superString", "laueft."));
    final AuthorizationDecision decision = this.pdpInstance.decisionRequest(e);

    assertTrue(decision.isEventAllowed());
    assertEquals(1, decision.getModifiers().size());
    assertEquals("latitude", decision.getModifiers().get(0).getName());
    assertThat(decision.getModifiers().get(0).getEngine(), allOf(notNullValue(), hasSize(1)));

    final ModifierEngine engine = decision.getModifiers().get(0).getEngine().get(0);
    assertEquals("delete", engine.getMethod());
    assertThat(engine.getParameters(), allOf(notNullValue(), hasSize(1)));
    assertEquals("laueft.", (String) engine.getParameters().get(0).getValue());
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

  @AfterAll
  static void cleanUp() throws Exception {
    PolicyDecisionPoint.getInstance().reset();
    final Field pdpInstanceField = PolicyDecisionPoint.class.getDeclaredField("pdpInstance");
    pdpInstanceField.setAccessible(true);
    pdpInstanceField.set(null, null);
  }

  /**
   * Reset.
   *
   * @throws IOException                  Signals that an I/O exception has occurred.
   * @throws ResourceUpdateException
   * @throws ConflictingResourceException
   * @throws InvalidEntityException
   * @throws IllegalAccessException
   * @throws SecurityException
   * @throws NoSuchFieldException
   */
  @BeforeEach
  void reset() throws IOException, ConflictingResourceException, ResourceUpdateException,
      InvalidEntityException, IllegalAccessException, NoSuchFieldException, SecurityException,
      NoSuchEntityException {
    try {

      final ConnectorFactory factoryMock = Mockito.mock(ConnectorFactory.class);
      final Field factory = ConnectorCache.class.getDeclaredField("connectorFactory");
      factory.setAccessible(true);
      factory.set(PolicyDecisionPoint.getInstance().getConnectorCache(), factoryMock);

      final IBasicManagementService pmp = Mockito.mock(IBasicManagementService.class);
      Mockito.when(factoryMock.getPmpClient((URI) any())).thenReturn(pmp);
      Mockito.when(factoryMock.getPmpClient((URI) any(), (OAuthCredentials) any())).thenReturn(pmp);

      Mockito.when(pmp.addPxp(any(PxpComponentInformation.class))).thenReturn(new ComponentId());

      this.pdpInstance = PolicyDecisionPoint.getInstance();
      this.pdpInstance.reset();
      PolicyDecisionPoint.getInstance().getConnectorCache().setOAuthCredentials("PDP", "secret123",
          "http://localhost:8081/oauth/token");

      this.pdpInstance.initialize(new ComponentId("urn:component:test:pdp:decisionservice"),
          URI.create("http://localhost:8080/ws/pmp"), 4, false, null);

      final List<URI> dummyUris = new ArrayList<URI>();
      dummyUris.add(URI.create("http://dummy"));

      final PxpComponentInformation dummyPxp = new PxpComponentInformation(
          new ComponentId("urn:component:test:pxp:test"), dummyUris);
      final Set<PxpComponentInformation> pxpComps = new HashSet<>();
      pxpComps.add(dummyPxp);
      Mockito.when(pmp.lookupPxp(any(SolutionId.class), any(MethodInterfaceDescription.class)))
          .thenReturn(pxpComps);
      this.pxp = Mockito.mock(IPolicyExecutionPoint.class);
      Mockito.when(factoryMock.getPxp(ArgumentMatchers.isA(URI.class))).thenReturn(this.pxp);

      final PipComponentInformation dummyPip = new PipComponentInformation(
          new ComponentId("urn:component:test:pip:test"), dummyUris);
      final Set<PipComponentInformation> pipComps = new HashSet<>();
      pipComps.add(dummyPip);
      Mockito.when(pmp.lookupPip(any(SolutionId.class), any(MethodInterfaceDescription.class)))
          .thenReturn(pipComps);
      this.pip = Mockito.mock(IPolicyInformationPoint.class);
      Mockito.when(factoryMock.getPip(ArgumentMatchers.isA(URI.class))).thenReturn(this.pip);

      Mockito.when(factoryMock.getPip(ArgumentMatchers.isA(URI.class))).thenReturn(this.pip);

    } catch (final IOException e) {
      e.printStackTrace();
    } catch (final IllegalArgumentException e) {
      e.printStackTrace();
    }
  }
}
