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
import de.fraunhofer.iese.mydata.component.information.PipComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PxpComponentInformation;

import com.google.common.base.Objects;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.OrderColumn;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Describes an interface of a method. Contains of a name, a list of parameters and a return type.
 */
@Entity
@Getter
@Setter
public class MethodInterfaceDescription extends MyDataEntity {

  /**
   * The component_id.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "method_interface_description_id")
  protected Long id;

  /**
   * The name of the method.
   */
  @NotBlank
  protected String methodName;

  /**
   * A description of the method.
   */
  @Lob
  @JdbcTypeCode(Types.LONGVARCHAR)
  protected String description;

  /**
   * The return type of the method.
   */
  @NotBlank
  protected String returnType;

  @Hide
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pip_component_id")
  protected PipComponentInformation pipComponentInformation;

  @Hide
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pep_component_id")
  protected PepComponentInformation pepComponentInformation;

  @Hide
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pxp_component_id")
  protected PxpComponentInformation pxpComponentInformation;

  /**
   * A list of parameters.
   */
  @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
  @JoinColumn(name = "method_interface_description_id")
  @Fetch(FetchMode.SUBSELECT)
  @OrderColumn(name = "parameter_order")
  private List<@Valid InputParameterDescription> parameters;
  // normally set is sufficient. however at runtime, we do not know the parameter names
  // (compiler removes them). Thus a list (with ordered elements) is needed.

  /**
   * Instantiates a new method interface description.
   */
  protected MethodInterfaceDescription() {
    // required by JPA
  }

  /**
   * Instantiates a new interface description.
   *
   * @param methodName  name of the method
   * @param returnType  return type of the method
   * @param description description of the method
   */
  public MethodInterfaceDescription(String methodName, Class<?> returnType, String description) {
    this(methodName, returnType, description, Collections.emptyList());
  }

  /**
   * Instantiates a new interface description.
   *
   * @param methodName  name of the method
   * @param returnType  return type of the method
   * @param description description of the method
   * @param parameters  a list of parameters for the method
   */
  public MethodInterfaceDescription(String methodName, Class<?> returnType, String description,
      InputParameterDescription... parameters) {
    this(methodName, returnType, description, Arrays.asList(parameters));
  }

  /**
   * Instantiates a new interface description.
   *
   * @param methodName  name of the method
   * @param returnType  return type of the method
   * @param description description of the method
   * @param parameters  a list of parameters for the method
   */
  public MethodInterfaceDescription(String methodName, Class<?> returnType, String description,
      List<InputParameterDescription> parameters) {
    this.description = description;
    this.methodName = methodName;
    this.parameters = new ArrayList<>();
    this.parameters.addAll(parameters);

    this.setReturnType(returnType);
    // TODO Check difference with this.returnType =
    // returnType.getClass().getCanonicalName();

  }

  /**
   * Adds the parameter.
   *
   * @param param the param
   */
  public void addParameter(InputParameterDescription param) {
    this.parameters.add(param);
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof MethodInterfaceDescription mid) {
      if (!Objects.equal(this.getReturnType(), mid.getReturnType())) {
        return false;
      }
      if (!Objects.equal(this.getMethodName(), mid.getMethodName())) {
        return false;
      }
      if (!Objects.equal(this.getDescription(), mid.getDescription())) {
        return false;
      }
      return Objects.equal(this.parameters, mid.getParameters());
    }
    return false;
  }

  /**
   * Gets the return type of the method.
   *
   * @return                        the return type of the method
   * @throws ClassNotFoundException
   */
  public Class<?> getReturnTypeClass() throws ClassNotFoundException {
    return TypeByName.getClassForName(this.returnType);
  }

  /**
   * @param clazz
   */
  public void setReturnType(Class<?> clazz) {
    this.returnType = clazz.getCanonicalName();
  }

  @Override
  public int hashCode() {
    final HashCodeBuilder builder = new HashCodeBuilder(17, 31);
    builder.append(this.id);
    builder.append(this.methodName);
    builder.append(this.returnType);
    builder.append(this.parameters);
    builder.append(this.description);
    return builder.toHashCode();
  }

  /**
   * @return the list of inputparametersdescription elements
   */
  public List<InputParameterDescription> getParameters() {
    if (this.parameters == null) {
      this.parameters = new ArrayList<>();
    }
    return this.parameters;
  }

  /**
   * @param parameters
   */
  public void setParameters(List<InputParameterDescription> parameters) {
    this.getParameters();
    this.parameters.clear();
    this.parameters.addAll(parameters);
  }
}
