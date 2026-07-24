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

package de.fraunhofer.iese.mydata.pdp.language.parser;

import de.fraunhofer.iese.mydata.pdp.language.model.Policy;

import java.io.File;

/**
 * A simple parser interface which offers methods to parse a policy from a file
 * or string.
 */
public interface IPolicyParser {
  /**
   * Method definition to parse a policy from a file.
   *
   * @param file A file object describing the policy.
   * @return A policy object which has been parsed out of the file.
   * @throws IllegalArgumentException In case of any error
   */
  Policy parsePolicyFile(File file) throws IllegalArgumentException;

  /**
   * Method definition to parse a policy from a string.
   *
   * @param document A string object containing the policy as text.
   * @return A policy object which has been parsed out of the string.
   * @throws IllegalArgumentException In case of any error
   */
  Policy parsePolicyText(String document) throws IllegalArgumentException;

}
