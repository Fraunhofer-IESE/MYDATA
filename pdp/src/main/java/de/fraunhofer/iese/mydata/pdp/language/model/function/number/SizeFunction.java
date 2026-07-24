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

package de.fraunhofer.iese.mydata.pdp.language.model.function.number;

import de.fraunhofer.iese.mydata.pdp.language.model.PolicyConstant;
import de.fraunhofer.iese.mydata.pdp.language.model.function.Function;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.List;

/**
 * The Class SizeFunction.
 */
public class SizeFunction extends Function {

  /**
   * The logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(SizeFunction.class);

  /**
   * Instantiates a new size function.
   */
  public SizeFunction() {
  }

  /**
   * Instantiates a new size function.
   *
   * @param capacity the capacity
   */
  public SizeFunction(int capacity) {
    super(capacity, Integer.class);
  }

  /*
   * (non-Javadoc)
   * @see
   * de.fraunhofer.iese.mydata.pdp.java.language.model.IOperator#evaluate(de.
   * fraunhofer.iese.mydata.internal.policy.Event)
   */
  @Override
  public DataObject<Integer> evaluate(Event e) throws EvaluationUndecidableException {
    LOG.debug("Entering evaluate(name={}, parameters={}, event={})", this.getName(), this.getParameters(), (e == null ? "null" : e.getActionId()));

    if (this.getParameters().isEmpty()) {
      LOG.warn("Insufficient parameters for size function");
      throw new IllegalArgumentException("Insufficent parameters");
    }

    final Object o = this.getParameters().get(0).evaluate(e).getValue();

    int size;
    if (o.getClass().isArray()) {
      size = Array.getLength(o);

    } else if (o instanceof List) {
      size = ((List<?>)o).size();

    } else if (o instanceof String) {
      size = ((String)o).length();

    } else {
      LOG.warn("Parameter must be a String, Array or List, but is of type {}", o.getClass());
      throw new IllegalArgumentException("Parameter must be a String, Array or List, but is of type " + o.getClass());
    }

    LOG.debug("Leaving evaluate(): {}", size);
    return new PolicyConstant<>(size);

  }

}
