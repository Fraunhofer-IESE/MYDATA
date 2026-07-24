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

package de.fraunhofer.iese.mydata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.fraunhofer.iese.mydata.client.ClientId;
import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.connector.Authentication;
import de.fraunhofer.iese.mydata.component.connector.ConnectorFactory;
import de.fraunhofer.iese.mydata.component.connector.OAuthCredentials;
import de.fraunhofer.iese.mydata.component.information.PdpComponentInformation;
import de.fraunhofer.iese.mydata.component.interfaces.IManagementService;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyDecisionPoint;
import de.fraunhofer.iese.mydata.exception.InitializationException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.internal.IMyDataEnvironmentFullFace;
import de.fraunhofer.iese.mydata.internal.IMyDataEnvironmentInitializer;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(MockitoExtension.class)
class MyDataEnvironmentManagerTest {
  private final SolutionId solutionId = new SolutionId("urn:solution:test");

  private IMyDataEnvironmentFullFace myDataEnvironment;

  @BeforeEach
  void setUp() {
    MyDataEnvironmentManager.enableOverwritingOfExistingMyDataEnvironments();
  }

  @AfterEach
  void tearDown() {
    MyDataEnvironmentManager.disableOverwritingOfExistingMyDataEnvironments();
  }

  private IMyDataEnvironmentFullFace initLocal() throws InitializationException {
    return (IMyDataEnvironmentFullFace) MyDataEnvironmentManager.constructDefaultEnvironment()
        .initializeLocal(this.solutionId, "Europe/Berlin", 4, true, null);
  }

  private IMyDataEnvironmentFullFace initLocalWithFileSync() throws InitializationException {
    return (IMyDataEnvironmentFullFace) MyDataEnvironmentManager.constructDefaultEnvironment()
        .initializeLocalWithFileSync(this.solutionId, "Europe/Berlin",
            "target/test-classes/policies", 4, false, null);
  }

  private IMyDataEnvironmentFullFace initLocalWithCloudSync(ConnectorFactory connectorFactory)
      throws InitializationException {
    final IMyDataEnvironmentInitializer builder = MyDataEnvironmentManager
        .constructDefaultEnvironment();
    try {
      final Field f = MyDataEnvironmentManager.Builder.class.getDeclaredField("connectorFactory");
      f.setAccessible(true);
      f.set(builder, connectorFactory);
    } catch (IllegalAccessException | NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
    return (IMyDataEnvironmentFullFace) builder.initializeLocalWithCloudSync(this.solutionId,
        URI.create("http://localhost:8080"),
        new OAuthCredentials(new ClientId("urn:client:test:test"), "secret",
            URI.create("http://localhost:8080/oauth/token")),
        "Europe/Berlin", false, null, null, "PT5M", "0/5 * * * * ?", false, 4, false, null);
  }

  private IMyDataEnvironmentFullFace initCloud(ConnectorFactory connectorFactory)
      throws InitializationException {
    final IMyDataEnvironmentInitializer builder = MyDataEnvironmentManager
        .constructDefaultEnvironment();
    try {
      final Field f = MyDataEnvironmentManager.Builder.class.getDeclaredField("connectorFactory");
      f.setAccessible(true);
      f.set(builder, connectorFactory);
    } catch (IllegalAccessException | NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
    return (IMyDataEnvironmentFullFace) builder.initializeCloud(this.solutionId,
        URI.create("http://localhost:8080"),
        new OAuthCredentials(new ClientId("urn:client:test:test"), "secret",
            URI.create("http://localhost:8080/oauth/token")));
  }

  // needs to be executed first
  @Test
  void aaaa_whenNotInitialized_thenDefaultNotAccessible() {
    assertThrows(IllegalStateException.class, () -> {
      MyDataEnvironmentManager.getDefaultEnvironment();
    });
  }

  @Test
  void whenGetEnvironmentWithNull_exceptionWillBeThrown() {
    assertThrows(IllegalArgumentException.class,
        () -> MyDataEnvironmentManager.getEnvironment(null));
  }

  @Test
  void whenOverwritingDisabled_DuplicatesResultInException() throws InitializationException {
    MyDataEnvironmentManager.disableOverwritingOfExistingMyDataEnvironments();

    assertThrows(InitializationException.class, () -> {
      this.myDataEnvironment = this.initLocal();
    });
  }

  @Test
  void defaultEnvironmentCanBeReplacedInTests() throws InitializationException {
    this.myDataEnvironment = this.initLocal();
    final IMyDataEnvironmentFullFace oldMyDataEnvironment = this.myDataEnvironment;
    this.myDataEnvironment = this.initLocal();
    assertSame(MyDataEnvironmentManager.getDefaultEnvironment(), this.myDataEnvironment);
    assertNotSame(oldMyDataEnvironment, this.myDataEnvironment);
  }

  @Test
  void initializeLocal_defaultIsAccessible() throws InitializationException {
    this.myDataEnvironment = this.initLocal();
    assertSame(MyDataEnvironmentManager.getDefaultEnvironment(), this.myDataEnvironment);
    assertEquals(OperationalMode.LOCAL, this.myDataEnvironment.getOperationalMode());
    assertEquals(this.solutionId, this.myDataEnvironment.getSolutionId());
  }

  @Test
  void initializeLocalWithFileSync_defaultIsAccessible() throws InitializationException {
    this.myDataEnvironment = this.initLocalWithFileSync();
    assertSame(MyDataEnvironmentManager.getDefaultEnvironment(), this.myDataEnvironment);
    assertEquals(OperationalMode.LOCAL_WITH_FILE_SYNC, this.myDataEnvironment.getOperationalMode());
    assertEquals(this.solutionId, this.myDataEnvironment.getSolutionId());
  }

  @Test
  void initializeLocalWithCloudSync_defaultIsAccessible() throws InitializationException {
    final ConnectorFactory factoryMock = Mockito.mock(ConnectorFactory.class);
    final IManagementService msMock = Mockito.mock(IManagementService.class);
    Mockito.when(factoryMock.getManagementService(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(msMock);
    this.myDataEnvironment = this.initLocalWithCloudSync(factoryMock);

    assertSame(MyDataEnvironmentManager.getDefaultEnvironment(), this.myDataEnvironment);
    assertEquals(OperationalMode.LOCAL_WITH_CLOUD_SYNC,
        this.myDataEnvironment.getOperationalMode());
    assertEquals(this.solutionId, this.myDataEnvironment.getSolutionId());
  }

  @Test
  void initializeCloud_defaultIsAccessible()
      throws InitializationException, IOException, NoSuchEntityException {
    final ConnectorFactory factoryMock = Mockito.mock(ConnectorFactory.class);
    final IManagementService msMock = Mockito.mock(IManagementService.class);
    final ComponentId pdpComponentId = new ComponentId("urn:component:mydata:pdp:pdp");
    final List<URI> ts = new ArrayList<>();
    ts.add(URI.create("http://test:8081"));
    Mockito.when(msMock.getPdp()).thenReturn(new PdpComponentInformation(pdpComponentId, ts));
    final IPolicyDecisionPoint pdpMock = Mockito.mock(IPolicyDecisionPoint.class);
    Mockito.when(factoryMock.getManagementService(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(msMock);
    Mockito.when(factoryMock.getPdp(ArgumentMatchers.any(PdpComponentInformation.class),
        ArgumentMatchers.any(Authentication.class))).thenReturn(pdpMock);
    this.myDataEnvironment = this.initCloud(factoryMock);

    assertSame(MyDataEnvironmentManager.getDefaultEnvironment(), this.myDataEnvironment);
    assertEquals(OperationalMode.CLOUD, this.myDataEnvironment.getOperationalMode());
    assertEquals(this.solutionId, this.myDataEnvironment.getSolutionId());
  }

}
