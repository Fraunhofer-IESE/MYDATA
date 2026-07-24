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

package de.fraunhofer.iese.mydata.connectors.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.policy.decision.AuthorizationDecision;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * The Class PdpRestConnectorTest.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class PdpRestConnectorTest extends CommonTestSetup {
  @Mock
  protected RestTemplate mockRestTemplate;

  /**
   * Constant for single Result
   */
  private static final String POLICY_ID = "urn:policy:demo:demo";

  /**
   * The connector.
   */
  private PdpRestConnector connector;

  /**
   * Decision request test.
   *
   * @throws UnsupportedOperationException  the unsupported operation exception
   * @throws IOException                    Signals that an I/O exception has occurred.
   * @throws EvaluationUndecidableException
   */
  @Test
  public void decisionRequestTest() throws UnsupportedOperationException, IOException, EvaluationUndecidableException {
    final Event event = new Event(new ActionId("urn:action:es:demo"), new Parameter[0]);
    when(mockRestTemplate.postForObject(Constants.BASE_URL + "event", event, AuthorizationDecision.class)).thenReturn(AuthorizationDecision.getDecisionAllow());
    final AuthorizationDecision decision = connector.decisionRequest(event);
    assertEquals(AuthorizationDecision.DECISION_ALLOW, decision);

    verify(mockRestTemplate).postForObject(Constants.BASE_URL + "event", event, AuthorizationDecision.class);
  }

  /**
   * Decision request wrong answer test.
   *
   * @throws UnsupportedOperationException  the unsupported operation exception
   * @throws IOException                    Signals that an I/O exception has occurred.
   * @throws EvaluationUndecidableException
   */
  @Test
  public void decisionRequestWrongAnswerTest() throws UnsupportedOperationException, EvaluationUndecidableException {
    assertThrows(IOException.class, () -> {
      final Event event = new Event(new ActionId("urn:action:es:demo"), new Parameter[0]);
      when(mockRestTemplate.postForObject(Constants.BASE_URL + "event", event, AuthorizationDecision.class)).thenThrow(new RestClientException("Intentded Test Exception"));
      connector.decisionRequest(event);
    });
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.connectors.rest.CommonTestSetup#setUp()
   */
  @Override
  @BeforeEach
  public void setUp() throws UnsupportedOperationException, IOException, InvalidEntityException {
    super.setUp();
    connector = new PdpRestConnector(Constants.BASE_URL);
    setInternalState(connector, "httpClient", mockRestTemplate);
  }

}
