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

package de.fraunhofer.iese.mydata.pdp.language.model.operator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.information.PipComponentInformation;
import de.fraunhofer.iese.mydata.component.information.method.MethodInterfaceDescription;
import de.fraunhofer.iese.mydata.component.interfaces.IBasicManagementService;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyInformationPoint;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.pdp.PolicyDecisionPoint;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.PipOperator;
import de.fraunhofer.iese.mydata.pdp.utils.ConnectorCache;
import de.fraunhofer.iese.mydata.pdp.utils.PipOperatorCache;
import de.fraunhofer.iese.mydata.policy.PipRequest;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.exception.InformationUndeterminableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Class PipOperatorTest.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PipOperatorTest {

  private static final String ACTION_BASE = "urn:action:testscope:";

  private static SolutionId SOLUTION;

  /**
   * The pip.
   */
  IPolicyInformationPoint pip;

  @Mock
  ConnectorCache cacheMock;

  /**
   * Inits the.
   *
   * @throws IOException              Signals that an I/O exception has occurred.
   * @throws InvalidEntityException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   * @throws SecurityException
   * @throws NoSuchFieldException
   */
  @BeforeEach
  void init() throws IOException, InvalidEntityException, IllegalArgumentException,
      IllegalAccessException, NoSuchFieldException, SecurityException, NoSuchEntityException {
    SOLUTION = new SolutionId("urn:solution:testscope");
    //    ConnectorFactory factoryMock = Mockito.mock(ConnectorFactory.class);

    //    Field factory = ConnectorCache.class.getDeclaredField("connectorFactory");
    //    factory.setAccessible(true);
    //    factory.set(PolicyDecisionPoint.getInstance().getConnectorCache(), factoryMock);

    final IBasicManagementService pmp = Mockito.mock(IBasicManagementService.class);
    //    Mockito.when(factoryMock.getPmpClient((URI)Matchers.isNull())).thenReturn(pmp);

    final List<URI> dummyUris = new ArrayList<URI>();
    dummyUris.add(URI.create("http://dummy"));
    final PipComponentInformation dummyPip = new PipComponentInformation(
        new ComponentId("urn:component:test:pip:dummy"), dummyUris);

    final Set<PipComponentInformation> comps = new HashSet<PipComponentInformation>();
    comps.add(dummyPip);
    Mockito.when(pmp.lookupPip(any(SolutionId.class),
        ArgumentMatchers.any(MethodInterfaceDescription.class))).thenReturn(comps);

    this.pip = Mockito.mock(IPolicyInformationPoint.class);

    //    Mockito.when(factoryMock.getPip(Matchers.isA(URI.class))).thenReturn(this.pip);
    //    Mockito.mockStatic(ConnectorFactory.class);
    //    Mockito.when(factoryMock.getPip(any(URI.class))).thenReturn(this.pip);

    //    final ConnectorCache cache = Mockito.mock(ConnectorCache.class);
    Mockito.when(this.cacheMock.getPmpConnectionFromCache()).thenReturn(pmp);
    Mockito.when(this.cacheMock.getPipConnectionFromCache(any(MethodInterfaceDescription.class),
        eq(SOLUTION))).thenReturn(this.pip);

    final Field f2 = PolicyDecisionPoint.class.getDeclaredField("connectorCache");
    f2.setAccessible(true);
    f2.set(PolicyDecisionPoint.getInstance(), this.cacheMock);
    final Field f3 = PipOperatorCache.class.getDeclaredField("connectorCache");
    f3.setAccessible(true);
    f3.set(PolicyDecisionPoint.getInstance().getPipOperatorCache(), this.cacheMock);

  }

  @AfterAll
  static void cleanUp() throws Exception {
    PolicyDecisionPoint.getInstance().reset();
    final Field pdpInstanceField = PolicyDecisionPoint.class.getDeclaredField("pdpInstance");
    pdpInstanceField.setAccessible(true);
    pdpInstanceField.set(null, null);
  }

  /**
   * PIP operator basic test.
   *
   * @throws EvaluationUndecidableException     the evaluation undecidable exception
   * @throws IOException                        Signals that an I/O exception has occurred.
   * @throws IllegalArgumentException           the illegal argument exception
   * @throws InformationUndeterminableException the information undeterminable exception
   * @throws InvalidEntityException
   */
  @Test
  void pipOperatorBasicTest() throws Exception {
    final PipOperator<String> pipOperator = new PipOperator<>("urn:info:test:bla", "",
        new DataObject<>("def"), String.class);
    Mockito.when(this.pip.evaluate(ArgumentMatchers.any(PipRequest.class)))
        .thenReturn(new DataObject("blubb"));
    final DataObject<?> res = pipOperator
        .evaluate(new Event(new ActionId(ACTION_BASE + "action1")));
    assertEquals("blubb", res.getValue());
  }

  @Test
  void pipOperatorDefaultConstructorTest()
      throws Exception {
    final PipOperator pipOperator = new PipOperator();
    Mockito.when(this.pip.evaluate(ArgumentMatchers.any(PipRequest.class)))
        .thenReturn(new DataObject("blubb"));
    final DataObject<?> res = pipOperator
        .evaluate(new Event(new ActionId(ACTION_BASE + "action1")));
    assertEquals("blubb", res.getValue());
  }

  /**
   * Test the interpretation of the TTL string
   *
   * @throws IllegalArgumentException the illegal argument exception
   * @throws SecurityException
   * @throws NoSuchFieldException
   * @throws IllegalAccessException
   * @throws InvalidEntityException
   */
  @Test
  void pipOperatorTTLTest() throws Exception {
    final PipOperator pipOperator = new PipOperator("urn:info:test:bla", "1y2w3d4h5m6s",
        new DataObject<>("def"), String.class);

    final Field f = PipOperator.class.getDeclaredField("timeToLive");
    f.setAccessible(true);
    final long ttlValue = f.getLong(pipOperator);
    assertEquals(33019506000L, ttlValue);
  }

  /**
   * PIP operator communication failure.
   *
   * @throws EvaluationUndecidableException     the evaluation undecidable exception
   * @throws IOException                        Signals that an I/O exception has occurred.
   * @throws IllegalArgumentException           the illegal argument exception
   * @throws InformationUndeterminableException the information undeterminable exception
   * @throws InvalidEntityException
   */
  @Test
  void pipOperatorCommunicationFailure() throws Exception {
    final PipOperator pipOperator = new PipOperator("urn:info:test:bla", "",
        new DataObject<>("def"), String.class);

    Mockito.when(this.pip.evaluate(ArgumentMatchers.any(PipRequest.class)))
        .thenThrow(new IOException("No dummy is here"));
    final DataObject<?> res = pipOperator
        .evaluate(new Event(new ActionId(ACTION_BASE + "action1")));
    assertEquals("def", res.getValue());
  }

}
