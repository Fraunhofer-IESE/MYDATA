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

import de.fraunhofer.iese.mydata.pdp.language.model.PolicyConstant;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class EqualsFunction checks if the provided elements are equal to each other.
 * <p>
 * A list of numerical numbers is compared based on double precision, all other values are compared
 * using the Object.equals() function.
 * </p>
 */
@XmlRootElement(name = "equals", namespace = "http://www.mydata-control.de/4.0/mydataLanguage")
public class EqualsFunction extends BooleanFunction {
  /**
   * The logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(EqualsFunction.class);

  /**
   * Instantiates a new equals function.
   */
  public EqualsFunction() {
    super();
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.pdp.java.language.model.function.BooleanFunction
   * #evaluate(de.fraunhofer.iese.mydata.api.policy.Event)
   */
  @Override
  public DataObject<Boolean> evaluate(Event e) throws EvaluationUndecidableException {
    LOG.debug("Entering evaluate(name={}, parameters={}", this.getName(), this.getParameters());
    LOG.trace("event={}", e);

    if (this.getParameters().isEmpty() || this.getParameters().size() < 2) {
      LOG.warn("At least two parameters required");
      throw new IllegalArgumentException("At least two parameters required");
    }

    final DataObject<?> baseLine = this.getParameters().get(0).evaluate(e);

    JsonElement a;
    JsonElement b;

    if (baseLine.isComplex()) {
      a = JsonParser.parseString(baseLine.getValue().toString());
    } else {
      a = new Gson().toJsonTree(baseLine.getValue());
    }

    int i = 1;
    do {
      final DataObject<?> toCompare = this.getParameters().get(i).evaluate(e);
      if (toCompare.isComplex()) {
        b = JsonParser.parseString(toCompare.getValue().toString());
      } else {
        b = new Gson().toJsonTree(toCompare.getValue());
      }

      if (!a.equals(b)) {
        return PolicyConstant.FALSE;
      }
      i++;
    } while (i < this.getParameters().size());

    LOG.debug("Leaving evaluate(): true");
    return PolicyConstant.TRUE;
  }
}
