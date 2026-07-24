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

package de.fraunhofer.iese.mydata.pdp.language.parser;

import de.fraunhofer.iese.mydata.pdp.language.model.EventOccurrence;
import de.fraunhofer.iese.mydata.pdp.language.model.Policy;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyConstant;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyDecision;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyEvent;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyIfBlock;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyMechanism;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyModify;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyParameter;
import de.fraunhofer.iese.mydata.pdp.language.model.function.Function;
import de.fraunhofer.iese.mydata.pdp.language.model.function.IFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.ExecuteFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.PipOperator;

import java.util.List;

public abstract class AbstractPolicyVisitor implements PolicyVisitor {
  @Override
  public void visit(Policy policy) {
    policy.getVariableDeclarations().stream().forEach(variableDeclaration -> variableDeclaration.accept(this));
    policy.getMechanisms().stream().forEach(policyMechanism -> policyMechanism.accept(this));
  }

  @Override
  public void visit(PolicyMechanism policyMechanism) {
    policyMechanism.getIfBlock().accept(this);
    if (policyMechanism.getElseIfBlocks() != null) {
      policyMechanism.getElseIfBlocks().stream().forEach(policyIfBlock -> policyIfBlock.accept(this));
    }
    final PolicyDecision elseBlock = policyMechanism.getElseBlock();
    if (elseBlock != null) {
      elseBlock.accept(this);
    }

    policyMechanism.getExecuteActions().stream().forEach(executionAction -> executionAction.accept(this));
  }

  @Override
  public void visit(PolicyIfBlock policyIfBlock) {
    if (policyIfBlock.getOperator() != null) {
      policyIfBlock.getOperator().accept(this);
    }
    policyIfBlock.getDecision().accept(this);
  }

  @Override
  public void visit(PolicyDecision policyDecision) {
    policyDecision.getModifiers().stream().forEach(policyModify -> policyModify.accept(this));
    policyDecision.getExecuteActions().stream().forEach(executionAction -> executionAction.accept(this));
  }

  @Override
  public void visit(PolicyModify policyModify) {
    final List<PolicyParameter<?>> params = policyModify.getParams();
    if (params != null) {
      params.stream().forEach(policyParameter -> policyParameter.accept(this));
    }

  }

  @Override
  public <T> void visit(PolicyEvent<T> tPolicyEvent) {
    // Intended Blank -> has no children
  }

  @Override
  public <T> void visit(PolicyParameter<T> parameter) {
    if (parameter.getFunctionsAndParameters() != null) {
      parameter.getFunctionsAndParameters().stream().forEach(param -> param.accept(this));
    }
  }

  @Override
  public <T> void visit(PolicyConstant<T> tPolicyConstant) {

  }

  @Override
  public void visit(Function function) {
    function.getParameters().stream().forEach(parameter -> parameter.accept(this));
  }

  @Override
  public void visit(PipOperator pipOperator) {
    final List<IFunction> subOperators = pipOperator.getSubOperators();
    if (subOperators != null) {
      subOperators.stream().forEach(operator -> operator.accept(this));
    }
  }

  @Override
  public void visit(EventOccurrence eventOccurrence) {
    final List<IFunction> subOperators = eventOccurrence.getSubOperators();
    if (subOperators != null) {
      subOperators.stream().forEach(operator -> operator.accept(this));
    }
  }

  @Override
  public void visit(ExecuteFunction executeFunction) {
    final List<PolicyParameter<?>> parameters = executeFunction.getParameters();
    if (parameters != null) {
      parameters.stream().forEach(param -> param.accept(this));
    }
  }
}
