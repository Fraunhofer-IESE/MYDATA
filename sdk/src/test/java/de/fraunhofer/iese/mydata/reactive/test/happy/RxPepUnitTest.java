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

package de.fraunhofer.iese.mydata.reactive.test.happy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import de.fraunhofer.iese.mydata.JavaNonParameterizedType;
import de.fraunhofer.iese.mydata.User;
import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.information.PepComponentInformation;
import de.fraunhofer.iese.mydata.component.information.method.MethodInterfaceDescription;
import de.fraunhofer.iese.mydata.component.interfaces.IBasicManagementService;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyDecisionPoint;
import de.fraunhofer.iese.mydata.exception.ConflictingResourceException;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.exception.ResourceUpdateException;
import de.fraunhofer.iese.mydata.internal.IMyDataEnvironmentFullFace;
import de.fraunhofer.iese.mydata.pep.MockedPdpPmp;
import de.fraunhofer.iese.mydata.pep.PolicyEnforcementPoint;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.reactive.RxPepFactory;
import de.fraunhofer.iese.mydata.reactive.common.RxPep;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import io.reactivex.rxjava3.core.Observable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class RxPepUnitTest {

  private static Logger log = LoggerFactory.getLogger(PolicyEnforcementPoint.class);

  private RxPep<MyRxPepDocumentationAPI> rxPep;

  private MyRxPepDocumentationAPI rxMyPepInterface;

  @BeforeEach
  void init() throws IOException, URISyntaxException, IllegalArgumentException,
      EvaluationUndecidableException, NoSuchEntityException, ConflictingResourceException,
      ResourceUpdateException, InvalidEntityException {
    final IMyDataEnvironmentFullFace mockedMyDataEnvironment = Mockito
        .mock(IMyDataEnvironmentFullFace.class);
    final IBasicManagementService ipmp = MockedPdpPmp.mockedPMP();
    final IPolicyDecisionPoint ipdp = MockedPdpPmp.mockedPDP();
    doReturn(ipmp).when(mockedMyDataEnvironment).getPmp();
    doReturn(Optional.of(ipdp)).when(mockedMyDataEnvironment).getPdp();
    doReturn(new SolutionId("urn:solution:solution1")).when(mockedMyDataEnvironment)
        .getSolutionId();

    when(mockedMyDataEnvironment.registerPep(any(PepComponentInformation.class)))
        .thenReturn(new ComponentId("urn:component:solution1:pep:123"));
    // initialize rxPep

    this.rxPep = RxPepFactory.createRxPep(mockedMyDataEnvironment, MyRxPepDocumentationAPI.class);
    this.rxPep.doRegisterAtPMP().blockingSubscribe((b) -> {
      // on next observable so only one object
      assertTrue(b);
      this.rxMyPepInterface = this.rxPep.createInstanceAPI();
    });
  }

  private User getUser() {
    final User user = new User();
    user.setName("Hans Maier");
    user.setPhoneNo(new Long[] {
        4915234767022L, 919748087957L
    });
    final Map<String, User.CreditCardInfo> accountDetails = new HashMap<>();
    accountDetails.put("1234-5678-9101-1121", new User.CreditCardInfo("1111", "STADPARKASSE"));
    accountDetails.put("3141-5161-7181-9202", new User.CreditCardInfo("2222", "Commercez"));
    user.setAccountDetails(accountDetails);

    return user;
  }

  @Test
  void ModifirAnnotationWithClasses() throws IllegalAccessException {
    final PolicyEnforcementPoint pep = (PolicyEnforcementPoint) this.rxPep
        .getPolicyEnforcementPoint();
    final Class<?> secretClass = pep.getClass();
    final Field[] fields = secretClass.getDeclaredFields();

    for (final Field field : fields) {
      if (field.getName().equalsIgnoreCase("methodInterfaceDescriptions")) {
        // access to private fields of Pep
        field.setAccessible(true);
        final List<MethodInterfaceDescription> modifierInterfaceDescriptions = (List<MethodInterfaceDescription>) field
            .get(pep);
        // Append, Replace, Anagram, Delete
        assertEquals(4, modifierInterfaceDescriptions.size());
      }
    }
  }

  @Test
  void testOnlyUserWithEventObservable() {
    this.rxMyPepInterface.enforceForCSProjectShow(this.getUser()).blockingSubscribe((event) -> {
      final User user = (User) event.getValueForName("user");
      assertEquals("Mr. Hans Maier", user.getName());
      assertEquals(1, user.getPhoneNo().length);
      // assertEquals("xxxx",
      // user.getAccountDetails().get("1234-5678-9101-1121").getPin());
      log.info(event.toJson(false));
    }, (throwable) -> log
        .info("Error occurred while processing enforceForCSProjectShow" + throwable.getMessage()));
  }

  @Test
  void testOnlyUserWithAuthorizationDecisionObservable() {
    this.rxMyPepInterface.enforceForCSProjectDontShow(this.getUser())
        .blockingSubscribe(eventAuthorizationDecisionPair -> {
          try {
            RxPepUnitTest.this.rxPep.getPolicyEnforcementPoint().enforceDecision(
                eventAuthorizationDecisionPair.getKey(), eventAuthorizationDecisionPair.getValue());
            final User user = (User) eventAuthorizationDecisionPair.getKey()
                .getValueForName("user");
            assertEquals("Mr. Hans Maier", user.getName());
            assertEquals(1, user.getPhoneNo().length);
            // assertEquals("xxxx",
            // user.getAccountDetails().get("1234-5678-9101-1121").getPin());
            log.info(eventAuthorizationDecisionPair.getKey().toJson(false));
          } catch (final Exception e) {
            log.info("Error occurred while processing enforceForCSProjectDontShow: {}",
                e.getMessage(), e);
          }
        }, throwable -> log.info("Error occured while processing enforceForCSProjectDontShow: {}",
            throwable.getMessage(), throwable));

  }

  @Test
  void testOnlyUserWithEventObservableContextProject() {
    this.rxMyPepInterface.enforceForCSProjectShow(this.getUser()).blockingSubscribe(event -> {
      final User user = (User) event.getValueForName("user");
      assertEquals("Mr. Hans Maier", user.getName());
      assertEquals(1, user.getPhoneNo().length);
      // assertEquals("xxxx",
      // user.getAccountDetails().get("1234-5678-9101-1121").getPin());
      log.info(event.toJson(false));
    }, throwable -> log
        .info("Error occurred while processing enforceForCSProjectShow" + throwable.getMessage()));
  }

  @Test
  void testNonParameterizedTypeInherit() {
    final JavaNonParameterizedType javaNonParameterizedType = new JavaNonParameterizedType();
    javaNonParameterizedType.add("Foo");
    javaNonParameterizedType.add("bar");
    final Observable<Event> eventObservable = this.rxMyPepInterface
        .enforceNonParameterizedObjectInherit(javaNonParameterizedType);
    eventObservable.blockingSubscribe(event -> {
      assertEquals(Arrays.asList("Foo", "bar"), event.getValueForName("nonParameterizedObject"));
      assertEquals(javaNonParameterizedType, event.getValueForName("nonParameterizedObject"));
      if (!(event.getValueForName("nonParameterizedObject") instanceof JavaNonParameterizedType)) {
        fail("Parameter value is not instanceof JavaNonParameterizedType");
      }
    }, throwable -> fail());
  }

  @Test
  public void testNonParameterizedType() {
    final ArrayList<String> someArrayList = new ArrayList<>();
    someArrayList.add("Foo");
    someArrayList.add("bar");
    final Observable<Event> eventObservable = this.rxMyPepInterface
        .enforceNonParameterizedObject(someArrayList);
    eventObservable.blockingSubscribe(event -> {
      assertEquals(Arrays.asList("Foo", "bar"), event.getValueForName("nonParameterizedObject2"));
      assertEquals(someArrayList, event.getValueForName("nonParameterizedObject2"));
    }, throwable -> fail());
  }

  @Test
  public void testParameterizedTypeToNonParameterizedTypeParameter() {
    final ArrayList<String> someArrayList = new ArrayList<>();
    someArrayList.add("Foo");
    someArrayList.add("bar");
    final Observable<Event> eventObservable = this.rxMyPepInterface
        .enforceNonParameterizedObject(someArrayList);
    eventObservable.blockingSubscribe(event -> {
      assertEquals(Arrays.asList("Foo", "bar"), event.getValueForName("nonParameterizedObject2"));
      assertEquals(someArrayList, event.getValueForName("nonParameterizedObject2"));
      final ArrayList<String> theList = (ArrayList<String>) event
          .getValueForName("nonParameterizedObject2");
      final String firstElement = theList.iterator().next();
      if (!(firstElement.startsWith("Foo"))) {
        fail();
      }
      final ArrayList<String> hopefullyTheSameList = event
          .getParameterValue("nonParameterizedObject2", someArrayList.getClass());
      final String hopefullyTheSameFirstElement = hopefullyTheSameList.get(0);
      if (!(hopefullyTheSameFirstElement.startsWith("Foo"))) {
        fail();
      }
    }, throwable -> fail());
  }

}
