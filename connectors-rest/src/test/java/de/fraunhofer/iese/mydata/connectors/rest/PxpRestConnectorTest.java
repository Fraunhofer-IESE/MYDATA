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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.policy.decision.ExecuteAction;

import org.apache.hc.client5.http.ClientProtocolException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * The Class PxpRestConnectorTest.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class PxpRestConnectorTest extends CommonTestSetup {

  @Mock
  protected RestTemplate mockRestTemplate;

  /**
   * The action.
   */
  private ExecuteAction action;

  /**
   * The connector.
   */
  private PxpRestConnector connector;

  private HttpEntity requestEntity;

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.connectors.rest.CommonTestSetup#setUp()
   */
  @Override
  @BeforeEach
  public void setUp() throws UnsupportedOperationException, IOException, InvalidEntityException {
    super.setUp();
    this.action = new ExecuteAction("urn:action:demo:demo");
    this.connector = new PxpRestConnector(Constants.BASE_URL);
    setInternalState(this.connector, "httpClient", this.mockRestTemplate);
    this.requestEntity = new HttpEntity<>(this.action);

  }

  /**
   * Execute test successful.
   *
   * @throws ClientProtocolException the client protocol exception
   * @throws IOException             Signals that an I/O exception has occurred.
   */
  @Test
  public void executeTestSuccessful() throws IOException {
    Mockito.when(this.mockRestTemplate.postForObject(Constants.BASE_URL + "execute",
        this.requestEntity, boolean.class)).thenReturn(true);

    final boolean executionResult = this.connector.execute(this.action);
    assertThat(executionResult, equalTo(Boolean.TRUE));

    Mockito.verify(this.mockRestTemplate).postForObject(Constants.BASE_URL + "execute",
        this.requestEntity, boolean.class);
  }

  /**
   * Execute test wrong answer.
   *
   * @throws ClientProtocolException the client protocol exception
   * @throws IOException             Signals that an I/O exception has occurred.
   */
  @Test
  public void executeTestWrongAnswer() throws IOException {
    Mockito.when(this.mockRestTemplate.postForObject(Constants.BASE_URL + "execute",
        this.requestEntity, boolean.class)).thenThrow(new RestClientException("Test message"));

    final boolean executionResult = this.connector.execute(this.action);
    assertThat(executionResult, equalTo(Boolean.FALSE));

  }
}
