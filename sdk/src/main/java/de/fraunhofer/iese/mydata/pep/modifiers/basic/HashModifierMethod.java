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

package de.fraunhofer.iese.mydata.pep.modifiers.basic;

import de.fraunhofer.iese.mydata.pep.common.PrimitiveModifierMethod;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;
import de.fraunhofer.iese.mydata.registry.ActionDescription;
import de.fraunhofer.iese.mydata.registry.ActionParameterDescription;

import org.apache.commons.codec.digest.DigestUtils;

/***
 * This a default Modifiers method with Pep sdk which hashes a string
 */
public class HashModifierMethod extends PrimitiveModifierMethod {

  @Override
  public Object doModification(Object currentObject, ParameterList modifierMethodParameterList) {
    if (currentObject == null) {
      return null;
    }
    final String algorithm = modifierMethodParameterList.getParameterValue("algorithm", String.class);
    return this.hash(currentObject.toString(), algorithm);
  }

  @ActionDescription(description = "Replaces the string", pepSupportedType = Object.class)
  public Object hash(String currentObject,
      @ActionParameterDescription(name = "algorithm", description = "Hashing algorithm. Supported: SHA256 (default), MD5, SHA1", mandatory = false) String algorithm) {

    if ("md5".equalsIgnoreCase(algorithm)) {
      return DigestUtils.md5Hex(currentObject);
    }

    if ("sha1".equalsIgnoreCase(algorithm)) {
      return DigestUtils.sha1Hex(currentObject);
    }

    return DigestUtils.sha256Hex(currentObject);
  }

  @Override
  public String getDisplayName() {
    return "hash";
  }

}
