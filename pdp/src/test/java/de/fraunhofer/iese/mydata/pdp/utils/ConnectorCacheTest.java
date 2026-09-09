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

package de.fraunhofer.iese.mydata.pdp.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.fraunhofer.iese.mydata.component.connector.ConnectorFactory;
import de.fraunhofer.iese.mydata.component.connector.OAuthCredentials;
import de.fraunhofer.iese.mydata.component.information.PipComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PxpComponentInformation;
import de.fraunhofer.iese.mydata.component.information.method.MethodInterfaceDescription;
import de.fraunhofer.iese.mydata.component.interfaces.IBasicManagementService;
import de.fraunhofer.iese.mydata.component.interfaces.IMyDataComponent;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyExecutionPoint;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyInformationPoint;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.pdp.PolicyDecisionPoint;
import de.fraunhofer.iese.mydata.pdp.interfaces.IConnectorCache;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The Class ConnectorCacheTest.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ConnectorCacheTest {

  private ConnectorCache connectorCache;

  @Mock
  private ConnectorFactory connectorFactory;

  private SolutionId solutionId;

  @Mock
  static OAuthCredentials oAuthCredentials;

  @Mock
  private IBasicManagementService pmpMock;

  @Mock
  private IPolicyInformationPoint pipMock;

  @Mock
  private IPolicyExecutionPoint pxpMock;

  @Mock
  private PipComponentInformation pipComponent;

  @Mock
  private PxpComponentInformation pxpComponent;

  /**
   * The query.
   */
  private final MethodInterfaceDescription query = new MethodInterfaceDescription("demo", Boolean.class, "");

  /**
   * Setup.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws URISyntaxException the URI syntax exception
   * @throws InvalidEntityException
   * @throws SecurityException
   * @throws NoSuchFieldException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   */
  @BeforeEach
  void setup() throws IOException, URISyntaxException, InvalidEntityException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, NoSuchEntityException {
    connectorCache = new ConnectorCache(this.connectorFactory);
    solutionId = new SolutionId("urn:solution:testscope");
    setInternalState(this.connectorCache, "pipCache", new HashMap<>(10));
    setInternalState(this.connectorCache, "pxpCache", new HashMap<>(10));
    setInternalState(this.connectorCache, "pmpCache", this.pmpMock);
    setInternalState(this.connectorCache, "oAuthCredentials", ConnectorCacheTest.oAuthCredentials);

    when(this.pmpMock.lookupPip(any(SolutionId.class), eq(this.query))).thenReturn(Set.of(this.pipComponent));
    when(this.pmpMock.lookupPxp(any(SolutionId.class), eq(this.query))).thenReturn(Set.of(this.pxpComponent));

    when(this.pipComponent.getUrls()).thenReturn(List.of(new URI("http://localhost/pipComponent")));
    when(this.pxpComponent.getUrls()).thenReturn(List.of(new URI("http://localhost/pxpComponent")));

    Mockito.when(connectorFactory.getPip(any(URI.class))).thenReturn(this.pipMock);
    Mockito.when(connectorFactory.getPxp(any(URI.class))).thenReturn(this.pxpMock);

    this.query.setId(22L);
  }

  /**
   * Clear PIP cache test should result in empty cache.
   *
   * @throws SecurityException
   * @throws NoSuchFieldException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   */
  @Test
  void clearPipCacheTest_ShouldResultInEmptyCache() throws Exception {
    final IPolicyInformationPoint pipConnector = this.connectorCache.getPipConnectionFromCache(this.query, this.solutionId);
    assertNotNull(pipConnector);

    final Map<MethodInterfaceDescription, IMyDataComponent> pipCache = (Map<MethodInterfaceDescription, IMyDataComponent>) getInternalState(this.connectorCache, "pipCache");
    assertEquals(1, pipCache.size());

    this.connectorCache.clearPipCache();
    assertEquals(0, pipCache.size());
  }

  private Object getInternalState(Object o, String string) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    final Field f = o.getClass().getDeclaredField(string);
    f.setAccessible(true);
    return f.get(o);
  }

  /**
   * Clear PXP cache test should result in empty cache.
   *
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   * @throws SecurityException
   * @throws NoSuchFieldException
   */
  @Test
  void clearPxpCacheTest_ShouldResultInEmptyCache() throws Exception {
    final IPolicyExecutionPoint pxpConnector = this.connectorCache.getPxpConnectionFromCache(this.query, this.solutionId);
    assertNotNull(pxpConnector);

    final Map<MethodInterfaceDescription, IMyDataComponent> pxpCache = (Map<MethodInterfaceDescription, IMyDataComponent>) getInternalState(this.connectorCache, "pxpCache");
    assertEquals(1, pxpCache.size());

    this.connectorCache.clearPxpCache();
    assertEquals(0, pxpCache.size());
  }

  /**
   * Clear PMP cache test should result in empty cache.
   *
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   * @throws SecurityException
   * @throws NoSuchFieldException
   */
  @Test
  void clearPMPCacheTest_ShouldResultInEmptyCache() throws Exception {
    IBasicManagementService pmpConnector = (IBasicManagementService) getInternalState(this.connectorCache, "pmpCache");
    assertNotNull(pmpConnector);
    this.connectorCache.clearPMPCache();
    pmpConnector = (IBasicManagementService) getInternalState(this.connectorCache, "pmpCache");
    assertNull(pmpConnector);
  }

  /**
   * Gets the PIP component from cache null parameter test should result in
   * null.
   */
  @Test
  void getPipComponentFromCacheNullParameterTest_ShouldResultInNull() {
    final IPolicyInformationPoint pipConnector = this.connectorCache.getPipConnectionFromCache(null, this.solutionId);
    assertNull(pipConnector);
  }

  /**
   * Gets the PXP component from cache null parameter test should result in
   * null.
   */
  @Test
  void getPxpComponentFromCacheNullParameterTest_ShouldResultInNull() {
    final IPolicyExecutionPoint pxpConnector = this.connectorCache.getPxpConnectionFromCache(null, this.solutionId);
    assertNull(pxpConnector);
  }

  /**
   * Lookup component cache full test should result in component.
   *
   * @throws NoSuchMethodException the no such method exception
   * @throws SecurityException the security exception
   * @throws IllegalAccessException the illegal access exception
   * @throws IllegalArgumentException the illegal argument exception
   * @throws InvocationTargetException the invocation target exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InvalidEntityException
   * @throws NoSuchFieldException
   */
  @Test
  void lookupComponentCacheFullTest_ShouldResultInComponent()
      throws Exception {
    final Map<MethodInterfaceDescription, IMyDataComponent> pipCache = (Map<MethodInterfaceDescription, IMyDataComponent>) getInternalState(this.connectorCache, "pipCache");
    pipCache.clear();
    pipCache.put(this.query, this.pipMock);

    final Method lookupMethod = this.getMethodToTest();
    lookupMethod.setAccessible(true);
    final IPolicyInformationPoint lookupResult = this.invokeTestMethod(pipCache, lookupMethod);
    assertEquals(lookupResult, this.pipMock);
    verify(this.pmpMock, never()).lookupPip(any(SolutionId.class), eq(this.query));
  }

  /**
   * Lookup component cache empty lookup failed test should result in null.
   *
   * @throws NoSuchMethodException the no such method exception
   * @throws SecurityException the security exception
   * @throws IllegalAccessException the illegal access exception
   * @throws IllegalArgumentException the illegal argument exception
   * @throws InvocationTargetException the invocation target exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InvalidEntityException
   * @throws NoSuchFieldException
   */
  @Test
  void lookupComponentCacheEmptyLookupFailedTest_ShouldResultInNull()
      throws Exception {
    final Map<MethodInterfaceDescription, IMyDataComponent> pipCache = (Map<MethodInterfaceDescription, IMyDataComponent>) getInternalState(this.connectorCache, "pipCache");
    pipCache.clear();

    when(this.pmpMock.lookupPip(any(SolutionId.class), eq(this.query))).thenThrow(new IOException());
    when(this.pmpMock.lookupPxp(any(SolutionId.class), eq(this.query))).thenThrow(new IOException());

    final Method lookupMethod = this.getMethodToTest();
    lookupMethod.setAccessible(true);
    final IPolicyInformationPoint lookupResult = this.invokeTestMethod(pipCache, lookupMethod);
    assertNull(lookupResult);
  }

  /**
   * Lookup component cache empty lookup null comp test should result in null.
   *
   * @throws NoSuchMethodException the no such method exception
   * @throws SecurityException the security exception
   * @throws IllegalAccessException the illegal access exception
   * @throws IllegalArgumentException the illegal argument exception
   * @throws InvocationTargetException the invocation target exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InvalidEntityException
   * @throws NoSuchFieldException
   */
  @Test
  void lookupComponentCacheEmptyLookupNullCompTest_ShouldResultInNull()
      throws Exception {

    final Map<MethodInterfaceDescription, IMyDataComponent> pipCache = (Map<MethodInterfaceDescription, IMyDataComponent>) getInternalState(this.connectorCache, "pipCache");
    pipCache.clear();

    when(this.pmpMock.lookupPip(any(SolutionId.class), eq(this.query))).thenReturn(null);
    when(this.pmpMock.lookupPxp(any(SolutionId.class), eq(this.query))).thenReturn(null);

    final Method lookupMethod = this.getMethodToTest();
    lookupMethod.setAccessible(true);
    final IPolicyInformationPoint lookupResult = this.invokeTestMethod(pipCache, lookupMethod);
    assertNull(lookupResult);
    verify(this.pmpMock, times(1)).lookupPip(any(SolutionId.class), any(MethodInterfaceDescription.class));
  }

  /**
   * Lookup component cache empty lookup empty comp test should result in null.
   *
   * @throws NoSuchMethodException the no such method exception
   * @throws SecurityException the security exception
   * @throws IllegalAccessException the illegal access exception
   * @throws IllegalArgumentException the illegal argument exception
   * @throws InvocationTargetException the invocation target exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InvalidEntityException
   * @throws NoSuchFieldException
   */
  @Test
  void lookupComponentCacheEmptyLookupEmptyCompTest_ShouldResultInNull()
      throws Exception {

    final Map<MethodInterfaceDescription, IMyDataComponent> pipCache = (Map<MethodInterfaceDescription, IMyDataComponent>) getInternalState(this.connectorCache, "pipCache");
    pipCache.clear();

    when(this.pmpMock.lookupPip(any(SolutionId.class), eq(this.query))).thenReturn(Collections.emptySet());
    when(this.pmpMock.lookupPxp(any(SolutionId.class), eq(this.query))).thenReturn(Collections.emptySet());

    final Method lookupMethod = this.getMethodToTest();
    lookupMethod.setAccessible(true);
    final IPolicyInformationPoint lookupResult = this.invokeTestMethod(pipCache, lookupMethod);
    assertNull(lookupResult);
    verify(this.pmpMock, times(1)).lookupPip(any(SolutionId.class), any(MethodInterfaceDescription.class));
  }

  private IPolicyInformationPoint invokeTestMethod(Map<MethodInterfaceDescription, IMyDataComponent> pipCache, Method lookupMethod) throws IllegalAccessException, InvocationTargetException {
    return (IPolicyInformationPoint) lookupMethod.invoke(this.connectorCache, this.query, pipCache, new ReentrantReadWriteLock(), new SolutionId());
  }

  private Method getMethodToTest() throws NoSuchMethodException {
    return ConnectorCache.class.getDeclaredMethod("lookupComponent", MethodInterfaceDescription.class, Map.class, ReadWriteLock.class, SolutionId.class);
  }

  private Method getUpdatePip() throws NoSuchMethodException {
    return ConnectorCache.class.getDeclaredMethod("updatePipCache", PipComponentInformation.class);
  }

  /**
   * Lookup component null conn urls test should result in null.
   *
   * @throws NoSuchMethodException the no such method exception
   * @throws SecurityException the security exception
   * @throws IllegalAccessException the illegal access exception
   * @throws IllegalArgumentException the illegal argument exception
   * @throws InvocationTargetException the invocation target exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws URISyntaxException the URI syntax exception
   * @throws InvalidEntityException
   * @throws NoSuchFieldException
   */
  @Test
  void lookupComponentNullConnUrlsTest_ShouldResultInNull() throws Exception {

    final Map<MethodInterfaceDescription, IMyDataComponent> pipCache = (Map<MethodInterfaceDescription, IMyDataComponent>) getInternalState(this.connectorCache, "pipCache");
    pipCache.clear();

    when(this.pmpMock.lookupPip(any(SolutionId.class), eq(this.query))).thenReturn(Set.of(this.pipComponent));
    when(this.pmpMock.lookupPxp(any(SolutionId.class), eq(this.query))).thenReturn(Set.of(this.pxpComponent));

    when(this.pipComponent.getUrls()).thenReturn(null);
    when(this.pxpComponent.getUrls()).thenReturn(null);

    final Method lookupMethod = this.getMethodToTest();
    lookupMethod.setAccessible(true);
    final IPolicyInformationPoint lookupResult = this.invokeTestMethod(pipCache, lookupMethod);
    assertNull(lookupResult);
    verify(this.pmpMock, times(1)).lookupPip(any(SolutionId.class), any(MethodInterfaceDescription.class));
  }

  /**
   * Lookup component empty conn urls test should result in null.
   *
   * @throws NoSuchMethodException the no such method exception
   * @throws SecurityException the security exception
   * @throws IllegalAccessException the illegal access exception
   * @throws IllegalArgumentException the illegal argument exception
   * @throws InvocationTargetException the invocation target exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws URISyntaxException the URI syntax exception
   * @throws InvalidEntityException
   * @throws NoSuchFieldException
   */
  @Test
  void lookupComponentEmptyConnUrlsTest_ShouldResultInNull() throws Exception {

    final Map<MethodInterfaceDescription, IMyDataComponent> pipCache = (Map<MethodInterfaceDescription, IMyDataComponent>) getInternalState(this.connectorCache, "pipCache");
    pipCache.clear();

    when(this.pmpMock.lookupPip(any(SolutionId.class), eq(this.query))).thenReturn(Set.of(this.pipComponent));
    when(this.pmpMock.lookupPxp(any(SolutionId.class), eq(this.query))).thenReturn(Set.of(this.pxpComponent));

    when(this.pipComponent.getUrls()).thenReturn(Collections.emptyList());
    when(this.pxpComponent.getUrls()).thenReturn(Collections.emptyList());

    final Method lookupMethod = this.getMethodToTest();
    lookupMethod.setAccessible(true);
    final IPolicyInformationPoint lookupResult = this.invokeTestMethod(pipCache, lookupMethod);
    assertNull(lookupResult);
    verify(this.pmpMock, times(1)).lookupPip(any(SolutionId.class), any(MethodInterfaceDescription.class));
  }

  /**
   * Gets the PMP conn from cache test should result in PM pfrom cache.
   *
   * @throws Exception the exception
   */
  @Test
  void getPMPConnFromCacheTest_ShouldResultInPMPfromCache() throws Exception {
    final IConnectorCache mockCache = this.connectorCache;

    setInternalState(mockCache, "pmpCache", this.pmpMock);

    assertEquals(this.connectorCache.getPmpConnectionFromCache(), this.pmpMock);
    // Mockito.verifyPrivate(this.connectorCache, never()).invoke("lookupPMP",
    // new Object[0]);
  }

  /**
   * Gets the PMP conn via lookup test should result in PM pfrom lookup.
   *
   * @throws Exception the exception
   */
  @Test
  void getPMPConnViaLookupTest_ShouldResultInPMPfromLookup() throws Exception {
    final Object nullObject = null;
    setInternalState(this.connectorCache, "pmpCache", nullObject);
    Mockito.when(this.connectorFactory.getPmpClient(any())).thenReturn(this.pmpMock);
    Mockito.when(this.connectorFactory.getPmpClient(any(), any())).thenReturn(this.pmpMock);

    IBasicManagementService lookedUpPMP = (IBasicManagementService) getInternalState(this.connectorCache, "pmpCache");
    assertNull(lookedUpPMP);
    setInternalState(PolicyDecisionPoint.getInstance(), "pmpURI", new URI("http://localhost:8082/ws/pmp"));
    assertEquals(this.connectorCache.getPmpConnectionFromCache(), this.pmpMock);
    // Mockito.verifyPrivate(this.connectorCache, times(1)).invoke("lookupPMP");

    lookedUpPMP = (IBasicManagementService) getInternalState(this.connectorCache, "pmpCache");
    assertEquals(lookedUpPMP, this.pmpMock);
  }

  /**
   * Gets the PMP conn via lookup null test should result in null.
   *
   * @throws Exception the exception
   */
  @Test
  void getPMPConnViaLookupNullTest_ShouldResultInNull() throws Exception {
    final Object nullObject = null;
    setInternalState(this.connectorCache, "pmpCache", nullObject);

    IBasicManagementService lookedUpPMP = (IBasicManagementService) getInternalState(this.connectorCache, "pmpCache");
    assertNull(lookedUpPMP);

    final IBasicManagementService thisPMP = this.connectorCache.getPmpConnectionFromCache();
    assertNull(thisPMP);
    // Mockito.verifyPrivate(this.connectorCache, times(1)).invoke("lookupPMP",
    // new Object[0]);

    lookedUpPMP = (IBasicManagementService) getInternalState(this.connectorCache, "pmpCache");
    assertNull(lookedUpPMP);
  }

  /**
   * Lookup PMP test should result in PM pmock.
   *
   * @throws Exception the exception
   */
  @Test
  void lookupPMPTest_ShouldResultInPMPmock() throws Exception {
    when(this.connectorFactory.getPmpClient(any(URI.class))).thenReturn(this.pmpMock);
    when(this.connectorFactory.getPmpClient(any(URI.class), any(OAuthCredentials.class))).thenReturn(this.pmpMock);

    final Method method = ConnectorCache.class.getDeclaredMethod("lookupPMP");
    method.setAccessible(true);
    setInternalState(PolicyDecisionPoint.getInstance(), "pmpURI", new URI("http://localhost:8082/ws/pmp"));
    final IBasicManagementService lookedUpPMP = (IBasicManagementService) method.invoke(this.connectorCache);

    assertEquals(lookedUpPMP, this.pmpMock);
  }

  /**
   * Lookup PMP null test should result in null.
   *
   * @Ignore because it seems to be a useless Test
   * @throws Exception the exception
   */
  @Test
  void lookupPMPNullTest_ShouldResultInNull() throws Exception {
    setInternalState(PolicyDecisionPoint.getInstance(), "pmpURI", null);
    when(this.connectorFactory.getPmpClient(any(URI.class))).thenReturn(null);
    when(this.connectorFactory.getPmpClient(any(URI.class), any(OAuthCredentials.class))).thenReturn(null);

    final Method method = ConnectorCache.class.getDeclaredMethod("lookupPMP");
    method.setAccessible(true);

    final IBasicManagementService lookedUpPMP = (IBasicManagementService) method.invoke(this.connectorCache);
    assertNull(lookedUpPMP);
  }

  /**
   * Lookup PMP test the conditional call if oAuht is null !! Attention throws
   * org.mockito.exceptions.misusing.NotAMockException: Argument should be a
   * mock, but is: class java.lang.Class if the times value is not fulfilled
   *
   * @throws Exception the exception
   */
  @Test
  void lookupPMP_OauthNullTest() throws Exception {
    setInternalState(PolicyDecisionPoint.getInstance(), "pmpURI", new URI("http://localhost:8082/ws/pmp"));

    final Object nullObject = null;
    setInternalState(this.connectorCache, "oAuthCredentials", nullObject);
    final Method method = ConnectorCache.class.getDeclaredMethod("lookupPMP");
    method.setAccessible(true);
    method.invoke(this.connectorCache);

    Mockito.verify(connectorFactory, Mockito.times(1)).getPmpClient(any(URI.class));

  }

  /**
   * Lookup PMP test the conditional call if oAuht is not null !! Attention
   * throws org.mockito.exceptions.misusing.NotAMockException: Argument should
   * be a mock, but is: class java.lang.Class if the times value is not
   * fulfilled
   *
   * @throws Exception the exception
   */
  @Test
  void lookupPMP_OauthTest() throws Exception {
    setInternalState(PolicyDecisionPoint.getInstance(), "pmpURI", new URI("http://localhost:8082/ws/pmp"));

    final OAuthCredentials oAuthCredentials = (OAuthCredentials) ReflectionTestUtils.getField(this.connectorCache, "oAuthCredentials");
    assertNotNull(oAuthCredentials);
    final Method method = ConnectorCache.class.getDeclaredMethod("lookupPMP");
    method.setAccessible(true);
    method.invoke(this.connectorCache);

    Mockito.verify(connectorFactory, VerificationModeFactory.times(1)).getPmpClient(any(URI.class), any(OAuthCredentials.class));
  }

  public static void setInternalState(Object target, String field, Object value) {
    final Class<?> c = target.getClass();
    try {
      final Field f = getFieldFromHierarchy(c, field);
      f.setAccessible(true);
      f.set(target, value);
    } catch (final Exception e) {
      throw new RuntimeException("Unable to set internal state on a private field. Please report to mockito mailing list.", e);
    }
  }

  private static Field getFieldFromHierarchy(Class<?> clazz, String field) {
    Field f = getField(clazz, field);
    while (f == null && clazz != Object.class) {
      clazz = clazz.getSuperclass();
      f = getField(clazz, field);
    }
    if (f == null) {
      throw new RuntimeException("You want me to get this field: '" + field + "' on this class: '" + clazz.getSimpleName() + "' but this field is not declared within the hierarchy of this class!");
    }
    return f;
  }

  private static Field getField(Class<?> clazz, String field) {
    try {
      return clazz.getDeclaredField(field);
    } catch (final NoSuchFieldException e) {
      return null;
    }
  }

}
