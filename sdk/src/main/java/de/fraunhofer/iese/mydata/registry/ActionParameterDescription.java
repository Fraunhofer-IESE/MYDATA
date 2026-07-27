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

package de.fraunhofer.iese.mydata.registry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes the Parameter of an Method that is annotated with
 * {@link ActionDescription}. Some information are introspected by the Discovery
 * that registeres the Action at the PolicyManagementPoint. But for example the
 * parameter cannot be introspected in any situation. Up to Java 1.7 argument
 * names are dropped during compilation and in java 1.8 option preserve argument
 * names is not activated per default. Thus each instance of this
 * {@link ActionParameterDescription} should contain at least the name. For
 * example
 *
 * <pre>
 *   {@literal @ActionDescription}(description = "anagram the parameter")
 *    public String anagram(@ActionParameterDescription(name = "percentage",description = "The percentage you need to anagram") Integer percentage) {
 *      return "xxxx";
 *    }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface ActionParameterDescription {

  /**
   * Description of the Parameter.
   *
   * @return Description
   */
  String description() default "";

  /**
   * Mandatory parameter?.
   *
   * @return True if the policy have to specify the value for this parameter.
   */
  boolean mandatory() default false;

  /**
   * Name of the Parameter.
   *
   * @return The parameter name.
   */
  String name();
}
