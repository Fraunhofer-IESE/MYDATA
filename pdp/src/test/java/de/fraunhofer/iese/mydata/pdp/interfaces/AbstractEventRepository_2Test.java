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

import de.fraunhofer.iese.mydata.policy.Policy;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.event.history.HistoricEvent;
import de.fraunhofer.iese.mydata.policy.event.history.HistoricEventParameter;
import de.fraunhofer.iese.mydata.policy.event.history.HistoricEventTrackItem;
import de.fraunhofer.iese.mydata.policy.event.history.IEventRepository;
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

class AbstractEventRepository_2Test {

  private final IEventRepository aer = new DemoEventRepository();

  private final String actionBase = "urn:action:demo:";

  @Test
  void deployPolicyWithEventParams() throws Exception {
    final String policyWithEventParameters = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("policyWithEventParameters.xml")).toURI())));

    this.aer.policyDeployed(new Policy(policyWithEventParameters));

    final Map<ActionId, Set<PolicyId>> eventsToBeStored = this.aer.getEventsToBeStored();
    final Map<ActionId, Set<HistoricEventTrackItem>> paramsToBeStored = this.aer.getHistoricEventTrackItemsPerActionId();

    final Event event = new Event(new ActionId("urn:action:cs4:enforceBankData"), new Parameter<Object>("user", new User("test", 5L, "bob")), new Parameter<>("blabla", "pouet"));

    this.aer.notify(event);

  }

  @Test
  void deployAndRevokePolicyWithEventParams() throws Exception {
    final String policyWithEventParameters = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("policyWithEventParameters.xml")).toURI())));
    final Policy policy = new Policy(policyWithEventParameters);
    this.aer.policyDeployed(policy);

    final Map<ActionId, Set<PolicyId>> eventsToBeStored = this.aer.getEventsToBeStored();
    final Map<ActionId, Set<HistoricEventTrackItem>> paramsToBeStored = this.aer.getHistoricEventTrackItemsPerActionId();
    this.aer.policyRevoked(policy);
    final Event event = new Event(new ActionId("urn:action:cs4:enforceBankData"), new Parameter<Object>("user", new User("test", 5L, "bob")), new Parameter<>("blabla", "pouet"));
    this.aer.notify(event);
  }

  @Test
  void deployAndUpdatePolicyWithEventParams() throws Exception {
    final String policyWithEventParameters = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("policyWithEventParameters.xml")).toURI())));
    final Policy policy = new Policy(policyWithEventParameters);
    this.aer.policyDeployed(policy);

    final Map<ActionId, Set<PolicyId>> eventsToBeStored = this.aer.getEventsToBeStored();
    final Map<ActionId, Set<HistoricEventTrackItem>> paramsToBeStored = this.aer.getHistoricEventTrackItemsPerActionId();
    this.aer.policyUpdate(policy, policy.getPolicyId());
    final Event event = new Event(new ActionId("urn:action:cs4:enforceBankData"), new Parameter<Object>("user", new User("test", 5L, "bob")), new Parameter<>("blabla", "pouet"));
    this.aer.notify(event);
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
