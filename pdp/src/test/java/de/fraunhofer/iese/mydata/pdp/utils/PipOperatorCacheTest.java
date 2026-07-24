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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import de.fraunhofer.iese.mydata.component.information.method.MethodInterfaceDescription;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyInformationPoint;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.pdp.PolicyDecisionPoint;
import de.fraunhofer.iese.mydata.policy.PipRequest;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.event.InfoId;
import de.fraunhofer.iese.mydata.policy.exception.InformationUndeterminableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * The Class PipOperatorCacheTest.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PipOperatorCacheTest {

  private static ConnectorCache originalConnectorCache;

  /**
   * The pip op cache.
   */
  private PipOperatorCache pipOpCache;

  /**
   * The data object.
   */
  private final DataObject dataObject = new DataObject<>(true);

  /**
   * The pip query.
   */
  private final MethodInterfaceDescription pipQuery = new MethodInterfaceDescription("method", Boolean.class, "");

  /**
   * The pip cache mock.
   */
  private final ConcurrentHashMap<MethodInterfaceDescription, Object> pipCache = new ConcurrentHashMap<>(10);

  /**
   * The connector cache mock.
   */
  @Mock
  private ConnectorCache connectorCacheMock;

  /**
   * The pip mock.
   */
  @Mock
  private IPolicyInformationPoint pipMock;

  /**
   * The inner class.
   */
  private Object innerClass;

  @BeforeAll
  static void before() {
    originalConnectorCache = PolicyDecisionPoint.getInstance().getConnectorCache();
  }

  @AfterAll
  static void after() {
    setInternalState(PolicyDecisionPoint.getInstance(), "connectorCache", originalConnectorCache);
  }

  private static Object getInternalState(Object o, String string) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    if (o instanceof Class<?>) {
      final Class<?> c = (Class<?>) o;
      final Field f = c.getDeclaredField(string);
      f.setAccessible(true);
      return f.get(null);
    } else {
      final Field f = o.getClass().getDeclaredField(string);
      f.setAccessible(true);
      return f.get(o);
    }
  }

  private static void setInternalState(Object target, String field, Object value) {
    if (target instanceof Class<?>) {
      final Class<?> c = (Class<?>) target;
      try {
        final Field f = getFieldFromHierarchy(c, field);
        f.setAccessible(true);
        f.set(null, value);
      } catch (final Exception e) {
        throw new RuntimeException("Unable to set internal state on a private field.", e);
      }
    } else {
      final Class<?> c = target.getClass();
      try {
        final Field f = getFieldFromHierarchy(c, field);
        f.setAccessible(true);
        f.set(target, value);
      } catch (final Exception e) {
        throw new RuntimeException("Unable to set internal state on a private field.", e);
      }
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

  @BeforeEach
  void setUp() throws Exception {
    this.pipOpCache = new PipOperatorCache(connectorCacheMock);
//    setInternalState(PolicyDecisionPoint.getInstance(), "connectorCache", connectorCacheMock);
//    setInternalState(PolicyDecisionPoint.getInstance(), "pipOperatorCache", pipOpCache);
    final Field field = PipOperatorCache.class.getDeclaredField("pipCache");
    field.setAccessible(true);
    field.set(this.pipOpCache, this.pipCache);
//    final Class clazz = Whitebox.getInnerClassType(PipOperatorCache.class, "PipResponseDetail");
//    final Constructor<?> constructor = Whitebox.getConstructor(clazz, PipOperatorCache.class, DataObject.class, long.class);
//    constructor.setAccessible(true);
//    this.innerClass = constructor.newInstance(PipOperatorCache.getCacheInstance(), this.dataObject, 0);
  }

  @AfterEach
  void cleanup() {
    this.pipOpCache.clearCache();
  }

  /**
   * Cleanup task test should result in method call.
   *
   * @throws Exception
   */
  @Test
  void cleanupTaskTest_ShouldResultInMethodCall() throws Exception {
    final Event event = new Event(new ActionId("urn:action:test:demo"), new Parameter[0]);
    final PipRequest pipRequest = new PipRequest(new InfoId("urn:info:test:demoMethod"), new Parameter[0]);
    Mockito.when(this.connectorCacheMock.getPipConnectionFromCache(any(MethodInterfaceDescription.class), eq(SolutionId.fromActionId(event.getActionId())))).thenReturn(this.pipMock);
    Mockito.when(this.pipMock.evaluate(eq(pipRequest))).thenReturn(this.dataObject);

    // Result must be deleted
    assertEquals(this.dataObject, this.pipOpCache.getEvalResultFromCache(5000, this.pipQuery, pipRequest, SolutionId.fromActionId(event.getActionId())));
    Mockito.verify(this.pipMock, times(1)).evaluate(eq(pipRequest));
    assertEquals(1, this.pipCache.size());

    Thread.sleep(2000);

    ScheduledFuture<?> cleanupTask = (ScheduledFuture<?>) getInternalState(pipOpCache, "scheduledCleanupTask");

    Thread.sleep(6000);

    assertEquals(0, this.pipCache.size());
    Mockito.verify(this.pipMock, times(1)).evaluate(eq(pipRequest));
    assertTrue(cleanupTask.isDone()); // Cache is empty, thread will be
    // removed
    assertTrue(cleanupTask.isCancelled());

    cleanupTask = (ScheduledFuture<?>) getInternalState(pipOpCache, "scheduledCleanupTask");
    assertNull(cleanupTask);

    // Result must not be deleted
    assertEquals(this.dataObject, this.pipOpCache.getEvalResultFromCache(20000, this.pipQuery, pipRequest, SolutionId.fromActionId(event.getActionId())));
    Mockito.verify(this.pipMock, times(2)).evaluate(eq(pipRequest));
    assertEquals(1, this.pipCache.size());
    Thread.sleep(2000);

    cleanupTask = (ScheduledFuture<?>) getInternalState(pipOpCache, "scheduledCleanupTask");
    assertFalse(cleanupTask.isDone());
    assertFalse(cleanupTask.isCancelled());

    Thread.sleep(16000);

    assertEquals(1, this.pipCache.size());
    Mockito.verify(this.pipMock, times(2)).evaluate(eq(pipRequest));
    assertFalse(cleanupTask.isDone());
    assertFalse(cleanupTask.isCancelled());

    Thread.sleep(4000);

    // Should be deleted on the second run
    assertEquals(0, this.pipCache.size());
    Mockito.verify(this.pipMock, times(2)).evaluate(eq(pipRequest));
  }

  /**
   * Clear cache test should result in empty cache.
   *
   * @throws Exception
   */
  @Test
  void clearCacheTest_ShouldResultInEmptyCache() throws Exception {
    final Event event = new Event(new ActionId("urn:action:test:demo"), new Parameter[0]);
    final PipRequest pipRequest = new PipRequest(new InfoId("urn:info:test:demoMethod"), new Parameter[0]);
    Mockito.when(this.connectorCacheMock.getPipConnectionFromCache(any(MethodInterfaceDescription.class), eq(SolutionId.fromActionId(event.getActionId())))).thenReturn(this.pipMock);
    Mockito.when(this.pipMock.evaluate(eq(pipRequest))).thenReturn(this.dataObject);

    assertEquals(this.dataObject, this.pipOpCache.getEvalResultFromCache(100000, this.pipQuery, pipRequest, SolutionId.fromActionId(event.getActionId())));
    Mockito.verify(this.pipMock, times(1)).evaluate(eq(pipRequest));
    assertEquals(1, this.pipCache.size());

    pipOpCache.clearCache();

    assertEquals(0, this.pipCache.size());
    final ScheduledFuture<?> cleanupTask = (ScheduledFuture<?>) getInternalState(pipOpCache, "scheduledCleanupTask");
    assertNull(cleanupTask);
  }

  /**
   * Gets the eval result from cache null eval result test should result in null
   * data object.
   * <p>
   * the eval result from cache null eval result test should result in null data
   * object
   *
   * @throws Exception
   */
  @Test
  void getEvalResultFromCacheNullEvalResultTest_ShouldResultInNullDataObject() throws Exception {
    final Event event = new Event(new ActionId("urn:action:test:demo"), new Parameter[0]);
    final PipRequest pipRequest = new PipRequest(new InfoId("urn:info:test:demoMethod"), new Parameter[0]);
    Mockito.when(this.connectorCacheMock.getPipConnectionFromCache(any(MethodInterfaceDescription.class), eq(SolutionId.fromActionId(event.getActionId())))).thenReturn(this.pipMock);
    Mockito.when(this.pipMock.evaluate(eq(pipRequest))).thenReturn(this.dataObject);

    assertEquals(this.dataObject, this.pipOpCache.getEvalResultFromCache(1000, this.pipQuery, pipRequest, SolutionId.fromActionId(event.getActionId())));
    Mockito.verify(this.pipMock, times(1)).evaluate(eq(pipRequest));

    Thread.sleep(2000);

    Mockito.when(this.pipMock.evaluate(eq(pipRequest))).thenReturn(null);

    assertNull(this.pipOpCache.getEvalResultFromCache(1000, this.pipQuery, pipRequest, SolutionId.fromActionId(event.getActionId())));
    assertEquals(0, this.pipCache.size());
    Mockito.verify(this.pipMock, times(2)).evaluate(eq(pipRequest));

    assertNull(this.pipOpCache.getEvalResultFromCache(1000, this.pipQuery, pipRequest, SolutionId.fromActionId(event.getActionId())));
    assertEquals(0, this.pipCache.size());
    Mockito.verify(this.pipMock, times(3)).evaluate(eq(pipRequest));
  }

  /**
   * Gets the eval result from cache test should result in cached data object.
   *
   * @throws Exception the exception
   */
  @Test
  void getEvalResultFromCacheTest_ShouldResultInCachedDataObject() throws Exception {
    final Event event = new Event(new ActionId("urn:action:test:demo"), new Parameter[0]);
    final PipRequest pipRequest = new PipRequest(new InfoId("urn:info:demo:demoMethod"), new Parameter[0]);
    Mockito.when(this.connectorCacheMock.getPipConnectionFromCache(any(MethodInterfaceDescription.class), eq(SolutionId.fromActionId(event.getActionId())))).thenReturn(this.pipMock);
    Mockito.when(this.pipMock.evaluate(eq(pipRequest))).thenReturn(this.dataObject);

    assertEquals(this.dataObject, this.pipOpCache.getEvalResultFromCache(10000, this.pipQuery, pipRequest, SolutionId.fromActionId(event.getActionId())));
    Mockito.verify(this.pipMock, times(1)).evaluate(eq(pipRequest));

    assertEquals(this.dataObject, this.pipOpCache.getEvalResultFromCache(10000, this.pipQuery, pipRequest, SolutionId.fromActionId(event.getActionId())));
    Mockito.verify(this.pipMock, times(1)).evaluate(eq(pipRequest));
  }

  @Test
  void cacheIndexDifferentiatesViaPipRequestParameters() throws Exception {
    final SolutionId solutionId = new SolutionId("urn:solution:demo");
    PipRequest pipRequest1 = new PipRequest(new InfoId("urn:info:demo:demoMethod"), new Parameter<>("name", "Peter"));
    Mockito.when(this.connectorCacheMock.getPipConnectionFromCache(any(MethodInterfaceDescription.class), eq(solutionId))).thenReturn(this.pipMock);
    final DataObject dataObject1 = new DataObject<>("test1");
    Mockito.when(this.pipMock.evaluate(eq(pipRequest1))).thenReturn(dataObject1);

    assertEquals(dataObject1, this.pipOpCache.getEvalResultFromCache(10_000, this.pipQuery, pipRequest1, solutionId));
    Mockito.verify(this.pipMock, times(1)).evaluate(eq(pipRequest1));

    PipRequest pipRequest2 = new PipRequest(new InfoId("urn:info:demo:demoMethod"), new Parameter<>("name", "Paul"));
    final DataObject dataObject2 = new DataObject<>("test2");
    Mockito.when(this.pipMock.evaluate(eq(pipRequest2))).thenReturn(dataObject2);

    assertEquals(dataObject2, this.pipOpCache.getEvalResultFromCache(10_000, this.pipQuery, pipRequest2, solutionId));
    Mockito.verify(this.pipMock, times(1)).evaluate(eq(pipRequest2));

    assertEquals(dataObject1, this.pipOpCache.getEvalResultFromCache(10_000, this.pipQuery, pipRequest1, solutionId));
    Mockito.verify(this.pipMock, times(1)).evaluate(eq(pipRequest1));
  }

  /**
   * Gets the not up to date eval result from cache test should result in
   * reevaluated data object.
   * <p>
   * the not up to date eval result from cache test should result in reevaluated
   * data object
   *
   * @throws Exception
   */
  @Test
  void getNotUpToDateEvalResultFromCacheTest_ShouldResultInReevaluatedDataObject() throws Exception {
    final Event event = new Event(new ActionId("urn:action:test:demo"), new Parameter[0]);
    final PipRequest pipRequest = new PipRequest(new InfoId("urn:info:test:demoMethod"), new Parameter[0]);
    Mockito.when(this.connectorCacheMock.getPipConnectionFromCache(any(MethodInterfaceDescription.class), eq(SolutionId.fromActionId(event.getActionId())))).thenReturn(this.pipMock);
    Mockito.when(this.pipMock.evaluate(eq(pipRequest))).thenReturn(this.dataObject);

    assertEquals(this.dataObject, this.pipOpCache.getEvalResultFromCache(2000, this.pipQuery, pipRequest, SolutionId.fromActionId(event.getActionId())));
    Mockito.verify(this.pipMock, times(1)).evaluate(eq(pipRequest));

    Thread.sleep(3000);

    assertEquals(this.dataObject, this.pipOpCache.getEvalResultFromCache(10000, this.pipQuery, pipRequest, SolutionId.fromActionId(event.getActionId())));
    Mockito.verify(this.pipMock, times(2)).evaluate(eq(pipRequest));
  }

  /**
   * Reevaluate exception test should result in IO exception.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InformationUndeterminableException the information undeterminable
   *           exception
   * @throws InvalidEntityException
   */
  @Test
  void reevaluateExceptionTest_ShouldResultInIOException() throws Exception {
    final Event event = new Event(new ActionId("urn:action:test:demo"), new Parameter[0]);
    final PipRequest pipRequest = new PipRequest(new InfoId("urn:info:demo:demoMethod"), new Parameter[0]);
    Mockito.when(this.connectorCacheMock.getPipConnectionFromCache(any(MethodInterfaceDescription.class), eq(SolutionId.fromActionId(event.getActionId())))).thenReturn(null);
    Mockito.when(this.pipMock.evaluate(eq(pipRequest))).thenReturn(this.dataObject);

    try {
      assertEquals(this.dataObject, this.pipOpCache.getEvalResultFromCache(10000, this.pipQuery, pipRequest, SolutionId.fromActionId(event.getActionId())));
      Mockito.verify(this.pipMock, never()).evaluate(eq(pipRequest));
      fail("No exception has been thrown.");
    } catch (final IOException e) {
      assertEquals("Error during PIP lookup (null)", e.getMessage());
    }

    Mockito.when(this.connectorCacheMock.getPipConnectionFromCache(any(MethodInterfaceDescription.class), eq(SolutionId.fromActionId(event.getActionId())))).thenReturn(this.pipMock);
    Mockito.when(this.pipMock.evaluate(eq(pipRequest))).thenThrow(new IOException("IOException"));

    try {
      assertEquals(this.dataObject, this.pipOpCache.getEvalResultFromCache(10000, this.pipQuery, pipRequest, SolutionId.fromActionId(event.getActionId())));
      Mockito.verify(this.pipMock, never()).evaluate(eq(pipRequest));
      fail("No exception has been thrown.");
    } catch (final IOException e) {
      assertEquals("IOException", e.getMessage());
    }

    Mockito.when(this.pipMock.evaluate(eq(pipRequest))).thenThrow(new InformationUndeterminableException("InformationUndeterminableException"));
    try {
      assertEquals(this.dataObject, this.pipOpCache.getEvalResultFromCache(10000, this.pipQuery, pipRequest, SolutionId.fromActionId(event.getActionId())));
      Mockito.verify(this.pipMock, never()).evaluate(eq(pipRequest));
      fail("No exception has been thrown.");
    } catch (final InformationUndeterminableException e) {
      assertEquals("InformationUndeterminableException", e.getMessage());
    }
  }

  /**
   * Update scheduler interval test should result in method call.
   *
   * @throws Exception
   */
  @Test
  void updateSchedulerIntervalTest_ShouldResultInMethodCall() throws Exception {
    final Event event = new Event(new ActionId("urn:action:test:demo"), new Parameter[0]);
    final PipRequest pipRequest = new PipRequest(new InfoId("urn:info:test:demoMethod"), new Parameter[0]);
    Mockito.when(this.connectorCacheMock.getPipConnectionFromCache(any(MethodInterfaceDescription.class), eq(SolutionId.fromActionId(event.getActionId())))).thenReturn(this.pipMock);
    Mockito.when(this.pipMock.evaluate(eq(pipRequest))).thenReturn(this.dataObject);

    // Result must be deleted
    assertEquals(this.dataObject, this.pipOpCache.getEvalResultFromCache(7000, this.pipQuery, pipRequest, SolutionId.fromActionId(event.getActionId())));
    Mockito.verify(this.pipMock, times(1)).evaluate(eq(pipRequest));
    assertEquals(1, this.pipCache.size());

    Thread.sleep(2000);

    final long ttl = (long) getInternalState(pipOpCache, "currentTTL");
    final ScheduledFuture<?> cleanupTask = (ScheduledFuture<?>) getInternalState(pipOpCache, "scheduledCleanupTask");

    assertEquals(this.dataObject, this.pipOpCache.getEvalResultFromCache(2000, this.pipQuery, pipRequest, SolutionId.fromActionId(event.getActionId())));
    Mockito.verify(this.pipMock, times(1)).evaluate(eq(pipRequest));
    assertEquals(1, this.pipCache.size());

    Thread.sleep(2500);

    final long ttlAfterUpdate = (long) getInternalState(pipOpCache, "currentTTL");
    final ScheduledFuture<?> cleanupTaskAfterUpdate = (ScheduledFuture<?>) getInternalState(pipOpCache, "scheduledCleanupTask");

    assertTrue(ttlAfterUpdate <= ttl);
    if (cleanupTask != null) {
      assertNotSame(cleanupTask, cleanupTaskAfterUpdate);
      assertTrue(cleanupTask.isCancelled());

    }
    Thread.sleep(2000);
    assertFalse(cleanupTaskAfterUpdate.isDone());
  }
}
