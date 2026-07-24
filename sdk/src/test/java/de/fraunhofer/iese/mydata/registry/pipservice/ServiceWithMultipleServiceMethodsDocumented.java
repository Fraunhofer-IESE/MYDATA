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

package de.fraunhofer.iese.mydata.registry.pipservice;

import de.fraunhofer.iese.mydata.registry.ActionDescription;
import de.fraunhofer.iese.mydata.registry.ActionParameterDescription;

public class ServiceWithMultipleServiceMethodsDocumented {

  /**
   * With param documented name.
   *
   * @param  originalParam the original param
   * @return               the string
   */
  @ActionDescription(description = "some method")
  public String withParamDocumentedName(
      @ActionParameterDescription(name = "documentedName") String originalParam) {
    return "hallo";
  }

  /**
   * With param multiple parameters.
   *
   * @param  originalParam the original param
   * @param  param2        the param 2
   * @return               the string
   */
  @ActionDescription(description = "some method 2")
  public String withParamMultipleParameters(
      @ActionParameterDescription(name = "documentedName") String originalParam,
      @ActionParameterDescription(name = "param2", description = "some description") String param2) {
    return "hallo";
  }

  /**
   * With param multiple patterns.
   *
   * @param  originalParam the original param
   * @return               the string
   */
  @ActionDescription(description = "some method 3")
  public String withParamMultiplePatterns(
      @ActionParameterDescription(name = "documentedName") String originalParam) {
    return "hallo";
  }
}
