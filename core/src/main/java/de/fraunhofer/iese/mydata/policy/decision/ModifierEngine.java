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
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterListProvider;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/**
 * A modifier engine describes a method that can be performed on event parameters. Despite an engine
 * name it contains a list of parameters required for the processing the engine.
 */
@Getter
public class ModifierEngine extends MyDataEntity implements ParameterListProvider {
  /**
   * The engine name.
   */
  @NotBlank
  private final String method;

  /**
   * List of parameters modifiers for this modifier engine; use map here to guarantee, that every
   * parameter can only exist once!.
   */
  @Valid
  private final ParameterList parameters = new ParameterList();

  /**
   * Instantiates a new modifier engine.
   *
   * @param name the name of the parameter
   */
  public ModifierEngine(String name) {
    this(name, null);
  }

  /**
   * Instantiates a new modifier engine.
   *
   * @param name   the name of the parameter
   * @param params the parameter values
   */
  public ModifierEngine(String name, ParameterList params) {
    this.method = name;
    this.setParameters(params);
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
   * @see de.fraunhofer.iese.mydata.api.policy.parameter.ParameterListProvider#
   * getParameterForName(java.lang.String)
   */

  @Override
  public Parameter<?> getParameterForName(String name) {
    return this.parameters.getParameterForName(name);
  }
  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.api.policy.parameter.ParameterListProvider#
   * getParameterValue(java.lang.String, java.lang.Class)
   */

  @Override
  public <T> T getParameterValue(String name, Class<T> clazz) {
    return this.parameters.getParameterValue(name, clazz);
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.api.policy.parameter.ParameterListProvider#
   * removeParameter(java.lang.String)
   */
  @Override
  public void removeParameter(String name) {
    this.parameters.removeParameter(name);
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.api.policy.parameter.ParameterListProvider#
   * setParameters(de.fraunhofer.iese.mydata.api.policy.parameter. ParameterList)
   */
  @Override
  public void setParameters(ParameterList params) {
    this.parameters.setParameters(params);
  }

}
