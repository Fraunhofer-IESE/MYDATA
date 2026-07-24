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
import de.fraunhofer.iese.mydata.pdp.language.model.function.EMultiFunctionMode;
import de.fraunhofer.iese.mydata.pdp.language.model.function.IFunction;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;

import jakarta.xml.bind.annotation.XmlAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ExistsFunction checks if an element exists in the event.
 */
public class ExistsFunction extends BooleanFunction {
  /**
   * The logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ExistsFunction.class);

  /**
   * The function mode.
   */
  private EMultiFunctionMode functionMode;

  /**
   * Instantiates a new contains function.
   */
  public ExistsFunction() {
    this(EMultiFunctionMode.ALL);
  }

  /**
   * Instantiates a new contains function.
   *
   * @param mode the mode (match all or one)
   */
  public ExistsFunction(EMultiFunctionMode mode) {
    super();
    this.functionMode = mode;
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.pdp.java.language.model.function.BooleanFunction
   * #evaluate(de.fraunhofer.iese.mydata.api.policy.Event)
   */
  @Override
  public DataObject<Boolean> evaluate(Event e) throws EvaluationUndecidableException {
    LOG.debug("Entering evaluate(name={}, parameters={}, event={})", this.getName(),
        this.getParameters(), e);

    if (this.getParameters() == null || this.getParameters().isEmpty()) {
      LOG.warn("Nothing to check");
      throw new EvaluationUndecidableException("Nothing to check");
    }

    if (e == null) {
      LOG.debug("No event provided");
      LOG.debug("Leaving evaluate(): false");
      return PolicyConstant.FALSE;
    }

    LOG.debug("Mode is {}", this.functionMode);
    int numberOfMatches = 0;
    for (final IFunction param : this.getParameters()) {

      final DataObject<?> paramToCheckObject = param.evaluate(e);
      final String paramName = (String) paramToCheckObject.getValue();

      final boolean contained = e.getParameterForName(paramName) != null;
      LOG.debug("Contained: {} {}", paramName, contained);

      if (contained && this.functionMode == EMultiFunctionMode.AT_LEAST_ONE) {
        LOG.debug("Parameter {} found", paramName);
        LOG.debug("Leaving evaluate(): true");
        return PolicyConstant.TRUE;

      } else if (!contained && this.functionMode == EMultiFunctionMode.ALL) {
        LOG.debug("Parameter {} does not exist in the event", paramName);
        LOG.debug("Leaving evaluate(): false");
        return PolicyConstant.FALSE;

      } else if (contained && this.functionMode == EMultiFunctionMode.NONE) {
        LOG.debug("Parameter {} contained in the list, but mode is NONE", paramName);
        LOG.debug("Leaving evaluate(): false");
        return PolicyConstant.FALSE;

      } else if (contained && this.functionMode == EMultiFunctionMode.EXACTLY_ONE) {
        if (numberOfMatches > 0) {
          LOG.debug("More than one parameter contained in the list, but mode is EXACTLY_ONE");
          LOG.debug("Leaving evaluate(): false");
          return PolicyConstant.FALSE;
        }
        numberOfMatches++;
      }

    }

    if (this.functionMode == EMultiFunctionMode.EXACTLY_ONE) {
      final PolicyConstant<Boolean> result = numberOfMatches == 1 ? PolicyConstant.TRUE
          : PolicyConstant.FALSE;

      LOG.debug("{} parameters matched in EXACTLY_ONE mode", numberOfMatches);
      LOG.debug("Leaving evaluate(): {}", result.getValue());
      return result;
    }

    LOG.debug("Leaving evaluate(): {}", this.functionMode != EMultiFunctionMode.AT_LEAST_ONE);
    return this.functionMode == EMultiFunctionMode.AT_LEAST_ONE ? PolicyConstant.FALSE
        : PolicyConstant.TRUE;
  }

  /**
   * Gets the function mode.
   *
   * @return the function mode
   */
  @XmlAttribute(name = "mode")
  public EMultiFunctionMode getFunctionMode() {
    return this.functionMode;
  }

  @SuppressWarnings("javadoc")
  public void setFunctionMode(EMultiFunctionMode functionMode) {
    this.functionMode = functionMode;
  }
}
