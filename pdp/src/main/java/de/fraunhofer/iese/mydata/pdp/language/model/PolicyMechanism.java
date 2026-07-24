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

package de.fraunhofer.iese.mydata.pdp.language.model;

import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.ExecuteFunction;
import de.fraunhofer.iese.mydata.pdp.language.parser.PolicyVisitor;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class Mechanism.
 */
@Getter
@Setter
@XmlRootElement(namespace = "http://www.mydata-control.de/4.0/mydataLanguage", name = "mechanism")
public class PolicyMechanism {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PolicyMechanism.class);

  /**
   * The name.
   */
  private String id;

  /**
   * The policy.
   */
  private Policy policy;

  /**
   * The description.
   */
  private String description;

  /**
   * The event name that triggers the mechanism
   */
  private ActionId event;

  /**
   * The first if condition.
   */
  private PolicyIfBlock ifBlock;

  /**
   * Sorted list of else-if blocks
   */
  private List<PolicyIfBlock> elseIfBlocks;

  /**
   * Final else block if all if/elseif before are false
   */
  private PolicyDecision elseBlock;

  /**
   * The not prerequisite execute actions.
   */
  private List<ExecuteFunction> executeActions;

  private ZoneId zoneId;

  /**
   * Instantiates a new mechanism.
   */
  public PolicyMechanism() {
    this.setExecuteActions(new ArrayList<ExecuteFunction>());
    this.setId("Unnamed");
  }

  /**
   * Evaluates the event itself and the conditions.
   *
   * @param  evt                            the evt
   * @return                                true, if successful
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   */
  public PolicyDecision evaluate(Event evt) throws EvaluationUndecidableException {
    evt.setZoneId(this.zoneId);
    evt.setPolicyId(this.policy.getPolicyId());
    if (evt.getActionId() == null || !evt.getActionId().equals(this.getEvent())) {
      LOG.debug("Mechanism [{}] is not triggered by event [{}]", this.getId(), evt.getActionId());
      return new PolicyDecision(true);
    }

    PolicyDecision result = this.ifBlock.evaluate(evt);
    if (result != null) {
      LOG.debug("If condition of mechanism [{}] in policy [{}] matches.", this.getId(),
          this.policy.getPolicyId());
      return result;
    }

    if (this.elseIfBlocks != null && !this.elseIfBlocks.isEmpty()) {
      for (int i = 0; i < this.elseIfBlocks.size(); i++) {
        result = this.elseIfBlocks.get(i).evaluate(evt);
        if (result != null) {
          LOG.debug("Else-If Condition {} of mechanism [{}] in policy [{}] matches.", i,
              this.getId(), this.policy.getPolicyId());
          return result;
        }
      }
    }

    if (this.elseBlock != null) {
      result = this.elseBlock;
      LOG.debug("Else Condition of mechanism [{}] in policy [{}] matches.", this.getId(),
          this.policy.getPolicyId());
      return result;
    }

    LOG.debug("No condition for event [{}] in policy [{}] matches!", evt.getActionId(),
        this.policy.getPolicyId());
    return PolicyDecision.NO_DECISION;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  @XmlAttribute
  public String getId() {
    return this.id;
  }

  /**
   * Sets the name.
   *
   * @param name the name to set
   */
  public void setId(String name) {
    this.id = name;
  }

  /**
   * Gets the policy.
   *
   * @return the policy
   */
  public Policy getPolicy() {
    return this.policy;
  }

  /**
   * Sets the policy.
   *
   * @param policy the policy to set
   */
  public void setPolicy(Policy policy) {
    this.policy = policy;
  }

  /**
   * Gets the description.
   *
   * @return the description
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * Sets the description.
   *
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Gets the execute actions.
   *
   * @return the execute actions
   */
  @XmlElement(name = "execute", namespace = "http://www.mydata-control.de/4.0/mydataLanguage")
  public List<ExecuteFunction> getExecuteActions() {
    return this.executeActions;
  }

  /**
   * Sets the execute actions.
   *
   * @param notPrerequisiteExecuteActions the new execute actions
   */
  public void setExecuteActions(List<ExecuteFunction> notPrerequisiteExecuteActions) {
    this.executeActions = notPrerequisiteExecuteActions;
  }

  @XmlAttribute
  @XmlJavaTypeAdapter(EventXMLAdapter.class)
  public ActionId getEvent() {
    return this.event;
  }

  public void setEvent(ActionId event) {
    this.event = event;
  }

  @XmlElement(name = "if", namespace = "http://www.mydata-control.de/4.0/mydataLanguage")
  public PolicyIfBlock getIfBlock() {
    return this.ifBlock;
  }

  public void setIfBlock(PolicyIfBlock ifBlock) {
    this.ifBlock = ifBlock;
  }

  @XmlElement(name = "elseif", namespace = "http://www.mydata-control.de/4.0/mydataLanguage")
  public List<PolicyIfBlock> getElseIfBlocks() {
    return this.elseIfBlocks;
  }

  public void setElseIfBlocks(List<PolicyIfBlock> elseIfBlocks) {
    this.elseIfBlocks = elseIfBlocks;
  }

  @XmlElement(name = "else", namespace = "http://www.mydata-control.de/4.0/mydataLanguage")
  public PolicyDecision getElseBlock() {
    return this.elseBlock;
  }

  public void setElseBlock(PolicyDecision elseBlock) {
    this.elseBlock = elseBlock;
  }

  public void accept(PolicyVisitor visitor) {
    visitor.visit(this);
  }

}
