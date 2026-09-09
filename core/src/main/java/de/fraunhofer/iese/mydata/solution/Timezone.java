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

package de.fraunhofer.iese.mydata.solution;

import de.fraunhofer.iese.mydata.common.Hide;
import de.fraunhofer.iese.mydata.common.MyDataEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "timezone", indexes = {
    @Index(columnList = "zoneid", unique = true)
})
@Getter
@Setter
public class Timezone extends MyDataEntity {

  @Id
  @NotBlank
  @Column(name = "zoneid", unique = true)
  private String zoneid;

  @Hide
  @OneToMany(fetch = FetchType.LAZY, mappedBy = "timezone")
  private List<Solution> solutions;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof Timezone)) {
      return false;
    }

    final Timezone timezone = (Timezone) o;

    return new EqualsBuilder().append(this.getZoneid(), timezone.getZoneid()).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(this.getZoneid()).toHashCode();
  }

  public void setSolutions(List<Solution> solutions) {
    if (solutions == null) {
      throw new IllegalArgumentException("Solutions can not be null");
    }
    this.getSolutions();
    this.solutions.clear();
    this.solutions.addAll(solutions);
  }

  public List<Solution> getSolutions() {
    if (this.solutions == null) {
      this.solutions = new ArrayList<>();
    }
    return this.solutions;
  }
}
