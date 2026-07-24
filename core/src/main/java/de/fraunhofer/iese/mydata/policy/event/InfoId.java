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

package de.fraunhofer.iese.mydata.policy.event;

import de.fraunhofer.iese.mydata.common.MyDataEntity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * The ActionId uniquely identifies an identifier that can occur in a certain system on a certain
 * abstraction level. Examples might be: *
 * <ul>
 * <li>urn:action:laptop123:filesystem-open</li>
 * </ul>
 * Concrete actions that occurred are represented by an {@link Event}.
 */

@Getter
public class InfoId extends MyDataEntity {

  /**
   * The URN pattern for an InfoId. Must be of the format
   * &quot;urn:info:&lt;solution&gt;:&lt;identifier&gt;&quot;
   * <ul>
   * <li>solution is the solution (e.g., system, domain) where the info (PIP) occurs</li>
   * <li>identifier is the info unique identifier</li>
   * </ul>
   */
  @NotNull
  @Pattern(regexp = "^urn:info:[a-z0-9-_\\.]+:[a-zA-Z0-9-_\\.]+$")
  private final String urn;

  /**
   * Instantiates a new identifier component_id.
   *
   * @param urn the urn
   */
  public InfoId(String urn) {
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

    if (!(o instanceof InfoId)) {
      return false;
    }

    final InfoId infoId = (InfoId) o;

    return new EqualsBuilder().append(this.getUrn(), infoId.getUrn()).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(this.getUrn()).toHashCode();
  }
}
