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

package de.fraunhofer.iese.mydata.pdp.interfaces;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.policy.Policy;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.event.history.HistoricEvent;
import de.fraunhofer.iese.mydata.policy.event.history.HistoricEventParameter;
import de.fraunhofer.iese.mydata.policy.event.history.HistoricEventTrackItem;
import de.fraunhofer.iese.mydata.policy.event.history.IEventRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.w3c.dom.DOMException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
class AbstractEventRepositoryTest {

  @Spy
  private final IEventRepository eventRepository = new DemoEventRepository();

  private final String actionBase = "urn:action:test:";

  @Test
  void deploy_revoke_notify() throws Exception {
    final ActionId event32 = new ActionId(actionBase + "32");
    final ActionId event42 = new ActionId(actionBase + "42");
    final ActionId event52 = new ActionId(actionBase + "52");

    final String policy1 = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("policyOneEventOccurrence.xml")).toURI())));
    final String policy2 = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("policyTwoEventOccurrence.xml")).toURI())));

    this.eventRepository.policyDeployed(new Policy(policy1));
    assertEquals(1L, this.eventRepository.getEventsToBeStored().size());

    this.eventRepository.policyDeployed(new Policy(policy2));
    assertEquals(2L, this.eventRepository.getEventsToBeStored().size());
    assertEquals(2L, this.eventRepository.getEventsToBeStored().get(event32).size());

    this.eventRepository.notify(new Event(event32));
    this.eventRepository.notify(new Event(event42));
    this.eventRepository.notify(new Event(event52));

    Mockito.verify(this.eventRepository, Mockito.times(3)).notify(any(Event.class));
    Mockito.verify(this.eventRepository, Mockito.times(2)).saveEventOccurrence(any(Event.class), any(Set.class));

    this.eventRepository.policyRevoked(new Policy(policy2));
    assertEquals(1L, this.eventRepository.getEventsToBeStored().size());
    assertEquals(1L, this.eventRepository.getEventsToBeStored().get(event32).size());
    assertFalse(this.eventRepository.getEventsToBeStored().containsKey(event42));

    this.eventRepository.notify(new Event(event32));
    this.eventRepository.notify(new Event(event42));
    this.eventRepository.notify(new Event(event52));

    Mockito.verify(this.eventRepository, Mockito.times(6)).notify(any(Event.class));
    Mockito.verify(this.eventRepository, Mockito.times(3)).saveEventOccurrence(any(Event.class), any(Set.class));
  }

  /**
   * 1. Deploy policy with 2 value change block and 2 events to be stored 2.
   * Update same policy with 2 value change block and 2 events to be stored. 1
   * value change block and 1 event to be stored is to be same as in previous
   * policy 3. Check if everything is working as expected
   *
   * @throws URISyntaxException
   * @throws IOException
   * @throws DOMException
   * @throws SecurityException
   * @throws NoSuchFieldException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   * @throws InvalidEntityException
   */
  @Test
  void updatePolicyTest()
      throws Exception {
    final String policyBeforeUpdate = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("policyBeforeUpdate.xml")).toURI())));
    final String policyAfterUpdate = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("policyAfterUpdate.xml")).toURI())));

    this.eventRepository.policyDeployed(new Policy(policyBeforeUpdate));

    final Field f = AbstractEventRepository.class.getDeclaredField("valueChangeBlockWithVariableBlock");
    f.setAccessible(true);

    Map valueChange = (Map) ((Map) f.get(this.eventRepository)).get(new PolicyId("urn:policy:cs4:updateTest"));
    Map<ActionId, Set<PolicyId>> eventsToBeStored = this.eventRepository.getEventsToBeStored();

    assertEquals(2L, eventsToBeStored.size());
    assertEquals(2L, valueChange.size());
    assertTrue(eventsToBeStored.containsKey(new ActionId("urn:action:cs4:show-task")));
    assertTrue(valueChange.containsKey("dc021c0a-1774-272b-3031-d47f67110cfc"));

    this.eventRepository.policyUpdate(new Policy(policyAfterUpdate));

    valueChange = (Map) ((Map) f.get(this.eventRepository)).get(new PolicyId("urn:policy:cs4:updateTest"));
    eventsToBeStored = this.eventRepository.getEventsToBeStored();

    assertEquals(2L, eventsToBeStored.size());
    assertEquals(2L, valueChange.size());
    assertFalse(eventsToBeStored.containsKey(new ActionId("urn:action:cs4:show-task")));
    assertFalse(valueChange.containsKey("dc021c0a-1774-272b-3031-d47f67110cfc"));
  }

  class User {

    public String name;

    public String firstName;

    public UserName uname;

    public long id;

    public User(String name, long id, String firstName) {
      this.name = name;
      this.id = id;
      this.firstName = firstName;
      uname = new UserName(firstName, name);
    }

    @Override
    public String toString() {
      return this.name + " " + this.id;
    }

    class UserName {
      public String prenom;

      public String nomFamille;

      public UserName(String firstName, String name) {
        this.prenom = firstName;
        this.nomFamille = name;
      }
    }
  }

  /**
   * 1. Deploy policy with 2 value change block and 2 events to be stored 2.
   * Update policyid Check if events still there is working as expected
   *
   * @throws URISyntaxException
   * @throws IOException
   * @throws DOMException
   * @throws SecurityException
   * @throws NoSuchFieldException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   * @throws InvalidEntityException
   */
  //TODO: also test as integration tests
  @Test
  void updatePolicyIdTest()
      throws Exception {
    final String policyBeforeUpdate = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("policyBeforeUpdate.xml")).toURI())));
    final Policy p = new Policy(policyBeforeUpdate);
    final PolicyId oldPolicyId = p.getPolicyId();

    final String otherPolicy = policyBeforeUpdate.replace("updateTest", "updateTest2").replace("event='urn:action:cs4:show-task'", "event='urn:action:cs4:toto'");
    final Policy op = new Policy(otherPolicy);

    final String newPolicy = policyBeforeUpdate.replace("updateTest", "updateTestNewId");
    final Policy np = new Policy(newPolicy);
    final PolicyId newPolicyId = np.getPolicyId();

    this.eventRepository.policyDeployed(p);
    this.eventRepository.policyDeployed(op);

    final Field f = AbstractEventRepository.class.getDeclaredField("valueChangeBlockWithVariableBlock");
    f.setAccessible(true);

    Map<?, ?> valueChange = (Map) ((Map) f.get(this.eventRepository)).get(oldPolicyId);
    Map<ActionId, Set<PolicyId>> eventsToBeStored = this.eventRepository.getEventsToBeStored();

    assertEquals(3L, eventsToBeStored.size());
    assertEquals(2L, valueChange.size());
    assertTrue(eventsToBeStored.containsKey(new ActionId("urn:action:cs4:show-task")));
    assertTrue(valueChange.containsKey("dc021c0a-1774-272b-3031-d47f67110cfc"));

    this.eventRepository.policyUpdate(np, oldPolicyId);

    valueChange = (Map) ((Map) f.get(this.eventRepository)).get(newPolicyId);
    eventsToBeStored = this.eventRepository.getEventsToBeStored();

    assertEquals(3L, eventsToBeStored.size());
    assertEquals(2L, valueChange.size());
    assertTrue(eventsToBeStored.containsKey(new ActionId("urn:action:cs4:show-task")));
    assertTrue(valueChange.containsKey("dc021c0a-1774-272b-3031-d47f67110cfc"));

  }

  class DemoEventRepository extends AbstractEventRepository {

    @Override
    public void saveEventOccurrence(Event event, Set<HistoricEventTrackItem> pa) {
      new HistoricEvent(event, pa);
    }

    @Override
    public List<HistoricEvent> findByOccurredAtMsBetweenAndActionId(long start, long end, ActionId actionId) {
      return new ArrayList<>();
    }

    @Override
    public long findByOccurredAtMsBeforeAndActionId(long end, ActionId actionId) {
      return 0;
    }

    @Override
    public long findByOccurredAtMsAfterAndActionId(long start, ActionId actionId) {
      return 0;
    }

    @Override
    public List<HistoricEvent> findByActionId(ActionId actionId) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public HistoricEvent findByActionIdAndMode(ActionId actionId, String mode, List<HistoricEventParameter> parameters) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public List<HistoricEvent> findAll() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public List<HistoricEvent> findByActionIdAndHistoricEventParametersAndOccurredAtMsBetween(ActionId actionId, List<HistoricEventParameter> hep, long start, long end) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public List<HistoricEvent> findByActionIdAndOccurredAtMsBetweenParamIndependant(ActionId actionId, long start, long end) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void deleteEventOccurrenceByActionId(ActionId actionId) {
      // TODO Auto-generated method stub

    }

    @Override
    public void deleteValueChangeBlock(PolicyId policyId, Map<String, String> variableValueChangeBlock) {
      // TODO Auto-generated method stub

    }

    @Override
    public String getValueChanged(String blockId, Policy policy) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void saveValueChangeBlock(Policy policy, Map<String, String> variableValueChangeBlock) {
      // TODO Auto-generated method stub

    }

    @Override
    public void setValueChanged(Policy policy, String blockId, String valueInPolicy) {
      // TODO Auto-generated method stub

    }

    @Override
    public void updateValueChangeBlockPolicyId(Policy newPolicy, PolicyId oldPolicyId) {
      // TODO Auto-generated method stub

    }
  }

}
