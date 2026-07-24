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

import de.fraunhofer.iese.mydata.pdp.language.parser.PolicyVisitor;
import de.fraunhofer.iese.mydata.policy.PolicyId;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class Policy.
 */
@XmlRootElement(namespace = "http://www.mydata-control.de/4.0/mydataLanguage")
@Getter
@Setter
public class Policy {

  private String id;

  /**
   * The ID of the policy.
   */
  private PolicyId policyId;

  /**
   * The description.
   */
  private String description;

  /**
   * The mechanisms.
   */
  private List<PolicyMechanism> mechanisms = new ArrayList<>();

  /**
   *
   */
  private List<VariableDeclaration> variableDeclarations = new ArrayList<>();

  /**
   * Instantiates a new policy.
   */
  public Policy() {
    this.setMechanisms(new ArrayList<PolicyMechanism>());
  }

  /**
   * Instantiates a new policy.
   *
   * @param capacity the capacity
   */
  public Policy(int capacity) {
    this.setMechanisms(new ArrayList<PolicyMechanism>(capacity));
  }

  /**
   * Gets the description.
   *
   * @return the description
   */
  @XmlAttribute
  public String getDescription() {
    return this.description;
  }

  /**
   * Gets the mechanisms.
   *
   * @return the mechanisms
   */
  @XmlElement(name = "mechanism", namespace = "http://www.mydata-control.de/4.0/mydataLanguage")
  public List<PolicyMechanism> getMechanisms() {
    return this.mechanisms;
  }

  /**
   * Adds the mechanism.
   *
   * @param mechanism the mechanism
   */
  public void addMechanism(PolicyMechanism mechanism) {
    if (this.mechanisms == null) {
      this.mechanisms = new ArrayList<>();
    }
    this.mechanisms.add(mechanism);
  }

  /**
   * Gets the ID of the policy.
   *
   * @return the component_id
   */

  @XmlAttribute
  @XmlJavaTypeAdapter(PolicyIdXMLAdapter.class)
  private String getId() {
    return this.id;
  }

  /**
   * Sets the ID of the policy.
   *
   * @param id the component_id to set
   */
  public void setId(String id) {
    this.id = id;
    this.policyId = new PolicyId(id);
  }

  /**
   * @return a list of variable found in the XML if they match the XMLelements declared
   */
  @XmlElements({
      @XmlElement(name = "string", namespace = "http://www.mydata-control.de/4.0/variableDeclaration"),
      @XmlElement(name = "number", namespace = "http://www.mydata-control.de/4.0/variableDeclaration"),
      @XmlElement(name = "boolean", namespace = "http://www.mydata-control.de/4.0/variableDeclaration"),
      @XmlElement(name = "list", namespace = "http://www.mydata-control.de/4.0/variableDeclaration"),
      @XmlElement(name = "object", namespace = "http://www.mydata-control.de/4.0/variableDeclaration")
  })
  public List<VariableDeclaration> getVariableDeclarations() {
    return this.variableDeclarations;
  }

  /**
   * @param variableDeclarations
   */
  public void setVariableDeclarations(List<VariableDeclaration> variableDeclarations) {
    this.variableDeclarations = variableDeclarations;
  }

  @Override
  public String toString() {
    return "Policy{" + "id=" + this.id + ", description='" + this.description + '\'' + '}';
  }

  /**
   * @param policyVisitor
   */
  public void accept(PolicyVisitor policyVisitor) {
    policyVisitor.visit(this);
  }
}
