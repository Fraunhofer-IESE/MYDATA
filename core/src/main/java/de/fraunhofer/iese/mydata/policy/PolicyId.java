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

import de.fraunhofer.iese.mydata.common.MyDataEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
public class PolicyId extends MyDataEntity implements Serializable {

  private static final long serialVersionUID = -289290444447644072L;

  @NotNull
  @Pattern(regexp = "^urn:policy:[a-z0-9-_\\.]+:[a-zA-Z0-9-_\\.]+$")
  @Column(name = "policy_id")
  private String urn;

  public PolicyId() {
    // required by JPA
  }

  public PolicyId(String urn) {
    this.urn = urn;
  }

  @Override
  public String toString() {
    return this.urn;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof PolicyId)) {
      return false;
    }

    final PolicyId policyId = (PolicyId) o;

    return new EqualsBuilder().append(this.urn, policyId.urn).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(this.urn).toHashCode();
  }
}
