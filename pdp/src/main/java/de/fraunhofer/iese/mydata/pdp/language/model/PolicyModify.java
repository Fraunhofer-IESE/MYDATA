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

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

/**
 * The Class ModifyEngine.
 */
@XmlRootElement
public class PolicyModify {

  /**
   * The name.
   */
  private String eventParameter;

  private String method;

  private String jsonPathQuery;

  /**
   * The params.
   */
  private List<PolicyParameter<?>> params;

  /**
   * Used for JAXB.
   */
  public PolicyModify() {

  }

  public PolicyModify(String eventParameter, String method) {
    super();
    this.eventParameter = eventParameter;
    this.method = method;
  }

  public PolicyModify(String eventParameter, String method, String jsonPathQuery,
      List<PolicyParameter<?>> params) {
    super();
    this.eventParameter = eventParameter;
    this.method = method;
    this.jsonPathQuery = jsonPathQuery;
    this.params = params;
  }

  public PolicyModify(String eventParameter, String method, List<PolicyParameter<?>> params) {
    super();
    this.eventParameter = eventParameter;
    this.method = method;
    this.params = params;
  }

  @XmlAttribute
  public String getEventParameter() {
    return this.eventParameter;
  }

  public void setEventParameter(String eventParameter) {
    this.eventParameter = eventParameter;
  }

  @XmlAttribute
  public String getMethod() {
    return this.method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  @XmlAttribute
  public String getJsonPathQuery() {
    return this.jsonPathQuery;
  }

  public void setJsonPathQuery(String jsonPathQuery) {
    this.jsonPathQuery = jsonPathQuery;
  }

  @XmlElements({
      @XmlElement(name = "string", namespace = "http://www.mydata-control.de/4.0/parameter", type = StringParameter.class),
      @XmlElement(name = "object", namespace = "http://www.mydata-control.de/4.0/parameter", type = ObjectParameter.class),
      @XmlElement(name = "list", namespace = "http://www.mydata-control.de/4.0/parameter", type = ListParameter.class),
      @XmlElement(name = "boolean", namespace = "http://www.mydata-control.de/4.0/parameter", type = BooleanParameter.class),
      @XmlElement(name = "number", namespace = "http://www.mydata-control.de/4.0/parameter", type = NumberParameter.class)
  })
  public List<PolicyParameter<?>> getParams() {
    return this.params;
  }

  public void setParams(List<PolicyParameter<?>> params) {
    this.params = params;
  }

  public void accept(PolicyVisitor policyVisitor) {
    policyVisitor.visit(this);
  }
}
