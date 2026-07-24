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

package de.fraunhofer.iese.mydata.policy.parameter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * The Class ParameterList.
 *
 * @author Fraunhofer IESE
 */
public class ParameterList extends ArrayList<Parameter<?>> {

  /**
   * The Constant serialVersionUID.
   */
  private static final long serialVersionUID = 7913615319509313890L;

  /**
   * Instantiates a new parameter list.
   */
  public ParameterList() {
    super();
  }

  /**
   * Instantiates a new parameter list.
   *
   * @param params the params
   */
  public ParameterList(Collection<? extends Parameter<?>> params) {
    super();
    if (params != null) {
      this.addAll(params);
    }
  }

  /**
   * Instantiates a new parameter list.
   *
   * @param params the params
   */
  public ParameterList(Parameter<?>... params) {
    super();
    if (params != null) {
      this.addAll(Arrays.asList(params));
    }
  }

  /*
   * (non-Javadoc)
   * @see java.util.ArrayList#add(java.lang.Object)
   */
  @Override
  public boolean add(Parameter<?> e) {
    if (e != null) {
      for (final Parameter<?> param : this) {
        if (e.getName().equals(param.getName())) {
          return false;
        }
      }
      return super.add(e);
    }
    return false;
  }

  /**
   * Adds the parameter.
   *
   * @param <T>   the generic type
   * @param name  the name
   * @param value the value
   */
  public <T> void addParameter(String name, T value) {
    this.add(new Parameter<>(name, value));
  }

  /**
   * Adds the parameter.
   *
   * @param <T>       the generic type
   * @param name      the name
   * @param value     the value
   * @param valueType the value type
   */
  public <T> void addParameter(String name, T value, Type valueType) {
    this.add(new Parameter<>(name, value, valueType));
  }

  /**
   * Gets the parameter for name.
   *
   * @param name the name
   * @return the parameter for name
   */
  public Parameter<?> getParameterForName(String name) {
    if (name != null) {
      for (final Parameter<?> param : this) {
        if (name.equals(param.getName())) {
          return param;
        }
      }
    }
    return null;
  }

  /**
   * Gets the parameter value.
   *
   * @param <T>   the generic type
   * @param name  the name
   * @param clazz the clazz
   * @return the parameter value
   */
  @SuppressWarnings("unchecked")
  public <T> T getParameterValue(String name, Class<T> clazz) {
    final Parameter<?> param = this.getParameterForName(name);
    if (param == null) {
      return null;
    }

    final Object value = param.getValue();
    if (clazz.isAssignableFrom(value.getClass())) {
      return (T) value;
    }
    throw new ClassCastException("Parameter of type " + value.getClass().getCanonicalName() + " cannot be cast to " + clazz);
  }

  /**
   * Gets the parameter value for name.
   *
   * @param name the name
   * @return the parameter value for name
   */
  public Object getParameterValueForName(String name) {
    final Parameter<?> prameter = this.getParameterForName(name);
    if (prameter != null) {
      return prameter.getValue();
    } else {
      return null;
    }
  }

  /**
   * Removes the parameter.
   *
   * @param name the name
   */
  public void removeParameter(String name) {
    this.remove(this.getParameterForName(name));
  }

  /**
   * Sets the parameters.
   *
   * @param params the new parameters
   */
  public void setParameters(ParameterList params) {
    if (params != null) {
      this.clear();
      this.addAll(params);
    }
  }

  /*
   * (non-Javadoc)
   * @see java.util.ArrayList#toArray()
   */
  @Override
  public Parameter<?>[] toArray() {
    return this.toArray(new Parameter<?>[0]);
  }

}
