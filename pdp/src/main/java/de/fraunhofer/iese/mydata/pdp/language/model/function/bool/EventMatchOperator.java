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
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyParameter;
import de.fraunhofer.iese.mydata.pdp.language.model.function.Function;
import de.fraunhofer.iese.mydata.pdp.language.model.function.IFunction;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * The Class EventMatchOperator.
 */
public class EventMatchOperator extends Function {

  /**
   * The action.
   */
  private String action;

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(EventMatchOperator.class);

  /**
   * Instantiates a new event match operator.
   */
  public EventMatchOperator() {
    super();
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.pdp.java.language.model.operator.Operator#
   * evaluate(de.fraunhofer.iese.mydata.api.policy.Event)
   */
  @SuppressWarnings("unchecked")
  @Override
  public DataObject<Boolean> evaluate(Event evt) throws EvaluationUndecidableException {
    LOG.debug("Entering evaluate(name={}, event={})", this.getName(), evt.getActionId());

    if (!this.getAction().equals(evt.getActionId().getUrn())) {
      LOG.debug("Action [{}] does not match action [{}] in event object.", this.getAction(),
          evt.getActionId());
      LOG.debug("Leaving evaluate(): false.");
      return PolicyConstant.FALSE;
    }
    LOG.debug("Event matches action");

    if (this.parameters != null) {
      for (final IFunction param : this.getParameters()) {
        if (param instanceof PolicyParameter) {
          LOG.debug("Try to match event param: {}", param);
          if (!this.compareParameters((PolicyParameter<?>) param, evt)) {
            LOG.debug("Try to match event param [{}] failed.", param);
            LOG.debug("Leaving evaluate(): false.");

            return PolicyConstant.FALSE;
          }
        } else if (param instanceof Function) {
          LOG.debug("Try to match function with name: {}", ((Function) param).getName());
          final Function paramFunction = (Function) param;
          final DataObject<?> functionEvalResult = paramFunction.evaluate(evt);
          if (functionEvalResult.getType() == Boolean.class
              || Boolean.FALSE.equals(((DataObject<Boolean>) functionEvalResult).getValue())) {
            LOG.error(
                "Result of function is not of type boolean or evaluation result is false. Result: {}",
                ((DataObject<Boolean>) functionEvalResult).getValue());
            LOG.debug("Leaving evaluate(): false.");
            return PolicyConstant.FALSE;
          }
        } else {
          LOG.error("Eventmatch operator contains unsupported parameters");
          LOG.debug("Leaving evaluate(): false.");
          return PolicyConstant.FALSE;
        }
      }
    }
    LOG.info("Event match successful.");
    return PolicyConstant.TRUE;
  }

  /**
   * Compare parameters.
   *
   * @param  param                          the param
   * @param  evt                            the evt
   * @return                                true, if successful
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   */
  private boolean compareParameters(PolicyParameter<?> param, Event evt)
      throws EvaluationUndecidableException {
    if (param == null || evt == null) {
      return false;
    }

    final DataObject<?> left = param.evaluate(evt);
    final de.fraunhofer.iese.mydata.policy.parameter.Parameter<?> right = evt
        .getParameterForName(param.getName());

    if (param.isComplex()) {
      final JsonElement a = JsonParser.parseString(right.getValue().toString());
      final JsonElement b = JsonParser.parseString(left.getValue().toString());

      if (a != null && b != null) {
        return a.equals(b);
      }
      return false;

    } else {
      if (left != null && right != null) {
        return left.getValue().equals(right.getValue());
      } else if (left == null) {
        return true;
      }
      return false;
    }

  }

  /**
   * Adds the parameter.
   *
   * @param parameter the parameter
   */
  public void addParameter(PolicyParameter<?> parameter) {
    if (this.parameters == null) {
      this.parameters = new ArrayList<>();
    }
    this.parameters.add(parameter);
  }

  /**
   * Gets the action.
   *
   * @return the action
   */
  public String getAction() {
    return this.action;
  }

  /**
   * Sets the action.
   *
   * @param action the action to set
   */
  public void setAction(String action) {
    this.action = action;
  }

}
