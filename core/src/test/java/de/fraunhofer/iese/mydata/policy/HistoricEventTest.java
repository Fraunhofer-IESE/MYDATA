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

package de.fraunhofer.iese.mydata.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.event.history.HistoricEvent;
import de.fraunhofer.iese.mydata.policy.event.history.HistoricEventParameter;
import de.fraunhofer.iese.mydata.policy.event.history.HistoricEventTrackItem;
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class HistoricEventTest {

  @Test
  void withObject() {
    final Event event = this.prepareEvent();
    this.doTest(event);
  }

  @Test
  void withUnknownObject() {
    Event event = this.prepareEvent();
    String jsonString = MyDataEntity.getGson().toJson(event);
    // make sure that User class is unknown for deserialization
    jsonString = jsonString.replaceAll("de.fraunhofer.iese.mydata.policy.HistoricEventTest.User",
        "de.fraunhofer.iese.mydata.policy.HistoricEventTest.UserBlaNonExisting");
    jsonString = jsonString.replaceAll("de.fraunhofer.iese.mydata.policy.HistoricEventTest\\$User",
        "de.fraunhofer.iese.mydata.policy.HistoricEventTest.UserBlaNonExisting");
    event = MyDataEntity.getGson().fromJson(jsonString, Event.class);

    this.doTest(event);
  }

  @Test
  void withDeserializedObject() {
    Event event = this.prepareEvent();
    String jsonString = MyDataEntity.getGson().toJson(event);
    // make sure that User class is known for deserialization
    jsonString = jsonString.replaceAll("de.fraunhofer.iese.mydata.policy.HistoricEventTest.User",
        "de.fraunhofer.iese.mydata.policy.HistoricEventTest\\$User");
    event = MyDataEntity.getGson().fromJson(jsonString, Event.class);

    this.doTest(event);
  }

  private Event prepareEvent() {
    final List<User> userList = new ArrayList<>();
    userList.add(new User("Du1", 2L));
    userList.add(new User("D32", 3L));
    userList.add(new User("D23", 4L));

    final User user = new User("Hans Bach", 5L);
    final ActionId actionId = new ActionId("urn:action:cs4:access-list");
    final Event event = new Event(actionId);
    event.addParameter("name", "Mark");
    event.addParameter("age", 38);
    event.addParameter("active", false);
    event.addParameter("users", userList);
    event.addParameter("user", user);
    return event;
  }

  private void doTest(final Event event) {
    final ActionId actionId = event.getActionId();
    final Set<HistoricEventTrackItem> trackItems = new HashSet<>();
    trackItems.add(HistoricEventTrackItem.of(actionId, "name", null, null));
    trackItems.add(HistoricEventTrackItem.of(actionId, "active", null, null));
    trackItems.add(HistoricEventTrackItem.of(actionId, "users", null, null));
    trackItems.add(HistoricEventTrackItem.of(actionId, "users", "$[0].name", null));
    trackItems.add(HistoricEventTrackItem.of(actionId, "user", "$.name", null));
    trackItems.add(HistoricEventTrackItem.of(actionId, "user", "$.id", null));
    trackItems.add(HistoricEventTrackItem.of(actionId, "user", "$.blatest", null));
    trackItems.add(HistoricEventTrackItem.of(actionId, "age", null, null));
    final HistoricEvent historicEvent = new HistoricEvent(event, trackItems);

    assertEquals(event.getActionId(), historicEvent.getActionId());
    assertEquals(event.getMillisecondSinceEpoch(), historicEvent.getOccurredAtMs());

    final Collection<HistoricEventParameter> hep = historicEvent.getHistoricEventParameters();
    int nameMatching = 0;
    int valueMatching = 0;
    for (final Parameter e : event.getParameters()) {
      for (final HistoricEventParameter historicEventParameter : hep) {
        if (historicEventParameter.getName().equalsIgnoreCase(e.getName())) {
          nameMatching++;
          if (historicEventParameter.getName().equals("user")
              && "$.name".equals(historicEventParameter.getJsonPath())) {
            if (historicEventParameter.getValue()
                .equalsIgnoreCase(HistoricEventParameter.hashValue("Hans Bach"))) {
              valueMatching++;
            }
          } else if (historicEventParameter.getName().equals("user")
              && "$.blatest".equals(historicEventParameter.getJsonPath())) {
            if (historicEventParameter.getValue()
                .equalsIgnoreCase(HistoricEventParameter.hashValue(""))) {
              valueMatching++;
            }
          } else if (historicEventParameter.getName().equals("user")
              && "$.id".equals(historicEventParameter.getJsonPath())) {
            if (historicEventParameter.getValue()
                .equalsIgnoreCase(HistoricEventParameter.hashValue("5"))) {
              valueMatching++;
            }
          } else if (historicEventParameter.getName().equals("users")
              && "$[0].name".equals(historicEventParameter.getJsonPath())) {
            if (historicEventParameter.getValue()
                .equalsIgnoreCase(HistoricEventParameter.hashValue("Du1"))) {
              valueMatching++;
            }
          } else if (e.getValue() instanceof Collection) {
            if (historicEventParameter.getValue().equalsIgnoreCase(
                HistoricEventParameter.hashValue(MyDataEntity.getGson().toJson(e.getValue())))) {
              valueMatching++;
            }
          } else if (historicEventParameter.getValue()
              .equalsIgnoreCase(HistoricEventParameter.hashValue(e.getValue().toString()))) {
            valueMatching++;
          }
        }
      }
    }
    //expect name, active, user but only two values because of the jsonpath that can not be evaluated here for the Parameter
    assertEquals(8, nameMatching);
    assertEquals(8, valueMatching);
  }

  class User {

    public String name;

    public long id;

    public User(String name, long id) {
      this.name = name;
      this.id = id;
    }

    @Override
    public String toString() {
      return this.name + "-with-" + this.id;
    }
  }
}
