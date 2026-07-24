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
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
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
import de.fraunhofer.iese.mydata.policy.PolicyDeployableGroup;
import de.fraunhofer.iese.mydata.policy.event.Event;
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

import jakarta.validation.groups.Default;

/**
 * The Class BlackboxTest.
 */

@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("javadoc")
@ExtendWith(MockitoExtension.class)
class BlackboxJsonPathTest {

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

  @Test
  void policyInhibitWithJsonPathStringMatching()
      throws Exception {

    final Policy p = new Policy(
        this.readResourceFile("policyInhibitWithJsonPathStringMatching.xml"));
    final boolean res = this.pdpInstance.deploy(p, ZoneId.of("Europe/Berlin"));
    assertThat(res, is(true));
    final Event e = MyDataEntity.fromJson(
        this.readResourceFile("policyInhibitWithJsonPathStringMatching.json"), Event.class);

    assertFalse(this.pdpInstance.decisionRequest(e).isEventAllowed());
  }

  @Test
  void policyInhibitWithJsonPathStringNotMatching()
      throws Exception {
    final Policy p = new Policy(
        this.readResourceFile("policyInhibitWithJsonPathStringMatching.xml"));
    final boolean res = this.pdpInstance.deploy(p, ZoneId.of("Europe/Berlin"));
    assertThat(res, is(true));
    final Event e = MyDataEntity.fromJson(
        this.readResourceFile("policyInhibitWithJsonPathStringMatching.json"), Event.class);
    e.removeParameter("userName");
    e.addParameter("userName", "Hans");

    assertTrue(this.pdpInstance.decisionRequest(e).isEventAllowed());
  }

  @Test
  void policyInhibitWithJsonPathObjectMatching()
      throws Exception {
    final Policy p = new Policy(
        this.readResourceFile("policyInhibitWithJsonPathObjectMatching.xml"));
    final boolean res = this.pdpInstance.deploy(p, ZoneId.of("Europe/Berlin"));
    assertThat(res, is(true));
    final Event e = MyDataEntity.fromJson(
        this.readResourceFile("policyInhibitWithJsonPathStringMatching.json"), Event.class);

    assertFalse(this.pdpInstance.decisionRequest(e).isEventAllowed());
  }

  @Test
  void policyInhibitWithObjectComparision()
      throws Exception {
    final Policy p = new Policy(this.readResourceFile("policyInhibitWithObjectComparision.xml"));
    final boolean res = this.pdpInstance.deploy(p, ZoneId.of("Europe/Berlin"));
    assertThat(res, is(true));
    final Event e = MyDataEntity.fromJson(
        this.readResourceFile("policyInhibitWithJsonPathStringMatching.json"), Event.class);

    assertFalse(this.pdpInstance.decisionRequest(e).isEventAllowed());
  }

  @Test
  void policyInvalidShouldNotGetDeployed()
      throws Exception {
    final Policy p = new Policy(this.readResourceFile("policyOldSchemaNotDeployable.xml"));

    final boolean res = this.pdpInstance.deploy(p, ZoneId.of("Europe/Berlin"));
    assertFalse(res);
  }

  @Test
  void policyValueChangeWithoutID() throws Exception {
    final Policy p = new Policy(this.readResourceFile("policyValueChangedWithoutID.xml"));
    assertFalse(p.isXmlValid());
    Class x = PolicyDeployableGroup.class;
    Class x1 = Default.class;
    assertThrows(InvalidEntityException.class, () ->
      MyDataEntity.validate(p, x, x1));

    //    assertTrue(p.getIllegalArgumentExceptions().size() > 0);
    //    assert (p.getIllegalArgumentExceptions().stream().filter(i -> i.getCause().toString().contains("Attribute 'component_id' must appear on element 'valueChanged")).collect(Collectors.toList())
    //        .size() == 1);
  }

  @Test
  void policyValueChangeDuplicated()
      throws Exception {
    final Policy p = new Policy(this.readResourceFile("policyValueChangedDuplicateID.xml"));
    assertFalse(p.isXmlValid());
    Class x = PolicyDeployableGroup.class;
    Class x1 = Default.class;
    assertThrows(InvalidEntityException.class, () ->
      MyDataEntity.validate(p, x, x1));
    //
    //    assertTrue(p.getIllegalArgumentExceptions().size() > 0);
    //    assert (p.getIllegalArgumentExceptions().stream().filter(i -> i.getCause().toString().contains("There are multiple occurrences of ID value 'de027ca5-502b-4d82-bb04-d7a3e8b2a773'"))
    //        .collect(Collectors.toList()).size() == 1);
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
