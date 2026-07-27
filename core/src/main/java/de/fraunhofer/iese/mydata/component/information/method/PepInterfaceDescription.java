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

package de.fraunhofer.iese.mydata.component.information.method;

import de.fraunhofer.iese.mydata.common.Hide;
import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.component.information.PepComponentInformation;
import de.fraunhofer.iese.mydata.policy.event.ActionId;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Describes the capabilities of Policy Enforcement Points in terms of intercepted events and
 * enforceable methods.
 */
@Entity
@Getter
@Setter
public class PepInterfaceDescription extends MyDataEntity {

  /**
   * The component_id.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "pep_interface_description_id")
  protected Long id;

  /**
   * A description of the method.
   */
  @Lob
  @JdbcTypeCode(Types.LONGVARCHAR)
  protected String description;

  /**
   * The event that is intercepted or monitored.
   */
  @NotNull
  @Valid
  @Embedded
  private ActionId event;

  /**
   * A flag that indicated whether the event can be modified or blocked.
   */
  private boolean isPreventive;

  @Hide
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "component_id")
  private PepComponentInformation pepComponentInformation;

  /** * List of parameter. */
  @OneToMany(orphanRemoval = true, mappedBy = "pepInterfaceDescription", cascade = {
      CascadeType.PERSIST, CascadeType.MERGE
  })
  @Fetch(FetchMode.SUBSELECT)
  @OrderColumn(name = "parameter_order")
  private List<@Valid InputParameterDescription> eventParameterDescription;

  /**
   * Default Constructor, at least required by JPA.s
   */
  public PepInterfaceDescription() {
    // required by JPA
  }

  /**
   * Instantiates a new pep interface description.
   *
   * @param event        the event that the PEP intercepts or monitors
   * @param isPreventive a flag that indicated whether the event can be modified or blocked
   * @param description  the description
   */
  public PepInterfaceDescription(ActionId event, boolean isPreventive, String description) {
    this(event, isPreventive, description, Collections.emptyList());
  }

  /**
   * Instantiates a new pep interface description.
   *
   * @param event                the event that the PEP intercepts or monitors
   * @param isPreventive         a flag that indicated whether the event can be modified or blocked
   * @param description          the description
   * @param eventInputParameters the eventInputParameters
   */
  public PepInterfaceDescription(ActionId event, boolean isPreventive, String description,
      List<InputParameterDescription> eventInputParameters) {
    this.description = description;
    this.event = event;
    this.isPreventive = isPreventive;
    this.eventParameterDescription = new ArrayList<>();
    this.eventParameterDescription.addAll(eventInputParameters);
    for (final InputParameterDescription pid : eventInputParameters) {
      pid.setPepInterfaceDescription(this);
    }
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PepInterfaceDescription)) {
      return false;
    }

    final PepInterfaceDescription that = (PepInterfaceDescription) o;

    if (this.isPreventive != that.isPreventive) {
      return false;
    }
    if (!Objects.equals(this.id, that.id)) {
      return false;
    }
    if (!Objects.equals(this.description, that.description)) {
      return false;
    }
    if (!Objects.equals(this.event, that.event)) {
      return false;
    }
    return Objects.equals(this.eventParameterDescription, that.eventParameterDescription);

  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    int result = this.id != null ? this.id.hashCode() : 0;
    result = 31 * result + (this.description != null ? this.description.hashCode() : 0);
    result = 31 * result + (this.event != null ? this.event.hashCode() : 0);
    result = 31 * result + (this.isPreventive ? 1 : 0);
    result = 31 * result
        + (this.eventParameterDescription != null ? this.eventParameterDescription.hashCode() : 0);
    return result;
  }

  /**
   * @return the list of InputParameterDescriptions
   */
  public List<InputParameterDescription> getEventParameterDescription() {
    if (this.eventParameterDescription == null) {
      this.eventParameterDescription = new ArrayList<>();
    }
    return this.eventParameterDescription;
  }

  /**
   * @param eventParameterDescription
   */
  public void setEventParameterDescription(
      List<InputParameterDescription> eventParameterDescription) {
    this.getEventParameterDescription();
    this.eventParameterDescription.clear();
    this.eventParameterDescription.addAll(eventParameterDescription);
    for (final InputParameterDescription pid : eventParameterDescription) {
      pid.setPepInterfaceDescription(this);
    }
  }

}
