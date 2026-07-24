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
import de.fraunhofer.iese.mydata.component.information.JsonSchemaGenerator;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.JdbcTypeCode;

import java.lang.reflect.Type;
import java.sql.Types;

/**
 * The Class InputParameterDescription is used by different component as PXP, PIP, PEP and others to
 * specify {@link MethodInterfaceDescription} at the time of registration at PMP.
 */
@Entity
@Getter
@Setter
public class InputParameterDescription extends MyDataEntity {

  /**
   * The input parameter ID.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "input_parameter_description_id")
  private Long id;

  /**
   * The name of the parameter.
   */
  @NotBlank
  private String name;

  /**
   * A description about what the parameter is used for.
   */
  @Lob
  @JdbcTypeCode(Types.LONGVARCHAR)
  private String description;

  /**
   * A flag, indicating whether the parameter is mandatory or not.
   */
  @NotNull
  boolean mandatory = true;

  /**
   * The parameter type.
   */

  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "type_description_id")
  private TypeDescription typeDescription;

  @Hide
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "method_interface_description_id")
  private MethodInterfaceDescription methodInterfaceDescription;

  @Hide
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pep_interface_description_id")
  private PepInterfaceDescription pepInterfaceDescription;

  /**
   * Instantiates a new input parameter description for JPA.
   */
  InputParameterDescription() {
    // required by JPA
  }

  /**
   * Instantiates a new input parameter description.
   *
   * @param name        the name of the parameter
   * @param description the description of the parameter
   * @param type        the type of the parameter
   */
  public InputParameterDescription(String name, String description, Class<?> type) {
    this(name, description, true, type);
  }

  /**
   * Instantiates a new input parameter description.
   *
   * @param name        the name of the parameter
   * @param description the description of the parameter
   * @param mandatory   indication a parameter that is mandatory
   * @param clazz       the clazz of the parameter
   */
  public InputParameterDescription(String name, String description, boolean mandatory,
      Class<?> clazz) {
    this(name, description, mandatory, clazz, clazz);
  }

  /**
   * Instantiates a new input parameter description.
   *
   * @param name        the name
   * @param description the description
   * @param mandatory   the mandatory
   * @param type        the type
   * @param clazz       the clazz
   */
  public InputParameterDescription(String name, String description, boolean mandatory, Type type,
      Class<?> clazz) {
    this.name = name;
    this.description = description;
    this.mandatory = mandatory;
    this.typeDescription = JsonSchemaGenerator.generateTypeDescription(type, clazz);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }

    final InputParameterDescription that = (InputParameterDescription) o;

    return (this.name != null && that.name != null && this.name.equals(that.name)
        && this.description != null && that.description != null
        && this.description.equals(that.description) && this.typeDescription != null
        && that.typeDescription != null && this.typeDescription.equals(that.typeDescription));
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final HashCodeBuilder builder = new HashCodeBuilder(17, 31);
    builder.append(this.id);
    builder.append(this.name);
    builder.append(this.mandatory);
    builder.append(this.description);
    builder.append(this.typeDescription);
    return builder.toHashCode();
  }

}
