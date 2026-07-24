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
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

class ManagementServiceComponentInformationTest {

  @Test
  void msComponentWithValidComponentId() throws Exception {
    final ManagementServiceComponentInformation componentInformation = new ManagementServiceComponentInformation(
        new ComponentId("urn:component:test:ms:test"),
        Collections.singletonList(URI.create("http://localhost")));
    MyDataEntity.validateAndNullCheck(componentInformation);
  }

  @Test
  void msComponentWithWrongComponentId() throws Exception {
    final ManagementServiceComponentInformation componentInformation = new ManagementServiceComponentInformation(
          new ComponentId("urn:component:test:pdp:test"),
          Collections.singletonList(URI.create("http://localhost")));
    assertThrows(InvalidEntityException.class, () ->
      MyDataEntity.validateAndNullCheck(componentInformation));
  }

  @Test
  void msComponentWithNullComponentId() throws Exception {
    assertThrows(IllegalArgumentException.class, () -> {
      new ManagementServiceComponentInformation(null,
          Collections.singletonList(URI.create("http://localhost")));
    });
  }

  @Test
  void whenAddUrlThenOk() {
    final ManagementServiceComponentInformation componentInformation = new ManagementServiceComponentInformation(
        new ComponentId("urn:component:test:ms:test"));

    final URI uri = URI.create("https://localhost");
    componentInformation.addUrl(uri);

    assertEquals(1, componentInformation.getUrls().size());
    assertTrue(componentInformation.getUrls().contains(uri));
  }

  @Test
  void whenAddNullUrlThenThrowException() {
    final ManagementServiceComponentInformation componentInformation = new ManagementServiceComponentInformation(
          new ComponentId("urn:component:test:ms:test"));
    assertThrows(IllegalArgumentException.class, () ->

      componentInformation.addUrl(null));
  }

  @Test
  void getUrlsForProtocolOk() {
    final URI uri1 = URI.create("http://127.0.0.1");
    final URI uri2 = URI.create("http://localhost");
    final URI uri3 = URI.create("https://localhost");

    final ManagementServiceComponentInformation componentInformation = new ManagementServiceComponentInformation(
        new ComponentId("urn:component:test:ms:test"), Arrays.asList(uri1, uri2, uri3));

    assertEquals(3, componentInformation.getUrlsForProtocol("http", "https").size());
    assertEquals(3, componentInformation.getUrlsForProtocol().size());
    assertTrue(componentInformation.getUrlsForProtocol("https").contains(uri3));
    assertTrue(componentInformation.getUrlsForProtocol("http").contains(uri1));
    assertTrue(componentInformation.getUrlsForProtocol("http").contains(uri2));
    assertFalse(componentInformation.getUrlsForProtocol("http").contains(uri3));
  }

  @Test
  void whenSetUrlsThenOldUrlsAreReplaced() {
    final URI uri1 = URI.create("http://127.0.0.1");
    final URI uri2 = URI.create("http://localhost");
    final URI uri3 = URI.create("https://localhost");

    final ManagementServiceComponentInformation componentInformation = new ManagementServiceComponentInformation(
        new ComponentId("urn:component:test:ms:test"));

    componentInformation.addUrl(uri1);
    componentInformation.addUrl(uri2);
    componentInformation.addUrl(uri3);

    final URI uri4 = URI.create("http://management-service.local");
    final URI uri5 = URI.create("https://management-service.local");

    componentInformation.setUrls(Arrays.asList(uri4, uri5));

    assertTrue(componentInformation.getUrls().contains(uri4));
    assertTrue(componentInformation.getUrls().contains(uri5));
    assertFalse(componentInformation.getUrls().contains(uri1));
    assertFalse(componentInformation.getUrls().contains(uri2));
    assertFalse(componentInformation.getUrls().contains(uri3));
  }

  @Test
  void setUrls_whenUrlsAreNull_thenThrowException() {
    final ManagementServiceComponentInformation componentInformation = new ManagementServiceComponentInformation(
          new ComponentId("urn:component:test:ms:test"),
          Collections.singletonList(URI.create("http://localhost")));
    assertThrows(IllegalArgumentException.class, () ->

      componentInformation.setUrls(null));
  }

  @Test
  void whenSameComponentIdThenEqualsAndHashCodeOk() {
    final ManagementServiceComponentInformation componentInformation1 = new ManagementServiceComponentInformation(
        new ComponentId("urn:component:test:pmp:test"),
        Collections.singletonList(URI.create("http://localhost")));
    final ManagementServiceComponentInformation componentInformation2 = new ManagementServiceComponentInformation(
        new ComponentId("urn:component:test:pmp:test"),
        Collections.singletonList(URI.create("https://localhost")));

    assertEquals(componentInformation1, componentInformation2);
    assertEquals(componentInformation1.hashCode(), componentInformation2.hashCode());
  }

  @Test
  void whenDifferentComponentIdthenEqualsAndHashCodeFail() {
    final ManagementServiceComponentInformation componentInformation1 = new ManagementServiceComponentInformation(
        new ComponentId("urn:component:test:pmp:test1"),
        Collections.singletonList(URI.create("http://localhost")));
    final ManagementServiceComponentInformation componentInformation2 = new ManagementServiceComponentInformation(
        new ComponentId("urn:component:test:pmp:test2"),
        Collections.singletonList(URI.create("https://localhost")));

    assertNotEquals(componentInformation1, componentInformation2);
    assertNotSame(componentInformation1.hashCode(), componentInformation2.hashCode());
  }

}
