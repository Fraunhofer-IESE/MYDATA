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

import jakarta.xml.bind.ValidationEventHandler;
import jakarta.xml.bind.annotation.DomHandler;

import java.io.StringWriter;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;

@SuppressWarnings("javadoc")
public class DecisionAdapter implements DomHandler<Boolean, StreamResult> {

  private static final String INHIBIT_START_TAG = "<inhibit>";

  private final StringWriter xmlWriter = new StringWriter();

  @Override
  public StreamResult createUnmarshaller(ValidationEventHandler errorHandler) {
    return new StreamResult(this.xmlWriter);
  }

  @Override
  public Boolean getElement(StreamResult rt) {
    final String xml = rt.getWriter().toString();
    final int inhibitIndex = xml.indexOf(INHIBIT_START_TAG);

    return inhibitIndex >= 0 ? false : true;
  }

  @Override
  public Source marshal(Boolean aBoolean, ValidationEventHandler validationEventHandler) {
    return null;
  }
}
