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
 * The Class RegexFunction.
 */
public class RegexFunction extends BooleanFunction {

  /**
   * The logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(RegexFunction.class);

  /**
   * The match any.
   */
  private EMultiFunctionMode functionMode;

  /**
   * The regex.
   */
  private String regex;

  /**
   * Instantiates a new regex function.
   */
  public RegexFunction() {
    this.functionMode = EMultiFunctionMode.ALL;
    this.regex = null;
  }

  /**
   * Instantiates a new regex function.
   *
   * @param regex the regex
   * @param mode  the mode
   */
  public RegexFunction(String regex, EMultiFunctionMode mode) {
    super();
    this.regex = regex;
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

    if (this.getParameters().isEmpty()) {
      LOG.warn("Insufficient parameters for concat function");
      throw new IllegalArgumentException("Need Strings to match");
    }

    LOG.debug("Mode is {}", this.functionMode);
    int numberOfMatches = 0;
    for (int i = 0; i < this.getParameters().size(); i++) {
      final IFunction param = this.getParameters().get(i);
      final DataObject<?> paramToCheckObject = param.evaluate(e);
      if (null == paramToCheckObject) {
        LOG.info("The String to be analyzed with the regexp is null!!");
      }

      if (paramToCheckObject == null || paramToCheckObject.getType() != String.class) {
        LOG.warn("Parameter must be a String, but is of type {}",
            paramToCheckObject == null ? "null" : paramToCheckObject.getType());
        throw new IllegalArgumentException("Only string parameters can be matched.");
      }

      final boolean matches = ((String) paramToCheckObject.getValue()).matches(this.regex);
      final Object paramName = paramToCheckObject.getValue();
      if (matches && this.functionMode == EMultiFunctionMode.AT_LEAST_ONE) {
        LOG.debug("Parameter {} matches and mode is AT_LEAST_ONE", paramName);
        LOG.debug("Leaving evaluate(): true");
        return PolicyConstant.TRUE;

      } else if (!matches && this.functionMode == EMultiFunctionMode.ALL) {
        LOG.debug("Parameter {} does not match, but mode is ALL", paramName);
        LOG.debug("Leaving evaluate(): false");
        return PolicyConstant.FALSE;

      } else if (matches && this.functionMode == EMultiFunctionMode.NONE) {
        LOG.debug("Parameter {} matches, but mode is NONE", paramName);
        LOG.debug("Leaving evaluate(): false");
        return PolicyConstant.FALSE;

      } else if (matches && this.functionMode == EMultiFunctionMode.EXACTLY_ONE) {
        if (numberOfMatches > 0) {
          LOG.debug("More than one parameter matches but mode is EXACTLY_ONE");
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
   * Gets the regex.
   *
   * @return the regex
   */
  @XmlAttribute(name = "regex")
  public String getRegex() {
    return this.regex;
  }

  /**
   * Sets the regex
   * 
   * @param regex
   */
  public void setRegex(String regex) {
    this.regex = regex;
  }

  /**
   * Gets the match any.
   *
   * @return the match any
   */
  @XmlAttribute(name = "mode")
  public EMultiFunctionMode getFunctionMode() {
    return this.functionMode;
  }

  public void setFunctionMode(EMultiFunctionMode functionMode) {
    this.functionMode = functionMode;
  }
}
