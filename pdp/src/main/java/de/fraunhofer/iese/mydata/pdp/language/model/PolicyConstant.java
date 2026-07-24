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

import de.fraunhofer.iese.mydata.pdp.language.model.function.IFunction;
import de.fraunhofer.iese.mydata.pdp.language.parser.PolicyVisitor;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;

import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;

/**
 * The Class Constant.
 *
 * @param <T> the generic type
 */
@XmlRootElement
@Getter
public class PolicyConstant<T> extends DataObject<T> implements IFunction {

  /**
   * The boolean Constant TRUE.
   */
  public static final PolicyConstant<Boolean> TRUE = new PolicyConstant<>(true);

  /**
   * The boolean Constant FALSE.
   */
  public static final PolicyConstant<Boolean> FALSE = new PolicyConstant<>(false);

  /**
   * The Constant serialVersionUID.
   */
  private static final long serialVersionUID = 2554666941944255554L;

  /**
   * Used for JAXB
   */
  public PolicyConstant() {
  }

  /**
   * Instantiates a new constant.
   *
   * @param clazz the clazz
   */
  public PolicyConstant(Class<T> clazz) {
    super(clazz);
  }

  /**
   * Instantiates a new constant.
   *
   * @param val the val
   */
  public PolicyConstant(T val) {
    super(val);
  }

  /**
   * Instantiates a new constant.
   *
   * @param val   the val
   * @param clazz the clazz
   */
  public PolicyConstant(T val, Class<T> clazz) {
    super(clazz);
    super.setValue(val);
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.pdp.java.language.model.IOperator#evaluate(de.
   * fraunhofer.iese.mydata.internal.policy.Event)
   */
  @Override
  public DataObject<?> evaluate(Event evt) throws EvaluationUndecidableException {
    return this;
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.api.policy.parameter.DataObject#setValue(java. lang.Object)
   */
  @Override
  public void setValue(T value) {
    throw new UnsupportedOperationException("Constants are final");
  }

  /**
   * To override value by subclass.
   *
   * @param value New Value
   */
  protected void setValueIntern(T value) {
    super.setValue(value);
  }

  @Override
  public void accept(PolicyVisitor policyVisitor) {
    policyVisitor.visit(this);
  }

  @Override
  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }
}
