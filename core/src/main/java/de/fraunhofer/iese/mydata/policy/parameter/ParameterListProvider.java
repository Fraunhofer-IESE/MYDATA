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

/**
 * The Interface ParameterListProvider.
 */
public interface ParameterListProvider {

  /**
   * Adds a parameter.
   *
   * @param param the parameter to add
   */
  void addParameter(Parameter<?> param);

  /**
   * Adds a parameter.
   *
   * @param <T>   the generic type
   * @param name  the name of the parameter
   * @param value the value of the parameter
   */
  <T> void addParameter(String name, T value);

  /**
   * Removes all parameters.
   */
  void clearParameters();

  /**
   * Gets a parameter.
   *
   * @param  name the name of the parameter
   * @return      the parameter with the given name, or null if it does not exist
   */
  Parameter<?> getParameterForName(String name);

  /**
   * Gets the list of all parameters.
   *
   * @return the list of all parameters
   */
  ParameterList getParameters();

  /**
   * Gets a parameter value.
   *
   * @param  <T>   the generic type
   * @param  name  the name of the parameter
   * @param  clazz the value of the parameter
   * @return       the value of the parameter, or null if it does not exist
   */
  <T> T getParameterValue(String name, Class<T> clazz);

  /**
   * Adds a parameter.
   *
   * @param name the name of the parameter to be deleted
   */
  void removeParameter(String name);

  /**
   * Replaces all parameters with the parameters of the provided list.
   *
   * @param params list of parameters
   */
  void setParameters(ParameterList params);
}
