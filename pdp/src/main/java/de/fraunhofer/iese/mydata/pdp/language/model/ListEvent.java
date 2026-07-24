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

package de.fraunhofer.iese.mydata.pdp.language.model;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.util.List;

@XmlRootElement(name = "list", namespace = "http://www.mydata-control.de/4.0/event")
@SuppressWarnings({
    "javadoc", "rawtypes"
})
public class ListEvent extends PolicyEvent<List> {

  /**
   *
   */
  private static final long serialVersionUID = 4624689903918220410L;

  /**
   *
   */

  public ListEvent() {
    super(List.class);
  }

  public ListEvent(String name) {
    this();
    super.setName(name);
  }

  @XmlAttribute
  @Override
  @XmlJavaTypeAdapter(ParameterListAdapter.class)
  public List getValue() {
    return super.getValue();
  }

  @Override
  public void setValue(List value) {
    super.setValue(value);
  }
}
