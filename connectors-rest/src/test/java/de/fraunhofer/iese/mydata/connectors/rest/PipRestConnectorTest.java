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
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.policy.PipRequest;
import de.fraunhofer.iese.mydata.policy.event.InfoId;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;

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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * The Class PipRestConnectorTest.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class PipRestConnectorTest extends CommonTestSetup {

  @Mock
  protected RestTemplate mockRestTemplate;

  /**
   * The pip integer response.
   */
  private final DataObject<Integer> pipIntegerResponse = new DataObject<>(1);

  /**
   * The pip integer request.
   */
  private PipRequest pipIntegerRequest;

  /**
   * The connector.
   */
  private PipRestConnector connector;

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.connectors.rest.CommonTestSetup#setUp()
   */
  @Override
  @BeforeEach
  public void setUp() throws UnsupportedOperationException, IOException, InvalidEntityException {
    super.setUp();

    this.connector = new PipRestConnector(Constants.BASE_URL);
    setInternalState(this.connector, "httpClient", this.mockRestTemplate);

    this.pipIntegerRequest = new PipRequest(new InfoId("urn:info:test:demo"), new Parameter[0]);
  }

  /**
   * Evaluate test successful.
   *
   * @throws ClientProtocolException the client protocol exception
   * @throws IOException             Signals that an I/O exception has occurred.
   */
  @Test
  public void evaluateTestSuccessful() throws IOException {
    final HttpEntity<PipRequest> requestEntity = new HttpEntity<>(this.pipIntegerRequest);
    Mockito
        .when(this.mockRestTemplate.exchange(Constants.BASE_URL + "execute", HttpMethod.POST,
            requestEntity, DataObject.class))
        .thenReturn(new ResponseEntity<DataObject>(this.pipIntegerResponse, HttpStatus.OK));

    final DataObject<?> pipEvaluationResult = this.connector.evaluate(this.pipIntegerRequest);
    assertThat(pipEvaluationResult.getValue(), instanceOf(Integer.class));
    assertThat(pipEvaluationResult.getValue(), equalTo(1));

    Mockito.verify(this.mockRestTemplate).exchange(Constants.BASE_URL + "execute", HttpMethod.POST,
        requestEntity, DataObject.class);
  }

  /**
   * Evaluate test successful wrong answer.
   *
   * @throws ClientProtocolException the client protocol exception
   * @throws IOException             Signals that an I/O exception has occurred.
   */
  @Test
  public void evaluateTestSuccessful_WrongAnswer() {
    assertThrows(IOException.class, () -> {
      final HttpEntity<PipRequest> requestEntity = new HttpEntity<>(this.pipIntegerRequest);
      Mockito
          .when(this.mockRestTemplate.exchange(Constants.BASE_URL + "execute", HttpMethod.POST,
              requestEntity, DataObject.class))
          .thenThrow(new RestClientException("Intended Exception"));
      this.connector.evaluate(this.pipIntegerRequest);
    });
  }
}
