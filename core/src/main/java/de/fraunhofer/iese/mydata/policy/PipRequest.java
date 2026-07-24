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
import de.fraunhofer.iese.mydata.policy.event.InfoId;
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterListProvider;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A request that instructs the PIP.
 *
 * @author Fraunhofer IESE
 */
@Getter
public class PipRequest extends MyDataEntity implements ParameterListProvider {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PipRequest.class);

  /**
   * The id of the method to be evaluated.
   */
  @Valid
  @NotNull
  private final InfoId infoId;

  /**
   * A set of parameters that are used for evaluation.
   */
  @Valid
  private final ParameterList parameters;

  /**
   * Instantiates a new pip request.
   *
   * @param infoId the id of the method to be executed
   * @param params parameters used for evaluation
   */
  public PipRequest(InfoId infoId, Parameter<?>... params) {
    this(infoId, new ParameterList(params));
  }

  /**
   * Instantiates a new pip request.
   *
   * @param infoId the id of the method to be executed
   * @param params parameters used for evaluation
   */
  public PipRequest(InfoId infoId, ParameterList params) {
    this.infoId = infoId;
    this.parameters = new ParameterList();
    if (params != null) {
      this.parameters.addAll(params);
    }
  }

  @Override
  public void addParameter(Parameter<?> param) {
    this.parameters.add(param);
  }

  @Override
  public <R> void addParameter(String name, R value) {
    this.parameters.add(new Parameter<>(name, value));
  }

  @Override
  public void clearParameters() {
    this.parameters.clear();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof PipRequest)) {
      return false;
    }

    final PipRequest pip2 = (PipRequest) obj;
    LOG.debug("Comparing pipRequest: " + this.getInfoId());

    if (!this.infoId.equals(pip2.getInfoId())) {
      return false;
    }

    LOG.debug("Comparing parameter: this: {}; other: {}", this.parameters.size(),
        pip2.parameters.size());

    if (this.parameters.size() != pip2.parameters.size()) {
      return false;
    }

    for (final Parameter<?> curParam : this.parameters) {
      try {
        if (!curParam.equals(pip2.parameters.getParameterForName(curParam.getName()))) {
          LOG.debug("param {} differ: [{}] vs. [{}]", curParam.getName(), curParam.getValue(),
              pip2.parameters.getParameterForName(curParam.getName()).getValue());
          return false;
        }
      } catch (final NullPointerException e) {
        final String msg = "Param: " + curParam.getName() + " is NOT present.";
        LOG.debug(msg);
        LOG.trace(msg, e);
        return false;
      }
    }

    return true;
  }

  @Override
  public int hashCode() {
    final HashCodeBuilder builder = new HashCodeBuilder(17, 31);
    builder.append(this.getInfoId());
    final List<Parameter> copyOfParameterList = new ArrayList<>(this.parameters);
    // make sure that we use a fixed order of parameters here, as equal objects need equal hashcode!
    // equals does not respect parameter order so we must make sure that they get the same hashcode
    copyOfParameterList.sort(Comparator.comparing(Parameter::getName));
    for (final Parameter<?> p : this.parameters) {
      builder.append(p);
    }
    return builder.toHashCode();
  }

  @Override
  public Parameter<?> getParameterForName(String name) {
    return this.parameters.getParameterForName(name);
  }

  @Override
  public ParameterList getParameters() {
    return this.parameters;
  }

  @Override
  public void setParameters(ParameterList params) {
    this.parameters.setParameters(params);
  }

  @Override
  public <R> R getParameterValue(String name, Class<R> clazz) {
    return this.parameters.getParameterValue(name, clazz);
  }

  @Override
  public void removeParameter(String name) {
    this.parameters.removeParameter(name);
  }
}
