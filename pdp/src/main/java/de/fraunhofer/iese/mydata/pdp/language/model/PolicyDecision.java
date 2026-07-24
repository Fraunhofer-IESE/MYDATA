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

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The Class Decision.
 */
@XmlRootElement(name = "then", namespace = "http://www.mydata-control.de/4.0/mydataLanguage")
public class PolicyDecision {

  private Optional<Boolean> allowed = Optional.empty();

  private String allow;

  private String inhibit;

  private List<PolicyModify> modifiers = new ArrayList<>();

  private List<ExecuteFunction> executeActions = new ArrayList<>(); // optional

  public static final PolicyDecision NO_DECISION = new PolicyDecision();

  public PolicyDecision(List<PolicyModify> modifiers) {
    super();
    this.modifiers = modifiers;
  }

  public PolicyDecision(List<PolicyModify> modifiers, List<ExecuteFunction> executeActions) {
    super();
    this.modifiers = modifiers;
    this.executeActions = executeActions;
  }

  public PolicyDecision(boolean allowed) {
    super();
    this.allowed = Optional.of(allowed);
  }

  public PolicyDecision() {
    super();
  }

  @XmlTransient()
  public Optional<Boolean> isAllowed() {
    return this.allowed;
  }

  public void setAllowed(boolean allowed) {
    this.allowed = Optional.of(allowed);
  }

  @XmlElement(name = "allow", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", defaultValue = "allow")
  public String getAllow() {
    return this.allow;
  }

  public void setAllow(String allow) {
    this.allow = allow;
    this.setAllowed(true);
  }

  @XmlElement(name = "inhibit", namespace = "http://www.mydata-control.de/4.0/mydataLanguage", defaultValue = "inhibit")
  public String getInhibit() {
    return this.inhibit;
  }

  public void setInhibit(String inhibit) {
    this.inhibit = inhibit;
    this.setAllowed(false);
  }

  @XmlElement(name = "modify", namespace = "http://www.mydata-control.de/4.0/mydataLanguage")
  public List<PolicyModify> getModifiers() {
    return this.modifiers;
  }

  public void setModifiers(List<PolicyModify> modifiers) {
    this.modifiers = modifiers;
  }

  @XmlElement(name = "execute", namespace = "http://www.mydata-control.de/4.0/mydataLanguage")
  public List<ExecuteFunction> getExecuteActions() {
    return this.executeActions;
  }

  public void setExecuteActions(List<ExecuteFunction> executeActions) {
    this.executeActions = executeActions;
  }

  public void accept(PolicyVisitor policyVisitor) {
    policyVisitor.visit(this);
  }
}
