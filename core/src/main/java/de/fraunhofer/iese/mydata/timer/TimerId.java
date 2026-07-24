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

package de.fraunhofer.iese.mydata.timer;

import de.fraunhofer.iese.mydata.common.MyDataEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
public class TimerId extends MyDataEntity implements Serializable {
  @NotNull
  @Pattern(regexp = "^urn:timer:[a-z0-9-_\\.]+:[a-zA-Z0-9-_\\.]+$")
  @Column(name = "timer_id")
  private String urn;

  private static final long serialVersionUID = -289290443447644072L;

  public TimerId() {
    // required by JPA
  }

  public TimerId(String id) {
    this.urn = id;
  }

  @Override
  public String toString() {
    return this.urn;
  }

  @Override
  public boolean equals(Object obj) {

    if (this.getUrn() == null || obj == null || !(obj instanceof TimerId)) {
      return false;
    }

    return this.getUrn().equals(((TimerId) obj).getUrn());
  }

  @Override
  public int hashCode() {
    final HashCodeBuilder builder = new HashCodeBuilder(17, 31);
    builder.append(this.urn);
    return builder.toHashCode();
  }
}
