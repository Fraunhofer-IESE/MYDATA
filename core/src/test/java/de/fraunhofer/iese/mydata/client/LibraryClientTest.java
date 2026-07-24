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

package de.fraunhofer.iese.mydata.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;

import org.junit.jupiter.api.Test;

class LibraryClientTest {

  @Test
  void whenEmptyThenFail() {
    final LibraryClient client = new LibraryClient();
    assertThrows(InvalidEntityException.class, () ->

      MyDataEntity.validate(client));
  }

  @Test
  void whenInvalidIdThenFail() {
    final LibraryClient client = new LibraryClient();
    client.setClientId(new ClientId("urn:sol:test"));
    assertThrows(InvalidEntityException.class, () ->

      MyDataEntity.validate(client));
  }

  @Test
  void whenMandatoryThenOk() throws Exception {
    final LibraryClient client = new LibraryClient();
    client.setClientId(new ClientId("urn:client:solution:test"));

    MyDataEntity.validate(client);
  }

  @Test
  void whenNewWithClientIdThenOk() throws Exception {
    final LibraryClient client = new LibraryClient(new ClientId("urn:client:solution:test"));

    MyDataEntity.validate(client);
  }

  @Test
  void whenSetLatestSyncNotificationThenOk() {
    final LibraryClient client = new LibraryClient(new ClientId("urn:client:solution:test"));
    final SyncNotification latestSyncNotification = new SyncNotification(client.getClientId());

    client.setLatestSyncNotification(latestSyncNotification);

    assertEquals(client, latestSyncNotification.getLibraryClient());
    assertEquals(latestSyncNotification, client.getLatestSyncNotification());
  }

  @Test
  void whenSetLatestSyncNotifictionIsNullThenThrowException() {
    final LibraryClient client = new LibraryClient(new ClientId("urn:client:solution:test"));
    assertThrows(IllegalArgumentException.class, () ->

      client.setLatestSyncNotification(null));
  }

  @Test
  void whenRemoveLatestSyncNotificationThenOk() {
    final LibraryClient client = new LibraryClient(new ClientId("urn:client:solution:test"));
    final SyncNotification latestSyncNotification = new SyncNotification(client.getClientId());

    client.setLatestSyncNotification(latestSyncNotification);

    // check if the latest sync notification was added
    assertEquals(client, latestSyncNotification.getLibraryClient());
    assertEquals(latestSyncNotification, client.getLatestSyncNotification());

    client.removeLatestSyncNotification(latestSyncNotification);

    // check if the latest sync notification was removed
    assertNull(client.getLatestSyncNotification());
    assertNull(latestSyncNotification.getLibraryClient());
  }

  @Test
  void removeLatestSyncNotification_whenWasNull_thenOk() {
    final LibraryClient client = new LibraryClient(new ClientId("urn:client:solution:test"));
    final SyncNotification latestSyncNotification = new SyncNotification(client.getClientId());

    client.removeLatestSyncNotification(latestSyncNotification);

    assertNull(client.getLatestSyncNotification());
    assertNull(latestSyncNotification.getLibraryClient());
  }

  @Test
  void removeLatestSyncNotification_whenNotEquals_doNothing() {
    final LibraryClient client = new LibraryClient(new ClientId("urn:client:solution:test"));
    final SyncNotification latestSyncNotification = new SyncNotification(client.getClientId());
    final SyncNotification latestSyncNotification2 = new SyncNotification(
        new ClientId("urn:client:solution:test1234"));

    client.setLatestSyncNotification(latestSyncNotification);

    client.removeLatestSyncNotification(latestSyncNotification2);

    assertEquals(client, latestSyncNotification.getLibraryClient());
    assertEquals(latestSyncNotification, client.getLatestSyncNotification());
  }

  @Test
  void removeLatestSyncNotification_whenNull_thenThrowException() {
    final LibraryClient client = new LibraryClient(new ClientId("urn:client:solution:test"));
    assertThrows(IllegalArgumentException.class, () ->

      client.removeLatestSyncNotification(null));
  }

  @Test
  void whenEqualThenEqualsAndHashCodeOk() {
    final LibraryClient client1 = new LibraryClient();
    client1.setClientId(new ClientId("urn:client:solution:test"));

    final LibraryClient client2 = new LibraryClient();
    client2.setClientId(new ClientId("urn:client:solution:test"));

    assertEquals(client1, client2);
    assertEquals(client1.hashCode(), client2.hashCode());
  }

  @Test
  void whenSameObjectThenEqualsOk() {
    final LibraryClient client = new LibraryClient();
    client.setClientId(new ClientId("urn:client:solution:test"));
    assertEquals(client, client);
  }

  @Test
  void whenDifferentClassesThenEqualsFail() {
    final LibraryClient client = new LibraryClient(new ClientId("urn:client:solution:test"));
    final SyncNotification latestSyncNotification = new SyncNotification(client.getClientId());
    assertNotEquals(client, latestSyncNotification);
  }

  @Test
  void whenEqualWithNullThenEqualsFail() {
    final LibraryClient client = new LibraryClient(new ClientId("urn:client:solution:test"));
    assertNotEquals(null, client);
  }

}
