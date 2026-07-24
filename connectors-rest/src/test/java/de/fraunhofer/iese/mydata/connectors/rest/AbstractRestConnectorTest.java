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

import de.fraunhofer.iese.mydata.client.ClientId;
import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.connector.OAuthCredentials;
import de.fraunhofer.iese.mydata.component.health.HealthStatus;
import de.fraunhofer.iese.mydata.component.health.Status;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;

import org.apache.hc.core5.http.NameValuePair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;

/**
 * The Class AbstractRestConnectorTest.
 */
@ExtendWith(MockitoExtension.class)
public class AbstractRestConnectorTest extends CommonTestSetup {

  @Mock
  protected RestTemplate mockRestTemplate;

  /**
   * The initialize params.
   */
  private final Class<?>[] initializeParams = new Class[4];

  /**
   * The mock connector.
   */
  private AbstractRestConnector testSubject;

  /**
   * The component componentId.
   */
  private ComponentId componentId;

  //  /**
  //   * The json component componentId response.
  //   */
  //  private static String jsonComponentIdResponse = MyDataEntity.getGson().toJson(componentId);

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.connectors.rest.CommonTestSetup#setUp()
   */
  @Override
  @BeforeEach
  public void setUp() throws UnsupportedOperationException, IOException, InvalidEntityException {
    super.setUp();
    this.componentId = new ComponentId("urn:component:test:pdp:demo");

    this.testSubject = new AbstractRestConnector(Constants.SCHEME, Constants.HOST, Constants.PORT,
        Constants.NAME) {
      private static final long serialVersionUID = 8488078732825029033L;

      private final ArrayList<NameValuePair> list = new ArrayList<>(1);

      {
        this.list.add(new NameValuePair() {

          @Override
          public String getName() {
            return "Constants.NAME";
          }

          @Override
          public String getValue() {
            return "value";
          }
        });
      }

    };
    setInternalState(this.testSubject, "httpClient", this.mockRestTemplate);

    this.initializeParams[0] = String.class;
    this.initializeParams[1] = String.class;
    this.initializeParams[2] = int.class;
    this.initializeParams[3] = String.class;
  }

  /**
   * Constructor short tests.
   *
   * @throws UnsupportedOperationException the unsupported operation exception
   * @throws IOException                   Signals that an I/O exception has occurred.
   */
  @Test
  public void constructorShortTests() throws UnsupportedOperationException, IOException {
    try {
      final AbstractRestConnector restConn = new AbstractRestConnector(null) {
        private static final long serialVersionUID = 1765333485040341189L;
      };
      assertThat(restConn.getBaseUrl(), equalTo(Constants.BASE_URL));
    } catch (final Exception e) {
      assertThat(e, instanceOf(IllegalArgumentException.class));
      assertThat(e.getMessage(), equalTo("URL must not be null"));
    }
  }

  /**
   * Constructor tests.
   *
   * @throws UnsupportedOperationException the unsupported operation exception
   * @throws IOException                   Signals that an I/O exception has occurred.
   */
  @Test
  public void constructorTests() throws UnsupportedOperationException, IOException {
    try {
      final AbstractRestConnector restConn = new AbstractRestConnector(Constants.SCHEME,
          Constants.HOST, Constants.PORT, Constants.NAME) {
        private static final long serialVersionUID = 245677062542169872L;
      };

      assertThat(restConn.getBaseUrl(), equalTo(Constants.BASE_URL));
    } catch (final Exception e) {
      Assertions.fail("Exception has been thrown: " + e.getMessage());
    }
  }

  /**
   * Gets the componentId tests.
   *
   * @throws UnsupportedOperationException the unsupported operation exception
   * @throws IOException                   Signals that an I/O exception has occurred.
   */
  @Test
  public void getIdTests() throws UnsupportedOperationException, IOException {
    Mockito.when(
        this.mockRestTemplate.getForObject(Constants.BASE_URL + "component-id", ComponentId.class))
        .thenReturn(this.componentId);

    final ComponentId idResponse = this.testSubject.getId();
    Assertions.assertEquals(this.componentId, idResponse);
  }

  /**
   * Gets the componentId tests wrong answer.
   *
   * @throws UnsupportedOperationException the unsupported operation exception
   * @throws IOException                   Signals that an I/O exception has occurred.
   */
  @Disabled("old concept?") // TODO check
  @Test
  public void getIdTests_WrongAnswer() throws UnsupportedOperationException, IOException {
    Mockito
        .when(this.mockRestTemplate.getForObject(Constants.BASE_URL + "component-id", String.class))
        .thenReturn("bla");
    Assertions.assertThrows(IllegalArgumentException.class, () -> this.testSubject.getId());
  }

  @Test
  public void getHealthTests() throws IOException {
    Mockito
        .when(this.mockRestTemplate.getForObject(Constants.BASE_URL + "health", HealthStatus.class))
        .thenReturn(HealthStatus.of(Status.UP), HealthStatus.of(Status.DOWN),
            HealthStatus.of(Status.UNKNOWN));
    final HealthStatus hopefullyUp = this.testSubject.getHealth();
    Assertions.assertEquals(HealthStatus.of(Status.UP), hopefullyUp);
    final HealthStatus hopefullyDown = this.testSubject.getHealth();
    Assertions.assertEquals(HealthStatus.of(Status.DOWN), hopefullyDown);
    final HealthStatus hopefullyUnknown = this.testSubject.getHealth();
    Assertions.assertEquals(HealthStatus.of(Status.UNKNOWN), hopefullyUnknown);
  }

  @Test
  public void getHealthTests_WrongAnswer() throws UnsupportedOperationException, IOException {
    Mockito
        .when(this.mockRestTemplate.getForObject(Constants.BASE_URL + "health", HealthStatus.class))
        .thenThrow(new RestClientException("intended for testing"));
    Assertions.assertThrows(IOException.class, () -> this.testSubject.getHealth());
  }

  /**
   * Initialize tests.
   *
   * @throws UnsupportedOperationException the unsupported operation exception
   * @throws IOException                   Signals that an I/O exception has occurred.
   * @throws InvocationTargetException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws SecurityException
   * @throws NoSuchMethodException
   */
  @Test
  public void initializeTests() {
    Assertions.assertDoesNotThrow(() -> {
      final Method method = AbstractRestConnector.class.getDeclaredMethod("initialize",
          this.initializeParams);
      method.setAccessible(true);
      method.invoke(this.testSubject, new Object[] {
          Constants.SCHEME, Constants.HOST, Integer.valueOf(Constants.PORT), Constants.NAME
      });
    });
  }

  /**
   * Initialize wrong host tests.
   */
  @Test
  public void initializeWrongHostTests() {
    try {
      final Method method = AbstractRestConnector.class.getDeclaredMethod("initialize",
          this.initializeParams);
      method.setAccessible(true);
      method.invoke(this.testSubject, new Object[] {
          Constants.SCHEME, null, Integer.valueOf(Constants.PORT), Constants.NAME
      });
      Assertions.fail("No exception has been thrown.");
    } catch (final Exception e) {
      assertThat(e.getCause(), instanceOf(IllegalArgumentException.class));
      assertThat(e.getCause().getMessage(), equalTo("Host must not be null"));
    }
  }

  /**
   * Initialize wrong name tests.
   */
  @Test
  public void initializeWrongNameTests() {
    try {
      Method method = AbstractRestConnector.class.getDeclaredMethod("initialize",
          this.initializeParams);
      method.setAccessible(true);
      method.invoke(this.testSubject, new Object[] {
          Constants.SCHEME, Constants.HOST, Constants.PORT, null
      });
      assertThat((String) getInternalState(this.testSubject, "name"), equalTo(""));

      method = AbstractRestConnector.class.getDeclaredMethod("initialize", this.initializeParams);
      method.setAccessible(true);
      method.invoke(this.testSubject, new Object[] {
          Constants.SCHEME, Constants.HOST, Constants.PORT, "/" + Constants.NAME
      });
      assertThat((String) getInternalState(this.testSubject, "name"), equalTo(Constants.NAME));

      method = AbstractRestConnector.class.getDeclaredMethod("initialize", this.initializeParams);
      method.setAccessible(true);
      method.invoke(this.testSubject, new Object[] {
          Constants.SCHEME, Constants.HOST, Constants.PORT, Constants.NAME
      });
      assertThat((String) getInternalState(this.testSubject, "name"), equalTo(Constants.NAME));
    } catch (final Exception e) {
      Assertions.fail("Exception has been thrown: " + e.getMessage());
    }
  }

  /**
   * Initialize wrong port tests.
   */
  @Test
  public void initializeWrongPortTests() {
    try {
      final Method method = AbstractRestConnector.class.getDeclaredMethod("initialize",
          this.initializeParams);
      method.setAccessible(true);
      method.invoke(this.testSubject, Constants.SCHEME, Constants.HOST, Integer.valueOf(-1),
          Constants.NAME);
      assertThat(Integer.valueOf((int) getInternalState(this.testSubject, "port")),
          equalTo(Integer.valueOf(80)));
    } catch (final Exception e) {
      Assertions.fail("Exception has been thrown: " + e.getMessage());
    }
  }

  /**
   * Initialize wrong scheme tests.
   */
  @Test
  public void initializeWrongSchemeTests() {
    try {
      final Method method = AbstractRestConnector.class.getDeclaredMethod("initialize",
          this.initializeParams);
      method.setAccessible(true);
      method.invoke(this.testSubject, new Object[] {
          "demo", Constants.HOST, Integer.valueOf(Constants.PORT), Constants.NAME
      });
      Assertions.fail("No exception has been thrown.");
    } catch (final Exception e) {
      assertThat(e.getCause(), instanceOf(IllegalArgumentException.class));
      assertThat(e.getCause().getMessage(), equalTo("Scheme demo not supported"));
    }
  }

  /**
   * Reset tests.
   *
   * @throws UnsupportedOperationException the unsupported operation exception
   * @throws IOException                   Signals that an I/O exception has occurred.
   */
  @Test
  public void resetTests() throws UnsupportedOperationException, IOException {
    Mockito.when(this.mockRestTemplate.getForObject(Constants.BASE_URL + "reset", boolean.class))
        .thenReturn(true);
    final boolean resetResponse = this.testSubject.reset();
    assertThat(resetResponse, equalTo(Boolean.TRUE));
  }

  /**
   * Reset tests wrong answer.
   *
   * @throws UnsupportedOperationException the unsupported operation exception
   * @throws IOException                   Signals that an I/O exception has occurred.
   */
  @Test
  public void resetTests_WrongAnswer() throws UnsupportedOperationException, IOException {
    Mockito.when(this.mockRestTemplate.getForObject(Constants.BASE_URL + "reset", boolean.class))
        .thenThrow(new RestClientException("Intended Test Exception"));

    final boolean resetResponse = this.testSubject.reset();
    assertThat(resetResponse, equalTo(Boolean.FALSE));

  }

  @Disabled("Oauth credentials are not supported yet")
  @Test
  public void testOAuthSetup() throws Exception {
    final PdpRestConnector connector = new PdpRestConnector("http://localhost:8080",
        new OAuthCredentials(new ClientId("urn:client:somesolution:some"), "somesecret",
            URI.create("http://localhost:8080")));
    final Object httpClient = getInternalState(connector, "httpClient");
    Assertions.assertEquals(RestTemplate.class, httpClient.getClass());
  }

  /**
   * Various small tests.
   */
  @Test
  public void variousSmallTests() {
    this.testSubject.setBaseUrl(Constants.BASE_URL);
    final String url = this.testSubject.getBaseUrl();
    assertThat(url, equalTo(Constants.BASE_URL));

    final String toStringResponse = this.testSubject.toString();
    assertThat(toStringResponse,
        equalTo(this.testSubject.getClass().getSimpleName() + ": " + Constants.BASE_URL));
  }
}
