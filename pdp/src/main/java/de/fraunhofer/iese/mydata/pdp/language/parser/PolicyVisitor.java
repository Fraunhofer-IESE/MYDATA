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
import de.fraunhofer.iese.mydata.pdp.language.model.VariableDeclaration;
import de.fraunhofer.iese.mydata.pdp.language.model.VariableReference;
import de.fraunhofer.iese.mydata.pdp.language.model.function.Function;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.ExecuteFunction;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.PipOperator;

public interface PolicyVisitor {
  void visit(Policy policy);

  void visit(VariableDeclaration variableDeclaration);

  void visit(PolicyMechanism policyMechanism);

  void visit(PolicyIfBlock policyIfBlock);

  void visit(PolicyDecision policyDecision);

  void visit(PolicyModify policyModify);

  <T> void visit(PolicyEvent<T> tPolicyEvent);

  <T> void visit(PolicyParameter<T> parameter);

  <T> void visit(PolicyConstant<T> tPolicyConstant);

  void visit(Function function);

  void visit(VariableReference varialbleReference);

  <T> void visit(PipOperator<T> pipOperator);

  void visit(ExecuteFunction executeFunction);

  void visit(EventOccurrence eventOccurrence);

}
