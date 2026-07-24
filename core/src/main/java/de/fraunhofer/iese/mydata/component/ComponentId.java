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

package de.fraunhofer.iese.mydata.component;

import de.fraunhofer.iese.mydata.common.MyDataEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * A component ID uniquely names an IND&sup2;UCE component. The ID is an URN which must follow the
 * format &quot;urn:component:&lt;solution&gt;:&lt;p*p&gt;:&lt;name&gt;&quot;, where
 * &quot;&lt;p*p&gt;&quot; can be pap, pdp, pep, pip, pmp, ms, prp, or pxp.
 */
@Embeddable
@Getter
@Setter
public class ComponentId extends MyDataEntity implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 3679962513886518846L;

  /**
   * The pattern of a component ID.
   * ^urn:component:([a-z0-9-_\.])+:(p[adeimrx]p|ms):([a-z0-9-_\.]+|%[0-9a-f]{2})$
   */
  @NotNull
  @Pattern(regexp = "^urn:component:([a-z0-9-_\\.'])+:(p[adeimrx]p|ms):([a-z0-9-_\\.]|%[0-9a-f]{2})+$")
  @Column(name = "component_id")
  private String urn;

  /**
   * Parameterless constructor is required for JPA.
   */
  public ComponentId() {
    super();
  }

  /**
   * Instantiates a new component component_id.
   *
   * @param urn the urn
   */
  public ComponentId(String urn) {
    this.urn = urn;
  }

  @Override
  public String toString() {
    return this.urn;
  }

  public ComponentType getComponentType() {
    return ComponentType.fromComponentId(this);
  }

  @Override
  public boolean equals(Object obj) {

    if (this.getUrn() == null || obj == null || !(obj instanceof ComponentId)) {
      return false;
    }

    return this.getUrn().equals(((ComponentId) obj).getUrn());
  }

  @Override
  public int hashCode() {
    final HashCodeBuilder builder = new HashCodeBuilder(17, 31);
    builder.append(this.urn);
    return builder.toHashCode();
  }

}
