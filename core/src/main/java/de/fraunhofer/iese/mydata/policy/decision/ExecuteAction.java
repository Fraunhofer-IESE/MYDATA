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

package de.fraunhofer.iese.mydata.policy.decision;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterListProvider;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * An action to be performed. Actions have a name and a set of name-value parameters.
 */
@Getter
public class ExecuteAction extends MyDataEntity implements ParameterListProvider {

  private static final Logger LOG = LoggerFactory.getLogger(ExecuteAction.class);

  /**
   * The component_id.
   */
  @NotNull
  @Valid
  private final ActionId id;

  /**
   * The parameters.
   */
  @Valid
  @NotNull
  private final ParameterList parameters = new ParameterList();

  /**
   * Instantiates a new execute action.
   *
   * @param actionId the action component_id
   * @param params   the params
   */
  public ExecuteAction(ActionId actionId, ParameterList params) {
    this.id = Objects.requireNonNull(actionId);
    this.setParameters(params);
  }

  /**
   * Instantiates a new execute action.
   *
   * @param actionId the action component_id
   */
  public ExecuteAction(String actionId) {
    this(new ActionId(actionId), null);
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.api.policy.parameter.ParameterListProvider#
   * addParameter(de.fraunhofer.iese.mydata.api.policy.parameter.Parameter)
   */
  @Override
  public void addParameter(Parameter<?> param) {
    this.parameters.add(param);
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.api.policy.parameter.ParameterListProvider#
   * addParameter(java.lang.String, java.lang.Object)
   */
  @Override
  public <T> void addParameter(String name, T value) {
    this.parameters.add(new Parameter<>(name, value));
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.api.policy.parameter.ParameterListProvider# clearParameters()
   */
  @Override
  public void clearParameters() {
    this.parameters.clear();
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ExecuteAction)) {
      return false;
    }

    final ExecuteAction exec2 = (ExecuteAction) obj;
    LOG.debug("Comparing executeAction: {}", this.getId());

    if (!this.id.equals(exec2.getId())) {
      return false;
    }

    if (this.parameters.size() != exec2.parameters.size()) {
      return false;
    }

    for (final Parameter<?> curParam : this.parameters) {
      LOG.debug("Comparing param: {}", curParam.getName());
      if (!curParam.equals(exec2.getParameters().getParameterForName(curParam.getName()))) {
        LOG.debug("param {} differ.", curParam.getName());
        return false;
      }
    }

    return true;
  }

  @Override
  public Parameter<?> getParameterForName(String name) {
    return this.parameters.getParameterForName(name);
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final HashCodeBuilder builder = new HashCodeBuilder(17, 31);
    builder.append(this.id);
    for (final Parameter<?> p : this.parameters) {
      builder.append(p.getName());
      builder.append(p.getValue());
    }
    return builder.toHashCode();
  }

  @Override
  public void setParameters(ParameterList params) {
    this.parameters.setParameters(params);
  }

  @Override
  public <T> T getParameterValue(String name, Class<T> clazz) {
    return this.getParameters().getParameterValue(name, clazz);
  }

  @Override
  public void removeParameter(String name) {
    this.getParameters().removeParameter(name);
  }
}
