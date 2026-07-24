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

package de.fraunhofer.iese.mydata.pdp.language.model.function.bool;

import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * The Class PipOperator.
 */
@XmlRootElement

@SuppressWarnings("javadoc")
public class PipOperatorBoolean extends PipOperator<Boolean> {

  public PipOperatorBoolean() throws InvalidEntityException {
    this.returnType = Boolean.class;
  }

  @XmlAttribute(name = "default")
  @Override
  public Boolean getDefaultValue() {
    return super.getDefaultValue();
  }

  @Override
  public void setDefaultValue(Boolean defaultValue) {
    super.setDefaultValue(defaultValue);
    super.setDefaultReturnValue(new DataObject<Boolean>(defaultValue, Boolean.class));
  }
}
