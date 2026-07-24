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
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterListProvider;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.ZoneId;

/**
 * An event that occured in the system under observation, intercepted or monitored by a
 * PolicyEnforcementPoint.
 *
 * @author Fraunhofer IESE
 */

@Getter
public class Event extends MyDataEntity implements ParameterListProvider {

  /**
   * The component_id of the system action that occurred.
   */
  @NotNull
  @Valid
  private final ActionId actionId;

  /**
   * The time at which the action occurred / was intercepted by the PEP.
   */
  @NotNull
  private Long timestamp;

  /**
   * The parameters.
   */
  @Valid // TODO not enough
  private ParameterList parameters = new ParameterList();

  // TODO move that PDP specific stuff to a PDP specific event class...
  /** The zone id. */
  @Setter
  private transient ZoneId zoneId;

  /** The policy id. */
  @Valid
  @Setter
  private transient PolicyId policyId;

  /**
   * Instantiates a new event.
   *
   * @param actionId the action component_id
   * @param instant  the instant, will set the timestamp
   * @param params   the params
   */
  public Event(ActionId actionId, Instant instant, Parameter<?>... params) {
    this(actionId, instant, new ParameterList(params));
  }

  /**
   * Instantiates a new event.
   *
   * @param actionId the action component_id
   * @param instant  the instant, will set the timestamp
   * @param params   the params
   */
  public Event(ActionId actionId, Instant instant, ParameterList params) {
    if (actionId == null) {
      throw new IllegalArgumentException("ActionId may not be null");
    }
    this.actionId = actionId;
    this.setTimestamp(instant);
    this.setParameters(params);
  }

  /**
   * Instantiates a new event.
   *
   * @param actionId the action component_id
   * @param isTry    the is try
   * @param params   the params
   */
  public Event(ActionId actionId, boolean isTry, Parameter<?>... params) {
    this(actionId, Instant.now(), params);
  }

  /**
   * Instantiates a new event.
   *
   * @param actionId the action component_id
   * @param params   the params
   */
  public Event(ActionId actionId, Parameter<?>... params) {
    this(actionId, Instant.now(), params);
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.api.policy.parameter.ParameterListProvider#
   * addParameter(de.fraunhofer.iese.mydata.api.policy.parameter.Parameter)
   */
  @Override
  public void addParameter(Parameter<?> param) {
    if (this.parameters == null) {
      this.parameters = new ParameterList();
    }
    this.parameters.add(param);
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.api.policy.parameter.ParameterListProvider#
   * addParameter(java.lang.String, java.lang.Object)
   */
  @Override
  public <T> void addParameter(String name, T value) {
    if (this.parameters == null) {
      this.parameters = new ParameterList();
    }
    this.parameters.add(new Parameter<>(name, value));
  }

  /**
   * Clear parameters.
   */
  @Override
  public void clearParameters() {
    this.parameters.clear();
  }

  /**
   * Gets the time at which the action occurred / was intercepted by the PEP.
   *
   * @return the milliseconds since epoch
   */
  public Long getMillisecondSinceEpoch() {
    return this.timestamp;
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.api.policy.parameter.ParameterListProvider#
   * getParameterForName(java.lang.String)
   */
  @Override
  public Parameter<?> getParameterForName(String name) {
    if (this.parameters == null) {
      return null;
    }
    return this.parameters.getParameterForName(name);
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.api.policy.parameter.ParameterListProvider#
   * getParameterValue(java.lang.String, java.lang.Class)
   */
  @Override
  public <T> T getParameterValue(String name, Class<T> clazz) {
    if (this.parameters == null) {
      return null;
    }
    return this.parameters.getParameterValue(name, clazz);
  }

  /**
   * Gets the object which is added as parameter for given name.
   *
   * @param  name the name
   * @return      the object, null in case the parameter with the given name does not exist
   */
  public Object getValueForName(String name) {
    final Parameter<?> param = this.getParameterForName(name);
    if (null != param) {
      return param.getValue();
    } else {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.api.policy.parameter.ParameterListProvider#
   * removeParameter(java.lang.String)
   */
  @Override
  public void removeParameter(String name) {
    this.parameters.remove(this.getParameterForName(name));
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.api.policy.parameter.ParameterListProvider#
   * setParameters(de.fraunhofer.iese.mydata.api.policy.parameter. ParameterList)
   */
  @Override
  public void setParameters(ParameterList params) {
    if (this.parameters == null) {
      this.parameters = new ParameterList();
    }
    this.parameters.setParameters(params);
  }

  /**
   * Sets the time at which the action occurred / was intercepted by the PEP.
   *
   * @param instant the instant to set
   */
  public void setTimestamp(Instant instant) {
    this.timestamp = instant.toEpochMilli();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.actionId == null) ? 0 : this.actionId.hashCode());
    result = prime * result + ((this.parameters == null) ? 0 : this.parameters.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final Event other = (Event) obj;
    if (this.actionId == null) {
      if (other.actionId != null) {
        return false;
      }
    } else if (!this.actionId.equals(other.actionId)) {
      return false;
    }
    if (this.parameters == null) {
      if (other.parameters != null) {
        return false;
      }
    } else if (!this.parameters.equals(other.parameters)) {
      return false;
    }
    return true;
  }
}
