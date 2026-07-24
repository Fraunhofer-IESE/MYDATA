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

package de.fraunhofer.iese.mydata.pdp.language.model.function.string;


import de.fraunhofer.iese.mydata.pdp.language.model.function.Function;
import de.fraunhofer.iese.mydata.pdp.language.model.function.IFunction;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ConcatFunction concatinates its children (as a string value).
 */
public class ConcatFunction extends Function {

  /**
   * The logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ConcatFunction.class);

  /**
   * Instantiates a new concat function.
   */
  public ConcatFunction() {
  }

  /**
   * Instantiates a new concat function.
   *
   * @param capacity the expected, initial number of children
   */
  public ConcatFunction(int capacity) {
    super(capacity, String.class);
  }

  /*
   * (non-Javadoc)
   * @see
   * de.fraunhofer.iese.mydata.pdp.java.language.model.IOperator#evaluate(de.
   * fraunhofer.iese.mydata.internal.policy.Event)
   */
  @Override
  public DataObject<String> evaluate(Event e) throws EvaluationUndecidableException {
    LOG.debug("Entering evaluate(name={}, parameters={}, event={})", this.getName(), this.getParameters(), e);

    if (this.getParameters() == null || this.getParameters().isEmpty()) {
      LOG.debug("Leaving evaluate(): empty string");
      return new DataObject<>("");
    }

    final StringBuilder buffer = new StringBuilder();
    for (final IFunction param : this.getParameters()) {
      final DataObject<?> evalResult = param.evaluate(e);
      if (evalResult.getType() != String.class) {
        buffer.append(String.valueOf(evalResult.getValue()));
      } else {
        buffer.append((String)evalResult.getValue());
      }
    }
    final String result = buffer.toString();
    LOG.debug("Leaving evaluate(): {}", result);
    return new DataObject<>(result);
  }

}
