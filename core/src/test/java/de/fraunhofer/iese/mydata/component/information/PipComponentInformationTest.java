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

package de.fraunhofer.iese.mydata.component.information;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.information.method.MethodInterfaceDescription;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

class PipComponentInformationTest {

  @Test
  void pipComponentWithValidMethodName() throws Exception {
    final PipComponentInformation componentInformation = new PipComponentInformation(
        new ComponentId("urn:component:test:pip:test"),
        Collections.singletonList(URI.create("http://localhost")), Collections.singletonList(
            new MethodInterfaceDescription("urn:info:test:test", String.class, "description")));
    MyDataEntity.validateAndNullCheck(componentInformation);
    // everything should be fine
  }

  @Test
  void pipComponentWithWrongComponentId() throws Exception {
    final PipComponentInformation componentInformation = new PipComponentInformation(
          new ComponentId("urn:component:test:pxp:test"),
          Collections.singletonList(URI.create("http://localhost")), Collections.singletonList(
              new MethodInterfaceDescription("urn:info:test:test", String.class, "description")));
    assertThrows(InvalidEntityException.class, () ->
      MyDataEntity.validateAndNullCheck(componentInformation));
  }

  @Test
  void pipComponentWithWrongSolutionMethod() throws Exception {
    final PipComponentInformation componentInformation = new PipComponentInformation(
          new ComponentId("urn:component:test:pip:test"),
          Collections.singletonList(URI.create("http://localhost")),
          Collections.singletonList(new MethodInterfaceDescription("urn:info:falsche-solution:test",
              String.class, "description")));
    assertThrows(InvalidEntityException.class, () ->
      MyDataEntity.validateAndNullCheck(componentInformation));
  }

  @Test
  void pipComponentWithInvalidMethodName() throws Exception {
    final PipComponentInformation componentInformation = new PipComponentInformation(
          new ComponentId("urn:component:test:pip:test"),
          Collections.singletonList(URI.create("http://localhost")), Collections.singletonList(
              new MethodInterfaceDescription("urn:mir-ist-das-egal", String.class, "description")));
    assertThrows(InvalidEntityException.class, () ->
      MyDataEntity.validateAndNullCheck(componentInformation));
  }

  @Test
  void getUrlsForProtocolOk() {
    final URI uri1 = URI.create("http://127.0.0.1");
    final URI uri2 = URI.create("http://localhost");
    final URI uri3 = URI.create("https://localhost");

    final PipComponentInformation componentInformation = new PipComponentInformation(
        new ComponentId("urn:component:test:pip:test"), Arrays.asList(uri1, uri2, uri3),
        Collections.singletonList(
            new MethodInterfaceDescription("urn:info:test:test", String.class, "description")));

    assertEquals(3, componentInformation.getUrlsForProtocol("http", "https").size());
    assertEquals(3, componentInformation.getUrlsForProtocol().size());
    assertTrue(componentInformation.getUrlsForProtocol("https").contains(uri3));
    assertTrue(componentInformation.getUrlsForProtocol("http").contains(uri1));
    assertTrue(componentInformation.getUrlsForProtocol("http").contains(uri2));
    assertFalse(componentInformation.getUrlsForProtocol("http").contains(uri3));
  }

  @Test
  void whenSameComponentIdThenEqualsAndHashCodeOk() {
    final PipComponentInformation componentInformation1 = new PipComponentInformation(
        new ComponentId("urn:component:test:pip:test"),
        Collections.singletonList(URI.create("http://localhost")), Collections.singletonList(
            new MethodInterfaceDescription("urn:info:test:test", String.class, "description")));

    final PipComponentInformation componentInformation2 = new PipComponentInformation(
        new ComponentId("urn:component:test:pip:test"),
        Collections.singletonList(URI.create("http://localhost")), Collections.singletonList(
            new MethodInterfaceDescription("urn:info:test:test", String.class, "description")));

    assertEquals(componentInformation1, componentInformation2);
    assertEquals(componentInformation1.hashCode(), componentInformation2.hashCode());
  }

  @Test
  void whenDifferentComponentIdThenEqualsAndHashCodeFail() {
    final PipComponentInformation componentInformation1 = new PipComponentInformation(
        new ComponentId("urn:component:test:pip:test1"),
        Collections.singletonList(URI.create("http://localhost")), Collections.singletonList(
            new MethodInterfaceDescription("urn:info:test:test", String.class, "description")));

    final PipComponentInformation componentInformation2 = new PipComponentInformation(
        new ComponentId("urn:component:test:pip:test2"),
        Collections.singletonList(URI.create("http://localhost")), Collections.singletonList(
            new MethodInterfaceDescription("urn:info:test:test", String.class, "description")));

    assertNotEquals(componentInformation1, componentInformation2);
    assertNotSame(componentInformation1.hashCode(), componentInformation2.hashCode());
  }

}
