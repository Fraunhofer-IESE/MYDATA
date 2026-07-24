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

package de.fraunhofer.iese.mydata.pdp;

import de.fraunhofer.iese.mydata.component.information.method.InputParameterDescription;
import de.fraunhofer.iese.mydata.component.information.method.MethodInterfaceDescription;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyExecutionPoint;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyParameter;
import de.fraunhofer.iese.mydata.pdp.language.model.function.IFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.ExecuteFunction;
import de.fraunhofer.iese.mydata.policy.decision.ExecuteAction;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * The Class PxpExecutor.
 */
public class PxpExecutor implements Callable<Boolean> {

  /**
   * The log.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PxpExecutor.class);

  /**
   * The execute action.
   */
  private final ExecuteFunction executeAction;

  /**
   * The evt.
   */
  private final Event evt;

  /**
   * Instantiates a new PXP executor.
   *
   * @param actionToExecute the action to execute
   * @param e the e
   */
  public PxpExecutor(ExecuteFunction actionToExecute, Event e) {
    this.executeAction = actionToExecute;
    this.evt = e;
  }

  /*
   * (non-Javadoc)
   * @see java.util.concurrent.Callable#call()
   */
  @Override
  public Boolean call() throws Exception {
    LOG.debug("Going to execute action with urn [{}]", this.executeAction.getName());

    final ParameterList finalParamList = new ParameterList();

    final MethodInterfaceDescription pxpQuery = new MethodInterfaceDescription(this.executeAction.getName(), Boolean.class, "");
    for (final IFunction param : this.executeAction.getParameters()) {
      try {
        final DataObject<?> paramEvalResult = param.evaluate(this.evt);
        final Parameter<?> convertedParameter = new Parameter<>(param.getName(), paramEvalResult.getValue());
        finalParamList.add(convertedParameter);
        if (param instanceof PolicyParameter) {
          final PolicyParameter<?> p = (PolicyParameter<?>)param;
          pxpQuery.addParameter(new InputParameterDescription(p.getName(), "", paramEvalResult.getType()));
        }
      } catch (final Exception e) {
        LOG.warn("Could not evaluate: {}", e.getMessage(), e);
      }
    }

    try {
      final IPolicyExecutionPoint pxpExecutionPoint = PolicyDecisionPoint.getInstance().getConnectorCache().getPxpConnectionFromCache(pxpQuery, SolutionId.fromActionId(evt.getActionId()));
      if (null == pxpExecutionPoint) {
        throw new IOException("Unable to retrieve PXP connector and triggering PXP!");
      }
      LOG.trace("Triggering execution: {}", this.executeAction.getName());
      final ExecuteAction convertedAction = new ExecuteAction(new ActionId(this.executeAction.getName()), finalParamList);
      final boolean executionResult = pxpExecutionPoint.execute(convertedAction);
      LOG.info("PXP executed with result: {}", executionResult);
      return executionResult;
    } catch (IOException | RuntimeException e) {
      LOG.warn("Exception occured while retrieving PXP connector and triggering PXP: {}", e.getMessage(), e);
      return Boolean.FALSE;
    }
  }

  /**
   * Gets the execute action.
   *
   * @return the execute action
   */
  public ExecuteFunction getExecuteAction() {
    return this.executeAction;
  }
}
