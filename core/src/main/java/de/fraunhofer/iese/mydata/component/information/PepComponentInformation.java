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

package de.fraunhofer.iese.mydata.component.information;

import de.fraunhofer.iese.mydata.common.Hide;
import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.ComponentType;
import de.fraunhofer.iese.mydata.component.information.method.MethodInterfaceDescription;
import de.fraunhofer.iese.mydata.component.information.method.PepInterfaceDescription;
import de.fraunhofer.iese.mydata.component.information.validation.MyDataComponentInformation;
import de.fraunhofer.iese.mydata.solution.Solution;

import jakarta.persistence.CascadeType;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(indexes = {
    @Index(columnList = "component_id", unique = true)
})
@Getter
@Setter
@MyDataComponentInformation(componentType = ComponentType.PEP)
public class PepComponentInformation extends MyDataEntity {

  @Valid
  @NotNull
  @EmbeddedId
  protected ComponentId componentId;

  // optional as components in the library do not need a solution..
  @Hide
  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  private Solution solution;

  /**
   * The interface descriptions.
   */
  @Fetch(FetchMode.SUBSELECT)
  @OneToMany(orphanRemoval = true, mappedBy = "pepComponentInformation", cascade = CascadeType.ALL)
  private List<PepInterfaceDescription> interfaceDescriptions;

  /**
   * Set of Modifier.
   */
  @Valid
  @Fetch(FetchMode.SUBSELECT)
  @OneToMany(orphanRemoval = true, mappedBy = "pepComponentInformation", cascade = {
      CascadeType.PERSIST, CascadeType.MERGE
  })
  private List<MethodInterfaceDescription> methodInterfaceDescriptions;

  /**
   * Used for JPA.
   */
  public PepComponentInformation() {
    // required by JPA
  }

  /**
   * Instantiates a new pep component.
   *
   * @param id the component_id
   */
  public PepComponentInformation(ComponentId id) {
    this.componentId = id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof PepComponentInformation)) {
      return false;
    }

    final PepComponentInformation that = (PepComponentInformation) o;

    return new EqualsBuilder().append(this.getComponentId(), that.getComponentId()).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(this.getComponentId()).toHashCode();
  }

  /**
   * @return the list of Methods interface descriptions
   */
  public List<MethodInterfaceDescription> getMethodInterfaceDescriptions() {
    if (this.methodInterfaceDescriptions == null) {
      this.methodInterfaceDescriptions = new ArrayList<>();
    }
    return this.methodInterfaceDescriptions;
  }

  /**
   * @param methodInterfaceDescriptions
   */
  public void setMethodInterfaceDescriptions(
      @NonNull List<MethodInterfaceDescription> methodInterfaceDescriptions) {
    this.getMethodInterfaceDescriptions();
    this.methodInterfaceDescriptions.clear();
    this.methodInterfaceDescriptions.addAll(methodInterfaceDescriptions);
    for (final MethodInterfaceDescription mid : methodInterfaceDescriptions) {
      mid.setPepComponentInformation(this);
    }
  }

  /**
   * @return the list of PEP-interface descriptions
   */
  public List<PepInterfaceDescription> getInterfaceDescriptions() {
    if (this.interfaceDescriptions == null) {
      this.interfaceDescriptions = new ArrayList<>();
    }
    return this.interfaceDescriptions;
  }

  /**
   * @param interfaceDescriptions
   */
  public void setInterfaceDescriptions(
      @NonNull List<PepInterfaceDescription> interfaceDescriptions) {
    this.getInterfaceDescriptions();
    this.interfaceDescriptions.clear();
    this.interfaceDescriptions.addAll(interfaceDescriptions);
    for (final PepInterfaceDescription pid : interfaceDescriptions) {
      pid.setPepComponentInformation(this);
    }
  }

}
